package net.daum.android.map.openapi.database;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by P12126 on 2015-04-24.
 */
public class DBConst {
    public final static String DB_NAME = "memo_on_map.db";
    public final static String PLACE_TABLE_NAME = "place_table";
    public final static String AREA_TABLE_NAME = "area_table";
    public static final String AUTHORITY = "net.daum.android.map.openapi.database.MemoProvider";

    public static final class MemoColumns implements BaseColumns {
        // This class cannot be instantiated
        private MemoColumns() {}

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/memos");

        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of notes.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.google.memos";

        /**
         * The MIME type of a {@link #CONTENT_URI} sub-directory of a single note.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.google.memos";

        /**
         * The default sort order for this table
         */
        public static final String DEFAULT_SORT_ORDER = "created_date DESC";
        public final static String PLACE_NAME = "name";
        public final static String PLACE_LAT = "lat";
        public final static String PLACE_LONG = "long";
        public final static String PLACE_TEL = "tel";
        public final static String PLACE_MEMO = "memo";
        public final static String PLACE_AREA_ID = "area_id";
        public final static String PLACE_ID = "_id";

        public final static String AREA_ID = "area_id";
        public final static String AREA_COLOR = "area_color";
        public final static String AREA_NAME = "area_name";

        public static final String PLACE_USER_ID = "user_id";
        public static final String PLACE_CREATED_DATE ="created_date";
        public static final String PLACE_PIN_IMAGE_URL = "pin_image_url";
    }
}
