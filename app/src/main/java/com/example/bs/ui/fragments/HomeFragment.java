package com.example.bs.ui.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;
import com.example.bs.R;
import com.google.android.material.button.MaterialButton;

/**
 * Фрагмент главной страницы с информацией, слайдером и кнопкой для связи.
 */
public class HomeFragment extends Fragment {

    private ViewPager2 viewPager;
    private Handler sliderHandler = new Handler(Looper.getMainLooper());
    private Runnable sliderRunnable;
    private static final long SLIDER_DELAY_MS = 7000; // 7 секунд задержка

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Настройка слайдера
        viewPager = view.findViewById(R.id.view_pager_slider);
        viewPager.setAdapter(new ImageSliderAdapter());

        // Автоматическая прокрутка
        sliderRunnable = new Runnable() {
            @Override
            public void run() {
                if (viewPager.getCurrentItem() == Integer.MAX_VALUE / 2) {
                    viewPager.setCurrentItem(0, true);
                } else {
                    viewPager.setCurrentItem(viewPager.getCurrentItem() + 1, true);
                }
                sliderHandler.postDelayed(this, SLIDER_DELAY_MS);
            }
        };
        sliderHandler.postDelayed(sliderRunnable, SLIDER_DELAY_MS);

        // Кнопка "Связаться с нами"
        MaterialButton buttonContact = view.findViewById(R.id.button_contact);
        buttonContact.setOnClickListener(v -> {
            String phoneNumber = "tel:+73532123456";
            Intent callIntent = new Intent(Intent.ACTION_DIAL, Uri.parse(phoneNumber));
            startActivity(callIntent);
        });

        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        sliderHandler.removeCallbacks(sliderRunnable); // Остановка при выходе
    }

    @Override
    public void onResume() {
        super.onResume();
        sliderHandler.postDelayed(sliderRunnable, SLIDER_DELAY_MS); // Возобновление
    }
}