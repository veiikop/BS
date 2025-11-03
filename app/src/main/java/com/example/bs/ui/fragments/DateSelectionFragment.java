package com.example.bs.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.bs.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Фрагмент для выбора даты записи на услугу.
 * Пользователь может выбрать дату с помощью MaterialDatePicker, но только начиная с сегодняшнего дня.
 */
public class DateSelectionFragment extends Fragment {

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

        // Получаем аргументы из бандла
        if (getArguments() != null) {
            serviceId = getArguments().getLong("service_id");
        }

        // Находим элементы UI
        TextView dateText = view.findViewById(R.id.text_selected_date);
        MaterialButton selectDateButton = view.findViewById(R.id.button_select_date);
        MaterialButton nextButton = view.findViewById(R.id.button_next);
        TextView errorText = view.findViewById(R.id.text_error);

        // Настраиваем ограничения для выбора даты: минимальная дата - текущая (сегодня разрешена)
        CalendarConstraints.Builder constraintsBuilder = new CalendarConstraints.Builder();
        constraintsBuilder.setValidator(DateValidatorPointForward.now());

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
            dateText.setText("Выбранная дата: " + selectedDate);
            errorText.setVisibility(View.GONE);
            nextButton.setEnabled(true);
        });

        // Обработчик клика на кнопку выбора даты
        selectDateButton.setOnClickListener(v -> datePicker.show(getParentFragmentManager(), "DATE_PICKER"));

        // Обработчик клика на кнопку "Далее" для перехода к выбору времени
        nextButton.setOnClickListener(v -> {
            if (selectedDate == null) {
                Toast.makeText(requireContext(), "Пожалуйста, выберите дату", Toast.LENGTH_SHORT).show();
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
        view.findViewById(R.id.button_back).setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());
    }

    /**
     * Проверяет, является ли дата прошедшей.
     * @param date Дата в формате yyyy-MM-dd
     * @return true, если дата в прошлом, false иначе
     */
    private boolean isDateInPast(String date) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date selectedDateObj = sdf.parse(date);
            Date today = new Date();

            Calendar cal1 = Calendar.getInstance();
            Calendar cal2 = Calendar.getInstance();
            cal1.setTime(selectedDateObj);
            cal2.setTime(today);

            return cal1.get(Calendar.YEAR) < cal2.get(Calendar.YEAR) ||
                    (cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                            cal1.get(Calendar.DAY_OF_YEAR) < cal2.get(Calendar.DAY_OF_YEAR));
        } catch (ParseException e) {
            e.printStackTrace();
            return true;
        }
    }
}