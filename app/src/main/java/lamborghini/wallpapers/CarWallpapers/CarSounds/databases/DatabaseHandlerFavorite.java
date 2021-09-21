package lamborghini.wallpapers.CarWallpapers.CarSounds.databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import lamborghini.wallpapers.CarWallpapers.CarSounds.models.Video;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHandlerFavorite extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "db_video_favorite";
    private static final String TABLE_NAME = "tbl_video_favorite";
    private static final String KEY_ID = "id";

    private static final String KEY_CAT_NAME = "category_name";

    private static final String KEY_VID = "vid";
    private static final String KEY_VIDEO_TITLE = "video_title";
    private static final String KEY_VIDEO_URL = "video_url";
    private static final String KEY_VIDEO_ID = "video_id";
    private static final String KEY_VIDEO_THUMBNAIL = "video_thumbnail";
    private static final String KEY_VIDEO_DURATION = "video_duration";
    private static final String KEY_VIDEO_DESCRIPTION = "video_description";
    private static final String KEY_VIDEO_TYPE = "video_type";
    private static final String KEY_TOTAL_VIEWS = "total_views";
    private static final String KEY_DATE_TIME = "date_time";

    public DatabaseHandlerFavorite(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_NAME + "("
                + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_CAT_NAME + " TEXT,"
                + KEY_VID + " TEXT,"
                + KEY_VIDEO_TITLE + " TEXT,"
                + KEY_VIDEO_URL + " TEXT,"
                + KEY_VIDEO_ID + " TEXT,"
                + KEY_VIDEO_THUMBNAIL + " TEXT,"
                + KEY_VIDEO_DURATION + " TEXT,"
                + KEY_VIDEO_DESCRIPTION + " TEXT,"
                + KEY_VIDEO_TYPE + " TEXT,"
                + KEY_TOTAL_VIEWS + " INTEGER,"
                + KEY_DATE_TIME + " TEXT"
                + ")";
        db.execSQL(CREATE_CONTACTS_TABLE);

    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);

        // Create tables again
        onCreate(db);
    }

    //Adding Record in Database

    public void AddtoFavorite(Video pj) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_CAT_NAME, pj.getCategory_name());
        values.put(KEY_VID, pj.getVid());
        values.put(KEY_VIDEO_TITLE, pj.getVideo_title());
        values.put(KEY_VIDEO_URL, pj.getVideo_url());
        values.put(KEY_VIDEO_ID, pj.getVideo_id());
        values.put(KEY_VIDEO_THUMBNAIL, pj.getVideo_thumbnail());
        values.put(KEY_VIDEO_TYPE, pj.getVideo_type());

        // Inserting Row
        db.insert(TABLE_NAME, null, values);
        db.close(); // Closing database connection

    }

    public void AddWallpapertoFavorite(String strWallpaperUrl) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_CAT_NAME, "");
        values.put(KEY_VID, "");
        values.put(KEY_VIDEO_TITLE, "");
        values.put(KEY_VIDEO_URL, strWallpaperUrl);
        values.put(KEY_VIDEO_ID, "");
        values.put(KEY_VIDEO_THUMBNAIL, strWallpaperUrl);
        values.put(KEY_VIDEO_DURATION, "");
        values.put(KEY_VIDEO_DESCRIPTION, "");
        values.put(KEY_VIDEO_TYPE, "wallpaper");
        values.put(KEY_TOTAL_VIEWS, 0);
        values.put(KEY_DATE_TIME, "");

        // Inserting Row
        db.insert(TABLE_NAME, null, values);
        db.close(); // Closing database connection

    }

    // Getting All Data
    public List<Video> getAllData() {
        List<Video> dataList = new ArrayList<Video>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_NAME + " ORDER BY id DESC";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Video contact = new Video();
                contact.setId(Integer.parseInt(cursor.getString(0)));
                contact.setCategory_name(cursor.getString(1));
                contact.setVid(cursor.getString(2));
                contact.setVideo_title(cursor.getString(3));
                contact.setVideo_url(cursor.getString(4));
                contact.setVideo_id(cursor.getString(5));
                contact.setVideo_thumbnail(cursor.getString(6));
                contact.setVideo_type(cursor.getString(9));

                // Adding contact to list
                dataList.add(contact);
            } while (cursor.moveToNext());
        }

        // return contact list
        return dataList;
    }

    public List<String> getAllDataUrl() {
        List<String> dataList = new ArrayList<String>();
        // Select All Query
        String selectQuery = "SELECT id, video_url FROM " + TABLE_NAME + " ORDER BY id DESC";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                String strTmpUrl = cursor.getString(1);
                // Adding contact to list
                dataList.add(strTmpUrl);
            } while (cursor.moveToNext());

        }

        // return contact list
        return dataList;
    }

    //getting single row
    public List<Video> getFavRow(String id) {
        List<Video> dataList = new ArrayList<Video>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_NAME + " WHERE vid=" + id;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Video contact = new Video();
                contact.setId(Integer.parseInt(cursor.getString(0)));
                contact.setCategory_name(cursor.getString(1));
                contact.setVid(cursor.getString(2));
                contact.setVideo_title(cursor.getString(3));
                contact.setVideo_url(cursor.getString(4));
                contact.setVideo_id(cursor.getString(5));
                contact.setVideo_thumbnail(cursor.getString(6));
                contact.setVideo_type(cursor.getString(9));

                // Adding contact to list
                dataList.add(contact);
            } while (cursor.moveToNext());
        }

        // return contact list
        return dataList;
    }

    public Boolean getFavRowURL(String strUrl) {
        // Select All Query
        String selectQuery = "SELECT id FROM " + TABLE_NAME + " WHERE video_url='" + strUrl + "'";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            return true;
        }else {
            return false;
        }

    }

    public List<String> getWallpaperFavRow(String strWallpaperUrl) {
        List<String> dataList = new ArrayList<String>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_NAME + " WHERE video_url='" + strWallpaperUrl + "'";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                // Adding contact to list
                dataList.add(cursor.getString(4));
            } while (cursor.moveToNext());
        }

        // return contact list
        return dataList;
    }

    //for remove favorite
    public void RemoveFav(Video contact) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, KEY_VID + " = ?",
                new String[]{String.valueOf(contact.getVid())});
        db.close();
    }

    //for remove favorite
    public void RemoveFavUrl(String strUrl) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, KEY_VIDEO_URL + " = ?",
                new String[]{String.valueOf(strUrl)});
        db.close();
    }

    public void RemoveWallpaperFav(String strWallpaperUrl) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, KEY_VIDEO_URL + " = ?",
                new String[]{String.valueOf(strWallpaperUrl)});
        db.close();
    }

    public enum DatabaseManager {
        INSTANCE;
        private SQLiteDatabase db;
        private boolean isDbClosed = true;
        DatabaseHandlerFavorite dbHelper;

        public void init(Context context) {
            dbHelper = new DatabaseHandlerFavorite(context);
            if (isDbClosed) {
                isDbClosed = false;
                this.db = dbHelper.getWritableDatabase();
            }

        }

        public boolean isDatabaseClosed() {
            return isDbClosed;
        }

        public void closeDatabase() {
            if (!isDbClosed && db != null) {
                isDbClosed = true;
                db.close();
                dbHelper.close();
            }
        }
    }

}
