package com.example.bishnu.datasyncapp;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.amazonaws.mobileconnectors.cognito.Record;

import java.util.List;

/**
 * Created by Bishnu.Reddy on 9/19/2017.
 */

public class EvenListAdapter extends RecyclerView.Adapter<EvenListAdapter.MyViewHolder> {

    public List<Record> eventRecordList;
    public Context context;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView listItemEvent;
        private TextView listItemTiming;

        public MyViewHolder(View itemView) {
            super(itemView);
            listItemEvent = (TextView) itemView.findViewById(R.id.list_item_event);
            listItemTiming = (TextView) itemView.findViewById(R.id.list_item_timing);
        }
    }

    public EvenListAdapter(List<Record> eventRecordList, Context context) {
        this.eventRecordList = eventRecordList;
        this.context = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.event_list_item_layout, null, false));
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.listItemEvent.setText(eventRecordList.get(position).getKey());
        holder.listItemTiming.setText(eventRecordList.get(position).getValue());
    }

    @Override
    public int getItemCount() {
        return eventRecordList.size();
    }


}
