package com.example.unisol.commuteongo;



import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;

import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class DriverMapsActivity extends FragmentActivity implements LocationListener {
    private int REQUEST_CODE_RECOVER_PLAY_SERVICES = 1001;
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private SupportMapFragment driverMapFragment;

    private TextView mLocationView;

    private GoogleApiClient mGoogleApiClient;

    private LocationRequest mLocationRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_maps);

        setUpMapIfNeeded();

        mMap.clear();

        mMap.setMyLocationEnabled(true);
        getLatLongFromAddress();
        /* LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String provider = service.getBestProvider(criteria, false);
        service.requestLocationUpdates(provider, 20000, 0, this);

        //Location userLocation = mMap.getMyLocation();
        LatLng myLocation = null;
        if (coord != null) {
            //myLocation = new LatLng(47.6356639, -122.3432309);
            myLocation = new LatLng(coord.latitude, coord.longitude);
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation,
                    mMap.getMaxZoomLevel() - 5));

            setUpMap(coord.latitude, coord.longitude);
            //setUpMap(47.6356639, -122.3432309);
        }*/
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    @Override
    public void onLocationChanged(Location location) {
        // TODO Auto-generated method stub
    }
    @Override
    public void onProviderDisabled(String provider) {
        // TODO Auto-generated method stub
    }

    public static String formatAddressForUri(String address)
    {
        try {
            if (address != null) {
                // return address.trim().replaceAll(" +", "%20");
                return URLEncoder.encode(address, "UTF-8");
            }
        }
        catch(UnsupportedEncodingException ex)
        {
            return address.trim().replaceAll(" +", "%20");
        }

        return null;
    }

    public void getLatLongFromAddress() {
        EditText addressButton = (EditText) findViewById(R.id.drvsrc);

        Intent intent = getIntent();
        String srcAddress = null;
        String destAddress = null;

        if (null != intent) {
            srcAddress = formatAddressForUri(intent.getStringExtra("srcDrvAddress"));
            destAddress = intent.getStringExtra("destDrvAddress");
        }

        if ((srcAddress != null) &&
                !srcAddress.toLowerCase().equals("current location")) {
            String uri = "http://maps.google.com/maps/api/geocode/json?address=" +
                    srcAddress + "&sensor=false";

            StringBuilder stringBuilder;


            AsycnGeoCode asyncTask = new AsycnGeoCode(new AsyncResponse() {

                @Override
                public void processFinish(StringBuilder output) {
                    LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
                    Criteria criteria = new Criteria();
                    String provider = service.getBestProvider(criteria, false);
                    Location userLocation = service.getLastKnownLocation(provider);
                    JSONObject jsonObject = new JSONObject();
                    LatLng coord;

                    try {
                        jsonObject = new JSONObject(output.toString());

                        double lng = ((JSONArray) jsonObject.get("results")).getJSONObject(0)
                                .getJSONObject("geometry").getJSONObject("location")
                                .getDouble("lng");

                        double lat = ((JSONArray) jsonObject.get("results")).getJSONObject(0)
                                .getJSONObject("geometry").getJSONObject("location")
                                .getDouble("lat");
                        coord = new LatLng(lat, lng);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        coord = new LatLng(userLocation.getLatitude(), userLocation.getLongitude());
                    }

                    //Location userLocation = mMap.getMyLocation();
                    LatLng myLocation = null;
                    if (coord != null) {
                        //myLocation = new LatLng(47.6356639, -122.3432309);
                        myLocation = new LatLng(coord.latitude, coord.longitude);
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation,
                                mMap.getMaxZoomLevel() - 5));

                        setUpMap(coord.latitude, coord.longitude);
                        //setUpMap(47.6356639, -122.3432309);
                    }

                }

            });

            asyncTask.execute(uri);
        }
    }

    public interface AsyncResponse
    {
        public void processFinish(StringBuilder output);
    }

    protected class AsycnGeoCode extends AsyncTask<String, Void, StringBuilder> {
        public AsyncResponse delegate = null;//Call back interface
        ProgressDialog mProgressDialog;

        public AsycnGeoCode(AsyncResponse asyncResponse) {
            delegate = asyncResponse;//Assigning call back interfacethrough constructor
        }

        @Override
        protected void onPostExecute(StringBuilder result) {
            mProgressDialog.dismiss();
            delegate.processFinish(result);
        }

        @Override
        protected void onPreExecute() {
            mProgressDialog = ProgressDialog.show(DriverMapsActivity.this,
                    "Loading...", "Data is Loading...");
        }

        @Override
        protected StringBuilder doInBackground(String... params) {
            StringBuilder stringBuilder = new StringBuilder();

            try {
                HttpGet httpGet = new HttpGet(params[0]);
                HttpClient client = new DefaultHttpClient();
                HttpResponse response;

                response = client.execute(httpGet);
                HttpEntity entity = response.getEntity();
                InputStream stream = entity.getContent();
                int b;
                while ((b = stream.read()) != -1) {
                    stringBuilder.append((char) b);
                }
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            // your network operation
            return stringBuilder;
        }
    }

    @Override
    public void onProviderEnabled(String provider) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap(double lat, double long)} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            SupportMapFragment m = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.driverMap));
            mMap = m.getMap();

            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap(0,0);
            }
            else {
                int checkGooglePlayServices = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
                if (checkGooglePlayServices != ConnectionResult.SUCCESS) {
                    // google play services is missing!!!!
              /*
               * Returns status code indicating whether there was an error.
               * Can be one of following in ConnectionResult: SUCCESS,
               * SERVICE_MISSING, SERVICE_VERSION_UPDATE_REQUIRED,
               * SERVICE_DISABLED, SERVICE_INVALID.
               */
                    Dialog d = GooglePlayServicesUtil.getErrorDialog(checkGooglePlayServices,
                            this, REQUEST_CODE_RECOVER_PLAY_SERVICES);
                    d.show();
                }
            }
        }
    }


    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap(double lat, double lng) {
        mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lng)).title("Marker"));
    }
}

