package com.ph7.foodscan.fragments;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.ph7.foodscan.R;
import com.ph7.foodscan.activities.analyse.TestDetailForAnalyseActivity;
import com.ph7.foodscan.activities.analyse.TestResultsActivity;

import com.ph7.foodscan.models.scio.SCIOResultDataModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by craigtweedy on 04/07/2016.
 */
public class TestListFragment extends Fragment {

    SCIOResultDataAdapter resultDataAdapter;
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view =  inflater.inflate(R.layout.test_list_fragment,container,false) ;
        this.setupListData(view);
        return view;
    }

    private void setupListData(View view) {
        Bundle bundle = getArguments() ;
        ListView listViewMyTest = (ListView) view.findViewById(R.id.listViewMyTest);
        listViewMyTest.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                SCIOResultDataModel dataModel =   resultDataAdapter.getItem(i) ;
                // 0 =>  Not Analysed  1 => Analysed
                Intent intent ;
                switch(dataModel.test_status)
                {
                    case "0" :
                        intent =  new Intent(getActivity(), TestDetailForAnalyseActivity.class);
                        intent.putExtra("scan_result",dataModel);
                        startActivity(intent);
                        break ;

                    case "1" :
                        intent =  new Intent(getActivity(), TestResultsActivity.class);
                        intent.putExtra("test_id",dataModel.test_id);
                        intent.putExtra("isEnableRetest",false) ;
                        startActivity(intent);
                        break;
                }
            }
        });
        if(bundle!=null)
        {
            List<SCIOResultDataModel> results ;
            results = (List<SCIOResultDataModel>) bundle.getSerializable("results");
            if(results==null)
            {
                results = new ArrayList<>();
            }
            resultDataAdapter = new SCIOResultDataAdapter(getActivity(),results) ;
            listViewMyTest.setAdapter(resultDataAdapter);
        }

    }

    public class SCIOResultDataAdapter extends ArrayAdapter<SCIOResultDataModel> {
        public SCIOResultDataAdapter(final Context context, final List<SCIOResultDataModel> listResultData) {
            super(context, 0, listResultData);
        }

        @Override
        public View getView(final int position, View convertView, final ViewGroup parent) {
            final SCIOResultDataModel scioResultDataModel = getItem(position);

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.simple_details_item, parent, false);
            }

            final TextView tvHeading = (TextView) convertView.findViewById(R.id.tvHeading);
            final TextView tvSubheading = (TextView) convertView.findViewById(R.id.tvSubheading);
            tvHeading.setText(scioResultDataModel.test_name);
            tvSubheading.setText(scioResultDataModel.create_datetime);
            return convertView;
        }
    }
}
