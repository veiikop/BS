package com.example.bs.util;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Утилита для работы с датами, выходными и праздниками салона
 */
public class DateUtils {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private static final SimpleDateFormat DATETIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

    // Дни недели (1 = воскресенье, 2 = понедельник, ..., 7 = суббота)
    public static final int MONDAY = Calendar.MONDAY;

    // Важные праздники (фиксированные даты) - формат "MM-dd"
    private static final Set<String> FIXED_HOLIDAYS = new HashSet<>(Arrays.asList(
            "12-31", // 31 декабря
            "01-01", // 1 января
            "01-07", // 7 января (Рождество)
            "02-23", // 23 февраля
            "03-08", // 8 марта
            "05-01", // 1 мая
            "05-09", // 9 мая
            "06-12", // 12 июня
            "11-04"  // 4 ноября
    ));

    /**
     * Проверяет, является ли дата выходным днем салона
     * @param date Дата в формате "yyyy-MM-dd"
     * @return true если это выходной
     */
    public static boolean isSalonHoliday(String date) {
        try {
            Date parsedDate = DATE_FORMAT.parse(date);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(parsedDate);

            // 1. Проверяем понедельники (выходной)
            if (calendar.get(Calendar.DAY_OF_WEEK) == MONDAY) {
                return true;
            }

            // 2. Проверяем фиксированные праздники
            String monthDay = String.format(Locale.getDefault(), "%02d-%02d",
                    calendar.get(Calendar.MONTH) + 1,
                    calendar.get(Calendar.DAY_OF_MONTH));

            if (FIXED_HOLIDAYS.contains(monthDay)) {
                return true;
            }

            return false;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Проверяет, является ли дата и время в прошлом
     */
    public static boolean isDateTimeInPast(String dateTime) {
        try {
            Date selected = DATETIME_FORMAT.parse(dateTime);
            return selected.before(new Date());
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
    }

    /**
     * Проверяет, является ли дата в прошлом (без учета времени)
     */
    public static boolean isDateInPast(String date) {
        try {
            Date selected = DATE_FORMAT.parse(date);
            Date today = new Date();

            Calendar calSelected = Calendar.getInstance();
            Calendar calToday = Calendar.getInstance();
            calSelected.setTime(selected);
            calToday.setTime(today);

            // Обнуляем время для сравнения только дат
            calSelected.set(Calendar.HOUR_OF_DAY, 0);
            calSelected.set(Calendar.MINUTE, 0);
            calSelected.set(Calendar.SECOND, 0);
            calSelected.set(Calendar.MILLISECOND, 0);

            calToday.set(Calendar.HOUR_OF_DAY, 0);
            calToday.set(Calendar.MINUTE, 0);
            calToday.set(Calendar.SECOND, 0);
            calToday.set(Calendar.MILLISECOND, 0);

            return calSelected.before(calToday);
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
    }

    /**
     * Генерирует список доступных дат для записи на ближайшие N дней
     */
    public static List<String> generateAvailableDates(int daysAhead) {
        List<String> availableDates = new java.util.ArrayList<>();
        Calendar calendar = Calendar.getInstance();

        for (int i = 0; i < daysAhead; i++) {
            calendar.add(Calendar.DAY_OF_YEAR, i == 0 ? 0 : 1);

            String date = DATE_FORMAT.format(calendar.getTime());

            // Проверяем, что дата не в прошлом и не выходной
            if (!isDateInPast(date) && !isSalonHoliday(date)) {
                availableDates.add(date);
            }
        }

        return availableDates;
    }

    /**
     * Возвращает читабельное название дня недели
     */
    public static String getDayOfWeekName(String date) {
        try {
            Date parsedDate = DATE_FORMAT.parse(date);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(parsedDate);

            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
            String[] days = {"Воскресенье", "Понедельник", "Вторник", "Среда",
                    "Четверг", "Пятница", "Суббота"};

            return days[dayOfWeek - 1];
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Форматирует дату в читаемый вид
     */
    public static String formatDateForDisplay(String date) {
        try {
            Date parsedDate = DATE_FORMAT.parse(date);
            SimpleDateFormat displayFormat = new SimpleDateFormat("dd MMMM yyyy", new Locale("ru"));
            return displayFormat.format(parsedDate);
        } catch (Exception e) {
            return date;
        }
    }
}