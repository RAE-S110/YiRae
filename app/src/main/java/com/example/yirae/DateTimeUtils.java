package com.example.yirae;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public final class DateTimeUtils {
    private static final Locale LOCALE = Locale.getDefault();
    private static final SimpleDateFormat STORAGE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd", LOCALE);
    private static final SimpleDateFormat STORAGE_DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm", LOCALE);
    private static final SimpleDateFormat DISPLAY_DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd", LOCALE);
    private static final SimpleDateFormat DISPLAY_DATE_TIME_FORMAT = new SimpleDateFormat("yyyy/MM/dd HH:mm", LOCALE);
    private static final SimpleDateFormat DISPLAY_TIME_FORMAT = new SimpleDateFormat("HH:mm", LOCALE);

    private DateTimeUtils() {
    }

    public static String buildStoredDate(Calendar calendar, boolean hasTime) {
        return hasTime ? STORAGE_DATE_TIME_FORMAT.format(calendar.getTime()) : STORAGE_DATE_FORMAT.format(calendar.getTime());
    }

    public static String formatDisplay(String storedDateTime) {
        Calendar calendar = parseStoredDate(storedDateTime);
        if (calendar == null) {
            return storedDateTime == null ? "" : storedDateTime;
        }
        return hasTime(storedDateTime)
                ? DISPLAY_DATE_TIME_FORMAT.format(calendar.getTime())
                : DISPLAY_DATE_FORMAT.format(calendar.getTime());
    }

    public static String formatDisplayDate(Calendar calendar) {
        return DISPLAY_DATE_FORMAT.format(calendar.getTime());
    }

    public static String formatDisplayTime(Calendar calendar) {
        return DISPLAY_TIME_FORMAT.format(calendar.getTime());
    }

    public static String formatDisplayMonth(Calendar calendar) {
        return String.format(LOCALE, "%04d/%02d", calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1);
    }

    public static boolean hasTime(String storedDateTime) {
        return storedDateTime != null && storedDateTime.trim().length() > 10;
    }

    public static Calendar parseStoredDate(String storedDateTime) {
        if (storedDateTime == null || storedDateTime.trim().isEmpty()) {
            return null;
        }

        Date date;
        try {
            date = hasTime(storedDateTime)
                    ? STORAGE_DATE_TIME_FORMAT.parse(storedDateTime)
                    : STORAGE_DATE_FORMAT.parse(storedDateTime);
        } catch (Exception e) {
            return null;
        }

        if (date == null) {
            return null;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar;
    }

    public static boolean isSameDay(String storedDateTime, Calendar selectedDay) {
        Calendar storyDay = parseStoredDate(storedDateTime);
        return storyDay != null
                && storyDay.get(Calendar.YEAR) == selectedDay.get(Calendar.YEAR)
                && storyDay.get(Calendar.DAY_OF_YEAR) == selectedDay.get(Calendar.DAY_OF_YEAR);
    }

    public static boolean isSameMonth(String storedDateTime, Calendar selectedMonth) {
        Calendar storyDay = parseStoredDate(storedDateTime);
        return storyDay != null
                && storyDay.get(Calendar.YEAR) == selectedMonth.get(Calendar.YEAR)
                && storyDay.get(Calendar.MONTH) == selectedMonth.get(Calendar.MONTH);
    }

    public static int compareStoredDateDesc(String left, String right) {
        Calendar leftCalendar = parseStoredDate(left);
        Calendar rightCalendar = parseStoredDate(right);
        long leftTime = leftCalendar == null ? Long.MIN_VALUE : leftCalendar.getTimeInMillis();
        long rightTime = rightCalendar == null ? Long.MIN_VALUE : rightCalendar.getTimeInMillis();
        return Long.compare(rightTime, leftTime);
    }
}
