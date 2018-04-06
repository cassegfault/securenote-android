package com.v3x.securenote.network;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.HttpHeaderParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class APIRequest extends JsonObjectRequest {
    public static String apiBaseUrl = "http://10.0.2.2:5000";
    public Map<String, String> mHeaders = new HashMap<>();

    //  APIRequest(Request.Method.POST, "/verify-auth", mHeaders, successListener, errorListener);

    public APIRequest(String endpoint, ErrorListener errorListener, Listener<JSONObject> responseListener){
        super(Method.GET, APIRequest.apiBaseUrl.concat(endpoint), null, responseListener, errorListener);
    }

    public APIRequest(int method, String endpoint, ErrorListener errorListener, Listener<JSONObject> responseListener){
        super(method, APIRequest.apiBaseUrl.concat(endpoint), null, responseListener, errorListener);
    }

    public APIRequest(String endpoint, JSONObject bodyData, ErrorListener errorListener, Listener<JSONObject> responseListener){
        super(Method.GET, APIRequest.apiBaseUrl.concat(endpoint), bodyData, responseListener, errorListener);
    }

    public APIRequest(int method, String endpoint, JSONObject bodyData, ErrorListener errorListener, Listener<JSONObject> responseListener){
        super(method, APIRequest.apiBaseUrl.concat(endpoint), bodyData, responseListener, errorListener);
    }

    public APIRequest(String endpoint, Map<String, String> headers, ErrorListener errorListener, Listener<JSONObject> responseListener){
        super(Method.GET, APIRequest.apiBaseUrl.concat(endpoint), null, responseListener, errorListener);
        mHeaders = headers;
    }

    public APIRequest(int method, String endpoint, Map<String, String> headers, ErrorListener errorListener, Listener<JSONObject> responseListener){
        super(method, APIRequest.apiBaseUrl.concat(endpoint), null, responseListener, errorListener);
        mHeaders = headers;
    }

    public APIRequest(String endpoint, Map<String, String> headers, JSONObject bodyData, ErrorListener errorListener, Listener<JSONObject> responseListener){
        super(Method.GET, APIRequest.apiBaseUrl.concat(endpoint), bodyData, responseListener, errorListener);
        mHeaders = headers;
    }

    public APIRequest(int method, String endpoint, Map<String, String> headers, JSONObject bodyData, ErrorListener errorListener, Listener<JSONObject> responseListener){
        super(method, APIRequest.apiBaseUrl.concat(endpoint), bodyData, responseListener, errorListener);
        mHeaders = headers;
    }

    @Override
    public Map<String, String> getHeaders(){
        return mHeaders;
    }
}
