package com.example.yirae;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class SQLiteStoryDao implements StoryDao {
    private static final String[] STORY_COLUMNS = {
            StoryDatabaseHelper.COLUMN_ID,
            StoryDatabaseHelper.COLUMN_TITLE,
            StoryDatabaseHelper.COLUMN_DATE,
            StoryDatabaseHelper.COLUMN_PLACE,
            StoryDatabaseHelper.COLUMN_PEOPLE,
            StoryDatabaseHelper.COLUMN_MEMORY_TEXT,
            StoryDatabaseHelper.COLUMN_TAGS,
            StoryDatabaseHelper.COLUMN_IMAGE_URIS,
            StoryDatabaseHelper.COLUMN_FAVORITE,
            StoryDatabaseHelper.COLUMN_REMOTE_STORY_TITLE,
            StoryDatabaseHelper.COLUMN_REMOTE_STORY_CONTENT,
            StoryDatabaseHelper.COLUMN_REMOTE_STORY_IMAGE_URI
    };

    private final StoryDatabaseHelper databaseHelper;

    public SQLiteStoryDao(StoryDatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    @Override
    public List<PhotoStory> getStories() {
        ArrayList<PhotoStory> stories = new ArrayList<>();
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        try (Cursor cursor = db.query(
                StoryDatabaseHelper.TABLE_STORIES,
                STORY_COLUMNS,
                null,
                null,
                null,
                null,
                StoryDatabaseHelper.COLUMN_ID + " ASC"
        )) {
            while (cursor.moveToNext()) {
                stories.add(readStory(cursor));
            }
        }
        return stories;
    }

    @Override
    public PhotoStory addStory(String title, String date, String place, String people, String memoryText, List<String> imageUris) {
        return addStory(title, date, place, people, memoryText, new ArrayList<>(), imageUris, "", "", "");
    }

    @Override
    public PhotoStory addStory(String title, String date, String place, String people, String memoryText, List<String> tags, List<String> imageUris) {
        return addStory(title, date, place, people, memoryText, tags, imageUris, "", "", "");
    }

    @Override
    public PhotoStory addStory(String title, String date, String place, String people, String memoryText, List<String> imageUris, String remoteStoryTitle, String remoteStoryContent, String remoteStoryImageUri) {
        return addStory(title, date, place, people, memoryText, new ArrayList<>(), imageUris, remoteStoryTitle, remoteStoryContent, remoteStoryImageUri);
    }

    @Override
    public PhotoStory addStory(String title, String date, String place, String people, String memoryText, List<String> tags, List<String> imageUris, String remoteStoryTitle, String remoteStoryContent, String remoteStoryImageUri) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        long id = db.insert(
                StoryDatabaseHelper.TABLE_STORIES,
                null,
                buildValues(title, date, place, people, memoryText, tags, imageUris, false, remoteStoryTitle, remoteStoryContent, remoteStoryImageUri)
        );

        if (id == -1) {
            return null;
        }
        return findById((int) id);
    }

    @Override
    public PhotoStory updateStory(int storyId, String title, String date, String place, String people, String memoryText, List<String> imageUris) {
        return updateStory(storyId, title, date, place, people, memoryText, new ArrayList<>(), imageUris, "", "", "");
    }

    @Override
    public PhotoStory updateStory(int storyId, String title, String date, String place, String people, String memoryText, List<String> tags, List<String> imageUris) {
        return updateStory(storyId, title, date, place, people, memoryText, tags, imageUris, "", "", "");
    }

    @Override
    public PhotoStory updateStory(int storyId, String title, String date, String place, String people, String memoryText, List<String> imageUris, String remoteStoryTitle, String remoteStoryContent, String remoteStoryImageUri) {
        return updateStory(storyId, title, date, place, people, memoryText, new ArrayList<>(), imageUris, remoteStoryTitle, remoteStoryContent, remoteStoryImageUri);
    }

    @Override
    public PhotoStory updateStory(int storyId, String title, String date, String place, String people, String memoryText, List<String> tags, List<String> imageUris, String remoteStoryTitle, String remoteStoryContent, String remoteStoryImageUri) {
        PhotoStory existingStory = findById(storyId);
        if (existingStory == null) {
            return null;
        }

        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        int updatedRows = db.update(
                StoryDatabaseHelper.TABLE_STORIES,
                buildValues(title, date, place, people, memoryText, tags, imageUris, existingStory.isFavorite(), remoteStoryTitle, remoteStoryContent, remoteStoryImageUri),
                StoryDatabaseHelper.COLUMN_ID + " = ?",
                new String[]{String.valueOf(storyId)}
        );

        if (updatedRows == 0) {
            return null;
        }
        return findById(storyId);
    }

    @Override
    public PhotoStory findById(int storyId) {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        try (Cursor cursor = db.query(
                StoryDatabaseHelper.TABLE_STORIES,
                STORY_COLUMNS,
                StoryDatabaseHelper.COLUMN_ID + " = ?",
                new String[]{String.valueOf(storyId)},
                null,
                null,
                null
        )) {
            if (cursor.moveToFirst()) {
                return readStory(cursor);
            }
        }
        return null;
    }

    @Override
    public void deleteStory(int storyId) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.delete(
                StoryDatabaseHelper.TABLE_STORIES,
                StoryDatabaseHelper.COLUMN_ID + " = ?",
                new String[]{String.valueOf(storyId)}
        );
    }

    @Override
    public PhotoStory setFavorite(int storyId, boolean favorite) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(StoryDatabaseHelper.COLUMN_FAVORITE, favorite ? 1 : 0);
        int updatedRows = db.update(
                StoryDatabaseHelper.TABLE_STORIES,
                values,
                StoryDatabaseHelper.COLUMN_ID + " = ?",
                new String[]{String.valueOf(storyId)}
        );

        if (updatedRows == 0) {
            return null;
        }
        return findById(storyId);
    }

    @Override
    public ArrayList<PhotoStory> getStoriesByIds(int[] storyIds) {
        ArrayList<PhotoStory> result = new ArrayList<>();
        if (storyIds == null) {
            return result;
        }

        for (int storyId : storyIds) {
            PhotoStory story = findById(storyId);
            if (story != null) {
                result.add(story);
            }
        }
        return result;
    }

    private ContentValues buildValues(String title, String date, String place, String people, String memoryText, List<String> tags, List<String> imageUris, boolean favorite, String remoteStoryTitle, String remoteStoryContent, String remoteStoryImageUri) {
        ContentValues values = new ContentValues();
        values.put(StoryDatabaseHelper.COLUMN_TITLE, safe(title));
        values.put(StoryDatabaseHelper.COLUMN_DATE, safe(date));
        values.put(StoryDatabaseHelper.COLUMN_PLACE, safe(place));
        values.put(StoryDatabaseHelper.COLUMN_PEOPLE, safe(people));
        values.put(StoryDatabaseHelper.COLUMN_MEMORY_TEXT, safe(memoryText));
        values.put(StoryDatabaseHelper.COLUMN_TAGS, encodeValues(tags));
        values.put(StoryDatabaseHelper.COLUMN_IMAGE_URIS, encodeImageUris(imageUris));
        values.put(StoryDatabaseHelper.COLUMN_FAVORITE, favorite ? 1 : 0);
        values.put(StoryDatabaseHelper.COLUMN_REMOTE_STORY_TITLE, safe(remoteStoryTitle));
        values.put(StoryDatabaseHelper.COLUMN_REMOTE_STORY_CONTENT, safe(remoteStoryContent));
        values.put(StoryDatabaseHelper.COLUMN_REMOTE_STORY_IMAGE_URI, safe(remoteStoryImageUri));
        return values;
    }

    private PhotoStory readStory(Cursor cursor) {
        return new PhotoStory(
                cursor.getInt(cursor.getColumnIndexOrThrow(StoryDatabaseHelper.COLUMN_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(StoryDatabaseHelper.COLUMN_TITLE)),
                cursor.getString(cursor.getColumnIndexOrThrow(StoryDatabaseHelper.COLUMN_DATE)),
                cursor.getString(cursor.getColumnIndexOrThrow(StoryDatabaseHelper.COLUMN_PLACE)),
                cursor.getString(cursor.getColumnIndexOrThrow(StoryDatabaseHelper.COLUMN_PEOPLE)),
                cursor.getString(cursor.getColumnIndexOrThrow(StoryDatabaseHelper.COLUMN_MEMORY_TEXT)),
                decodeValues(cursor.getString(cursor.getColumnIndexOrThrow(StoryDatabaseHelper.COLUMN_TAGS))),
                decodeImageUris(cursor.getString(cursor.getColumnIndexOrThrow(StoryDatabaseHelper.COLUMN_IMAGE_URIS))),
                cursor.getInt(cursor.getColumnIndexOrThrow(StoryDatabaseHelper.COLUMN_FAVORITE)) == 1,
                cursor.getString(cursor.getColumnIndexOrThrow(StoryDatabaseHelper.COLUMN_REMOTE_STORY_TITLE)),
                cursor.getString(cursor.getColumnIndexOrThrow(StoryDatabaseHelper.COLUMN_REMOTE_STORY_CONTENT)),
                cursor.getString(cursor.getColumnIndexOrThrow(StoryDatabaseHelper.COLUMN_REMOTE_STORY_IMAGE_URI))
        );
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String encodeImageUris(List<String> imageUris) {
        return encodeValues(imageUris);
    }

    private String encodeValues(List<String> values) {
        JSONArray jsonArray = new JSONArray();
        if (values == null) {
            return jsonArray.toString();
        }

        for (String value : values) {
            if (value != null && !value.isEmpty()) {
                jsonArray.put(value);
            }
        }
        return jsonArray.toString();
    }

    private ArrayList<String> decodeImageUris(String value) {
        return decodeValues(value);
    }

    private ArrayList<String> decodeValues(String value) {
        ArrayList<String> imageUris = new ArrayList<>();
        if (value == null || value.isEmpty()) {
            return imageUris;
        }

        try {
            JSONArray jsonArray = new JSONArray(value);
            for (int i = 0; i < jsonArray.length(); i++) {
                String imageUri = jsonArray.optString(i);
                if (!imageUri.isEmpty()) {
                    imageUris.add(imageUri);
                }
            }
        } catch (JSONException ignored) {
        }
        return imageUris;
    }
}
