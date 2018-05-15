package codingwithmitch.com.LocationAlarm;


import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;

import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import codingwithmitch.com.googlemapsgoogleplaces.R;
import codingwithmitch.com.LocationAlarm.models.PlaceInfo;

/**
 * Created by User on 10/2/2017.
 */

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {



    private static final String TAG = "MapActivity";
    private LocationCallback mLocationCallback;
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final float DEFAULT_ZOOM = 15f;
    private static final int PLACE_PICKER_REQUEST = 1;
    private static final LatLngBounds LAT_LNG_BOUNDS = new LatLngBounds(
            new LatLng(23.63936, 68.14712), new LatLng(28.20453, 97.34466));


    //widgets
    private AutoCompleteTextView mSearchText;
    private ImageView mGps, mInfo, mPlacePicker;
    private EditText label;


    //vars
    private Boolean mLocationPermissionsGranted = false;
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private PlaceAutocompleteAdapter mPlaceAutocompleteAdapter;
    private GoogleApiClient mGoogleApiClient;
    private PlaceInfo mPlace;
    private Marker mMarker;
    private Location mCurrentLocation=new Location("dummyprovider");



    private LocationRequest mLocationRequest = new LocationRequest();
    public LatLng lll=new LatLng(00,00);
    private Location mDestination= new Location("dummyprovider");

    public Task location=null;
    public Vibrator v;
    public double radius=1609.34;  //metres...
    public String unit="mi";
    public NumberPicker np=null,np2=null;

    public Circle circle= null;
    public Uri alert=null;
    Ringtone r=null;
    private static final int ERROR_DIALOG_REQUEST = 9001;


    //--------------------------------------Use following method anyhow----------------------//

/*
    public boolean isServicesOK(){
        Log.d(TAG, "isServicesOK: checking google services version");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MapActivity.this);

        if(available == ConnectionResult.SUCCESS){
            //everything is fine and the user can make map requests
            Log.d(TAG, "isServicesOK: Google Play Services is working");
            return true;
        }
        else if(GoogleApiAvailability.getInstance().isUserResolvableError(available)){
            //an error occured but we can resolve it
            Log.d(TAG, "isServicesOK: an error occured but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(MapActivity.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        }else{
            Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show();
        }
        return false;
    }
*/








        public void circleupdate()
        {
            circle.remove();
            circle = mMap.addCircle(new CircleOptions()
                    .center(new LatLng(mDestination.getLatitude(),mDestination.getLongitude()))
                    .radius(this.radius)
                    .strokeColor(Color.argb(200, 255, 20, 0))
                    .fillColor(Color.argb(50, 243, 50, 33))
                    .strokeWidth(5.0f));
            circle.setRadius(radius);
        }





    @Override
    protected void onResume() {
        super.onResume();
        startLocationUpdates();
    }

    public void startLocationUpdates() {


            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                return;
            }
            mFusedLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        Toast.makeText(this, "Map is Ready", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onMapReady: map is ready");
        mMap = googleMap;

        if (mLocationPermissionsGranted) {


            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);

            init();
        }
    }






    @Override
    protected void onPause() {
        super.onPause();
        v.cancel();
        r.stop();
    }

    public int temp=0;





    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);


        //lll=new LatLng(getIntent().getExtras().getDouble("lat"),getIntent().getExtras().getDouble("lng"));
        mSearchText = (AutoCompleteTextView) findViewById(R.id.input_search);

        np=(NumberPicker)findViewById(R.id.numberPicker4);
        np2=(NumberPicker)findViewById(R.id.numberPicker3);


        np.setMaxValue(50);
        np.setMinValue(1);

        String[] values2={"mi","km"};
        np2.setMinValue(1);
        np2.setMaxValue(values2.length);
        np2.setDisplayedValues(values2);
        np.setWrapSelectorWheel(true);
        np2.setWrapSelectorWheel(false);

        alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        r=RingtoneManager.getRingtone(getApplicationContext(),alert);



        mGps = (ImageView) findViewById(R.id.ic_gps);
        mInfo = (ImageView) findViewById(R.id.place_info);
        mPlacePicker = (ImageView) findViewById(R.id.place_picker);


        Log.d(TAG,"Step 1");
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        getLocationPermission();
        v=(Vibrator) getSystemService(Context.VIBRATOR_SERVICE);



        mLocationCallback=new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if(locationResult==null)
                {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    mCurrentLocation=location;
                    compare();
                }

            }
        };


        np.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {

                if(np2.getValue()==1)
                {
                    radius=np.getValue()*1609.34;
                }
                else {
                    radius=np.getValue()*1000.00;
                }

                v.cancel();
                r.stop();
                compare();
            }
        });



        np2.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {


                /*
                if(newVal==1)
                {
                    temp=(int)(np.getValue()/1.60934);
                   // Toast.makeText(MapActivity.this,temp+"",Toast.LENGTH_SHORT).show();


                    np.setValue(temp);









                }
                if(newVal==2)
                {
                    temp=(int)(np.getValue()*1.60934);
                    //Toast.makeText(MapActivity.this,temp+"",Toast.LENGTH_SHORT).show();
                    np.setValue(temp);
                }
*/
                if(newVal==1)
                {
                    unit="mi";
                    radius=np.getValue()*1609.34;
                    v.cancel();
                    r.stop();
                    compare();
                }
                else
                {
                    unit="km";
                    radius=np.getValue()*1000;
                    v.cancel();
                    r.stop();
                    compare();
                }


            }
        });
    }




    public void updateCircle(double latitude,double longitude, int proximityReached)
    {

        Log.d(TAG,""+latitude+" "+longitude+" "+proximityReached+"..................");

        Log.d(TAG,proximityReached+"");
        if(proximityReached==0) {

            if(circle!=null)
            {
                circle.remove();
            }

            circle = mMap.addCircle(new CircleOptions()
                    .center(new LatLng(latitude,longitude))
                    .radius(this.radius)
                    .strokeColor(Color.argb(200, 63, 81, 181))
                    .fillColor(Color.argb(50, 33, 150, 243))
                    .strokeWidth(5.0f));

            //Toast.makeText(this,circle.getStrokeWidth()+"",Toast.LENGTH_SHORT).show();
        }

        else {

            if(circle!=null)
            {
                circle.remove();
            }

            circle = mMap.addCircle(new CircleOptions()
                    .center(new LatLng(latitude,longitude))
                    .radius(this.radius)
                    .strokeColor(Color.argb(200, 255, 20, 0))
                    .fillColor(Color.argb(50, 243, 50, 33))
                    .strokeWidth(5.0f));
        }


    }


    public long[] pattern={0, 100, 1000, 300, 200, 100, 500, 200, 100};
    public double distance;
    public void compare()
    {
        Log.d(TAG,""+mDestination.getLatitude()+"   "+mDestination.getLongitude());
        Log.d(TAG,""+mCurrentLocation.getLatitude()+"   "+mCurrentLocation.getLongitude());

        distance=mCurrentLocation.distanceTo(mDestination);

        if(distance < radius)
        {

            v.vibrate(pattern,0);
            r.play();
            updateCircle(mDestination.getLatitude(),mDestination.getLongitude(),1);
            Log.d(TAG,"Vibrating");



        }
        else
        {
            v.cancel();
            r.stop();
            updateCircle(mDestination.getLatitude(),mDestination.getLongitude(),0);
        }


    }




    private void init(){
        Log.d(TAG, "init: initializing");

        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();

        mGoogleApiClient.connect();

        mSearchText.setOnItemClickListener(mAutocompleteClickListener);

        mPlaceAutocompleteAdapter = new PlaceAutocompleteAdapter(this, mGoogleApiClient,
                LAT_LNG_BOUNDS, null);

        mSearchText.setAdapter(mPlaceAutocompleteAdapter);

        mSearchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if(actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || keyEvent.getAction() == KeyEvent.ACTION_DOWN
                        || keyEvent.getAction() == KeyEvent.KEYCODE_ENTER){

                    //execute our method for searching
                    geoLocate();
                }

                return false;
            }
        });

        mGps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: clicked gps icon");
                getDeviceLocation();
            }
        });

        mInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: clicked place info");
                try{
                    if(mMarker.isInfoWindowShown()){
                        mMarker.hideInfoWindow();
                    }else{
                        Log.d(TAG, "onClick: place info: " + mPlace.toString());
                        mMarker.showInfoWindow();
                    }
                }catch (NullPointerException e){
                    Log.e(TAG, "onClick: NullPointerException: " + e.getMessage() );
                }
            }
        });

        mPlacePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

                try {
                    startActivityForResult(builder.build(MapActivity.this), PLACE_PICKER_REQUEST);
                } catch (GooglePlayServicesRepairableException e) {
                    Log.e(TAG, "onClick: GooglePlayServicesRepairableException: " + e.getMessage() );
                } catch (GooglePlayServicesNotAvailableException e) {
                    Log.e(TAG, "onClick: GooglePlayServicesNotAvailableException: " + e.getMessage() );
                }
            }
        });

      /*  MarkerOptions options=new MarkerOptions()
                .position(lll);
        mMarker=mMap.addMarker(options);

        mDestination.setLatitude(lll.latitude);
        mDestination.setLongitude(lll.longitude);

        moveCamera2(lll,DEFAULT_ZOOM);



        hideSoftKeyboard();*/
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(this, data);

                PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                        .getPlaceById(mGoogleApiClient, place.getId());
                placeResult.setResultCallback(mUpdatePlaceDetailsCallback);
            }
        }
    }

    private void geoLocate(){
        Log.d(TAG, "geoLocate: geolocating");

        String searchString = mSearchText.getText().toString();

        Geocoder geocoder = new Geocoder(MapActivity.this);
        List<Address> list = new ArrayList<>();
        try{
            list = geocoder.getFromLocationName(searchString, 1);
        }catch (IOException e){
            Log.e(TAG, "geoLocate: IOException: " + e.getMessage() );
        }

        if(list.size() > 0){
            Address address = list.get(0);

            Log.d(TAG, "geoLocate: found a location: " + address.toString());
            //Toast.makeText(this, address.toString(), Toast.LENGTH_SHORT).show();

            moveCamera(new LatLng(lll.latitude,lll.longitude), DEFAULT_ZOOM);
        }
    }


    private void getDeviceLocation(){
        Log.d(TAG, "getDeviceLocation: getting the devices current location");
        try{
            if(mLocationPermissionsGranted){

                location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if(task.isSuccessful()){
                            Log.d(TAG, "onComplete: found location!");
                            mCurrentLocation= (Location) task.getResult();

                            moveCamera(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()),
                                    DEFAULT_ZOOM);

                        }else{
                            Log.d(TAG, "onComplete: current location is null");
                            Toast.makeText(MapActivity.this, "unable to get current location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }catch (SecurityException e){
            Log.e(TAG, "getDeviceLocation: SecurityException: " + e.getMessage() );
        }
    }




    private void moveCamera(LatLng latLng, float zoom, PlaceInfo placeInfo){
        Log.d(TAG, "moveCamera: moving the camera to: lat: " + latLng.latitude + ", lng: " + latLng.longitude );
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));

        mMap.clear();

        mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter(MapActivity.this));

        if(placeInfo != null){
            try{
                String snippet = "Address: " + placeInfo.getAddress() + "\n" +
                        "Phone Number: " + placeInfo.getPhoneNumber() + "\n" +
                        "Website: " + placeInfo.getWebsiteUri() + "\n" +
                        "Price Rating: " + placeInfo.getRating() + "\n";

                MarkerOptions options = new MarkerOptions()
                        .position(latLng)
                        .title(placeInfo.getName())
                        .snippet(snippet);
                mMarker = mMap.addMarker(options);

                compare();

            }catch (NullPointerException e){
                Log.e(TAG, "moveCamera: NullPointerException: " + e.getMessage() );
            }
        }else{
            mMap.addMarker(new MarkerOptions().position(latLng));
            updateCircle(mMarker.getPosition().latitude,mMarker.getPosition().longitude,0);
        }

        hideSoftKeyboard();
    }

    private void moveCamera(LatLng latLng, float zoom){
        Log.d(TAG, "moveCamera: moving the camera to: lat: " + latLng.latitude + ", lng: " + latLng.longitude );
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
        hideSoftKeyboard();
    }


    private void moveCamera2(LatLng latLng, float zoom){
        Log.d(TAG, "moveCamera222: moving the camera to: lat: " + latLng.latitude + ", lng: " + latLng.longitude );
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));

        /*
            MarkerOptions options = new MarkerOptions()
                    .position(latLng);
            mMap.addMarker(options);
        */


        hideSoftKeyboard();
    }


    private void initMap(){
        Log.d(TAG, "initMap: initializing map");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        mapFragment.getMapAsync(MapActivity.this);

        getDeviceLocation();
    }

    private void getLocationPermission(){
        Log.d(TAG, "getLocationPermission: getting location permissions");
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION};

        if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                mLocationPermissionsGranted = true;
                initMap();
            }else{
                Log.d(TAG,"Inner else executed");
                ActivityCompat.requestPermissions(this,
                        permissions,
                        LOCATION_PERMISSION_REQUEST_CODE);
            }
        }else{

            Log.d(TAG,"Outer else executed");

            ActivityCompat.requestPermissions(this,
                    permissions,
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult: called.");
        mLocationPermissionsGranted = false;

        switch(requestCode){
            case LOCATION_PERMISSION_REQUEST_CODE:{
                if(grantResults.length > 0){
                    for(int i = 0; i < grantResults.length; i++){
                        if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
                            mLocationPermissionsGranted = false;
                            Log.d(TAG, "onRequestPermissionsResult: permission failed");
                            return;
                        }
                    }
                    Log.d(TAG, "onRequestPermissionsResult: permission granted");
                    mLocationPermissionsGranted = true;
                    //initialize our map
                    initMap();
                }
            }
        }
    }

    private void hideSoftKeyboard(){
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    /*
        --------------------------- google places API autocomplete suggestions -----------------
     */

    private AdapterView.OnItemClickListener mAutocompleteClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            hideSoftKeyboard();

            final AutocompletePrediction item = mPlaceAutocompleteAdapter.getItem(i);
            final String placeId = item.getPlaceId();

            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                    .getPlaceById(mGoogleApiClient, placeId);
            placeResult.setResultCallback(mUpdatePlaceDetailsCallback);
        }
    };

    private ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallback = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(@NonNull PlaceBuffer places) {
            if(!places.getStatus().isSuccess()){
                Log.d(TAG, "onResult: Place query did not complete successfully: " + places.getStatus().toString());
                places.release();
                return;
            }
            final Place place = places.get(0);

            try{

                lll=place.getLatLng();
                mDestination.setLatitude(lll.latitude);
                mDestination.setLongitude(lll.longitude);
                Log.d(TAG,lll.latitude+" "+lll.longitude);
                updateCircle(mDestination.getLatitude(),mDestination.getLongitude(),0);
                Log.d(TAG,"updated circle");
                v.cancel();
                r.stop();
                compare();
                mPlace = new PlaceInfo();
                mPlace.setName(place.getName().toString());
                Log.d(TAG, "onResult: name: " + place.getName());
                mPlace.setAddress(place.getAddress().toString());
                Log.d(TAG, "onResult: address: " + place.getAddress());
//                mPlace.setAttributions(place.getAttributions().toString());
//                Log.d(TAG, "onResult: attributions: " + place.getAttributions());
                mPlace.setId(place.getId());
                Log.d(TAG, "onResult: id:" + place.getId());
                mPlace.setLatlng(place.getLatLng());
                Log.d(TAG, "onResult: latlng: " + place.getLatLng());
                mPlace.setRating(place.getRating());
                Log.d(TAG, "onResult: rating: " + place.getRating());
                mPlace.setPhoneNumber(place.getPhoneNumber().toString());
                Log.d(TAG, "onResult: phone number: " + place.getPhoneNumber());
                mPlace.setWebsiteUri(place.getWebsiteUri());
                Log.d(TAG, "onResult: website uri: " + place.getWebsiteUri());

                Log.d(TAG, "onResult: place: " + mPlace.toString());
            }catch (NullPointerException e){
                Log.e(TAG, "onResult: NullPointerException: " + e.getMessage() );
            }

            moveCamera(new LatLng(lll.latitude,
                    lll.longitude), DEFAULT_ZOOM, mPlace);

            places.release();
        }
    };


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(10);
        mLocationRequest.setSmallestDisplacement(0.001f);
        mLocationRequest.setFastestInterval(5000);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }
}











