package com.ph7.foodscan.activities.onboarding;


import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.ph7.foodscan.R;

import java.net.URI;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class AuthDialog extends AppCompatActivity {

    private static String TAG = "AuthDialog";

    private static String OAUTH_AUTH_URL = "https://auth.foodscan.co.uk/oauth/v2/auth";
    private static String OAUTH_CLIENT_ID = "5536626e-476d-4115-9054-6624edafd3ad";
    private static String OAUTH_REDIRECT_URI="http://localhost";
    private static String OAUTH_RESPONSE_TYPE="token";
    private static String OAUTH_SCOPES="basic";

    public static String ERROR_DESCRIPTION =  "errorDescription" ;
    public static String ERROR_CODE =  "errorCode" ;
    private String state;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth_dialog);
        getSupportActionBar().hide();
        state = UUID.randomUUID().toString();

        WebView web = (WebView) findViewById(R.id.web_view);
        web.getSettings().setJavaScriptEnabled(true);
        web.loadUrl(
                OAUTH_AUTH_URL
                        +"?redirect_uri="+OAUTH_REDIRECT_URI
                        +"&response_type="+OAUTH_RESPONSE_TYPE
                        +"&client_id="+OAUTH_CLIENT_ID
                        +"&scope="+OAUTH_SCOPES
                        +"&state="+state
        );

        web.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
                super.onReceivedHttpError(view, request, errorResponse);
            }

            @Override
            public void onReceivedError(final WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    Snackbar.make(view,"Error Code : "+error.getErrorCode()+" Description : "+error.getDescription(), Snackbar.LENGTH_INDEFINITE)
                            .setAction("Retry", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    view.loadUrl(view.getOriginalUrl());
                                }
                            }).show();
                }
                else{
                    Toast.makeText(AuthDialog.this, "Error", Toast.LENGTH_SHORT).show();
                }
            }

            public boolean shouldOverrideUrlLoading(WebView view, String url)
            {
                Log.d("url",url);
                if (url.startsWith("http://localhost/#")) {
                    try {
                        URI uri = new URI(url);
                        String fragment = uri.getFragment();

                        String accessToken = getAccessToken(fragment);
                        String state = getState(fragment);

                        if (! state.equals(AuthDialog.this.state)) {
                            Log.e(TAG, "Invalid state");
                        }

                        Intent intent = new Intent();
                        intent.putExtra("token",accessToken);

                        setResult(RESULT_OK,intent);
                        finish();

                    } catch (Exception e) {
                        Log.e(TAG, e.getMessage());
                    }
                    return true;
                }
                return false;
            }

            private String getAccessToken(String uriFragment) throws Exception {
                Pattern p = Pattern.compile("access_token=([^&]+)");
                Matcher m = p.matcher(uriFragment);
                if (m.find()) {
                    return m.group(1);
                }
                throw new Exception("Access Token not found.");
            }

            private String getState(String uriFragment) throws Exception {
                Pattern p = Pattern.compile("state=([^&]+)");
                Matcher m = p.matcher(uriFragment);
                if (m.find()) {
                    return m.group(1);
                }
                throw new Exception("State not found.");
            }

            private String getExpires(String uriFragment) throws Exception {
                Pattern p = Pattern.compile("expires=([^&]+)");
                Matcher m = p.matcher(uriFragment);
                if (m.find()) {
                    return m.group(1);
                }
                throw new Exception("Expires not found.");
            }

        });
    }
}
