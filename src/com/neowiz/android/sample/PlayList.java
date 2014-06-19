package com.neowiz.android.sample;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.neowiz.android.sample.adapter.SampleTrackAdapter;

public class PlayList extends BaseMusicActivity implements OnItemClickListener {
	private static final String TAG = "PlayList"; 
	private ListView mListView;
	private SampleTrackAdapter mTrackListAdapter;
	Cursor mCursor;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.playlist);
		
		mListView = (ListView) findViewById(R.id.list);
		mListView.setOnItemClickListener(this);
		mCursor = MainActivity.getCursor();
		
		getList();
	}
	
	private void getList() {
		if (mCursor != null) {
			ListAdapter listAdapter = new ListAdapter(this, mCursor);
			mListView.setAdapter(listAdapter);
		}
	}
	
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		//sendBroadcastTracks(String.valueOf(id)+"|"+3174962);
		
		Intent intent = new Intent();
		intent.putExtra("playPos", position);
		setResult(RESULT_OK, intent);
		
		finish();
	}
	
	protected void metaChange() {}
	protected void repeatmodeChange(int type) {}
	protected void shufflemodeChange(int type) {}
	protected void bufferingChange(Intent intent) {}
	protected void playStateChange(boolean isPlaying) {}
	protected void queuChange() {}
	protected void ayncOpenStart() {}
	protected void ayncOpenComplete() {}
	protected void playstateInfo(String trackTitle, String trackArtistNm, String trackAlbumImageUrl, int playpos, int repeatmode, int shufflemode, boolean isPlaying, boolean isPrepare, int playingType, int requestType) {}
	protected void progressInfo(long position, long duration) {}
	
	private class ListAdapter extends CursorAdapter {
		public ListAdapter(Context context, Cursor c) {
			super(context, c);
		}
		
		public void bindView(View view, Context context, Cursor cursor) {
			final TextView title = (TextView)view.findViewById(R.id.track_title);
			final TextView artist = (TextView)view.findViewById(R.id.artist_nm);
			final TextView album = (TextView)view.findViewById(R.id.album_title);
			  
			title.setText(cursor.getString(1));
			artist.setText(cursor.getString(2));
			album.setText(cursor.getString(3));
		}
		
		public View newView(Context context, Cursor cursor, ViewGroup parent) {  
			LayoutInflater inflater = LayoutInflater.from(context);
			View v = inflater.inflate(R.layout.music_track_item, parent, false);
			return v;
		}
	}
}