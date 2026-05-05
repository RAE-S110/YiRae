package com.example.yirae;

import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import java.util.ArrayList;

public class DetailActivity extends SecureActivity {
    private ImageView ivStoryImage;
    private LinearLayout detailContent;
    private TextView tvTitle;
    private TextView tvDate;
    private TextView tvPlace;
    private TextView tvPeople;
    private TextView tvMemoryText;
    private TextView tvSwipeHint;
    private TextView tvImageIndex;
    private Button btnToggleFavorite;
    private Button btnShareStory;
    private Button btnEditStory;
    private ImageButton btnPrevImage;
    private ImageButton btnNextImage;
    private Button btnPrevStory;
    private Button btnNextStory;
    private ArrayList<PhotoStory> stories;
    private int currentIndex;
    private int currentImageIndex;
    private GestureDetector storyGestureDetector;

    private final ActivityResultLauncher<Intent> editStoryLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() != RESULT_OK || result.getData() == null) {
                    return;
                }

                Intent data = result.getData();
                int storyId = data.getIntExtra("storyId", -1);
                if (storyId == -1) {
                    return;
                }

                StoryRepository.updateStory(
                        DetailActivity.this,
                        storyId,
                        data.getStringExtra("title"),
                        data.getStringExtra("date"),
                        data.getStringExtra("place"),
                        data.getStringExtra("people"),
                        data.getStringExtra("memoryText"),
                        data.getStringArrayListExtra("imageUris")
                );

                stories = StoryRepository.getStoriesByIds(getIntent().getIntArrayExtra("storyIds"));
                Toast.makeText(DetailActivity.this, R.string.story_saved_success, Toast.LENGTH_SHORT).show();
                showStoryAt(currentIndex);
                setResult(RESULT_OK);
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        ivStoryImage = findViewById(R.id.ivStoryImage);
        detailContent = findViewById(R.id.detailContent);
        tvTitle = findViewById(R.id.tvTitle);
        tvDate = findViewById(R.id.tvDate);
        tvPlace = findViewById(R.id.tvPlace);
        tvPeople = findViewById(R.id.tvPeople);
        tvMemoryText = findViewById(R.id.tvMemoryText);
        tvSwipeHint = findViewById(R.id.tvSwipeHint);
        tvImageIndex = findViewById(R.id.tvImageIndex);
        btnToggleFavorite = findViewById(R.id.btnToggleFavorite);
        btnShareStory = findViewById(R.id.btnShareStory);
        btnEditStory = findViewById(R.id.btnEditStory);
        btnPrevImage = findViewById(R.id.btnPrevImage);
        btnNextImage = findViewById(R.id.btnNextImage);
        btnPrevStory = findViewById(R.id.btnPrevStory);
        btnNextStory = findViewById(R.id.btnNextStory);

        stories = StoryRepository.getStoriesByIds(getIntent().getIntArrayExtra("storyIds"));
        currentIndex = getIntent().getIntExtra("currentIndex", 0);
        if (stories.isEmpty()) {
            finish();
            return;
        }

        storyGestureDetector = buildStoryGestureDetector();
        detailContent.setOnTouchListener((v, event) -> storyGestureDetector.onTouchEvent(event));
        ivStoryImage.setOnTouchListener((v, event) -> false);

        btnPrevImage.setOnClickListener(v -> showImageAt(currentImageIndex - 1));
        btnNextImage.setOnClickListener(v -> showImageAt(currentImageIndex + 1));
        btnPrevStory.setOnClickListener(v -> showStoryAt(currentIndex - 1));
        btnNextStory.setOnClickListener(v -> showStoryAt(currentIndex + 1));

        showStoryAt(currentIndex);
    }

    private GestureDetector buildStoryGestureDetector() {
        return new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            private static final int SWIPE_THRESHOLD = 120;
            private static final int SWIPE_VELOCITY_THRESHOLD = 120;

            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (e1 == null || e2 == null) {
                    return false;
                }

                float diffX = e2.getX() - e1.getX();
                float diffY = e2.getY() - e1.getY();
                if (Math.abs(diffX) <= Math.abs(diffY)
                        || Math.abs(diffX) < SWIPE_THRESHOLD
                        || Math.abs(velocityX) < SWIPE_VELOCITY_THRESHOLD) {
                    return false;
                }

                if (diffX < 0) {
                    showStoryAt(currentIndex + 1);
                } else {
                    showStoryAt(currentIndex - 1);
                }
                return true;
            }
        });
    }

    private void showStoryAt(int index) {
        if (index < 0 || index >= stories.size()) {
            return;
        }

        currentIndex = index;
        currentImageIndex = 0;
        PhotoStory story = stories.get(index);

        tvTitle.setText(story.getTitle().isEmpty() ? getString(R.string.no_title) : story.getTitle());
        tvDate.setText(getString(R.string.detail_date, DateTimeUtils.formatDisplay(story.getDate())));
        tvPlace.setText(getString(R.string.detail_place, story.getPlace()));
        tvPeople.setText(getString(R.string.detail_people, story.getPeople()));
        tvMemoryText.setText(getString(R.string.detail_memory, story.getMemoryText()));
        tvSwipeHint.setText(getString(R.string.swipe_hint, currentIndex + 1, stories.size()));
        btnToggleFavorite.setText(story.isFavorite() ? R.string.unfavorite_story : R.string.favorite_story);
        btnPrevStory.setEnabled(currentIndex > 0);
        btnNextStory.setEnabled(currentIndex < stories.size() - 1);
        btnPrevStory.setAlpha(currentIndex > 0 ? 1f : 0.4f);
        btnNextStory.setAlpha(currentIndex < stories.size() - 1 ? 1f : 0.4f);
        showImageAt(0);

        btnToggleFavorite.setOnClickListener(v -> {
            boolean favorite = !story.isFavorite();
            StoryRepository.setFavorite(DetailActivity.this, story.getId(), favorite);
            stories = StoryRepository.getStoriesByIds(getIntent().getIntArrayExtra("storyIds"));
            Toast.makeText(
                    DetailActivity.this,
                    favorite ? R.string.favorite_added_success : R.string.favorite_removed_success,
                    Toast.LENGTH_SHORT
            ).show();
            showStoryAt(currentIndex);
            setResult(RESULT_OK);
        });
        btnShareStory.setOnClickListener(v -> shareStory(story));

        btnEditStory.setOnClickListener(v -> {
            Intent intent = new Intent(DetailActivity.this, AddStoryActivity.class);
            intent.putExtra("isEditMode", true);
            intent.putExtra("storyId", story.getId());
            intent.putExtra("title", story.getTitle());
            intent.putExtra("date", story.getDate());
            intent.putExtra("place", story.getPlace());
            intent.putExtra("people", story.getPeople());
            intent.putExtra("memoryText", story.getMemoryText());
            intent.putStringArrayListExtra("imageUris", story.getImageUris());
            editStoryLauncher.launch(intent);
        });
    }

    private void showImageAt(int index) {
        PhotoStory story = stories.get(currentIndex);
        ArrayList<String> imageUris = story.getImageUris();
        if (imageUris.isEmpty()) {
            currentImageIndex = 0;
            ivStoryImage.setVisibility(ImageView.GONE);
            tvImageIndex.setText(R.string.no_image_selected);
            btnPrevImage.setEnabled(false);
            btnNextImage.setEnabled(false);
            btnPrevImage.setAlpha(0.4f);
            btnNextImage.setAlpha(0.4f);
            return;
        }

        if (index < 0 || index >= imageUris.size()) {
            return;
        }

        currentImageIndex = index;
        ivStoryImage.setVisibility(ImageView.VISIBLE);
        ivStoryImage.setImageURI(Uri.parse(imageUris.get(index)));
        tvImageIndex.setText(getString(R.string.image_index_hint, currentImageIndex + 1, imageUris.size()));
        btnPrevImage.setEnabled(currentImageIndex > 0);
        btnNextImage.setEnabled(currentImageIndex < imageUris.size() - 1);
        btnPrevImage.setAlpha(currentImageIndex > 0 ? 1f : 0.4f);
        btnNextImage.setAlpha(currentImageIndex < imageUris.size() - 1 ? 1f : 0.4f);
    }

    private void shareStory(PhotoStory story) {
        ArrayList<Uri> imageUris = new ArrayList<>();
        for (String imageUri : story.getImageUris()) {
            if (imageUri != null && !imageUri.isEmpty()) {
                imageUris.add(Uri.parse(imageUri));
            }
        }

        Intent shareIntent;
        if (imageUris.size() > 1) {
            shareIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
            shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, imageUris);
            shareIntent.setType("image/*");
        } else if (imageUris.size() == 1) {
            shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_STREAM, imageUris.get(0));
            shareIntent.setType("image/*");
        } else {
            shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
        }

        shareIntent.putExtra(Intent.EXTRA_SUBJECT, story.getTitle().isEmpty() ? getString(R.string.no_title) : story.getTitle());
        shareIntent.putExtra(Intent.EXTRA_TEXT, buildShareText(story));
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        if (!imageUris.isEmpty()) {
            ClipData clipData = ClipData.newUri(getContentResolver(), "story_images", imageUris.get(0));
            for (int i = 1; i < imageUris.size(); i++) {
                clipData.addItem(new ClipData.Item(imageUris.get(i)));
            }
            shareIntent.setClipData(clipData);
        }

        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_story)));
    }

    private String buildShareText(PhotoStory story) {
        StringBuilder builder = new StringBuilder();
        builder.append(story.getTitle().isEmpty() ? getString(R.string.no_title) : story.getTitle());
        builder.append("\n");
        builder.append(getString(R.string.detail_date, DateTimeUtils.formatDisplay(story.getDate())));
        builder.append("\n");
        builder.append(getString(R.string.detail_place, story.getPlace()));
        builder.append("\n");
        builder.append(getString(R.string.detail_people, story.getPeople()));
        builder.append("\n\n");
        builder.append(story.getMemoryText());
        return builder.toString();
    }
}
