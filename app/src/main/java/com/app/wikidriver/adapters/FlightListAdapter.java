package com.app.wikidriver.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.app.wikidriver.R;
import com.app.wikidriver.models.Flight;
import com.app.wikidriver.utils.Config;

import java.util.ArrayList;
import java.util.List;

public class FlightListAdapter extends RecyclerView.Adapter<FlightListAdapter.ViewHolder>{

    private Context context;
    private List<Flight> flightList = new ArrayList<>();

    public FlightListAdapter(Context context, List<Flight> flightList) {
        this.context = context;
        this.flightList = flightList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View itemView = inflater.inflate(R.layout.flight_item_layout, parent, false);
        return new FlightListAdapter.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Flight flight = flightList.get(position);

        holder.setIsRecyclable(false);

        long time = System.currentTimeMillis();
        long arrivalTime = flight.getArrivalTime() * 1000;
        String arrivalTimeString = Config.epochToDate(arrivalTime, "HH:mm");
        String departureTimeString = Config.epochToDate(flight.getDepartureTime() * 1000, "HH:mm");

        /*if(arrivalTime > time) {

        if(arrivalTime - time < (60 * 10 * 1000) && arrivalTime - time > (60 * 2 * 1000)) {
            holder.layout.setBackgroundColor(Color.YELLOW);
            holder.txtStartCityCode.setTextColor(Color.BLACK);
            holder.txtTrueArrivalTime.setTextColor(Color.BLACK);
        }
        else if(arrivalTime - time < 60 * 2 * 1000) {
            holder.layout.setBackgroundColor(Color.RED);
            holder.txtTrueArrivalTime.setTextColor(Color.BLACK);
            holder.txtStartCity.setTextColor(Color.WHITE);
            holder.txtStartCityCode.setTextColor(Color.WHITE);
        } else {
            holder.layout.setBackgroundColor(Color.GREEN);
            holder.txtTrueArrivalTime.setTextColor(Color.BLACK);
            holder.txtStartCityCode.setTextColor(Color.WHITE);
        }

        } else {
            holder.txtStatus.setText(context.getResources().getString(R.string.landed));
        }*/

        holder.txtStartCity.setText(flight.getStartCity());
        holder.txtStartCityCode.setText(flight.getStartCityCode());
        holder.txtArrivalTime.setText(arrivalTimeString);
        holder.txtDepartureTime.setText(departureTimeString);
        holder.txtEndCity.setText(flight.getEndCity());
        holder.txtEndCityCode.setText(flight.getEndCityCode());
        holder.txtFlightId.setText(flight.getFlightId());
    }
    @Override
    public int getItemCount() {
        return flightList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        //private TextView txtStartCity, txtStatus, txtStartCityCode, txtTrueArrivalTime, txtFlightId;
        //private LinearLayout layout;

        private TextView txtStartCity, txtEndCity, txtEndCityCode, txtStartCityCode, txtArrivalTime, txtFlightId, txtDepartureTime;
        public ViewHolder(View itemView) {
            super(itemView);

            /*layout = itemView.findViewById(R.id.layout);
            txtStatus = itemView.findViewById(R.id.txtStatus);
            txtStartCity = itemView.findViewById(R.id.txtStartCity);
            txtStartCityCode = itemView.findViewById(R.id.txtStartCityCode);
            txtTrueArrivalTime = itemView.findViewById(R.id.txtTrueArrivalTime);
            txtFlightId = itemView.findViewById(R.id.txtFlightId);*/

            txtStartCity = itemView.findViewById(R.id.txtStartCity);
            txtStartCityCode = itemView.findViewById(R.id.txtStartCityCode);
            txtEndCity = itemView.findViewById(R.id.txtEndCity);
            txtEndCityCode = itemView.findViewById(R.id.txtEndCityCode);
            txtArrivalTime = itemView.findViewById(R.id.txtArrivalTime);
            txtDepartureTime = itemView.findViewById(R.id.txtDepartureTime);
            txtFlightId = itemView.findViewById(R.id.txtFlightId);
        }
    }
}
