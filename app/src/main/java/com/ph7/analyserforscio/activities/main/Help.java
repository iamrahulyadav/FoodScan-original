package com.ph7.analyserforscio.activities.main;

import android.graphics.Paint;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

import com.ph7.analyserforscio.R;
import com.ph7.analyserforscio.activities.AppActivity;

public class Help extends AppActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        getSupportActionBar().setElevation(0f);


        final TextView tvEmailId1= (TextView) findViewById(R.id.tvEmailId1) ;
        tvEmailId1.setPaintFlags(tvEmailId1.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        tvEmailId1.setOnClickListener(click);
        final TextView tvEmailId2= (TextView) findViewById(R.id.tvEmailId2) ;
        tvEmailId2.setPaintFlags(tvEmailId2.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        tvEmailId2.setOnClickListener(click);


    }

    View.OnClickListener click =  new View.OnClickListener() {
        @Override
        public void onClick(View view) {
//            String emailId  =  ((TextView)view).getText().toString().trim() ;
//            Intent emailIntent = new Intent(Intent.ACTION_SEND);
//            emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{ emailId });
//            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "");
//            emailIntent.putExtra(Intent.EXTRA_TEXT, "");
//            startActivity(Intent.createChooser(emailIntent, "Select email client"));
        }
    };


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return false ;
    }
}
