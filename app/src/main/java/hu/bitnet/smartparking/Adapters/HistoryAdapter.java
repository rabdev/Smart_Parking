package hu.bitnet.smartparking.Adapters;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import hu.bitnet.smartparking.R;
import hu.bitnet.smartparking.objects.History;

import static android.content.ContentValues.TAG;
import static java.lang.Long.parseLong;

/**
 * Created by nyulg on 2017. 08. 05..
 */

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {
    private ArrayList<History> android;

    public HistoryAdapter(ArrayList<History> android) {
        this.android = android;
    }


    @Override
    public HistoryAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_history, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(HistoryAdapter.ViewHolder viewHolder, int i) {

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy. MMMM dd. kk:mm");

        // Create a calendar object that will convert the date and time value in milliseconds to date.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(parseLong(android.get(i).getSum().getStart()+"000"));
        Log.d(TAG, "calendar: "+calendar);

        /*Calendar calendar2 = Calendar.getInstance();
        calendar2.setTimeInMillis(parseLong(android.get(i).getStoppedAt()+"000"));
        Log.d(TAG, "calendar: "+calendar2);*/

        viewHolder.tv_address.setText(android.get(i).getZone().getAddress().toString() + " ("+android.get(i).getZone().getId().toString() + ". zóna)");
        viewHolder.tv_priceper.setText(android.get(i).getSum().getPrice().toString()+" Ft ("+android.get(i).getZone().getPrice().toString()+" Ft/óra)");
        //viewHolder.tv_price.setText(android.get(i).getSum().getPrice().toString()+ " Ft");
        //viewHolder.tv_timestamp.setText(formatter.format(calendar.getTime()).toString() +" - "+ formatter.format(calendar2.getTime()).toString());
        Log.d(TAG, "time: "+String.valueOf((int) Math.ceil(Double.parseDouble(android.get(i).getSum().getTime())/60.0)));
        viewHolder.tv_time.setText(String.valueOf((int) Math.ceil(Double.parseDouble(android.get(i).getSum().getTime())/60.0)) + " perc");
    }

    @Override
    public int getItemCount() {
        return android.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private TextView tv_address,tv_priceper,tv_price, tv_timestamp, tv_time;
        public ViewHolder(View view) {
            super(view);

            tv_address = (TextView)view.findViewById(R.id.history_address);
            tv_priceper = (TextView)view.findViewById(R.id.history_priceper);
            tv_price = (TextView)view.findViewById(R.id.history_price);
            tv_timestamp = (TextView)view.findViewById(R.id.history_timestamp);
            tv_time= (TextView) view.findViewById(R.id.history_time);

        }
    }
}

