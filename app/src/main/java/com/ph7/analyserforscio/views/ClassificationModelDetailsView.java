package com.ph7.analyserforscio.views;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.ph7.analyserforscio.R;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by sony on 29-08-2016.
 */
public class ClassificationModelDetailsView extends LinearLayout {
    private TableLayout modelRecords,modelResults;
    private TextView tvModelName;


    public ClassificationModelDetailsView(Context context) {
        super(context);
        init();
    }



    public ClassificationModelDetailsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ClassificationModelDetailsView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    private void init() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        inflater.inflate(R.layout.classification_model_detail_view,this);
        tvModelName =(TextView) this.findViewById(R.id.tvModelName);
        modelRecords = (TableLayout) this.findViewById(R.id.modelRecords);
        modelResults = (TableLayout) this.findViewById(R.id.modelResults);
    }

    private float totalConfidence =  0.0f ;
    private HashMap <String,Float> results = new HashMap<>();
    public void setModelRecords(ArrayList<String> modelRows,String name)
    {
        tvModelName.setText(name) ;
        final int scans  = modelRows.size();
        totalConfidence =  100.0f * scans ;

        for (int indexTableRow = 0; indexTableRow < scans ; indexTableRow++) {
            float confidence = 0.0f ;
            String brandName="" ;
            String tableRowString  =  modelRows.get(indexTableRow);
            try {

                JSONObject tableRowJson =  new JSONObject(tableRowString);
                JSONArray attributesJsonArray  = tableRowJson.getJSONArray("attributes");
                for (int indexJsonAttrObj = 0; indexJsonAttrObj < attributesJsonArray.length(); indexJsonAttrObj++) {
                    JSONObject attributeObj = attributesJsonArray.getJSONObject(indexJsonAttrObj);
                    brandName = attributeObj.getString("value");
                    confidence = Float.parseFloat(attributeObj.getString("confidence")!=null?attributeObj.getString("confidence"):"0");
                    if(!results.containsKey(brandName))
                    {
                        Float totalBrandConf =  Float.valueOf(confidence*100f) ;
                        results.put(brandName,totalBrandConf);
                    }
                    else
                    {
                        Float totalBrandConf =  results.get(brandName);
                        totalBrandConf = Float.valueOf((totalBrandConf.floatValue()+(confidence*100f))) ;
                        results.put(brandName,totalBrandConf);
                    }
                    break;
                }


            } catch (JSONException e) {
                e.printStackTrace();
            }

            //setupTableRow(indexTableRow+1,brandName,confidence*100);

        }
    }

    public void setModelRecords(String modelRows,String collectionId)
    {

         int scans  = 0 ;//modelRows.size();

        if(!modelRows.isEmpty())
        {
            try {
                JSONObject rootJobj  =  new JSONObject(modelRows);
                JSONObject modelJObj  =  rootJobj.getJSONObject("model");
                String modelName  =  modelJObj.getString("name");
                String modelId  =  modelJObj.getString("uuid");
                tvModelName.setText(modelName);

                JSONArray inputsJArr  =  rootJobj.getJSONArray("input");
                scans =  inputsJArr.length() ;
                for (int indexInput = 0; indexInput <scans; indexInput++) {
                    JSONObject inputJObj =  inputsJArr.getJSONObject(indexInput);
                    String confidence =  inputJObj.getString("confidence");
                    String value  =  inputJObj.getString("value");

                    setupTableRow(indexInput+1,value ,confidence);
                }

                JSONArray outputJArr  =  rootJobj.getJSONArray("output");

                    for (int indexOutput = 0; indexOutput <outputJArr.length(); indexOutput++) {

                        JSONObject outputJObj =  outputJArr.getJSONObject(indexOutput);
                        String confidence =  outputJObj.getString("confidence");
                        String value  =  outputJObj.getString("value");
                        String image  =  outputJObj.getString("image");
                        setupTableResultRow(value,image,confidence);
                    }


            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        totalConfidence =  100.0f * scans ;
    }


    private void setupTableRow(int i, String brandName, String confidence) {
        TableRow tr = new TableRow(getContext());
        LayoutParams lpTableRow =  new LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT);
        lpTableRow.setMargins(0,0,0,5);
        tr.setLayoutParams(lpTableRow);
        tr.setWeightSum(5f);
        TableRow.LayoutParams param1 = new TableRow.LayoutParams(0,LayoutParams.MATCH_PARENT, 1.0f);
        TableRow.LayoutParams param = new TableRow.LayoutParams(0,LayoutParams.MATCH_PARENT, 2.0f);

        final float TEXT_SIZE = 18f ;
        final int GRAVITY_VALUE = Gravity.CENTER ;
        TextView tv1 = new TextView(getContext());

        tv1.setText(String.valueOf(i));
        tv1.setGravity(GRAVITY_VALUE);
        tv1.setTextSize(TEXT_SIZE);
        tv1.setTextColor(Color.BLACK);
        tv1.setLayoutParams(param1);

        TextView tv2 = new TextView(getContext());
        tv2.setText(brandName);
        tv2.setGravity(GRAVITY_VALUE);
        tv2.setTextSize(TEXT_SIZE);
        tv2.setTextColor(Color.BLACK);
        tv2.setLayoutParams(param);


        TextView tv3 = new TextView(getContext());
        tv3.setText(confidence);
        tv3.setGravity(GRAVITY_VALUE);
        tv3.setTextSize(TEXT_SIZE);
        tv3.setTextColor(Color.BLACK);
        tv3.setLayoutParams(param);

        tr.addView(tv1);
        tr.addView(tv2);
        tr.addView(tv3);
        int alternate = i % 2 ;
        if(alternate == 0)
        {
            tr.setBackgroundColor(Color.parseColor("#FFF9F9F9"));
        }
        modelRecords.addView(tr);
    }



    public void setClassificationResult(String modelID,String collectionId) {

        Set set = results.entrySet();
        Iterator iterator = set.iterator();
        while(iterator.hasNext()) {
            Map.Entry mentry = (Map.Entry)iterator.next();
            String brandname  = (String) mentry.getKey();
            Float totalConfidenceBrand  = results.get(brandname);
            float brandConfidence =  totalConfidenceBrand.floatValue()*100/totalConfidence ;
           // setupTableResultRow(brandname,modelID,collectionId,brandConfidence);
        }
    }

    private void setupTableResultRow(final String brandName,String imagePath , String  confidence) {
        TableRow tr = new TableRow(getContext());
        LayoutParams lpTableRow =  new LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT);
        lpTableRow.setMargins(0,0,0,5);
        tr.setLayoutParams(lpTableRow);
        tr.setWeightSum(5f);
        TableRow.LayoutParams param = new TableRow.LayoutParams(0,LayoutParams.WRAP_CONTENT, 2.5f);

        final float TEXT_SIZE = 24f ;
        final int GRAVITY_VALUE = Gravity.CENTER ;

        final ImageView brandImageView = new ImageView(getContext());
        brandImageView.setContentDescription(brandName);
        param.height =  256 ;
        param.width = 128;
        //brandImageView.setBackgroundColor(Color.parseColor("#f0f0f0"));
        brandImageView.setLayoutParams(param);
//        new AsyncTask<Void,Void,Void>()
//        {
//            @Override
//            protected Void doInBackground(Void... voids) {
//                new FoodScanService().searchForImages(collectionId, modelID, brandName, new FoodScanHandler() {
//                    @Override
//                    public void onSuccess(JSONObject object) {
//                        try {
//                            JSONArray attributesJObj  = object.getJSONArray("attributes");
//                            for (int indexAttribute = 0; indexAttribute < attributesJObj.length(); indexAttribute++) {
//                                JSONObject attributeObj  = attributesJObj.getJSONObject(indexAttribute);
//                                String type =  attributeObj.getString("type") ;
//                                if(type.toLowerCase().equals("image")) {
//                                    JSONArray imageJArray = attributeObj.getJSONArray("formats");
//                                    for (int indexImageObj = 0; indexImageObj < imageJArray.length(); indexImageObj++) {
//                                        JSONObject imageJObj = imageJArray.getJSONObject(indexImageObj);
//                                        int i_width = imageJObj.getInt("width");
//                                        int i_height = imageJObj.getInt("height");
//
//                                        String url = imageJObj.getString("url");
//
//                                        Picasso.with(getContext()).load(url).resize(i_width, i_height).centerInside().into(brandImageView);
//
//                                    }
//                                }
//                            }
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//                    }
//
//                    @Override
//                    public void onError() {
//
//                    }
//                });
//                return null;
//            }
//        }.execute(null,null,null);

//        TextView tv2 = new TextView(getContext());
//        tv2.setText(brandName);
//        tv2.setGravity(GRAVITY_VALUE);
//        tv2.setTextSize(TEXT_SIZE);
//        tv2.setTextColor(Color.BLACK);
//        tv2.setLayoutParams(param);
        Picasso.with(getContext()).load(imagePath).resize(256, 128).centerInside().into(brandImageView);
        TextView tv3 = new TextView(getContext());
        tv3.setText(confidence);
        tv3.setGravity(GRAVITY_VALUE);
        tv3.setTextSize(TEXT_SIZE);
        tv3.setTextColor(Color.BLACK);
        tv3.setLayoutParams(param);

        tr.addView(brandImageView);
        tr.addView(tv3);

        modelResults.addView(tr);
    }


}
