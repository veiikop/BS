package com.example.bs.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.example.bs.model.Appointment;
import com.example.bs.model.Master;
import com.example.bs.model.Service;
import com.example.bs.model.Category;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * DAO для операций с записями (паттерн Facade).
 */
public class AppointmentDao {
    private DBHelper dbHelper;
    private Context context;
    private MasterDao masterDao;
    private ServiceDao serviceDao;
    private CategoryDao categoryDao;

    public AppointmentDao(Context context) {
        this.context = context;
        dbHelper = DBHelper.getInstance(context);
        masterDao = new MasterDao(context);
        serviceDao = new ServiceDao(context);
        categoryDao = new CategoryDao(context);
    }

    /**
     * Создает новую запись.
     * @param appointment Объект Appointment
     * @return ID вставленной записи
     */
    public long insertAppointment(Appointment appointment) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("user_id", appointment.getUserId());
        values.put("service_id", appointment.getServiceId());
        values.put("master_id", appointment.getMasterId());
        values.put("date_time", appointment.getDateTime());
        values.put("price", appointment.getPrice());
        values.put("status", appointment.getStatus());
        return db.insert("appointments", null, values);
    }

    /**
     * Получает запись по ID.
     * @param id ID записи
     * @return Appointment или null
     */
    public Appointment getAppointmentById(long id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("appointments", null, "id=?", new String[]{String.valueOf(id)}, null, null, null);
        Appointment appointment = null;
        if (cursor.moveToFirst()) {
            appointment = new Appointment(
                    cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                    cursor.getLong(cursor.getColumnIndexOrThrow("user_id")),
                    cursor.getLong(cursor.getColumnIndexOrThrow("service_id")),
                    cursor.getLong(cursor.getColumnIndexOrThrow("master_id")),
                    cursor.getString(cursor.getColumnIndexOrThrow("date_time")),
                    cursor.getDouble(cursor.getColumnIndexOrThrow("price")),
                    cursor.getString(cursor.getColumnIndexOrThrow("status"))
            );
        }
        cursor.close();
        return appointment;
    }

    /**
     * Получает все записи пользователя с сортировкой по дате.
     * @param userId ID пользователя
     * @return Список записей
     */
    public List<Appointment> getAppointmentsByUserId(long userId) {
        List<Appointment> appointments = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("appointments", null, "user_id=?", new String[]{String.valueOf(userId)}, null, null, "date_time DESC");
        if (cursor.moveToFirst()) {
            do {
                Appointment appointment = new Appointment(
                        cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                        cursor.getLong(cursor.getColumnIndexOrThrow("user_id")),
                        cursor.getLong(cursor.getColumnIndexOrThrow("service_id")),
                        cursor.getLong(cursor.getColumnIndexOrThrow("master_id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("date_time")),
                        cursor.getDouble(cursor.getColumnIndexOrThrow("price")),
                        cursor.getString(cursor.getColumnIndexOrThrow("status"))
                );
                appointments.add(appointment);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return appointments;
    }

    /**
     * Получает список свободных мастеров для заданной даты, времени и услуги.
     * @param dateTime Дата и время начала (формат YYYY-MM-DD HH:MM)
     * @param serviceId ID услуги
     * @return Список свободных мастеров
     */
    public List<Master> getAvailableMasters(String dateTime, long serviceId) {
        // 1. Получить услугу для определения категории и продолжительности
        Service service = serviceDao.getServiceById(serviceId);
        if (service == null) return new ArrayList<>();

        // 2. Получить категорию услуги
        Category category = categoryDao.getCategoryById(service.getCategoryId());
        if (category == null) return new ArrayList<>();

        // 3. Получить всех мастеров этой специализации
        List<Master> allMasters = masterDao.getMastersBySpecialty(category.getName());

        // 4. Отфильтровать занятых
        List<Master> availableMasters = new ArrayList<>();
        for (Master master : allMasters) {
            if (isSlotAvailable(dateTime, serviceId, master.getId())) {
                availableMasters.add(master);
            }
        }

        return availableMasters;
    }

    /**
     * Проверяет доступность мастера в заданный интервал времени.
     * @param startDateTime Начало интервала (формат YYYY-MM-DD HH:MM)
     * @param serviceId ID услуги (для определения длительности)
     * @param masterId ID мастера
     * @return true, если слот свободен
     */
    public boolean isSlotAvailable(String startDateTime, long serviceId, long masterId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // 1. Получить продолжительность услуги
        Service service = serviceDao.getServiceById(serviceId);
        if (service == null) return false;

        int duration = service.getDuration();

        // 2. Рассчитать конец интервала
        String endDateTime = calculateEndTime(startDateTime, duration);
        if (endDateTime == null) return false;

        // 3. Проверить, есть ли пересекающиеся записи у мастера
        // Исправленный запрос - получаем продолжительность через JOIN
        String query = "SELECT a.* FROM appointments a " +
                "JOIN services s ON a.service_id = s.id " +
                "WHERE a.master_id = ? " +
                "AND a.status != 'cancelled' " +
                "AND ( " +
                "  (a.date_time BETWEEN ? AND ?) " +
                "  OR (datetime(a.date_time, '+' || s.duration || ' minutes') BETWEEN ? AND ?) " +
                "  OR (? BETWEEN a.date_time AND datetime(a.date_time, '+' || s.duration || ' minutes')) " +
                "  OR (? BETWEEN a.date_time AND datetime(a.date_time, '+' || s.duration || ' minutes')) " +
                ")";

        Cursor cursor = db.rawQuery(query, new String[]{
                String.valueOf(masterId),
                startDateTime, endDateTime,
                startDateTime, endDateTime,
                startDateTime,
                endDateTime
        });

        boolean isAvailable = !cursor.moveToFirst();
        cursor.close();
        return isAvailable;
    }

    /**
     * Рассчитывает время окончания услуги.
     * @param startDateTime Начальное время (YYYY-MM-DD HH:MM)
     * @param duration Продолжительность в минутах
     * @return Время окончания (YYYY-MM-DD HH:MM) или null при ошибке
     */
    private String calculateEndTime(String startDateTime, int duration) {
        if (TextUtils.isEmpty(startDateTime)) return null;

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            Date date = sdf.parse(startDateTime);
            if (date == null) return null;

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.add(Calendar.MINUTE, duration);
            return sdf.format(calendar.getTime());
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Получает доступные временные слоты для даты и услуги.
     * @param date Дата в формате YYYY-MM-DD
     * @param serviceId ID услуги
     * @return Список доступных времен в формате HH:MM
     */
    public List<String> getAvailableTimeSlots(String date, long serviceId) {
        List<String> availableSlots = new ArrayList<>();

        // Проверяем, что дата не в прошлом
        if (isDateInPast(date)) {
            return availableSlots;
        }

        // Время работы салона
        int startHour = 9; // 9:00
        int endHour = 21;  // 21:00
        int interval = 30; // Интервал 30 минут

        // Получить продолжительность услуги
        Service service = serviceDao.getServiceById(serviceId);
        if (service == null) return availableSlots;

        int duration = service.getDuration();

        // Проверить каждый временной слот
        for (int hour = startHour; hour < endHour; hour++) {
            for (int minute = 0; minute < 60; minute += interval) {
                String time = String.format(Locale.getDefault(), "%02d:%02d", hour, minute);
                String dateTime = date + " " + time;

                // Проверить, что время не в прошлом (для сегодняшней даты)
                if (!isDateTimeInPast(dateTime)) {
                    // Проверить, есть ли хотя бы один свободный мастер на этот слот
                    List<Master> availableMasters = getAvailableMasters(dateTime, serviceId);
                    if (!availableMasters.isEmpty()) {
                        availableSlots.add(time);
                    }
                }
            }
        }

        return availableSlots;
    }

    /**
     * Проверяет, является ли дата прошедшей.
     * @param date Дата в формате YYYY-MM-DD
     * @return true, если дата в прошлом
     */
    private boolean isDateInPast(String date) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date selectedDate = sdf.parse(date);
            Date today = new Date();

            // Сравниваем даты без времени
            Calendar cal1 = Calendar.getInstance();
            Calendar cal2 = Calendar.getInstance();
            cal1.setTime(selectedDate);
            cal2.setTime(today);
            cal2.set(Calendar.HOUR_OF_DAY, 0);
            cal2.set(Calendar.MINUTE, 0);
            cal2.set(Calendar.SECOND, 0);
            cal2.set(Calendar.MILLISECOND, 0);

            return cal1.before(cal2);
        } catch (ParseException e) {
            e.printStackTrace();
            return true;
        }
    }

    /**
     * Проверяет, является ли дата и время прошедшими.
     * @param dateTime Дата и время в формате YYYY-MM-DD HH:MM
     * @return true, если дата и время в прошлом
     */
    private boolean isDateTimeInPast(String dateTime) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            Date selectedDateTime = sdf.parse(dateTime);
            return selectedDateTime.before(new Date());
        } catch (ParseException e) {
            e.printStackTrace();
            return true;
        }
    }

    /**
     * Обновляет статус записи.
     * @param id ID записи
     * @param status Новый статус
     * @return Количество обновленных строк
     */
    public int updateAppointmentStatus(long id, String status) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("status", status);
        return db.update("appointments", values, "id=?", new String[]{String.valueOf(id)});
    }

    /**
     * Удаляет запись.
     * @param id ID записи
     * @return Количество удаленных строк
     */
    public int deleteAppointment(long id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete("appointments", "id=?", new String[]{String.valueOf(id)});
    }

    /**
     * Получает все записи.
     * @return Список записей
     */
    public List<Appointment> getAllAppointments() {
        List<Appointment> appointments = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("appointments", null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                Appointment appointment = new Appointment(
                        cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                        cursor.getLong(cursor.getColumnIndexOrThrow("user_id")),
                        cursor.getLong(cursor.getColumnIndexOrThrow("service_id")),
                        cursor.getLong(cursor.getColumnIndexOrThrow("master_id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("date_time")),
                        cursor.getDouble(cursor.getColumnIndexOrThrow("price")),
                        cursor.getString(cursor.getColumnIndexOrThrow("status"))
                );
                appointments.add(appointment);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return appointments;
    }

    /**
     * Получает записи по ID мастера.
     * @param masterId ID мастера
     * @return Список записей
     */
    public List<Appointment> getAppointmentsByMasterId(long masterId) {
        List<Appointment> appointments = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("appointments", null, "master_id=?", new String[]{String.valueOf(masterId)}, null, null, "date_time ASC");
        if (cursor.moveToFirst()) {
            do {
                Appointment appointment = new Appointment(
                        cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                        cursor.getLong(cursor.getColumnIndexOrThrow("user_id")),
                        cursor.getLong(cursor.getColumnIndexOrThrow("service_id")),
                        cursor.getLong(cursor.getColumnIndexOrThrow("master_id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("date_time")),
                        cursor.getDouble(cursor.getColumnIndexOrThrow("price")),
                        cursor.getString(cursor.getColumnIndexOrThrow("status"))
                );
                appointments.add(appointment);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return appointments;
    }

    /**
     * Получает записи по статусу.
     * @param status Статус записи
     * @return Список записей
     */
    public List<Appointment> getAppointmentsByStatus(String status) {
        List<Appointment> appointments = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("appointments", null, "status=?", new String[]{status}, null, null, "date_time ASC");
        if (cursor.moveToFirst()) {
            do {
                Appointment appointment = new Appointment(
                        cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                        cursor.getLong(cursor.getColumnIndexOrThrow("user_id")),
                        cursor.getLong(cursor.getColumnIndexOrThrow("service_id")),
                        cursor.getLong(cursor.getColumnIndexOrThrow("master_id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("date_time")),
                        cursor.getDouble(cursor.getColumnIndexOrThrow("price")),
                        cursor.getString(cursor.getColumnIndexOrThrow("status"))
                );
                appointments.add(appointment);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return appointments;
    }

    /**
     * Проверяет, существует ли запись с заданными параметрами.
     * @param userId ID пользователя
     * @param serviceId ID услуги
     * @param masterId ID мастера
     * @param dateTime Дата и время
     * @return true, если запись существует, false иначе
     */
    public boolean appointmentExists(long userId, long serviceId, long masterId, String dateTime) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("appointments", new String[]{"id"},
                "user_id=? AND service_id=? AND master_id=? AND date_time=?",
                new String[]{String.valueOf(userId), String.valueOf(serviceId), String.valueOf(masterId), dateTime},
                null, null, null);
        boolean exists = cursor.moveToFirst();
        cursor.close();
        return exists;
    }
    /**
     * Проверяет, есть ли у пользователя запись на указанное время (независимо от услуги/мастера).
     * @param userId ID пользователя
     * @param dateTime Дата и время (yyyy-MM-dd HH:mm)
     * @return true, если уже есть запись
     */
    public boolean hasAppointmentAtTime(long userId, String dateTime) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                "appointments",
                new String[]{"id"},
                "user_id = ? AND date_time = ?",
                new String[]{String.valueOf(userId), dateTime},
                null, null, null
        );
        boolean exists = cursor.moveToFirst();
        cursor.close();
        return exists;
    }
}