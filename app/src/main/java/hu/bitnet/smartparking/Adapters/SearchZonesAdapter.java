package hu.bitnet.smartparking.Adapters;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import hu.bitnet.smartparking.R;
import hu.bitnet.smartparking.objects.Addresses;

import static android.content.ContentValues.TAG;

/**
 * Created by nyulg on 2017. 08. 05..
 */

public class SearchZonesAdapter extends RecyclerView.Adapter<SearchZonesAdapter.ViewHolder> {
    private ArrayList<Addresses> android;
    private static SearchZonesAdapter.ClickListener clickListener;

    public SearchZonesAdapter(ArrayList<Addresses> android) {
        this.android = android;
    }


    @Override
    public SearchZonesAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_zones, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(SearchZonesAdapter.ViewHolder viewHolder, int i) {

        viewHolder.tv_address.setText(android.get(i).getAddress());
        Log.d(TAG, "cím: "+android.get(i).getAddress());
        viewHolder.tv_count.setText(String.valueOf(android.size()));
        viewHolder.tv_priceper.setText(String.format("%.0f", Double.parseDouble(android.get(i).getPrice())) + " Ft/óra");
        //viewHolder.tv_km.setText(String.format("%.1f", Double.parseDouble(android.get(i).getDistance())/1000.0)+" km");
        //viewHolder.tv_traffic.setText(String.format("%.1f", Double.parseDouble(android.get(i).getTime()))+" min without traffic");
        //viewHolder.tv_count.setText(android.get(i).getFreePlaces());*/
    }

    @Override
    public int getItemCount() {
        return android.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener{
        private TextView tv_km, tv_priceper, tv_address, tv_traffic, tv_count;

        public ViewHolder(View view) {
            super(view);

            tv_address = (TextView) view.findViewById(R.id.zones_address);
            tv_priceper = (TextView) view.findViewById(R.id.zones_priceper);
            tv_km = (TextView) view.findViewById(R.id.zones_distance);
            tv_traffic = (TextView) view.findViewById(R.id.zones_traffic);
            tv_count = (TextView) view.findViewById(R.id.zones_count);

            view.setOnClickListener(this);
            view.setOnLongClickListener(this);

        }

        @Override
        public void onClick(View v) {
            clickListener.onItemClick(getAdapterPosition(), v);
        }

        @Override
        public boolean onLongClick(View v) {
            clickListener.onItemLongClick(getAdapterPosition(), v);
            return false;
        }
    }

    public void setOnItemClickListener(SearchZonesAdapter.ClickListener clickListener) {
        SearchZonesAdapter.clickListener = clickListener;
    }

    public interface ClickListener {
        void onItemClick(int position, View v);
        void onItemLongClick(int position, View v);
    }
}