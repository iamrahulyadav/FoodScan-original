package com.ph7.foodscan.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.ph7.foodscan.R;
import com.ph7.foodscan.models.ph7.ScioCollectionModel;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by craigtweedy on 05/07/2016.
 */
public class SpinnerSCIOModelAdapter extends ArrayAdapter<ScioCollectionModel> {

    private final int layout;
    private Context context;
    private List<ScioCollectionModel> models;
    private LayoutInflater layoutInflator;

    public SpinnerSCIOModelAdapter(Context context, int resource, List<ScioCollectionModel> objects) {
        super(context, resource, objects);
        this.context = context;
        this.layout = resource;
        this.models = objects;
        this.layoutInflator = LayoutInflater.from(context);

        // Change By atul
        if(getCount()<=0)
        {
            ScioCollectionModel scioCollectionModel = new ScioCollectionModel("Choose a model");
            this.add(scioCollectionModel);
        }
        //
    }

    public int getCount(){
        return this.models.size();
    }

    public ScioCollectionModel getItem(int position){
        return this.models.get(position);
    }

    public long getItemId(int position){
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        convertView = layoutInflator.inflate(this.layout, null);
        TextView label = (TextView)convertView.findViewById(R.id.tvName);

        label.setText(this.models.get(position).getName());
        // Change By atul
        if(position>0) {
            label.setTextColor(Color.BLACK);
        }else {

            label.setTextColor(Color.parseColor("#ffa0a09e"));
        }
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

        label.setText(this.models.get(position).getName());

        if(position>0) {
            label.setTextColor(Color.BLACK);
       /*     Changed by Muhib on 26/10/2017*/
            Collections.sort(models.subList(1,models.size()), new Comparator<ScioCollectionModel>() {
                @Override
                public int compare(ScioCollectionModel o1, ScioCollectionModel o2) {
                    return o1.getName().compareTo(o2.getName());
                }
            });
/*sorting*/
        }
        else label.setTextColor(Color.parseColor("#ffa0a09e"));

//        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
//        Display display = wm.getDefaultDisplay();
//        Point size = new Point();
//        display.getSize(size);
//        int _width = size.x;

        label.setWidth(parent.getWidth());
        return label;
    }
}
