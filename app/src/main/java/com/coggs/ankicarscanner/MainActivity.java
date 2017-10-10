package com.coggs.ankicarscanner;

import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;
import com.coggs.ankicarscanner.barcode.BarcodeCaptureActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final int BARCODE_READER_REQUEST_CODE = 1;

    private TextView mResultTextView;
    private TextView speedTV;
    private TextView avgTV;
    private TextView lapTV;
    private TextView distTV;
    private TextView disttdTV;
    private TextView flapTV;
    private TextView eventTV;
    private TextView batteryTV;

    private static  String JSON_URL = "";
    private ImageView carIcon;
    private ImageView carView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mResultTextView = (TextView) findViewById(R.id.result_textview);
        speedTV = (TextView) findViewById(R.id.speedTV);
        batteryTV = (TextView) findViewById(R.id.batteryTV);
        avgTV = (TextView) findViewById(R.id.avgTV);
        lapTV = (TextView) findViewById(R.id.lapTV);
        distTV = (TextView) findViewById(R.id.distTV);
        disttdTV = (TextView) findViewById(R.id.disttdTV);
        flapTV = (TextView) findViewById(R.id.flapTV);
        eventTV = (TextView) findViewById(R.id.eventTV);

        carIcon = (ImageView) findViewById(R.id.iconView);
        carView = (ImageView) findViewById(R.id.carView);

        //speedTV.setVisibility(View.INVISIBLE);
        //batteryTV.setVisibility(View.INVISIBLE);


        Button scanBarcodeButton = (Button) findViewById(R.id.scan_barcode_button);
        scanBarcodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), BarcodeCaptureActivity.class);
                startActivityForResult(intent, BARCODE_READER_REQUEST_CODE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == BARCODE_READER_REQUEST_CODE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    Barcode barcode = data.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);
                    Point[] p = barcode.cornerPoints;

                    //mResultTextView.setText(barcode.displayValue);
                    //Log.i("ANKITEST", barcode.displayValue);

                    JSON_URL =  getString(R.string.ankiURL)+ barcode.displayValue;

                    //Log.i("ANKITEST", JSON_URL);
                    //Toast.makeText(this, JSON_URL, Toast.LENGTH_SHORT).show();
                    loadCarData();

                } else mResultTextView.setText(R.string.no_barcode_captured);
            } else Log.e(LOG_TAG, String.format(getString(R.string.barcode_error_format),
                    CommonStatusCodes.getStatusCodeString(resultCode)));
        } else super.onActivityResult(requestCode, resultCode, data);
    }

    private void loadCarData() {
        //getting the progressbar
        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);

        //making the progressbar visible
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

                            //we have the array named hero inside the object
                            //so here we are getting that json array
                            JSONArray itemArray = obj.getJSONArray("items");

                           // Test for a no data response
                            if (itemArray.length() != 0) {
                                //now looping through all the elements of the json array
                                for (int i = 0; i < itemArray.length(); i++) {

                                    //getting the json object of the particular index inside the array
                                    setUI(itemArray.getJSONObject(i));

                                }
                            } else {
                                Toast.makeText(getApplicationContext(), "Car has not raced today", Toast.LENGTH_LONG).show();
                            }

                            //adding the adapter to listview
                            //listView.setAdapter(adapter);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //displaying the error in toast if occurrs

                        Toast.makeText(getApplicationContext(), "ANKITEST:" + error.toString(), Toast.LENGTH_SHORT).show();
                    }
                });

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        //creating a request queue
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        //adding the string request to request queue
        requestQueue.add(stringRequest);
    }

    private void setUI(JSONObject itemObject) {
        //creating a hero object and giving them the values from json object
        //Hero hero = new Hero(heroObject.getString("name"), heroObject.getString("imageurl"));

        try {
        String icn_carName = (itemObject.getString("carname")).toLowerCase().replace(" ", "");
        //Log.i("ANKITEST", carName);

        int batterylvl = (int) itemObject.get("batterylevel");
        int batterymax = (int) itemObject.get("hi_battery");
        int batterymin = (int) itemObject.get("low_battery");
        float batterypc = 1-((batterymax-batterylvl) / (batterymax - batterymin));
        Log.i("ANKITEST", String.valueOf(batterypc));
            //round(1-(max_lvl-current_lvl)/(max_lvl - min_lvl),2)

        int imageid = getResources().getIdentifier(icn_carName, "drawable", getPackageName());
        carView.setImageResource(imageid);

        String carPanel = itemObject.getString("carname");
        speedTV.setText(getString(R.string.speed) + " " + itemObject.getString("maxspeed"));
        batteryTV.setText(getString(R.string.battery) + " " + itemObject.get("batterylevel") + "mv");
        lapTV.setText(getString(R.string.laps) + " " + itemObject.get("lapcount"));
        distTV.setText(getString(R.string.Distance) + " " + itemObject.get("totdist"));
        disttdTV.setText(getString(R.string.DistanceTD) + " " + itemObject.get("tddist"));
        flapTV.setText(getString(R.string.fastest_lap) + " " + itemObject.get("fastestlap") + "s");
        avgTV.setText(getString(R.string.avg) + " " + itemObject.get("avg"));
        eventTV.setText(getString(R.string.event) + " " + itemObject.get("event"));

        speedTV.setVisibility(View.VISIBLE);
        batteryTV.setVisibility(View.VISIBLE);
        lapTV.setVisibility(View.VISIBLE);
        distTV.setVisibility(View.VISIBLE);
        disttdTV.setVisibility(View.VISIBLE);
        flapTV.setVisibility(View.VISIBLE);
        avgTV.setVisibility(View.VISIBLE);
        eventTV.setVisibility(View.VISIBLE);

        // carPanel += "\nCrashes Today: " + itemObject.getString("crashes");
        //carPanel += "\nLaps Today: " + itemObject.getString("lapcount");
        //carPanel += "\n\n at " + itemObject.get("event");
        // speed, avg, lap, event, battery

        mResultTextView.setText(carPanel);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
