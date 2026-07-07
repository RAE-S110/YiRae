package com.example.yirae;

import java.util.ArrayList;
import java.util.List;

public interface StoryDao {
    List<PhotoStory> getStories();

    PhotoStory addStory(String title, String date, String place, String people, String memoryText, List<String> imageUris);

    PhotoStory addStory(String title, String date, String place, String people, String memoryText, List<String> tags, List<String> imageUris);

    PhotoStory addStory(String title, String date, String place, String people, String memoryText, List<String> imageUris, String remoteStoryTitle, String remoteStoryContent, String remoteStoryImageUri);

    PhotoStory addStory(String title, String date, String place, String people, String memoryText, List<String> tags, List<String> imageUris, String remoteStoryTitle, String remoteStoryContent, String remoteStoryImageUri);

    PhotoStory updateStory(int storyId, String title, String date, String place, String people, String memoryText, List<String> imageUris);

    PhotoStory updateStory(int storyId, String title, String date, String place, String people, String memoryText, List<String> tags, List<String> imageUris);

    PhotoStory updateStory(int storyId, String title, String date, String place, String people, String memoryText, List<String> imageUris, String remoteStoryTitle, String remoteStoryContent, String remoteStoryImageUri);

    PhotoStory updateStory(int storyId, String title, String date, String place, String people, String memoryText, List<String> tags, List<String> imageUris, String remoteStoryTitle, String remoteStoryContent, String remoteStoryImageUri);

    PhotoStory findById(int storyId);

    void deleteStory(int storyId);

    PhotoStory setFavorite(int storyId, boolean favorite);

    ArrayList<PhotoStory> getStoriesByIds(int[] storyIds);
}
