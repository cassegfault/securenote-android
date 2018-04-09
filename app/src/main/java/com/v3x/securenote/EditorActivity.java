package com.v3x.securenote;

import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.v3x.securenote.models.NoteContent;
import com.v3x.securenote.network.SecureAPI;

import org.json.JSONException;
import org.json.JSONObject;

public class EditorActivity extends AppCompatActivity {

    TextView mTitle;
    TextView mBody;
    String editingId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        Bundle b = getIntent().getExtras();

        mTitle = (TextView)findViewById(R.id.editor_title);
        mBody = (TextView)findViewById(R.id.editor_body);

        if(b != null){
            String noteId = b.getString("id");
            if (noteId != null) {
                NoteContent.NoteItem editorNote = NoteContent.getById(noteId);
                mTitle.setText(editorNote.title);
                mBody.setText(editorNote.body);
                editingId = noteId;
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                saveNote();
                NavUtils.navigateUpFromSameTask(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed(){
        Log.i("EDIT", "Back pressed");
        saveNote();
    }

    public void saveNote(){
        final SecureAPI mApi = SecureAPI.getInstance();
        // create a noteitem, encrypt it, convert it to json, send it to server

        JSONObject njson = new JSONObject();
        JSONObject jsoncontent = new JSONObject();
        if(editingId == null || editingId.equals("")){
            try {
                jsoncontent.put("body", mBody.getText());
                jsoncontent.put("title", mTitle.getText());
                njson.put("data", jsoncontent.toString());
            } catch(JSONException e){
                Log.e("JSON",e.toString());
            }
            NoteContent.NoteItem newNote = new NoteContent.NoteItem(njson);
            mApi.encryptNote(newNote);
            mApi.put_auth_endpoint(this, "/notes", newNote.toJSON(), new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    NoteContent.NoteItem newNote = new NoteContent.NoteItem(response);
                    mApi.decryptNote(newNote);
                    NoteContent.addItem(newNote);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("NET","Failed to save note");
                }
            });
        } else {
            try {
                jsoncontent.put("body", mBody.getText());
                jsoncontent.put("title", mTitle.getText());
                jsoncontent.put("id",editingId);
                njson.put("data", jsoncontent.toString());
            } catch(JSONException e) {
                Log.e("JSON",e.toString());
            }
            NoteContent.NoteItem newNote = new NoteContent.NoteItem(njson);
            mApi.encryptNote(newNote);

            mApi.post_auth_endpoint(this, "/notes/" + editingId, newNote.toJSON(), new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    NoteContent.NoteItem newNote = new NoteContent.NoteItem(response);
                    mApi.decryptNote(newNote);
                    NoteContent.updateItem(newNote);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("NET","Failed to save note");
                }
            });
        }

    }
}
