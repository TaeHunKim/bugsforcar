package com.neowiz.android.sample;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import net.daum.mf.speech.api.SpeechRecognizeListener;
import net.daum.mf.speech.api.SpeechRecognizerClient;
import net.daum.mf.speech.api.SpeechRecognizerManager;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

public class VoiceRecognizeService extends Service implements BugsThirdPartyApi {
	private static final String TAG = "VoiceRecognizeService";
	//protected static SpeechRecognizerClient.Builder builder;
	protected static SpeechRecognizerClient client;
	protected final Messenger mServerMessenger = new Messenger(
			new IncomingHandler(this));

	protected boolean mIsListening;
	protected volatile boolean mIsCountDownOn;

	//static final int MSG_RECOGNIZER_START_LISTENING_FIRST = 0;
	static final int MSG_RECOGNIZER_START_LISTENING = 1;
	static final int MSG_RECOGNIZER_CANCEL = 2;
	static final int MSG_RECOGNIZER_STOP = 3;
	
	
	private MetaChangeReceiver mMetaChangeReceiver = new MetaChangeReceiver();

	// variables for control intents
	public static final int NEXT = 1;
	public static final int PREV = 2;
	public static final int PLAY = 3;
	public static final int PAUSE = 4;
	public static final int OPEN = 5;
	public static final int STOP = 6;
	public static final int REPEATMODE = 7;
	public static final int SHUFFLEMODE = 8;
	
	// variables for identifying requirer of music information
	public static final int REQUEST_MAIN = 0;
	public static final int REQUEST_PLAYLIST = 1;

	private AudioManager am;
	int streamType = 3;
	float SENSITIVITY = 2;
	
	
	
	

	@Override
	public void onCreate() {
		super.onCreate();
		am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		SpeechRecognizerManager.getInstance().initializeLibrary(this);
	}
	

	
	protected void volumeUp() {
		
		int currentVolume = am.getStreamVolume(streamType);
		int maxVolume = am.getStreamMaxVolume(streamType);
		
		if (currentVolume < maxVolume) am.setStreamVolume(streamType, ++currentVolume, AudioManager.FLAG_PLAY_SOUND);
	}
	
	protected void volumeDown() {
		//am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		int currentVolume = am.getStreamVolume(streamType);
		
		if (currentVolume > 0) am.setStreamVolume(streamType, --currentVolume, AudioManager.FLAG_PLAY_SOUND);
	}
	
	class RecognizeThread extends Thread {
		public void run() {
			runclient();
		}
		
	}

	protected class IncomingHandler extends Handler {
		private WeakReference<VoiceRecognizeService> mtarget;


		IncomingHandler(VoiceRecognizeService target) {
			mtarget = new WeakReference<VoiceRecognizeService>(target);
		}

		@Override
		public void handleMessage(Message msg) {
			final VoiceRecognizeService target = mtarget.get();

			switch (msg.what) {
				
			case MSG_RECOGNIZER_START_LISTENING:
				try{
					
				if(target.mIsListening) {
					if(client != null) client.cancelRecording();
					target.mIsListening = false;
				}
				if (!target.mIsListening) {
					RecognizeThread thread = new RecognizeThread();
					thread.start();
				}

				Log.d("message", "message start listening");
				}
				catch (Exception e) {
				Log.d("message", "message start listening fail : "+e.getMessage());
				}
				break;

			case MSG_RECOGNIZER_STOP:
				if (target.mIsCountDownOn) {
					target.mIsCountDownOn = false;
					target.mNoSpeechCountDown.cancel();
				}
				target.mIsListening = false;
				if(client != null) client.stopRecording();
				Log.d("message", "message stopped recognizer"); //$NON-NLS-1$
				break;
				
			case MSG_RECOGNIZER_CANCEL:
				if (target.mIsCountDownOn) {
					target.mIsCountDownOn = false;
					target.mNoSpeechCountDown.cancel();
				}
				target.mIsListening = false;
				if(client != null) client.cancelRecording();
				Log.d("message", "message canceled recognizer"); //$NON-NLS-1$
				break;
			default:
				Log.d("message", "message etc recognizer");

			}
		}
	}

	// Count down timer for Jelly Bean work around
	protected CountDownTimer mNoSpeechCountDown = new CountDownTimer(10000, 10000) {

		@Override
		public void onTick(long millisUntilFinished) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onFinish() {
			mIsCountDownOn = false;
			Message message = Message.obtain(null, MSG_RECOGNIZER_STOP);
			try {
				mServerMessenger.send(message);
			} catch (RemoteException e) {

			}
		}
	};

	@Override
	public void onDestroy() {
		super.onDestroy();

		if (mIsCountDownOn) {
			mNoSpeechCountDown.cancel();
		}
		SpeechRecognizerManager.getInstance().finalizeLibrary();
		unregisterReceiver(mMetaChangeReceiver);
		Log.d("voicerecognize", "onDestroy");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// handleStart(intent, startId);
		Message message = Message.obtain(null, MSG_RECOGNIZER_START_LISTENING);
		try {
			mServerMessenger.send(message);
		} catch (RemoteException e) {

		}
		registerBr();

		return START_REDELIVER_INTENT;
	}
	
	private void registerBr() {
		// MUSIC
		IntentFilter filter = new IntentFilter();
		filter.addAction(PLAYSTATE_CHANGED);
		filter.addAction(META_CHANGED);
		filter.addAction(PLAYBACK_COMPLETE);
		filter.addAction(QUEUE_CHANGED);
		filter.addAction(ASYNC_OPEN_START);
		filter.addAction(ASYNC_OPEN_COMPLETE);
		filter.addAction(BUFFERING_CHANGED);
		filter.addAction(PLAYSTATE_INFO_NEW);
		filter.addAction(PROGRESS_INFO);
		filter.addAction(REPEATMODE_CHANGED);
		filter.addAction(SHUFFLEMODE_CHANGED);
		filter.addAction(ERROR);
		registerReceiver(mMetaChangeReceiver, new IntentFilter(filter));
	}


	public void runclient() {
		client = null;
		SpeechRecognizerClient.Builder builder = new SpeechRecognizerClient.Builder().setApiKey(
				"df7574b13b34c11dac7e5efb3d31ed4a").setGlobalTimeOut(11); // 발급받은 api key
		
		client = builder.build();
		client.setSpeechRecognizeListener(new SpeechRecognizeListener() {
			@Override
			public void onBeginningOfSpeech() {
				// speech input will be processed, so there is no need for count
				// down anymore
				if (mIsCountDownOn) {
					mIsCountDownOn = false;
					mNoSpeechCountDown.cancel();
				}
				// Toast.makeText(VoiceRecognizeService.this,
				// "seppch", Toast.LENGTH_SHORT).show();
				Log.d("voicerecognize", "onBeginingOfSpeech"); //$NON-NLS-1$
			}

			@Override
			public void onEndOfSpeech() {
				// mIsListening = false;
				Message message = Message.obtain(null, MSG_RECOGNIZER_STOP);
				try {
					mServerMessenger.send(message);
				} catch (RemoteException e) {

				}
				// client.stopRecording();
				Log.d("voicerecognize", "onEndOfSpeech"); //$NON-NLS-1$
			}

			@Override
			public void onError(int errorCode, String errorMsg) {

				if (mIsCountDownOn) {
					mIsCountDownOn = false;
					mNoSpeechCountDown.cancel();
				}
				mIsListening = false;
				//client = null;
				Message message = Message.obtain(null,
						MSG_RECOGNIZER_CANCEL);
				try {
					mServerMessenger.send(message);
					message = Message.obtain(null,
					MSG_RECOGNIZER_START_LISTENING);
					mServerMessenger.send(message);
				} catch (RemoteException e) {

				}
				
				Log.d("voicerecognizetest", "error = " + errorCode + " "
						+ errorMsg);
			}

			@Override
			public void onResults(Bundle results) {
				String key = SpeechRecognizerClient.KEY_RECOGNITION_RESULTS;
				ArrayList<String> mResult = results.getStringArrayList(key);
				//String[] rs = new String[mResult.size()];
				//mResult.toArray(rs);
				if(mResult.size() > 0) {
				for(String rt : mResult) {
					Log.d(TAG, rt);
					if(rt.compareTo("재생") == 0) {
						updateMusicHandler(PLAY);
						Log.d(TAG, "play");
						break;
					}
					
					if(rt.indexOf("일시") >= 0) {
						updateMusicHandler(PAUSE);
						Log.d(TAG, "PAUSE");
						break;
					}
					if(rt.compareTo("다음") == 0) {
						updateMusicHandler(NEXT);
						Log.d(TAG, "NEXT");
						break;
					}
					if(rt.compareTo("이전") == 0) {
						updateMusicHandler(PREV);
						updateMusicHandler(PREV);
						Log.d(TAG, "PREV");
						break;
					}
					if(rt.compareTo("정지") == 0) {
						updateMusicHandler(STOP);
						Log.d(TAG, "STOP");
						break;
					}
					if(rt.indexOf("올려") >= 0 || rt.indexOf("높여") >= 0) {
						volumeUp();
						Log.d(TAG, "v_up");
						break;
					}
					if(rt.indexOf("내려") >= 0 || rt.indexOf("낮춰") >= 0) {
						volumeDown();
						Log.d(TAG, "v_down");
						break;
					}
					
				}
				
				}
				//if(mResult.contains("재생")) updateMusicHandler(PLAY);
				//if(mResult.contains("정지")) updateMusicHandler(STOP);
				// mIsCountDownOn = false;
				//client = null;
				Log.d("voicerecognize", "onResults" + mResult.toString());

				// client.startRecording(false);
				//$NON-NLS-1$

			}

			@Override
			public void onAudioLevel(float arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onFinished() {
				
				mIsListening = false;
				Message message = Message.obtain(null,
						MSG_RECOGNIZER_START_LISTENING);
				try {
					mServerMessenger.send(message);
				} catch (RemoteException e) {

				}
				Log.d("voicerecognize", "onFinished");
				// TODO Auto-generated method stub
				// client.startRecording(false);

			}

			@Override
			public void onPartialResult(String arg0) {
				// TODO Auto-generated method stub
				/*
				 * String key = ""; key = SpeechRecognizer.RESULTS_RECOGNITION;
				 * ArrayList<String> mResult =
				 * partialResults.getStringArrayList(key); String[] rs = new
				 * String[mResult.size()]; mResult.toArray(rs);
				 * Toast.makeText(VoiceRecognizeService.this, ""+rs[0],
				 * Toast.LENGTH_SHORT).show();
				 * 
				 * Log.d("voicerecognize", "onpartialResults"); //$NON-NLS-1$
				 */
				//if(arg0 == "재생") updateMusicHandler(PLAY);
				//if(arg0 == "정지") updateMusicHandler(STOP);
				/*if(arg0.compareTo("재생") == 0|| arg0.compareTo("정지") == 0 || arg0.compareTo("다음")  == 0|| arg0.compareTo("이전") == 0) {*/
				if((arg0.length() >= 4) || (!arg0.startsWith("볼") && arg0.length() >= 2)) {
					Message message = Message.obtain(null,
							MSG_RECOGNIZER_STOP);
					try {
						mServerMessenger.send(message);
						// message = Message.obtain(null,
						// MSG_RECOGNIZER_START_LISTENING);
						// mServerMessenger.send(message);
					} catch (RemoteException e) {

					}	
				}
				Log.d("voicerecognize", "onpartialResults : " + arg0); //$NON-NLS-1$
			}

			@Override
			public void onReady() {
				// TODO Auto-generated method stub
				if (mIsCountDownOn) {
					mIsCountDownOn = false;
					mNoSpeechCountDown.cancel();
				}
				mIsCountDownOn = true;
				mNoSpeechCountDown.start();
				
				Log.d("voicerecognize", "onReady : "); //$NON-NLS-1$
			}

		});

		this.mIsListening = true;
		client.startRecording(false);

		//$NON-NLS-1$

	}
	
	
	
	public boolean mIsOffLineMode;

	class MetaChangeReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();

			if (action.equals(META_CHANGED)) {				
				metaChange();
			} else if (action.equals(BUFFERING_CHANGED)) {
				bufferingChange(intent);
			} else if (action.equals(PLAYSTATE_CHANGED)) {
			} else if (action.equals(PLAYBACK_COMPLETE)) {
				playStateChange(intent.getBooleanExtra("isplaying", false));
			} else if (action.equals(QUEUE_CHANGED)) {
				queuChange();
			} else if (action.equals(ASYNC_OPEN_START)) {
				ayncOpenStart();
			} else if (action.equals(ASYNC_OPEN_COMPLETE)) {
				ayncOpenComplete();
			} else if (action.equals(SHUFFLEMODE_CHANGED)) {
			} else if (action.equals(REPEATMODE_CHANGED)) {
			} else if (action.equals(PROGRESS_INFO)) {
				long position = intent.getLongExtra("position", -1);
				long duration = intent.getLongExtra("duration", -1);
				//progressInfo(position, duration);
			} else if (action.equals(PLAYSTATE_INFO_NEW)) {
				int playpos = intent.getIntExtra("playpos", -1);
				int repeatmode = intent.getIntExtra("repeatmode", -1);
				int shufflemode = intent.getIntExtra("shufflemode", -1);
				boolean isplaying = intent.getBooleanExtra("isplaying", false);
				boolean isPrepare = intent.getBooleanExtra("isPrepare", false);
				String trackTitle = intent.getStringExtra("trackTitle");
				String trackArtistNm = intent.getStringExtra("trackArtistNm");
				String trackAlbumUrl = intent.getStringExtra("trackAlbumUrl");
				
				playstateInfo(trackTitle, trackArtistNm, trackAlbumUrl, playpos, repeatmode, shufflemode, isplaying, isPrepare, -1, MainActivity.requirer);
			}
		}
	}
	
	
	protected void metaChange() {
		Log.d(TAG, "metaChange ");
		musicServiceInfo();
	}


	protected void repeatmodeChange(int type) {
	}


	protected void shufflemodeChange(int type) {
	}

	protected void bufferingChange(Intent intent) {
	}

	protected void playStateChange(boolean isPlaying) {
	}


	protected void queuChange() {
	}


	protected void ayncOpenStart() {
	}


	protected void ayncOpenComplete() {
		Log.d(TAG, "ayncOpenComplete ");
	}


	protected void playstateInfo(String trackTitle, String trackArtistNm, String trackAlbumImageUrl, int playpos, int repeatmode, int shufflemode, boolean isPlaying, boolean isPrepare, int playingType, int requestType) {

	}
	
	protected Handler mCmdMusicHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			int what = msg.what;
			switch (what) {
			case NEXT:
				musicNext();
				break;
			case PREV:
				musicPrev();
				break;
			case PLAY:
				musicPlay();
				break;
			case OPEN:
				musicOpen(msg.arg1);
				break;
			case PAUSE:
				musicPause();
				break;
			case STOP:
				musicStop();
				break;
			case REPEATMODE:
				musicRepeateMode(msg.arg1);
				break;
			case SHUFFLEMODE:
				musicShufflemode(msg.arg1);
				break;
			}
		}
	};
	
	protected void updateMusicHandler(int what) {
		mCmdMusicHandler.removeCallbacksAndMessages(null);
		mCmdMusicHandler.sendEmptyMessageDelayed(what, 300);
		Log.d(TAG, "updatemusichandler1");
	}

	protected void updateMusicHandler(int what, int args1) {
		mCmdMusicHandler.removeCallbacksAndMessages(null);
		mCmdMusicHandler.sendMessageDelayed(mCmdMusicHandler.obtainMessage(what, args1, 0), 300);
		Log.d(TAG, "updatemusichandler2");
	}

	private void musicPrev() {
		sendBroadcast("previous");
	}

	private void musicNext() {
		sendBroadcast("next");
	}

	private void musicPlay() {
		sendBroadcast("togglepause");
		Log.d(TAG, "togglepause");
	}

	private void musicOpen(int position) {
		sendBroadcast("open", "playpos", position);
	}

	private void musicPause() {
		sendBroadcast("pause");
		Log.d(TAG, "pause");
	}

	private void musicStop() {
		sendBroadcast("stop");
		Log.d(TAG, "stop");
	}

	// " MUSIC 정보를 요청하면 BR 로 데이터가 넘어온다. "
	public void musicServiceInfo() {
		sendBroadcast("info");
	}

	// " 프로그래스 정보를 요청하면 BR 로 데이터가 넘어온다. "
	public void musicProgressInfo() {
		sendBroadcast("progress");
	}

	// " BR 응답없음. "
	private void musicRepeateMode(int repeatmode) {
		sendBroadcast("repeatmode", "repeatmode", repeatmode);
	}

	private void musicShufflemode(int shufflemode) {
		sendBroadcast("shufflemode", "shufflemode", shufflemode);
	}

	private void sendBroadcast(String cmd) {
		Intent intent = new Intent(MEDIA_MESSAGE);
		intent.putExtra("command", cmd);
		this.getApplicationContext().sendBroadcast(intent);
	}

	private void sendBroadcast(String cmd, String extraCmd, int args1) {
		Log.d(TAG, extraCmd + ", " + args1);
		Intent intent = new Intent(MEDIA_MESSAGE);
		intent.putExtra("command", cmd);
		intent.putExtra(extraCmd, args1);
		this.getApplicationContext().sendBroadcast(intent);
	}

	/**
	 * 트랙아이디를 전달하면 해당 곡들을 재생한다. 123456|123010
	 * 
	 * @param trackIds
	 */
	protected void sendBroadcastTracks(String trackIds) {
		Log.d(TAG, "sendBroadcastTracks : "+trackIds);
		Intent intent = new Intent(MEDIA_MESSAGE);
		intent.putExtra("command", "open_track");
		intent.putExtra("track_ids", trackIds);
		sendBroadcast(intent);
	}

	protected void sendBroadcast(int realpos, String channel) {
		Intent intent = new Intent(MEDIA_MESSAGE);
		intent.putExtra("command", "openchanneltrack");
		intent.putExtra("channel", channel); // "tracks/new/total, tracks/genre/7080/1970ost"
		intent.putExtra("realPosition", realpos);
		sendBroadcast(intent);
	}
	

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
}