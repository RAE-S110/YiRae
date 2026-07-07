package com.example.yirae;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.net.Uri;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class NetworkStoryRepository {
    public interface RemoteImageCallback {
        void onSuccess(String localUriString);

        void onError(String message);
    }

    public interface RemoteStoryCallback {
        void onSuccess(RemoteStory story);

        void onError(String message);
    }

    private static final String SAMPLE_STORY_URL = "https://picsum.photos/id/1062/info";
    private static final String FALLBACK_STORY_JSON = "{"
            + "\"id\":\"1062\","
            + "\"author\":\"YiRae Sample\","
            + "\"width\":5616,"
            + "\"height\":3744,"
            + "\"url\":\"https://unsplash.com/photos/K785Da4A_JA\","
            + "\"download_url\":\"https://picsum.photos/id/1062/5616/3744\""
            + "}";

    private final OkHttpClient client = new OkHttpClient();
    private final Gson gson = new Gson();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public void loadSampleStory(RemoteStoryCallback callback) {
        Request request = new Request.Builder()
                .url(SAMPLE_STORY_URL)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                postFallbackStory(callback);
            }

            @Override
            public void onResponse(Call call, Response response) {
                try (Response responseToClose = response) {
                    if (!responseToClose.isSuccessful() || responseToClose.body() == null) {
                        postFallbackStory(callback);
                        return;
                    }

                    String json = responseToClose.body().string();
                    RemoteStory story = gson.fromJson(json, RemoteStory.class);
                    mainHandler.post(() -> callback.onSuccess(story));
                } catch (Exception e) {
                    postFallbackStory(callback);
                }
            }
        });
    }

    public void cacheRemoteImage(Context context, String imageUrl, RemoteImageCallback callback) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            mainHandler.post(() -> callback.onError("empty image url"));
            return;
        }

        Request request = new Request.Builder()
                .url(imageUrl)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }

            @Override
            public void onResponse(Call call, Response response) {
                try (Response responseToClose = response) {
                    if (!responseToClose.isSuccessful() || responseToClose.body() == null) {
                        mainHandler.post(() -> callback.onError("image download failed"));
                        return;
                    }

                    File imageDir = new File(context.getCacheDir(), "remote_images");
                    if (!imageDir.exists() && !imageDir.mkdirs()) {
                        mainHandler.post(() -> callback.onError("cache dir unavailable"));
                        return;
                    }

                    File outputFile = new File(imageDir, "remote_" + System.currentTimeMillis() + ".jpg");
                    try (FileOutputStream outputStream = new FileOutputStream(outputFile)) {
                        outputStream.write(responseToClose.body().bytes());
                        outputStream.flush();
                    }

                    String localUri = Uri.fromFile(outputFile).toString();
                    mainHandler.post(() -> callback.onSuccess(localUri));
                } catch (Exception e) {
                    mainHandler.post(() -> callback.onError(e.getMessage()));
                }
            }
        });
    }

    private void postFallbackStory(RemoteStoryCallback callback) {
        try {
            RemoteStory story = gson.fromJson(FALLBACK_STORY_JSON, RemoteStory.class);
            mainHandler.post(() -> callback.onSuccess(story));
        } catch (Exception e) {
            mainHandler.post(() -> callback.onError(e.getMessage()));
        }
    }
}
