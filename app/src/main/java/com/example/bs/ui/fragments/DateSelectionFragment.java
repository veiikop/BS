package com.example.bs.ui.fragments;

import android.os.Bundle;
import android.os.Parcel;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import com.example.bs.R;
import com.example.bs.util.DateUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.MaterialDatePicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Фрагмент для выбора даты записи на услугу.
 * Пользователь может выбрать дату с помощью MaterialDatePicker,
 * но только начиная с сегодняшнего дня и исключая выходные и праздники.
 */
public class DateSelectionFragment extends BaseFragment {

    private long serviceId; // ID выбранной услуги
    private String selectedDate; // Выбранная дата в формате yyyy-MM-dd

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Инфлейтим разметку фрагмента
        return inflater.inflate(R.layout.fragment_date_selection, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Проверяем авторизацию
        if (!isUserLoggedIn) {
            Toast.makeText(requireContext(), "Пожалуйста, войдите в систему для записи", Toast.LENGTH_SHORT).show();
            requireActivity().getSupportFragmentManager().popBackStack();
            return;
        }

        // Получаем аргументы из бандла
        if (getArguments() != null) {
            serviceId = getArguments().getLong("service_id");
        }

        // Находим элементы UI
        TextView dateText = view.findViewById(R.id.text_selected_date);
        MaterialButton selectDateButton = view.findViewById(R.id.button_select_date);
        MaterialButton nextButton = view.findViewById(R.id.button_next);
        TextView errorText = view.findViewById(R.id.text_error);

        // Информация о выходных днях
        TextView holidayInfoText = view.findViewById(R.id.text_holiday_info);
        if (holidayInfoText != null) {
            holidayInfoText.setText("⚠️ Выходные дни: понедельники и праздники");
        }

        // Изначально кнопка "Далее" неактивна
        nextButton.setEnabled(false);

        // Настраиваем календарь с валидатором, который запрещает выбор выходных дней
        CalendarConstraints.Builder constraintsBuilder = new CalendarConstraints.Builder();

        // Создаем кастомный валидатор для запрета выходных дней
        CalendarConstraints.DateValidator dateValidator = new CalendarConstraints.DateValidator() {
            @Override
            public boolean isValid(long dateInMillis) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(dateInMillis);

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                String dateString = sdf.format(calendar.getTime());

                // Проверяем несколько условий:
                // 1. Дата не в прошлом
                if (DateUtils.isDateInPast(dateString)) {
                    return false;
                }

                // 2. Дата не является выходным днем салона
                if (DateUtils.isSalonHoliday(dateString)) {
                    return false;
                }

                return true;
            }

            @Override
            public int describeContents() {
                return 0;
            }

            @Override
            public void writeToParcel(Parcel dest, int flags) {
                // Реализация для Parcelable
            }
        };

        // Устанавливаем минимальную дату - сегодня
        Calendar minDate = Calendar.getInstance();
        minDate.set(Calendar.HOUR_OF_DAY, 0);
        minDate.set(Calendar.MINUTE, 0);
        minDate.set(Calendar.SECOND, 0);
        minDate.set(Calendar.MILLISECOND, 0);

        // Устанавливаем максимальную дату - 3 месяца вперед
        Calendar maxDate = Calendar.getInstance();
        maxDate.add(Calendar.MONTH, 3);

        constraintsBuilder.setStart(minDate.getTimeInMillis());
        constraintsBuilder.setEnd(maxDate.getTimeInMillis());
        constraintsBuilder.setValidator(dateValidator);

        // Создаем MaterialDatePicker с ограничениями
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Выберите дату")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .setCalendarConstraints(constraintsBuilder.build())
                .setInputMode(MaterialDatePicker.INPUT_MODE_CALENDAR)
                .build();

        // Обработчик положительного выбора даты
        datePicker.addOnPositiveButtonClickListener(selection -> {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            selectedDate = sdf.format(new Date(selection));

            // Получаем информацию о дне недели
            String dayOfWeek = DateUtils.getDayOfWeekName(selectedDate);
            String formattedDate = DateUtils.formatDateForDisplay(selectedDate);

            // Отображаем выбранную дату с днем недели
            dateText.setText(String.format("Выбранная дата: %s (%s)", formattedDate, dayOfWeek));
            errorText.setVisibility(View.GONE);
            nextButton.setEnabled(true);

            // Показываем предупреждение о празднике (если это не понедельник)
            if (!dayOfWeek.equals("Понедельник") && DateUtils.isSalonHoliday(selectedDate)) {
                showHolidayWarning(selectedDate);
            }
        });

        // Обработчик отмены выбора даты
        datePicker.addOnNegativeButtonClickListener(dialog -> {
            // Ничего не делаем при отмене
        });

        // Обработчик ошибки выбора даты
        datePicker.addOnCancelListener(dialog -> {
            // Ничего не делаем при отмене
        });

        // Обработчик клика на кнопку выбора даты
        selectDateButton.setOnClickListener(v -> {
            // Проверяем, доступен ли сегодняшний день для записи
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String today = sdf.format(new Date());

            if (DateUtils.isSalonHoliday(today)) {
                String dayOfWeek = DateUtils.getDayOfWeekName(today);
                new AlertDialog.Builder(requireContext())
                        .setTitle("Сегодня выходной")
                        .setMessage("Сегодня " + dayOfWeek.toLowerCase() +
                                " - выходной день в салоне. Пожалуйста, выберите другую дату.")
                        .setPositiveButton("OK", (dialog, which) -> {
                            datePicker.show(getParentFragmentManager(), "DATE_PICKER");
                        })
                        .show();
            } else {
                datePicker.show(getParentFragmentManager(), "DATE_PICKER");
            }
        });

        // Обработчик клика на кнопку "Далее" для перехода к выбору времени
        nextButton.setOnClickListener(v -> {
            if (selectedDate == null) {
                Toast.makeText(requireContext(), "Пожалуйста, выберите дату", Toast.LENGTH_SHORT).show();
                return;
            }

            // Проверяем, не является ли выбранная дата выходным (на всякий случай)
            if (DateUtils.isSalonHoliday(selectedDate)) {
                String dayOfWeek = DateUtils.getDayOfWeekName(selectedDate);
                new AlertDialog.Builder(requireContext())
                        .setTitle("Выбран выходной день")
                        .setMessage("Вы выбрали " + dayOfWeek.toLowerCase() +
                                " - выходной день. Пожалуйста, выберите другую дату.")
                        .setPositiveButton("OK", null)
                        .show();
                return;
            }

            // Переход к выбору времени
            TimeSelectionFragment timeFragment = new TimeSelectionFragment();
            Bundle args = new Bundle();
            args.putLong("service_id", serviceId);
            args.putString("selected_date", selectedDate);
            timeFragment.setArguments(args);

            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, timeFragment)
                    .addToBackStack(null)
                    .commit();
        });

        // Обработчик кнопки "Назад"
        view.findViewById(R.id.button_back).setOnClickListener(v -> {
            // Возвращаемся на предыдущий экран
            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                getParentFragmentManager().popBackStack();
            } else {
                // Если в стеке ничего нет, возвращаемся к каталогу
                requireActivity().getSupportFragmentManager().popBackStack("catalog", 0);
            }
        });
    }

    /**
     * Показывает предупреждение о праздничном дне
     * @param date Выбранная дата в формате yyyy-MM-dd
     */
    private void showHolidayWarning(String date) {
        String message;
        String holidayName = "";

        // Определяем название праздника по дате
        if (date.contains("-12-31")) {
            holidayName = "31 декабря - Новый год";
            message = "31 декабря - праздничный день. Салоны работают по специальному графику с 10:00 до 18:00.";
        } else if (date.contains("-01-07")) {
            holidayName = "7 января - Рождество";
            message = "7 января - Рождество Христово. Салоны работают по сокращенному графику.";
        } else if (date.contains("-01-01")) {
            holidayName = "1 января - Новый год";
            message = "1 января - Новый год. Салоны не работают.";
        } else if (date.contains("-02-23")) {
            holidayName = "23 февраля - День защитника Отечества";
            message = "23 февраля - праздничный день. Салоны работают по стандартному графику.";
        } else if (date.contains("-03-08")) {
            holidayName = "8 марта - Международный женский день";
            message = "8 марта - праздничный день. Рекомендуем записываться заранее.";
        } else if (date.contains("-05-01")) {
            holidayName = "1 мая - Праздник весны и труда";
            message = "1 мая - праздничный день.";
        } else if (date.contains("-05-09")) {
            holidayName = "9 мая - День Победы";
            message = "9 мая - День Победы. Салоны работают по особому графику.";
        } else if (date.contains("-06-12")) {
            holidayName = "12 июня - День России";
            message = "12 июня - День России.";
        } else if (date.contains("-11-04")) {
            holidayName = "4 ноября - День народного единства";
            message = "4 ноября - День народного единства.";
        } else {
            holidayName = "Праздничный день";
            message = "В этот день салон работает по праздничному расписанию.";
        }

        new AlertDialog.Builder(requireContext())
                .setTitle(holidayName)
                .setMessage(message + "\n\nВы уверены, что хотите продолжить с этой датой?")
                .setPositiveButton("Да, продолжить", null)
                .setNegativeButton("Выбрать другую дату", (dialog, which) -> {
                    // Сбрасываем выбранную дату
                    selectedDate = null;
                    TextView dateText = requireView().findViewById(R.id.text_selected_date);
                    dateText.setText("Дата не выбрана");
                    MaterialButton nextButton = requireView().findViewById(R.id.button_next);
                    nextButton.setEnabled(false);

                    // Снова показываем выбор даты
                    requireView().findViewById(R.id.button_select_date).performClick();
                })
                .show();
    }

    /**
     * Проверяет, является ли дата доступной для записи
     * @param date Дата в формате yyyy-MM-dd
     * @return true, если дата доступна для записи
     */
    private boolean isDateAvailableForBooking(String date) {
        // Проверяем, не является ли дата в прошлом
        if (DateUtils.isDateInPast(date)) {
            return false;
        }

        // Проверяем, не является ли дата выходным днем салона
        if (DateUtils.isSalonHoliday(date)) {
            return false;
        }

        return true;
    }

    @Override
    public void onResume() {
        super.onResume();

        // При возобновлении фрагмента проверяем авторизацию
        if (!isUserLoggedIn) {
            Toast.makeText(requireContext(), "Сессия истекла, войдите заново", Toast.LENGTH_SHORT).show();
            requireActivity().getSupportFragmentManager().popBackStack();
            return;
        }

        // Проверяем, не истекла ли выбранная дата
        if (selectedDate != null) {
            if (!isDateAvailableForBooking(selectedDate)) {
                // Сбрасываем выбор, если дата стала недоступной
                selectedDate = null;
                TextView dateText = requireView().findViewById(R.id.text_selected_date);
                if (dateText != null) {
                    dateText.setText("Дата не выбрана (предыдущая дата стала недоступной)");
                }
                MaterialButton nextButton = requireView().findViewById(R.id.button_next);
                if (nextButton != null) {
                    nextButton.setEnabled(false);
                }

                Toast.makeText(requireContext(),
                        "Выбранная ранее дата больше недоступна для записи",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
}