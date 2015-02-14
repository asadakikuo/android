package jp.co.webact.rssreader;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.TextView;
/**
 * 詳細画面アクティビティ
 * 
 * @author asada
 *
 */
public class ItemDetailActivity extends Activity {
	private static final String TAG = "ItemDetailActivity";
	private TextView mTitle;
	private WebView mDescr;
	private TextView mUrl;
	
	/**
	 * アクティビティ作成時の処理
	 * 
	 * @param savedInstanceState 
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.item_detail);
		final String mimeType = "text/html; charset=utf-8";
		Intent intent = getIntent();
		String sitetitle = intent.getStringExtra("SITETITLE");
		setTitle(sitetitle);
		String title = intent.getStringExtra("TITLE");
		mTitle = (TextView) findViewById(R.id.item_detail_title);
		mTitle.setText(title);
		String descr = intent.getStringExtra("DESCRIPTION");
		mDescr = (WebView) findViewById(R.id.item_detail_descr);
		mDescr.loadData(descr, mimeType, "utf-8");
		String url = intent.getStringExtra("ENCLOSURE");
		mUrl = (TextView) findViewById(R.id.item_detail_url);
		mUrl.setText(url);
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		Log.d(TAG, "start onCreateOptionsMenu");
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.itemdetail_options_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// TODO 自動生成されたメソッド・スタブ
		super.onPrepareOptionsMenu(menu);
		MenuItem mPlayItem = menu.findItem(R.id.menu_play);
		
		if (mUrl.getText().toString().length() != 0) {
			mPlayItem.setEnabled(true);
		} else {
			mPlayItem.setEnabled(false);
		}
		return true;
	}
//	@Override
//	public boolean onOptionsItemSelected(MenuItem item) {
//		switch (item.getItemId()) {
//        case R.id.menu_play:
//        	Intent i = new Intent(MusicService.ACTION_URL);
//            Uri uri = Uri.parse(input.getText().toString());
//            i.setData(uri);
//            startService(i);
//        default:
//            return super.onOptionsItemSelected(item);
//        }
//	}
}
