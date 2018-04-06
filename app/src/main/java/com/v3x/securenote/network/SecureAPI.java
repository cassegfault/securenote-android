package com.v3x.securenote.network;

import android.content.Context;
import android.support.v4.util.ArrayMap;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.v3x.securenote.Encryption;
import com.v3x.securenote.models.NoteContent;
import com.v3x.securenote.network.APIRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Map;

public class SecureAPI {
    private static SecureAPI mInstance;
    private SecureAPI() { }

    public static synchronized SecureAPI getInstance() {
        if (mInstance == null) {
            mInstance = new SecureAPI();
        }
        return mInstance;
    }


    private String session_token = null;
    private String private_key = null;
    private String UID = null;
    public static BigInteger N = new BigInteger("00c037c37588b4329887e61c2da3324b1ba4b81a63f9748fed2d8a410c2fc21b1232f0d3bfa024276cfd88448197aae486a63bfca7b8bf7754dfb327c7201f6fd17fd7fd74158bd31ce772c9f5f8ab584548a99a759b5a2c0532162b7b6218e8f142bce2c30d7784689a483e095e701618437913a8c39c3dd0d4ca3c500b885fe3", 16);
    public static BigInteger g = new BigInteger("2");
    public static BigInteger k = new BigInteger("d24e2e1d1500ea44d19052db8e1398d0052e09392e0798faaa78ca229bf4a390", 16);

    public static String Hash(String... args){
        String toHash = TextUtils.join(":", args);

        byte encBytes[] = Encryption.SHAHash(toHash);
        return Encryption.toHex(encBytes);
    }

    public static String Hash(BigInteger... args){
        ArrayList<String> hashItems = new ArrayList<String>();
        for (BigInteger arg : args){
            hashItems.add(arg.toString(16));
        }
        String toHash = TextUtils.join(":", hashItems);
        byte encBytes[] = Encryption.SHAHash(toHash);
        return Encryption.toHex(encBytes);
    }

    public static byte[] cryptrand(int length){
        SecureRandom random = new SecureRandom();
        byte bytes[] = new byte[length];
        random.nextBytes(bytes);
        return bytes;
    }

    String getAuthHeader(){
        if(mInstance.UID == null){
            return "{}";
        }
        try {
            JSONObject data = new JSONObject();
            data = data.put("UID", mInstance.UID);
            data = data.put("session_token", mInstance.session_token);
            Log.i("AUTH",data.toString());
            Log.i("AUTH-UID", mInstance.UID);
            return data.toString();
        } catch (JSONException e) {
            return "";
        }
    }

    public void loadAuthData(String session, String uid, String key){
        mInstance.session_token = session;
        mInstance.UID = uid;
        mInstance.private_key = key;
    }

    public void decryptNote(NoteContent.NoteItem note){
        note.data = Encryption.AesDecrypt(mInstance.private_key, note.data, note.iv);
        note.didDecrypt();
    }

    public void get_endpoint(Context ctx, String endpoint, Response.Listener<JSONObject> successListener, Response.ErrorListener errorListener){
        APIRequest req = new APIRequest(Request.Method.GET, endpoint, errorListener, successListener);
        NetworkQueue.getInstance(ctx).addToRequestQueue(req);
    }

    public void post_endpoint(Context ctx, String endpoint, JSONObject jsonData, Response.Listener<JSONObject> successListener, Response.ErrorListener errorListener){
        APIRequest req = new APIRequest(Request.Method.POST, endpoint, jsonData, errorListener, successListener);
        NetworkQueue.getInstance(ctx).addToRequestQueue(req);
    }
    /*
    * Must instantiate to use authenticated endpoints!
    */
    public void get_auth_endpoint(Context ctx, String endpoint, Response.Listener<JSONObject> successListener, Response.ErrorListener errorListener){
        final Map<String, String> mHeaders = new ArrayMap<String, String>();

        mHeaders.put("Authentication", getAuthHeader());

        APIRequest req = new APIRequest(Request.Method.GET, endpoint, mHeaders, errorListener, successListener);
        NetworkQueue.getInstance(ctx).addToRequestQueue(req);
    }
    
    public void post_auth_endpoint(Context ctx, String endpoint, JSONObject jsonData, Response.Listener<JSONObject> successListener, Response.ErrorListener errorListener){
        final Map<String, String> mHeaders = new ArrayMap<String, String>();
        mHeaders.put("Authentication", getAuthHeader());

        APIRequest req = new APIRequest(Request.Method.POST, endpoint, mHeaders, jsonData, errorListener, successListener);
        NetworkQueue.getInstance(ctx).addToRequestQueue(req);
    }
}
