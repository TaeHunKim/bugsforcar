package com.neowiz.android.sample.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.neowiz.android.sample.R;

public class SampleTrackAdapter extends SimpleCursorAdapter {

	private static final String TAG = "PlayListSongAdapter";

	private int mIdxID;
	private int mIdxTrackTitle;
	private int mIdxArtist;
	private int mIdxAlbum;

	private int mIdxImageUrl;
	private int mIdxSmallImageUrl;
	private int mIdxDuration;

	private int mIdxIsAdult;
	private int mIdxState;
	private int mIdxData;
	private int mIdxIsListenable;
	private int mIdxRestrict;

	private int mPlayMode;

	public SampleTrackAdapter(Context context, Cursor c) {

		super(context, 0, c, new String[] {}, new int[] {});

		getColumnIdx(c);
	}

	private void getColumnIdx(Cursor c) {

		if (c == null)
			return;
		mIdxID = c.getColumnIndex("_id");
		mIdxTrackTitle = c.getColumnIndex("track_title");
		mIdxArtist = c.getColumnIndex("artist_nm");
		mIdxAlbum = c.getColumnIndex("album_title");

		mIdxImageUrl = c.getColumnIndex("album_image_url");
		mIdxSmallImageUrl = c.getColumnIndex("album_small_image_url");
		mIdxDuration = c.getColumnIndex("duration");

		mIdxIsAdult = c.getColumnIndex("adult");
		mIdxState = c.getColumnIndex("state");
		mIdxData = c.getColumnIndex("local");
		mIdxIsListenable = c.getColumnIndex("islistenable");
		mIdxRestrict = c.getColumnIndex("restrict");

	}

	public void setPlayMode(int playMode) {
		mPlayMode = playMode;
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {

		View v = new TrackView(context);

		ViewHolder vh = new ViewHolder();
		vh.mTrackTitle = (TextView) v.findViewById(R.id.track_title);
		vh.mArtistNm = (TextView) v.findViewById(R.id.artist_nm);
		vh.mAlbumTitle = (TextView) v.findViewById(R.id.album_title);
		// vh.mChangeableIcon = (ImageView)
		// v.findViewById(R.id.track_changeable_icon);

		v.setTag(vh);
		return v;
	}

	private long mNowplayTrackId;

	public void setNowplayTrackId(long trackId) {
		mNowplayTrackId = trackId;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {

		ViewHolder vh = (ViewHolder) view.getTag();
		vh.mTrackTitle.setText(cursor.getString(mIdxTrackTitle));
		vh.mArtistNm.setText(cursor.getString(mIdxArtist));
		vh.mAlbumTitle.setText(cursor.getString(mIdxAlbum));

		TrackView trackView = (TrackView) view;
		boolean isPlaying = (mNowplayTrackId == cursor.getLong(mIdxID));

		int restrictType = -1;

		if (mIdxRestrict > 0)
			restrictType = parseInt(cursor.getString(mIdxRestrict));
		trackView.setTrackState(isPlaying, cursor.getString(mIdxIsListenable), cursor.getString(mIdxState), cursor.getString(mIdxIsAdult), cursor.getString(mIdxData), restrictType);
	}

	private int parseInt(String index) {
		try {
			return Integer.parseInt(index);
		} catch (Exception e) {
		}
		return 0;
	}

	public Object[] getSections() {
		return null;
	}

	static class ViewHolder {
		TextView mTrackTitle;
		TextView mArtistNm;
		TextView mAlbumTitle;
		ImageView mChangeableIcon;
	}

}
