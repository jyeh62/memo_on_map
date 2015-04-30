package net.daum.android.map.openapi.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import static net.daum.android.map.openapi.database.DBConst.MemoColumns.*;
/**
 * Created by P12126 on 2015-04-24.
 */
public class MemoDataBaseHelper extends SQLiteOpenHelper {

    public final static int VERSION = 1;
    private static volatile MemoDataBaseHelper mInstance;

    public static MemoDataBaseHelper getInstance(Context context){
        if(mInstance == null){
            synchronized(MemoDataBaseHelper.class) {
                if(mInstance == null){
                    mInstance = new MemoDataBaseHelper(context);
                }
            }
        }
        return mInstance;
    }

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
                + PLACE_CREATED_DATE + " TEXT, "
                + PLACE_PIN_IMAGE_URL + " TEXT);");

        db.execSQL("CREATE TABLE " + DBConst.AREA_TABLE_NAME + "(" +  AREA_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + AREA_NAME + " TEXT, "
                + AREA_COLOR + " TEXT);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + DBConst.PLACE_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + AREA_NAME);;
    }


}
