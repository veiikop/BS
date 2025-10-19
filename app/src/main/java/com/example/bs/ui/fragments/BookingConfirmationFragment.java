package com.example.bs.ui.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
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

/**
 * Фрагмент для подтверждения записи.
 * Отображает детали выбранной услуги, мастера, даты и времени.
 * Позволяет подтвердить создание записи с предотвращением дубликатов.
 */
public class BookingConfirmationFragment extends Fragment {

    private long serviceId; // ID выбранной услуги
    private String dateTime; // Дата и время записи (yyyy-MM-dd HH:mm)
    private long masterId; // ID выбранного мастера

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Инфлейтим разметку фрагмента
        return inflater.inflate(R.layout.fragment_booking_confirmation, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Получаем аргументы из бандла
        if (getArguments() != null) {
            serviceId = getArguments().getLong("service_id");
            dateTime = getArguments().getString("date_time");
            masterId = getArguments().getLong("master_id");
        }

        // Отображаем информацию о записи
        displayBookingInfo(view);

        // Находим элементы UI
        ProgressBar progress = view.findViewById(R.id.progress_bar);
        MaterialButton confirmButton = view.findViewById(R.id.button_confirm_booking);
        MaterialButton backButton = view.findViewById(R.id.button_back);
        MaterialButton cancelButton = view.findViewById(R.id.button_cancel);

        // Обработчик кнопки подтверждения
        confirmButton.setOnClickListener(v -> {
            confirmButton.setEnabled(false); // Отключаем кнопку, чтобы предотвратить повторные клики
            progress.setVisibility(View.VISIBLE); // Показываем индикатор загрузки
            createAppointment(); // Создаем запись
            progress.setVisibility(View.GONE); // Скрываем индикатор
            confirmButton.setEnabled(true); // Включаем кнопку обратно
        });

        // Обработчик кнопки "Назад"
        backButton.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        // Обработчик кнопки "Вернуться в каталог"
        cancelButton.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Вернуться в каталог?")
                    .setMessage("Все данные записи будут сброшены.")
                    .setPositiveButton("Да", (dialog, which) -> requireActivity().getSupportFragmentManager().popBackStack("catalog", 0))
                    .setNegativeButton("Нет", null)
                    .show();
        });
    }

    /**
     * Отображает детали записи (услуга, мастер, дата/время, цена).
     * @param view Вью фрагмента
     */
    private void displayBookingInfo(View view) {
        ServiceDao serviceDao = new ServiceDao(requireContext());
        MasterDao masterDao = new MasterDao(requireContext());

        Service service = serviceDao.getServiceById(serviceId);
        Master master = masterDao.getMasterById(masterId);

        if (service != null && master != null) {
            TextView serviceText = view.findViewById(R.id.text_service);
            TextView masterText = view.findViewById(R.id.text_master);
            TextView dateText = view.findViewById(R.id.text_date_time);
            TextView priceText = view.findViewById(R.id.text_price);

            serviceText.setText("Услуга: " + service.getName());
            masterText.setText("Мастер: " + master.getName() + " " + master.getSurname());
            dateText.setText("Дата и время: " + dateTime);
            priceText.setText(String.format("Цена: %.2f руб", service.getPrice()));
        } else {
            Toast.makeText(requireContext(), "Ошибка загрузки данных", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Создает запись в базе данных, проверяя на дубликаты.
     */
    private void createAppointment() {
        // TODO: Заменить на реальный ID пользователя (например, из SharedPreferences)
        long userId = 1; // Временное значение для демо

        ServiceDao serviceDao = new ServiceDao(requireContext());
        Service service = serviceDao.getServiceById(serviceId);

        if (service != null) {
            AppointmentDao appointmentDao = new AppointmentDao(requireContext());
            // Проверяем, не существует ли уже такая запись
            if (appointmentDao.appointmentExists(userId, serviceId, masterId, dateTime)) {
                Toast.makeText(requireContext(), "Запись уже существует", Toast.LENGTH_SHORT).show();
                return;
            }

            // Создаем объект записи
            Appointment appointment = new Appointment();
            appointment.setUserId(userId);
            appointment.setServiceId(serviceId);
            appointment.setMasterId(masterId);
            appointment.setDateTime(dateTime);
            appointment.setPrice(service.getPrice());
            appointment.setStatus("future");

            // Вставляем запись в базу данных
            long appointmentId = appointmentDao.insertAppointment(appointment);

            if (appointmentId != -1) {
                Toast.makeText(requireContext(), "Запись успешно создана!", Toast.LENGTH_LONG).show();
                // Возвращаемся в каталог
                requireActivity().getSupportFragmentManager().popBackStack("catalog", 0);
            } else {
                Toast.makeText(requireContext(), "Ошибка при создании записи", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(requireContext(), "Ошибка загрузки услуги", Toast.LENGTH_SHORT).show();
        }
    }
}