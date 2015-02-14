package jp.co.webact.rssreader;

public class Item {
	// �L���̃^�C�g��
	private CharSequence mTitle;
	// �L���̖{��
	private CharSequence mDescription;
	// URL
	private CharSequence mUrl;
	
	public Item() {
		mTitle = "";
		mDescription = "";
		mUrl = "";
	}

	public CharSequence getDescription() {
		return mDescription;
	}

	public void setDescription(CharSequence description) {
		mDescription = description;
	}

	public CharSequence getTitle() {
		return mTitle;
	}

	public void setTitle(CharSequence title) {
		mTitle = title;
	}

	public CharSequence getUrl() {
		return mUrl;
	}

	public void setUrl(CharSequence url) {
		this.mUrl = url;
	}

}
