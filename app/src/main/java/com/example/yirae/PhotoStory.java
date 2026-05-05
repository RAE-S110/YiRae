package com.example.yirae;

import java.util.ArrayList;
import java.util.List;

public class PhotoStory {
    private final int id;
    private final String title;
    private final String date;
    private final String place;
    private final String people;
    private final String memoryText;
    private final ArrayList<String> imageUris;
    private final boolean favorite;

    public PhotoStory(int id, String title, String date, String place, String people, String memoryText, List<String> imageUris, boolean favorite) {
        this.id = id;
        this.title = title;
        this.date = date;
        this.place = place;
        this.people = people;
        this.memoryText = memoryText;
        this.imageUris = new ArrayList<>(imageUris);
        this.favorite = favorite;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDate() {
        return date;
    }

    public String getPlace() {
        return place;
    }

    public String getPeople() {
        return people;
    }

    public String getMemoryText() {
        return memoryText;
    }

    public ArrayList<String> getImageUris() {
        return new ArrayList<>(imageUris);
    }

    public boolean isFavorite() {
        return favorite;
    }

    public String getCoverImageUri() {
        return imageUris.isEmpty() ? "" : imageUris.get(0);
    }

    public boolean matchesKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return true;
        }

        String normalizedKeyword = keyword.trim().toLowerCase();
        return contains(title, normalizedKeyword)
                || contains(date, normalizedKeyword)
                || contains(place, normalizedKeyword)
                || contains(people, normalizedKeyword)
                || contains(memoryText, normalizedKeyword);
    }

    public String buildSubtitle() {
        StringBuilder builder = new StringBuilder();
        appendPart(builder, DateTimeUtils.formatDisplay(date));
        appendPart(builder, place);
        appendPart(builder, people);

        if (memoryText != null && !memoryText.isEmpty()) {
            if (builder.length() > 0) {
                builder.append(" | ");
            }

            if (memoryText.length() > 24) {
                builder.append(memoryText, 0, 24).append("...");
            } else {
                builder.append(memoryText);
            }
        }

        return builder.toString();
    }

    private boolean contains(String source, String keyword) {
        return source != null && source.toLowerCase().contains(keyword);
    }

    private void appendPart(StringBuilder builder, String value) {
        if (value == null || value.isEmpty()) {
            return;
        }

        if (builder.length() > 0) {
            builder.append(" | ");
        }
        builder.append(value);
    }
}
