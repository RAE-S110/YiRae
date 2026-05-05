package com.example.yirae;

public final class SessionManager {
    private static boolean unlocked;

    private SessionManager() {
    }

    public static boolean isUnlocked() {
        return unlocked;
    }

    public static void unlock() {
        unlocked = true;
    }

    public static void lock() {
        unlocked = false;
    }
}
