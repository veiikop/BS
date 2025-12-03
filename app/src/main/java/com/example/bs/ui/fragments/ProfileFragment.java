package com.example.bs.ui.fragments;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import com.example.bs.R;
import com.example.bs.db.UserDao;
import com.example.bs.model.User;
import com.example.bs.ui.LoginActivity;
import com.example.bs.util.NotificationHelper;
import com.example.bs.util.NotificationScheduler;
import com.example.bs.util.PhoneValidator;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Фрагмент профиля пользователя
 */
public class ProfileFragment extends Fragment {

    private UserDao userDao;
    private SharedPreferences sharedPreferences;
    private long currentUserId;

    private TextView textLogin;
    private EditText editName, editSurname, editBirthdate, editPhone;
    private RadioGroup radioGender;
    private RadioButton radioMale, radioFemale;
    private Button buttonSave, buttonAbout, buttonLogout;
    private SwitchCompat switchNotifications;

    // DatePicker для выбора даты рождения
    private Calendar birthdateCalendar;
    private SimpleDateFormat dateFormatter;

    private boolean isSwitchUpdateFromUser = false;

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
            showToast("Ошибка: пользователь не авторизован");
            navigateToLogin();
            return;
        }

        initViews(view);
        setupDatePicker();
        setupPhoneValidation();
        setupNotificationSettings(view);
        loadUserData();
        setupListeners();
    }

    /**
     * Инициализирует все View элементы
     */
    private void initViews(View view) {
        textLogin = view.findViewById(R.id.text_login);
        editName = view.findViewById(R.id.edit_name);
        editSurname = view.findViewById(R.id.edit_surname);
        editBirthdate = view.findViewById(R.id.edit_birthdate);
        editPhone = view.findViewById(R.id.edit_phone);
        radioGender = view.findViewById(R.id.radio_gender);
        radioMale = view.findViewById(R.id.radio_male);
        radioFemale = view.findViewById(R.id.radio_female);
        buttonSave = view.findViewById(R.id.button_save);
        buttonAbout = view.findViewById(R.id.button_about);
        buttonLogout = view.findViewById(R.id.button_logout);
        switchNotifications = view.findViewById(R.id.switch_notifications);

        // Инициализация форматтера даты
        dateFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        birthdateCalendar = Calendar.getInstance();
    }

    /**
     * Настраивает Switch уведомлений
     */
    private void setupNotificationSettings(View view) {
        switchNotifications.setOnCheckedChangeListener(null);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            boolean notificationsEnabled = NotificationScheduler.areNotificationsEnabled(requireContext());

            if (switchNotifications.isChecked() != notificationsEnabled) {
                switchNotifications.setChecked(notificationsEnabled);
            }

            setupSwitchListener();

            android.util.Log.d("ProfileFragment",
                    "Notification switch initialized to: " + notificationsEnabled);

        }, 100);
    }

    /**
     * Настраивает слушатель для Switch
     */
    private void setupSwitchListener() {
        switchNotifications.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (buttonView.isPressed() || isSwitchUpdateFromUser) {
                    isSwitchUpdateFromUser = false;

                    android.util.Log.d("ProfileFragment",
                            "Switch changed by user to: " + isChecked);

                    // Сохраняем настройку
                    NotificationScheduler.setNotificationsEnabled(requireContext(), isChecked);

                    // Дополнительные действия если включили
                    if (isChecked) {
                        checkSystemNotifications();
                    }
                }
            }
        });
    }

    /**
     * Проверяет системные настройки уведомлений
     */
    private void checkSystemNotifications() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (!NotificationHelper.areNotificationsEnabled(requireContext())) {
                // Если уведомления отключены на уровне системы, предлагаем включить
                requireActivity().runOnUiThread(() -> {
                    new AlertDialog.Builder(requireContext())
                            .setTitle("Включите уведомления")
                            .setMessage("Уведомления отключены в настройках системы. " +
                                    "Хотите включить их сейчас?")
                            .setPositiveButton("Включить", (dialog, which) -> {
                                NotificationHelper.openNotificationSettings(requireContext());
                            })
                            .setNegativeButton("Позже", null)
                            .setNeutralButton("Не показывать снова", (dialog, which) -> {
                                SharedPreferences prefs = requireContext()
                                        .getSharedPreferences("app_settings", Context.MODE_PRIVATE);
                                prefs.edit().putBoolean("dont_show_notification_warning", true).apply();
                            })
                            .show();
                });
            }
        }, 300);
    }

    /**
     * Настраивает DatePicker для выбора даты рождения
     */
    private void setupDatePicker() {
        editBirthdate.setOnClickListener(v -> showDatePickerDialog());

        //  возможность ручного ввода с валидацией
        editBirthdate.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 10) { // ГГГГ-ММ-ДД
                    validateBirthdate(s.toString());
                }
            }
        });
    }

    /**
     * Настраивает валидацию и форматирование телефона
     */
    private void setupPhoneValidation() {
        editPhone.addTextChangedListener(PhoneValidator.createPhoneTextWatcher(editPhone));

        // Валидация при потере фокуса
        editPhone.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String phone = editPhone.getText().toString().trim();
                if (!phone.isEmpty() && !PhoneValidator.isValidPhone(phone)) {
                    editPhone.setError("Введите корректный номер телефона");
                } else {
                    editPhone.setError(null);
                }
            }
        });
    }

    /**
     * Загружает данные пользователя
     */
    private void loadUserData() {
        User user = userDao.getUserById(currentUserId);
        if (user != null) {
            textLogin.setText(user.getLogin());
            editName.setText(user.getName());
            editSurname.setText(user.getSurname());
            editBirthdate.setText(user.getBirthdate());
            editPhone.setText(PhoneValidator.formatPhone(user.getPhone()));

            // Устанавливаем пол
            if ("мужской".equalsIgnoreCase(user.getGender()) || "м".equalsIgnoreCase(user.getGender())) {
                radioMale.setChecked(true);
            } else if ("женский".equalsIgnoreCase(user.getGender()) || "ж".equalsIgnoreCase(user.getGender())) {
                radioFemale.setChecked(true);
            }
        } else {
            showToast("Ошибка загрузки данных");
        }
    }

    /**
     * Настраивает обработчики событий
     */
    private void setupListeners() {
        buttonSave.setOnClickListener(v -> saveUserData());
        buttonAbout.setOnClickListener(v -> showAboutDialog());
        buttonLogout.setOnClickListener(v -> logout());
    }

    /**
     * Показывает DatePicker для выбора даты рождения
     */
    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        try {
            if (!editBirthdate.getText().toString().isEmpty()) {
                calendar.setTime(dateFormatter.parse(editBirthdate.getText().toString()));
            }
        } catch (Exception e) {
            // Используем текущую дату по умолчанию
            calendar.set(2000, Calendar.JANUARY, 1); // Дата по умолчанию - 2000 год
        }

        DatePickerDialog datePicker = new DatePickerDialog(requireContext(),
                (view, year, month, dayOfMonth) -> {
                    birthdateCalendar.set(year, month, dayOfMonth);
                    editBirthdate.setText(dateFormatter.format(birthdateCalendar.getTime()));
                    editBirthdate.setError(null);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));

        // Устанавливаем максимальную дату - сегодня
        datePicker.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePicker.show();
    }

    /**
     * Валидирует дату рождения
     */
    private void validateBirthdate(String date) {
        try {
            Calendar selected = Calendar.getInstance();
            selected.setTime(dateFormatter.parse(date));
            Calendar today = Calendar.getInstance();

            if (selected.after(today)) {
                editBirthdate.setError("Дата рождения не может быть в будущем");
            } else {
                editBirthdate.setError(null);
            }
        } catch (Exception e) {
            editBirthdate.setError("Неверный формат даты");
        }
    }

    /**
     * Сохраняет данные пользователя
     */
    private void saveUserData() {
        if (!validateForm()) {
            return;
        }

        User user = userDao.getUserById(currentUserId);
        if (user != null) {
            user.setName(editName.getText().toString().trim());
            user.setSurname(editSurname.getText().toString().trim());
            user.setBirthdate(editBirthdate.getText().toString().trim());
            user.setPhone(editPhone.getText().toString().trim());
            user.setGender(getSelectedGender());

            int updated = userDao.updateUser(user);
            if (updated > 0) {
                showToast("Данные успешно обновлены");
            } else {
                showToast("Ошибка обновления данных");
            }
        }
    }

    /**
     * Валидирует форму перед сохранением
     */
    private boolean validateForm() {
        String name = editName.getText().toString().trim();
        String surname = editSurname.getText().toString().trim();
        String phone = editPhone.getText().toString().trim();

        // Проверка имени и фамилии
        if (name.isEmpty()) {
            editName.setError("Введите имя");
            editName.requestFocus();
            return false;
        }

        if (surname.isEmpty()) {
            editSurname.setError("Введите фамилию");
            editSurname.requestFocus();
            return false;
        }

        // Проверка формата имени и фамилии
        if (!name.matches("[а-яА-Яa-zA-Z-]+")) {
            editName.setError("Имя может содержать только буквы и дефис");
            editName.requestFocus();
            return false;
        }

        if (!surname.matches("[а-яА-Яa-zA-Z-]+")) {
            editSurname.setError("Фамилия может содержать только буквы и дефис");
            editSurname.requestFocus();
            return false;
        }

        // Проверка телефона (если указан)
        if (!phone.isEmpty() && !PhoneValidator.isValidPhone(phone)) {
            editPhone.setError("Введите корректный номер телефона");
            editPhone.requestFocus();
            return false;
        }

        // Проверка даты рождения (если указана)
        String birthdate = editBirthdate.getText().toString().trim();
        if (!birthdate.isEmpty()) {
            try {
                validateBirthdate(birthdate);
                if (editBirthdate.getError() != null) {
                    editBirthdate.requestFocus();
                    return false;
                }
            } catch (Exception e) {
                editBirthdate.setError("Неверный формат даты");
                editBirthdate.requestFocus();
                return false;
            }
        }

        return true;
    }

    /**
     * Получает выбранный пол
     */
    private String getSelectedGender() {
        int selectedId = radioGender.getCheckedRadioButtonId();
        if (selectedId == R.id.radio_male) {
            return "мужской";
        } else if (selectedId == R.id.radio_female) {
            return "женский";
        }
        return "";
    }

    /**
     * Показывает диалог "О приложении"
     */
    private void showAboutDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("О приложении")
                .setMessage("Тема проекта: Мобильное приложение салона красоты\nИсполнитель: Гришкова Виктория")
                .setPositiveButton("OK", null)
                .show();
    }

    /**
     * Выход из аккаунта
     */
    private void logout() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Выход")
                .setMessage("Вы уверены, что хотите выйти?")
                .setPositiveButton("Да", (dialog, which) -> {
                    sharedPreferences.edit().clear().apply();
                    navigateToLogin();
                })
                .setNegativeButton("Нет", null)
                .show();
    }

    /**
     * Переход на экран логина
     */
    private void navigateToLogin() {
        startActivity(new Intent(requireContext(), LoginActivity.class));
        requireActivity().finish();
    }

    /**
     * Показывает всплывающее уведомление
     */
    private void showToast(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }
    /**
     * Обновляет состояние Switch при возвращении на фрагмент
     */
    @Override
    public void onResume() {
        super.onResume();

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (switchNotifications != null) {
                boolean notificationsEnabled = NotificationScheduler.areNotificationsEnabled(requireContext());

                switchNotifications.setOnCheckedChangeListener(null);
                switchNotifications.setChecked(notificationsEnabled);
                setupSwitchListener();

                android.util.Log.d("ProfileFragment",
                        "Switch updated on resume to: " + notificationsEnabled);
            }
        }, 200);
    }

    /**
     * Сохраняет состояние фрагмента
     */
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        if (switchNotifications != null) {
            outState.putBoolean("notification_switch_state", switchNotifications.isChecked());
        }
    }

    /**
     * Восстанавливает состояние фрагмента
     */
    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

        if (savedInstanceState != null && switchNotifications != null) {
            boolean savedState = savedInstanceState.getBoolean("notification_switch_state", true);

            switchNotifications.setOnCheckedChangeListener(null);
            switchNotifications.setChecked(savedState);

            boolean actualState = NotificationScheduler.areNotificationsEnabled(requireContext());
            if (savedState != actualState) {
                switchNotifications.setChecked(actualState);
            }

            setupSwitchListener();
        }
    }
}