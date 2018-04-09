package com.v3x.securenote.network;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.security.keystore.KeyProperties;
import android.security.keystore.KeyProtection;
import android.support.v4.util.ArrayMap;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.v3x.securenote.Encryption;
import com.v3x.securenote.R;
import com.v3x.securenote.models.NoteContent;
import com.v3x.securenote.network.APIRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.math.BigInteger;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Map;

import javax.crypto.SecretKey;

import static com.v3x.securenote.Encryption.AesDecrypt;
import static com.v3x.securenote.Encryption.toHex;

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
    private SharedPreferences prefs;
    public static BigInteger N = new BigInteger("00c037c37588b4329887e61c2da3324b1ba4b81a63f9748fed2d8a410c2fc21b1232f0d3bfa024276cfd88448197aae486a63bfca7b8bf7754dfb327c7201f6fd17fd7fd74158bd31ce772c9f5f8ab584548a99a759b5a2c0532162b7b6218e8f142bce2c30d7784689a483e095e701618437913a8c39c3dd0d4ca3c500b885fe3", 16);
    public static BigInteger g = new BigInteger("2");
    public static BigInteger k = new BigInteger("d24e2e1d1500ea44d19052db8e1398d0052e09392e0798faaa78ca229bf4a390", 16);

    public static String Hash(String... args){
        String toHash = TextUtils.join(":", args);

        byte encBytes[] = Encryption.SHAHash(toHash);
        return toHex(encBytes);
    }

    public static String Hash(BigInteger... args){
        ArrayList<String> hashItems = new ArrayList<String>();
        for (BigInteger arg : args){
            hashItems.add(arg.toString(16));
        }
        String toHash = TextUtils.join(":", hashItems);
        byte encBytes[] = Encryption.SHAHash(toHash);
        return toHex(encBytes);
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

    public void loadAuthData(Context ctx, String session, String uid, String key, String password, String salt){
        mInstance.session_token = session;
        mInstance.UID = uid;
        mInstance.private_key = key;

        prefs = ctx.getSharedPreferences("com.v3x.auth",Context.MODE_PRIVATE);
        //prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("session_token",session);
        editor.putString("UID",uid);

        SecureRandom rand = new SecureRandom();
        byte random_iv[] = new byte[16];
        rand.nextBytes(random_iv);

        String secure_pass = toHex(Encryption.SHAHash(password + salt));
        Log.i("API","Securing Key");
        String encrypted_key = Encryption.AesEncrypt(secure_pass, toHex(key), toHex(random_iv));
        Log.i("API","Generating test");
        String encrypted_test = Encryption.AesEncrypt(secure_pass, toHex(uid), toHex(random_iv));
        editor.putString("salt",salt);
        editor.putString("key_iv",toHex(random_iv));
        editor.putString("key",encrypted_key);
        editor.putString("key_test",encrypted_test);
        editor.commit();
        Log.i("API", prefs.getString("key",""));

    }

    public boolean unlock(Context ctx, String passphrase){
        SharedPreferences prefs = ctx.getSharedPreferences("com.v3x.auth",Context.MODE_PRIVATE);
        String iv               = prefs.getString("key_iv", null);
        String test             = prefs.getString("key_test", null);
        String enc_key          = prefs.getString("key", null);
        String salt             = prefs.getString("salt", null);
        String uid              = prefs.getString("UID", null);
        String session_token    = prefs.getString("session_token", null);

        // Do both decryptions before returning to prevent timing attacks
        Log.i("PSP","Generating secure pass");
        String secure_pass = toHex(Encryption.SHAHash(passphrase + salt));
        Log.i("PSP","Testing key");
        String decrypted_test = Encryption.AesDecrypt(secure_pass, test, iv);
        Log.i("PSP","decrypting key");
        String decrypted_key = AesDecrypt(secure_pass, enc_key, iv);

        Log.i("PSP","test: " + decrypted_test);
        Log.i("PSP", "UID " + uid);
        if(decrypted_test.equals(uid)){
            this.private_key = decrypted_key;
            this.UID = uid;
            this.session_token = session_token;
            return true;
        }
        return false;
    }

    public boolean isAuthenticated(){
        return mInstance.session_token != null && mInstance.UID != null && mInstance.private_key != null;
    }

    public void decryptNote(NoteContent.NoteItem note){
        note.data = Encryption.AesDecrypt(mInstance.private_key, note.data, note.iv);
        note.didDecrypt();
    }
    public void encryptNote(NoteContent.NoteItem note){
        note.iv = Encryption.cryptrand(16);
        note.data = Encryption.AesEncrypt(mInstance.private_key, toHex(note.data), note.iv);
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
    public void put_auth_endpoint(Context ctx, String endpoint, JSONObject jsonData, Response.Listener<JSONObject> successListener, Response.ErrorListener errorListener){
        final Map<String, String> mHeaders = new ArrayMap<String, String>();
        mHeaders.put("Authentication", getAuthHeader());

        APIRequest req = new APIRequest(Request.Method.PUT, endpoint, mHeaders, jsonData, errorListener, successListener);
        NetworkQueue.getInstance(ctx).addToRequestQueue(req);
    }
}
