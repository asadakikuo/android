package jp.co.webact.rssreader;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;

import android.content.ClipDescription;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.content.ContentProvider.PipeDataWriter;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.text.TextUtils;
import android.util.Log;

public class RssSiteProvider extends ContentProvider implements PipeDataWriter<Cursor> {
	private static final String TAG = "RssSiteProvider";
	RssReaderDataBase mDatabaseHelper;
    /**
     * Content authority for this provider.
     */
    private static final String AUTHORITY = RssSite.CONTENT_AUTHORITY;
    
    private static final String[] READ_RSSSITE_PROJECTION = new String[]{
    	RssSite.Site._ID,
    	RssSite.Site.COLUMN_NAME_TITLE,
    	RssSite.Site.COLUMN_NAME_URL,
    	RssSite.Site.COLUMN_NAME_GROUP,
    };
    private static final int READ_RSSSITE_TITLE_INDEX = 1;
    private static final int READ_RSSSITE_URL_INDEX = 2;
    /**
     * URI ID for route: /entries
     */
    public static final int ROUTE_ENTRIES = 1;

    /**
     * URI ID for route: /entries/{ID}
     */
    public static final int ROUTE_ENTRIES_ID = 2;
    /**
     * A projection map used to select columns from the database
     */
    private static HashMap<String, String> sRssSiteProjectionMap;

    /**
     * UriMatcher, used to decode incoming URIs.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        sUriMatcher.addURI(AUTHORITY, "sites", ROUTE_ENTRIES);
        sUriMatcher.addURI(AUTHORITY, "sites/*", ROUTE_ENTRIES_ID);

        sRssSiteProjectionMap = new HashMap<String, String>();

        // Maps the string "_ID" to the column name "_ID"
        sRssSiteProjectionMap.put(RssSite.Site._ID, RssSite.Site._ID);
        sRssSiteProjectionMap.put(RssSite.Site.COLUMN_NAME_TITLE, RssSite.Site.COLUMN_NAME_TITLE);
        sRssSiteProjectionMap.put(RssSite.Site.COLUMN_NAME_URL, RssSite.Site.COLUMN_NAME_URL);
        sRssSiteProjectionMap.put(RssSite.Site.COLUMN_NAME_GROUP, RssSite.Site.COLUMN_NAME_GROUP);
        sRssSiteProjectionMap.put(RssSite.Site.COLUMN_NAME_PUBLISHED, RssSite.Site.COLUMN_NAME_PUBLISHED);
    }
	@Override
	public int delete(Uri uri, String where, String[] whereArgs) {
		Log.d(TAG, "start delete");
		Log.d(TAG, "uri="+uri.toString());
		 // Opens the database object in "write" mode.
		Log.d(TAG, "call getWritableDatabase");
        SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        String finalWhere;

        int count;

        // Does the delete based on the incoming URI pattern.
        switch (sUriMatcher.match(uri)) {

            // If the incoming pattern matches the general pattern for notes, does a delete
            // based on the incoming "where" columns and arguments.
            case ROUTE_ENTRIES:
        		Log.d(TAG, "ROUTE_ENTRIES pattern");
            	Log.d(TAG, "call delete ["+ where +"]");
                count = db.delete(
                    RssSite.Site.TABLE_NAME,  // The database table name
                    where,                     // The incoming where clause column names
                    whereArgs                  // The incoming where clause values
                );
                break;

                // particular note ID.
            case ROUTE_ENTRIES_ID:
            	Log.d(TAG, "ROUTE_ENTRIES_ID pattern");
            	String siteID = uri.getPathSegments().get(RssSite.Site.SITE_ID_PATH_POSITION); 
                finalWhere =
                        RssSite.Site._ID +                              // The ID column name
                        " = " +                                          // test for equality
                        siteID
                ;

                // If there were additional selection criteria, append them to the final
                // WHERE clause
                if (where != null) {
                    finalWhere = finalWhere + " AND " + where;
                }

                // Performs the delete.
            	Log.d(TAG, "call delete ["+ finalWhere +"]");
                count = db.delete(
                		RssSite.Site.TABLE_NAME,  // The database table name.
                    finalWhere,                // The final WHERE clause
                    whereArgs                  // The incoming where clause values.
                );
                break;

            // If the incoming pattern is invalid, throws an exception.
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);

        // Returns the number of rows deleted.
		Log.d(TAG, "end delete ["+String.valueOf(count)+"]");
        return count;
	}

	@Override
	public String getType(Uri uri) {
		Log.d(TAG, "start getType");
		Log.d(TAG, "uri="+uri.toString());
		/**
		 * Chooses the MIME type based on the incoming URI pattern
		 */
		switch (sUriMatcher.match(uri)) {

		// If the pattern is for notes or live folders, returns the general content type.
		case ROUTE_ENTRIES:
			Log.d(TAG, "end getType["+RssSite.Site.CONTENT_TYPE+"]");
			return RssSite.Site.CONTENT_TYPE;

			// If the pattern is for note IDs, returns the note ID content type.
		case ROUTE_ENTRIES_ID:
			Log.d(TAG, "end getType["+RssSite.Site.CONTENT_ITEM_TYPE+"]");
			return RssSite.Site.CONTENT_ITEM_TYPE;

			// If the URI pattern doesn't match any permitted patterns, throws an exception.
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
	}
	
	static ClipDescription RSSSITE_STREAM_TYPES = new ClipDescription(null,
            new String[] { ClipDescription.MIMETYPE_TEXT_PLAIN });
	@Override
	public String[] getStreamTypes(Uri uri, String mimeTypeFilter) {
		Log.d(TAG, "start getStreamTypes");
		switch (sUriMatcher.match(uri)) {
		case ROUTE_ENTRIES:
			Log.d(TAG, "end getStreamTypes null");
			return null;
		case ROUTE_ENTRIES_ID:
			Log.d(TAG, "end getStreamTypes id");
			return RSSSITE_STREAM_TYPES.filterMimeTypes(mimeTypeFilter);
		default:
			throw new IllegalArgumentException("Unknown URI" + uri);	
		}
	}
	@Override
	public AssetFileDescriptor openTypedAssetFile(Uri uri,
			String mimeTypeFilter, Bundle opts) throws FileNotFoundException {
        // Checks to see if the MIME type filter matches a supported MIME type.
        String[] mimeTypes = getStreamTypes(uri, mimeTypeFilter);

        // If the MIME type is supported
        if (mimeTypes != null) {
        	Cursor c = query(uri,
        			READ_RSSSITE_PROJECTION,
        			null,
        			null,
        			null);
        	  // If the query fails or the cursor is empty, stop
            if (c == null || !c.moveToFirst()) {

                // If the cursor is empty, simply close the cursor and return
                if (c != null) {
                    c.close();
                }

                // If the cursor is null, throw an exception
                throw new FileNotFoundException("Unable to query " + uri);
            }

            // Start a new thread that pipes the stream data back to the caller.
            return new AssetFileDescriptor(
                    openPipeHelper(uri, mimeTypes[0], opts, c, this), 0,
                    AssetFileDescriptor.UNKNOWN_LENGTH);
        }		
		return super.openTypedAssetFile(uri, mimeTypeFilter, opts);
	}
	@Override
	public void writeDataToPipe(ParcelFileDescriptor output, Uri uri,
			String mimeType, Bundle opts, Cursor c) {
		FileOutputStream fout = new FileOutputStream(output.getFileDescriptor());
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(new OutputStreamWriter(fout, "UTF-8"));
            pw.println(c.getString(READ_RSSSITE_TITLE_INDEX));
            pw.println("");
            pw.println(c.getString(READ_RSSSITE_URL_INDEX));
        } catch (UnsupportedEncodingException e) {
            Log.w(TAG, "Ooops", e);
        } finally {
            c.close();
            if (pw != null) {
                pw.flush();
            }
            try {
                fout.close();
            } catch (IOException e) {
            }
        }
	}
	@Override
	public Uri insert(Uri uri, ContentValues initialValues) {
		Log.d(TAG, "start insert");
		Log.d(TAG, "uri="+uri.toString());

		// Validates the incoming URI. Only the full provider URI is allowed for inserts.
        if (sUriMatcher.match(uri) != ROUTE_ENTRIES) {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        // A map to hold the new record's values.
        ContentValues values;

        // If the incoming values map is not null, uses it for the new values.
        if (initialValues != null) {
            values = new ContentValues(initialValues);

        } else {
            // Otherwise, create a new value map
            values = new ContentValues();
        }

        // Gets the current system time in milliseconds
        Long now = Long.valueOf(System.currentTimeMillis());

        if (values.containsKey(RssSite.Site.COLUMN_NAME_PUBLISHED) == false) {
            values.put(RssSite.Site.COLUMN_NAME_PUBLISHED, now);
        }

        if (values.containsKey(RssSite.Site.COLUMN_NAME_TITLE) == false) {
            Resources r = Resources.getSystem();
            values.put(RssSite.Site.COLUMN_NAME_TITLE, r.getString(android.R.string.untitled));
        }

        if (values.containsKey(RssSite.Site.COLUMN_NAME_URL) == false) {
            values.put(RssSite.Site.COLUMN_NAME_URL, "");
        }
        if (values.containsKey(RssSite.Site.COLUMN_NAME_GROUP) == false) {
            values.put(RssSite.Site.COLUMN_NAME_GROUP, "");
        }

        // Opens the database object in "write" mode.
        SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();

        // Performs the insert and returns the ID of the new note.
        long rowId = db.insert(
            RssSite.Site.TABLE_NAME,         // The table to insert into.
            RssSite.Site.COLUMN_NAME_TITLE,  // A hack, SQLite sets this column value to null
                                             // if values is empty.
            values                           // A map of column names, and the values to insert
                                             // into the columns.
        );

        // If the insert succeeded, the row ID exists.
        if (rowId > 0) {
            Uri siteUri = Uri.parse(RssSite.Site.CONTENT_URI + "/" + rowId);
            getContext().getContentResolver().notifyChange(siteUri, null);
            return siteUri;
        }

        // If the insert didn't succeed, then the rowID is <= 0. Throws an exception.
        throw new SQLException("Failed to insert row into " + uri);
	}

	@Override
	public boolean onCreate() {
		Log.d(TAG, "start onCreate");
		mDatabaseHelper = new RssReaderDataBase(getContext());
		Log.d(TAG, "end onCreate");
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		Log.d(TAG, "start query");
        Log.d(TAG, "uri="+uri.toString());
        // Constructs a new query builder and sets its table name
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(RssSite.Site.TABLE_NAME);
        
        int uriMatch = sUriMatcher.match(uri);
        Log.d(TAG, "uriMatch="+String.valueOf(uriMatch));
        switch (uriMatch) {
            case ROUTE_ENTRIES_ID:
            	String siteID = uri.getPathSegments().get(RssSite.Site.SITE_ID_PATH_POSITION);
                qb.setProjectionMap(sRssSiteProjectionMap);
                qb.appendWhere(
                		RssSite.Site._ID +    // the name of the ID column
                    "=" +
                    // the position of the ID itself in the incoming URI
                    siteID);
                break;
            case ROUTE_ENTRIES:
            	 qb.setProjectionMap(sRssSiteProjectionMap);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        String orderBy;
        // If no sort order is specified, uses the default
        if (TextUtils.isEmpty(sortOrder)) {
            orderBy = RssSite.Site.DEFAULT_SORT_ORDER;
        } else {
            // otherwise, uses the incoming sort order
            orderBy = sortOrder;
        }

        // Opens the database object in "read" mode, since no writes need to be done.
        Log.d(TAG, "call getReadableDatabase");
        SQLiteDatabase db = mDatabaseHelper.getReadableDatabase();

        /*
         * Performs the query. If no problems occur trying to read the database, then a Cursor
         * object is returned; otherwise, the cursor variable contains null. If no records were
         * selected, then the Cursor object is empty, and Cursor.getCount() returns 0.
         */
        Log.d(TAG, "call query");
        Cursor c = qb.query(
            db,            // The database to query
            projection,    // The columns to return from the query
            selection,     // The columns for the where clause
            selectionArgs, // The values for the where clause
            null,          // don't group the rows
            null,          // don't filter by row groups
            orderBy        // The sort order
        );

        // Tells the Cursor what URI to watch, so it knows when its source data changes
        Log.d(TAG, "call setNotificationUri");
        c.setNotificationUri(getContext().getContentResolver(), uri);
        Log.d(TAG, "end query");
        return c;
	}

	@Override
	public int update(Uri uri, ContentValues values, String where,
			String[] whereArgs) {
        Log.d(TAG, "start update");
		  // Opens the database object in "write" mode.
        SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        int count;
        String finalWhere;

        // Does the update based on the incoming URI pattern
        switch (sUriMatcher.match(uri)) {

            // If the incoming URI matches the general notes pattern, does the update based on
            // the incoming data.
            case ROUTE_ENTRIES:

                // Does the update and returns the number of rows updated.
                count = db.update(
                    RssSite.Site.TABLE_NAME, // The database table name.
                    values,                   // A map of column names and new values to use.
                    where,                    // The where clause column names.
                    whereArgs                 // The where clause column values to select on.
                );
                break;

            // If the incoming URI matches a single note ID, does the update based on the incoming
            // data, but modifies the where clause to restrict it to the particular note ID.
            case ROUTE_ENTRIES_ID:
                // From the incoming URI, get the note ID
                String siteID = uri.getPathSegments().get(RssSite.Site.SITE_ID_PATH_POSITION);
                Log.d(TAG, "siteID="+siteID);
                /*
                 * Starts creating the final WHERE clause by restricting it to the incoming
                 * note ID.
                 */
                finalWhere =
                		RssSite.Site._ID +                              // The ID column name
                        " = " +                                          // test for equality
                        siteID
                ;

                // If there were additional selection criteria, append them to the final WHERE
                // clause
                if (where !=null) {
                    finalWhere = finalWhere + " AND " + where;
                }

                Log.d(TAG, "finalWhere="+finalWhere);
                Log.d(TAG, "whereArgs="+whereArgs);
                // Does the update and returns the number of rows updated.
                count = db.update(
                		RssSite.Site.TABLE_NAME, // The database table name.
                		values,                   // A map of column names and new values to use.
                		finalWhere,               // The final WHERE clause to use
                		// placeholders for whereArgs
                		whereArgs                 // The where clause column values to select on, or
                		// null if the values are in the where argument.
                		);
                break;
            // If the incoming pattern is invalid, throws an exception.
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);

        Log.d(TAG, "end update count="+String.valueOf(count));
        // Returns the number of rows updated.
        return count;
	}
    /**
     * SQLite backend for @{link RssSiteProvider}.
     *
     */
    static class RssReaderDataBase extends SQLiteOpenHelper {
        /** Schema version. */
        public static final int DATABASE_VERSION = 1;
        /** Filename for SQLite file. */
        public static final String DATABASE_NAME = "rssreader.db";

        private static final String TYPE_TEXT = " TEXT";
        private static final String TYPE_INTEGER = " INTEGER";
        private static final String COMMA_SEP = ",";
    	
        /** SQL statement to create "sites" table. */
        private static final String SQL_CREATE_ENTRIES =
                "CREATE TABLE " + RssSite.Site.TABLE_NAME + " (" +
                		RssSite.Site._ID + " INTEGER PRIMARY KEY," +
                        RssSite.Site.COLUMN_NAME_TITLE    + TYPE_TEXT + COMMA_SEP +
                        RssSite.Site.COLUMN_NAME_URL + TYPE_TEXT + COMMA_SEP +
                        RssSite.Site.COLUMN_NAME_GROUP + TYPE_TEXT + COMMA_SEP +
                        RssSite.Site.COLUMN_NAME_PUBLISHED + TYPE_INTEGER + ")";
        /** SQL statement to drop "entry" table. */
        private static final String SQL_DELETE_ENTRIES =
        		"DROP TABLE IF EXISTS " + RssSite.Site.TABLE_NAME;
        /**
         * コンストラクタ
         * @param context
         */
        public RssReaderDataBase(Context context) {
        	super(context, DATABASE_NAME, null, DATABASE_VERSION);
        	Log.d(TAG, "call RssReaderDataBase");
        }
        
        @Override
        public void onCreate(SQLiteDatabase db) {
        	Log.d(TAG, "start onCreate");
        	db.execSQL(SQL_CREATE_ENTRIES);
        	Log.d(TAG, "end onCreate");
        }
        
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        	Log.d(TAG, "start onUpgrade");
            // This database is only a cache for online data, so its upgrade policy is        	
            // to simply to discard the data and start over
            db.execSQL(SQL_DELETE_ENTRIES);
            onCreate(db);
        	Log.d(TAG, "end onUpgrade");
        }
    }
}
