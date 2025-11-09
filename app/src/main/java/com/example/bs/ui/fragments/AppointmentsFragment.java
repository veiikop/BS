package com.example.bs.ui.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.bs.R;
import com.example.bs.db.AppointmentDao;
import com.example.bs.model.Appointment;
import com.example.bs.ui.fragments.AppointmentAdapter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AppointmentsFragment extends Fragment {

    private RecyclerView recyclerView;
    private AppointmentAdapter adapter;
    private TextView textEmpty;
    private Spinner spinnerFilter;
    private AppointmentDao appointmentDao;
    private long currentUserId = 1; // TODO: Заменить на реальный ID из SharedPreferences / Session

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_appointments, container, false);

        recyclerView = view.findViewById(R.id.recycler_appointments);
        textEmpty = view.findViewById(R.id.text_empty);
        spinnerFilter = view.findViewById(R.id.spinner_filter);

        appointmentDao = new AppointmentDao(requireContext());
        adapter = new AppointmentAdapter(requireContext());

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        setupFilter();
        loadAppointments("all");

        return view;
    }

    private void setupFilter() {
        String[] filters = {"Все", "Будущие", "Прошедшие"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, filters);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFilter.setAdapter(spinnerAdapter);

        spinnerFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String filter = position == 0 ? "all" : (position == 1 ? "future" : "past");
                loadAppointments(filter);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void loadAppointments(String filter) {
        executor.execute(() -> {
            // Обновляем статусы
            appointmentDao.updateStatusBasedOnTime();

            List<Appointment> allAppointments = appointmentDao.getAppointmentsByUserId(currentUserId);
            List<Appointment> filtered = new ArrayList<>();

            for (Appointment a : allAppointments) {
                if (filter.equals("all") ||
                        (filter.equals("future") && a.getStatus().equals("future")) ||
                        (filter.equals("past") && a.getStatus().equals("past"))) {
                    filtered.add(a);
                }
            }

            // Сортировка по дате
            filtered.sort((a1, a2) -> a2.getDateTime().compareTo(a1.getDateTime())); // DESC

            mainHandler.post(() -> {
                adapter.setAppointments(filtered);
                textEmpty.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
            });
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}