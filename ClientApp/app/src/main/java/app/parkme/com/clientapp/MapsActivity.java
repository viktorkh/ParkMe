package app.parkme.com.clientapp;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.SearchManager;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
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

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends AppCompatActivity implements
        GoogleMap.OnMyLocationButtonClickListener,
        OnMapReadyCallback,GoogleMap.OnMapClickListener,
        GoogleMap.OnMapLongClickListener, GoogleMap.OnCameraChangeListener,
        ActivityCompat.OnRequestPermissionsResultCallback,
        DatePickerDialog.OnDateSetListener,TimePickerDialog.OnTimeSetListener {

    private GoogleMap mMap;

    private String searchStr = "";

    private Toolbar toolbar;

    private UiSettings mUiSettings;

    private final int LIMIT_ATTEMPTS_FIND_LOCATION = 1;

    private static LatLng centerCircle;

    private static Circle circle;

    private static LatLng searchLoc;

    private double circleLat = 31.89470689;
    private double circleLong = 35.01337203;

    private static Marker searchMarker;

    public static CameraPosition searchLocationCameraPosition = null;


    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private static final int MY_LOCATION_PERMISSION_REQUEST_CODE = 1;

    private static final int LOCATION_LAYER_PERMISSION_REQUEST_CODE = 2;

    static Calendar cTime;

    static Calendar cDate;


    private boolean mPermissionDenied = false;
    SupportMapFragment mapFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

            enableSearchLocation(searchStr);
        } else {
            enableMyLocation();
        }

        setCircle();
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

            Location currentLocation = getMyLocation();
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
                } else {


                    Toast.makeText(getApplicationContext(), R.string.search_location_out_of_circle, Toast.LENGTH_LONG).show();

                }


                LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

                // Creating a criteria object to retrieve provider
                Criteria criteria = new Criteria();

                // Getting the name of the best provider
                String provider = locationManager.getBestProvider(criteria, true);

                // Getting Current Location
                Location location = locationManager.getLastKnownLocation(provider);
                location.setLatitude(addr.getLatitude());
                location.setLongitude(addr.getLongitude());
                //drawMarker(location);
                latLng = new LatLng(addr.getLatitude(), addr.getLongitude());


                //return;

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

            searchLocationCameraPosition = getSearchLocationCameraPosition(latLng);

            mMap.addMarker(new MarkerOptions()
                    .position(latLng));

            // mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

            CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(searchLocationCameraPosition);

            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));


            // mMap.moveCamera(cameraUpdate);
            // Zoom in the Google Map
            //     mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
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

        if (searchLoc != null && cDate != null && cTime != null) {

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


        searchMarker = mMap.addMarker(new MarkerOptions()
                .position(latLng));

        float[] distance = new float[2];

        Location.distanceBetween(latLng.latitude, latLng.longitude, circle.getCenter().latitude, circle.getCenter().longitude, distance);

        if (distance[0] <= circle.getRadius()) {


            searchLoc = latLng;
        } else {


            Toast.makeText(getApplicationContext(), R.string.search_location_out_of_circle, Toast.LENGTH_LONG).show();

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
        if (searchMarker != null) {
            searchMarker.setTitle( cDate.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault())
                    +" "+hourOfDay
                    +":"+minute);



            searchMarker.showInfoWindow();
        }
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
}

