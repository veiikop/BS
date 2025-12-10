package com.example.bs.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import com.example.bs.R;
import com.example.bs.db.AppointmentDao;
import com.example.bs.model.Master;
import com.google.android.material.button.MaterialButton;

import java.util.List;

/**
 * Фрагмент для выбора мастера.
 * Гарантирует выбор ТОЛЬКО ОДНОГО мастера.
 */
public class MasterSelectionFragment extends BaseFragment {

    private long serviceId;
    private String selectedDate;
    private String selectedTime;
    private long selectedMasterId = -1; // -1 = ничего не выбрано

    // Хранит текущую выбранную кнопку (для сброса стиля)
    private MaterialButton selectedButton = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_master_selection, container, false);
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

        // Получаем аргументы
        if (getArguments() != null) {
            serviceId = getArguments().getLong("service_id");
            selectedDate = getArguments().getString("selected_date");
            selectedTime = getArguments().getString("selected_time");
        }

        String dateTime = selectedDate + " " + selectedTime;

        // UI элементы
        TextView infoText = view.findViewById(R.id.text_selection_info);
        MaterialButton confirmButton = view.findViewById(R.id.button_confirm);
        LinearLayout masterContainer = view.findViewById(R.id.layout_masters);
        MaterialButton backButton = view.findViewById(R.id.button_back);

        infoText.setText(String.format("Дата: %s\nВремя: %s", selectedDate, selectedTime));

        // Изначально кнопка "Подтвердить" неактивна
        confirmButton.setEnabled(false);

        // Загружаем мастеров
        loadAvailableMasters(masterContainer, confirmButton, dateTime);

        // Кнопка подтверждения
        confirmButton.setOnClickListener(v -> {
            if (selectedMasterId == -1) {
                Toast.makeText(requireContext(), "Выберите мастера", Toast.LENGTH_SHORT).show();
                return;
            }

            // Проверяем авторизацию перед переходом
            if (!isUserLoggedIn) {
                Toast.makeText(requireContext(), "Сессия истекла, войдите заново", Toast.LENGTH_SHORT).show();
                return;
            }

            BookingConfirmationFragment confirmationFragment = new BookingConfirmationFragment();
            Bundle args = new Bundle();
            args.putLong("service_id", serviceId);
            args.putString("date_time", dateTime);
            args.putLong("master_id", selectedMasterId);
            confirmationFragment.setArguments(args);

            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, confirmationFragment)
                    .addToBackStack(null)
                    .commit();
        });

        // Кнопка "Назад"
        backButton.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());
    }

    /**
     * Загружает мастеров и обеспечивает выбор только одного.
     */
    private void loadAvailableMasters(LinearLayout container, MaterialButton confirmButton, String dateTime) {
        AppointmentDao appointmentDao = new AppointmentDao(requireContext());
        List<Master> availableMasters = appointmentDao.getAvailableMasters(dateTime, serviceId);

        container.removeAllViews();

        if (availableMasters.isEmpty()) {
            TextView noMastersText = new TextView(requireContext());
            noMastersText.setText("Нет доступных мастеров на это время");
            noMastersText.setTextSize(16);
            noMastersText.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorError));
            noMastersText.setPadding(0, 32, 0, 0);
            container.addView(noMastersText);
            confirmButton.setEnabled(false);
            return;
        }

        // Цвета для состояний
        int colorPrimary = ContextCompat.getColor(requireContext(), R.color.colorPrimary);
        int colorPrimaryDark = ContextCompat.getColor(requireContext(), R.color.colorButtonPressed);

        for (Master master : availableMasters) {
            View masterItem = LayoutInflater.from(requireContext())
                    .inflate(R.layout.item_master, container, false);

            TextView nameText = masterItem.findViewById(R.id.text_master_name);
            TextView specialtyText = masterItem.findViewById(R.id.text_master_specialty);
            MaterialButton selectButton = masterItem.findViewById(R.id.button_select_master);

            nameText.setText(master.getName() + " " + master.getSurname());
            specialtyText.setText("Специализация: " + master.getSpecialty());

            // Сброс стиля по умолчанию
            selectButton.setText("Выбрать");
            selectButton.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(colorPrimary)
            );
            selectButton.setEnabled(true);

            // Обработчик выбора
            selectButton.setOnClickListener(v -> {
                // 1. Сбрасываем предыдущий выбор
                if (selectedButton != null && selectedButton != v) {
                    selectedButton.setText("Выбрать");
                    selectedButton.setBackgroundTintList(
                            android.content.res.ColorStateList.valueOf(colorPrimary)
                    );
                    selectedButton.setEnabled(true);
                }

                // 2. Выбираем текущий
                selectedButton = selectButton;
                selectedButton.setText("Выбрано");
                selectedButton.setBackgroundTintList(
                        android.content.res.ColorStateList.valueOf(colorPrimaryDark)
                );
                selectedButton.setEnabled(false); // Блокируем, чтобы нельзя было снять

                // 3. Сохраняем ID
                selectedMasterId = master.getId();

                // 4. Активируем кнопку "Подтвердить"
                confirmButton.setEnabled(true);
            });

            container.addView(masterItem);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // Проверяем авторизацию при возобновлении
        if (!isUserLoggedIn) {
            Toast.makeText(requireContext(), "Сессия истекла, войдите заново", Toast.LENGTH_SHORT).show();
            requireActivity().getSupportFragmentManager().popBackStack();
        }
    }
}