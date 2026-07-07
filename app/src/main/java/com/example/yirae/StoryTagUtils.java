package com.example.yirae;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class StoryTagUtils {
    private StoryTagUtils() {
    }

    public static ArrayList<String> parseTags(String rawValue) {
        LinkedHashSet<String> orderedTags = new LinkedHashSet<>();
        if (rawValue == null || rawValue.trim().isEmpty()) {
            return new ArrayList<>();
        }

        String normalized = rawValue
                .replace('\n', ',')
                .replace('，', ',')
                .replace('；', ',')
                .replace(';', ',');
        String[] parts = normalized.split(",");
        for (String part : parts) {
            String cleaned = normalizeTag(part);
            if (!cleaned.isEmpty()) {
                orderedTags.add(cleaned);
            }
        }
        return new ArrayList<>(orderedTags);
    }

    public static String joinForInput(List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return "";
        }

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < tags.size(); i++) {
            if (i > 0) {
                builder.append(", ");
            }
            builder.append(tags.get(i));
        }
        return builder.toString();
    }

    public static String buildDisplayText(List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return "";
        }

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < tags.size(); i++) {
            if (i > 0) {
                builder.append("  ");
            }
            builder.append('#').append(tags.get(i));
        }
        return builder.toString();
    }

    public static boolean containsTag(List<String> tags, String token) {
        if (tags == null || tags.isEmpty() || token == null || token.isEmpty()) {
            return false;
        }

        String normalizedToken = token.toLowerCase(Locale.getDefault());
        for (String tag : tags) {
            if (tag != null && tag.toLowerCase(Locale.getDefault()).contains(normalizedToken)) {
                return true;
            }
        }
        return false;
    }

    public static List<String> splitKeywordTokens(String keyword) {
        ArrayList<String> tokens = new ArrayList<>();
        if (keyword == null || keyword.trim().isEmpty()) {
            return tokens;
        }

        String[] parts = keyword.trim().split("\\s+");
        for (String part : parts) {
            String token = part.trim().toLowerCase(Locale.getDefault());
            if (!token.isEmpty()) {
                tokens.add(token);
            }
        }
        return tokens;
    }

    private static String normalizeTag(String rawTag) {
        if (rawTag == null) {
            return "";
        }

        String cleaned = rawTag.trim();
        while (cleaned.startsWith("#")) {
            cleaned = cleaned.substring(1).trim();
        }
        return cleaned;
    }
}
