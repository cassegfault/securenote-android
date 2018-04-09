package com.v3x.securenote;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.v3x.securenote.network.SecureAPI;

public class PassphraseActivity extends AppCompatActivity {
    TextView mPassphrase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passphrase);

        mPassphrase = findViewById(R.id.unlock_passphrase);
        Button mButton = findViewById(R.id.unlock_button);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                testUnlock();
            }
        });
        Button loginButton = findViewById(R.id.passphrase_gologin);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goLogin();
            }
        });
    }

    private void testUnlock(){
        SecureAPI mApi = SecureAPI.getInstance();

        if(mApi.unlock(this.getApplicationContext(),mPassphrase.getText().toString())){
            Intent mIntent = new Intent(PassphraseActivity.this, MainActivity.class);
            PassphraseActivity.this.startActivity(mIntent);
        } else {
            mPassphrase.setError(getString(R.string.error_bad_credentials));
        }
    }

    private void goLogin(){
        Intent mIntent = new Intent(this,LoginActivity.class);
        startActivity(mIntent);
    }
}
