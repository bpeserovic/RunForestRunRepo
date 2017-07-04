package com.etfos.bpeserovic.runforestrun;

import android.app.Activity;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Path;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Time;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

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

//    NumberFormat nf = NumberFormat.getNumberInstance(Locale.GERMAN);
//    DecimalFormat df = (DecimalFormat)nf;

    //notifikacija
//    private static final String PROX_ALERT_INTENT = "com.etfos.bpeserovic.runforestrun.ProximityAlert";
    public static String alertTitle;
    public static String alertText;
    private static final float RADIUS = 100; //radijus u metrima
    private static final long EXPIRATION = -1; //istek notifikacije u ms, -1 za beskonačno
    private int alertID = 0;
    private static final String PROXI_INTENT = "PROXI_INTENT";

    //markeri
    ArrayList markerPoints = new ArrayList();
    ArrayList POImarkers = new ArrayList();

    //google map
    private GoogleMap mMap;

    LocationManager myLocationManager;
    String provider;
    Criteria criteria;
    android.location.LocationListener mLocationListener;
    Location mCurrentLocation;

    //opis za marker
    public String dialogTitleText;

    //provjerava je li user dodaje poi markere
    public boolean isPOIMarker = false;

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

//    private void addProximityAlert(double latitude, double longitude) {
////        Intent intent = new Intent(PROX_ALERT_INTENT);
////        PendingIntent proximityIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, 0);
//
//        Intent intent = new Intent("com.etfos.bpeserovic.ENTERING_AREA");
//        PendingIntent proximityIntent = PendingIntent.getBroadcast(getApplicationContext(), 10, intent, 0);
//
//        if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//            return;
//        }
//        myLocationManager.addProximityAlert(
//                latitude,
//                longitude,
//                100, //radius u metrima
//                -1, //vrijeme za proxyalert u milisec
//                proximityIntent
//        );
////        IntentFilter filter = new IntentFilter(PROX_ALERT_INTENT);
////        this.registerReceiver(new ProximityIntentReceiver(), filter);
//    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mGoogleMap);
        mapFragment.getMapAsync(this);

        //button za kontrolu koje markere dodajem
        final Button bAddPOI = (Button) findViewById(R.id.mAddPOI);
        bAddPOI.setText(getString(R.string.bAddPoi));

        //alert stuff
        Context alertContext = getApplicationContext();
        Resources res = alertContext.getResources();
        alertTitle = res.getString(R.string.alertTitle);
        alertText = res.getString(R.string.alertText);

        //location stuff
        myLocationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
//        getActivity().getSystemService(Service.LOCATION_SERVICE);
        provider = myLocationManager.getBestProvider(criteria, true);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }


        this.mLocationListener = new android.location.LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                mCurrentLocation = location;
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };


        initializeReceiver();


        //timer stuff
        mTime = (TextView) findViewById(R.id.mTime);
        mTime.setText("0:00:00");

        final Button bStart = (Button) findViewById(R.id.mStartButton);
        bStart.setText(getString(R.string.bStart));
        bStart.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Button bStart = (Button) v;
                if (bStart.getText().equals(getString(R.string.bStartStop))) {
                    myTime = mTime.getText().toString();
                    Log.d("BORIS myTime: ", myTime);

                    Times t = new Times(myTime);
                    if (MainActivity.dbHelper != null)
                        MainActivity.dbHelper.addTime(t);

                    timerHandler.removeCallbacks(timerRunnable);
                    bStart.setText(getString(R.string.bStart));
                } else {
                    startTime = System.currentTimeMillis();
                    timerHandler.postDelayed(timerRunnable, 0);
                    bStart.setText(getString(R.string.bStartStop));
                }
            }
        });

        //clear points on map and array list
        Button bReset = (Button) findViewById(R.id.mResetRouteButton);
        bReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                markerPoints.clear();
                mMap.clear();
                isPOIMarker = false;
                POImarkers.clear();
                bAddPOI.setText(getString(R.string.bAddPoi));
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        this.myLocationManager.requestLocationUpdates(provider, 500, 1, mLocationListener);
        Log.d("BORIS ", "Started Location Tracking");
    }

    @Override
    public void onPause() {
        super.onPause();
        timerHandler.removeCallbacks(timerRunnable);
        Button bStart = (Button) findViewById(R.id.mStartButton);
        bStart.setText(getString(R.string.bStart));
        if(mLocationListener != null){
            myLocationManager.removeUpdates(this.mLocationListener);
            Log.d("BORIS ", "Location tracking stopped");
        }
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

        //buttons
        final Button bDrawRoute = (Button) findViewById(R.id.mDrawRoute);
        final Button bAddPOI = (Button) findViewById(R.id.mAddPOI);
        bAddPOI.setText(getString(R.string.bAddPoi));

        final Context context = this;

        //klikanje po mapi
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {

                Log.d("BORIS latLng: ", latLng.toString());

//                // Already 10 locations with 8 waypoints and 1 start location and 1 end location.
//                // Upto 8 waypoints are allowed in a query for non-business users
//                if(markerPoints.size()>=10) {
//                    return;
//                }

                // Adding new item to the ArrayList
                //dodavanje route markera
                if(isPOIMarker == false) {

                    markerPoints.add(latLng);
                    // Creating MarkerOptions
                    final MarkerOptions options = new MarkerOptions();

                    // Setting the position of the marker
                    options.position(latLng);


                    if (markerPoints.size() == 1) {
                        options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                        mMap.addMarker(options);
                    } else if (markerPoints.size() == 2) {
                        options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                        mMap.addMarker(options);
                    } else if (markerPoints.size() > 2) {
                        options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                        mMap.addMarker(options);
                    }
                }

                //kontrola dodavanja POI
                bAddPOI.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(bAddPOI.getText().equals(getString(R.string.bAddPoi))){
                            isPOIMarker = true;
                            bAddPOI.setText(getString(R.string.bAddPoiStop));
                        }
                        else if (bAddPOI.getText().equals(getString(R.string.bAddPoiStop))){
                            isPOIMarker = false;
                            bAddPOI.setText(getString(R.string.bAddPoi));
                        }


                    }
                });

                //dodavanje POI markera
                if(isPOIMarker == true){

                    POImarkers.add(latLng);
                    Log.d("BORIS poimarker", POImarkers.toString());
                    final MarkerOptions optionsPOI = new MarkerOptions();
                    optionsPOI.position(latLng);

                    optionsPOI.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));

                    String[] latLngString = latLng.toString().replace("lat/lng: ", "").replace("(", "").replace(")", "").split(",");
                    Log.d("BORIS latlngstring: ", latLngString.toString());
                    final double latitude = Double.parseDouble(latLngString[0]);
                    final double longitude = Double.parseDouble(latLngString[1]);
                    String latString = String.valueOf(latitude);
                    String lngString = String.valueOf(longitude);
                    Log.d("BORIS marker latitude", latString);
                    Log.d("BORIS marker longitude", lngString);

                    //adding proximity alert for marker
                    addProximityAlert(latitude, longitude);

                    //dialog stuff
                    final Dialog dialog = new Dialog(context);
                    dialog.setContentView(R.layout.dialog_location);
                    dialog.setTitle(getString(R.string.dialogTitle));

                    TextView dialogTextView = (TextView) dialog.findViewById(R.id.dialogTextView);
                    dialogTextView.setText(getString(R.string.dialogText));
                    final EditText dialogEditText = (EditText) dialog.findViewById(R.id.dialogEditText);
                    dialogEditText.setEditableFactory(Editable.Factory.getInstance());

                    Button dialogOKButton = (Button) dialog.findViewById(R.id.dialogOKButton);
                    dialogOKButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                            dialogTitleText = dialogEditText.getText().toString();
                            mMap.addMarker(optionsPOI).setTitle(dialogTitleText);
                        }
                    });
                    dialog.show();
                }


                //crtanje rute
                bDrawRoute.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
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

            }

//            private void addProximityAlert(double latitude, double longitude) {
//                Intent intent = new Intent(PROX_ALERT_INTENT);
//                PendingIntent proximityIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, 0);
//
//                if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//                    // TODO: Consider calling
//                    //    ActivityCompat#requestPermissions
//                    // here to request the missing permissions, and then overriding
//                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//                    //                                          int[] grantResults)
//                    // to handle the case where the user grants the permission. See the documentation
//                    // for ActivityCompat#requestPermissions for more details.
//                    return;
//                }
//                myLocationManager.addProximityAlert(
//                        latitude,
//                        longitude,
//                        100, //radius u metrima
//                        1000, //vrijeme za proxyalert u milisec
//                        proximityIntent
//                );
//                IntentFilter filter = new IntentFilter(PROX_ALERT_INTENT);
//                registerReceiver(new ProximityIntentReceiver(), filter);
//            }
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


    private void addProximityAlert(double latitude, double longitude) {

        Bundle extras = new Bundle();
        extras.putInt("id", alertID);

        Intent intent = new Intent(PROXI_INTENT);
        intent.putExtra(PROXI_INTENT, extras);

        PendingIntent proximityIntent = PendingIntent.getBroadcast(MapActivity.this, alertID, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        if (latitude != 0 && longitude != 0) {

            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;

            }
            myLocationManager.addProximityAlert(
                    latitude,
                    longitude,
                    RADIUS,
                    EXPIRATION,
                    proximityIntent
            );
            alertID++;
        }

    }

    private void initializeReceiver(){
        IntentFilter filter = new IntentFilter(PROXI_INTENT);
        registerReceiver(new ProximityIntentReceiver(), filter);
    }

    // Fetches data from url passed
    private class DownloadTask extends AsyncTask<String, Void, String> {

        // Downloading data in non-ui thread
        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try {
                // Fetching the data from web service
                data = downloadUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        // Executes in UI thread, after the execution of
        // doInBackground()
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data
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

        // Executes in UI thread, after the parsing process
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
                lineOptions.width(5);
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

        //Waypoints
        String waypoints = "";
        for(int i = 2; i < markerPoints.size(); i++){
            LatLng latLng = (LatLng) markerPoints.get(i);
            if(i == 2) {
                waypoints = "waypoints=";
            }
            waypoints += latLng.latitude + "," + latLng.longitude + "|";
        }

        // Building the parameters to the web service
        String parameters = str_origin + "&" + waypoints + "&" + str_dest + "&" + sensor + "&" + mode;

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

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
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
