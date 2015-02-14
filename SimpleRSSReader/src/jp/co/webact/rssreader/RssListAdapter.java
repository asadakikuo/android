package jp.co.webact.rssreader;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.TextView;
/**
 * リストに格納されているデータを画面に表示するための中間クラス
 * 
 * @author asada
 *
 */
public class RssListAdapter extends ArrayAdapter<Item> {
	private LayoutInflater mInflater;
	private TextView mTitle;
	private TextView mDescr;
	/**
	 * コンストラクタ
	 * @param context リスト形式アクティビティ
	 * @param objects リストデータ
	 */
	public RssListAdapter(Context context, 
			List<Item> objects) {
		super(context, 0, objects);
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	/**
	 * 画面表示 リスト形式の行部分の表示を行う
	 * @param position 表示位置
	 * @param convertView 表示箇所
	 * @param parent 親
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		// 
		if(convertView == null) {
			view = mInflater.inflate(R.layout.item_row, null);
		}
		// 現在参照しているリストの位置からItemを取得する
		Item item = this.getItem(position);
		if (item != null) {
			// Itemから必要なデータを取り出し、それぞれTextViewにセットする
			String title = item.getTitle().toString();
			mTitle = (TextView) view.findViewById(R.id.item_title);
			mTitle.setText(title);
//			String descr = item.getDescription().toString();
//			mDescr = (TextView) view.findViewById(R.id.item_descr);
//			mDescr.setText(descr);
		}
		return view;
	}


}
