package jp.co.webact.rssreader;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;

public class RssSiteEditor extends Activity {
	private static final String TAG = "RssSiteEditor";
	public static final String EDIT_SITE_ACTION = "jp.co.webact.rssreader.action.EDIT_SITE";
	 /*
     * Creates a projection that returns the note ID and the note contents.
     */
    private static final String[] PROJECTION =  new String[] {
    	RssSite.Site._ID,
    	RssSite.Site.COLUMN_NAME_TITLE,
    	RssSite.Site.COLUMN_NAME_URL,
    	RssSite.Site.COLUMN_NAME_GROUP
    };
 // A label for the saved state of the activity
    private static final String ORIGINAL_CONTENT = "origContent";

    // This Activity can be started by more than one action. Each action is represented
    // as a "state" constant
    private static final int STATE_EDIT = 0;
    private static final int STATE_INSERT = 1;

    // Global mutable variables
    private int mState;
    private Uri mUri;
    private Cursor mCursor;
    private EditText mTitle;
    private EditText mSiteUrl;
    private EditText mGroup;
    private String mOriginalContent;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	Log.d(TAG, "start onCreate");
    	final Intent intent = getIntent();
    	final String action = intent.getAction();
    	Log.d(TAG, "action=" + action);
    	if (Intent.ACTION_EDIT.equals(action)) {

            // Sets the Activity state to EDIT, and gets the URI for the data to be edited.
            mState = STATE_EDIT;
            mUri = intent.getData();

            // For an insert or paste action:
        } else if (Intent.ACTION_INSERT.equals(action)
                || Intent.ACTION_PASTE.equals(action)) {

            // Sets the Activity state to INSERT, gets the general note URI, and inserts an
            // empty record in the provider
            mState = STATE_INSERT;
            mUri = getContentResolver().insert(intent.getData(), null);

            /*
             * If the attempt to insert the new note fails, shuts down this Activity. The
             * originating Activity receives back RESULT_CANCELED if it requested a result.
             * Logs that the insert failed.
             */
            if (mUri == null) {

                // Writes the log identifier, a message, and the URI that failed.
                Log.e(TAG, "Failed to insert new note into " + getIntent().getData());

                // Closes the activity.
                finish();
                return;
            }

            // Since the new entry was created, this sets the result to be returned
            // set the result to be returned.
            setResult(RESULT_OK, (new Intent()).setAction(mUri.toString()));

        // If the action was other than EDIT or INSERT:
        } else {

            // Logs an error that the action was not understood, finishes the Activity, and
            // returns RESULT_CANCELED to an originating Activity.
            Log.e(TAG, "Unknown action, exiting");
            finish();
            return;
        }
    	Log.d(TAG, "mUri=" + mUri);
    	mCursor = getContentResolver().query(
    			mUri,         // The URI that gets multiple notes from the provider.
                PROJECTION,   // A projection that returns the note ID and note content for each note.
                null,
                null,
                null
                );
    	// For a paste, initializes the data from clipboard.
        // (Must be done after mCursor is initialized.)
        if (Intent.ACTION_PASTE.equals(action)) {
            // Does the paste
            performPaste();
            // Switches the state to EDIT so the title can be modified.
            mState = STATE_EDIT;
        }

        // Sets the layout for this Activity. See res/layout/note_editor.xml
        setContentView(R.layout.rsssite_editor);
        mTitle = (EditText) findViewById(R.id.text_title);
        mSiteUrl = (EditText) findViewById(R.id.text_url);
        mGroup = (EditText) findViewById(R.id.text_group);
        if (savedInstanceState != null) {
            mOriginalContent = savedInstanceState.getString(ORIGINAL_CONTENT);
        }
    	Log.d(TAG, "end onCreate");
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    	Log.d(TAG, "start onResume");
        if (mCursor != null) {
        	//requery‚Ì‘ã‘Ö
        	if (mState == STATE_EDIT) {
            	mCursor = getContentResolver().query(
            			mUri,         // The URI that gets multiple notes from the provider.
                        PROJECTION,   // A projection that returns the note ID and note content for each note.
                        null,
                        null,
                        null
                        );
        	}
        	mCursor.moveToFirst();
        	if (mState == STATE_EDIT) {
        		// debug
                int colIdIndex = mCursor.getColumnIndex(RssSite.Site._ID);
                String _id = mCursor.getString(colIdIndex);
        		Log.d(TAG, "id="+_id);
        		// debug
                // Set the title of the Activity to include the note title
                int colTitleIndex = mCursor.getColumnIndex(RssSite.Site.COLUMN_NAME_TITLE);
                String title = mCursor.getString(colTitleIndex);
            	Log.d(TAG, "title="+title);
                Resources res = getResources();
                mTitle.setText(title);
            // Sets the title to "create" for inserts
            } else if (mState == STATE_INSERT) {
            	
            	mTitle.setText(getText(R.string.title_create));
            }

            /*
             * onResume() may have been called after the Activity lost focus (was paused).
             * The user was either editing or creating a note when the Activity paused.
             * The Activity should re-display the text that had been retrieved previously, but
             * it should not move the cursor. This helps the user to continue editing or entering.
             */

            // Gets the note text from the Cursor and puts it in the TextView, but doesn't change
            // the text cursor's position.
            int colUrlIndex = mCursor.getColumnIndex(RssSite.Site.COLUMN_NAME_URL);
            String url = mCursor.getString(colUrlIndex);
            mSiteUrl.setText(url);
            int colGroupIndex = mCursor.getColumnIndex(RssSite.Site.COLUMN_NAME_GROUP);
            String group = mCursor.getString(colGroupIndex);
            mGroup.setText(group);

            // Stores the original note text, to allow the user to revert changes.
            if (mOriginalContent == null) {
                mOriginalContent = mTitle.getText().toString();
            }
            Log.d(TAG, "mOriginalContent="+mOriginalContent);
            Log.d(TAG, "url="+url);
            Log.d(TAG, "group="+group);
        } else {
            
            mTitle.setText(getText(R.string.error_message));
        }
    	Log.d(TAG, "end onResume");
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
    	Log.d(TAG, "start onSaveInstanceState");
    	Log.d(TAG, "mOriginalContent="+mOriginalContent);
    	outState.putString(ORIGINAL_CONTENT, mOriginalContent);
    	Log.d(TAG, "end onSaveInstanceState");
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
    	Log.d(TAG, "start onPause");
    	if (mCursor != null) {
    		// debug
            int colIdIndex = mCursor.getColumnIndex(RssSite.Site._ID);
            String _id = mCursor.getString(colIdIndex);
    		Log.d(TAG, "id="+_id);
    		// debug
    	    String text = mTitle.getText().toString();
    	    String url = mSiteUrl.getText().toString();
    	    String group = mGroup.getText().toString();
    	    Log.d(TAG, "text="+text); 	
            int length = text.length();
            if (isFinishing() && (length == 0)) {
                setResult(RESULT_CANCELED);
                deleteSite();

                /*
                 * Writes the edits to the provider. The note has been edited if an existing note was
                 * retrieved into the editor *or* if a new note was inserted. In the latter case,
                 * onCreate() inserted a new empty note into the provider, and it is this new note
                 * that is being edited.
                 */
            } else if (mState == STATE_EDIT) {
                // Creates a map to contain the new values for the columns
                updateSite(url, group, text);
            } else if (mState == STATE_INSERT) {
                updateSite(url, group, text);
                mState = STATE_EDIT;
          }
    	}
    	Log.d(TAG, "end onPause");
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	Log.d(TAG, "start onCreateOptionsMenu");
    	// Inflate menu from XML resource
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.editor_options_menu, menu);
        if (mState == STATE_EDIT) {
        	Intent intent = new Intent(null, mUri);
            intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
            menu.addIntentOptions(Menu.CATEGORY_ALTERNATIVE, 0, 0,
                    new ComponentName(this, RssSiteEditor.class), null, intent, 0, null);
        }
        Log.d(TAG, "end onCreateOptionsMenu");
    	return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
    	Log.d(TAG, "start onPrepareOptionsMenu");
    	 // Check if note has changed and enable/disable the revert option
        int colTitleIndex = mCursor.getColumnIndex(RssSite.Site.COLUMN_NAME_TITLE);
        String savedTitle = mCursor.getString(colTitleIndex);
        String currentTitle = mTitle.getText().toString();
    	Log.d(TAG, "savedTitle="+savedTitle);
    	Log.d(TAG, "currentTitle="+currentTitle);
        if (savedTitle.equals(currentTitle)) {
            menu.findItem(R.id.menu_revert).setVisible(false);
        } else {
            menu.findItem(R.id.menu_revert).setVisible(true);
        }
        Log.d(TAG, "end onPrepareOptionsMenu");
    	return super.onPrepareOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	Log.d(TAG, "start onOptionsItemSelected");
        switch (item.getItemId()) {
        case R.id.menu_save:
            String text = mTitle.getText().toString();
            String url = mSiteUrl.getText().toString();
            String group = mGroup.getText().toString();
            updateSite(url, group, text);
            finish();
            break;
        case R.id.menu_delete:
            deleteSite();
            finish();
            break;
        case R.id.menu_revert:
            cancelSite();
            break;
        }
        Log.d(TAG, "end onOptionsItemSelected");
        return super.onOptionsItemSelected(item);
    }
    /**
     * @category 
     * 
     */
	private final void performPaste() {
    	Log.d(TAG, "start performPaste");
        // Gets a handle to the Clipboard Manager
        ClipboardManager clipboard = (ClipboardManager)
                getSystemService(Context.CLIPBOARD_SERVICE);

        // Gets a content resolver instance
        ContentResolver cr = getContentResolver();

        // Gets the clipboard data from the clipboard
        ClipData clip = clipboard.getPrimaryClip();
        if (clip != null) {

            String url=null;
            String group=null;
            String title=null;

            // Gets the first item from the clipboard data
            ClipData.Item item = clip.getItemAt(0);

            // Tries to get the item's contents as a URI pointing to a note
            Uri uri = item.getUri();

            // Tests to see that the item actually is an URI, and that the URI
            // is a content URI pointing to a provider whose MIME type is the same
            // as the MIME type supported by the Note pad provider.
            if (uri != null && RssSite.Site.CONTENT_ITEM_TYPE.equals(cr.getType(uri))) {

                // The clipboard holds a reference to data with a note MIME type. This copies it.
                Cursor orig = cr.query(
                        uri,            // URI for the content provider
                        PROJECTION,     // Get the columns referred to in the projection
                        null,           // No selection variables
                        null,           // No selection variables, so no criteria are needed
                        null            // Use the default sort order
                );

                // If the Cursor is not null, and it contains at least one record
                // (moveToFirst() returns true), then this gets the note data from it.
                if (orig != null) {
                    if (orig.moveToFirst()) {
                        int colUrlIndex = mCursor.getColumnIndex(RssSite.Site.COLUMN_NAME_URL);
                        int colTitleIndex = mCursor.getColumnIndex(RssSite.Site.COLUMN_NAME_TITLE);
                        int colGroupIndex = mCursor.getColumnIndex(RssSite.Site.COLUMN_NAME_GROUP);
                        url = orig.getString(colUrlIndex);
                        title = orig.getString(colTitleIndex);
                        group = orig.getString(colGroupIndex);
                    }

                    // Closes the cursor.
                    orig.close();
                }
            }

            // If the contents of the clipboard wasn't a reference to a note, then
            // this converts whatever it is to text.
            if (url == null) {
                url = item.coerceToText(this).toString();
            }

            updateSite(url, group, title);
            Log.d(TAG, "end performPaste");
        }
    }
    /**
     * 
     * @param url
     * @param grp
     * @param title
     */
    private final void updateSite(String url, String grp, String title) {
    	Log.d(TAG, "start updateSite");
    	Log.d(TAG, "url="+url);
    	Log.d(TAG, "grp="+grp);
    	Log.d(TAG, "title="+title);
    	ContentValues values = new ContentValues();
    	values.put(RssSite.Site.COLUMN_NAME_PUBLISHED , System.currentTimeMillis());
        values.put(RssSite.Site.COLUMN_NAME_TITLE, title);
        values.put(RssSite.Site.COLUMN_NAME_URL, url);
        values.put(RssSite.Site.COLUMN_NAME_GROUP, grp);
    	
    	getContentResolver().update(
    			mUri,    // The URI for the record to update.
    			values,  // The map of column names and new values to apply to them.
    			null,    // No selection criteria are used, so no where columns are necessary.
    			null     // No where columns are used, so no where arguments are necessary.
    			);
    	Log.d(TAG, "end updateSite");
    }
    /**
     * cancelSite
     */
    private final void cancelSite() {
    	Log.d(TAG, "start cancelSite");
    	if (mCursor != null) {
            if (mState == STATE_EDIT) {
                // Put the original note text back into the database
                mCursor.close();
                mCursor = null;
                ContentValues values = new ContentValues();
                values.put(RssSite.Site.COLUMN_NAME_TITLE, mOriginalContent);
                getContentResolver().update(mUri, values, null, null);
            } else if (mState == STATE_INSERT) {
                // We inserted an empty note, make sure to delete it
                deleteSite();
            }
        }
        setResult(RESULT_CANCELED);
        finish();
        Log.d(TAG, "end cancelSite");
    }
    /**
     * Take care of deleting a site.  Simply deletes the entry.
     */
    private final void deleteSite() {
    	Log.d(TAG, "start deleteSite");
        if (mCursor != null) {
            mCursor.close();
            mCursor = null;
            getContentResolver().delete(mUri, null, null);
            mTitle.setText("");
            mSiteUrl.setText("");
            mGroup.setText("");
        }
        Log.d(TAG, "end deleteSite");
    }
}
