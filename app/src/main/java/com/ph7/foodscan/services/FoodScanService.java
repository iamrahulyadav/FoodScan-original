package com.ph7.foodscan.services;

import android.location.Location;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.JsonSyntaxException;
import com.ph7.foodscan.application.FoodScanApplication;
import com.ph7.foodscan.callbacks.FoodScanHandler;
import com.ph7.foodscan.models.ph7.ScanBundle;
import com.ph7.foodscan.models.ph7.ScioTestResults;
import com.ph7.foodscan.services.interfaces.FoodScanServiceInterface;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by craigtweedy on 18/04/2016.
 */
public class FoodScanService implements FoodScanServiceInterface {

    private SessionService sessionService = new SessionService();
    private RequestQueue queue = Volley.newRequestQueue(FoodScanApplication.getAppContext());
    public URL serverURL = null;
    private String version = "/v1";

    public FoodScanService() {
        try {
            if(sessionService.getuserpref()!=null)
            {
                String pref=sessionService.getuserpref();
                if(pref.equalsIgnoreCase("Live"))
                {
                    this.serverURL = new URL("https://api.foodscan.co.uk");
                }
                else if(pref.equalsIgnoreCase("Test"))
                {
                    this.serverURL = new URL("https://apitest.foodscan.co.uk");
                }
            } else {
                this.serverURL = new URL("https://apitest.foodscan.co.uk");
            }
         //   this.serverURL = new URL("https://api.foodscan.co.uk");
          //  this.serverURL = new URL("https://apitest.foodscan.co.uk");/*For Testing Environment*/
        } catch (MalformedURLException e) {
        }
    }

    @Override
    public void login(Map<String, String> params, FoodScanHandler handler) {
        String url = this.concatenate(this.serverURL, "/token").toString();
        ParamsRequest request = this.paramsRequest(Request.Method.POST, url, params, handler);
        this.queue.add(request);
    }

    //[Removed]
    @Override
    public void logScan(ScioTestResults testResults, FoodScanHandler handler) {
        String url = this.concatenate(this.serverURL, "/collection/" + testResults.getCollectionUuid() + "/scan").toString();
        JsonRequest request = this.jsonRequest(Request.Method.POST, url, testResults.toJSON(), handler);
        request.setUsesAuthentication(this.sessionService.getUserToken());
        this.queue.add(request);
    }

    //[Removed]
    @Override
    public void logScan(ScanBundle bundle, FoodScanHandler handler) {
        String url = this.concatenate(this.serverURL, "/collection/" + bundle.getCollectionUuid() + "/scan").toString();
        JsonRequest request = this.jsonRequest(Request.Method.POST, url, bundle.toJSON(), handler);
        request.setUsesAuthentication(this.sessionService.getUserToken());
        this.queue.add(request);
    }

    public void logScan(ScanBundle bundle, Map<String,String> bodyParams, FoodScanHandler handler) {
        bodyParams.put("json",bundle.toJSON().toString());
        Log.d("json",bundle.toJSON().toString()) ;
        String url = this.concatenate(this.serverURL, "/test" ).toString();//+ bundle.getCollectionUuid() + "/scan").toString();
        FormParamsRequest request = this.formParamRequest(Request.Method.POST, url, bodyParams, handler);
        request.setUsesAuthentication(this.sessionService.getUserToken());
        this.queue.add(request);
    }



    @Override
    public void logSCIO(JSONObject jsonObject, FoodScanHandler handler) {
        String url = this.concatenate(this.serverURL, "/scio-log").toString();
        JsonRequest request = this.jsonRequest(Request.Method.POST, url, jsonObject, handler);
        request.setUsesAuthentication(this.sessionService.getUserToken());
        this.queue.add(request);
    }

    public void logError(JSONObject jsonObject, FoodScanHandler handler) {
        String url = "http://webdevelopmentreviews.net/global/Webservice/track";
        JsonRequest request = this.jsonRequest(Request.Method.POST, url, jsonObject, handler);
        // request.setUsesAuthentication(this.sessionService.getUserToken());
        this.queue.add(request);
    }


    @Override
    public void getBusinesses(Location location, FoodScanHandler handler) {
        Map params = new HashMap<String, String>();
        String path = "/business?lat=" + String.valueOf(location.getLatitude()) + "&lng=" + String.valueOf(location.getLongitude());
        String url = this.concatenate(this.serverURL, path).toString();
        ParamsRequest request = this.paramsRequest(Request.Method.GET, url, params, handler);
        request.setUsesAuthentication(this.sessionService.getUserToken());
        this.queue.add(request);
    }

    @Override
    public void getCollections(FoodScanHandler handler) {
        Map params = new HashMap<String, String>();
        String url = this.concatenate(this.serverURL, "/collection").toString();
        ParamsRequest request = this.paramsRequest(Request.Method.GET, url, params, handler);
        request.setUsesAuthentication(this.sessionService.getUserToken());
        this.queue.add(request);
    }



    public void getModels(FoodScanHandler handler) {
        Map params = new HashMap<String, String>();
        String url = this.concatenate(this.serverURL, "/model").toString();
        ParamsRequest request = this.paramsRequest(Request.Method.GET, url, params, handler);
        request.setUsesAuthentication(this.sessionService.getUserToken());
        this.queue.add(request);
    }


    @Override
    public void searchForImages(String collectionId, String modelId, String value, FoodScanHandler handler) {
        Map params = new HashMap<String, String>();
        try {
            String path = "/collection/" + collectionId + "/model/" + modelId + "/attributes?type=image&value="+ URLEncoder.encode(value, "UTF-8");
            String url = this.concatenate(this.serverURL, path).toString();
            ParamsRequest request = this.paramsRequest(Request.Method.GET, url, params, handler);
            request.setUsesAuthentication(this.sessionService.getUserToken());
            this.queue.add(request);
        } catch (UnsupportedEncodingException e) {
            String errorResponse  = e.getMessage() ;
            handler.onError();

        }
    }


    private URL concatenate(URL baseUrl, String extraPath) {

        URI uri = null;
        try {
            uri = baseUrl.toURI();
            String newPath = this.version + uri.getPath() + extraPath;
            URI newUri = uri.resolve(newPath);
            return newUri.toURL();
        } catch (URISyntaxException e) {
            return baseUrl;
        } catch (MalformedURLException e) {
            return baseUrl;
        }
    }

    private JsonRequest jsonRequest(int method, final String url, JSONObject json, final FoodScanHandler handler) {
        return new JsonRequest(method, url, json, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                handler.onSuccess(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
//                JSONObject rootJObj= null ;
//                try {
//                    String errorResponse  = new String(error.networkResponse.data) ;
//                    if(!errorResponse.trim().isEmpty())
//                        rootJObj  = new JSONObject(errorResponse);
//                    else
//                        rootJObj.put("error",errorResponse);
//                } catch (Exception e) {
//                    rootJObj = new JSONObject();
//                    e.printStackTrace();
//                }
                handler.onError();
                Log.d("FoodScan","Error Getting Response"+" : URL "+url);
            }
        });
    }

    private ParamsRequest paramsRequest(int method, final String url, Map<String, String> params, final FoodScanHandler handler) {
        return new ParamsRequest(method, url, params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                handler.onSuccess(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

//                JSONObject rootJObj= null ;
//                try {
//                    String errorResponse  = new String(error.networkResponse.data) ;
//                    if(!errorResponse.trim().isEmpty())
//                        rootJObj  = new JSONObject(errorResponse);
//                    else
//                        rootJObj.put("error",errorResponse);
//                } catch (Exception e) {
//                    rootJObj = new JSONObject();
//                    e.printStackTrace();
//                }
                handler.onError();
                Log.d("FoodScan","Error Getting Response"+" : URL "+url);
            }
        });
    }

    private FormParamsRequest formParamRequest(int method, String url, Map<String, String> params, final FoodScanHandler handler) {
        return new FormParamsRequest(method, url, params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                handler.onSuccess(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                handler.onError();
            }
        });
    }

    public class JsonRequest extends JsonObjectRequest    {

        boolean usesAuthentication = false;
        String authenticationToken = "";

        public  JsonRequest(int method, String url, JSONObject jsonRequest, Response.Listener listener, Response.ErrorListener errorListener)
        {
            super(method, url, jsonRequest, listener, errorListener);
        }

        @Override
        public Map getHeaders() throws AuthFailureError {
            Map headers = new HashMap();
            headers.put("Content-Type", "application/json");
            headers.put("Accept", "application/json");

            if(this.usesAuthentication) {
                headers.put("Authorization", "Bearer "+this.authenticationToken);
            }

            return headers;
        }

        @Override
        public String getBodyContentType() {
            return "application/json";
        }

        public void setUsesAuthentication(String token) {
            this.usesAuthentication = true;
            this.authenticationToken = token;
        }
    }

    public class ParamsRequest extends Request {

        boolean usesAuthentication = false;
        String authenticationToken = "";

        Map<String, String> params = null;
        private final Response.Listener listener;

        public ParamsRequest(int method, String url, Map<String,String> params, Response.Listener listener, Response.ErrorListener errorListener)
        {
            super(method, url, errorListener);
            this.params = params;
            this.listener = listener;
        }

        @Override
        public Map<String, String> getParams() {
            return this.params;
        }

        @Override
        public Map<String, String> getHeaders() throws AuthFailureError {
            Map<String,String> params = new HashMap<String, String>();
            params.put("Content-Type","application/x-www-form-urlencoded");

            if(this.usesAuthentication) {
                params.put("Authorization", "Bearer "+this.authenticationToken);
            }

            return params;
        }

        @Override
        protected void deliverResponse(Object response) {
            this.listener.onResponse(response);
        }

        @Override
        protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
            try {
                String json = new String(
                        response.data,
                        HttpHeaderParser.parseCharset(response.headers));
                return Response.success(
                        new JSONObject(json),
                        HttpHeaderParser.parseCacheHeaders(response));
            } catch (UnsupportedEncodingException e) {
                return Response.error(new ParseError(e));
            } catch (JsonSyntaxException e) {
                return Response.error(new ParseError(e));
            } catch (JSONException e) {
                return Response.error(new ParseError(e));

            }
        }

        public void setUsesAuthentication(String token) {
            this.usesAuthentication = true;
            this.authenticationToken = token;
        }


    }

    public class FormParamsRequest extends Request {

        boolean usesAuthentication = false;
        String authenticationToken = "";

        Map<String, String> params = null;
        private final Response.Listener listener;
        private final String BOUNDRY = "s2retfgsGSRFsERFGHfgdfgw734yhFHW567TYHSrf4yarg";


        public FormParamsRequest(int method, String url, Map<String,String> params, Response.Listener listener, Response.ErrorListener errorListener)
        {
            super(method, url, errorListener);
            this.params = params;
            this.listener = listener;
        }

        @Override
        public Map<String, String> getParams() {
            return this.params;
        }

        @Override
        public Map<String, String> getHeaders() throws AuthFailureError {
            Map<String,String> params = new HashMap<String, String>();
            params.put("Content-Type", "multipart/form-data;boundary=" + BOUNDRY);

            if(this.usesAuthentication) {
                params.put("Authorization", "Bearer "+this.authenticationToken);
            }

            return params;
        }
        @Override
        public byte[] getBody() throws AuthFailureError {
            String postBody = createPostBody(this.params);
            return postBody.getBytes();
        }

        private String createPostBody(Map<String, String> params) {
            StringBuilder sbPost = new StringBuilder();
            if (params != null) {
                for (String key : params.keySet()) {
                    if (params.get(key) != null) {
                        sbPost.append("\r\n" + "--" + BOUNDRY + "\r\n");
                        sbPost.append("Content-Disposition: form-data; name=\"" + key + "\"" + "\r\n\r\n");
                        sbPost.append(params.get(key).toString());
                    }
                }
            }
            return sbPost.toString();
        }
        @Override
        protected void deliverResponse(Object response) {
            this.listener.onResponse(response);
        }

        @Override
        protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
            try {
                String json = new String(
                        response.data,
                        HttpHeaderParser.parseCharset(response.headers));
                return Response.success(
                        new JSONObject(json),
                        HttpHeaderParser.parseCacheHeaders(response));
            } catch (UnsupportedEncodingException e) {
                return Response.error(new ParseError(e));
            } catch (JsonSyntaxException e) {
                return Response.error(new ParseError(e));
            } catch (JSONException e) {
                return Response.error(new ParseError(e));
            }
        }

        public void setUsesAuthentication(String token) {
            this.usesAuthentication = true;
            this.authenticationToken = token;
        }
    }


}
