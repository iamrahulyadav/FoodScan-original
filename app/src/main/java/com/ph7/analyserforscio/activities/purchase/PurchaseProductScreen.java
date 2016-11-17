package com.ph7.analyserforscio.activities.purchase;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.gc.materialdesign.utils.Utils;
import com.ph7.analyserforscio.R;
import com.ph7.analyserforscio.activities.AppActivity;
import com.ph7.analyserforscio.device.BluetoothDevice;
import com.ph7.analyserforscio.models.data.ProductModel;
import com.ph7.analyserforscio.services.SessionService;
import com.ph7.analyserforscio.util.IabBroadcastReceiver;
import com.ph7.analyserforscio.util.IabHelper;
import com.ph7.analyserforscio.util.IabResult;
import com.ph7.analyserforscio.util.Inventory;
import com.ph7.analyserforscio.util.Purchase;
import com.ph7.analyserforscio.util.SkuDetails;
import com.ph7.analyserforscio.utils.Validation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class PurchaseProductScreen extends AppActivity  implements IabBroadcastReceiver.IabBroadcastListener {

    private static final String TAG = "FoodScan";
    ProgressDialog dialog ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_purchase_product_screen);
        setActionBarOverlayZero();
        dialog = ProgressDialog.show(PurchaseProductScreen.this,"","Loading...",true);
        inAppBillingModule();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return false ;
    }

    private void setupSkuProductListView() {
        final ListView lv = (ListView) findViewById(R.id.listView);
        if(lv!=null) {
            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    final ProductModel productModel = (ProductModel) adapterView.getAdapter().getItem(i);

                    launchPurchase(productModel.getProductId());

                    // For Testing
                    //launchPurchase("android.test.refunded");
                }
            });

            SKUProductAdapter productAdapter = new SKUProductAdapter(getApplicationContext(), this.skuProductList);
            lv.setAdapter(productAdapter);
        }

    }

    public class SKUProductAdapter extends ArrayAdapter<ProductModel> {
        public SKUProductAdapter(final Context context, final List<ProductModel> productModels) {
            super(context, 0, productModels);
        }

        @Override
        public View getView(final int position, View convertView, final ViewGroup parent) {
            final ProductModel productModel = getItem(position);

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.product_detail_item, parent, false);
            }
            final TextView tvProductTitle = (TextView) convertView.findViewById(R.id.tvProductTitle);
            final TextView tvProductPrice = (TextView) convertView.findViewById(R.id.tvProductPrice);
            final TextView tvProductDesc = (TextView) convertView.findViewById(R.id.tvProductDesc);
            tvProductTitle.setText(productModel.getProductTitle());
            tvProductDesc.setText(productModel.getProductDesc());
            tvProductPrice.setText(productModel.getProductPrice());
            return convertView;
        }
    }

    /****
     * *********************************
     * <p/>
     * IN-APP BILLING & IN-APP PURCHASE
     * IMPLEMENTATION
     * <p/>
     * *********************************
     ****/

    private IabHelper mHelper;
    private IabBroadcastReceiver mBroadcastReceiver;
    final String SKU_1000_SCANS = "1000_scans";
    final String SKU_5000_SCANS = "5000_scans";
    final String SKU_10000_SCANS = "10000_scans";

    private List<String> additionalSkuList;
    public ArrayList<ProductModel> skuProductList;

    private void inAppBillingModule() {
        // compute your public key and store it in base64EncodedPublicKey
        mHelper = new IabHelper(this, base64EncodedPublicKey);
        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            @Override
            public void onIabSetupFinished(IabResult result) {
                if (!result.isSuccess()) {
                    // Oh no, there was a problem.
                    Log.d("InAPP-FoodScan", "Problem setting up In-app Billing: " + result);
                }
                if (mHelper == null) return;

                mBroadcastReceiver = new IabBroadcastReceiver(PurchaseProductScreen.this);
                IntentFilter broadcastFilter = new IntentFilter(IabBroadcastReceiver.ACTION);
                registerReceiver(mBroadcastReceiver, broadcastFilter);
                additionalSkuList = new ArrayList();
                additionalSkuList.add(SKU_1000_SCANS);
                additionalSkuList.add(SKU_5000_SCANS);
                additionalSkuList.add(SKU_10000_SCANS);
                //  Log.d(TAG, "Setup successful. Querying inventory.");
                try {
                    mHelper.queryInventoryAsync(true, additionalSkuList, null, mGotInventoryListener);
                } catch (IabHelper.IabAsyncInProgressException e) {
                }

            }
        });

        // Listener that's called when we finish querying the items and subscriptions we own
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // very important:
        if (mBroadcastReceiver != null) {
            unregisterReceiver(mBroadcastReceiver);
        }

        // very important:
        if (mHelper != null) {
            mHelper.disposeWhenFinished();
            mHelper = null;
        }
    }

    private IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
            // Have we been disposed of in the meantime? If so, quit.
            if (mHelper == null) return;

            // Is it a failure?
            if (result.isFailure()) {
                return;
            }
            setupSKUProductList(inventory);

//            Purchase scanPurchase = inventory.getPurchase(SKU_1000_SCANS);
//            inventory.getSkuDetails(SKU_1000_SCANS);
//            if (scanPurchase != null && verifyDeveloperPayload(scanPurchase)) {
//                try {
//                    mHelper.consumeAsync(inventory.getPurchase(SKU_1000_SCANS), mConsumeFinishedListener);
//                } catch (IabHelper.IabAsyncInProgressException e) {
//                    //      complain("Error consuming gas. Another async operation in progress.");
//                }
//                return;
//            }
            // updateUi();
            //  setWaitScreen(false);
            // Log.d(TAG, "Initial inventory query finished; enabling main UI.");
        }
    };



    private void setupSKUProductList(Inventory inventory) {
        skuProductList = new ArrayList<>();
        for (String product_id : additionalSkuList) {
            if (inventory.hasDetails(product_id)) {
                ProductModel productModel = new ProductModel();
                SkuDetails skuDetails = inventory.getSkuDetails(product_id);
                String productPrice = skuDetails.getPrice();
                String currencyCode = skuDetails.getPriceCurrencyCode();
                String productDesc = skuDetails.getDescription();
                String productTitle = skuDetails.getTitle();
                String productType = skuDetails.getType();

                productModel.setProductId(product_id);
                productModel.setProductTitle(productTitle);
                productModel.setProductDesc(productDesc);
                productModel.setProductType(productType);
                productModel.setProductPrice(productPrice);
                productModel.setCurrencyCode(currencyCode);
                if(skuProductList!=null) skuProductList.add(productModel);
            }
        }

        setupSkuProductListView();
        if(dialog!=null) dialog.dismiss();
    }


    /**
     * Verifies the developer payload of a purchase.
     */
    private boolean verifyDeveloperPayload(Purchase p) {
        String payload = p.getDeveloperPayload();
        return true;
    }

    @Override
    public void receivedBroadcast() {
        try {
            mHelper.queryInventoryAsync(mGotInventoryListener);
        } catch (IabHelper.IabAsyncInProgressException e) {

        }
    }

    // Called when consumption is complete
    private IabHelper.OnConsumeFinishedListener mConsumeFinishedListener = new IabHelper.OnConsumeFinishedListener() {
        public void onConsumeFinished(Purchase purchase, IabResult result) {
            Log.d(TAG, "Consumption finished. Purchase: " + purchase + ", result: " + result);

            if (mHelper == null)
                return;
            if (result.isSuccess()) {
                Log.d(TAG, "Consumption successful. Provisioning.");
            } else {
            }
            Log.d(TAG, "End consumption flow.");
        }
    };


    private void launchPurchase(String product) {
        /* TODO: for security, generate your payload here for verification. See the comments on
         *        verifyDeveloperPayload() for more info. Since this is a SAMPLE, we just use
         *        an empty string, but on a production app you should carefully generate this. */
        String payload = Validation.generateRandomString(30);

        try {
            mHelper.launchPurchaseFlow(this, product, 20000,
                    mPurchaseFinishedListener, payload);
        } catch (IabHelper.IabAsyncInProgressException e) {
        }
        // Callback for when a purchase is finished
    }


    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
            Log.d(TAG, "Purchase finished: " + result + ", purchase: " + purchase);
            // if we were disposed of in the meantime, quit.
            if (mHelper == null) return;

            if (result.isFailure()) {
                alert(result.getMessage());
                return;
            }
            if (!verifyDeveloperPayload(purchase)) {
                return;
            }
            final SessionService sessionService = new SessionService() ;
            switch(purchase.getSku())
            {
                case SKU_1000_SCANS :
                    sessionService.setScanCount(sessionService.getScanCount()+1000);
                    break ;

                case SKU_5000_SCANS :
                    sessionService.setScanCount(sessionService.getScanCount()+5000);
                    break ;

                case SKU_10000_SCANS :
                    sessionService.setScanCount(sessionService.getScanCount()+10000);
                    break ;
            }

            AlertDialog.Builder bld = new AlertDialog.Builder(PurchaseProductScreen.this);
            bld.setMessage("Payment Successful");
            dialog.setCancelable(false);
            final AlertDialog dialog =  bld.create();
            bld.setNeutralButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialog.dismiss();
                    finish();
                }
            });
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult(" + requestCode + "," + resultCode + "," + data);
        if (mHelper == null) return;

        // Pass on the activity result to the helper for handling
        if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
            // not handled, so handle it ourselves (here's where you'd
            // perform any handling of activity results not related to in-app
            // billing...
            super.onActivityResult(requestCode, resultCode, data);
        }
        else {
            Log.d(TAG, "onActivityResult handled by IABUtil.");
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            dialog.dismiss();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    void alert(String message) {
        AlertDialog.Builder bld = new AlertDialog.Builder(this);
        bld.setMessage(message);
        bld.setNeutralButton("OK", null);
        Log.d(TAG, "Showing alert dialog: " + message);
        bld.create().show();
    }
}

