package com.example.media;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class ConnectAdapter extends ArrayAdapter<Connect> {
    private int resourceId;
    public ConnectAdapter(Context context, int textViewResourceId, List<Connect> objects){
        super(context, textViewResourceId, objects);
        resourceId = textViewResourceId;
    }
    @Override
    public View getView(int position,View convertView, ViewGroup parent){
        Connect connect = getItem(position);
        View view = LayoutInflater.from(getContext()).inflate(resourceId,parent,false);
        TextView connectName = (TextView) view.findViewById(R.id.name);
        TextView connectPhone = (TextView) view.findViewById(R.id.phone);
        connectName.setText(connect.getName());
        connectPhone.setText(connect.getPhone());
        return view;
    }
}
