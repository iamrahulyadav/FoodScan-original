package com.ph7.analyserforscio.activities.main;

import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;

import com.ph7.analyserforscio.R;
import com.ph7.analyserforscio.activities.AppActivity;
import com.ph7.analyserforscio.fragments.TestListFragment;
import com.ph7.analyserforscio.models.scio.SCIOResultDataModel;
import com.ph7.analyserforscio.services.FCDBService;

import java.util.ArrayList;
import java.util.List;

/**
 * The MyTestsActivity is where users can go to see tests they have analysed and tests they
 * have yet to analyse.
 *
 * @author  Craig Tweedy
 * @version 0.7
 * @since   2016-07-04
 */
public class MyTestsActivity extends AppActivity {

    ViewPager viewPager ;
    TabLayout tabLayout ;

    private ArrayList<SCIOResultDataModel> listNotAnalysedData = null;
    private ArrayList<SCIOResultDataModel> listAnalysedData = null;
    private int index;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_my_tests);
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        tabLayout = (TabLayout) findViewById(R.id.tabs);
        index =  getIntent().getIntExtra("index",0);
        setActionBarOverlayZero();
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        Bundle bundle = null;

        TestListFragment analysedListFragment = new TestListFragment();
        bundle =  new Bundle();
      //  bundle.putInt("type",1);
        bundle.putSerializable("results",listAnalysedData);
        analysedListFragment.setArguments(bundle);

        TestListFragment notAnalysedListFragment = new TestListFragment();
        bundle =  new Bundle();
       // bundle.putInt("type",0);
        bundle.putSerializable("results",listNotAnalysedData);
        notAnalysedListFragment.setArguments(bundle);

        adapter.addFragment(analysedListFragment, "Analysed");
        adapter.addFragment(notAnalysedListFragment, "Not Analysed");
        viewPager.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }


    private void setSavedScanResult() {

        FCDBService fcdbService = new FCDBService(getApplicationContext());
        Cursor testsCursor =  fcdbService.getAllTests() ;

        listAnalysedData = new ArrayList<>() ;
        listNotAnalysedData= new ArrayList<>() ;
        try
        {
            testsCursor.moveToFirst();
            while (!testsCursor.isAfterLast()) {
                int testStatus = Integer.parseInt(testsCursor.getString(9)) ;
                if(testStatus==0){
                    getNotAnalysedData(testsCursor) ;
                }
                else{
                    getAnalysedData(testsCursor) ;
                }
                testsCursor.moveToNext();
            }

        }catch(Exception x)
        {
            x.printStackTrace();
        }
        finally {
            if(testsCursor!=null) testsCursor.close();
            if(fcdbService!=null) fcdbService.close();
        }

    }

    private void getAnalysedData(Cursor testsCursor) {
        SCIOResultDataModel analysedDataModel = new SCIOResultDataModel();
        testsCursor.getString(0); // id
        analysedDataModel.test_id = testsCursor.getString(1); // test_id
        analysedDataModel.test_name = testsCursor.getString(2); // test_name
        analysedDataModel.test_note = testsCursor.getString(3); // test_note
        analysedDataModel.test_location = testsCursor.getString(4); // test_location
        analysedDataModel.model_ids = testsCursor.getString(5); // model_ids
        analysedDataModel.collection_id = testsCursor.getString(6); // collection_id
        analysedDataModel.test_scan_result = testsCursor.getString(7); // test_scan_result
        analysedDataModel.scan_count = testsCursor.getInt(8); // scan_count
        analysedDataModel.test_status = testsCursor.getString(9); // test_status
        analysedDataModel.create_datetime = testsCursor.getString(10); // create datetime
        analysedDataModel.expire = testsCursor.getString(11); // expire
        analysedDataModel.imgs_path = testsCursor.getString(12); // imgs_path
        this.listAnalysedData.add(analysedDataModel);
    }


    private void getNotAnalysedData(Cursor testsCursor) {
        SCIOResultDataModel notAnalysedDataModel = new SCIOResultDataModel();
        testsCursor.getString(0); // id
        notAnalysedDataModel.test_id = testsCursor.getString(1); // test_id
        notAnalysedDataModel.test_name = testsCursor.getString(2); // test_name
        notAnalysedDataModel.test_note = testsCursor.getString(3); // test_note
        notAnalysedDataModel.test_location = testsCursor.getString(4); // test_location
        notAnalysedDataModel.model_ids = testsCursor.getString(5); // model_ids
        notAnalysedDataModel.collection_id = testsCursor.getString(6); // collection_id
        notAnalysedDataModel.test_scan_result = testsCursor.getString(7); // test_scan_result
        notAnalysedDataModel.scan_count = testsCursor.getInt(8); // scan_count
        notAnalysedDataModel.test_status = testsCursor.getString(9); // test_status
        notAnalysedDataModel.create_datetime = testsCursor.getString(10); // create datetime
        notAnalysedDataModel.expire = testsCursor.getString(11); // expire
        notAnalysedDataModel.imgs_path = testsCursor.getString(12); // imgs_path
        this.listNotAnalysedData.add(notAnalysedDataModel);
    }

    class ViewPagerAdapter extends FragmentStatePagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }


        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewPager.removeAllViews();
        setSavedScanResult();
        setupViewPager(viewPager);
        viewPager.setCurrentItem(index);
        tabLayout.setupWithViewPager(viewPager);
    }
}
