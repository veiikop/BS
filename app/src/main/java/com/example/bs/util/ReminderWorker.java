package com.example.bs.util;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.example.bs.db.AppointmentDao;
import com.example.bs.db.MasterDao;
import com.example.bs.db.ServiceDao;
import com.example.bs.model.Appointment;
import com.example.bs.model.Master;
import com.example.bs.model.Service;
import com.example.bs.util.NotificationHelper;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Worker для проверки и отправки напоминаний за день до записи
 */
public class ReminderWorker extends Worker {

    private static final String TAG = "ReminderWorker";

    public ReminderWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            Log.d(TAG, "ReminderWorker started");

            Context context = getApplicationContext();
            AppointmentDao appointmentDao = new AppointmentDao(context);

            // Получаем все будущие записи
            List<Appointment> futureAppointments = appointmentDao.getAppointmentsByStatus("future");

            if (futureAppointments.isEmpty()) {
                Log.d(TAG, "No future appointments found");
                return Result.success();
            }

            // Текущая дата
            Calendar currentCalendar = Calendar.getInstance();
            currentCalendar.setTime(new Date());

            // Дата для проверки (завтра)
            Calendar tomorrowCalendar = Calendar.getInstance();
            tomorrowCalendar.add(Calendar.DAY_OF_YEAR, 1);
            String tomorrowDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    .format(tomorrowCalendar.getTime());

            Log.d(TAG, "Checking appointments for tomorrow: " + tomorrowDate);

            ServiceDao serviceDao = new ServiceDao(context);
            MasterDao masterDao = new MasterDao(context);

            int remindersSent = 0;

            for (Appointment appointment : futureAppointments) {
                // Проверяем, что запись на завтра
                if (appointment.getDateTime().startsWith(tomorrowDate)) {
                    // Получаем детали записи
                    Service service = serviceDao.getServiceById(appointment.getServiceId());
                    Master master = masterDao.getMasterById(appointment.getMasterId());

                    if (service != null && master != null) {
                        // Отправляем уведомление
                        NotificationHelper.showReminderNotification(
                                context,
                                service.getName(),
                                appointment.getDateTime(),
                                master.getName() + " " + master.getSurname()
                        );

                        remindersSent++;
                        Log.d(TAG, "Reminder sent for appointment ID: " + appointment.getId());
                    }
                }
            }

            Log.d(TAG, "ReminderWorker finished. Sent " + remindersSent + " reminders");
            return Result.success();

        } catch (Exception e) {
            Log.e(TAG, "Error in ReminderWorker: " + e.getMessage(), e);
            return Result.failure();
        }
    }
}