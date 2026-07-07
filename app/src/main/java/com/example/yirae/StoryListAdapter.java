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

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class StoryListAdapter extends BaseAdapter {
    public interface StoryActionListener {
        void onStorySelected(PhotoStory story);
        void onStoryFavoriteToggle(PhotoStory story);
        void onStoryShare(PhotoStory story);
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
        if (story.hasTags()) {
            holder.tags.setText(story.buildTagText());
            holder.tags.setVisibility(View.VISIBLE);
        } else {
            holder.tags.setVisibility(View.GONE);
        }
        holder.favoriteBadge.setVisibility(story.isFavorite() ? View.VISIBLE : View.GONE);
        holder.foreground.setAlpha(1f);

        if (story.getCoverImageUri().isEmpty()) {
            holder.image.setImageResource(android.R.drawable.ic_menu_gallery);
        } else {
            Glide.with(parent.getContext())
                    .load(story.getCoverImageUri())
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_report_image)
                    .into(holder.image);
        }

        int actionWidth = holder.actionContainer.getLayoutParams().width;
        boolean canDelete = deleteEnabled;
        holder.foreground.setTranslationX(story.getId() == openedStoryId && canDelete ? -actionWidth : 0f);
        holder.actionContainer.setVisibility(canDelete ? View.VISIBLE : View.GONE);
        holder.swipeFavoriteLabel.setText(story.isFavorite() ? R.string.unfavorite_story : R.string.favorite_story);

        holder.foreground.setOnClickListener(v -> {
            if (story.getId() == openedStoryId) {
                closeOpenedItem();
            } else {
                listener.onStorySelected(story);
            }
        });
        holder.swipeFavoriteContainer.setOnClickListener(v -> listener.onStoryFavoriteToggle(story));
        holder.swipeShareContainer.setOnClickListener(v -> listener.onStoryShare(story));
        holder.deleteContainer.setOnClickListener(v -> listener.onStoryDelete(story));

        if (canDelete) {
            attachSwipeTouch(holder, story, actionWidth);
        } else {
            holder.foreground.setOnTouchListener(null);
        }

        return convertView;
    }

    private void attachSwipeTouch(ViewHolder holder, PhotoStory story, int actionWidth) {
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
                            float minTranslation = deleteEnabled ? -actionWidth : 0f;
                            float newTranslation = Math.max(minTranslation, Math.min(0f, startTranslationX + diffX));
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
                        if (finalTranslation < -actionWidth / 3f && deleteEnabled) {
                            openedStoryId = story.getId();
                            v.setTranslationX(-actionWidth);
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
        private final View actionContainer;
        private final View swipeFavoriteContainer;
        private final View swipeShareContainer;
        private final View deleteContainer;
        private final ImageView image;
        private final TextView favoriteBadge;
        private final TextView swipeFavoriteLabel;
        private final TextView title;
        private final TextView subtitle;
        private final TextView tags;

        private ViewHolder(View view) {
            foreground = view.findViewById(R.id.storyForeground);
            actionContainer = view.findViewById(R.id.actionContainer);
            swipeFavoriteContainer = view.findViewById(R.id.swipeFavoriteContainer);
            swipeShareContainer = view.findViewById(R.id.swipeShareContainer);
            deleteContainer = view.findViewById(R.id.deleteContainer);
            image = view.findViewById(R.id.ivItemImage);
            favoriteBadge = view.findViewById(R.id.tvFavoriteBadge);
            swipeFavoriteLabel = view.findViewById(R.id.tvSwipeFavoriteLabel);
            title = view.findViewById(R.id.tvItemTitle);
            subtitle = view.findViewById(R.id.tvItemSubtitle);
            tags = view.findViewById(R.id.tvItemTags);
        }
    }
}
