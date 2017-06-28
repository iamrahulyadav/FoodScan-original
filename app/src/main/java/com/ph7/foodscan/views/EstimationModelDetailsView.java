package com.ph7.foodscan.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ph7.foodscan.R;
import com.ph7.foodscan.services.FCDBService;
import com.ph7.foodscan.services.SessionService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by sony on 29-08-2016.
 */
public class EstimationModelDetailsView extends LinearLayout {
    private GridLayout modelRecords ;
    ImageView ivEstimationGraph;
    private TextView tvModelName;
    private final String baseUrl = "https://api.foodscan.co.uk" ;
    private final Context context;

    public EstimationModelDetailsView(Context context) {
        super(context);
        this.context = context ;
        init();
    }



    public EstimationModelDetailsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context ;
        init();
    }

    public EstimationModelDetailsView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context ;
        init();
    }
    private void init() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        inflater.inflate(R.layout.estimation_model_detail_view,this);
        tvModelName =(TextView) this.findViewById(R.id.tvModelName);
        ivEstimationGraph =(ImageView) this.findViewById(R.id.ivEstimationGraph);
        modelRecords = (GridLayout) this.findViewById(R.id.modelRecords);
        setOrientation(VERTICAL);
    }

    private float totalConfidence =  0.0f ;
    private HashMap <String,Float> results = new HashMap<>();
    public void setModelRecords(ArrayList<String> modelRows,String name)
    {

        tvModelName.setText(name);
        final int scans  = modelRows.size();
        totalConfidence =  100.0f * scans ;

        for (int indexTableRow = 0; indexTableRow < scans ; indexTableRow++) {
            String jsonString =  modelRows.get(indexTableRow);
            double min = 0.0 ;
            double max = 0.0 ;
            try {
                JSONObject jObj  = new JSONObject(jsonString);
                JSONArray attributesArr = jObj.getJSONArray("attributes") ;
                for (int indexAttributeObj = 0; indexAttributeObj < attributesArr.length(); indexAttributeObj++) {
                    JSONObject attributeObj  = attributesArr.getJSONObject(indexAttributeObj);
                    double value  =  attributeObj.getDouble("value");

                    if(value<min)  min = value ;
                    if(value>max) max =  value ;

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            // setupRow(indexTableRow+1,min,max);
        }

    }

    public void  setModelRecords(String modelRows,final String collectionId,final String test_id)
    {
        if(!modelRows.isEmpty())
        {
            try {
                JSONObject rootJobj  =  new JSONObject(modelRows);
                JSONObject modelJObj  =  rootJobj.getJSONObject("model");
                String modelName  =  modelJObj.getString("name");
                final String modelID  =  modelJObj.getString("uuid");
                tvModelName.setText(modelName);

                String png  = rootJobj.getString("png");
                final String graphUrl  =   baseUrl+png ;
                new AsyncTask<Void,Void,Bitmap>()
                {
                    @Override
                    protected void onPostExecute(Bitmap aVoid) {
                        super.onPostExecute(aVoid);
                        if(aVoid!=null)
                        ivEstimationGraph.setImageBitmap(aVoid);
                    }

                    @Override
                    protected Bitmap doInBackground(Void... voids) {
                        Bitmap bitmap = null;
                        try {
                            URL url  = new URL(graphUrl) ;
                            HttpURLConnection connection = (HttpURLConnection) url.openConnection() ;
                            connection.setRequestMethod("GET");
                            StringBuilder sb = new StringBuilder();
                            sb.append(URLEncoder.encode("Content-Type", "UTF-8")).append('=').append(URLEncoder.encode("application/x-www-form-urlencoded", "UTF-8"));
                            sb.append(URLEncoder.encode("Authorization", "UTF-8")).append('=').append(URLEncoder.encode("Bearer "+ new SessionService().getUserToken(), "UTF-8"));
                            connection.addRequestProperty("Content-Type","application/x-www-form-urlencoded");
                            connection.addRequestProperty("Authorization","Bearer "+ new SessionService().getUserToken());

                            bitmap = BitmapFactory.decodeStream(connection.getInputStream());

                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        return bitmap;
                    }
                }.execute(null,null,null);

               // Picasso.with(getContext()).load(graphUrl).into(ivEstimationGraph);
                JSONObject outputJObj =  rootJobj.getJSONObject("output");
                String min  = outputJObj.getString("min");
                String max = outputJObj.getString("max");
                String value = outputJObj.getString("value");

               // setupRow(0,min,max,value);
                setupAggregate(min,max,value);
//                JSONArray inputsJArr  =  rootJobj.getJSONArray("input");
//                for (int indexInput = 0; indexInput < inputsJArr.length(); indexInput++) {
//
//                    JSONObject inputJObj =  inputsJArr.getJSONObject(indexInput);
//                    min = inputJObj.getString("min");
//                    max =  inputJObj.getString("max");
//                    value  =  inputJObj.getString("value");
//
//                    setupRow(indexInput+1,min,max,value);
//                }
            } catch (JSONException e) {



                e.printStackTrace();
            }
        }
        updateViewStatus(test_id) ;
    }

    private void updateViewStatus(String test_id){
        FCDBService fcdbService = new FCDBService(context);
        fcdbService.updateResultStatus(test_id);
    }

    public byte[] readBytes(InputStream inputStream) throws IOException {
        // this dynamically extends to take the bytes you read
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();

        // this is storage overwritten on each iteration with bytes
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        // we need to know how may bytes were read to write them to the byteBuffer
        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }

        // and then we can return your byte array.
        return byteBuffer.toByteArray();
    }
    public void setNotEstimationModelRecord(String modelRows,String test_id)
    {
        if(!modelRows.isEmpty()) {
            try {
                JSONObject rootJobj = new JSONObject(modelRows);
                JSONObject modelJObj = rootJobj.getJSONObject("model");
                String modelName = modelJObj.getString("name");
                tvModelName.setText(modelName);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            ivEstimationGraph.setVisibility(GONE);
            modelRecords.removeAllViews();
            setupRow(-1, "", "", "Not Known");

        }

        updateViewStatus(test_id) ;
    }

    private void setupRow(int i, String min , String max,String value) {

        ViewGroup.LayoutParams param = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT );


        final float TEXT_SIZE = 18f ;
        final int GRAVITY_VALUE = Gravity.CENTER ;
        TextView tv1 = new TextView(getContext());

        if(i>0) tv1.setText("Scan" + i);
        else if(i==0) tv1.setText("Aggregate");
        else tv1.setText("Aggregation");

        tv1.setPadding(10,10,10,10);
        tv1.setTextSize(TEXT_SIZE);
        tv1.setTextColor(Color.BLACK);
        tv1.setLayoutParams(param);
        tv1.setTypeface(Typeface.DEFAULT_BOLD);
        int alternate = i % 2 ;
        if(alternate == 0)
        {
            tv1.setBackgroundColor(Color.parseColor("#FFF9F9F9"));
        }
        TextView tv2 = new TextView(getContext());
        if(i>0) tv2.setText("");
        else tv2.setText(min);
        tv2.setPadding(10,10,10,10);
        tv2.setTextSize(TEXT_SIZE);
        tv2.setTextColor(Color.BLACK);
        tv2.setLayoutParams(param);

        if(alternate == 0)
        {
            tv2.setBackgroundColor(Color.parseColor("#FFF9F9F9"));
        }

        TextView tv3 = new TextView(getContext());
        tv3.setPadding(10,10,10,10);
        tv3.setText(value);
        tv3.setTextSize(TEXT_SIZE);
        tv3.setTextColor(Color.BLACK);
        tv3.setLayoutParams(param);

        if(alternate == 0)
        {
            tv3.setBackgroundColor(Color.parseColor("#FFF9F9F9"));
        }
        TextView tv4 = new TextView(getContext());
        if(i>0) tv4.setText("");
        else tv4.setText(max);
        tv4.setPadding(10,10,10,10);
        tv4.setTextSize(TEXT_SIZE);
        tv4.setTextColor(Color.BLACK);
        tv4.setLayoutParams(param);

        if(alternate == 0)
        {
            tv4.setBackgroundColor(Color.parseColor("#FFF9F9F9"));
        }

        modelRecords.addView(tv1);
        modelRecords.addView(tv3);
        modelRecords.addView(tv2);
        modelRecords.addView(tv4);
    }

    private void setupAggregate(String min,String max ,String value){
        modelRecords.removeAllViews();
        ViewGroup.LayoutParams param = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT );


        final float TEXT_SIZE = 18f ;
        final int GRAVITY_VALUE = Gravity.CENTER ;
        TextView tv1 = new TextView(getContext());
        tv1.setText("Aggregate");
        tv1.setPadding(10,10,10,10);
        tv1.setTextSize(TEXT_SIZE);
        tv1.setTextColor(Color.BLACK);
        tv1.setLayoutParams(param);
        tv1.setTypeface(Typeface.DEFAULT_BOLD);

        TextView tv2 = new TextView(getContext());
        tv2.setPadding(10,10,10,10);
        tv2.setText(value);
        tv2.setTextSize(TEXT_SIZE);
        tv2.setTextColor(Color.BLACK);
        tv2.setLayoutParams(param);


        TextView tv3 = new TextView(getContext());
        tv3.setPadding(10,10,10,10);
        tv3.setText("Min");
        tv3.setTextSize(TEXT_SIZE);
        tv3.setTextColor(Color.BLACK);
        tv3.setLayoutParams(param);
        tv3.setTypeface(Typeface.DEFAULT_BOLD);

        TextView tv4 = new TextView(getContext());
        tv4.setPadding(10,10,10,10);
        tv4.setText(min);
        tv4.setTextSize(TEXT_SIZE);
        tv4.setTextColor(Color.BLACK);
        tv4.setLayoutParams(param);


        TextView tv5 = new TextView(getContext());
        tv5.setPadding(10,10,10,10);
        tv5.setText("Max");
        tv5.setTextSize(TEXT_SIZE);
        tv5.setTextColor(Color.BLACK);
        tv5.setLayoutParams(param);
        tv5.setTypeface(Typeface.DEFAULT_BOLD);

        TextView tv6 = new TextView(getContext());
        tv6.setPadding(10,10,10,10);
        tv6.setText(max);
        tv6.setTextSize(TEXT_SIZE);
        tv6.setTextColor(Color.BLACK);
        tv6.setLayoutParams(param);

        modelRecords.addView(tv1);
        modelRecords.addView(tv2);
        modelRecords.addView(tv3);
        modelRecords.addView(tv4);
        modelRecords.addView(tv5);
        modelRecords.addView(tv6);

    }
}
