package com.neowiz.android.sample;

import java.io.BufferedInputStream;
import java.net.URL;
import java.net.URLConnection;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.neowiz.android.sample.adapter.SampleTrackAdapter;
import com.neowiz.android.sample.task.BaseAsyncTask.OnPostExecuteListener;
import com.neowiz.android.sample.task.TrackListTask;

public class MainActivity extends BaseMusicActivity {
	private static final String TAG = "MainActivity";
	private AudioManager am;
	
	// variables for creating list
	private static Cursor mCursor;
	private SampleTrackAdapter mTrackListAdapter;
	
	// variables for motion events
	float xp1 = 0, xp2 = 0, xp3 = 0, yp1 = 0, yp2 = 0, yp3 = 0;
	final double RATIO = 1.5;
	
	// variables for volume control
	int streamType = 3;
	float SENSITIVITY = 2;
	
	// variables for meta data
	private TextView mTxtTitle;
	private TextView mTxtArtist;
	private ImageView mAlbumCover;
	
	// variables for searching list
	private static final int ARTIST = 0;
	private static final int COVER = 1;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		mTxtTitle = (TextView) findViewById(R.id.txt_title);
		mTxtArtist = (TextView) findViewById(R.id.txt_artist);
		mAlbumCover = (ImageView) findViewById(R.id.album_cover);
		
		getContents(CONTENT_URI_CHARTLIST);
		
		sendBroadcast(0, "charts/track/realtime");
		
		musicServiceInfo();
	}

	private void getContents(Uri uri) {

		final TrackListTask task = new TrackListTask(getApplicationContext());
		task.setOnPostExecuteListener(new OnPostExecuteListener<Cursor>() {
			@Override
			public void onPostExecute(Cursor cursor) {
				// "HIDE LOADING"
				if (cursor != null) {
					Log.d(TAG, "Content Count : " + cursor.getCount());
					if (mCursor != null) mCursor.close();

					int code = getErrorCode(cursor);
					
					if (code != SUCCESS) {
						Log.d(TAG, "ERROR");
						// " TODO DISP ERROR VIEW "
						return;
					}
					
					mCursor = cursor;
					
					setList(cursor);				
				}
				else Log.d(TAG, "Content is null");
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
		URL url;
		URLConnection conn;
		BufferedInputStream bis;
		Bitmap bm;
		
		try {	
			url = new URL(search(mCursor, playpos, COVER));
			conn = url.openConnection();
			conn.connect();
			bis = new BufferedInputStream(conn.getInputStream());
			bm = BitmapFactory.decodeStream(bis);
			
			bis.close();
			
			mAlbumCover.setImageBitmap(bm);
		} catch (Exception e) {}
		
		mTxtTitle.setText(trackTitle);
		mTxtArtist.setText(search(mCursor, playpos, ARTIST));
	}

	@Override
	protected void progressInfo(long position, long duration) {
		// TODO Auto-generated method stub
		Log.d(TAG, "position " + position);
	}
	
	protected void volumeUp() {
		am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		int currentVolume = am.getStreamVolume(streamType);
		int maxVolume = am.getStreamMaxVolume(streamType);
		
		if (currentVolume < maxVolume) am.setStreamVolume(streamType, ++currentVolume, AudioManager.FLAG_PLAY_SOUND);
	}
	
	protected void volumeDown() {
		am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		int currentVolume = am.getStreamVolume(streamType);
		
		if (currentVolume > 0) am.setStreamVolume(streamType, --currentVolume, AudioManager.FLAG_PLAY_SOUND);
	}
	
	protected String search(Cursor cursor, int index, int resultType) {		
		if (cursor == null) Log.d(TAG, "void cursor");
		else {
			cursor.moveToPosition(index);
			
			switch (resultType) {
				case ARTIST: return cursor.getString(2);
				case COVER: return cursor.getString(4);
			}
		}
		
		return null;
	}
	
	public static Cursor getCursor() {
		return mCursor;
	}
	
	public boolean onTouchEvent(MotionEvent event) {
		super.onTouchEvent(event);
		
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			xp1 = event.getX();
			yp1 = event.getY();
		}
		if (event.getAction() == MotionEvent.ACTION_MOVE) {
			float width = 0, height = 0;
			
			xp3 = event.getX();
			yp3 = event.getY();
			
			if (xp2 != 0 && Math.abs(yp2 - yp3) > SENSITIVITY) {
				width = xp3 - xp2;
				height = yp3 - yp2;
			} 
			
			if (Math.abs(height) > Math.abs(width) * RATIO) {
				if (height < 0) volumeUp();
				else volumeDown();
			}
			
			xp2 = xp3;
			yp2 = yp3;
		}
		
		if (event.getAction() == MotionEvent.ACTION_UP) {
			if (xp3 > 0) {
				float width = xp3 - xp1;
				float height = yp3 - yp1;
				
				if (Math.abs(width) > Math.abs(height) * RATIO) {
					if (width < 0) updateMusicHandler(NEXT);
					else updateMusicHandler(PREV);
				}
			}
			else updateMusicHandler(PLAY);
			
			xp1 = 0;
			xp2 = 0;
			xp3 = 0;
			yp1 = 0;
			yp2 = 0;
			yp3 = 0;
		}
		
		return false;
	}
	
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		
		menu.add(0, 1, 0, "재생목록");
		menu.add(0, 2, 0, "환경설정");
		menu.add(0, 3, 0, "도움말");
		
		return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case 1:
				Intent intent = new Intent(getApplicationContext(), PlayList.class);
				
				startActivity(intent);
				
				return true;
			case 2:
				Toast.makeText(this, "환경설정", Toast.LENGTH_SHORT).show();
				return true;
			case 3:
				Toast.makeText(this, "도움말", Toast.LENGTH_SHORT).show();
				return true;
			default:
				return false;
		}
	}
}
