package com.example.yirae;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class StoryDatabaseHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "yirae_stories.db";
    public static final int DATABASE_VERSION = 3;

    public static final String TABLE_STORIES = "stories";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_PLACE = "place";
    public static final String COLUMN_PEOPLE = "people";
    public static final String COLUMN_MEMORY_TEXT = "memory_text";
    public static final String COLUMN_TAGS = "tags";
    public static final String COLUMN_IMAGE_URIS = "image_uris";
    public static final String COLUMN_FAVORITE = "favorite";
    public static final String COLUMN_REMOTE_STORY_TITLE = "remote_story_title";
    public static final String COLUMN_REMOTE_STORY_CONTENT = "remote_story_content";
    public static final String COLUMN_REMOTE_STORY_IMAGE_URI = "remote_story_image_uri";

    private static final String SQL_CREATE_STORIES =
            "CREATE TABLE " + TABLE_STORIES + " ("
                    + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + COLUMN_TITLE + " TEXT NOT NULL, "
                    + COLUMN_DATE + " TEXT NOT NULL, "
                    + COLUMN_PLACE + " TEXT NOT NULL, "
                    + COLUMN_PEOPLE + " TEXT NOT NULL, "
                    + COLUMN_MEMORY_TEXT + " TEXT NOT NULL, "
                    + COLUMN_TAGS + " TEXT NOT NULL DEFAULT '[]', "
                    + COLUMN_IMAGE_URIS + " TEXT NOT NULL, "
                    + COLUMN_FAVORITE + " INTEGER NOT NULL DEFAULT 0, "
                    + COLUMN_REMOTE_STORY_TITLE + " TEXT NOT NULL DEFAULT '', "
                    + COLUMN_REMOTE_STORY_CONTENT + " TEXT NOT NULL DEFAULT '', "
                    + COLUMN_REMOTE_STORY_IMAGE_URI + " TEXT NOT NULL DEFAULT ''"
                    + ")";

    public StoryDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_STORIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE " + TABLE_STORIES + " ADD COLUMN " + COLUMN_REMOTE_STORY_TITLE + " TEXT NOT NULL DEFAULT ''");
            db.execSQL("ALTER TABLE " + TABLE_STORIES + " ADD COLUMN " + COLUMN_REMOTE_STORY_CONTENT + " TEXT NOT NULL DEFAULT ''");
            db.execSQL("ALTER TABLE " + TABLE_STORIES + " ADD COLUMN " + COLUMN_REMOTE_STORY_IMAGE_URI + " TEXT NOT NULL DEFAULT ''");
        }
        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE " + TABLE_STORIES + " ADD COLUMN " + COLUMN_TAGS + " TEXT NOT NULL DEFAULT '[]'");
        }
    }
}
