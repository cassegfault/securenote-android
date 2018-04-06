package com.v3x.securenote;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.v3x.securenote.models.NoteContent;
import com.v3x.securenote.network.NetworkQueue;
import com.v3x.securenote.network.SecureAPI;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ListActivity extends AppCompatActivity implements NoteFragment.OnListFragmentInteractionListener {
    NoteRecyclerViewAdapter mViewAdapter;
    @Override
    public void onListFragmentInteraction(NoteContent.NoteItem item) {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*
                Intent myIntent = new Intent(MainActivity.this, NoteEditorActivity.class);
                MainActivity.this.startActivity(myIntent);
                */
            }
        });

        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        RecyclerView rv = (RecyclerView)findViewById(R.id.note_list);

        mViewAdapter = new NoteRecyclerViewAdapter(NoteContent.ITEMS);
        rv.setLayoutManager(llm);
        rv.setAdapter( mViewAdapter );
        getNoteItems();
    }

    protected void getNoteItems(){
        SecureAPI mApi = SecureAPI.getInstance();
        //);
        //JsonArrayRequest jreq = new JsonArrayRequest(Request.Method.GET, "http://10.0.2.2:5000/notes",null,
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
        //NetworkQueue.getInstance(this).addToRequestQueue(jreq);
    }


}
