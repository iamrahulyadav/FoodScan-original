package com.ph7.foodscan.views;

import android.app.Dialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;

import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.view.Display;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ph7.foodscan.R;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by sony on 29-07-2016.
 */
public class StatusView extends LinearLayout {

    private ImageView statusImage ;
    //private Button btstatus ;
    private TextView tvStatusMsg,btstatus ;
    private GifDecoderView gifDecoderView ;
    private int statusCode ;
    private static boolean  isStatusOpen = false ;
    private String statusMsg ;

    public StatusView(Context context) {
        super(context);
        init();
    }



    public StatusView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray typedArray = context.obtainStyledAttributes(R.styleable.StatusView);
        statusMsg = typedArray.getString(R.styleable.StatusView_status_msg) ;
        statusCode = typedArray.getInt(R.styleable.StatusView_status_code,0);
        typedArray.recycle();
        init();

    }

    public StatusView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init()
    {
        statusImage = new ImageView(getContext());
        btstatus=new TextView(getContext());
        /*****************/
        InputStream stream = null;
        try {
            stream = getContext().getAssets().open("scan.gif");
        } catch (IOException e) {
            e.printStackTrace();
        }
        gifDecoderView = new GifDecoderView(getContext(),stream);
        gifDecoderView.setLayoutParams(new LayoutParams(300,300));
        /******************/
        tvStatusMsg = new TextView(getContext());
        this.addView(statusImage);
        this.addView(gifDecoderView);
        this.addView(tvStatusMsg);
        this.addView(btstatus);


        setStatusCode(statusCode);
        setStatusMessage(statusMsg);
        setOrientation(VERTICAL);
        setGravity(Gravity.CENTER);
        //setElevation(1.0f);
    }
    private Dialog alertDialog ;
    public void hide()
    {
        gifDecoderView.stopRendering();
        gifDecoderView = null ;
        alertDialog.dismiss();
        isStatusOpen = false ;
    }
    public void show()
    {
        if(!isStatusOpen) {
            alertDialog = new Dialog(getContext());
            alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            //alertDialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
            WindowManager.LayoutParams wmlp = alertDialog.getWindow().getAttributes();

            wmlp.width = getMeasuredWidth();
            wmlp.height = getMeasuredHeight();

            /*WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            Point size = new Point();
            display.getRealSize(size);
            wmlp.width = size.x ;
            wmlp.height = size.y ;*/
            wmlp.gravity = Gravity.CENTER;

            this.setFitsSystemWindows(true);
            alertDialog.setContentView(this);
            alertDialog.setCancelable(false);
            setBGColor();
            // alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            alertDialog.getWindow().getAttributes().windowAnimations = R.style.ShowViewAnimation;
            alertDialog.show();
            isStatusOpen = true;
        }
    }

    public void setBGColor()
    {
        switch (this.statusCode) {
            case StatusType.SUCCESS:
                alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#b23daf64")));
                break;

            case StatusType.FAILURE:
                alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#b2c0181c")));
                break;

            case StatusType.SCANNING:
                alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#B2616363")));
                break;

            case StatusType.ANALYZING:
                alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#B2616363")));
                break;
        }
    }

    @Override
    protected void onLayout(boolean b, int i, int i1, int i2, int i3) {
        super.onLayout(b,0,0,getMeasuredWidth()-i,getMeasuredHeight()-i1);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        Display displayDevice= this.getDisplay();
        int wid=  displayDevice.getWidth();
        int hit=displayDevice.getHeight();

        /*WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getRealSize(size);
        int wid=  size.x ;
        int hit= size.y;*/

        setMeasuredDimension(wid,hit);
    }

    Animation myAnim ;
    /********************
     * Status.SUCCESS
     * Status.FAILURE
     * *******************/
    public void setStatusCode(int statusCode)
    {
        LinearLayout.LayoutParams llp=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        llp.setMargins(0,45,0,0);
        if(myAnim!=null)
        {
            myAnim.cancel();
            myAnim.reset();
            statusImage.clearAnimation();
        }
        this.statusCode = statusCode;
        switch(this.statusCode)
        {
            case StatusType.SUCCESS :
                gifDecoderView.setVisibility(GONE);
                statusImage.setVisibility(VISIBLE);
                statusImage.setImageResource(R.drawable.success);
                btstatus.setVisibility(VISIBLE);
                btstatus.setText("Continue");
                btstatus.setTextColor(Color.parseColor("#008e32"));
                btstatus.setBackgroundColor(Color.parseColor("#FFFFFF"));
                 btstatus.setPadding(55,30,55,30);
                btstatus.setTextSize(18.0f);
                btstatus.setTypeface(null, Typeface.BOLD);
                btstatus.setLayoutParams(llp);
                //  setBackgroundColor(Color
                // .parseColor("#b23daf64"));
                setBackgroundColor(Color.TRANSPARENT);
                break ;

            case StatusType.FAILURE:
                gifDecoderView.setVisibility(GONE);
                statusImage.setVisibility(VISIBLE);
                statusImage.setImageResource(R.drawable.failure);
                btstatus.setVisibility(VISIBLE);
                btstatus.setText("Continue");
                btstatus.setTextColor(Color.parseColor("#008e32"));
                btstatus.setBackgroundColor(Color.parseColor("#FFFFFF"));
                btstatus.setPadding(55,30,55,30);
                btstatus.setTextSize(18.0f);
                btstatus.setTypeface(null, Typeface.BOLD);
                btstatus.setLayoutParams(llp);


                // setBackgroundColor(Color.parseColor("#b2c0181c"));
                setBackgroundColor(Color.TRANSPARENT);
                break ;

            case StatusType.SCANNING :
                statusImage.setVisibility(GONE);
                btstatus.setVisibility(GONE);
                gifDecoderView.setVisibility(VISIBLE);
                setBackgroundColor(Color.TRANSPARENT);
                break ;

            case StatusType.ANALYZING :
                statusImage.setVisibility(VISIBLE);
                btstatus.setVisibility(GONE);
                gifDecoderView.setVisibility(GONE);
                statusImage.setImageResource(R.drawable.hax);
                myAnim = AnimationUtils.loadAnimation(getContext(), R.anim.pumping);
                statusImage.startAnimation(myAnim);
                setBackgroundColor(Color.TRANSPARENT);
                break ;
        }

    }
    public void setStatusMessage(String msg)
    {
        this.statusMsg =  msg ;
        tvStatusMsg.setText(msg);
        tvStatusMsg.setPadding(0,20,0,10);
        tvStatusMsg.setTextSize(18.0f);
        tvStatusMsg.setTextColor(Color.parseColor("#FFFFFF"));
        tvStatusMsg.setTypeface(null, Typeface.BOLD);
    }


}

interface StatusType
{
    int SUCCESS = 1 ;
    int FAILURE = 0 ;
    int SCANNING = 2 ;
    int ANALYZING = 3 ;
}
