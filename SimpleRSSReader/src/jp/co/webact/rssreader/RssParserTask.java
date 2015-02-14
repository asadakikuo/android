package jp.co.webact.rssreader;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;
import android.util.Xml;

/**
 * RSSデータ抽出ようの非同期タスク
 * 
 * Params：URL, Progress： ？, Result：リストアダプタ
 * @author asada
 *
 */
public class RssParserTask extends AsyncTask<String, Integer, RssListAdapter> {
	private RssReaderActivity mActivity;
	private RssListAdapter mAdapter;
	private ProgressDialog mProgressDialog;
	/**
	 * コンストラクタ
	 * @param activity アクティビティ
	 * @param adapter リストアダプタ
	 */
	public RssParserTask(RssReaderActivity activity, RssListAdapter adapter) {
		mActivity = activity;
		mAdapter = adapter;
	}
	/**
	 * 前処理
	 * タスクを実行した直後にコールされる
	 */
	@Override
	protected void onPreExecute() {
		// プログレスバーを表示する
		mProgressDialog = new ProgressDialog(mActivity);
		mProgressDialog.setMessage("Now Loading...");
		mProgressDialog.show();
	}
	/**
	 * バックグランウンド処理
	 * バックグランウンドスレッドから呼び出され
	 * バックグラウンドにおける処理を担う。タスク実行時に渡された値を引数とする
	 * @param params RSSのURL
	 */
	@Override
	protected RssListAdapter doInBackground(String... params) {
		RssListAdapter result = null;
		try {
			// パラｰメータで指定したURLのXMLデータを取得
			// HTTP経由でアクセスし、InputStreamを取得する
			URL url = new URL(params[0]);
			InputStream is = url.openConnection().getInputStream();
			// XMLを解析
			result = parseXml(is);
		} catch (Exception e) {
			e.printStackTrace();
		}
		// ここで返した値は、onPostExecuteメソッドの引数として渡される
		return result;
	}
	// メインスレッド上で実行される
	/**
	 * 後処理
	 * 結果を格納
	 */
	@Override
	protected void onPostExecute(RssListAdapter result) {
		mProgressDialog.dismiss();
		mActivity.setListAdapter(result);
	}

	/**
	 * XMLを解析
	 * 
	 * @param is XMLデータ
	 * @return リストアダプタ
	 * @throws IOException
	 * @throws XmlPullParserException
	 */
	public RssListAdapter parseXml(InputStream is) throws IOException, XmlPullParserException {
		XmlPullParser parser = Xml.newPullParser();
		try {
			// XMLパーサーにデータを格納
			parser.setInput(is, null);
			int eventType = parser.getEventType();
			Item currentItem = null;
			// xmlのタグを解析
			while (eventType != XmlPullParser.END_DOCUMENT) {
				String tag = null;
				switch (eventType) {
					case XmlPullParser.START_TAG:
						// タグの開始
						tag = parser.getName();
						if (tag.equals("item")) {
							currentItem = new Item();
						} else if (currentItem != null) {
							if (tag.equals("title")) {
								currentItem.setTitle(parser.nextText());
							} else if (tag.equals("description")) {
								currentItem.setDescription(parser.nextText());
							} else if (tag.equals("enclosure")) {
								for(int i = 0;i < parser.getAttributeCount();i++) {
									if (parser.getAttributeName(i).equals("url")) {
										String url = parser.getAttributeValue(i);										
										currentItem.setUrl(url);
									}
								}
							}
						}
						break;
					case XmlPullParser.END_TAG:
						// タグの終了
						tag = parser.getName();
						// タグの名称を取得
						if (tag.equals("item")) {
							mAdapter.add(currentItem);
						}
						break;
				}
				// タグを読み込む
				eventType = parser.next();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		// 結果を戻す
		return mAdapter;
	}
}
