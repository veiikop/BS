package com.example.bs.ui.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.fragment.app.Fragment;
import com.example.bs.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

/**
 *  фрагмент главной страницы
 */
public class HomeFragment extends Fragment {

    private int currentImageIndex = 0;
    private final int[] salonImages = {
            R.drawable.salon1,
            R.drawable.salon2,
            R.drawable.salon3
    };
    private ImageView mainImageView;
    private android.os.Handler imageSliderHandler;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        initViews(view);
        setupImageSlider();
        setupQuickActions(view);

        return view;
    }

    private void initViews(View view) {
        mainImageView = view.findViewById(R.id.image_logo);
    }

    private void setupImageSlider() {
        if (mainImageView != null) {
            imageSliderHandler = new android.os.Handler();
            startImageSlider();
        }
    }

    private void startImageSlider() {
        imageSliderHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mainImageView != null && isAdded()) {
                    currentImageIndex = (currentImageIndex + 1) % salonImages.length;
                    mainImageView.setImageResource(salonImages[currentImageIndex]);
                    imageSliderHandler.postDelayed(this, 3000); // Смена каждые 3 секунды
                }
            }
        }, 3000);
    }

    private void setupQuickActions(View view) {
        // Карточка "Записаться на услугу"
        MaterialCardView cardBookService = view.findViewById(R.id.card_quick_book);
        if (cardBookService != null) {
            cardBookService.setOnClickListener(v -> navigateToCatalog());
        }

        // Карточка "Мои записи"
        MaterialCardView cardMyAppointments = view.findViewById(R.id.card_my_appointments);
        if (cardMyAppointments != null) {
            cardMyAppointments.setOnClickListener(v -> navigateToAppointments());
        }

        // Карточка "Акции"
        MaterialCardView cardPromotions = view.findViewById(R.id.card_promotions);
        if (cardPromotions != null) {
            cardPromotions.setOnClickListener(v -> showPromotionsDialog());
        }

        // Кнопка "Позвонить"
        MaterialButton buttonCall = view.findViewById(R.id.button_contact);
        if (buttonCall != null) {
            buttonCall.setOnClickListener(v -> callSalon());
        }

        // Кнопка "Как добраться"
        MaterialButton buttonLocation = view.findViewById(R.id.button_location);
        if (buttonLocation != null) {
            buttonLocation.setOnClickListener(v -> openLocation());
        }
    }

    private void navigateToCatalog() {
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new CatalogFragment())
                .addToBackStack(null)
                .commit();
    }

    private void navigateToAppointments() {
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new AppointmentsFragment())
                .addToBackStack(null)
                .commit();
    }

    private void showPromotionsDialog() {
        new android.app.AlertDialog.Builder(requireContext())
                .setTitle("🎉 Акции месяца")
                .setMessage("• Стрижка + укладка = 1500₽\n• Маникюр + педикюр = 2000₽\n• Приведи подругу - скидка 20%\n• Именинникам скидка 25%")
                .setPositiveButton("Записаться", (dialog, which) -> navigateToCatalog())
                .setNegativeButton("Закрыть", null)
                .show();
    }

    private void callSalon() {
        String phoneNumber = "tel:+73532123456";
        Intent callIntent = new Intent(Intent.ACTION_DIAL, Uri.parse(phoneNumber));
        startActivity(callIntent);
    }

    private void openLocation() {
        Uri gmmIntentUri = Uri.parse("geo:0,0?q=Оренбург, ул. Кирова, 123");
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        if (mapIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
            startActivity(mapIntent);
        } else {
            // Если Google Maps не установлен, открываем в браузере
            Uri webUri = Uri.parse("https://maps.google.com/?q=Оренбург, ул. Кирова, 123");
            Intent webIntent = new Intent(Intent.ACTION_VIEW, webUri);
            startActivity(webIntent);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (imageSliderHandler != null) {
            imageSliderHandler.removeCallbacksAndMessages(null);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (imageSliderHandler != null) {
            startImageSlider();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (imageSliderHandler != null) {
            imageSliderHandler.removeCallbacksAndMessages(null);
        }
    }
}