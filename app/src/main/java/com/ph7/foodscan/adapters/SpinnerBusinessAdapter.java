package com.ph7.foodscan.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.ph7.foodscan.R;
import com.ph7.foodscan.models.ph7.Business;

import java.util.List;

/**
 * Created by craigtweedy on 05/07/2016.
 */
public class SpinnerBusinessAdapter extends ArrayAdapter<Business> {

    private final int layout;
    private Context context;
    private List<Business> models;
    private LayoutInflater layoutInflator;

    public SpinnerBusinessAdapter(Context context, int resource, List<Business> objects) {
        super(context, resource, objects);
        this.context = context;
        this.layout = resource;
        this.models = objects;
        this.layoutInflator = LayoutInflater.from(context);
    }

    public int getCount(){
        return this.models.size();
    }

    public Business getItem(int position){
        return this.models.get(position);
    }

    public long getItemId(int position){
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView==null)
            convertView = layoutInflator.inflate(this.layout, null);
        TextView tvHeading = (TextView)convertView.findViewById(R.id.tvHeading);
        TextView tvSubheading = (TextView)convertView.findViewById(R.id.tvSubheading);
        tvHeading.setText(this.models.get(position).getName());
        tvSubheading.setText(this.models.get(position).getAddress());
        return convertView;
    }

    // And here is when the "chooser" is popped up
    // Normally is the same view, but you can customize it if you want
    @Override
    public View getDropDownView(int position, View convertView,
                                ViewGroup parent) {
        convertView = layoutInflator.inflate(this.layout, null);
        TextView label = (TextView)convertView.findViewById(R.id.tvName);

        label.setText(this.models.get(position).getName());
        label.setTextColor(Color.BLACK);
        label.setTextSize(16f);
        label.setPadding(0,5,5,5);
        return label;
    }
}
