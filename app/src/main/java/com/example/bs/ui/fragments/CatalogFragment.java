package com.example.bs.ui.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.bs.R;
import com.example.bs.db.CategoryDao;
import com.example.bs.db.ServiceDao;
import com.example.bs.model.Category;
import com.example.bs.model.Service;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * Фрагмент каталога услуг с фильтрами и поиском.
 * Загрузка данных выполняется в фоновом потоке для оптимизации.
 */
public class CatalogFragment extends Fragment {

    private RecyclerView recyclerView;
    private ServiceAdapter adapter;
    private ServiceDao serviceDao;
    private CategoryDao categoryDao;
    private List<Service> allServices;
    private List<Service> filteredServices;
    private TextView textEmpty;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_catalog, container, false);

        // Инициализация DAO
        serviceDao = new ServiceDao(requireContext());
        categoryDao = new CategoryDao(requireContext());
        allServices = new ArrayList<>();
        filteredServices = new ArrayList<>();

        // Привязка элементов
        recyclerView = view.findViewById(R.id.recycler_view_services);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new ServiceAdapter(filteredServices, categoryDao, requireContext());
        recyclerView.setAdapter(adapter);

        Spinner spinnerCategory = view.findViewById(R.id.spinner_category);
        EditText editSearch = view.findViewById(R.id.edit_search);
        EditText editPriceFrom = view.findViewById(R.id.edit_price_from);
        EditText editPriceTo = view.findViewById(R.id.edit_price_to);
        MaterialButton buttonFilter = view.findViewById(R.id.button_filter);
        textEmpty = view.findViewById(R.id.text_empty);

        // Загрузка категорий и услуг в фоновом потоке
        loadInitialData(spinnerCategory);

        // Обработчики
        editSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterBySearch(s.toString(), spinnerCategory.getSelectedItemPosition(),
                        editPriceFrom.getText().toString(), editPriceTo.getText().toString());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        buttonFilter.setOnClickListener(v -> applyFilter(
                spinnerCategory.getSelectedItemPosition(),
                editPriceFrom.getText().toString(),
                editPriceTo.getText().toString()
        ));

        return view;
    }

    /**
     * Загружает начальные данные (услуги и категории) в фоновом потоке.
     * @param spinnerCategory Спиннер для категорий
     */
    private void loadInitialData(Spinner spinnerCategory) {
        executor.execute(() -> {
            List<Category> categories = categoryDao.getAllCategories();
            allServices = serviceDao.getAllServices();

            mainHandler.post(() -> {
                // Настройка Spinner
                List<String> categoryNames = new ArrayList<>();
                categoryNames.add("Все");
                for (Category category : categories) {
                    categoryNames.add(category.getName());
                }
                ArrayAdapter<String> adapterSpinner = new ArrayAdapter<>(
                        requireContext(), android.R.layout.simple_spinner_item, categoryNames);
                adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCategory.setAdapter(adapterSpinner);

                // Инициализация с начальными данными
                filteredServices.clear();
                filteredServices.addAll(allServices);
                updateUI();
            });
        });
    }

    /**
     * Применяет фильтры по категории и цене.
     * @param selectedPosition Позиция в спиннере категорий
     * @param priceFromStr Цена от
     * @param priceToStr Цена до
     */
    private void applyFilter(int selectedPosition, String priceFromStr, String priceToStr) {
        executor.execute(() -> {
            try {
                List<Service> result = new ArrayList<>();
                double priceFrom = priceFromStr.isEmpty() ? 0.0 : Double.parseDouble(priceFromStr);
                double priceTo = priceToStr.isEmpty() ? Double.MAX_VALUE : Double.parseDouble(priceToStr);

                long categoryId = selectedPosition == 0 ? -1 : categoryDao.getAllCategories().get(selectedPosition - 1).getId();

                if (categoryId == -1) {
                    result.addAll(serviceDao.getServicesByPriceRange(priceFrom, priceTo));
                } else {
                    List<Service> byCategory = serviceDao.getServicesByCategoryId(categoryId);
                    result.addAll(byCategory.stream()
                            .filter(service -> service.getPrice() >= priceFrom && service.getPrice() <= priceTo)
                            .collect(Collectors.toList()));
                }

                mainHandler.post(() -> {
                    filteredServices.clear();
                    filteredServices.addAll(result);
                    updateUI();
                });
            } catch (NumberFormatException e) {
                Log.e("CatalogFragment", "Неверный формат цены", e);
                mainHandler.post(() -> {
                    filteredServices.clear();
                    updateUI();
                });
            }
        });
    }

    /**
     * Фильтрует услуги по поисковому запросу, категории и цене.
     * @param searchText Поисковый запрос
     * @param selectedPosition Позиция в спиннере
     * @param priceFromStr Цена от
     * @param priceToStr Цена до
     */
    private void filterBySearch(String searchText, int selectedPosition, String priceFromStr, String priceToStr) {
        executor.execute(() -> {
            try {
                // Создаем начальный список для результатов
                List<Service> result = new ArrayList<>();
                double priceFrom = priceFromStr.isEmpty() ? 0.0 : Double.parseDouble(priceFromStr);
                double priceTo = priceToStr.isEmpty() ? Double.MAX_VALUE : Double.parseDouble(priceToStr);

                long categoryId = selectedPosition == 0 ? -1 : categoryDao.getAllCategories().get(selectedPosition - 1).getId();

                // Фильтрация по категории и цене
                if (categoryId == -1) {
                    result.addAll(serviceDao.getServicesByPriceRange(priceFrom, priceTo));
                } else {
                    List<Service> byCategory = serviceDao.getServicesByCategoryId(categoryId);
                    result.addAll(byCategory.stream()
                            .filter(service -> service.getPrice() >= priceFrom && service.getPrice() <= priceTo)
                            .collect(Collectors.toList()));
                }

                // Фильтрация по поисковому запросу (создаем новый список вместо изменения result)
                final List<Service> filteredResult = searchText.isEmpty()
                        ? result
                        : result.stream()
                        .filter(service -> service.getName().toLowerCase().contains(searchText.toLowerCase()))
                        .collect(Collectors.toList());

                // Обновление UI в главном потоке
                mainHandler.post(() -> {
                    filteredServices.clear();
                    filteredServices.addAll(filteredResult);
                    updateUI();
                });
            } catch (NumberFormatException e) {
                Log.e("CatalogFragment", "Неверный формат цены", e);
                mainHandler.post(() -> {
                    filteredServices.clear();
                    updateUI();
                });
            }
        });
    }

    /**
     * Обновляет UI с отфильтрованными услугами.
     */
    private void updateUI() {
        if (adapter != null) {
            adapter.updateList(filteredServices);
            textEmpty.setVisibility(filteredServices.isEmpty() ? View.VISIBLE : View.GONE);
            Log.d("CatalogFragment", "Filtered services count: " + filteredServices.size());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executor.shutdown(); // Очищаем пул потоков
    }
}