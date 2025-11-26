package com.example.bs.ui.fragments;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.bs.R;

/**
 *  адаптер для слайдера фотографий услуг
 */
public class ImageSliderAdapter extends RecyclerView.Adapter<ImageSliderAdapter.SliderViewHolder> {

    private final int[] imageResIds = {
            R.drawable.salon1,
            R.drawable.salon2,
            R.drawable.salon3
    };

    // Заголовки для каждого слайда
    private final String[] slideTitles = {
            "Профессиональный макияж",
            "Стильные стрижки",
            "Уход за кожей"
    };

    @NonNull
    @Override
    public SliderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_slider_image, parent, false);
        return new SliderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SliderViewHolder holder, int position) {
        int actualPosition = position % imageResIds.length;

        // Простая загрузка изображения
        holder.imageView.setImageResource(imageResIds[actualPosition]);
        holder.textTitle.setText(slideTitles[actualPosition]);
    }

    @Override
    public int getItemCount() {
        return Integer.MAX_VALUE; // бесконечная прокрутка
    }

    public int getRealItemCount() {
        return imageResIds.length;
    }

    static class SliderViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        com.google.android.material.textview.MaterialTextView textTitle;

        SliderViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_slider);
            textTitle = itemView.findViewById(R.id.text_slide_title);
        }
    }
}