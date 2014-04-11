package com.neowiz.android.sample;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.neowiz.android.sample.adapter.SampleTrackAdapter;
import com.neowiz.android.sample.task.BaseAsyncTask.OnPostExecuteListener;
import com.neowiz.android.sample.task.TrackListTask;

public class TestActivity extends BaseMusicActivity implements OnItemClickListener {

	private static final String TAG = "TestActivity";

	private TextView mTxtTitle;

//	private static final String SEARCH_TYPE_TRACK = "track";
//	private static final String SEARCH_TYPE_ALBUM = "album";
//	private static final String SEARCH_TYPE_MV = "mv";
//	private static final String SEARCH_TYPE_ESALBUM = "esalbum";
//	private static final String SEARCH_TYPE_ARTIST = "artist";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		mListView = (ListView) findViewById(R.id.list);
		mTxtTitle = (TextView) findViewById(R.id.txt_title);
		mListView.setOnItemClickListener(this);
		
		//"실시간 차트 리스트 가져오기"
//		getContents(CONTENT_URI_CHARTLIST);
		
		// "1980ost 리스트 가져오기"
//		Uri contentUri = Uri.withAppendedPath(CONTENT_URI_CHARTLIST, "tracks|genre|7080|1980ost");
//		getContents(contentUri);
		
		//"이승환 검색 결과 가져오기."
		Uri contentUri = Uri.withAppendedPath(CONTENT_URI_SEARCH, "/track/이승환");
		getContents(contentUri);
	}
	
	private Cursor mCurosr;
	private ListView mListView;
	private SampleTrackAdapter mTrackListAdapter;

	private void getContents(Uri uri) {

		final TrackListTask task = new TrackListTask(getApplicationContext());
		task.setOnPostExecuteListener(new OnPostExecuteListener<Cursor>() {
			@Override
			public void onPostExecute(Cursor cursor) {
				// "HIDE LOADING"
				if (cursor != null) {
					Log.d(TAG, "Content Count : " + cursor.getCount());
					if (mCurosr != null)
						mCurosr.close();

					int code = getErrorCode(cursor);
					if (code != SUCCESS) {
						Log.d(TAG, "ERROR");
						// " TODO DISP ERROR VIEW "
						return;
					}

					mCurosr = cursor;
					setList(cursor);
				} else {
					Log.d(TAG, "Content is null");
				}
			}
		});

		task.setTimeout(new Handler() {
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				Toast.makeText(getApplicationContext(), "데이터를 가져오지 못했습니다", Toast.LENGTH_SHORT).show();
			}
		}, 0);

		// "TODO DISP LOADING"
		task.execute(uri);
	}

	private void setList(Cursor cursor) {

		if (mTrackListAdapter == null) {
			mTrackListAdapter = new SampleTrackAdapter(getApplicationContext(), cursor);
			mListView.setAdapter(mTrackListAdapter);
		} else {
			mTrackListAdapter.changeCursor(cursor);
			mTrackListAdapter.notifyDataSetChanged();
		}
	}

	public static final int SUCCESS = 0;
	public static final int ERROR_CODE = -1;

	protected int getErrorCode(Cursor cursor) {
		cursor.moveToFirst();
		int ret = parseInt(cursor.getString(0));
		int error_code = parseInt(cursor.getString(2));
		if (ret == ERROR_CODE) {
			return error_code;
		}
		return SUCCESS;
	}

	protected int parseInt(String src) {
		try {
			return Integer.parseInt(src);
		} catch (Exception e) {
		}
		return 0;
	}

	@Override
	protected void metaChange() {
		// TODO Auto-generated method stub
		Log.d(TAG, "metaChange ");
		musicServiceInfo();
	}

	@Override
	protected void repeatmodeChange(int type) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void shufflemodeChange(int type) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void bufferingChange(Intent intent) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void playStateChange(boolean isPlaying) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void queuChange() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void ayncOpenStart() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void ayncOpenComplete() {
		// TODO Auto-generated method stub
		Log.d(TAG, "ayncOpenComplete ");
	}

	@Override
	protected void playstateInfo(String trackTitle, String trackArtistNm, String trackAlbumImageUrl, int playpos, int repeatmode, int shufflemode, boolean isPlaying, boolean isPrepare, int playingType) {
		// TODO Auto-generated method stub
		Log.d(TAG, "playstateInfo " + trackTitle);
		mTxtTitle.setText(trackTitle);
	}

	@Override
	protected void progressInfo(long position, long duration) {
		// TODO Auto-generated method stub
		Log.d(TAG, "position " + position);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Log.d(TAG, "playpos " + position + " / " + id);
		
//		sendBroadcast(position, null); // "실시간 차트 리스트를 포함하여 재생."
//		sendBroadcast(position, "tracks/genre/7080/1980ost"); // "1980ost OST 리스트를 포함하여 재생."
//		updateMusicHandler(OPEN, position); // "플레이리스트의 POS 해당하는 곡을 재생."
		
		sendBroadcastTracks(String.valueOf(id)+"|"+3174962); // "tracks id 에 해당하는 곡을 재생 / 검색후 id 를 보내서 재생하면된다."
	}

	// "플레이어 EVENT"
	public void onPlayer(View v) {
		int id = v.getId();
		if (id == R.id.btn_play) {
			updateMusicHandler(PLAY);
		} else if (id == R.id.btn_next) {
			updateMusicHandler(NEXT);
		} else if (id == R.id.btn_pre) {
			updateMusicHandler(PREV);
		} else {
		}
	}
}
