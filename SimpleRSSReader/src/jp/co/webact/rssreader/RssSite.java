package jp.co.webact.rssreader;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * フィールド と テーブル名
 * @author asada
 *
 */
public class RssSite {
	private RssSite() {
		
	}
    /**
     * Content provider authority.
     */
    public static final String CONTENT_AUTHORITY = "jp.co.webact.rssreader";
    /**
     * Base URI. (content://com.example.android.network.sync.basicsyncadapter)
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    /**
     * Path component for "entry"-type resources..
     */
    private static final String PATH_SITES = "sites";

    public static class Site implements BaseColumns {
        /**
         * MIME type for lists of entries.
         */
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.simplerssreader.sites";
        /**
         * MIME type for individual entries.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.simplerssreader.site";
    	
        /**
         * Fully qualified URI for "site" resources.
         */
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_SITES).build();
        /**
         * 0-relative position of a ID segment in the path part of a note ID URI
         */
        public static final int SITE_ID_PATH_POSITION = 1;        
        /**
         * Table name where records are stored for "m_rsssite" resources.
         */
        public static final String TABLE_NAME = "m_rsssite";
        /**
         * Article title
         */
        public static final String COLUMN_NAME_TITLE = "title";
        /**
         * Article url
         */
        public static final String COLUMN_NAME_URL = "url";
        /**
         * Article group
         */
        public static final String COLUMN_NAME_GROUP = "grp";
        /**
         * Date article was published.
         */
        public static final String COLUMN_NAME_PUBLISHED = "published";
        /**
         * The default sort order for this table
         */
        public static final String DEFAULT_SORT_ORDER = "grp, published DESC";
    }
	
}
