package com.example.yirae;

import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.core.content.FileProvider;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

public final class StoryExportHelper {
    private static final SimpleDateFormat FILE_TIME_FORMAT = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());

    private StoryExportHelper() {
    }

    public static boolean exportStoriesAsMarkdown(Context context, List<PhotoStory> stories) {
        File exportFile = writeFile(context, "yirae_stories_" + FILE_TIME_FORMAT.format(new Date()) + ".md", buildMarkdown(stories));
        if (exportFile == null) {
            return false;
        }
        shareExportFile(context, exportFile, "text/markdown", context.getString(R.string.export_markdown));
        return true;
    }

    public static boolean exportStoriesAsJson(Context context, List<PhotoStory> stories) {
        File exportFile = writeFile(context, "yirae_stories_" + FILE_TIME_FORMAT.format(new Date()) + ".json", buildJson(stories));
        if (exportFile == null) {
            return false;
        }
        shareExportFile(context, exportFile, "application/json", context.getString(R.string.export_json));
        return true;
    }

    private static File writeFile(Context context, String fileName, String content) {
        File exportDir = new File(context.getCacheDir(), "exports");
        if (!exportDir.exists() && !exportDir.mkdirs()) {
            return null;
        }

        File exportFile = new File(exportDir, fileName);
        try (FileOutputStream outputStream = new FileOutputStream(exportFile)) {
            outputStream.write(content.getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
            return exportFile;
        } catch (IOException e) {
            return null;
        }
    }

    private static void shareExportFile(Context context, File exportFile, String mimeType, String chooserTitle) {
        Uri uri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", exportFile);
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, exportFile.getName());
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        shareIntent.setType(mimeType);
        shareIntent.setClipData(ClipData.newUri(context.getContentResolver(), exportFile.getName(), uri));
        context.startActivity(Intent.createChooser(shareIntent, chooserTitle));
    }

    private static String buildMarkdown(List<PhotoStory> stories) {
        StringBuilder builder = new StringBuilder();
        builder.append("# YiRae Stories Export\n\n");
        builder.append("Total stories: ").append(stories == null ? 0 : stories.size()).append("\n\n");

        if (stories == null) {
            return builder.toString();
        }

        for (PhotoStory story : stories) {
            builder.append("## ").append(safeTitle(story)).append("\n\n");
            builder.append("- Date: ").append(DateTimeUtils.formatDisplay(story.getDate())).append("\n");
            builder.append("- Place: ").append(emptyFallback(story.getPlace())).append("\n");
            builder.append("- People: ").append(emptyFallback(story.getPeople())).append("\n");
            builder.append("- Favorite: ").append(story.isFavorite() ? "Yes" : "No").append("\n");
            builder.append("- Tags: ").append(story.hasTags() ? StoryTagUtils.buildDisplayText(story.getTags()) : "None").append("\n");
            builder.append("- Images: ").append(story.getImageUris().size()).append("\n\n");
            builder.append(story.getMemoryText().isEmpty() ? emptyFallback("") : story.getMemoryText()).append("\n\n");
            if (story.hasRemoteStory()) {
                builder.append("### Remote Story\n\n");
                builder.append("- Title: ").append(emptyFallback(story.getRemoteStoryTitle())).append("\n");
                builder.append("- Content: ").append(emptyFallback(story.getRemoteStoryContent())).append("\n");
                builder.append("- Image: ").append(emptyFallback(story.getRemoteStoryImageUri())).append("\n\n");
            }
        }
        return builder.toString();
    }

    private static String buildJson(List<PhotoStory> stories) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        LinkedHashMap<String, Object> payload = new LinkedHashMap<>();
        payload.put("app", "YiRae");
        payload.put("storyCount", stories == null ? 0 : stories.size());
        payload.put("stories", stories);
        return gson.toJson(payload);
    }

    private static String safeTitle(PhotoStory story) {
        return story.getTitle().isEmpty() ? "Untitled Memory" : story.getTitle();
    }

    private static String emptyFallback(String value) {
        return value == null || value.isEmpty() ? "-" : value;
    }
}
