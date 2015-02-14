package jp.co.webact.rssreader;

import java.util.ArrayList;

import android.net.Uri;
import android.os.Bundle;
import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

public class RssReaderActivity extends ListActivity {
	private static final String TAG = "RssReaderActivity";
	public static final String READ_RSS_ACTION = "jp.co.webact.rssreader.action.READ_RSS";
//	public static final String RSS_FEED_URL = "http://itpro.nikkeibp.co.jp/rss/ITpro.rdf";
	 /*
     * Creates a projection that returns the note ID and the note contents.
     */
    private static final String[] PROJECTION =  new String[] {
    	RssSite.Site._ID,
    	RssSite.Site.COLUMN_NAME_TITLE,
    	RssSite.Site.COLUMN_NAME_URL,
    	RssSite.Site.COLUMN_NAME_GROUP
    };

	private ArrayList<Item> mItems;
	private RssListAdapter mAdapter;
	private String mRssFeedUrl;
	private String mSiteTitle;
    private Uri mUri;
    private Cursor mCursor;
    private TextView mEmpty;
	/**
	 * アクティビティ作成時の処理
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		try {
			super.onCreate(savedInstanceState);
	    	Log.d(TAG, "start onCreate");
			setContentView(R.layout.activity_rss_reader);
			mEmpty = (TextView) findViewById(R.id.empty);
	    	final Intent intent = getIntent();
	    	final String action = intent.getAction();
	    	Log.d(TAG, "action=" + action);
	    	if (READ_RSS_ACTION.equals(action)) {
	    		mUri = intent.getData();
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
    		
	        if (mCursor != null) {
	        	mCursor.moveToFirst();
	        	int colTitleIndex = mCursor.getColumnIndex(RssSite.Site.COLUMN_NAME_TITLE);
	        	String mSiteTitle = mCursor.getString(colTitleIndex);
	        	setTitle(mSiteTitle);
	            int colUrlIndex = mCursor.getColumnIndex(RssSite.Site.COLUMN_NAME_URL);
	            mRssFeedUrl = mCursor.getString(colUrlIndex);
				// Itemオブジェクトを保持するためのリストを生成し、アダプタに追加する
				mItems = new ArrayList<Item>();
				mAdapter = new RssListAdapter(this, mItems);
				// パーサータスクを起動する
				RssParserTask task = new RssParserTask(this, mAdapter);
	            task.execute(mRssFeedUrl);
	        } else {
	            
	        	mEmpty.setText(getText(R.string.error_message));
	        }
	    	Log.d(TAG, "end onCreate");
		} catch(Exception e)
		{
			e.printStackTrace();
		}
		
		
	}
	/**
	 * リストの項目を選択したときの処理
	 */
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Item item = mItems.get(position);
		Intent intent = new Intent(this, ItemDetailActivity.class);
		intent.putExtra("SITETITLE", mSiteTitle);
		intent.putExtra("TITLE", item.getTitle());
		intent.putExtra("DESCRIPTION", item.getDescription());
		intent.putExtra("ENCLOSURE", item.getUrl());
		startActivity(intent);
	}
	/**
	 * メニューボタンを押下した時の処理
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.rss_reader, menu);
		return true;
	}
	/**
	 * メニュー項目を選択したときの処理 
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case R.id.action_reload:
			// アダプタを初期化し、タスクを起動する。
			mItems = new ArrayList<Item>();
			mAdapter = new RssListAdapter(this, mItems);
			// タスクを生成する
			RssParserTask task = new RssParserTask(this, mAdapter);
			task.execute(mRssFeedUrl);
		}
		return super.onOptionsItemSelected(item);
	}
}
