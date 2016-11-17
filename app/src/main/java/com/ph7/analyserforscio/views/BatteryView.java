package com.ph7.analyserforscio.views;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import com.ph7.analyserforscio.R;

/**
 * Created by Atul Kumar Gupta  on 04-08-2016.
 */
public class BatteryView extends RelativeLayout {

    private int percent ;
    private View batteryStatusView ,batteryTop ;
    public BatteryView(Context context) {
        super(context);
        init();
    }

    public BatteryView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BatteryView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void setBatteryStatus(int percent)
    {
        this.percent =  percent ;
      //  Log.d("BatteryView","Battery : "+percent+"%");
        if(percent<=15)
            batteryStatusView.setBackgroundColor(Color.parseColor("#d2585b"));
        else if(percent>15 && percent<=50)
            batteryStatusView.setBackgroundColor(Color.parseColor("#f0b130"));
        else
            batteryStatusView.setBackgroundColor(Color.parseColor("#3daf64"));

        // Set back Ground Color on basis of Percent
        configureBatteryStatus();

        invalidate();
    }

    private void configureBatteryStatus() {
        if(percent>=0 && percent<=100) {
            LayoutParams params = (LayoutParams) batteryStatusView.getLayoutParams();
            int batteryTopWidth = batteryTop.getMeasuredWidth();
            int batteryStatusView_pl = batteryStatusView.getPaddingLeft()+4;
            int batteryStatusView_pr = batteryStatusView.getPaddingRight()+4;
            int totalBatteryStatusViewWidth = getMeasuredWidth() - batteryTopWidth - batteryStatusView_pl - batteryStatusView_pr;
            int width = percent * totalBatteryStatusViewWidth / 100;
            params.width = width;
        //    Log.d("BatteryView","BatteryStatusWidth : "+width);
            batteryStatusView.setLayoutParams(params);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

        LayoutParams params = (LayoutParams) batteryTop.getLayoutParams() ;
        params.height = getMeasuredHeight()*5/8 ;
        batteryTop.setLayoutParams(params);
        super.onLayout(changed, l, t, r, b);
       // Log.d("BatteryView","onLayout : width:"+getMeasuredWidth()+" | Height: "+getMeasuredHeight());

        //configureBatteryStatus();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
       // configureBatteryStatus();
    }

    private void init()    {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        inflater.inflate(R.layout.battery_item,this);
        batteryStatusView =  this.findViewById(R.id.batteryStatusView);
        batteryTop =  this.findViewById(R.id.batteryTop);
    }
}
