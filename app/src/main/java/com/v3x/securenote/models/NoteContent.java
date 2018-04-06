package com.v3x.securenote.models;

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

    public static class NoteItem {
        public String id = "";
        public String iv = "";
        public String data = "";

        public NoteItem(String id, String content, String iv) {
            this.id = id;
            this.iv = iv;
            this.data = content;
        }

        public NoteItem(JSONObject noteJSON){
            try{
                this.id = noteJSON.getString("id");
                this.data = noteJSON.getString("data");
                this.iv = noteJSON.getString("iv");
            } catch(JSONException e) {

            }
        }
        public void didDecrypt(){

        }

        @Override
        public String toString() {
            return data;
        }
    }
}
