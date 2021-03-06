package com.neowiz.android.sample.task;


public class TrackListTask extends BaseAsyncTask<Uri, Integer, Cursor> {

	private static final String TAG = "TrackListTask";
	private Context mContext;

	public TrackListTask(Context context) {
		mContext = context;
	}

	@Override
	protected Cursor doInBackground(Uri... params) {
		ContentResolver resolver = mContext.getContentResolver();
		Cursor cursor = resolver.query(params[0], new String[] {}, null, null, null);
		return cursor;
	}

	@Override
	protected void onPostExecute(Cursor result) {
		super.onPostExecute(result);
	}

}
