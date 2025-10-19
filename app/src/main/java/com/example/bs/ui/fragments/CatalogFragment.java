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

public class CatalogFragment extends Fragment {

    private RecyclerView recyclerView;
    private ServiceAdapter adapter;
    private ServiceDao serviceDao;
    private CategoryDao categoryDao;
    private List<Service> allServices;
    private List<Service> filteredServices;
    private TextView textEmpty;
    private boolean isSpinnerInitialized = false; // Флаг инициализации Spinner

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

        // Отключаем TextWatcher до инициализации Spinner
        editSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isSpinnerInitialized) {
                    filterBySearch(s.toString(), spinnerCategory.getSelectedItemPosition(),
                            editPriceFrom.getText().toString(), editPriceTo.getText().toString());
                }
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

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

    private void loadInitialData(Spinner spinnerCategory) {
        executor.execute(() -> {
            List<Category> categories = categoryDao.getAllCategories();
            allServices = serviceDao.getAllServices();

            mainHandler.post(() -> {
                List<String> categoryNames = new ArrayList<>();
                categoryNames.add("Все");
                for (Category category : categories) {
                    categoryNames.add(category.getName());
                }
                ArrayAdapter<String> adapterSpinner = new ArrayAdapter<>(
                        requireContext(), android.R.layout.simple_spinner_item, categoryNames);
                adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCategory.setAdapter(adapterSpinner);
                isSpinnerInitialized = true; // Устанавливаем флаг после инициализации

                filteredServices.clear();
                filteredServices.addAll(allServices);
                updateUI();
            });
        });
    }

    private void applyFilter(int selectedPosition, String priceFromStr, String priceToStr) {
        executor.execute(() -> {
            try {
                List<Service> result = new ArrayList<>();
                double priceFrom = priceFromStr.isEmpty() ? 0.0 : Double.parseDouble(priceFromStr);
                double priceTo = priceToStr.isEmpty() ? Double.MAX_VALUE : Double.parseDouble(priceToStr);

                List<Category> categories = categoryDao.getAllCategories();
                long categoryId = -1;
                // Проверяем, что selectedPosition валиден
                if (selectedPosition > 0 && selectedPosition - 1 < categories.size()) {
                    categoryId = categories.get(selectedPosition - 1).getId();
                } else if (selectedPosition < 0) {
                    Log.w("CatalogFragment", "Invalid selectedPosition: " + selectedPosition);
                    mainHandler.post(() -> {
                        filteredServices.clear();
                        filteredServices.addAll(allServices);
                        updateUI();
                    });
                    return;
                }

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
            } catch (Exception e) {
                Log.e("CatalogFragment", "Ошибка в applyFilter", e);
            }
        });
    }

    private void filterBySearch(String searchText, int selectedPosition, String priceFromStr, String priceToStr) {
        executor.execute(() -> {
            try {
                List<Service> result = new ArrayList<>();
                double priceFrom = priceFromStr.isEmpty() ? 0.0 : Double.parseDouble(priceFromStr);
                double priceTo = priceToStr.isEmpty() ? Double.MAX_VALUE : Double.parseDouble(priceToStr);

                List<Category> categories = categoryDao.getAllCategories();
                long categoryId = -1;
                // Проверяем, что selectedPosition валиден
                if (selectedPosition > 0 && selectedPosition - 1 < categories.size()) {
                    categoryId = categories.get(selectedPosition - 1).getId();
                } else if (selectedPosition < 0) {
                    Log.w("CatalogFragment", "Invalid selectedPosition: " + selectedPosition);
                    mainHandler.post(() -> {
                        filteredServices.clear();
                        filteredServices.addAll(allServices);
                        updateUI();
                    });
                    return;
                }

                if (categoryId == -1) {
                    result.addAll(serviceDao.getServicesByPriceRange(priceFrom, priceTo));
                } else {
                    List<Service> byCategory = serviceDao.getServicesByCategoryId(categoryId);
                    result.addAll(byCategory.stream()
                            .filter(service -> service.getPrice() >= priceFrom && service.getPrice() <= priceTo)
                            .collect(Collectors.toList()));
                }

                final List<Service> filteredResult = searchText.isEmpty()
                        ? result
                        : result.stream()
                        .filter(service -> service.getName().toLowerCase().contains(searchText.toLowerCase()))
                        .collect(Collectors.toList());

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
            } catch (Exception e) {
                Log.e("CatalogFragment", "Ошибка в filterBySearch", e);
            }
        });
    }

    private void updateUI() {
        adapter.notifyDataSetChanged();
        Log.d("CatalogFragment", "Filtered services count: " + filteredServices.size());
        textEmpty.setVisibility(filteredServices.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}