package com.example.bs.ui.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
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
 * Фрагмент каталога услуг с фильтрами.
 */
public class CatalogFragment extends Fragment {

    private RecyclerView recyclerView;
    private ServiceAdapter adapter;
    private ServiceDao serviceDao;
    private CategoryDao categoryDao;
    private List<Service> allServices;
    private List<Service> filteredServices;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_catalog, container, false);

        // Инициализация DAO
        serviceDao = new ServiceDao(requireContext());
        categoryDao = new CategoryDao(requireContext());
        allServices = serviceDao.getAllServices();
        filteredServices = new ArrayList<>(allServices);

        // Настройка RecyclerView
        recyclerView = view.findViewById(R.id.recycler_view_services);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ServiceAdapter(filteredServices);
        recyclerView.setAdapter(adapter);


        // Настройка Spinner с категориями из БД
        Spinner spinnerCategory = view.findViewById(R.id.spinner_category);
        List<Category> categories = categoryDao.getAllCategories();
        List<String> categoryNames = new ArrayList<>();
        categoryNames.add("Все"); // Опция "Все"
        for (Category category : categories) {
            categoryNames.add(category.getName());
        }
        ArrayAdapter<String> adapterSpinner = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_spinner_item, categoryNames);
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapterSpinner);

        // Настройка EditText и кнопки фильтра
        EditText editPriceFrom = view.findViewById(R.id.edit_price_from);
        EditText editPriceTo = view.findViewById(R.id.edit_price_to);
        view.findViewById(R.id.button_filter).setOnClickListener(v -> applyFilter(
                spinnerCategory.getSelectedItemPosition(),
                editPriceFrom.getText().toString(),
                editPriceTo.getText().toString(),
                categories
        ));

        return view;
    }

    private void applyFilter(int selectedPosition, String priceFromStr, String priceToStr, List<Category> categories) {
        filteredServices.clear();
        double priceFrom = priceFromStr.isEmpty() ? 0.0 : Double.parseDouble(priceFromStr);
        double priceTo = priceToStr.isEmpty() ? Double.MAX_VALUE : Double.parseDouble(priceToStr);

        long categoryId = selectedPosition == 0 ? -1 : categories.get(selectedPosition - 1).getId();

        if (categoryId == -1) {
            // Фильтрация только по цене
            filteredServices.addAll(serviceDao.getServicesByPriceRange(priceFrom, priceTo));
        } else {
            // Фильтрация по категории и цене
            List<Service> byCategory = serviceDao.getServicesByCategoryId(categoryId);
            filteredServices.addAll(byCategory.stream()
                    .filter(service -> service.getPrice() >= priceFrom && service.getPrice() <= priceTo)
                    .collect(Collectors.toList()));
        }

        adapter.updateList(filteredServices);
        Log.d("CatalogFragment", "Filtered services count: " + filteredServices.size());
    }
}