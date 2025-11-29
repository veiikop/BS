package com.example.bs.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.bs.R;
import com.example.bs.db.MasterDao;
import com.example.bs.db.ServiceDao;
import com.example.bs.model.Appointment;
import com.example.bs.model.Master;
import com.example.bs.model.Service;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.ViewHolder> {

    private List<Appointment> appointments = new ArrayList<>();
    private final Context context;
    private final ServiceDao serviceDao;
    private final MasterDao masterDao;

    private final SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

    public AppointmentAdapter(Context context) {
        this.context = context;
        this.serviceDao = new ServiceDao(context);
        this.masterDao = new MasterDao(context);
    }

    public void setAppointments(List<Appointment> appointments) {
        this.appointments = appointments != null ? appointments : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_appointment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Appointment appointment = appointments.get(position);
        Service service = serviceDao.getServiceById(appointment.getServiceId());
        Master master = masterDao.getMasterById(appointment.getMasterId());

        if (service != null && master != null) {
            holder.textService.setText(service.getName());
            holder.textMaster.setText(master.getName() + " " + master.getSurname());
            holder.textPrice.setText(String.format("%.0f ₽", appointment.getPrice()));

            try {
                Date date = inputFormat.parse(appointment.getDateTime());
                if (date != null) {
                    holder.textDate.setText(dateFormat.format(date));
                    holder.textTime.setText(timeFormat.format(date));
                }
            } catch (ParseException e) {
                String[] parts = appointment.getDateTime().split(" ");
                holder.textDate.setText(parts[0]);
                holder.textTime.setText(parts[1]);
            }

            if (appointment.getStatus().equals("future")) {
                holder.textStatus.setText("Будет");
                holder.textStatus.setBackgroundResource(R.drawable.status_badge_future);
            } else {
                holder.textStatus.setText("Прошла");
                holder.textStatus.setBackgroundResource(R.drawable.status_badge_past);
            }
        }
    }

    @Override
    public int getItemCount() {
        return appointments.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textService, textMaster, textDate, textTime, textPrice, textStatus;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            textService = itemView.findViewById(R.id.text_service);
            textMaster = itemView.findViewById(R.id.text_master);
            textDate = itemView.findViewById(R.id.text_date);
            textTime = itemView.findViewById(R.id.text_time);
            textPrice = itemView.findViewById(R.id.text_price);
            textStatus = itemView.findViewById(R.id.text_status);
        }
    }
}