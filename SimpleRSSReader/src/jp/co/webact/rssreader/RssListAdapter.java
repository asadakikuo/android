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
 * ���X�g�Ɋi�[����Ă���f�[�^����ʂɕ\�����邽�߂̒��ԃN���X
 * 
 * @author asada
 *
 */
public class RssListAdapter extends ArrayAdapter<Item> {
	private LayoutInflater mInflater;
	private TextView mTitle;
	private TextView mDescr;
	/**
	 * �R���X�g���N�^
	 * @param context ���X�g�`���A�N�e�B�r�e�B
	 * @param objects ���X�g�f�[�^
	 */
	public RssListAdapter(Context context, 
			List<Item> objects) {
		super(context, 0, objects);
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	/**
	 * ��ʕ\�� ���X�g�`���̍s�����̕\�����s��
	 * @param position �\���ʒu
	 * @param convertView �\���ӏ�
	 * @param parent �e
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		// 
		if(convertView == null) {
			view = mInflater.inflate(R.layout.item_row, null);
		}
		// ���ݎQ�Ƃ��Ă��郊�X�g�̈ʒu����Item���擾����
		Item item = this.getItem(position);
		if (item != null) {
			// Item����K�v�ȃf�[�^�����o���A���ꂼ��TextView�ɃZ�b�g����
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
