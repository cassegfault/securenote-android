package com.v3x.securenote;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.v3x.securenote.network.APIRequest;
import com.v3x.securenote.network.NetworkQueue;
import com.v3x.securenote.network.SecureAPI;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigInteger;

public class MainActivity extends AppCompatActivity {

    TextView mTextView;
    EditText mUsername;
    EditText mPassword;

    String public_ephemeral;
    String client_proof;
    String shared_key;
    String private_ephemeral;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.mTextView = (TextView)findViewById(R.id.mainText);
        this.mUsername = (EditText)findViewById(R.id.username);
        this.mPassword = (EditText)findViewById(R.id.password);
    }


    public void doLogin(View v){
        String url = "http://10.0.2.2:5000/authentication";
        JSONObject requestData = new JSONObject();

        private_ephemeral = "59cfb0fe6ad85f81c6cded10e77516d5fbd967938636ac08d02ff439df956bc8e787fc1defc1b285bb06a2c0fdf1d156c0594e85b854b097dd8ced8cf9c38fe55121ef616b574ecbec63d1478d23a1f50caaf35bb13b0cec0ff02c999239cc1ec048cceb90d7226bb16020d7557e1fef3520c894b9b4fc8b10b697418d068e4a";//Encryption.toHex(SecureAPI.cryptrand(1024));
        BigInteger public_ephemeral_int = SecureAPI.g.modPow((new BigInteger(private_ephemeral, 16)).abs(), SecureAPI.N);

        public_ephemeral = public_ephemeral_int.toString(16); //Encryption.toHex(public_ephemeral_int.toByteArray());

        try {
            requestData.put("username", "chris");
            requestData.put("client_ephemeral", public_ephemeral);
            Log.i("SRP", "Public Ephem: " + public_ephemeral);
        } catch(JSONException e){

        }


        JsonObjectRequest jsonReq = new JsonObjectRequest(
                Request.Method.POST, url, requestData, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                mTextView.setText(response.toString());
                try {
                    send_proof(response.getString("server_ephemeral"), response.getString("salt"), response.getString("auth_session"));
                } catch(JSONException e) {

                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mTextView.setText(error.toString());
            }
        });
        NetworkQueue.getInstance(this).addToRequestQueue(jsonReq);

    }

    private void send_proof(final String server_ephemeral, String salt, String auth_session){
        BigInteger server_ephemeral_int = new BigInteger(server_ephemeral, 16);
        BigInteger private_ephemeral_int = new BigInteger(private_ephemeral, 16).abs();
        String username = mUsername.getText().toString();
        String password = mPassword.getText().toString();

        final String private_key = SecureAPI.Hash(salt, username, password);
        BigInteger private_key_int = new BigInteger(private_key, 16);

        BigInteger verifier = SecureAPI.g.modPow(private_key_int, SecureAPI.N);
        BigInteger u = new BigInteger(SecureAPI.Hash(public_ephemeral, server_ephemeral), 16);
        BigInteger scl = server_ephemeral_int.subtract(SecureAPI.k.multiply(verifier));
        BigInteger scr = private_ephemeral_int.add(u.multiply(private_key_int));
        BigInteger sc = scl.abs().modPow(scr, SecureAPI.N);
        shared_key = SecureAPI.Hash(sc.toString(16));


        String xorVal = (new BigInteger(SecureAPI.Hash(SecureAPI.N),16)).xor(new BigInteger(SecureAPI.Hash(SecureAPI.g), 16)).toString(16);

        client_proof = SecureAPI.Hash(
                xorVal,
                SecureAPI.Hash(username),
                salt,
                public_ephemeral,
                server_ephemeral,
                shared_key
        );

        JSONObject request_data = new JSONObject();
        try {
            request_data.put("client_proof",client_proof);
            request_data.put("auth_session", auth_session);
        } catch(JSONException e) {

        }
        final SecureAPI mApi = SecureAPI.getInstance();
        //        JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST, "http://10.0.2.2:5000/verify-auth", request_data,
        mApi.post_endpoint(this.getApplicationContext(), "/verify-auth", request_data,  new Response.Listener<JSONObject>(){
            @Override
            public void onResponse(JSONObject response) {

                String check = SecureAPI.Hash(public_ephemeral, client_proof, shared_key);
                Boolean did_work = false;
                try {
                    if(check.equals(response.getString("server_proof"))){
                        did_work = true;
                        mApi.loadAuthData(response.getString("session_token"), response.getString("UID"), private_key);
                    } else {
                        mTextView.setText("bad proof: ".concat(check.concat(" ".concat(response.getString("server_proof")))));
                    }
                } catch(JSONException e) {
                    Log.i("JSON","Error with the json");
                    mTextView.setText("json exception");
                }
                if(did_work){
                    mTextView.setText("Authenticated");
                    Intent myIntent = new Intent(MainActivity.this, ListActivity.class);
                    MainActivity.this.startActivity(myIntent);
                } else {
                    //mTextView.setText("Not Authenticated");
                }

            }
        }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error) {
                mTextView.setText("verify: ".concat(error.toString()));
            }
        });
        //NetworkQueue.getInstance(this).addToRequestQueue(req);
    }
}
