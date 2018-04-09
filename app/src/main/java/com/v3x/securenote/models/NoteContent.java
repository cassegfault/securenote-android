package com.v3x.securenote.models;

import android.util.Log;

import com.android.volley.toolbox.JsonObjectRequest;
import com.v3x.securenote.Encryption;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NoteContent {

    public static final List<NoteItem> ITEMS = new ArrayList<NoteItem>();

    public static final Map<String, NoteItem> ITEM_MAP = new HashMap<String, NoteItem>();

    public static void addItem(NoteItem item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }
    public static void clearItems(){
        ITEMS.clear();
        ITEM_MAP.clear();
    }
    public static NoteItem getById(String id){
        return ITEM_MAP.get(id);
    }
    public static void updateItem(NoteItem note){
        ITEM_MAP.remove(note.id);
        ITEM_MAP.put(note.id,note);
        ITEMS.clear();
        ITEMS.addAll(ITEM_MAP.values());
    }

    public static class NoteItem {
        public String id;
        public String iv;
        public String data;
        public String title;
        public String body;

        public NoteItem(String id, String content, String iv) {
            this.id = id;
            this.iv = iv;
            this.data = content;
        }

        public NoteItem(JSONObject noteJSON){
            this.id = noteJSON.optString("id","");
            this.data = noteJSON.optString("data","");
            this.iv = noteJSON.optString("iv","");

        }
        public void didDecrypt(){
            try {
                JSONObject jsonData = new JSONObject(this.data);
                this.body = jsonData.optString("body", "");
                this.title = jsonData.optString("title", "");
            } catch (JSONException e) {
                Log.e("JSON","Error decoding json from note: " + this.data + "\n\n" + e.toString());
            }
        }


        public JSONObject toJSON() {
            JSONObject json = new JSONObject();

            try {
                json.put("id", id);
                json.put("iv", iv);
                json.put("data", data);
            } catch (JSONException e)  {
                Log.e("JSON",e.toString());
            }
            return json;
        }

        @Override
        public String toString() {
            JSONObject json = new JSONObject();

            try {
                json.put("id", id);
                json.put("iv", iv);
                json.put("data", data);
            } catch (JSONException e)  {
                Log.e("JSON",e.toString());
            }
            return json.toString();
        }
    }
}
