package com.v3x.securenote;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.v3x.securenote.models.NoteContent;
import com.v3x.securenote.network.SecureAPI;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class MainActivity extends AppCompatActivity implements NoteFragment.OnListFragmentInteractionListener  {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Test whether or not we need to log in \ auth
        //SharedPreferences prefs = this.getApplicationContext().getSharedPreferences("auth",Context.MODE_PRIVATE);
        Context ctx = this.getApplicationContext();
        SharedPreferences prefs =  ctx.getSharedPreferences("com.v3x.auth",Context.MODE_PRIVATE);
        String testKey = prefs.getString("key", null);

        if(!SecureAPI.getInstance().isAuthenticated()) {
            if (testKey != null) {
                // Go to password decrypt
                Intent mIntent = new Intent(MainActivity.this, PassphraseActivity.class);
                MainActivity.this.startActivity(mIntent);
            } else {
                // Go to Login
                Intent mIntent = new Intent(MainActivity.this, LoginActivity.class);
                MainActivity.this.startActivity(mIntent);
            }
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent myIntent = new Intent(MainActivity.this, EditorActivity.class);
                MainActivity.this.startActivity(myIntent);

            }
        });

        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        RecyclerView rv = (RecyclerView)findViewById(R.id.note_list);

        mViewAdapter = new NoteRecyclerViewAdapter(NoteContent.ITEMS, this);
        rv.setLayoutManager(llm);
        rv.setAdapter( mViewAdapter );
        getNoteItems();
    }

    NoteRecyclerViewAdapter mViewAdapter;
    @Override
    public void onListFragmentInteraction(NoteContent.NoteItem item) {

    }

    protected void getNoteItems(){
        NoteContent.clearItems();
        SecureAPI mApi = SecureAPI.getInstance();
        mApi.get_auth_endpoint(this.getApplicationContext(),"/notes",new Response.Listener<JSONObject>(){
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray notes = response.getJSONArray("notes");
                    Log.i("SN","Notes: ".concat(String.valueOf(notes.length())));
                    for (int i = 0; i < notes.length(); i++) {
                        JSONObject noteItem = notes.getJSONObject(i);
                        NoteContent.NoteItem note = new NoteContent.NoteItem(noteItem);
                        SecureAPI.getInstance().decryptNote(note);
                        NoteContent.addItem(note);
                    }
                    mViewAdapter.notifyDataSetChanged();
                } catch(JSONException e) {
                    Log.i("JSON","Problem reading notes");
                }
            }
        }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
    }


}
