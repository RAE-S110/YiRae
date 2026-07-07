package com.example.yirae;

import com.google.gson.annotations.SerializedName;

public class RemoteStory {
    private String id;
    private String author;
    private int width;
    private int height;
    private String url;
    @SerializedName("download_url")
    private String downloadUrl;

    public String getId() {
        return safe(id);
    }

    public String getAuthor() {
        return safe(author);
    }

    public String getUrl() {
        return safe(url);
    }

    public String getDownloadUrl() {
        return safe(downloadUrl);
    }

    public String buildTitle() {
        return "\u7f51\u7edc\u56fe\u7247\u6545\u4e8b #" + getId();
    }

    public String buildMemoryText() {
        return "\u4f5c\u8005\uff1a" + getAuthor()
                + "\n\u7f51\u7edc\u793a\u4f8b\u56fe\u7247\u5c3a\u5bf8\uff1a" + width + " x " + height
                + "\n\u6765\u6e90\uff1a" + getUrl();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
