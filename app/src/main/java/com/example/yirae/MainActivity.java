package com.example.yirae;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends SecureActivity {
    private ListView listViewStories;
    private EditText etSearchKeyword;
    private TextView tvNickname;
    private TextView tvSearchSummary;
    private TextView tvEmptyState;
    private Button btnAddStory;
    private Button btnCalendar;
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
                        data.getStringArrayListExtra("imageUris")
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
        btnAddStory = findViewById(R.id.btnAddStory);
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

        if (showingFavoritesOnly && keyword.isEmpty()) {
            tvSearchSummary.setText(getString(R.string.favorite_story_summary, displayedStories.size()));
        } else if (keyword.isEmpty()) {
            tvSearchSummary.setText(getString(R.string.all_story_summary, displayedStories.size()));
        } else {
            tvSearchSummary.setText(getString(R.string.search_result_summary, keyword, displayedStories.size()));
        }
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
        int[] storyIds = new int[displayedStories.size()];
        int currentIndex = 0;
        for (int i = 0; i < displayedStories.size(); i++) {
            storyIds[i] = displayedStories.get(i).getId();
            if (displayedStories.get(i).getId() == story.getId()) {
                currentIndex = i;
            }
        }

        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra("storyIds", storyIds);
        intent.putExtra("currentIndex", currentIndex);
        detailLauncher.launch(intent);
    }
}
