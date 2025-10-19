package com.example.bs.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.bs.R;
import com.example.bs.db.CategoryDao;
import com.example.bs.db.ServiceDao;
import com.example.bs.model.Service;
import com.example.bs.model.Category;

/**
 * Фрагмент для отображения деталей услуги.
 * Показывает название, категорию, цену и длительность услуги.
 */
public class ServiceDetailFragment extends Fragment {

    private Service service;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_service_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Получаем ID услуги из аргументов
        if (getArguments() != null) {
            long serviceId = getArguments().getLong("service_id");
            ServiceDao serviceDao = new ServiceDao(requireContext());
            CategoryDao categoryDao = new CategoryDao(requireContext());

            service = serviceDao.getServiceById(serviceId);
            if (service != null) {
                Category category = categoryDao.getCategoryById(service.getCategoryId());
                // Находим элементы UI
                TextView nameText = view.findViewById(R.id.text_service_name);
                TextView categoryText = view.findViewById(R.id.text_service_category);
                TextView priceText = view.findViewById(R.id.text_service_price);
                TextView durationText = view.findViewById(R.id.text_service_duration);

                nameText.setText(service.getName());
                categoryText.setText(category != null ? category.getName() : "Неизвестно");
                priceText.setText(String.format("%.2f руб", service.getPrice()));
                durationText.setText(String.format("%d минут", service.getDuration()));

                Button bookButton = view.findViewById(R.id.button_book);
                bookButton.setOnClickListener(v -> {
                    // Переходим к выбору даты
                    DateSelectionFragment dateFragment = new DateSelectionFragment();
                    Bundle args = new Bundle();
                    args.putLong("service_id", serviceId);
                    dateFragment.setArguments(args);

                    requireActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, dateFragment)
                            .addToBackStack(null)
                            .commit();
                });
            }
        }

        // Кнопка назад
        view.findViewById(R.id.button_back).setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStack();
        });
    }
}