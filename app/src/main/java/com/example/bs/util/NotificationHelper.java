package com.example.bs.util;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import com.example.bs.R;
import com.example.bs.ui.MainActivity;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Утилита для управления уведомлениями
 */
public class NotificationHelper {

    private static final String CHANNEL_ID = "beauty_salon_channel";
    private static final String CHANNEL_NAME = "Beauty Salon Notifications";
    private static final String CHANNEL_DESCRIPTION = "Уведомления о записях в салоне красоты";

    private static final int NOTIFICATION_ID_IMMEDIATE = 1001;
    private static final int NOTIFICATION_ID_REMINDER = 1002;

    /**
     * Создает канал уведомлений (обязательно для API 26+)
     */
    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription(CHANNEL_DESCRIPTION);
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{100, 200, 100, 200});
            channel.enableLights(true);
            channel.setLightColor(context.getColor(R.color.colorPrimary));

            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    /**
     * Проверяет разрешение на отправку уведомлений
     */
    private static boolean checkNotificationPermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(context,
                    Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;
        }
        // Для версий ниже Android 13 разрешение не требуется
        return true;
    }

    /**
     * Показывает уведомление о успешной записи
     */
    public static void showBookingConfirmationNotification(
            Context context,
            String serviceName,
            String dateTime,
            long appointmentId
    ) {
        try {
            // Проверяем разрешение
            if (!checkNotificationPermission(context)) {
                Log.w("NotificationHelper", "Notification permission not granted");
                return;
            }

            // Создаем Intent для открытия приложения при нажатии на уведомление
            Intent intent = new Intent(context, MainActivity.class);
            intent.putExtra("open_appointments", true);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            PendingIntent pendingIntent = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                pendingIntent = PendingIntent.getActivity(
                        context,
                        0,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                );
            } else {
                pendingIntent = PendingIntent.getActivity(
                        context,
                        0,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
            }

            // Форматируем дату для отображения
            String formattedDate = formatDateTimeForDisplay(dateTime);

            // Создаем уведомление
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_calendar_check)
                    .setColor(context.getColor(R.color.colorPrimary))
                    .setContentTitle("✅ Запись подтверждена!")
                    .setContentText("Вы записаны на " + serviceName)
                    .setStyle(new NotificationCompat.BigTextStyle()
                            .bigText("Вы успешно записаны на услугу:\n" +
                                    "• " + serviceName + "\n" +
                                    "• " + formattedDate + "\n\n" +
                                    "Нажмите, чтобы просмотреть детали записи."))
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .setVibrate(new long[]{0, 250, 250, 250})
                    .setOnlyAlertOnce(true);

            try {
                // Показываем уведомление с проверкой разрешения
                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
                if (ActivityCompat.checkSelfPermission(context,
                        Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    Log.w("NotificationHelper", "Notification permission not granted, skipping");
                    return;
                }
                notificationManager.notify(NOTIFICATION_ID_IMMEDIATE, builder.build());

                Log.d("NotificationHelper", "Booking confirmation notification shown");
            } catch (SecurityException e) {
                Log.e("NotificationHelper", "SecurityException when showing notification: " + e.getMessage());
            }

        } catch (Exception e) {
            Log.e("NotificationHelper", "Error showing notification: " + e.getMessage(), e);
        }
    }

    /**
     * Показывает уведомление-напоминание за день до записи
     */
    public static void showReminderNotification(
            Context context,
            String serviceName,
            String dateTime,
            String masterName
    ) {
        try {
            // Проверяем разрешение
            if (!checkNotificationPermission(context)) {
                Log.w("NotificationHelper", "Notification permission not granted for reminder");
                return;
            }

            Intent intent = new Intent(context, MainActivity.class);
            intent.putExtra("open_appointments", true);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            PendingIntent pendingIntent = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                pendingIntent = PendingIntent.getActivity(
                        context,
                        1,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                );
            } else {
                pendingIntent = PendingIntent.getActivity(
                        context,
                        1,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
            }

            String formattedDate = formatDateTimeForDisplay(dateTime);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_calendar)
                    .setContentTitle("⏰ Напоминание о записи")
                    .setContentText("Завтра у вас запись на " + serviceName)
                    .setStyle(new NotificationCompat.BigTextStyle()
                            .bigText("Не забудьте о вашей записи завтра:\n" +
                                    "• Услуга: " + serviceName + "\n" +
                                    "• Время: " + formattedDate + "\n" +
                                    "• Мастер: " + masterName + "\n\n" +
                                    "Рекомендуем прийти за 10-15 минут до начала."))
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .setVibrate(new long[]{0, 500, 250, 500})
                    .setOnlyAlertOnce(true);

            try {
                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
                if (ActivityCompat.checkSelfPermission(context,
                        Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    Log.w("NotificationHelper", "Notification permission not granted for reminder, skipping");
                    return;
                }
                notificationManager.notify(NOTIFICATION_ID_REMINDER, builder.build());

                Log.d("NotificationHelper", "Reminder notification shown");

            } catch (SecurityException e) {
                Log.e("NotificationHelper", "SecurityException when showing reminder: " + e.getMessage());
            }

        } catch (Exception e) {
            Log.e("NotificationHelper", "Error showing reminder: " + e.getMessage(), e);
        }
    }

    /**
     * Форматирует дату и время для отображения
     */
    private static String formatDateTimeForDisplay(String dateTime) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMMM yyyy, HH:mm", new Locale("ru"));

            Date date = inputFormat.parse(dateTime);
            if (date != null) {
                return outputFormat.format(date);
            }
        } catch (Exception e) {
            Log.e("NotificationHelper", "Error formatting date: " + e.getMessage());
        }
        return dateTime;
    }

    /**
     * Проверяет, включены ли уведомления в системе
     */
    public static boolean areNotificationsEnabled(Context context) {
        try {
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            return notificationManager.areNotificationsEnabled();
        } catch (Exception e) {
            Log.e("NotificationHelper", "Error checking notification status: " + e.getMessage());
            return false;
        }
    }

    /**
     * Открывает настройки уведомлений приложения
     */
    public static void openNotificationSettings(Context context) {
        try {
            Intent intent = new Intent();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                intent.setAction(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                intent.putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, context.getPackageName());
            } else {
                intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
                intent.putExtra("app_package", context.getPackageName());
                intent.putExtra("app_uid", context.getApplicationInfo().uid);
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception e) {
            Log.e("NotificationHelper", "Error opening notification settings: " + e.getMessage());
            // Открываем общие настройки в случае ошибки
            try {
                Intent intent = new Intent(android.provider.Settings.ACTION_SETTINGS);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            } catch (Exception ex) {
                Log.e("NotificationHelper", "Error opening general settings: " + ex.getMessage());
            }
        }
    }
}