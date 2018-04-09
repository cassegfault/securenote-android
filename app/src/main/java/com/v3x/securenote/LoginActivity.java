package com.v3x.securenote;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.v3x.securenote.network.NetworkQueue;
import com.v3x.securenote.network.SecureAPI;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigInteger;


public class LoginActivity extends AppCompatActivity {

    private AutoCompleteTextView mUsernameView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Set up the login form.
        mUsernameView = (AutoCompleteTextView) findViewById(R.id.username);

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mUsernameSignInButton = (Button) findViewById(R.id.username_sign_in_button);
        mUsernameSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {

        // Reset errors.
        mUsernameView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String username = mUsernameView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // We need to do this only for signup, not for auth
        // Check for a valid password, if the user entered one.
        /*if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }*/

        // Check for a valid email address.
        /*if (TextUtils.isEmpty(username)) {
            mUsernameView.setError(getString(R.string.error_field_required));
            focusView = mUsernameView;
            cancel = true;
        } else if (!isEmailValid(username)) {
            mUsernameView.setError(getString(R.string.error_invalid_username));
            focusView = mUsernameView;
            cancel = true;
        }*/

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            // login does all network activity on separate thread
            doLogin(username,password);
        }
    }

    private boolean isUsernameValid(String username) {
        return username.length() > 2;
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    private String private_ephemeral;
    private String public_ephemeral;
    private String shared_key;
    private String client_proof;

    public void doLogin(final String username, final String password){
        showProgress(true);
        String url = "http://10.0.2.2:5000/authentication";
        JSONObject requestData = new JSONObject();

        private_ephemeral = Encryption.toHex(SecureAPI.cryptrand(1024));
        BigInteger public_ephemeral_int = SecureAPI.g.modPow((new BigInteger(private_ephemeral, 16)).abs(), SecureAPI.N);

        public_ephemeral = public_ephemeral_int.toString(16); //Encryption.toHex(public_ephemeral_int.toByteArray());

        try {
            requestData.put("username", username);
            requestData.put("client_ephemeral", public_ephemeral);
            Log.i("SRP", "Public Ephem: " + public_ephemeral);
        } catch(JSONException e){

        }


        JsonObjectRequest jsonReq = new JsonObjectRequest(
                Request.Method.POST, url, requestData, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.i("AUTH",response.toString());
                try {
                    send_proof(username, password, response.getString("server_ephemeral"), response.getString("salt"), response.getString("auth_session"));
                } catch(JSONException e) {

                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i("AUTH",error.toString());
                showProgress(false);
            }
        });
        NetworkQueue.getInstance(this).addToRequestQueue(jsonReq);

    }

    private void send_proof(String username, final String password, final String server_ephemeral, final String salt, String auth_session) {
        BigInteger server_ephemeral_int = new BigInteger(server_ephemeral, 16);
        BigInteger private_ephemeral_int = new BigInteger(private_ephemeral, 16).abs();

        final String private_key = SecureAPI.Hash(salt, username, password);
        BigInteger private_key_int = new BigInteger(private_key, 16);

        BigInteger verifier = SecureAPI.g.modPow(private_key_int, SecureAPI.N);
        BigInteger u = new BigInteger(SecureAPI.Hash(public_ephemeral, server_ephemeral), 16);
        BigInteger scl = server_ephemeral_int.subtract(SecureAPI.k.multiply(verifier));
        BigInteger scr = private_ephemeral_int.add(u.multiply(private_key_int));
        BigInteger sc = scl.abs().modPow(scr, SecureAPI.N);
        shared_key = SecureAPI.Hash(sc.toString(16));


        String xorVal = (new BigInteger(SecureAPI.Hash(SecureAPI.N), 16)).xor(new BigInteger(SecureAPI.Hash(SecureAPI.g), 16)).toString(16);

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
            request_data.put("client_proof", client_proof);
            request_data.put("auth_session", auth_session);
        } catch (JSONException e) {

        }
        final SecureAPI mApi = SecureAPI.getInstance();
        final Context appContext = this.getApplicationContext();

        mApi.post_endpoint(this.getApplicationContext(), "/verify-auth", request_data, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                String check = SecureAPI.Hash(public_ephemeral, client_proof, shared_key);
                Boolean did_work = false;
                try {
                    if (check.equals(response.getString("server_proof"))) {
                        did_work = true;
                        mApi.loadAuthData(appContext, response.getString("session_token"), response.getString("UID"), private_key, password, salt);
                    } else {
                        Log.i("AUTH", "bad proof: ".concat(check.concat(" ".concat(response.getString("server_proof")))));
                    }
                } catch (JSONException e) {
                    Log.i("AUTH", "json exception");
                }
                if (did_work) {
                    Log.i("AUTH", "Authenticated");
                    Intent myIntent = new Intent(LoginActivity.this, MainActivity.class);
                    LoginActivity.this.startActivity(myIntent);
                } else {
                    //mTextView.setText("Not Authenticated");
                    mPasswordView.setError(getString(R.string.error_bad_credentials));
                }
                showProgress(false);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i("AUTH", "verify: ".concat(error.toString()));
                showProgress(false);
            }
        });
    }
}

