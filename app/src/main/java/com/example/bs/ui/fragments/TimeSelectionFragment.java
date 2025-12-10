package com.example.bs.ui.fragments;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.gridlayout.widget.GridLayout;
import com.example.bs.R;
import com.example.bs.db.AppointmentDao;
import com.google.android.material.button.MaterialButton;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TimeSelectionFragment extends BaseFragment {

    private long serviceId;
    private String selectedDate;
    private String selectedTime;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_time_selection, container, false);
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

        if (getArguments() != null) {
            serviceId = getArguments().getLong("service_id");
            selectedDate = getArguments().getString("selected_date");
        }

        TextView dateText = view.findViewById(R.id.text_selected_date);
        TextView errorText = view.findViewById(R.id.text_error);
        MaterialButton nextButton = view.findViewById(R.id.button_next);
        GridLayout timeContainer = view.findViewById(R.id.layout_time_slots);

        dateText.setText("Дата: " + selectedDate);

        loadAvailableTimeSlots(timeContainer, errorText, nextButton);

        nextButton.setOnClickListener(v -> {
            if (selectedTime == null) {
                Toast.makeText(requireContext(), "Пожалуйста, выберите время перед продолжением", Toast.LENGTH_SHORT).show();
                return;
            }

            // Проверяем авторизацию перед переходом
            if (!isUserLoggedIn) {
                Toast.makeText(requireContext(), "Сессия истекла, войдите заново", Toast.LENGTH_SHORT).show();
                return;
            }

            // логика перехода
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
        });

        // Обработчик кнопки "Назад"
        view.findViewById(R.id.button_back).setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStack();
        });
    }

    private void loadAvailableTimeSlots(GridLayout container, TextView errorText, MaterialButton nextButton) {
        executor.execute(() -> {
            AppointmentDao appointmentDao = new AppointmentDao(requireContext());
            List<String> availableTimes = appointmentDao.getAvailableTimeSlots(selectedDate, serviceId);

            mainHandler.post(() -> {
                container.removeAllViews();

                if (availableTimes.isEmpty()) {
                    errorText.setText("Нет доступного времени на эту дату");
                    errorText.setVisibility(View.VISIBLE);
                    return;
                }

                errorText.setVisibility(View.GONE);

                ColorStateList defaultTint = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.colorBackground)); // Серый фон по умолчанию
                ColorStateList selectedTint = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.colorPrimary)); // Розовый #FF69B4

                for (String time : availableTimes) {
                    MaterialButton timeButton = new MaterialButton(requireContext());
                    timeButton.setText(time);
                    timeButton.setBackgroundTintList(defaultTint); // Используем tint вместо resource
                    timeButton.setTextColor(getResources().getColor(R.color.colorText));
                    timeButton.setPadding(32, 16, 32, 16);

                    GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                    params.width = 0;
                    params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
                    params.setMargins(8, 8, 8, 8);
                    timeButton.setLayoutParams(params);

                    timeButton.setOnClickListener(v -> {
                        selectedTime = time;
                        for (int i = 0; i < container.getChildCount(); i++) {
                            View child = container.getChildAt(i);
                            if (child instanceof MaterialButton) {
                                ((MaterialButton) child).setBackgroundTintList(defaultTint);
                            }
                        }
                        timeButton.setBackgroundTintList(selectedTint);
                        ObjectAnimator scaleX = ObjectAnimator.ofFloat(timeButton, "scaleX", 1.0f, 1.1f, 1.0f);
                        ObjectAnimator scaleY = ObjectAnimator.ofFloat(timeButton, "scaleY", 1.0f, 1.1f, 1.0f);
                        AnimatorSet animatorSet = new AnimatorSet();
                        animatorSet.playTogether(scaleX, scaleY);
                        animatorSet.setDuration(300);
                        animatorSet.start();
                    });

                    container.addView(timeButton);
                }
            });
        });
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}