package com.example.yirae;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public final class StoryRepository {
    private static final String PREFS_NAME = "story_repository";
    private static final String KEY_NEXT_STORY_ID = "next_story_id";
    private static final String KEY_STORIES = "stories";

    private static final ArrayList<PhotoStory> STORIES = new ArrayList<>();
    private static int nextStoryId = 1;
    private static boolean initialized = false;

    private StoryRepository() {
    }

    public static void initialize(Context context) {
        if (initialized) {
            return;
        }

        loadFromStorage(context.getApplicationContext());
        initialized = true;
    }

    public static List<PhotoStory> getStories() {
        return new ArrayList<>(STORIES);
    }

    public static PhotoStory addStory(Context context, String title, String date, String place, String people, String memoryText, List<String> imageUris) {
        PhotoStory story = new PhotoStory(
                nextStoryId++,
                safe(title),
                safe(date),
                safe(place),
                safe(people),
                safe(memoryText),
                safeList(imageUris),
                false
        );
        STORIES.add(story);
        saveToStorage(context.getApplicationContext());
        return story;
    }

    public static PhotoStory updateStory(Context context, int storyId, String title, String date, String place, String people, String memoryText, List<String> imageUris) {
        PhotoStory existingStory = findById(storyId);
        boolean favorite = existingStory != null && existingStory.isFavorite();
        PhotoStory updatedStory = new PhotoStory(
                storyId,
                safe(title),
                safe(date),
                safe(place),
                safe(people),
                safe(memoryText),
                safeList(imageUris),
                favorite
        );

        for (int i = 0; i < STORIES.size(); i++) {
            if (STORIES.get(i).getId() == storyId) {
                STORIES.set(i, updatedStory);
                saveToStorage(context.getApplicationContext());
                return updatedStory;
            }
        }

        return null;
    }

    public static PhotoStory findById(int storyId) {
        for (PhotoStory story : STORIES) {
            if (story.getId() == storyId) {
                return story;
            }
        }
        return null;
    }

    public static void deleteStory(Context context, int storyId) {
        for (int i = 0; i < STORIES.size(); i++) {
            if (STORIES.get(i).getId() == storyId) {
                STORIES.remove(i);
                saveToStorage(context.getApplicationContext());
                return;
            }
        }
    }

    public static PhotoStory setFavorite(Context context, int storyId, boolean favorite) {
        for (int i = 0; i < STORIES.size(); i++) {
            PhotoStory story = STORIES.get(i);
            if (story.getId() == storyId) {
                PhotoStory updatedStory = new PhotoStory(
                        story.getId(),
                        story.getTitle(),
                        story.getDate(),
                        story.getPlace(),
                        story.getPeople(),
                        story.getMemoryText(),
                        story.getImageUris(),
                        favorite
                );
                STORIES.set(i, updatedStory);
                saveToStorage(context.getApplicationContext());
                return updatedStory;
            }
        }
        return null;
    }

    public static ArrayList<PhotoStory> getStoriesByIds(int[] storyIds) {
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

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    private static ArrayList<String> safeList(List<String> values) {
        ArrayList<String> safeValues = new ArrayList<>();
        if (values == null) {
            return safeValues;
        }

        for (String value : values) {
            if (value != null && !value.isEmpty()) {
                safeValues.add(value);
            }
        }
        return safeValues;
    }

    private static void loadFromStorage(Context context) {
        STORIES.clear();

        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        nextStoryId = preferences.getInt(KEY_NEXT_STORY_ID, 1);
        String storiesJson = preferences.getString(KEY_STORIES, "[]");

        try {
            JSONArray storiesArray = new JSONArray(storiesJson);
            for (int i = 0; i < storiesArray.length(); i++) {
                JSONObject storyObject = storiesArray.getJSONObject(i);
                STORIES.add(new PhotoStory(
                        storyObject.optInt("id"),
                        storyObject.optString("title"),
                        storyObject.optString("date"),
                        storyObject.optString("place"),
                        storyObject.optString("people"),
                        storyObject.optString("memoryText"),
                        jsonArrayToList(storyObject.optJSONArray("imageUris")),
                        storyObject.optBoolean("favorite", false)
                ));
            }
        } catch (JSONException ignored) {
            STORIES.clear();
            nextStoryId = 1;
        }
    }

    private static void saveToStorage(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        JSONArray storiesArray = new JSONArray();

        for (PhotoStory story : STORIES) {
            JSONObject storyObject = new JSONObject();
            try {
                storyObject.put("id", story.getId());
                storyObject.put("title", story.getTitle());
                storyObject.put("date", story.getDate());
                storyObject.put("place", story.getPlace());
                storyObject.put("people", story.getPeople());
                storyObject.put("memoryText", story.getMemoryText());
                storyObject.put("imageUris", new JSONArray(story.getImageUris()));
                storyObject.put("favorite", story.isFavorite());
                storiesArray.put(storyObject);
            } catch (JSONException ignored) {
            }
        }

        preferences.edit()
                .putInt(KEY_NEXT_STORY_ID, nextStoryId)
                .putString(KEY_STORIES, storiesArray.toString())
                .apply();
    }

    private static ArrayList<String> jsonArrayToList(JSONArray jsonArray) {
        ArrayList<String> values = new ArrayList<>();
        if (jsonArray == null) {
            return values;
        }

        for (int i = 0; i < jsonArray.length(); i++) {
            String value = jsonArray.optString(i);
            if (!value.isEmpty()) {
                values.add(value);
            }
        }
        return values;
    }
}
