package jp.co.webact.rssreader;

import android.app.ListActivity;
import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class RssSiteList extends ListActivity implements
		LoaderCallbacks<Cursor> {
	private static final String TAG = "RssSiteList";
	/**
     * Cursor adapter for controlling ListView results.
     */
    private SimpleCursorAdapter mAdapter;
	 /**
     * Projection for querying the content provider.
     */
    private static final String[] PROJECTION = new String[]{
            RssSite.Site._ID,
            RssSite.Site.COLUMN_NAME_TITLE,
            RssSite.Site.COLUMN_NAME_URL,
            RssSite.Site.COLUMN_NAME_GROUP
    };
    // The position of the title column in a Cursor returned by the provider.
    private static final int COLUMN_INDEX_TITLE = 1;
    /**
     * List of Cursor columns to read from when preparing an adapter to populate the ListView.
     */
    private static final String[] FROM_COLUMNS = new String[]{
        RssSite.Site.COLUMN_NAME_TITLE,
        RssSite.Site.COLUMN_NAME_URL,
        RssSite.Site.COLUMN_NAME_GROUP
    };

    /**
     * List of Views which will be populated by Cursor data.
     */
    private static final int[] TO_FIELDS = new int[]{
            R.id.site_title,
            R.id.site_url,
            R.id.site_group};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "start onCreate");
		setContentView(R.layout.rsssite_list);
		 // Gets the intent that started this Activity.
        Intent intent = getIntent();

        // If there is no data associated with the Intent, sets the data to the default URI, which
        // accesses a list of notes.
        if (intent.getData() == null) {
            intent.setData(RssSite.Site.CONTENT_URI);
        }
        getListView().setOnCreateContextMenuListener(this);
        //
        Log.d(TAG, "call SimpleCursorAdapter");
        mAdapter = new SimpleCursorAdapter(
                this,       // Current context
                R.layout.rsssitelist_item,  // Layout for individual rows
                null,                // Cursor
                FROM_COLUMNS,        // Cursor columns to use
                TO_FIELDS,           // Layout fields to use
                0                    // No flags
        );
        Log.d(TAG, "call setListAdapter");
        setListAdapter(mAdapter);

        // Prepare the loader.  Either re-connect with an existing one,
        // or start a new one.
        Log.d(TAG, "call initLoader");
        getLoaderManager().initLoader(0, null, (LoaderManager.LoaderCallbacks<Cursor>)this);
        Log.d(TAG, "end onCreate");

	}
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		Log.d(TAG, "start onCreateLoader");
		Log.d(TAG, "call CursorLoader");
		return new CursorLoader(
				this,
				RssSite.Site.CONTENT_URI,
				PROJECTION,
				null,
				null,
				RssSite.Site.DEFAULT_SORT_ORDER);
	}
	@Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
		Log.d(TAG, "start onLoadFinished");
        mAdapter.changeCursor(cursor);
		Log.d(TAG, "end onLoadFinished");
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
		Log.d(TAG, "start onLoaderReset");
        mAdapter.changeCursor(null);
		Log.d(TAG, "end onLoaderReset");
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		Log.d(TAG, "start onCreateOptionsMenu");
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.rsssitelist, menu);
		Intent intent = new Intent(null, getIntent().getData());
        intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
        Log.d(TAG, "call addIntentOptions");
        menu.addIntentOptions(Menu.CATEGORY_ALTERNATIVE, 0, 0,
                new ComponentName(this, RssSiteList.class), null, intent, 0, null);
		Log.d(TAG, "call onCreateOptionsMenu");
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		Log.d(TAG, "start onPrepareOptionsMenu");

		// The paste menu item is enabled if there is data on the clipboard.
		Log.d(TAG, "call getSystemService");
		ClipboardManager clipboard = (ClipboardManager)
				getSystemService(Context.CLIPBOARD_SERVICE);


		MenuItem mPasteItem = menu.findItem(R.id.menu_paste);

		// If the clipboard contains an item, enables the Paste option on the menu.
		if (clipboard.hasPrimaryClip()) {
			mPasteItem.setEnabled(true);
		} else {
			// If the clipboard is empty, disables the menu's Paste option.
			mPasteItem.setEnabled(false);
		}

//		// Gets the number of notes currently being displayed.
//		final boolean haveItems = getListAdapter().getCount() > 0;
//
//		// If there are any notes in the list (which implies that one of
//		// them is selected), then we need to generate the actions that
//		// can be performed on the current selection.  This will be a combination
//		// of our own specific actions along with any extensions that can be
//		// found.
//		if (haveItems) {
//			Log.d(TAG, "haveItems=true");
//			Log.d(TAG,"id=" + String.valueOf(getSelectedItemId()));
//			// This is the selected item.
//			Uri uri = ContentUris.withAppendedId(getIntent().getData(), getSelectedItemId());
//			Log.d(TAG, "uri="+uri.toString());
//
//			// Creates an array of Intents with one element. This will be used to send an Intent
//			// based on the selected menu item.
//			Intent[] specifics = new Intent[1];
//
//			// Sets the Intent in the array to be an EDIT action on the URI of the selected note.
//			specifics[0] = new Intent(Intent.ACTION_EDIT, uri);
//
//			// Creates an array of menu items with one element. This will contain the EDIT option.
//			MenuItem[] items = new MenuItem[1];
//
//			// Creates an Intent with no specific action, using the URI of the selected note.
//			Intent intent = new Intent(null, uri);
//
//			/* Adds the category ALTERNATIVE to the Intent, with the note ID URI as its
//			 * data. This prepares the Intent as a place to group alternative options in the
//			 * menu.
//			 */
//			intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
//
//			/*
//			 * Add alternatives to the menu
//			 */
//			Log.d(TAG, "call addIntentOptions");
//			menu.addIntentOptions(
//					Menu.CATEGORY_ALTERNATIVE,  // Add the Intents as options in the alternatives group.
//					Menu.NONE,                  // A unique item ID is not required.
//					Menu.NONE,                  // The alternatives don't need to be in order.
//					null,                       // The caller's name is not excluded from the group.
//					specifics,                  // These specific options must appear first.
//					intent,                     // These Intent objects map to the options in specifics.
//					Menu.NONE,                  // No flags are required.
//					items                       // The menu items generated from the specifics-to-
//					// Intents mapping
//					);
//			// If the Edit menu item exists, adds shortcuts for it.
//			if (items[0] != null) {
//
//				// Sets the Edit menu item shortcut to numeric "1", letter "e"
//				items[0].setShortcut('1', 'e');
//			}
//		} else {
//			Log.d(TAG, "haveItems=false");
//			
//			// If the list is empty, removes any existing alternative actions from the menu
//			menu.removeGroup(Menu.CATEGORY_ALTERNATIVE);
//		}

		// Displays the menu
		Log.d(TAG, "end onPrepareOptionsMenu");
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Log.d(TAG, "start onOptionsItemSelected");
        switch (item.getItemId()) {
        case R.id.menu_add:
          /*
           * Launches a new Activity using an Intent. The intent filter for the Activity
           * has to have action ACTION_INSERT. No category is set, so DEFAULT is assumed.
           * In effect, this starts the NoteEditor Activity in NotePad.
           */
           startActivity(new Intent(Intent.ACTION_INSERT, getIntent().getData()));
           Log.d(TAG, "end onOptionsItemSelected insert");
           return true;
        case R.id.menu_paste:
          /*
           * Launches a new Activity using an Intent. The intent filter for the Activity
           * has to have action ACTION_PASTE. No category is set, so DEFAULT is assumed.
           * In effect, this starts the NoteEditor Activity in NotePad.
           */
          startActivity(new Intent(Intent.ACTION_PASTE, getIntent().getData()));
          Log.d(TAG, "end onOptionsItemSelected paste");
          return true;
        default:
            return super.onOptionsItemSelected(item);
        }
	}
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		Log.d(TAG, "start onCreateContextMenu");
        // The data from the menu item.
        AdapterView.AdapterContextMenuInfo info;

        // Tries to get the position of the item in the ListView that was long-pressed.
        try {
            // Casts the incoming data object into the type for AdapterView objects.
            info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        } catch (ClassCastException e) {
            // If the menu object can't be cast, logs an error.
            Log.e(TAG, "bad menuInfo", e);
            return;
        }

        /*
         * Gets the data associated with the item at the selected position. getItem() returns
         * whatever the backing adapter of the ListView has associated with the item. In NotesList,
         * the adapter associated all of the data for a note with its list item. As a result,
         * getItem() returns that data as a Cursor.
         */
        Cursor cursor = (Cursor) getListAdapter().getItem(info.position);

        // If the cursor is empty, then for some reason the adapter can't get the data from the
        // provider, so returns null to the caller.
        if (cursor == null) {
            // For some reason the requested item isn't available, do nothing
    		Log.d(TAG, "end onCreateContextMenu cursor is null");
            return;
        }

        // Inflate menu from XML resource
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.list_context_menu, menu);
		Log.d(TAG, "title="+cursor.getString(COLUMN_INDEX_TITLE));

        // Sets the menu header to be the title of the selected note.
        menu.setHeaderTitle(cursor.getString(COLUMN_INDEX_TITLE));

        // Append to the
        // menu items for any other activities that can do stuff with it
        // as well.  This does a query on the system for any activities that
        // implement the ALTERNATIVE_ACTION for our data, adding a menu item
        // for each one that is found.
		Log.d(TAG, "id="+Integer.toString((int) info.id));
        Intent intent = new Intent(null, Uri.withAppendedPath(getIntent().getData(), 
                                        Integer.toString((int) info.id) ));
        intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
        menu.addIntentOptions(Menu.CATEGORY_ALTERNATIVE, 0, 0,
                new ComponentName(this, RssSiteList.class), null, intent, 0, null);
		Log.d(TAG, "end onCreateContextMenu");
	}
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		Log.d(TAG, "start onContextItemSelected");
        // The data from the menu item.
        AdapterView.AdapterContextMenuInfo info;

        /*
         * Gets the extra info from the menu item. When an note in the Notes list is long-pressed, a
         * context menu appears. The menu items for the menu automatically get the data
         * associated with the note that was long-pressed. The data comes from the provider that
         * backs the list.
         *
         * The note's data is passed to the context menu creation routine in a ContextMenuInfo
         * object.
         *
         * When one of the context menu items is clicked, the same data is passed, along with the
         * note ID, to onContextItemSelected() via the item parameter.
         */
        try {
            // Casts the data object in the item into the type for AdapterView objects.
            info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        } catch (ClassCastException e) {

            // If the object can't be cast, logs an error
            Log.e(TAG, "bad menuInfo", e);

            // Triggers default processing of the menu item.
    		Log.d(TAG, "end onContextItemSelected");
            return false;
        }
        // Appends the selected note's ID to the URI sent with the incoming Intent.
        Uri uri = ContentUris.withAppendedId(getIntent().getData(), info.id);
		Log.d(TAG, "uri="+uri.toString());

        /*
         * Gets the menu item's ID and compares it to known actions.
         */
        Log.d(TAG, "itemid="+String.valueOf(item.getItemId()));
        switch (item.getItemId()) {
        case R.id.context_open:
            // Launch activity to view/edit the currently selected item
            startActivity(new Intent(Intent.ACTION_EDIT, uri));
    		Log.d(TAG, "end onContextItemSelected");
            return true;

        case R.id.context_copy:
            // Gets a handle to the clipboard service.
            ClipboardManager clipboard = (ClipboardManager)
                    getSystemService(Context.CLIPBOARD_SERVICE);
  
            // Copies the notes URI to the clipboard. In effect, this copies the note itself
            clipboard.setPrimaryClip(ClipData.newUri(   // new clipboard item holding a URI
                    getContentResolver(),               // resolver to retrieve URI info
                    "RssSite",                             // label for the clip
                    uri)                            // the URI
            );
  
            // Returns to the caller and skips further processing.
    		Log.d(TAG, "end onContextItemSelected");
            return true;

        case R.id.context_delete:
  
            // Deletes the note from the provider by passing in a URI in note ID format.
            // Please see the introductory note about performing provider operations on the
            // UI thread.
            getContentResolver().delete(
                uri,  // The URI of the provider
                null,     // No where clause is needed, since only a single note ID is being
                          // passed in.
                null      // No where clause is used, so no where arguments are needed.
            );
  
            // Returns to the caller and skips further processing.
    		Log.d(TAG, "end onContextItemSelected");
            return true;
        default:
    		Log.d(TAG, "end onContextItemSelected");
            return super.onContextItemSelected(item);
        }
	}
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Log.d(TAG, "start onListItemClick");
		// Constructs a new URI from the incoming URI and the row ID
        Uri uri = ContentUris.withAppendedId(getIntent().getData(), id);
		Log.d(TAG, "url="+uri.toString());

        // Gets the action from the incoming Intent
        String action = getIntent().getAction();

		Log.d(TAG, "action="+action);
        // Handles requests for note data
        if (Intent.ACTION_PICK.equals(action) || Intent.ACTION_GET_CONTENT.equals(action)) {

            // Sets the result to return to the component that called this Activity. The
            // result contains the new URI
            setResult(RESULT_OK, new Intent().setData(uri));
        } else {

            // Sends out an Intent to start an Activity that can handle ACTION_EDIT. The
            // Intent's data is the note ID URI. The effect is to call NoteEdit.
            startActivity(new Intent(jp.co.webact.rssreader.RssReaderActivity.READ_RSS_ACTION, uri));
        }
		Log.d(TAG, "end onListItemClick");
	}
}
