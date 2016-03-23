package com.example.xavier.infinitescroll.util;

import android.app.Activity;
import android.net.Uri;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonArrayRequest;
import com.example.xavier.infinitescroll.ISApp;

import org.json.JSONArray;

public class DataRequestHelper {

    public static final String PARAM_START_INDEX = "startIndex";
    public static final String PARAM_NUM = "num";

    public static final String URL_DATA_ENDPOINT = "https://hook.io/syshen/infinite-list";

    public static final int REQUEST_SIZE = 30;

    public static final String REQUEST_SIZE_STR = String.valueOf(REQUEST_SIZE);

    public static void retrieveData(final Activity cx, final int startIndex, final Response.Listener<JSONArray> responseListener, final Response.ErrorListener errorListener)
    {
        Uri builtUri = Uri.parse(URL_DATA_ENDPOINT + "?")
                .buildUpon()
                .appendQueryParameter(PARAM_START_INDEX, String.valueOf(startIndex))
                .appendQueryParameter(PARAM_NUM, REQUEST_SIZE_STR)
                .build();

        Log.d("DataRequestHelper", "request url = " + builtUri.toString());
        String requestUri = builtUri.toString();

        JsonArrayRequest jsonObjReq = new JsonArrayRequest(Request.Method.GET, requestUri, "", responseListener, errorListener);

        ((ISApp)cx.getApplication()).addToRequestQueue(jsonObjReq);
    }
}
