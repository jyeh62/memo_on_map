package net.daum.android.map.openapi.sampleapp.demos;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.LoaderManager;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import static net.daum.android.map.openapi.database.DBConst.MemoColumns.*;
import net.daum.android.map.openapi.database.DBConst;
import net.daum.android.map.openapi.sampleapp.R;

/**
 * Created by P12126 on 2015-04-27.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class MemoEditorActivity extends Activity implements LoaderManager.LoaderCallbacks<Cursor>{
    private static final String TAG = "MemoEditorActivity";

    /**
     * Standard projection for the interesting columns of a normal note.
     */
    private static final String[] PROJECTION = new String[] {
            PLACE_NAME,
            PLACE_LAT,
            PLACE_LONG,
            PLACE_TEL,
            PLACE_MEMO,
            PLACE_AREA_ID,
            PLACE_USER_ID,
            PLACE_CREATED_DATE,
            PLACE_PIN_IMAGE_URL
    };
    /** The index of the note column */
    private static final int COL_PLACE_NAME = 1;
    /** The index of the title column */
    private static final int COL_PLACE_LAT = 2;
    private static final int COL_PLACE_LONG = 3;
    private static final int COL_PLACE_TEL = 4;
    private static final int COL_PLACE_MEMO = 5;
    private static final int COL_PLACE_AREA_ID = 6;
    private static final int COL_PLACE_USER_ID = 7;
    private static final int COL_PLACE_CREATED_DATE = 8;
    private static final int COL_PLACE_PIN_IMAGE_URL = 9;

    private static final String USER_ID = "abd@gmail.com";
    private static final String PIN_IMAGE_URL = "http://123.com/pin_image";

    // This is our state data that is stored when freezing.
    private static final String ORIGINAL_CONTENT = "origContent";

    // The different distinct states the activity can be run in.
    private static final int STATE_EDIT = 0;
    private static final int STATE_INSERT = 1;

    private int mState;
    private Uri mUri;
    private Cursor mCursor;
    private EditText mName;
    private EditText mMemo;
    private EditText mTel;
    private EditText mLat;
    private EditText mLong;
    private String mOriginalContent;
    private Button mConfirm;
    private boolean mViewSet = false;

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if(mUri!=null){
            return new CursorLoader(this, mUri, PROJECTION, null, null, null);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if(mCursor != null) {
            mCursor = data;
            fillContents(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursor = null;
    }

    /**
     * A custom EditText that draws lines between each line of text that is displayed.
     */
    public static class LinedEditText extends EditText {
        private Rect mRect;
        private Paint mPaint;

        // we need this constructor for LayoutInflater
        public LinedEditText(Context context, AttributeSet attrs) {
            super(context, attrs);

            mRect = new Rect();
            mPaint = new Paint();
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setColor(0x800000FF);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            int count = getLineCount();
            Rect r = mRect;
            Paint paint = mPaint;

            for (int i = 0; i < count; i++) {
                int baseline = getLineBounds(i, r);

                canvas.drawLine(r.left, baseline + 1, r.right, baseline + 1, paint);
            }

            super.onDraw(canvas);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Intent intent = getIntent();

        // Do some setup based on the action being performed.
        final String action = intent.getAction();
        if (Intent.ACTION_EDIT.equals(action)) {
            // Requested to edit: set that state, and the data being edited.
            mState = STATE_EDIT;
            mUri = intent.getData();
        } else if (Intent.ACTION_INSERT.equals(action)) {
            // Requested to insert: set that state, and create a new entry
            // in the container.
            mState = STATE_INSERT;
            mUri = getContentResolver().insert(intent.getData(), null);
            // If we were unable to create a new note, then just finish
            // this activity.  A RESULT_CANCELED will be sent back to the
            // original activity if they requested a result.
            if (mUri == null) {
                Log.e(TAG, "Failed to insert new note into " + getIntent().getData());
                finish();
                return;
            }
            setResult(RESULT_OK, (new Intent()).setAction(mUri.toString()));
        } else {
            // Whoops, unknown action!  Bail.
            Log.e(TAG, "Unknown action, exiting");
            finish();
            return;
        }

        // Set the layout for this activity.  You can find it in res/layout/note_editor.xml
        setContentView(R.layout.place_edit);

        // The text view for our note, identified by its ID in the XML file.
        mName = (EditText) findViewById(R.id.place_name);
        mMemo = (EditText) findViewById(R.id.place_memo);
        mTel = (EditText) findViewById(R.id.place_tel);
        mLat = (EditText) findViewById(R.id.place_lat);
        mLong = (EditText) findViewById(R.id.place_long);

        mConfirm = (Button) findViewById(R.id.place_edit_done);
        mConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveNote();
            }
        });
        // Get the note!
        LoaderManager.LoaderCallbacks<Cursor> loaderCallbacks = this;
        LoaderManager loaderManager = getLoaderManager();
        loaderManager.initLoader(0, null, loaderCallbacks);

        // If an instance of this activity had previously stopped, we can
        // get the original text it started with.
        if (savedInstanceState != null) {
            mOriginalContent = savedInstanceState.getString(ORIGINAL_CONTENT);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!mViewSet) {
            fillContents(getIntent());
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // Save away the original text, so we still have it if the activity
        // needs to be killed while paused.
        outState.putString(ORIGINAL_CONTENT, mOriginalContent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // The user is going somewhere, so make sure changes are saved
        /*
        String text = mMemo.getText().toString();
        int length = text.length();

        // If this activity is finished, and there is no text, then we
        // simply delete the note entry.
        // Note that we do this both for editing and inserting...  it
        // would be reasonable to only do it when inserting.

        if (isFinishing() && (length == 0) && mCursor != null) {
            setResult(RESULT_CANCELED);
            deleteNote();
        } else {
            saveNote();
        }
        */
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        cancelNote();
    }

    private final void fillContents(Intent intent) {
        if (intent != null) {
            // Modify our overall title depending on the mode we are running in.
            if (mState == STATE_EDIT) {
                // Set the title of the Activity to include the note title
                String name = intent.getStringExtra("name");
                Resources res = getResources();
                String text = String.format(res.getString(R.string.title_edit), name);
                setTitle(text);
            } else if (mState == STATE_INSERT) {
                setTitle(getText(R.string.title_create));
            }

            // This is a little tricky: we may be resumed after previously being
            // paused/stopped.  We want to put the new text in the text view,
            // but leave the user where they were (retain the cursor position
            // etc).  This version of setText does that for us.

            String name = intent.getStringExtra("name");
            if(name != null) {
                android.util.Log.d(TAG, "fillContents " + name);
                mName.setTextKeepState(name);
            }

            String memo = intent.getStringExtra("memo");
            if(memo != null) {
                android.util.Log.d(TAG, "fillContents " + name);
                mMemo.setTextKeepState(memo);
            }

            String tel = intent.getStringExtra("tel");
            if(tel != null) {
                android.util.Log.d(TAG, "fillContents " + tel);
                mTel.setTextKeepState(tel);
            }

            String lat = intent.getStringExtra("lat");
            if(lat != null) {
                android.util.Log.d(TAG, "fillContents " + lat);
                mLat.setTextKeepState(lat);
            }

            String longitude = intent.getStringExtra("long");
            if(longitude != null) {
                android.util.Log.d(TAG, "fillContents " + longitude);
                mLong.setTextKeepState(longitude);
            }

        } else {
            setTitle(getText(R.string.error_title));
            mMemo.setText(getText(R.string.error_message));
        }
        mViewSet = true;
    }

    private final void fillContents(Cursor cursor) {
        if (cursor != null) {
            cursor.moveToFirst();
            // Modify our overall title depending on the mode we are running in.
            if (mState == STATE_EDIT) {
                // Set the title of the Activity to include the note title
                String name = cursor.getString(COL_PLACE_NAME);
                Resources res = getResources();
                String text = String.format(res.getString(R.string.title_edit), name);
                setTitle(text);
            } else if (mState == STATE_INSERT) {
                setTitle(getText(R.string.title_create));
            }

            // This is a little tricky: we may be resumed after previously being
            // paused/stopped.  We want to put the new text in the text view,
            // but leave the user where they were (retain the cursor position
            // etc).  This version of setText does that for us.
            String name = cursor.getString(COL_PLACE_NAME);
            mName.setTextKeepState(name);

            String memo = cursor.getString(COL_PLACE_MEMO);
            mMemo.setTextKeepState(memo);

            String tel = cursor.getString(COL_PLACE_TEL);
            mTel.setTextKeepState(tel);

            String lat = cursor.getString(COL_PLACE_LAT);
            mTel.setTextKeepState(lat);

            String longitude = cursor.getString(COL_PLACE_LONG);
            mTel.setTextKeepState(longitude);

            // If we hadn't previously retrieved the original text, do so
            // now.  This allows the user to revert their changes.
            if (mOriginalContent == null) {
                mOriginalContent = memo;
            }


        } else {
            setTitle(getText(R.string.error_title));
            mMemo.setText(getText(R.string.error_message));
        }
    }


    private final void saveNote() {
        // Make sure their current
        // changes are safely saved away in the provider.  We don't need
        // to do this if only editing.
        if (mCursor != null) {
            // Get out updates into the provider.
            ContentValues values = new ContentValues();

            String name = mName.getText().toString();

            StringBuilder testString = new StringBuilder();
            testString.append("name = " + name);
            if(name == null) {
                testString.append(", name = null");
                values.put(PLACE_NAME, "no name");
            }
            else
            {
                int length = name.length();
                // If we are creating a new note, then we want to also create
                // an initial title for it.

                if (length == 0) {
                    Toast.makeText(this, R.string.nothing_to_save, Toast.LENGTH_SHORT).show();
                    return;
                }
                testString.append("name = " + name);
                values.put(PLACE_NAME, name);
            }

            String memo = mMemo.getText().toString();
            if(memo == null) {
                testString.append(", memo = null");
                values.put(PLACE_MEMO, memo);
            }
            else
            {
                testString.append(", memo = " + memo);
                values.put(PLACE_MEMO, "no memo");
            }

            String tel = mTel.getText().toString();
            if(tel == null){
                testString.append(", tel = null");
                values.put(PLACE_TEL, "no tel");
            }
            else
            {
                testString.append(", tel = " + tel);
                values.put(PLACE_TEL, tel);
            }
            String lat = mLat.getText().toString();
            if(lat == null)
            {
                testString.append(", lat  = null");
                values.put(PLACE_LAT, Double.valueOf(lat));
            }
            else
            {
                testString.append(", lat = " + lat);
                values.put(PLACE_LAT, 0f);
            }
            String longitude = mLong.getText().toString();
            if(longitude == null)
            {
                testString.append("longitude,  = null");
                values.put(PLACE_LONG, Double.valueOf(longitude));
            }
            else
            {
                testString.append(", longitude = " + longitude);
                values.put(PLACE_LONG, 0f);
            }

            values.put(PLACE_USER_ID, USER_ID);
            values.put(PLACE_PIN_IMAGE_URL,PIN_IMAGE_URL);

            android.util.Log.d(TAG, "saveNote string = " + testString);
            // Commit all of our changes to persistent storage. When the update completes
            // the content provider will notify the cursor of the change, which will
            // cause the UI to be updated.
            try {
                getContentResolver().update(mUri, values, null, null);
            } catch (NullPointerException e) {
                Log.e(TAG, e.getMessage());
            }
        }
    }

    /**
     * Take care of canceling work on a note.  Deletes the note if we
     * had created it, otherwise reverts to the original text.
     */
    private final void cancelNote() {
        if (mCursor != null) {
            if (mState == STATE_EDIT) {
                // Put the original note text back into the database
                mCursor = null;
                ContentValues values = new ContentValues();
                values.put(PLACE_MEMO, mOriginalContent);
                getContentResolver().update(mUri, values, null, null);
            } else if (mState == STATE_INSERT) {
                // We inserted an empty note, make sure to delete it
                deleteNote();
            }
        }
        setResult(RESULT_CANCELED);
        finish();
    }

    /**
     * Take care of deleting a note.  Simply deletes the entry.
     */
    private final void deleteNote() {
        if (mCursor != null) {
            mCursor = null;
            getContentResolver().delete(mUri, null, null);
            mMemo.setText("");
        }
    }
}
