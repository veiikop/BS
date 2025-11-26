package com.example.bs.util;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import java.util.regex.Pattern;

/**
 * Утилита для валидации и форматирования телефонных номеров
 */
public class PhoneValidator {

    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+7\\s?\\(?\\d{3}\\)?\\s?\\d{3}-?\\d{2}-?\\d{2}$");
    private static final String PHONE_MASK = "+7 (XXX) XXX-XX-XX";

    /**
     * Проверяет валидность телефонного номера
     * @param phone Номер телефона
     * @return true если номер валиден, false в противном случае
     */
    public static boolean isValidPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }
        return PHONE_PATTERN.matcher(phone.trim()).matches();
    }

    /**
     * Форматирует номер телефона по маске
     * @param phone Неформатированный номер
     * @return Отформатированный номер
     */
    public static String formatPhone(String phone) {
        if (phone == null) return "";

        // Удаляем все нецифровые символы, кроме +
        String digits = phone.replaceAll("[^\\d+]", "");

        // Если номер начинается с 7 или 8, заменяем на +7
        if (digits.startsWith("7") && digits.length() == 11) {
            digits = "+7" + digits.substring(1);
        } else if (digits.startsWith("8") && digits.length() == 11) {
            digits = "+7" + digits.substring(1);
        } else if (!digits.startsWith("+7") && digits.length() == 10) {
            digits = "+7" + digits;
        }

        // Форматируем по маске
        if (digits.startsWith("+7") && digits.length() == 12) {
            String number = digits.substring(2);
            return String.format("+7 (%s) %s-%s-%s",
                    number.substring(0, 3),
                    number.substring(3, 6),
                    number.substring(6, 8),
                    number.substring(8, 10));
        }

        return phone; // Возвращаем как есть, если не соответствует формату
    }

    /**
     * Создает TextWatcher для автоматического форматирования номера телефона
     * @param editText Поле ввода телефона
     * @return TextWatcher для установки в поле ввода
     */
    public static TextWatcher createPhoneTextWatcher(EditText editText) {
        return new TextWatcher() {
            private boolean isFormatting = false;
            private String previousText = "";

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                previousText = s.toString();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Пустая реализация
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (isFormatting) return;

                isFormatting = true;

                String currentText = s.toString();

                // Если текст был удален, не форматируем
                if (currentText.length() < previousText.length()) {
                    isFormatting = false;
                    return;
                }

                // Форматируем только если введены цифры
                if (!currentText.isEmpty() && Character.isDigit(currentText.charAt(currentText.length() - 1))) {
                    String formatted = formatPhone(currentText);
                    if (!formatted.equals(currentText)) {
                        editText.setText(formatted);
                        editText.setSelection(formatted.length());
                    }
                }

                isFormatting = false;
            }
        };
    }
}