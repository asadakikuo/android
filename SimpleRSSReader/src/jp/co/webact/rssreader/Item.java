package jp.co.webact.rssreader;

public class Item {
	// 記事のタイトル
	private CharSequence mTitle;
	// 記事の本文
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
