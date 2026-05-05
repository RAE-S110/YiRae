package com.example.yirae;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class AddStoryActivity extends SecureActivity {
    private EditText etTitle;
    private EditText etPlace;
    private EditText etPeople;
    private EditText etMemoryText;
    private Button btnSelectDate;
    private Button btnSelectTime;
    private Button btnClearTime;
    private Button btnSelectImage;
    private Button btnClearImages;
    private Button btnSave;
    private TextView tvSelectedDate;
    private TextView tvSelectedTime;
    private TextView tvSelectedImage;
    private final ArrayList<String> selectedImageUris = new ArrayList<>();
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
        etMemoryText = findViewById(R.id.etMemoryText);
        btnSelectDate = findViewById(R.id.btnSelectDate);
        btnSelectTime = findViewById(R.id.btnSelectTime);
        btnClearTime = findViewById(R.id.btnClearTime);
        btnSelectImage = findViewById(R.id.btnSelectImage);
        btnClearImages = findViewById(R.id.btnClearImages);
        tvSelectedDate = findViewById(R.id.tvSelectedDate);
        tvSelectedTime = findViewById(R.id.tvSelectedTime);
        tvSelectedImage = findViewById(R.id.tvSelectedImage);
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
            intent.putExtra("memoryText", etMemoryText.getText().toString().trim());
            intent.putStringArrayListExtra("imageUris", new ArrayList<>(selectedImageUris));

            setResult(RESULT_OK, intent);
            finish();
        });
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

        updateDateTimeViews();
        updateSelectedImageText();
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
            builder.append(Uri.parse(imageUris.get(i)).getLastPathSegment());
        }
        if (imageUris.size() > previewCount) {
            builder.append("...");
        }
        return builder.toString();
    }
}
