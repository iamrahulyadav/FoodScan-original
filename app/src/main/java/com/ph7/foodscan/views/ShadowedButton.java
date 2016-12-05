package com.ph7.foodscan.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.widget.Button;

import com.ph7.foodscan.R;
import com.ph7.foodscan.application.FoodScanApplication;

/**
 * Created by craigtweedy on 04/07/2016.
 */
public class ShadowedButton extends Button {

    LayerDrawable backgroundDrawable;
    GradientDrawable foreground;
    GradientDrawable background;
    AttributeSet attrs ;

    public ShadowedButton(Context context) {
        super(context);
        configure(null);
    }

    public ShadowedButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray options = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.ShadowedButton,
                0, 0);

        configure(options);
        this.attrs = attrs ;
    }

    @Override
    public void setTextAppearance(int resId) {
        //super.setTextAppearance(resId);
        setThemeStyle(resId);
    }

    @Override
    public void setTextAppearance(Context context, int resId) {
        //super.setTextAppearance(context, resId);
        setThemeStyle(resId);
    }

    private void setThemeStyle(int resId) {
        getContext().setTheme(resId);
        TypedArray options = getContext().getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.ShadowedButton,
                0, 0);
        configure(options);
    }

    private void loadDrawable() {
        this.backgroundDrawable = (LayerDrawable) ContextCompat.getDrawable(FoodScanApplication.getAppContext(), R.drawable.shadowed_button_style);
        this.foreground = (GradientDrawable) this.backgroundDrawable.findDrawableByLayerId(R.id.foreground);
        this.background = (GradientDrawable) this.backgroundDrawable.findDrawableByLayerId(R.id.background);
    }

    private void configure(TypedArray options) {
        if (isInEditMode()) {
            return;
        }

        loadDrawable();
        if (options != null) {
            configureDrawable(options);
        }

        this.setBackground(this.backgroundDrawable);
        invalidate();
        requestLayout();
    }

    private void configureDrawable(TypedArray options) {
        try {
            foreground.setColor(options.getColor(R.styleable.ShadowedButton_foreground_color, getResources().getColor(R.color.white)));
            background.setColor(options.getColor(R.styleable.ShadowedButton_background_color, getResources().getColor(R.color.white)));
        } finally {
            options.recycle();
        }

    }
}
