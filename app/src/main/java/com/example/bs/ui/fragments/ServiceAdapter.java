package com.example.bs.ui.fragments;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.bs.R;
import com.example.bs.model.Service;

import java.util.List;

/**
 * Адаптер для отображения списка услуг.
 */
public class ServiceAdapter extends RecyclerView.Adapter<ServiceAdapter.ServiceViewHolder> {

    private List<Service> services;

    public ServiceAdapter(List<Service> services) {
        this.services = services;
    }

    @NonNull
    @Override
    public ServiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_service, parent, false);
        return new ServiceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ServiceViewHolder holder, int position) {
        Service service = services.get(position);
        holder.textName.setText(service.getName());
        holder.textPrice.setText(String.format("%.2f руб", service.getPrice()));
        holder.textDuration.setText(String.format("%d мин", service.getDuration()));

    }

    @Override
    public int getItemCount() {
        return services != null ? services.size() : 0;
    }

    public void updateList(List<Service> newServices) {
        this.services = newServices;
        notifyDataSetChanged();
    }

    static class ServiceViewHolder extends RecyclerView.ViewHolder {
        TextView textName, textPrice, textDuration;

        ServiceViewHolder(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.text_service_name);
            textPrice = itemView.findViewById(R.id.text_service_price);
            textDuration = itemView.findViewById(R.id.text_service_duration);
        }
    }
}