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
 * RSS�f�[�^���o�悤�̔񓯊��^�X�N
 * 
 * Params�FURL, Progress�F �H, Result�F���X�g�A�_�v�^
 * @author asada
 *
 */
public class RssParserTask extends AsyncTask<String, Integer, RssListAdapter> {
	private RssReaderActivity mActivity;
	private RssListAdapter mAdapter;
	private ProgressDialog mProgressDialog;
	/**
	 * �R���X�g���N�^
	 * @param activity �A�N�e�B�r�e�B
	 * @param adapter ���X�g�A�_�v�^
	 */
	public RssParserTask(RssReaderActivity activity, RssListAdapter adapter) {
		mActivity = activity;
		mAdapter = adapter;
	}
	/**
	 * �O����
	 * �^�X�N�����s��������ɃR�[�������
	 */
	@Override
	protected void onPreExecute() {
		// �v���O���X�o�[��\������
		mProgressDialog = new ProgressDialog(mActivity);
		mProgressDialog.setMessage("Now Loading...");
		mProgressDialog.show();
	}
	/**
	 * �o�b�N�O�����E���h����
	 * �o�b�N�O�����E���h�X���b�h����Ăяo����
	 * �o�b�N�O���E���h�ɂ����鏈����S���B�^�X�N���s���ɓn���ꂽ�l�������Ƃ���
	 * @param params RSS��URL
	 */
	@Override
	protected RssListAdapter doInBackground(String... params) {
		RssListAdapter result = null;
		try {
			// �p������[�^�Ŏw�肵��URL��XML�f�[�^���擾
			// HTTP�o�R�ŃA�N�Z�X���AInputStream���擾����
			URL url = new URL(params[0]);
			InputStream is = url.openConnection().getInputStream();
			// XML�����
			result = parseXml(is);
		} catch (Exception e) {
			e.printStackTrace();
		}
		// �����ŕԂ����l�́AonPostExecute���\�b�h�̈����Ƃ��ēn�����
		return result;
	}
	// ���C���X���b�h��Ŏ��s�����
	/**
	 * �㏈��
	 * ���ʂ��i�[
	 */
	@Override
	protected void onPostExecute(RssListAdapter result) {
		mProgressDialog.dismiss();
		mActivity.setListAdapter(result);
	}

	/**
	 * XML�����
	 * 
	 * @param is XML�f�[�^
	 * @return ���X�g�A�_�v�^
	 * @throws IOException
	 * @throws XmlPullParserException
	 */
	public RssListAdapter parseXml(InputStream is) throws IOException, XmlPullParserException {
		XmlPullParser parser = Xml.newPullParser();
		try {
			// XML�p�[�T�[�Ƀf�[�^���i�[
			parser.setInput(is, null);
			int eventType = parser.getEventType();
			Item currentItem = null;
			// xml�̃^�O�����
			while (eventType != XmlPullParser.END_DOCUMENT) {
				String tag = null;
				switch (eventType) {
					case XmlPullParser.START_TAG:
						// �^�O�̊J�n
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
						// �^�O�̏I��
						tag = parser.getName();
						// �^�O�̖��̂��擾
						if (tag.equals("item")) {
							mAdapter.add(currentItem);
						}
						break;
				}
				// �^�O��ǂݍ���
				eventType = parser.next();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		// ���ʂ�߂�
		return mAdapter;
	}
}
