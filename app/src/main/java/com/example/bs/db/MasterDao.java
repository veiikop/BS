package com.example.bs.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.bs.model.Master;

import java.util.ArrayList;
import java.util.List;

/**
 * DAO для операций с мастерами (паттерн Facade).
 */
public class MasterDao {
    private DBHelper dbHelper;

    public MasterDao(Context context) {
        dbHelper = DBHelper.getInstance(context);
    }

    /**
     * Добавляет нового мастера.
     * @param master Объект Master
     * @return ID вставленной записи
     */
    public long insertMaster(Master master) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", master.getName());
        values.put("surname", master.getSurname());
        values.put("specialty", master.getSpecialty());
        return db.insert("masters", null, values);
    }

    /**
     * Получает мастера по ID.
     * @param id ID мастера
     * @return Master или null
     */
    public Master getMasterById(long id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("masters", null, "id=?", new String[]{String.valueOf(id)}, null, null, null);
        Master master = null;
        if (cursor.moveToFirst()) {
            master = new Master(
                    cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                    cursor.getString(cursor.getColumnIndexOrThrow("name")),
                    cursor.getString(cursor.getColumnIndexOrThrow("surname")),
                    cursor.getString(cursor.getColumnIndexOrThrow("specialty"))
            );
        }
        cursor.close();
        return master;
    }

    /**
     * Получает мастеров по специализации.
     * @param specialty Специализация (имя категории)
     * @return Список мастеров
     */
    public List<Master> getMastersBySpecialty(String specialty) {
        List<Master> masters = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("masters", null, "specialty=?", new String[]{specialty}, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                Master master = new Master(
                        cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("name")),
                        cursor.getString(cursor.getColumnIndexOrThrow("surname")),
                        cursor.getString(cursor.getColumnIndexOrThrow("specialty"))
                );
                masters.add(master);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return masters;
    }

    /**
     * Получает всех мастеров.
     * @return Список мастеров
     */
    public List<Master> getAllMasters() {
        List<Master> masters = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("masters", null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                Master master = new Master(
                        cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("name")),
                        cursor.getString(cursor.getColumnIndexOrThrow("surname")),
                        cursor.getString(cursor.getColumnIndexOrThrow("specialty"))
                );
                masters.add(master);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return masters;
    }

    /**
     * Обновляет мастера.
     * @param master Объект Master
     * @return Количество обновленных строк
     */
    public int updateMaster(Master master) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", master.getName());
        values.put("surname", master.getSurname());
        values.put("specialty", master.getSpecialty());
        return db.update("masters", values, "id=?", new String[]{String.valueOf(master.getId())});
    }

    /**
     * Удаляет мастера.
     * @param id ID мастера
     * @return Количество удаленных строк
     */
    public int deleteMaster(long id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete("masters", "id=?", new String[]{String.valueOf(id)});
    }
}