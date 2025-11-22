package com.example.bs.ui.fragments;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.bs.R;
import com.example.bs.db.UserDao;
import com.example.bs.model.User;
import com.example.bs.ui.LoginActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class ProfileFragment extends Fragment {

    private UserDao userDao;
    private SharedPreferences sharedPreferences;
    private long currentUserId;

    private TextView textLogin;
    private EditText editName, editSurname, editBirthdate, editPhone, editGender;
    private Button buttonSave, buttonAbout, buttonLogout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        userDao = new UserDao(requireContext());
        sharedPreferences = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        currentUserId = sharedPreferences.getLong("user_id", -1);

        if (currentUserId == -1) {
            Toast.makeText(requireContext(), "Ошибка: пользователь не авторизован", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(requireContext(), LoginActivity.class));
            requireActivity().finish();
            return;
        }

        textLogin = view.findViewById(R.id.text_login);
        editName = view.findViewById(R.id.edit_name);
        editSurname = view.findViewById(R.id.edit_surname);
        editBirthdate = view.findViewById(R.id.edit_birthdate);
        editPhone = view.findViewById(R.id.edit_phone);
        editGender = view.findViewById(R.id.edit_gender);
        buttonSave = view.findViewById(R.id.button_save);
        buttonAbout = view.findViewById(R.id.button_about);
        buttonLogout = view.findViewById(R.id.button_logout);

        loadUserData();

        // DatePicker для birthdate
        editBirthdate.setOnClickListener(v -> showDatePicker());

        // Сохранение изменений
        buttonSave.setOnClickListener(v -> saveUserData());

        // О приложении
        buttonAbout.setOnClickListener(v -> showAboutDialog());

        // Выход
        buttonLogout.setOnClickListener(v -> logout());
    }

    private void loadUserData() {
        User user = userDao.getUserById(currentUserId);
        if (user != null) {
            textLogin.setText(user.getLogin());
            editName.setText(user.getName());
            editSurname.setText(user.getSurname());
            editBirthdate.setText(user.getBirthdate());
            editPhone.setText(user.getPhone());
            editGender.setText(user.getGender());
        } else {
            Toast.makeText(requireContext(), "Ошибка загрузки данных", Toast.LENGTH_SHORT).show();
        }
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            calendar.setTime(sdf.parse(editBirthdate.getText().toString()));
        } catch (Exception e) {
            // Используем текущую дату по умолчанию
        }

        DatePickerDialog datePicker = new DatePickerDialog(requireContext(),
                (view, year, month, dayOfMonth) -> {
                    Calendar selected = Calendar.getInstance();
                    selected.set(year, month, dayOfMonth);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    editBirthdate.setText(sdf.format(selected.getTime()));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        datePicker.show();
    }

    private void saveUserData() {
        User user = userDao.getUserById(currentUserId);
        if (user != null) {
            user.setName(editName.getText().toString().trim());
            user.setSurname(editSurname.getText().toString().trim());
            user.setBirthdate(editBirthdate.getText().toString().trim());
            user.setPhone(editPhone.getText().toString().trim());
            user.setGender(editGender.getText().toString().trim());

            // Валидация (простая)
            if (user.getName().isEmpty() || user.getSurname().isEmpty()) {
                Toast.makeText(requireContext(), "Имя и фамилия обязательны", Toast.LENGTH_SHORT).show();
                return;
            }

            int updated = userDao.updateUser(user);
            if (updated > 0) {
                Toast.makeText(requireContext(), "Данные обновлены", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(), "Ошибка обновления", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showAboutDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("О приложении")
                .setMessage("Тема проекта: Мобильное приложение салона красоты\nИсполнитель: Гришкова Виктория")
                .setPositiveButton("OK", null)
                .show();
    }

    private void logout() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Выход")
                .setMessage("Вы уверены, что хотите выйти?")
                .setPositiveButton("Да", (dialog, which) -> {
                    sharedPreferences.edit().clear().apply();
                    startActivity(new Intent(requireContext(), LoginActivity.class));
                    requireActivity().finish();
                })
                .setNegativeButton("Нет", null)
                .show();
    }
}