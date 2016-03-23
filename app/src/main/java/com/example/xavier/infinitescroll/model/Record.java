package com.example.xavier.infinitescroll.model;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by xavier on 16/3/23.
 */
public class Record {

    public String created;

    public int id;

    public Source source;

    public Destination destination;

    public Record(JSONObject jsonObject)
    {
        try {
            created = jsonObject.getString("created");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            id = jsonObject.getInt("id");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            source = new Source(jsonObject.getJSONObject("source"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            destination = new Destination(jsonObject.getJSONObject("destination"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static class Source
    {
        public String sender;
        public String note;

        public Source(JSONObject jsonObject)
        {
            try {
                sender = jsonObject.getString("sender");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            try {
                note = jsonObject.getString("note");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public static class Destination
    {
        public String recipient;
        public String currency;
        public int amount;

        public Destination(JSONObject jsonObject)
        {
            try {
                recipient = jsonObject.getString("recipient");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            try {
                currency = jsonObject.getString("currency");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            try {
                amount = jsonObject.getInt("amount");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}