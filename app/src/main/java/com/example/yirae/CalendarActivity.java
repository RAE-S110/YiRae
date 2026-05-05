package com.example.yirae;

import android.content.Intent;
import android.os.Bundle;
import android.widget.CalendarView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public class CalendarActivity extends SecureActivity {
    private CalendarView calendarView;
    private TextView tvCalendarSummary;
    private TextView tvMonthSummary;
    private ListView listViewCalendarStories;
    private StoryListAdapter adapter;
    private final ArrayList<PhotoStory> displayedStories = new ArrayList<>();
    private Calendar selectedDate;

    private final ActivityResultLauncher<Intent> detailLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> refreshStoryList());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);
        StoryRepository.initialize(this);

        calendarView = findViewById(R.id.calendarView);
        tvCalendarSummary = findViewById(R.id.tvCalendarSummary);
        tvMonthSummary = findViewById(R.id.tvMonthSummary);
        listViewCalendarStories = findViewById(R.id.listViewCalendarStories);
        selectedDate = Calendar.getInstance();
        selectedDate.setTimeInMillis(calendarView.getDate());

        adapter = new StoryListAdapter(this, new StoryListAdapter.StoryActionListener() {
            @Override
            public void onStorySelected(PhotoStory story) {
                openDetail(story);
            }

            @Override
            public void onStoryDelete(PhotoStory story) {
            }
        }, false);
        listViewCalendarStories.setAdapter(adapter);

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            selectedDate.set(Calendar.YEAR, year);
            selectedDate.set(Calendar.MONTH, month);
            selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            refreshStoryList();
        });

        refreshStoryList();
    }

    private void refreshStoryList() {
        List<PhotoStory> allStories = StoryRepository.getStories();
        displayedStories.clear();
        int monthStoryCount = 0;
        for (PhotoStory story : allStories) {
            if (DateTimeUtils.isSameMonth(story.getDate(), selectedDate)) {
                monthStoryCount++;
            }
            if (DateTimeUtils.isSameDay(story.getDate(), selectedDate)) {
                displayedStories.add(story);
            }
        }
        Collections.sort(displayedStories, (left, right) -> DateTimeUtils.compareStoredDateDesc(left.getDate(), right.getDate()));
        adapter.submitList(displayedStories);
        tvCalendarSummary.setText(getString(
                R.string.calendar_summary,
                DateTimeUtils.formatDisplayDate(selectedDate),
                displayedStories.size()
        ));
        tvMonthSummary.setText(getString(
                R.string.month_summary,
                DateTimeUtils.formatDisplayMonth(selectedDate),
                monthStoryCount
        ));
    }

    private void openDetail(PhotoStory story) {
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
