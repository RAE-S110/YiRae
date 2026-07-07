package com.example.yirae;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;

import java.util.Collections;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends SecureActivity {
    private ListView listViewStories;
    private EditText etSearchKeyword;
    private TextView tvNickname;
    private TextView tvSearchSummary;
    private TextView tvEmptyState;
    private TextView tvTodayReviewSummary;
    private TextView tvMonthReviewSummary;
    private Button btnAddStory;
    private Button btnExportMarkdown;
    private Button btnExportJson;
    private Button btnReviewToday;
    private ImageButton btnCalendar;
    private Button btnFavoriteStories;
    private Button btnSettings;
    private Button btnSearch;
    private StoryListAdapter adapter;
    private final ArrayList<PhotoStory> displayedStories = new ArrayList<>();
    private boolean showingFavoritesOnly;

    private final ActivityResultLauncher<Intent> addStoryLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() != RESULT_OK || result.getData() == null) {
                    return;
                }

                Intent data = result.getData();
                StoryRepository.addStory(
                        MainActivity.this,
                        data.getStringExtra("title"),
                        data.getStringExtra("date"),
                        data.getStringExtra("place"),
                        data.getStringExtra("people"),
                        data.getStringExtra("memoryText"),
                        data.getStringArrayListExtra("tags"),
                        data.getStringArrayListExtra("imageUris"),
                        data.getStringExtra("remoteStoryTitle"),
                        data.getStringExtra("remoteStoryContent"),
                        data.getStringExtra("remoteStoryImageUri")
                );
                Toast.makeText(MainActivity.this, R.string.story_saved_success, Toast.LENGTH_SHORT).show();
                refreshStoryList();
            });

    private final ActivityResultLauncher<Intent> detailLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> refreshStoryList());

    private final ActivityResultLauncher<Intent> calendarLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> refreshStoryList());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        StoryRepository.initialize(this);

        listViewStories = findViewById(R.id.listViewStories);
        etSearchKeyword = findViewById(R.id.etSearchKeyword);
        tvNickname = findViewById(R.id.tvNickname);
        tvSearchSummary = findViewById(R.id.tvSearchSummary);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        tvTodayReviewSummary = findViewById(R.id.tvTodayReviewSummary);
        tvMonthReviewSummary = findViewById(R.id.tvMonthReviewSummary);
        btnAddStory = findViewById(R.id.btnAddStory);
        btnExportMarkdown = findViewById(R.id.btnExportMarkdown);
        btnExportJson = findViewById(R.id.btnExportJson);
        btnReviewToday = findViewById(R.id.btnReviewToday);
        btnCalendar = findViewById(R.id.btnCalendar);
        btnFavoriteStories = findViewById(R.id.btnFavoriteStories);
        btnSettings = findViewById(R.id.btnSettings);
        btnSearch = findViewById(R.id.btnSearch);

        adapter = new StoryListAdapter(this, new StoryListAdapter.StoryActionListener() {
            @Override
            public void onStorySelected(PhotoStory story) {
                openDetail(story);
            }

            @Override
            public void onStoryFavoriteToggle(PhotoStory story) {
                boolean favorite = !story.isFavorite();
                StoryRepository.setFavorite(MainActivity.this, story.getId(), favorite);
                Toast.makeText(
                        MainActivity.this,
                        favorite ? R.string.favorite_added_success : R.string.favorite_removed_success,
                        Toast.LENGTH_SHORT
                ).show();
                refreshStoryList();
            }

            @Override
            public void onStoryShare(PhotoStory story) {
                StoryShareHelper.shareStory(MainActivity.this, story);
            }

            @Override
            public void onStoryDelete(PhotoStory story) {
                confirmDelete(story);
            }
        }, true);
        listViewStories.setAdapter(adapter);

        btnAddStory.setOnClickListener(v -> addStoryLauncher.launch(new Intent(MainActivity.this, AddStoryActivity.class)));
        btnCalendar.setOnClickListener(v -> calendarLauncher.launch(new Intent(MainActivity.this, CalendarActivity.class)));
        btnFavoriteStories.setOnClickListener(v -> {
            showingFavoritesOnly = !showingFavoritesOnly;
            refreshStoryList();
        });
        btnExportMarkdown.setOnClickListener(v -> exportStories(true));
        btnExportJson.setOnClickListener(v -> exportStories(false));
        btnReviewToday.setOnClickListener(v -> showTodayReviewDialog());
        btnSettings.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, SettingsActivity.class)));
        btnSearch.setOnClickListener(v -> refreshStoryList());

        refreshStoryList();
    }

    @Override
    protected void onResume() {
        super.onResume();
        tvNickname.setText(getString(R.string.nickname_greeting, UserSettingsRepository.getNickname(this)));
        refreshStoryList();
    }

    private void refreshStoryList() {
        String keyword = etSearchKeyword.getText().toString().trim();
        List<PhotoStory> allStories = StoryRepository.getStories();

        displayedStories.clear();
        for (PhotoStory story : allStories) {
            if ((!showingFavoritesOnly || story.isFavorite()) && story.matchesKeyword(keyword)) {
                displayedStories.add(story);
            }
        }
        Collections.sort(displayedStories, (left, right) -> {
            if (left.isFavorite() != right.isFavorite()) {
                return left.isFavorite() ? -1 : 1;
            }
            return DateTimeUtils.compareStoredDateDesc(left.getDate(), right.getDate());
        });

        adapter.submitList(displayedStories);
        btnFavoriteStories.setText(showingFavoritesOnly ? R.string.view_all_stories : R.string.favorite_stories);
        updateEmptyState(allStories);
        updateReviewSummary(allStories);
        if (showingFavoritesOnly && keyword.isEmpty()) {
            tvSearchSummary.setText(getString(R.string.favorite_story_summary, displayedStories.size()));
        } else if (keyword.isEmpty()) {
            tvSearchSummary.setText(getString(R.string.all_story_summary, displayedStories.size()));
        } else {
            tvSearchSummary.setText(getString(R.string.search_result_summary, keyword, displayedStories.size()));
        }
    }

    private void updateReviewSummary(List<PhotoStory> allStories) {
        Calendar today = Calendar.getInstance();
        ArrayList<PhotoStory> sameDayStories = new ArrayList<>();
        int monthStoryCount = 0;
        int monthFavoriteCount = 0;
        PhotoStory latestMonthStory = null;

        for (PhotoStory story : allStories) {
            if (DateTimeUtils.isSameMonthDay(story.getDate(), today)) {
                sameDayStories.add(story);
            }
            if (DateTimeUtils.isSameMonth(story.getDate(), today)) {
                monthStoryCount++;
                if (story.isFavorite()) {
                    monthFavoriteCount++;
                }
                if (latestMonthStory == null || DateTimeUtils.compareStoredDateDesc(story.getDate(), latestMonthStory.getDate()) < 0) {
                    latestMonthStory = story;
                }
            }
        }

        Collections.sort(sameDayStories, (left, right) -> DateTimeUtils.compareStoredDateDesc(left.getDate(), right.getDate()));
        if (sameDayStories.isEmpty()) {
            tvTodayReviewSummary.setText(R.string.today_review_empty);
            btnReviewToday.setEnabled(false);
            btnReviewToday.setAlpha(0.5f);
        } else {
            PhotoStory latest = sameDayStories.get(0);
            Calendar storyDate = DateTimeUtils.parseStoredDate(latest.getDate());
            String latestYear = storyDate == null ? "" : DateTimeUtils.formatDisplayYear(storyDate);
            tvTodayReviewSummary.setText(getString(
                    R.string.today_review_summary,
                    sameDayStories.size(),
                    latestYear,
                    latest.getTitle().isEmpty() ? getString(R.string.no_title) : latest.getTitle()
            ));
            btnReviewToday.setEnabled(true);
            btnReviewToday.setAlpha(1f);
        }

        String latestMonthTitle = latestMonthStory == null || latestMonthStory.getTitle().isEmpty()
                ? getString(R.string.no_title)
                : latestMonthStory.getTitle();
        tvMonthReviewSummary.setText(getString(
                R.string.month_review_summary,
                DateTimeUtils.formatDisplayMonth(today),
                monthStoryCount,
                monthFavoriteCount,
                monthStoryCount == 0 ? getString(R.string.month_review_empty_title) : latestMonthTitle
        ));
    }

    private void showTodayReviewDialog() {
        ArrayList<PhotoStory> reviewStories = new ArrayList<>();
        Calendar today = Calendar.getInstance();
        for (PhotoStory story : StoryRepository.getStories()) {
            if (DateTimeUtils.isSameMonthDay(story.getDate(), today)) {
                reviewStories.add(story);
            }
        }

        if (reviewStories.isEmpty()) {
            Toast.makeText(this, R.string.today_review_empty, Toast.LENGTH_SHORT).show();
            return;
        }

        Collections.sort(reviewStories, (left, right) -> DateTimeUtils.compareStoredDateDesc(left.getDate(), right.getDate()));
        CharSequence[] items = new CharSequence[reviewStories.size()];
        for (int i = 0; i < reviewStories.size(); i++) {
            PhotoStory story = reviewStories.get(i);
            Calendar storyDate = DateTimeUtils.parseStoredDate(story.getDate());
            String year = storyDate == null ? "" : DateTimeUtils.formatDisplayYear(storyDate);
            items[i] = year + " - " + (story.getTitle().isEmpty() ? getString(R.string.no_title) : story.getTitle());
        }

        new AlertDialog.Builder(this)
                .setTitle(R.string.today_review_dialog_title)
                .setItems(items, (dialog, which) -> openDetailFromStories(reviewStories, reviewStories.get(which)))
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void exportStories(boolean markdown) {
        List<PhotoStory> stories = StoryRepository.getStories();
        if (stories.isEmpty()) {
            Toast.makeText(this, R.string.empty_story_list, Toast.LENGTH_SHORT).show();
            return;
        }

        boolean exported = markdown
                ? StoryExportHelper.exportStoriesAsMarkdown(this, stories)
                : StoryExportHelper.exportStoriesAsJson(this, stories);
        Toast.makeText(
                this,
                exported ? (markdown ? R.string.export_markdown_success : R.string.export_json_success) : R.string.export_failed,
                Toast.LENGTH_SHORT
        ).show();
    }

    private void updateEmptyState(List<PhotoStory> allStories) {
        if (allStories.isEmpty()) {
            tvEmptyState.setText(R.string.empty_story_list);
            tvEmptyState.setVisibility(TextView.VISIBLE);
            return;
        }

        if (showingFavoritesOnly && displayedStories.isEmpty()) {
            tvEmptyState.setText(R.string.empty_favorite_list);
            tvEmptyState.setVisibility(TextView.VISIBLE);
            return;
        }

        if (displayedStories.isEmpty()) {
            tvEmptyState.setText(R.string.empty_search_result);
            tvEmptyState.setVisibility(TextView.VISIBLE);
        } else {
            tvEmptyState.setVisibility(TextView.GONE);
        }
    }

    private void confirmDelete(PhotoStory story) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_story)
                .setMessage(R.string.delete_story_confirm)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    StoryRepository.deleteStory(MainActivity.this, story.getId());
                    refreshStoryList();
                })
                .show();
    }

    private void openDetail(PhotoStory story) {
        adapter.closeOpenedItem();
        openDetailFromStories(displayedStories, story);
    }

    private void openDetailFromStories(List<PhotoStory> stories, PhotoStory selectedStory) {
        int[] storyIds = new int[stories.size()];
        int currentIndex = 0;
        for (int i = 0; i < stories.size(); i++) {
            storyIds[i] = stories.get(i).getId();
            if (stories.get(i).getId() == selectedStory.getId()) {
                currentIndex = i;
            }
        }

        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra("storyIds", storyIds);
        intent.putExtra("currentIndex", currentIndex);
        detailLauncher.launch(intent);
    }
}
