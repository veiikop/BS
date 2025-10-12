package com.example.bs.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.bs.model.Appointment;
import com.example.bs.model.Master;

import java.util.ArrayList;
import java.util.List;

/**
 * DAO для операций с записями (паттерн Facade).
 */
public class AppointmentDao {
    private DBHelper dbHelper;
    private MasterDao masterDao; // Для доступа к мастерам

    public AppointmentDao(Context context) {
        dbHelper = DBHelper.getInstance(context);
        masterDao = new MasterDao(context); // Инициализация MasterDao
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
     * Получает список свободных мастеров для заданной даты, времени и категории услуги.
     * @param dateTime Дата и время (формат YYYY-MM-DD HH:MM)
     * @param categoryName Название категории услуги (e.g. "макияж")
     * @return Список свободных мастеров
     */
    public List<Master> getAvailableMasters(String dateTime, String categoryName) {
        List<Master> allMasters = masterDao.getMastersBySpecialty(categoryName);
        List<Master> availableMasters = new ArrayList<>();

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        for (Master master : allMasters) {
            // Проверяем, свободен ли слот для этого мастера
            Cursor cursor = db.query("appointments", null,
                    "date_time=? AND master_id=?",
                    new String[]{dateTime, String.valueOf(master.getId())},
                    null, null, null);
            boolean isAvailable = !cursor.moveToFirst();
            cursor.close();

            if (isAvailable) {
                availableMasters.add(master);
            }
        }

        return availableMasters;
    }

    /**
     * Проверяет доступность слота для мастера.
     * @param dateTime Дата и время
     * @param masterId ID мастера
     * @return true, если слот свободен
     */
    public boolean isSlotAvailable(String dateTime, long masterId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("appointments", null, "date_time=? AND master_id=?", new String[]{dateTime, String.valueOf(masterId)}, null, null, null);
        boolean available = !cursor.moveToFirst();
        cursor.close();
        return available;
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
}