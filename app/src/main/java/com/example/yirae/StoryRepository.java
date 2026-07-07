package com.example.yirae;

import android.content.Context;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class StoryRepository {
    private static StoryDao storyDao;

    private StoryRepository() {
    }

    public static void initialize(Context context) {
        if (storyDao != null) {
            return;
        }

        StoryDatabaseHelper databaseHelper = new StoryDatabaseHelper(context.getApplicationContext());
        storyDao = new SQLiteStoryDao(databaseHelper);
    }

    public static List<PhotoStory> getStories() {
        return requireDao().getStories();
    }

    public static PhotoStory addStory(Context context, String title, String date, String place, String people, String memoryText, List<String> imageUris) {
        return addStory(context, title, date, place, people, memoryText, Collections.emptyList(), imageUris, "", "", "");
    }

    public static PhotoStory addStory(Context context, String title, String date, String place, String people, String memoryText, List<String> imageUris, String remoteStoryTitle, String remoteStoryContent, String remoteStoryImageUri) {
        return addStory(context, title, date, place, people, memoryText, Collections.emptyList(), imageUris, remoteStoryTitle, remoteStoryContent, remoteStoryImageUri);
    }

    public static PhotoStory addStory(Context context, String title, String date, String place, String people, String memoryText, List<String> tags, List<String> imageUris, String remoteStoryTitle, String remoteStoryContent, String remoteStoryImageUri) {
        initialize(context);
        return storyDao.addStory(title, date, place, people, memoryText, tags, imageUris, remoteStoryTitle, remoteStoryContent, remoteStoryImageUri);
    }

    public static PhotoStory updateStory(Context context, int storyId, String title, String date, String place, String people, String memoryText, List<String> imageUris) {
        return updateStory(context, storyId, title, date, place, people, memoryText, Collections.emptyList(), imageUris, "", "", "");
    }

    public static PhotoStory updateStory(Context context, int storyId, String title, String date, String place, String people, String memoryText, List<String> imageUris, String remoteStoryTitle, String remoteStoryContent, String remoteStoryImageUri) {
        return updateStory(context, storyId, title, date, place, people, memoryText, Collections.emptyList(), imageUris, remoteStoryTitle, remoteStoryContent, remoteStoryImageUri);
    }

    public static PhotoStory updateStory(Context context, int storyId, String title, String date, String place, String people, String memoryText, List<String> tags, List<String> imageUris, String remoteStoryTitle, String remoteStoryContent, String remoteStoryImageUri) {
        initialize(context);
        return storyDao.updateStory(storyId, title, date, place, people, memoryText, tags, imageUris, remoteStoryTitle, remoteStoryContent, remoteStoryImageUri);
    }

    public static PhotoStory findById(int storyId) {
        return requireDao().findById(storyId);
    }

    public static void deleteStory(Context context, int storyId) {
        initialize(context);
        storyDao.deleteStory(storyId);
    }

    public static PhotoStory setFavorite(Context context, int storyId, boolean favorite) {
        initialize(context);
        return storyDao.setFavorite(storyId, favorite);
    }

    public static ArrayList<PhotoStory> getStoriesByIds(int[] storyIds) {
        return requireDao().getStoriesByIds(storyIds);
    }

    private static StoryDao requireDao() {
        if (storyDao == null) {
            throw new IllegalStateException("StoryRepository.initialize(context) must be called before reading stories.");
        }
        return storyDao;
    }
}
