package com.example.yirae;

import android.content.Context;
import android.net.Uri;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class StoryListAdapter extends BaseAdapter {
    public interface StoryActionListener {
        void onStorySelected(PhotoStory story);

        void onStoryDelete(PhotoStory story);
    }

    private final android.view.LayoutInflater inflater;
    private final List<PhotoStory> stories = new ArrayList<>();
    private final StoryActionListener listener;
    private final boolean deleteEnabled;
    private final int touchSlop;
    private int openedStoryId = -1;

    public StoryListAdapter(Context context, StoryActionListener listener, boolean deleteEnabled) {
        inflater = android.view.LayoutInflater.from(context);
        this.listener = listener;
        this.deleteEnabled = deleteEnabled;
        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    public void submitList(List<PhotoStory> updatedStories) {
        stories.clear();
        stories.addAll(updatedStories);
        boolean stillExists = false;
        for (PhotoStory story : stories) {
            if (story.getId() == openedStoryId) {
                stillExists = true;
                break;
            }
        }
        if (!stillExists) {
            openedStoryId = -1;
        }
        notifyDataSetChanged();
    }

    public void closeOpenedItem() {
        if (openedStoryId == -1) {
            return;
        }
        openedStoryId = -1;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return stories.size();
    }

    @Override
    public PhotoStory getItem(int position) {
        return stories.get(position);
    }

    @Override
    public long getItemId(int position) {
        return stories.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_story, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        PhotoStory story = getItem(position);
        holder.title.setText(story.getTitle().isEmpty() ? parent.getContext().getString(R.string.no_title) : story.getTitle());

        String subtitle = story.buildSubtitle();
        holder.subtitle.setText(subtitle.isEmpty() ? parent.getContext().getString(R.string.no_story_summary) : subtitle);
        holder.favoriteBadge.setVisibility(story.isFavorite() ? View.VISIBLE : View.GONE);

        if (story.getCoverImageUri().isEmpty()) {
            holder.image.setImageResource(android.R.drawable.ic_menu_gallery);
        } else {
            holder.image.setImageURI(Uri.parse(story.getCoverImageUri()));
        }

        int deleteWidth = holder.deleteContainer.getLayoutParams().width;
        holder.foreground.setTranslationX(story.getId() == openedStoryId && deleteEnabled ? -deleteWidth : 0f);
        holder.deleteContainer.setVisibility(deleteEnabled ? View.VISIBLE : View.GONE);

        holder.foreground.setOnClickListener(v -> {
            if (story.getId() == openedStoryId) {
                closeOpenedItem();
            } else {
                listener.onStorySelected(story);
            }
        });

        holder.deleteContainer.setOnClickListener(v -> listener.onStoryDelete(story));

        if (deleteEnabled) {
            attachSwipeTouch(holder, story, deleteWidth);
        } else {
            holder.foreground.setOnTouchListener(null);
        }

        return convertView;
    }

    private void attachSwipeTouch(ViewHolder holder, PhotoStory story, int deleteWidth) {
        holder.foreground.setOnTouchListener(new View.OnTouchListener() {
            private float downX;
            private float startTranslationX;
            private boolean moved;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        downX = event.getRawX();
                        startTranslationX = v.getTranslationX();
                        moved = false;
                        return false;
                    case MotionEvent.ACTION_MOVE:
                        float diffX = event.getRawX() - downX;
                        if (!moved && Math.abs(diffX) > touchSlop) {
                            moved = true;
                            v.getParent().requestDisallowInterceptTouchEvent(true);
                        }
                        if (moved) {
                            float newTranslation = Math.max(-deleteWidth, Math.min(0f, startTranslationX + diffX));
                            v.setTranslationX(newTranslation);
                            return true;
                        }
                        return false;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        if (!moved) {
                            return false;
                        }
                        float finalTranslation = v.getTranslationX();
                        if (Math.abs(finalTranslation) > deleteWidth / 2f) {
                            openedStoryId = story.getId();
                            v.setTranslationX(-deleteWidth);
                        } else {
                            if (openedStoryId == story.getId()) {
                                openedStoryId = -1;
                            }
                            v.setTranslationX(0f);
                        }
                        notifyDataSetChanged();
                        return true;
                    default:
                        return false;
                }
            }
        });
    }

    private static class ViewHolder {
        private final View foreground;
        private final View deleteContainer;
        private final ImageView image;
        private final TextView favoriteBadge;
        private final TextView title;
        private final TextView subtitle;

        private ViewHolder(View view) {
            foreground = view.findViewById(R.id.storyForeground);
            deleteContainer = view.findViewById(R.id.deleteContainer);
            image = view.findViewById(R.id.ivItemImage);
            favoriteBadge = view.findViewById(R.id.tvFavoriteBadge);
            title = view.findViewById(R.id.tvItemTitle);
            subtitle = view.findViewById(R.id.tvItemSubtitle);
        }
    }
}
