package app.parkme.com.clientapp;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.SearchManager;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends AppCompatActivity implements
        GoogleMap.OnMyLocationButtonClickListener,
        OnMapReadyCallback, GoogleMap.OnMapClickListener,
        GoogleMap.OnMapLongClickListener, GoogleMap.OnCameraChangeListener,
        ActivityCompat.OnRequestPermissionsResultCallback,
        DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

    private static GoogleMap mMap;

    private String searchStr = "";

    private Toolbar toolbar;

    private UiSettings mUiSettings;

    private final int LIMIT_ATTEMPTS_FIND_LOCATION = 1;

    private static LatLng centerCircle;

    private static Circle circle;

    private static LatLng searchLoc;

    private static Calendar searchTime;

    private double circleLat = 31.89470689;
    private double circleLong = 35.01337203;

    private static Marker searchMarker;
    private  static  String estimTime;


    private static  Polyline routePolyline;

    private  static  PolylineOptions polyLineOptions;

    public static CameraPosition searchLocationCameraPosition = null;


    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private static final int MY_LOCATION_PERMISSION_REQUEST_CODE = 1;

    private static final int LOCATION_LAYER_PERMISSION_REQUEST_CODE = 2;

    static Calendar cTime;

    private static Location  currentLocation;

    static Calendar cDate;

    public static Context contextOfApplication;

    public static final String PREFS_NAME = "ParkmePrefsFile";

    public static final String PREFS_ROUTE_NAME = "route";
    public static final String PREFS_CALENDAR = "calendar";
    public static final String PREFS_INVOICE = "invoice";
    public static final String PREFS_SEARCH_LOCATION_LAT = "search_location_lat";
    public static final String PREFS_SEARCH_LOCATION_LONG = "search_location_long";

    public static String jSonRoute;
    public static String invoiceNumber;

    private boolean mPermissionDenied = false;

    private boolean isFirst = false;

    SupportMapFragment mapFragment;

    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        contextOfApplication = getApplicationContext();

        setContentView(R.layout.activity_maps);

        toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);


        ActionBar ab = getSupportActionBar();

        // Enable the Up button
        ab.setDisplayHomeAsUpEnabled(true);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        handleIntent(getIntent());
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();

        isFirst = false;

    }

    @Override
    protected void onStop(){
        super.onStop();

        // We need an Editor object to make preference changes.
        // All objects are from android.context.Context
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();


        editor.putString(PREFS_ROUTE_NAME,jSonRoute);

        SimpleDateFormat format = new SimpleDateFormat("EEEE, MMMM d, yyyy 'at' h:mm a");
        String strTime= format.format(searchTime.getTime());
        editor.putString(PREFS_CALENDAR,strTime);


        editor.putString(PREFS_SEARCH_LOCATION_LAT,String.valueOf( searchLoc.latitude));
        editor.putString(PREFS_SEARCH_LOCATION_LONG,String.valueOf( searchLoc.longitude));
        editor.apply();


        client.disconnect();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {


        MenuInflater inflater = getMenuInflater();
        // Inflate menu to add items to action bar if it is present.
        inflater.inflate(R.menu.menu_main, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));

        return true;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            searchStr = intent.getStringExtra(SearchManager.QUERY);
            // Do work using string


            //  mapFragment.getMapAsync();
            enableSearchLocation(searchStr);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        try {
            switch (item.getItemId()) {
                case R.id.action_coverarea:

                    navigateToCircle();

                    return true;

                default:
                    return super.onOptionsItemSelected(item);
            }
        } catch (Exception e) {

            Log.e("navigateToCircle ", e.getMessage());

        }


        //noinspection SimplifiableIfStatement
        // return super.onOptionsItemSelected(item);
        return true;
    }

    private void navigateToCircle() {

        CameraPosition cameraCenterCircle;

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);

            cameraCenterCircle = getSearchLocationCameraPosition(centerCircle);

            //     mMap.addMarker(new MarkerOptions().position(centerCircle));

            // mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

            CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraCenterCircle);

            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(centerCircle, 15));

            //     mMap.moveCamera(cameraUpdate);
            // Zoom in the Google Map
            //     mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
        }


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;

        mMap.setOnMapClickListener(this);
        mMap.setOnMapLongClickListener(this);
        mMap.setOnCameraChangeListener(this);


        mUiSettings = mMap.getUiSettings();

        mMap.setOnMyLocationButtonClickListener(this);

        mUiSettings.setMyLocationButtonEnabled(true);


        if (!checkReady()) {
            return;
        }


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            // Uncheck the box and request missing location permission.

            PermissionUtils.requestPermission(this, LOCATION_LAYER_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, false);
        }
        // mMap.setMyLocationEnabled(true);

        if (searchStr.length() > 0) {

         //   enableSearchLocation(searchStr);
        } else {
            enableMyLocation();
        }

        setCircle();


        if(!isFirst){

            try {
                InitMap();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            isFirst=true;
        }
    }

    private void InitMap() throws JSONException {

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);

        jSonRoute = settings.getString(PREFS_ROUTE_NAME,"");


        SimpleDateFormat format = new SimpleDateFormat("EEEE, MMMM d, yyyy 'at' h:mm a");
        Calendar cal = Calendar.getInstance();
        String strTime= settings.getString(PREFS_CALENDAR,"");
        try {
            cal.setTime(format.parse(strTime));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        searchTime = format.getCalendar();

        try {

            Double strLat = Double.parseDouble( settings.getString(PREFS_SEARCH_LOCATION_LAT,""));
            Double strLong = Double.parseDouble(settings.getString(PREFS_SEARCH_LOCATION_LONG,""));

            searchLoc = new LatLng(strLat,strLong);
            ReDrawMap();

        }catch (Exception ex){

            ex.printStackTrace();
        }







    }

    private void ReDrawMap() throws JSONException {

        ArrayList<LatLng> points = null;

        JSONObject  jObject = new JSONObject(jSonRoute);
        List<List<HashMap<String, String>>> routes=   new PathJSONParser().parse(jObject);

        if (routes.size() >= 2) {
            // traversing through routes
            for (int i = 0; i < routes.size() - 1; i++) {
                points = new ArrayList<LatLng>();
                polyLineOptions = new PolylineOptions();
                List<HashMap<String, String>> path = routes.get(i);

                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                polyLineOptions.addAll(points);
                polyLineOptions.width(8);
                polyLineOptions.color(Color.BLUE);
            }

            int index = routes.size() - 1;

            List<HashMap<String, String>> durationList = routes.get(index);

            if (durationList.size() > 0) {

                String dur = durationList.get(0).get("duration");

                Calendar calendar = Calendar.getInstance(); // gets a calendar using the default time zone and locale.
                calendar.add(Calendar.SECOND, Integer.parseInt(dur));

                searchTime = calendar;

                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                String _time = sdf.format(calendar.getTime());


                if (searchMarker != null) {

                    searchMarker = mMap.addMarker(new MarkerOptions()
                            .position(searchLoc));

                    if (searchMarker != null) {
                        searchMarker.setTitle(calendar.getDisplayName(calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault())
                                + " " + _time);



                        searchMarker.showInfoWindow();
                    }
                }
            }

            searchLocationCameraPosition = getSearchLocationCameraPosition(searchLoc);

            CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(searchLocationCameraPosition);

            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(searchLoc, 15));

            if (routePolyline != null) {
                routePolyline.remove();
            }


            routePolyline = mMap.addPolyline(polyLineOptions);
        }

    }

    private void setCircle() {

        double radiusInMeters = 500.0;
        int strokeColor = 0xffff0000; //red outline
        int shadeColor = 0x44ff0000;

        centerCircle = new LatLng(32.06440, 34.77033);

        circle = mMap.addCircle(new CircleOptions()
                .center(centerCircle)
                .radius(radiusInMeters)

                .strokeColor(strokeColor)
                .fillColor(shadeColor)
                .strokeWidth(8));
    }

    private CameraPosition getSearchLocationCameraPosition(LatLng latLng) {


        return new CameraPosition.Builder().target(latLng)
                .zoom(15.5f)
                .bearing(0)
                .tilt(25)
                .build();

    }

    private Location getMyLocation() {
        // Get location from GPS if it's available

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            // Uncheck the box and request missing location permission.

            PermissionUtils.requestPermission(this, LOCATION_LAYER_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, false);
        }

        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Location myLocation = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        // Location wasn't found, check the next most accurate place for the current location
        if (myLocation == null) {
            Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_COARSE);
            // Finds a provider that matches the criteria
            String provider = lm.getBestProvider(criteria, true);
            // Use the provider to get the last known location
            myLocation = lm.getLastKnownLocation(provider);
        }

        return myLocation;
    }

    private void enableMyLocation() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);

            currentLocation = getMyLocation();
            if (currentLocation != null) {
                LatLng currentCoordinates = new LatLng(
                        currentLocation.getLatitude(),
                        currentLocation.getLongitude());
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentCoordinates, 15));
            }


        }
    }

    private void enableSearchLocation(String str) {



        //    str = "עמק איילון 26, מודיעין";

        List<Address> addresses;
        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        LatLng latLng = null;
        try {
            List<Address> geoResults = geocoder.getFromLocationName(str, 1);
            int countAttemps = 0;
            while (geoResults.size() == 0 && countAttemps < LIMIT_ATTEMPTS_FIND_LOCATION) {
                geoResults = geocoder.getFromLocationName(str, 1);
                countAttemps++;
            }
            if (geoResults.size() > 0) {
                Address addr = geoResults.get(0);

                float[] distance = new float[2];

                Location.distanceBetween(addr.getLatitude(), addr.getLongitude(), circle.getCenter().latitude, circle.getCenter().longitude, distance);

                if (distance[0] <= circle.getRadius()) {


                    searchLoc = new LatLng(addr.getLatitude(), addr.getLongitude());

                    if (mMap != null) {

                       CreateRoute(searchLoc);
                    }

                } else {


                    Toast.makeText(getApplicationContext(), R.string.search_location_out_of_circle, Toast.LENGTH_LONG).show();

                }


//                LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
//
//                // Creating a criteria object to retrieve provider
//                Criteria criteria = new Criteria();
//
//                // Getting the name of the best provider
//                String provider = locationManager.getBestProvider(criteria, true);
//
//                // Getting Current Location
//                Location location = locationManager.getLastKnownLocation(provider);
//                location.setLatitude(addr.getLatitude());
//                location.setLongitude(addr.getLongitude());
//                //drawMarker(location);
//                latLng = new LatLng(addr.getLatitude(), addr.getLongitude());


            } else {
                Toast.makeText(getApplicationContext(), R.string.search_location_not_found, Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            System.out.print(e.getMessage());
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);
        }
    }

    /**
     * When the map is not ready the CameraUpdateFactory cannot be used. This should be called on
     * all entry points that call methods on the Google Maps API.
     */
    private boolean checkReady() {
        if (mMap == null) {
            Toast.makeText(this, R.string.map_not_ready, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }


    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return true;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation();
        } else {
            // Display the missing permission error dialog when the fragments resume.
            mPermissionDenied = true;
        }
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (mPermissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError();
            mPermissionDenied = false;
        }
    }

    /**
     * Displays a dialog with error message explaining that the location permission is missing.
     */
    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getSupportFragmentManager(), "dialog");
    }

    public void onClickCurrentLocationBtn(View view) {

        enableMyLocation();
    }

    public void onClickBtnOrder(View view) {

        if (searchLoc != null && searchTime != null) {

            Intent intent = SetOrderActivity
                    .createIntent(this, "Luke Skywalker");
            startActivity(intent);


        }


    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {

    }

    @Override
    public void onMapClick(LatLng latLng) {

    }

    @Override
    public void onMapLongClick(LatLng latLng) {


        CreateRoute(latLng);


    }

    public boolean CreateRoute(LatLng latLng) {

        if(searchMarker != null){

            searchMarker.remove();
        }

        searchMarker = mMap.addMarker(new MarkerOptions()
                .position(latLng));

        float[] distance = new float[2];

        Location.distanceBetween(latLng.latitude, latLng.longitude, circle.getCenter().latitude, circle.getCenter().longitude, distance);

        if (distance[0] <= circle.getRadius()) {


            searchLoc = latLng;


            route(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()),latLng);
            return true;


        } else {


            Toast.makeText(getApplicationContext(), R.string.search_location_out_of_circle, Toast.LENGTH_LONG).show();
            return  false;

        }
    }

    public void onClickTimeBtn(View view) {


    }

    public void showTimePickerDialog(View v) {
        DatePickerFragment newFragment = new DatePickerFragment();
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }

    // handle the date selected
    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        // store the values selected into a Calendar instance
        cDate = Calendar.getInstance();
        cDate.set(Calendar.YEAR, year);
        cDate.set(Calendar.MONTH, monthOfYear);
        cDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);

        TimePickerFragment newFragment = new TimePickerFragment();
        newFragment.show(getSupportFragmentManager(), "timePicker");
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        cTime = Calendar.getInstance();
        cDate.set(Calendar.HOUR_OF_DAY, hourOfDay);
        cDate.set(Calendar.MINUTE, minute);

        searchTime = cDate;

        if (searchMarker != null) {
            searchMarker.setTitle(cDate.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault())
                    + " " + hourOfDay
                    + ":" + minute);


            searchMarker.showInfoWindow();
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Maps Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://app.parkme.com.clientapp/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }



    public static class DatePickerFragment extends DialogFragment {

        DatePickerDialog.OnDateSetListener mActivity;

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);

            // if the Activity does not implement this interface, it will crash
            mActivity = (DatePickerDialog.OnDateSetListener) activity;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current time as the default values for the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of TimePickerDialog and return it
            // mActivity is the callback interface instance
            return new DatePickerDialog(getActivity(), mActivity, year, month, day);
        }
    }

    public static class TimePickerFragment extends DialogFragment {

        TimePickerDialog.OnTimeSetListener mActivity;

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            mActivity = (TimePickerDialog.OnTimeSetListener) activity;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current time as the default values for the picker
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), mActivity, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            // Do something with the time chosen by the user
        }
    }

    protected void route(LatLng sourcePosition, LatLng destPosition) {



                try {

                    GMapV2DirectionAsyncTask md = (GMapV2DirectionAsyncTask) new GMapV2DirectionAsyncTask(sourcePosition,destPosition, new GMapV2DirectionAsyncTask.AsyncResponse(){

                        @Override
                        public void processFinish(List<List<HashMap<String, String>>> routes) {
                            ArrayList<LatLng> points = null;


                            if (routes.size() >= 2) {
                                // traversing through routes
                                for (int i = 0; i < routes.size() - 1; i++) {
                                    points = new ArrayList<LatLng>();
                                    polyLineOptions = new PolylineOptions();
                                    List<HashMap<String, String>> path = routes.get(i);

                                    for (int j = 0; j < path.size(); j++) {
                                        HashMap<String, String> point = path.get(j);

                                        double lat = Double.parseDouble(point.get("lat"));
                                        double lng = Double.parseDouble(point.get("lng"));
                                        LatLng position = new LatLng(lat, lng);

                                        points.add(position);
                                    }

                                    polyLineOptions.addAll(points);
                                    polyLineOptions.width(8);
                                    polyLineOptions.color(Color.BLUE);
                                }

                                int index = routes.size() - 1;

                                List<HashMap<String, String>> durationList = routes.get(index);

                                if (durationList.size() > 0) {

                                    String dur = durationList.get(0).get("duration");

                                    Calendar calendar = Calendar.getInstance(); // gets a calendar using the default time zone and locale.
                                    calendar.add(Calendar.SECOND, Integer.parseInt(dur));

                                    searchTime = calendar;

                                    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                                            String _time = sdf.format(calendar.getTime());


                                    if (searchMarker != null) {

                                        searchMarker = mMap.addMarker(new MarkerOptions()
                                                .position(searchLoc));

                                        if (searchMarker != null) {
                                            searchMarker.setTitle(calendar.getDisplayName(calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault())
                                                    + " " + _time);



                                            searchMarker.showInfoWindow();
                                        }
                                    }
                                }

                                searchLocationCameraPosition = getSearchLocationCameraPosition(searchLoc);

                                CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(searchLocationCameraPosition);

                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(searchLoc, 15));

                                if (routePolyline != null) {
                                    routePolyline.remove();
                                }


                                routePolyline = mMap.addPolyline(polyLineOptions);
                            }
                        }
                    }).execute();


                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

    public static Context getContextOfApplication(){
        return contextOfApplication;
    }
}

