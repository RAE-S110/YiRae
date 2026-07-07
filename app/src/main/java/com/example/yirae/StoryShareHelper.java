package com.example.yirae;

import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.net.Uri;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class StoryShareHelper {
    private static final int CARD_WIDTH = 1080;
    private static final int CARD_HORIZONTAL_PADDING = 72;
    private static final int CARD_TOP_PADDING = 88;
    private static final int CARD_BOTTOM_PADDING = 88;
    private static final int SECTION_GAP = 26;
    private static final int LINE_GAP = 14;
    private static final int PARAGRAPH_GAP = 28;
    private static final int IMAGE_HEIGHT = 460;
    private static final int IMAGE_GAP = 16;

    private StoryShareHelper() {
    }

    public static void shareStory(Context context, PhotoStory story) {
        ArrayList<PhotoStory> stories = new ArrayList<>();
        stories.add(story);
        shareStories(context, stories);
    }

    public static void shareStories(Context context, List<PhotoStory> stories) {
        if (stories == null || stories.isEmpty()) {
            return;
        }

        ArrayList<Uri> imageUris = new ArrayList<>();
        StringBuilder textBuilder = new StringBuilder();
        for (int i = 0; i < stories.size(); i++) {
            PhotoStory story = stories.get(i);
            if (i > 0) {
                textBuilder.append("\n\n----------------\n\n");
            }
            textBuilder.append(buildShareText(context, story));
            Uri cardUri = createShareCard(context, story);
            if (cardUri != null) {
                imageUris.add(cardUri);
            }
        }

        if (imageUris.isEmpty()) {
            return;
        }

        Intent shareIntent;
        if (imageUris.size() > 1) {
            shareIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
            shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, imageUris);
            shareIntent.setType("image/*");
        } else {
            shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_STREAM, imageUris.get(0));
            shareIntent.setType("image/*");
        }

        PhotoStory firstStory = stories.get(0);
        shareIntent.putExtra(
                Intent.EXTRA_SUBJECT,
                firstStory.getTitle().isEmpty() ? context.getString(R.string.no_title) : firstStory.getTitle()
        );
        shareIntent.putExtra(Intent.EXTRA_TEXT, textBuilder.toString());
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        if (!imageUris.isEmpty()) {
            ClipData clipData = ClipData.newUri(context.getContentResolver(), "story_images", imageUris.get(0));
            for (int i = 1; i < imageUris.size(); i++) {
                clipData.addItem(new ClipData.Item(imageUris.get(i)));
            }
            shareIntent.setClipData(clipData);
        }

        context.startActivity(Intent.createChooser(shareIntent, context.getString(R.string.share_story)));
    }

    private static Uri createShareCard(Context context, PhotoStory story) {
        Paint titlePaint = createPaint(54f, true, 0xFF4E372A);
        Paint bodyPaint = createPaint(34f, false, 0xFF4E372A);
        Paint metaPaint = createPaint(32f, false, 0xFF7A6354);
        Paint badgePaint = createPaint(28f, true, 0xFF6F4A1E);

        String title = story.getTitle().isEmpty() ? context.getString(R.string.no_title) : story.getTitle();
        String date = context.getString(R.string.detail_date, DateTimeUtils.formatDisplay(story.getDate()));
        String place = context.getString(R.string.detail_place, story.getPlace());
        String people = context.getString(R.string.detail_people, story.getPeople());
        String tags = context.getString(R.string.detail_tags, story.hasTags() ? story.buildTagText() : context.getString(R.string.no_tags));
        String memory = story.getMemoryText().isEmpty() ? context.getString(R.string.no_story_summary) : story.getMemoryText();
        ArrayList<String> storyImages = story.getImageUris();
        boolean hasImage = !storyImages.isEmpty();

        float contentWidth = CARD_WIDTH - CARD_HORIZONTAL_PADDING * 2f;
        List<String> titleLines = wrapText(title, titlePaint, contentWidth);
        List<String> dateLines = wrapText(date, metaPaint, contentWidth);
        List<String> placeLines = wrapText(place, metaPaint, contentWidth);
        List<String> peopleLines = wrapText(people, metaPaint, contentWidth);
        List<String> tagLines = wrapText(tags, metaPaint, contentWidth);
        List<String> memoryLines = wrapText(memory, bodyPaint, contentWidth);

        int imageAreaHeight = resolveImageAreaHeight(storyImages.size());
        int cardHeight = CARD_TOP_PADDING
                + (hasImage ? imageAreaHeight + SECTION_GAP : 0)
                + measureBlockHeight(titleLines.size(), titlePaint)
                + SECTION_GAP
                + measureBlockHeight(dateLines.size(), metaPaint)
                + LINE_GAP
                + measureBlockHeight(placeLines.size(), metaPaint)
                + LINE_GAP
                + measureBlockHeight(peopleLines.size(), metaPaint)
                + LINE_GAP
                + measureBlockHeight(tagLines.size(), metaPaint)
                + PARAGRAPH_GAP
                + measureBlockHeight(memoryLines.size(), bodyPaint)
                + (story.isFavorite() ? (SECTION_GAP + 60) : 0)
                + CARD_BOTTOM_PADDING;

        Bitmap bitmap = Bitmap.createBitmap(CARD_WIDTH, cardHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(0xFFF3E7D3);

        Paint cardPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        cardPaint.setColor(0xFFFBF4E8);
        Paint shadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        shadowPaint.setColor(0xFFE2CBAE);

        RectF shadowRect = new RectF(28, 28, CARD_WIDTH - 28, cardHeight - 20);
        RectF cardRect = new RectF(20, 20, CARD_WIDTH - 36, cardHeight - 36);
        canvas.drawRoundRect(shadowRect, 36, 36, shadowPaint);
        canvas.drawRoundRect(cardRect, 36, 36, cardPaint);

        float x = CARD_HORIZONTAL_PADDING;
        float y = CARD_TOP_PADDING;
        if (hasImage) {
            drawStoryImages(context, canvas, storyImages, x, y, contentWidth, imageAreaHeight);
            y += imageAreaHeight + SECTION_GAP;
        }
        y = drawLines(canvas, titleLines, x, y, titlePaint);
        y += SECTION_GAP;
        y = drawLines(canvas, dateLines, x, y, metaPaint);
        y += LINE_GAP;
        y = drawLines(canvas, placeLines, x, y, metaPaint);
        y += LINE_GAP;
        y = drawLines(canvas, peopleLines, x, y, metaPaint);
        y += LINE_GAP;
        y = drawLines(canvas, tagLines, x, y, metaPaint);
        y += PARAGRAPH_GAP;
        y = drawLines(canvas, memoryLines, x, y, bodyPaint);

        if (story.isFavorite()) {
            float badgeTop = y + SECTION_GAP;
            Paint badgeBackground = new Paint(Paint.ANTI_ALIAS_FLAG);
            badgeBackground.setColor(0xFFE0C08A);
            RectF badgeRect = new RectF(x, badgeTop, x + 210, badgeTop + 56);
            canvas.drawRoundRect(badgeRect, 28, 28, badgeBackground);
            float badgeBaseline = badgeTop + 38;
            canvas.drawText(context.getString(R.string.favorite_badge), x + 26, badgeBaseline, badgePaint);
        }

        File outputDir = new File(context.getCacheDir(), "shared_cards");
        if (!outputDir.exists() && !outputDir.mkdirs()) {
            return null;
        }

        File outputFile = new File(outputDir, "story_" + story.getId() + "_" + System.currentTimeMillis() + ".png");
        try (FileOutputStream outputStream = new FileOutputStream(outputFile)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            outputStream.flush();
        } catch (IOException e) {
            return null;
        } finally {
            bitmap.recycle();
        }

        return FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", outputFile);
    }

    private static void drawStoryImages(Context context, Canvas canvas, List<String> imageUris, float x, float y, float width, float height) {
        if (imageUris == null || imageUris.isEmpty()) {
            return;
        }
        float currentY = y;
        for (int i = 0; i < imageUris.size(); i++) {
            drawStoryImage(context, canvas, imageUris.get(i), x, currentY, width, IMAGE_HEIGHT);
            currentY += IMAGE_HEIGHT;
            if (i < imageUris.size() - 1) {
                currentY += IMAGE_GAP;
            }
        }
    }

    private static int resolveImageAreaHeight(int imageCount) {
        if (imageCount <= 0) {
            return 0;
        }
        return imageCount * IMAGE_HEIGHT + (imageCount - 1) * IMAGE_GAP;
    }

    private static void drawStoryImage(Context context, Canvas canvas, String imageUri, float x, float y, float width, float height) {
        Bitmap sourceBitmap = null;
        Bitmap scaledBitmap = null;
        try {
            sourceBitmap = BitmapFactory.decodeStream(context.getContentResolver().openInputStream(Uri.parse(imageUri)));
            if (sourceBitmap == null) {
                return;
            }

            float scale = Math.max(width / sourceBitmap.getWidth(), height / sourceBitmap.getHeight());
            Matrix matrix = new Matrix();
            matrix.postScale(scale, scale);
            scaledBitmap = Bitmap.createBitmap(
                    sourceBitmap,
                    0,
                    0,
                    sourceBitmap.getWidth(),
                    sourceBitmap.getHeight(),
                    matrix,
                    true
            );

            float left = x + (width - scaledBitmap.getWidth()) / 2f;
            float top = y + (height - scaledBitmap.getHeight()) / 2f;
            Paint framePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            framePaint.setColor(0xFFF3E7D3);
            framePaint.setStyle(Paint.Style.STROKE);
            framePaint.setStrokeWidth(6f);
            canvas.save();
            canvas.clipRect(x, y, x + width, y + height);
            canvas.drawBitmap(scaledBitmap, left, top, null);
            canvas.restore();
            canvas.drawRoundRect(new RectF(x, y, x + width, y + height), 20, 20, framePaint);
        } catch (Exception ignored) {
        } finally {
            if (scaledBitmap != null) {
                scaledBitmap.recycle();
            }
            if (sourceBitmap != null) {
                sourceBitmap.recycle();
            }
        }
    }

    private static Paint createPaint(float textSize, boolean bold, int color) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(color);
        paint.setTextSize(textSize);
        paint.setFakeBoldText(bold);
        return paint;
    }

    private static int measureBlockHeight(int lineCount, Paint paint) {
        if (lineCount <= 0) {
            return 0;
        }
        Paint.FontMetrics fontMetrics = paint.getFontMetrics();
        float lineHeight = fontMetrics.descent - fontMetrics.ascent + 10f;
        return Math.round(lineCount * lineHeight);
    }

    private static float drawLines(Canvas canvas, List<String> lines, float x, float y, Paint paint) {
        Paint.FontMetrics fontMetrics = paint.getFontMetrics();
        float lineHeight = fontMetrics.descent - fontMetrics.ascent + 10f;
        float baseline = y - fontMetrics.ascent;
        for (String line : lines) {
            canvas.drawText(line, x, baseline, paint);
            baseline += lineHeight;
        }
        return baseline + fontMetrics.ascent;
    }

    private static List<String> wrapText(String text, Paint paint, float maxWidth) {
        ArrayList<String> lines = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            lines.add("");
            return lines;
        }

        String[] paragraphs = text.split("\\n");
        for (String paragraph : paragraphs) {
            if (paragraph.isEmpty()) {
                lines.add("");
                continue;
            }

            StringBuilder currentLine = new StringBuilder();
            for (int i = 0; i < paragraph.length(); i++) {
                char ch = paragraph.charAt(i);
                String candidate = currentLine.toString() + ch;
                if (paint.measureText(candidate) > maxWidth && currentLine.length() > 0) {
                    lines.add(currentLine.toString());
                    currentLine = new StringBuilder().append(ch);
                } else {
                    currentLine.append(ch);
                }
            }
            if (currentLine.length() > 0) {
                lines.add(currentLine.toString());
            }
        }
        return lines;
    }

    private static String buildShareText(Context context, PhotoStory story) {
        StringBuilder builder = new StringBuilder();
        builder.append(story.getTitle().isEmpty() ? context.getString(R.string.no_title) : story.getTitle());
        builder.append("\n");
        builder.append(context.getString(R.string.detail_date, DateTimeUtils.formatDisplay(story.getDate())));
        builder.append("\n");
        builder.append(context.getString(R.string.detail_place, story.getPlace()));
        builder.append("\n");
        builder.append(context.getString(R.string.detail_people, story.getPeople()));
        builder.append("\n");
        builder.append(context.getString(R.string.detail_tags, story.hasTags() ? story.buildTagText() : context.getString(R.string.no_tags)));
        builder.append("\n\n");
        builder.append(story.getMemoryText());
        return builder.toString();
    }
}
