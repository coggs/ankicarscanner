package com.coggs.ankicarscanner;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class moreDataActivity extends AppCompatActivity {

    float x1, x2, y1, y2;
    String JSON_URL;

    // Group Data
    private TextView carsTV, lapsTV, crashesTV, devicesTV, distanceTV, firstregTV, lastregTV, messagesTV, portTV, hostipTV, driveuiTV, registrationsTV;
    String cars, laps, crashes, devices, distance, firstreg, lastreg, messages, port, registrations;

    private String groupId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_more_data);

        Bundle bundle = getIntent().getExtras();
        groupId = bundle.getString("anki");

        carsTV = (TextView) findViewById(R.id.cars);
        lapsTV = (TextView) findViewById(R.id.laps);
        crashesTV = (TextView) findViewById(R.id.crashes);
        devicesTV = (TextView) findViewById(R.id.devices);
        distanceTV = (TextView) findViewById(R.id.distance);
        firstregTV = (TextView) findViewById(R.id.firstreg);
        lastregTV = (TextView) findViewById(R.id.lastreg);
        messagesTV = (TextView) findViewById(R.id.messages);
        portTV = (TextView) findViewById(R.id.cars);
        registrationsTV = (TextView) findViewById(R.id.cars);

        hostipTV = (TextView) findViewById(R.id.hostip);
        hostipTV.setMovementMethod(LinkMovementMethod.getInstance());
        driveuiTV = (TextView) findViewById(R.id.driveUI);
        driveuiTV.setMovementMethod(LinkMovementMethod.getInstance());

        if (groupId == null ) {
            Toast.makeText(this, "No Group Selected", Toast.LENGTH_SHORT).show();
        } else {

            // Go get Group Data

            JSON_URL = getString(R.string.ankiGroupURL) +  Uri.encode(groupId);
            Log.i("Anki", JSON_URL);

            //making the progressbar visible
            final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar2);
            progressBar.setVisibility(View.VISIBLE);

            //creating a string request to send request to the url
            StringRequest stringRequest = new StringRequest(Request.Method.GET, JSON_URL,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            //hiding the progressbar after completion
                            progressBar.setVisibility(View.INVISIBLE);

                            try {
                                //getting the whole json object from the response
                                JSONObject obj = new JSONObject(response);
                                JSONArray itemArray = obj.getJSONArray("items");
                                setUI(itemArray);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            //displaying the error in toast if occurrs
                            Toast.makeText(getApplicationContext(), "Oops:" + error.toString(), Toast.LENGTH_SHORT).show();
                        }

                    }){
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("x-api-key", getString(R.string.apikey));
                    return params;
                }
            };

            stringRequest.setRetryPolicy(new DefaultRetryPolicy(30000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            //creating a request queue
            RequestQueue requestQueue = Volley.newRequestQueue(this);

            //adding the string request to request queue
            requestQueue.add(stringRequest);
        }

    }

//    public boolean onTouchEvent(MotionEvent touchevent) {
//        switch (touchevent.getAction()){
//            case MotionEvent.ACTION_DOWN;
//                x1 = touchevent.getX();
//                y1 = touchevent.getY();
//            break;
//            case MotionEvent.ACTION_UP:
//                x2 = touchevent.getX();
//                y2 = touchevent.getY();
//
//                if (x1 < x2) {
//                    Intent i = new Intent(MainActivity.this SwipeLeft.class);
//                    startActivity(i);
//                }
//                break;
//        }
//        return false;
//
//    }

    private void setUI(JSONArray itemArray) {

        try {
            if (itemArray.length() != 0) {
                //now looping through all the elements of the json array
                    //getting the json object of the particular index inside the array
                    lapsTV.setText(getString(R.string.laps_text) + itemArray.getJSONObject(0).getString("value"));
                    carsTV.setText(getString(R.string.cars_text) + itemArray.getJSONObject(1).getString("value"));
                    crashesTV.setText(getString(R.string.crashes_text) + itemArray.getJSONObject(2).getString("value"));
                    devicesTV.setText(getString(R.string.devices_text) + itemArray.getJSONObject(3).getString("value"));
                    distanceTV.setText(getString(R.string.distance_text) + itemArray.getJSONObject(4).getString("value"));
                    firstregTV.setText(getString(R.string.first_registration_text) + itemArray.getJSONObject(5).getString("value"));
                    lastregTV.setText(getString(R.string.latest_registration_text) + itemArray.getJSONObject(7).getString("value"));
                    messagesTV.setText(getString(R.string.number_of_messages_text) + itemArray.getJSONObject(8).getString("value"));
                    portTV.setText(getString(R.string.port_text) + itemArray.getJSONObject(9).getString("value"));
                    registrationsTV.setText(getString(R.string.registrations_text) + itemArray.getJSONObject(10).getString("value"));

                    setAsLink(hostipTV, itemArray.getJSONObject(6).getString("value"));
                    setAsLink(driveuiTV, itemArray.getJSONObject(6).getString("value")+":7901");

            } else {
                Toast.makeText(getApplicationContext(), "No Data for Group", Toast.LENGTH_LONG).show();
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    private void setAsLink(TextView view, String url){
        Pattern pattern = Pattern.compile(url);
        Linkify.addLinks(view, pattern, "http://");
        view.setText(Html.fromHtml("<a href='http://"+url+"'>http://"+url+"/</a>"));
    }
}
