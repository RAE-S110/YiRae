package com.example.yirae;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class AddStoryActivity extends SecureActivity {
    private static final String RELIABLE_REMOTE_IMAGE_URL = "https://www.baidu.com/img/PCtm_d9c8750bed0b3c7d089fa7d55720d6cf.png";

    private EditText etTitle;
    private EditText etPlace;
    private EditText etPeople;
    private EditText etTags;
    private EditText etMemoryText;
    private Button btnSelectDate;
    private Button btnSelectTime;
    private Button btnClearTime;
    private Button btnSelectImage;
    private Button btnClearImages;
    private Button btnLoadRemoteStory;
    private Button btnSave;
    private TextView tvSelectedDate;
    private TextView tvSelectedTime;
    private TextView tvSelectedImage;
    private TextView tvRemoteStoryStatus;
    private TextView tvRemoteStoryTitle;
    private TextView tvRemoteStoryContent;
    private ImageView ivRemoteStoryImage;
    private final NetworkStoryRepository networkStoryRepository = new NetworkStoryRepository();
    private final ArrayList<String> selectedImageUris = new ArrayList<>();
    private String currentRemoteStoryTitle = "";
    private String currentRemoteStoryContent = "";
    private String currentRemoteStoryImageUri = "";
    private Calendar selectedDateTime;
    private boolean hasSelectedTime;

    private final ActivityResultLauncher<String[]> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.OpenMultipleDocuments(), uris -> {
                if (uris == null || uris.isEmpty()) {
                    return;
                }

                final int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION;
                for (Uri uri : uris) {
                    getContentResolver().takePersistableUriPermission(uri, takeFlags);
                    String uriString = uri.toString();
                    if (!selectedImageUris.contains(uriString)) {
                        selectedImageUris.add(uriString);
                    }
                }
                updateSelectedImageText();
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_story);

        etTitle = findViewById(R.id.etTitle);
        etPlace = findViewById(R.id.etPlace);
        etPeople = findViewById(R.id.etPeople);
        etTags = findViewById(R.id.etTags);
        etMemoryText = findViewById(R.id.etMemoryText);
        btnSelectDate = findViewById(R.id.btnSelectDate);
        btnSelectTime = findViewById(R.id.btnSelectTime);
        btnClearTime = findViewById(R.id.btnClearTime);
        btnSelectImage = findViewById(R.id.btnSelectImage);
        btnClearImages = findViewById(R.id.btnClearImages);
        btnLoadRemoteStory = findViewById(R.id.btnLoadRemoteStory);
        tvSelectedDate = findViewById(R.id.tvSelectedDate);
        tvSelectedTime = findViewById(R.id.tvSelectedTime);
        tvSelectedImage = findViewById(R.id.tvSelectedImage);
        tvRemoteStoryStatus = findViewById(R.id.tvRemoteStoryStatus);
        tvRemoteStoryTitle = findViewById(R.id.tvRemoteStoryTitle);
        tvRemoteStoryContent = findViewById(R.id.tvRemoteStoryContent);
        ivRemoteStoryImage = findViewById(R.id.ivRemoteStoryImage);
        btnSave = findViewById(R.id.btnSave);

        fillExistingStoryIfNeeded();

        btnSelectDate.setOnClickListener(v -> showDatePicker());
        btnSelectTime.setOnClickListener(v -> showTimePicker());
        btnClearTime.setOnClickListener(v -> {
            hasSelectedTime = false;
            updateDateTimeViews();
        });
        btnSelectImage.setOnClickListener(v -> imagePickerLauncher.launch(new String[]{"image/*"}));
        btnClearImages.setOnClickListener(v -> {
            selectedImageUris.clear();
            updateSelectedImageText();
        });
        btnLoadRemoteStory.setOnClickListener(v -> loadRemoteStory());

        btnSave.setOnClickListener(v -> {
            if (selectedDateTime == null) {
                selectedDateTime = Calendar.getInstance();
            }

            Intent intent = new Intent();
            intent.putExtra("storyId", getIntent().getIntExtra("storyId", -1));
            intent.putExtra("title", etTitle.getText().toString().trim());
            intent.putExtra("date", DateTimeUtils.buildStoredDate(selectedDateTime, hasSelectedTime));
            intent.putExtra("place", etPlace.getText().toString().trim());
            intent.putExtra("people", etPeople.getText().toString().trim());
            intent.putStringArrayListExtra("tags", StoryTagUtils.parseTags(etTags.getText().toString()));
            intent.putExtra("memoryText", etMemoryText.getText().toString().trim());
            intent.putExtra("remoteStoryTitle", currentRemoteStoryTitle);
            intent.putExtra("remoteStoryContent", currentRemoteStoryContent);
            intent.putExtra("remoteStoryImageUri", currentRemoteStoryImageUri);
            intent.putStringArrayListExtra("imageUris", new ArrayList<>(selectedImageUris));

            setResult(RESULT_OK, intent);
            finish();
        });
    }

    private void loadRemoteStory() {
        btnLoadRemoteStory.setEnabled(false);
        tvRemoteStoryStatus.setText(R.string.remote_story_loading);

        networkStoryRepository.loadSampleStory(new NetworkStoryRepository.RemoteStoryCallback() {
            @Override
            public void onSuccess(RemoteStory story) {
                btnLoadRemoteStory.setEnabled(true);
                applyRemoteStory(story);
            }

            @Override
            public void onError(String message) {
                btnLoadRemoteStory.setEnabled(true);
                String safeMessage = message == null || message.isEmpty() ? "unknown" : message;
                tvRemoteStoryStatus.setText(getString(R.string.remote_story_failed, safeMessage));
                Toast.makeText(AddStoryActivity.this, tvRemoteStoryStatus.getText(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void applyRemoteStory(RemoteStory story) {
        if (story == null) {
            tvRemoteStoryStatus.setText(getString(R.string.remote_story_failed, "empty"));
            return;
        }

        String title = story.buildTitle();
        String memoryText = story.buildMemoryText();
        currentRemoteStoryTitle = title;
        currentRemoteStoryContent = memoryText;
        tvRemoteStoryStatus.setText(R.string.remote_story_loaded);
        tvRemoteStoryTitle.setText(getString(R.string.remote_story_title, title));
        tvRemoteStoryContent.setText(getString(R.string.remote_story_content, memoryText));
        tvRemoteStoryTitle.setVisibility(View.VISIBLE);
        tvRemoteStoryContent.setVisibility(View.VISIBLE);

        String imageUrl = story.getDownloadUrl();
        if (!imageUrl.isEmpty()) {
            loadRemoteImage(buildReliableRemoteImageUrl(imageUrl));
        }
    }

    private void loadRemoteImage(String imageUrl) {
        Glide.with(this)
                .load(imageUrl)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_gallery)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        tvRemoteStoryStatus.setText(R.string.remote_story_loaded);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        addRemoteImageIfNeeded(imageUrl);
                        return false;
                    }
                })
                .into(ivRemoteStoryImage);
    }

    private void addRemoteImageIfNeeded(String imageUrl) {
        currentRemoteStoryImageUri = imageUrl;
        if (!selectedImageUris.contains(imageUrl)) {
            selectedImageUris.add(imageUrl);
            updateSelectedImageText();
        }
    }

    private String buildReliableRemoteImageUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return RELIABLE_REMOTE_IMAGE_URL;
        }
        if (imageUrl.contains("picsum.photos")) {
            return RELIABLE_REMOTE_IMAGE_URL;
        }
        return imageUrl;
    }

    private void fillExistingStoryIfNeeded() {
        selectedDateTime = Calendar.getInstance();
        hasSelectedTime = false;

        if (!getIntent().getBooleanExtra("isEditMode", false)) {
            updateDateTimeViews();
            updateSelectedImageText();
            return;
        }

        etTitle.setText(getIntent().getStringExtra("title"));
        etPlace.setText(getIntent().getStringExtra("place"));
        etPeople.setText(getIntent().getStringExtra("people"));
        etTags.setText(StoryTagUtils.joinForInput(getIntent().getStringArrayListExtra("tags")));
        etMemoryText.setText(getIntent().getStringExtra("memoryText"));
        btnSave.setText(R.string.save_changes);

        String storedDate = getIntent().getStringExtra("date");
        Calendar parsedDate = DateTimeUtils.parseStoredDate(storedDate);
        if (parsedDate != null) {
            selectedDateTime = parsedDate;
            hasSelectedTime = DateTimeUtils.hasTime(storedDate);
        }

        ArrayList<String> imageUris = getIntent().getStringArrayListExtra("imageUris");
        if (imageUris != null) {
            selectedImageUris.clear();
            selectedImageUris.addAll(imageUris);
        }

        currentRemoteStoryTitle = safe(getIntent().getStringExtra("remoteStoryTitle"));
        currentRemoteStoryContent = safe(getIntent().getStringExtra("remoteStoryContent"));
        currentRemoteStoryImageUri = safe(getIntent().getStringExtra("remoteStoryImageUri"));
        restoreRemoteStoryViews();

        updateDateTimeViews();
        updateSelectedImageText();
    }

    private void restoreRemoteStoryViews() {
        if (currentRemoteStoryTitle.isEmpty() && currentRemoteStoryContent.isEmpty() && currentRemoteStoryImageUri.isEmpty()) {
            return;
        }

        tvRemoteStoryStatus.setText(R.string.remote_story_loaded);
        tvRemoteStoryTitle.setText(getString(R.string.remote_story_title, currentRemoteStoryTitle));
        tvRemoteStoryContent.setText(getString(R.string.remote_story_content, currentRemoteStoryContent));
        tvRemoteStoryTitle.setVisibility(View.VISIBLE);
        tvRemoteStoryContent.setVisibility(View.VISIBLE);

        if (!currentRemoteStoryImageUri.isEmpty()) {
            Glide.with(this)
                    .load(currentRemoteStoryImageUri)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_gallery)
                    .into(ivRemoteStoryImage);
        }
    }

    private void showDatePicker() {
        Calendar calendar = selectedDateTime == null ? Calendar.getInstance() : selectedDateTime;
        new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    if (selectedDateTime == null) {
                        selectedDateTime = Calendar.getInstance();
                    }
                    selectedDateTime.set(Calendar.YEAR, year);
                    selectedDateTime.set(Calendar.MONTH, month);
                    selectedDateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    updateDateTimeViews();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    private void showTimePicker() {
        if (selectedDateTime == null) {
            selectedDateTime = Calendar.getInstance();
        }
        new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    selectedDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    selectedDateTime.set(Calendar.MINUTE, minute);
                    hasSelectedTime = true;
                    updateDateTimeViews();
                },
                selectedDateTime.get(Calendar.HOUR_OF_DAY),
                selectedDateTime.get(Calendar.MINUTE),
                true
        ).show();
    }

    private void updateDateTimeViews() {
        if (selectedDateTime == null) {
            tvSelectedDate.setText(R.string.no_date_selected);
            tvSelectedTime.setText(R.string.no_time_selected);
            return;
        }

        tvSelectedDate.setText(getString(R.string.selected_date_value, DateTimeUtils.formatDisplayDate(selectedDateTime)));
        if (hasSelectedTime) {
            tvSelectedTime.setText(getString(R.string.selected_time_value, DateTimeUtils.formatDisplayTime(selectedDateTime)));
        } else {
            tvSelectedTime.setText(R.string.no_time_selected);
        }
    }

    private void updateSelectedImageText() {
        if (selectedImageUris.isEmpty()) {
            tvSelectedImage.setText(R.string.no_image_selected);
            return;
        }

        tvSelectedImage.setText(getString(
                R.string.selected_images,
                selectedImageUris.size(),
                buildPreviewNames(selectedImageUris)
        ));
    }

    private String buildPreviewNames(List<String> imageUris) {
        StringBuilder builder = new StringBuilder();
        int previewCount = Math.min(imageUris.size(), 3);
        for (int i = 0; i < previewCount; i++) {
            if (i > 0) {
                builder.append(", ");
            }
            builder.append(buildPreviewName(imageUris.get(i)));
        }
        if (imageUris.size() > previewCount) {
            builder.append("...");
        }
        return builder.toString();
    }

    private String buildPreviewName(String imageUri) {
        if (imageUri == null || imageUri.isEmpty()) {
            return "";
        }

        Uri uri = Uri.parse(imageUri);
        String lastPathSegment = uri.getLastPathSegment();
        if (lastPathSegment != null && !lastPathSegment.isEmpty()) {
            return lastPathSegment;
        }
        return imageUri.startsWith("http") ? "network image" : imageUri;
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
