package com.example.bs.ui.fragments;

import android.os.Bundle;
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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Фрагмент каталога услуг с фильтрами и поиском.
 */
public class CatalogFragment extends Fragment {

    private RecyclerView recyclerView;
    private ServiceAdapter adapter;
    private ServiceDao serviceDao;
    private CategoryDao categoryDao;
    private List<Service> allServices;
    private List<Service> filteredServices;
    private TextView textEmpty;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_catalog, container, false);

        // Инициализация DAO
        serviceDao = new ServiceDao(requireContext());
        categoryDao = new CategoryDao(requireContext());
        allServices = serviceDao.getAllServices();
        filteredServices = new ArrayList<>(allServices);

        // Привязка элементов
        recyclerView = view.findViewById(R.id.recycler_view_services);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new ServiceAdapter(filteredServices, categoryDao); // Исправленный вызов
        recyclerView.setAdapter(adapter);

        Spinner spinnerCategory = view.findViewById(R.id.spinner_category);
        EditText editSearch = view.findViewById(R.id.edit_search);
        EditText editPriceFrom = view.findViewById(R.id.edit_price_from);
        EditText editPriceTo = view.findViewById(R.id.edit_price_to);
        com.google.android.material.button.MaterialButton buttonFilter = view.findViewById(R.id.button_filter);
        textEmpty = view.findViewById(R.id.text_empty);

        // Настройка Spinner
        List<Category> categories = categoryDao.getAllCategories();
        List<String> categoryNames = new ArrayList<>();
        categoryNames.add("Все");
        for (Category category : categories) {
            categoryNames.add(category.getName());
        }
        ArrayAdapter<String> adapterSpinner = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_spinner_item, categoryNames);
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapterSpinner);

        // Обработчики
        editSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterBySearch(s.toString(), spinnerCategory.getSelectedItemPosition(),
                        editPriceFrom.getText().toString(), editPriceTo.getText().toString(),
                        categories);
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        buttonFilter.setOnClickListener(v -> applyFilter(
                spinnerCategory.getSelectedItemPosition(),
                editPriceFrom.getText().toString(),
                editPriceTo.getText().toString(),
                categories
        ));

        // Инициализация с начальными данными
        applyFilter(spinnerCategory.getSelectedItemPosition(), "", "", categories);

        return view;
    }

    private void applyFilter(int selectedPosition, String priceFromStr, String priceToStr, List<Category> categories) {
        try {
            filteredServices.clear();
            double priceFrom = priceFromStr.isEmpty() ? 0.0 : Double.parseDouble(priceFromStr);
            double priceTo = priceToStr.isEmpty() ? Double.MAX_VALUE : Double.parseDouble(priceToStr);

            long categoryId = selectedPosition == 0 ? -1 : categories.get(selectedPosition - 1).getId();

            if (categoryId == -1) {
                filteredServices.addAll(serviceDao.getServicesByPriceRange(priceFrom, priceTo));
            } else {
                List<Service> byCategory = serviceDao.getServicesByCategoryId(categoryId);
                filteredServices.addAll(byCategory.stream()
                        .filter(service -> service.getPrice() >= priceFrom && service.getPrice() <= priceTo)
                        .collect(Collectors.toList()));
            }

            updateUI();
        } catch (NumberFormatException e) {
            Log.e("CatalogFragment", "Invalid price input", e);
            filteredServices.clear();
            updateUI();
        }
    }

    private void filterBySearch(String searchText, int selectedPosition, String priceFromStr, String priceToStr, List<Category> categories) {
        applyFilter(selectedPosition, priceFromStr, priceToStr, categories);
        if (!searchText.isEmpty()) {
            filteredServices = filteredServices.stream()
                    .filter(service -> service.getName().toLowerCase().contains(searchText.toLowerCase()))
                    .collect(Collectors.toList());
        }
        updateUI();
    }

    private void updateUI() {
        if (adapter != null) {
            adapter.updateList(filteredServices);
            textEmpty.setVisibility(filteredServices.isEmpty() ? View.VISIBLE : View.GONE);
            Log.d("CatalogFragment", "Filtered services count: " + filteredServices.size());
        }
    }
}