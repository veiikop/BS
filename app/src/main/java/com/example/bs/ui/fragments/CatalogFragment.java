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
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.bs.R;
import com.example.bs.db.CategoryDao;
import com.example.bs.db.ServiceDao;
import com.example.bs.model.Category;
import com.example.bs.model.Service;
import com.example.bs.ui.adapters.ServiceAdapter;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class CatalogFragment extends Fragment {

    private RecyclerView recyclerView;
    private ServiceAdapter adapter;
    private ServiceDao serviceDao;
    private CategoryDao categoryDao;
    private List<Service> allServices;
    private List<Service> filteredServices;
    private TextView textEmpty;
    private boolean isSpinnerInitialized = false;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_catalog, container, false);

        serviceDao = new ServiceDao(requireContext());
        categoryDao = new CategoryDao(requireContext());
        allServices = new ArrayList<>();
        filteredServices = new ArrayList<>();

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

        loadInitialData(spinnerCategory);

        // Поиск с задержкой
        editSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isSpinnerInitialized) {
                    filterBySearch(s.toString(),
                            spinnerCategory.getSelectedItemPosition(),
                            editPriceFrom.getText().toString(),
                            editPriceTo.getText().toString());
                }
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // Кнопка "Фильтр"
        buttonFilter.setOnClickListener(v -> {
            if (isSpinnerInitialized) {
                applyFilter(
                        spinnerCategory.getSelectedItemPosition(),
                        editPriceFrom.getText().toString(),
                        editPriceTo.getText().toString()
                );
            }
        });

        return view;
    }

    // ========================================================================
    // ВАЛИДАЦИЯ ЦЕН
    // ========================================================================
    private static class ValidationResult {
        boolean isValid;
        String errorMessage;
        double from, to;
        boolean clearFrom, clearTo;

        ValidationResult(boolean isValid, String errorMessage, double from, double to,
                         boolean clearFrom, boolean clearTo) {
            this.isValid = isValid;
            this.errorMessage = errorMessage;
            this.from = from;
            this.to = to;
            this.clearFrom = clearFrom;
            this.clearTo = clearTo;
        }
    }

    private ValidationResult validatePriceRange(String fromStr, String toStr) {
        EditText editFrom = requireView().findViewById(R.id.edit_price_from);
        EditText editTo = requireView().findViewById(R.id.edit_price_to);

        double from = 0.0;
        double to = Double.MAX_VALUE;
        boolean clearFrom = false;
        boolean clearTo = false;

        // "От"
        if (!fromStr.isEmpty()) {
            try {
                from = Double.parseDouble(fromStr);
                if (from < 0) {
                    return new ValidationResult(false, "Цена 'от' не может быть отрицательной", 0, 0, true, false);
                }
            } catch (NumberFormatException e) {
                return new ValidationResult(false, "Некорректное значение в поле 'от'", 0, 0, true, false);
            }
        }

        // "До"
        if (!toStr.isEmpty()) {
            try {
                to = Double.parseDouble(toStr);
                if (to < 0) {
                    return new ValidationResult(false, "Цена 'до' не может быть отрицательной", 0, 0, false, true);
                }
                if (from > 0 && to < from) {
                    return new ValidationResult(false, "Цена 'до' не может быть меньше 'от'", 0, 0, false, true);
                }
            } catch (NumberFormatException e) {
                return new ValidationResult(false, "Некорректное значение в поле 'до'", 0, 0, false, true);
            }
        }

        return new ValidationResult(true, "", from, to, clearFrom, clearTo);
    }

    // ========================================================================
    // ФИЛЬТРАЦИЯ
    // ========================================================================
    private void applyFilter(int selectedPosition, String priceFromStr, String priceToStr) {
        ValidationResult validation = validatePriceRange(priceFromStr, priceToStr);
        if (!validation.isValid) {
            mainHandler.post(() -> {
                Toast.makeText(requireContext(), validation.errorMessage, Toast.LENGTH_LONG).show();
                if (validation.clearFrom) {
                    ((EditText) requireView().findViewById(R.id.edit_price_from)).setText("");
                }
                if (validation.clearTo) {
                    ((EditText) requireView().findViewById(R.id.edit_price_to)).setText("");
                }
            });
            return;
        }

        double priceFrom = validation.from;
        double priceTo = validation.to;

        executor.execute(() -> {
            try {
                List<Service> result = new ArrayList<>();
                List<Category> categories = categoryDao.getAllCategories();
                long categoryId = -1;

                if (selectedPosition > 0 && selectedPosition - 1 < categories.size()) {
                    categoryId = categories.get(selectedPosition - 1).getId();
                }

                if (categoryId == -1) {
                    result.addAll(serviceDao.getServicesByPriceRange(priceFrom, priceTo));
                } else {
                    List<Service> byCategory = serviceDao.getServicesByCategoryId(categoryId);
                    result.addAll(byCategory.stream()
                            .filter(s -> s.getPrice() >= priceFrom && s.getPrice() <= priceTo)
                            .collect(Collectors.toList()));
                }

                mainHandler.post(() -> {
                    filteredServices.clear();
                    filteredServices.addAll(result);
                    updateUI();
                });
            } catch (Exception e) {
                Log.e("CatalogFragment", "Ошибка в applyFilter", e);
                mainHandler.post(() -> Toast.makeText(requireContext(), "Ошибка фильтрации", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void filterBySearch(String searchText, int selectedPosition, String priceFromStr, String priceToStr) {
        ValidationResult validation = validatePriceRange(priceFromStr, priceToStr);
        if (!validation.isValid) {
            mainHandler.post(() -> Toast.makeText(requireContext(), validation.errorMessage, Toast.LENGTH_SHORT).show());
            return;
        }

        double priceFrom = validation.from;
        double priceTo = validation.to;

        executor.execute(() -> {
            try {
                List<Service> result = new ArrayList<>();
                List<Category> categories = categoryDao.getAllCategories();
                long categoryId = -1;

                if (selectedPosition > 0 && selectedPosition - 1 < categories.size()) {
                    categoryId = categories.get(selectedPosition - 1).getId();
                }

                if (categoryId == -1) {
                    result.addAll(serviceDao.getServicesByPriceRange(priceFrom, priceTo));
                } else {
                    List<Service> byCategory = serviceDao.getServicesByCategoryId(categoryId);
                    result.addAll(byCategory.stream()
                            .filter(s -> s.getPrice() >= priceFrom && s.getPrice() <= priceTo)
                            .collect(Collectors.toList()));
                }

                final List<Service> filteredResult = searchText.isEmpty()
                        ? result
                        : result.stream()
                        .filter(s -> s.getName().toLowerCase().contains(searchText.toLowerCase()))
                        .collect(Collectors.toList());

                mainHandler.post(() -> {
                    filteredServices.clear();
                    filteredServices.addAll(filteredResult);
                    updateUI();
                });
            } catch (Exception e) {
                Log.e("CatalogFragment", "Ошибка в filterBySearch", e);
            }
        });
    }

    // ========================================================================
    // ЗАГРУЗКА ДАННЫХ
    // ========================================================================
    private void loadInitialData(Spinner spinnerCategory) {
        executor.execute(() -> {
            List<Category> categories = categoryDao.getAllCategories();
            allServices = serviceDao.getAllServices();

            mainHandler.post(() -> {
                List<String> categoryNames = new ArrayList<>();
                categoryNames.add("Все");
                for (Category c : categories) {
                    categoryNames.add(c.getName());
                }

                ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                        requireContext(), android.R.layout.simple_spinner_item, categoryNames);
                spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCategory.setAdapter(spinnerAdapter);
                isSpinnerInitialized = true;

                filteredServices.clear();
                filteredServices.addAll(allServices);
                updateUI();
            });
        });
    }

    private void updateUI() {
        adapter.notifyDataSetChanged();
        textEmpty.setVisibility(filteredServices.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (!executor.isShutdown()) {
            executor.shutdown();
        }
    }
}