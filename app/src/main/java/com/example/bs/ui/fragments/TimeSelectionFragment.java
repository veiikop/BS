package com.example.bs.ui.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.gridlayout.widget.GridLayout;
import com.example.bs.R;
import com.example.bs.db.AppointmentDao;
import com.google.android.material.button.MaterialButton;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Фрагмент для выбора времени записи.
 * Отображает доступные временные слоты в сетке (3 в ряд) для удобства.
 * Загрузка слотов выполняется в фоновом потоке для предотвращения перегрузки основного потока.
 */
public class TimeSelectionFragment extends Fragment {

    private long serviceId; // ID услуги
    private String selectedDate; // Выбранная дата
    private String selectedTime; // Выбранное время

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Инфлейтим разметку
        return inflater.inflate(R.layout.fragment_time_selection, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Получаем аргументы
        if (getArguments() != null) {
            serviceId = getArguments().getLong("service_id");
            selectedDate = getArguments().getString("selected_date");
        }

        // Находим элементы UI
        TextView dateText = view.findViewById(R.id.text_selected_date);
        TextView errorText = view.findViewById(R.id.text_error);
        MaterialButton nextButton = view.findViewById(R.id.button_next);
        GridLayout timeContainer = view.findViewById(R.id.layout_time_slots);

        dateText.setText("Дата: " + selectedDate);

        // Загружаем доступные слоты в фоновом потоке
        loadAvailableTimeSlots(timeContainer, errorText, nextButton);

        // Обработчик кнопки "Далее" для перехода к выбору мастера
        nextButton.setOnClickListener(v -> {
            if (selectedTime != null) {
                MasterSelectionFragment masterFragment = new MasterSelectionFragment();
                Bundle args = new Bundle();
                args.putLong("service_id", serviceId);
                args.putString("selected_date", selectedDate);
                args.putString("selected_time", selectedTime);
                masterFragment.setArguments(args);

                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, masterFragment)
                        .addToBackStack(null)
                        .commit();
            }
        });

        // Обработчик кнопки "Назад"
        view.findViewById(R.id.button_back).setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStack();
        });
    }

    /**
     * Загружает и отображает доступные временные слоты в GridLayout.
     * Выполняется в фоновом потоке для оптимизации.
     * @param container Контейнер для слотов (GridLayout)
     * @param errorText Текст для отображения ошибки
     * @param nextButton Кнопка "Далее" для активации
     */
    private void loadAvailableTimeSlots(GridLayout container, TextView errorText, MaterialButton nextButton) {
        executor.execute(() -> {
            AppointmentDao appointmentDao = new AppointmentDao(requireContext());
            List<String> availableTimes = appointmentDao.getAvailableTimeSlots(selectedDate, serviceId);

            // Переключаемся на главный поток для обновления UI
            mainHandler.post(() -> {
                container.removeAllViews();

                if (availableTimes.isEmpty()) {
                    errorText.setText("Нет доступного времени на эту дату");
                    errorText.setVisibility(View.VISIBLE);
                    nextButton.setEnabled(false);
                    return;
                }

                errorText.setVisibility(View.GONE);

                for (String time : availableTimes) {
                    MaterialButton timeButton = new MaterialButton(requireContext());
                    timeButton.setText(time);
                    timeButton.setBackgroundResource(R.drawable.time_slot_default);
                    timeButton.setTextColor(getResources().getColor(R.color.colorText));
                    timeButton.setPadding(32, 16, 32, 16);

                    GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                    params.width = 0;
                    params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
                    params.setMargins(8, 8, 8, 8);
                    timeButton.setLayoutParams(params);

                    timeButton.setOnClickListener(v -> {
                        selectedTime = time;
                        // Сброс выделения у всех кнопок
                        for (int i = 0; i < container.getChildCount(); i++) {
                            View child = container.getChildAt(i);
                            if (child instanceof MaterialButton) {
                                child.setBackgroundResource(R.drawable.time_slot_default);
                            }
                        }
                        // Выделяем выбранную кнопку
                        timeButton.setBackgroundResource(R.drawable.time_slot_selected);
                        nextButton.setEnabled(true);
                    });

                    container.addView(timeButton);
                }
            });
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executor.shutdown(); // Очищаем пул потоков
    }
}