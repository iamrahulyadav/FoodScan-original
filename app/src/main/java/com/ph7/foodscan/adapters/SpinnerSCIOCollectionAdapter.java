package com.ph7.foodscan.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.ph7.foodscan.R;
import com.ph7.foodscan.models.ph7.ScioCollection;
import com.ph7.foodscan.models.ph7.ScioCollectionModel;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by craigtweedy on 05/07/2016.
 */
public class SpinnerSCIOCollectionAdapter extends ArrayAdapter<ScioCollection> {

    private final int layout;
    private Context context;
    private List<ScioCollection> collections;
    private LayoutInflater layoutInflator;

    public SpinnerSCIOCollectionAdapter(Context context, int resource, List<ScioCollection> objects) {
        super(context, resource, objects);
        this.context = context;
        this.layout = resource;
        this.collections = objects;
        this.layoutInflator = LayoutInflater.from(context);
        if(getCount()<=0)
        {
            ScioCollection scioCollection = new ScioCollection("Choose a collection");
            Collections.sort(objects, new Comparator<ScioCollection>() {
                @Override
                public int compare(ScioCollection o1, ScioCollection o2) {
                    return o1.getName().compareTo(o2.getName());
                }
            });
            this.add(scioCollection);
        }

    }

    public int getCount(){
        return this.collections.size();
    }

    public ScioCollection getItem(int position){
        return this.collections.get(position);
    }

    public long getItemId(int position){
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {


        convertView = layoutInflator.inflate(this.layout, null);
        TextView label = (TextView)convertView.findViewById(R.id.tvName);

        label.setText(this.collections.get(position).getName());
        // Change By atul
        if(position>0) {
            label.setTextColor(Color.BLACK);
        }else label.setTextColor(Color.parseColor("#ffa0a09e"));
        //
        return label;
    }

    // And here is when the "chooser" is popped up
    // Normally is the same view, but you can customize it if you want
    @Override
    public View getDropDownView(int position, View convertView,
                                ViewGroup parent) {
        convertView = layoutInflator.inflate(this.layout, null);
        TextView label = (TextView)convertView.findViewById(R.id.tvName);

        label.setText(this.collections.get(position).getName());
        if(position>0) {
            label.setTextColor(Color.BLACK);
        }else label.setTextColor(Color.parseColor("#ffa0a09e"));

        return label;
    }
}
