package com.example.bs.util;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;
import androidx.core.content.ContextCompat;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import com.example.bs.util.ReminderWorker;
import java.util.concurrent.TimeUnit;

/**
 * Планировщик для периодической проверки записей
 */
public class NotificationScheduler {

    private static final String TAG = "NotificationScheduler";
    private static final String WORK_TAG = "beauty_salon_reminder_work";
    private static final String PREFS_NAME = "notification_prefs";
    private static final String KEY_NOTIFICATIONS_ENABLED = "notifications_enabled";

    /**
     * Получает SharedPreferences
     */
    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Запускает периодическую проверку записей для напоминаний
     */
    public static void scheduleReminderWork(Context context) {
        try {
            Log.d(TAG, "Attempting to schedule reminder work");

            // Проверяем разрешение на уведомления
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(context,
                        Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "Notification permission not granted, skipping scheduling");
                    return;
                }
            }

            // Проверяем, включены ли уведомления у пользователя
            boolean notificationsEnabled = areNotificationsEnabled(context);

            if (!notificationsEnabled) {
                Log.d(TAG, "Notifications disabled by user in app settings");
                cancelAllReminders(context);
                return;
            }

            // Проверяем, включены ли уведомления на системном уровне
            if (!NotificationHelper.areNotificationsEnabled(context)) {
                Log.d(TAG, "System notifications disabled, skipping scheduling");
                return;
            }

            // Создаем ограничения
            Constraints constraints = new Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .setRequiresBatteryNotLow(true)
                    .build();

            // Создаем периодическую задачу (каждые 12 часов)
            PeriodicWorkRequest reminderWork = new PeriodicWorkRequest.Builder(
                    ReminderWorker.class,
                    12, // повторять каждые 12 часов
                    TimeUnit.HOURS
            )
                    .setConstraints(constraints)
                    .addTag(WORK_TAG)
                    .build();

            // Планируем задачу (обновляем если уже существует)
            WorkManager workManager = WorkManager.getInstance(context);
            workManager.enqueueUniquePeriodicWork(
                    WORK_TAG,
                    ExistingPeriodicWorkPolicy.UPDATE,
                    reminderWork
            );

            Log.d(TAG, "Reminder work scheduled successfully");

        } catch (Exception e) {
            Log.e(TAG, "Error scheduling reminder work: " + e.getMessage(), e);
        }
    }

    /**
     * Останавливает все запланированные уведомления
     */
    public static void cancelAllReminders(Context context) {
        try {
            WorkManager.getInstance(context).cancelAllWorkByTag(WORK_TAG);
            Log.d(TAG, "All reminders cancelled");
        } catch (Exception e) {
            Log.e(TAG, "Error cancelling reminders: " + e.getMessage(), e);
        }
    }

    /**
     * Включает/выключает уведомления
     */
    public static void setNotificationsEnabled(Context context, boolean enabled) {
        try {
            SharedPreferences prefs = getSharedPreferences(context);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled);
            editor.apply();

            Log.d(TAG, "Notifications " + (enabled ? "enabled" : "disabled") + " in preferences");

            if (enabled) {
                // время на сохранение в SharedPreferences
                new android.os.Handler().postDelayed(() -> {
                    scheduleReminderWork(context);
                }, 100);
            } else {
                cancelAllReminders(context);
            }

        } catch (Exception e) {
            Log.e(TAG, "Error setting notification preference: " + e.getMessage(), e);
        }
    }

    /**
     * Проверяет, включены ли уведомления
     */
    public static boolean areNotificationsEnabled(Context context) {
        try {
            SharedPreferences prefs = getSharedPreferences(context);
            boolean enabled = prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true); // по умолчанию включены
            Log.d(TAG, "Reading notification preference: " + enabled);
            return enabled;
        } catch (Exception e) {
            Log.e(TAG, "Error reading notification preference: " + e.getMessage(), e);
            return true; // по умолчанию включены
        }
    }

    /**
     * Проверяет, есть ли активные задачи напоминаний
     */
    public static boolean hasScheduledReminders(Context context) {
        try {
            return areNotificationsEnabled(context);
        } catch (Exception e) {
            Log.e(TAG, "Error checking scheduled reminders: " + e.getMessage(), e);
            return false;
        }
    }
}