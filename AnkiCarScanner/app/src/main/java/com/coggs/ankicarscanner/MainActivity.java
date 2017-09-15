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
        carIcon = (ImageView) findViewById(R.id.iconView);
        carView = (ImageView) findViewById(R.id.carView);

        speedTV.setVisibility(View.INVISIBLE);
        batteryTV.setVisibility(View.INVISIBLE);


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

                    JSON_URL =  getString(R.string.ankiURL)+ barcode.displayValue;

                    //Toast.makeText(this, ankiURL, Toast.LENGTH_SHORT).show();
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

                            //now looping through all the elements of the json array
                            for (int i = 0; i < itemArray.length(); i++) {
                                //getting the json object of the particular index inside the array
                                JSONObject itemObject = itemArray.getJSONObject(i);

                                //creating a hero object and giving them the values from json object
                                //Hero hero = new Hero(heroObject.getString("name"), heroObject.getString("imageurl"));
                                String carName = (itemObject.getString("carname")).toLowerCase();

                                //Log.i("REST", carName);
                                int imageid = getResources().getIdentifier(carName, "drawable",getPackageName());
                                carView.setImageResource(imageid);

                                String carPanel = carName;
                                //carPanel += "\nCrashes Today: " + itemObject.getString("crashes");
                                carPanel += "\nLaps Today: " + itemObject.getString("lapcount");

                                carPanel += "\n\n at " + itemObject.get("event");

                                speedTV.setText(getString(R.string.speed) + itemObject.getString("maxspeed"));
                                batteryTV.setText(getString(R.string.battery) + itemObject.get("batterylevel") + "mv");

                                speedTV.setVisibility(View.VISIBLE);
                                batteryTV.setVisibility(View.VISIBLE);

                                mResultTextView.setText(carPanel);

                                // set screen elements
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
                        Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

        //creating a request queue
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        //adding the string request to request queue
        requestQueue.add(stringRequest);
    }

}
