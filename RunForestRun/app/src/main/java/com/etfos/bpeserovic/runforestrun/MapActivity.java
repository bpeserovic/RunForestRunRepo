package com.etfos.bpeserovic.runforestrun;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Path;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

/**
 * Created by Bobo on 11.6.2017..
 */

public class MapActivity extends FragmentActivity implements OnMapReadyCallback {
    ArrayList markerPoints = new ArrayList();
    private GoogleMap mMap;

    LocationManager myLocationManager;
    String provider;
    Criteria criteria;

//    TimeDB timeDb = new TimeDB();
    //public TimeDBHelper dbHelper = new TimeDBHelper(this);
//    ArrayAdapter<Times> arrayAdapterMap = new ArrayAdapter<Times>(timeDb, android.R.layout.simple_list_item_1);

    //Timer
    TextView mTime;
    long startTime = 0;
    Handler timerHandler = new Handler();

    public String myTime;
//    public Times t = new Times(myTime);

    Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            long millis = System.currentTimeMillis() - startTime;
            int seconds = (int) (millis / 1000);
            int minutes = seconds / 60;
            int hours = minutes / 60;
            seconds = seconds % 60;

            mTime.setText(String.format("%d:%02d:%02d", hours, minutes, seconds));

            timerHandler.postDelayed(this, 500);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mGoogleMap);
        mapFragment.getMapAsync(this);

        //location stuff
        criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        myLocationManager = (LocationManager) getSystemService(Service.LOCATION_SERVICE);
//        getActivity().getSystemService(Service.LOCATION_SERVICE);
        provider = myLocationManager.getBestProvider(criteria, true);

        //threads
//        workOnBack();

        //timer stuff
        mTime = (TextView) findViewById(R.id.mTime);
        mTime.setText("0:00:00");

        //database
//        timeDb.myTimes = timeDb.dbHelper.getTimes();
//        timeDb.myAdapter = new ArrayAdapter<Times>(getApplicationContext(), android.R.layout.simple_list_item_1, timeDb.myTimes);

        final Button bStart = (Button) findViewById(R.id.mStartButton);
        bStart.setText("START");
        bStart.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Button bStart = (Button) v;
                if (bStart.getText().equals("STOP")) {
                    myTime = mTime.getText().toString();
                    Log.d("BOBO myTime: ", myTime);

                    Times t = new Times(myTime);
                    if(MainActivity.dbHelper != null)
                    MainActivity.dbHelper.addTime(t);
//                    timeDb.myAdapter.clear();
//                    timeDb.myAdapter.addAll(timeDb.dbHelper.getTimes());
//                    timeDb.myAdapter.notifyDataSetChanged();


//                    //dodaje vrijeme u db na stop
//                    Times t = new Times(myTime);
//                    Log.d("BOBO t: ", t.toString());
////                    timeDb.myTimes = timeDb.dbHelper.getTimes();
////                    timeDb.myAdapter = new ArrayAdapter<Times>(getApplicationContext(), android.R.layout.simple_list_item_1, timeDb.myTimes);
//                    timeDb.dbHelper.addTime(t);
//                    Log.d("BOBOOOOO t: ", t.toString());
//                    timeDb.myAdapter.clear();
//                    timeDb.myAdapter.addAll(timeDb.dbHelper.getTimes());
////                    timeDb.lvTimes.setAdapter(timeDb.myAdapter);
//                    timeDb.myAdapter.notifyDataSetChanged();

                    timerHandler.removeCallbacks(timerRunnable);
                    bStart.setText("START");
                } else {
                    startTime = System.currentTimeMillis();
                    timerHandler.postDelayed(timerRunnable, 0);
                    bStart.setText("STOP");
                }
            }
        });

        //clear points and route on button
        Button bReset = (Button) findViewById(R.id.mResetRouteButton);
        bReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                markerPoints.clear();
                mMap.clear();
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        timerHandler.removeCallbacks(timerRunnable);
        Button bStart = (Button) findViewById(R.id.mStartButton);
        bStart.setText("START");
    }


//    public void workOnBack(){
//        DBThread dbThread = new DBThread();
//        dbThread.start();
//    }
//
//    private class DBThread extends Thread implements Runnable{
//
//        @Override
//        public void run() {
//            Times t = new Times(1, "MEMES");
//            timeDb.dbHelper.addTime(t);
//            Log.d("BOBOOOOO t: ", t.toString());
//            timeDb.myAdapter.clear();
//            timeDb.myAdapter.addAll(timeDb.dbHelper.getTimes());
//            timeDb.myAdapter.notifyDataSetChanged();
//        }
//    }

    LocationListener mLocationListener = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
            onChange(location);
        }
    };

    public void onChange(Location location) {
        String latlongString = "No location available";
        if (location != null) {
            latlongString = "Lat:" + location.getLatitude() + "\nLon:"
                    + location.getLongitude();
        }
        //tvLocation.setText(latlongString);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        UiSettings uisettings = this.mMap.getUiSettings();
        uisettings.setZoomControlsEnabled(true);
        uisettings.setMyLocationButtonEnabled(true);
        uisettings.setZoomGesturesEnabled(true);
        uisettings.setCompassEnabled(true);
        uisettings.setScrollGesturesEnabled(true);
//        LatLng sydney = new LatLng(-34, 151);
        //mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 16));

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {


                // Adding new item to the ArrayList
                markerPoints.add(latLng);

                // Creating MarkerOptions
                MarkerOptions options = new MarkerOptions();

                // Setting the position of the marker
                options.position(latLng);

                if (markerPoints.size() == 1) {
                    options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                } else if (markerPoints.size() == 2) {
                    options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                }

                // Add new marker to the Google Map Android API V2
                mMap.addMarker(options);

                // Checks, whether start and end locations are captured
                if (markerPoints.size() >= 2) {
                    LatLng origin = (LatLng) markerPoints.get(0);
                    LatLng dest = (LatLng) markerPoints.get(1);

                    // Getting URL to the Google Directions API
                    String url = getDirectionsUrl(origin, dest);

                    DownloadTask downloadTask = new DownloadTask();

                    // Start downloading json data from Google Directions API
                    downloadTask.execute(url);
                }

            }
        });

        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);

    }

    private class DownloadTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... url) {

            String data = "";

            try {
                data = downloadUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();


            parserTask.execute(result);

        }
    }


    /**
     * A class to parse the Google Places in JSON format
     */
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList points = null;
            PolylineOptions lineOptions = null;
            MarkerOptions markerOptions = new MarkerOptions();

            //traversing through all the routes
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList();
                lineOptions = new PolylineOptions();

                //fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                //fetching all the points in i-th route
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);


                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                lineOptions.addAll(points);
                lineOptions.width(10);
                lineOptions.color(Color.RED);
                lineOptions.geodesic(true);

            }

            if(lineOptions == null)
            {
                return;
            }
            else {
// Drawing polyline in the Google Map for the i-th route
                mMap.addPolyline(lineOptions);
            }
        }
    }

    private String getDirectionsUrl(LatLng origin, LatLng dest) {

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

        // Sensor enabled
        String sensor = "sensor=false";
        String mode = "mode=walking";
        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor + "&" + mode;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;


        return url;
    }

    /**
     * A method to download json data from url
     */
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            urlConnection = (HttpURLConnection) url.openConnection();

            urlConnection.connect();

            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        } catch (Exception e) {
            Log.d("Exception", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }
}
