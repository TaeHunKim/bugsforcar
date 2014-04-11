package com.neowiz.android.sample.task;

import java.util.Timer;
import java.util.TimerTask;

import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

public abstract class BaseAsyncTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {

	private OnPostExecuteListener<Result> mOnPostExceuteListener;
	private OnProgressUpdateListener<Progress> mOnProgressListener;

	private int mTimeout = 20 * 1000;
	private Handler mHandler;
	private int mType;

	public void setTimeout(Handler handler, int type) {
		this.mHandler = handler;
		this.mType = type;
	}

	public void setTimeout(int timeout, Handler handler, int type) {
		this.mTimeout = timeout;
		this.mHandler = handler;
		this.mType = type;
	}

	protected void onPreExecute() {
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			public void run() {
				boolean ret = selfCancle();
				if (ret) {
					try {
						mHandler.sendEmptyMessage(mType);
					} catch (Exception e) {
						Log.e("BaseAsyncTask", "Time out : error" + e.getMessage());
					}
				}
			}
		}, mTimeout);
	}

	private boolean selfCancle() {
		return cancel(true);
	}

	@Override
	protected void onPostExecute(Result result) {
		super.onPostExecute(result);
		if (mOnPostExceuteListener != null) {
			mOnPostExceuteListener.onPostExecute(result);
		}
	}

	@Override
	protected void onProgressUpdate(Progress... values) {
		super.onProgressUpdate(values);
		if (mOnProgressListener != null)
			mOnProgressListener.onProgressUpdate(values);
	}

	public void setOnPostExecuteListener(OnPostExecuteListener<Result> listener) {
		mOnPostExceuteListener = listener;
	}

	public void setOnProgressUpdateListener(OnProgressUpdateListener<Progress> listener) {
		mOnProgressListener = listener;
	}

	public void setTimeLimit(Handler handler, long limit, int what) {
		handler.sendEmptyMessageDelayed(what, limit);
	}

	// interface....
	public static interface OnProgressUpdateListener<Progress> {
		void onProgressUpdate(Progress... values);
	}

	public static interface OnPostExecuteListener<Result> {
		void onPostExecute(Result result);
	}

}
