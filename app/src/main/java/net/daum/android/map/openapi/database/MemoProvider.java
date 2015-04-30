package net.daum.android.map.openapi.database;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.LiveFolders;
import android.text.TextUtils;

import java.util.HashMap;

import static net.daum.android.map.openapi.database.DBConst.MemoColumns.*;


/**
 * Created by P12126 on 2015-04-24.
 */
public class MemoProvider extends ContentProvider{
    private static final String TAG = "MemoProvider";

    private static HashMap<String, String> sMemoProjectionMap;
    private static HashMap<String, String> sLiveFolderProjectionMap;

    private static final int MEMOS = 1;
    private static final int MEMO_ID = 2;
    private static final int AREAS = 3;

    private static final UriMatcher sUriMatcher;

    /**
     * This class helps open, create, and upgrade the database file.
     */
    private static class MemoDataBaseHelper extends SQLiteOpenHelper {

        public final static int VERSION = 1;

        private MemoDataBaseHelper(Context context) {
            super(context, DBConst.DB_NAME, null, VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + DBConst.PLACE_TABLE_NAME + "(" +  PLACE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + PLACE_NAME + " TEXT, "
                    + PLACE_LAT + " REAL, "
                    + PLACE_LONG + " REAL, "
                    + PLACE_TEL + " TEXT, "
                    + PLACE_MEMO + " TEXT, "
                    + PLACE_AREA_ID + " TEXT, "
                    + PLACE_USER_ID + " TEXT, "
                    + PLACE_CREATED_DATE + " INTEGER, "
                    + PLACE_PIN_IMAGE_URL + " TEXT);");

            db.execSQL("CREATE TABLE " + DBConst.AREA_TABLE_NAME + "(" +  AREA_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + AREA_NAME + " TEXT, "
                    + AREA_COLOR + " TEXT);");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + DBConst.PLACE_TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + AREA_NAME);
        }
    }

    private MemoDataBaseHelper mOpenHelper;

    @Override
    public boolean onCreate() {
        mOpenHelper = new MemoDataBaseHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(DBConst.PLACE_TABLE_NAME);

        switch (sUriMatcher.match(uri)) {
            case MEMOS:
                qb.setProjectionMap(sMemoProjectionMap);
                break;

            case MEMO_ID:
                qb.setProjectionMap(sMemoProjectionMap);
                qb.appendWhere(PLACE_ID + "=" + uri.getPathSegments().get(1));
                break;

            case AREAS:
                qb.setProjectionMap(sLiveFolderProjectionMap);
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        // If no sort order is specified use the default
        String orderBy;
        if (TextUtils.isEmpty(sortOrder)) {
            orderBy = DEFAULT_SORT_ORDER;
        } else {
            orderBy = sortOrder;
        }
        android.util.Log.d(TAG, "query : uri = " + uri);
        // Get the database and run the query
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        android.util.Log.d(TAG, "query : projection = " + projection + ", selection = " + selection + ", selectionArgs = " + selectionArgs);
        Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, orderBy);

        // Tell the cursor what uri to watch, so it knows when its source data changes
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Override
    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case MEMOS:
            case AREAS:
                return CONTENT_TYPE;

            case MEMO_ID:
                return CONTENT_ITEM_TYPE;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues initialValues) {
        // Validate the requested uri
        if (sUriMatcher.match(uri) != MEMOS) {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        ContentValues values;
        if (initialValues != null) {
            values = new ContentValues(initialValues);
        } else {
            values = new ContentValues();
        }

        Long now = Long.valueOf(System.currentTimeMillis());

        // Make sure that the fields are all set
        if (values.containsKey(PLACE_CREATED_DATE) == false) {
            values.put(PLACE_CREATED_DATE, now);
        }

        if (values.containsKey(PLACE_NAME) == false) {
            Resources r = Resources.getSystem();
            values.put(PLACE_NAME, r.getString(android.R.string.untitled));
        }

        if (values.containsKey(PLACE_MEMO) == false) {
            values.put(PLACE_MEMO, "");
        }
        if (values.containsKey(PLACE_TEL) == false) {
            values.put(PLACE_TEL, "");
        }
        if (values.containsKey(PLACE_PIN_IMAGE_URL) == false) {
            values.put(PLACE_PIN_IMAGE_URL, "");
        }

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        long rowId = db.insert(DBConst.PLACE_TABLE_NAME, PLACE_MEMO, values);
        if (rowId > 0) {
            Uri noteUri = ContentUris.withAppendedId(CONTENT_URI, rowId);
            getContext().getContentResolver().notifyChange(noteUri, null);
            return noteUri;
        }

        throw new SQLException("Failed to insert row into " + uri);
    }

    @Override
    public int delete(Uri uri, String where, String[] whereArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count;
        switch (sUriMatcher.match(uri)) {
            case MEMOS:
                count = db.delete(DBConst.PLACE_TABLE_NAME, where, whereArgs);
                break;

            case MEMO_ID:
                String memoId = uri.getPathSegments().get(1);
                count = db.delete(DBConst.PLACE_TABLE_NAME, PLACE_ID + "=" + memoId
                        + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count;
        switch (sUriMatcher.match(uri)) {
            case MEMOS:
                count = db.update(DBConst.PLACE_TABLE_NAME, values, where, whereArgs);
                break;

            case MEMO_ID:
                String noteId = uri.getPathSegments().get(1);
                count = db.update(DBConst.PLACE_TABLE_NAME, values, PLACE_ID + "=" + noteId
                        + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(DBConst.AUTHORITY, "memos", MEMOS);
        sUriMatcher.addURI(DBConst.AUTHORITY, "memos/#", MEMO_ID);
        sUriMatcher.addURI(DBConst.AUTHORITY, "areas", AREAS);

        sMemoProjectionMap = new HashMap<String, String>();
        sMemoProjectionMap.put(PLACE_ID, PLACE_ID);
        sMemoProjectionMap.put(PLACE_NAME, PLACE_NAME);
        sMemoProjectionMap.put(PLACE_LAT, PLACE_LAT);
        sMemoProjectionMap.put(PLACE_LONG, PLACE_LONG);
        sMemoProjectionMap.put(PLACE_TEL, PLACE_TEL);
        sMemoProjectionMap.put(PLACE_MEMO, PLACE_MEMO);
        sMemoProjectionMap.put(PLACE_AREA_ID, PLACE_AREA_ID);
        sMemoProjectionMap.put(PLACE_USER_ID, PLACE_USER_ID);
        sMemoProjectionMap.put(PLACE_PIN_IMAGE_URL, PLACE_PIN_IMAGE_URL);
        sMemoProjectionMap.put(PLACE_CREATED_DATE, PLACE_CREATED_DATE);
    }


}
