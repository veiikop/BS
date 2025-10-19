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
import androidx.fragment.app.Fragment;
import com.example.bs.R;
import com.example.bs.db.AppointmentDao;
import com.example.bs.db.MasterDao;
import com.example.bs.db.ServiceDao;
import com.example.bs.model.Appointment;
import com.example.bs.model.Master;
import com.example.bs.model.Service;
import com.google.android.material.button.MaterialButton;

import java.util.List;

/**
 * Фрагмент для выбора мастера.
 * Отображает список доступных мастеров для выбранного времени и услуги.
 */
public class MasterSelectionFragment extends Fragment {

    private long serviceId; // ID услуги
    private String selectedDate; // Дата
    private String selectedTime; // Время
    private long selectedMasterId; // ID выбранного мастера

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Инфлейтим разметку
        return inflater.inflate(R.layout.fragment_master_selection, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Получаем аргументы
        if (getArguments() != null) {
            serviceId = getArguments().getLong("service_id");
            selectedDate = getArguments().getString("selected_date");
            selectedTime = getArguments().getString("selected_time");
        }

        String dateTime = selectedDate + " " + selectedTime;

        // Находим элементы UI
        TextView infoText = view.findViewById(R.id.text_selection_info);
        MaterialButton confirmButton = view.findViewById(R.id.button_confirm);
        LinearLayout masterContainer = view.findViewById(R.id.layout_masters);

        infoText.setText(String.format("Дата: %s\nВремя: %s", selectedDate, selectedTime));

        // Загружаем мастеров
        loadAvailableMasters(masterContainer, confirmButton, dateTime);

        // Обработчик подтверждения выбора мастера
        confirmButton.setOnClickListener(v -> {
            if (selectedMasterId != 0) {
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
            }
        });

        // Обработчик кнопки "Назад"
        view.findViewById(R.id.button_back).setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStack();
        });
    }

    /**
     * Загружает и отображает доступных мастеров.
     * @param container Контейнер для элементов мастеров
     * @param confirmButton Кнопка подтверждения
     * @param dateTime Дата и время в формате yyyy-MM-dd HH:mm
     */
    private void loadAvailableMasters(LinearLayout container, MaterialButton confirmButton, String dateTime) {
        AppointmentDao appointmentDao = new AppointmentDao(requireContext());
        List<Master> availableMasters = appointmentDao.getAvailableMasters(dateTime, serviceId);

        container.removeAllViews();

        if (availableMasters.isEmpty()) {
            TextView noMastersText = new TextView(requireContext());
            noMastersText.setText("Нет доступных мастеров на это время");
            noMastersText.setTextSize(16);
            noMastersText.setTextColor(getResources().getColor(R.color.colorError));
            container.addView(noMastersText);
            confirmButton.setEnabled(false);
            return;
        }

        for (Master master : availableMasters) {
            View masterItem = LayoutInflater.from(requireContext())
                    .inflate(R.layout.item_master, container, false);

            TextView nameText = masterItem.findViewById(R.id.text_master_name);
            TextView specialtyText = masterItem.findViewById(R.id.text_master_specialty);
            MaterialButton selectButton = masterItem.findViewById(R.id.button_select_master);

            nameText.setText(master.getName() + " " + master.getSurname());
            specialtyText.setText("Специализация: " + master.getSpecialty());

            selectButton.setOnClickListener(v -> {
                selectedMasterId = master.getId();
                selectButton.setText("Выбрано");
                selectButton.setEnabled(false);
                confirmButton.setEnabled(true);
            });

            container.addView(masterItem);
        }
    }
}