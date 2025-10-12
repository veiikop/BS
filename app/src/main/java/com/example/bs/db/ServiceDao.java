package com.example.bs.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.bs.model.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * DAO для операций с услугами (паттерн Facade).
 */
public class ServiceDao {
    private DBHelper dbHelper;

    public ServiceDao(Context context) {
        dbHelper = DBHelper.getInstance(context);
    }

    /**
     * Добавляет новую услугу.
     * @param service Объект Service
     * @return ID вставленной записи
     */
    public long insertService(Service service) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", service.getName());
        values.put("category_id", service.getCategoryId());
        values.put("price", service.getPrice());
        values.put("duration", service.getDuration());
        return db.insert("services", null, values);
    }

    /**
     * Получает услугу по ID.
     * @param id ID услуги
     * @return Service или null
     */
    public Service getServiceById(long id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("services", null, "id=?", new String[]{String.valueOf(id)}, null, null, null);
        Service service = null;
        if (cursor.moveToFirst()) {
            service = new Service(
                    cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                    cursor.getString(cursor.getColumnIndexOrThrow("name")),
                    cursor.getLong(cursor.getColumnIndexOrThrow("category_id")),
                    cursor.getDouble(cursor.getColumnIndexOrThrow("price")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("duration"))
            );
        }
        cursor.close();
        return service;
    }

    /**
     * Получает услуги по ID категории.
     * @param categoryId ID категории
     * @return Список услуг
     */
    public List<Service> getServicesByCategoryId(long categoryId) {
        List<Service> services = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("services", null, "category_id=?", new String[]{String.valueOf(categoryId)}, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                Service service = new Service(
                        cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("name")),
                        cursor.getLong(cursor.getColumnIndexOrThrow("category_id")),
                        cursor.getDouble(cursor.getColumnIndexOrThrow("price")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("duration"))
                );
                services.add(service);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return services;
    }

    /**
     * Получает услуги в заданном диапазоне цен.
     * @param minPrice Минимальная цена
     * @param maxPrice Максимальная цена
     * @return Список услуг
     */
    public List<Service> getServicesByPriceRange(double minPrice, double maxPrice) {
        List<Service> services = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("services", null, "price BETWEEN ? AND ?", new String[]{String.valueOf(minPrice), String.valueOf(maxPrice)}, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                Service service = new Service(
                        cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("name")),
                        cursor.getLong(cursor.getColumnIndexOrThrow("category_id")),
                        cursor.getDouble(cursor.getColumnIndexOrThrow("price")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("duration"))
                );
                services.add(service);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return services;
    }

    /**
     * Получает все услуги.
     * @return Список услуг
     */
    public List<Service> getAllServices() {
        List<Service> services = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("services", null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                Service service = new Service(
                        cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("name")),
                        cursor.getLong(cursor.getColumnIndexOrThrow("category_id")),
                        cursor.getDouble(cursor.getColumnIndexOrThrow("price")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("duration"))
                );
                services.add(service);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return services;
    }

    /**
     * Обновляет услугу.
     * @param service Объект Service
     * @return Количество обновленных строк
     */
    public int updateService(Service service) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", service.getName());
        values.put("category_id", service.getCategoryId());
        values.put("price", service.getPrice());
        values.put("duration", service.getDuration());
        return db.update("services", values, "id=?", new String[]{String.valueOf(service.getId())});
    }

    /**
     * Удаляет услугу.
     * @param id ID услуги
     * @return Количество удаленных строк
     */
    public int deleteService(long id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete("services", "id=?", new String[]{String.valueOf(id)});
    }
}