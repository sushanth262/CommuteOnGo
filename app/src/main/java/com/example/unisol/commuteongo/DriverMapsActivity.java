package com.example.unisol.commuteongo;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
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
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

        LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String provider = service.getBestProvider(criteria, false);
        service.requestLocationUpdates(provider, 20000, 0, this);

        mapAddresses();
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

    public void mapAddresses() {
        EditText addressButton = (EditText) findViewById(R.id.drvsrc);

        Intent intent = getIntent();
        String srcAddress = null;
        String destAddress = null;

        if (null != intent) {
            srcAddress = formatAddressForUri(intent.getStringExtra("srcDrvAddress").trim());
            destAddress = formatAddressForUri(intent.getStringExtra("destDrvAddress").trim());
            if(srcAddress == null || destAddress == null)
            {
                getCurrentLocation();
                return;
            }
        }

        AsycnGeoCode asyncTask = new AsycnGeoCode(new AsyncResponse() {

            @Override
            public void processFinish(List<Address> addresses ) {
                Route rt = new Route();
                ArrayList<LatLng> coordinates = new ArrayList<LatLng>();

                if(addresses.size() == 2) {
                    LatLng src = new LatLng(addresses.get(0).getLatitude(), addresses.get(0).getLongitude());
                    coordinates.add(src);
                    coordinates.add(new LatLng(addresses.get(1).getLatitude(), addresses.get(1).getLongitude()));
                    setUpMap(addresses.get(0).getLatitude(), addresses.get(0).getLongitude());
                    setUpMap(addresses.get(1).getLatitude(), addresses.get(1).getLongitude());
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(src,
                            mMap.getMaxZoomLevel() - 5));
                    rt.drawRoute(mMap, DriverMapsActivity.this, coordinates, "en", true);
                }
            }

        });

        asyncTask.execute(srcAddress, destAddress);
    }

    public interface AsyncResponse
    {
        public void processFinish(List<Address> output);
    }

    protected class AsycnGeoCode extends AsyncTask<String, Void, List<Address>> {
        public AsyncResponse delegate = null;//Call back interface
        ProgressDialog mProgressDialog;

        public AsycnGeoCode(AsyncResponse asyncResponse) {
            delegate = asyncResponse;//Assigning call back interfacethrough constructor
        }

        @Override
        protected void onPostExecute(List<Address> result) {
            mProgressDialog.dismiss();
            delegate.processFinish(result);
        }

        @Override
        protected void onPreExecute() {
            mProgressDialog = ProgressDialog.show(DriverMapsActivity.this,
                    "Loading...", "Data is Loading...");
        }

        public List<Address> getAddressList(Geocoder coder, String address)
        {
            Location loc;
            List<Address> addresses;
            try {
                if (address.toLowerCase().trim().equals("current+location") ||
                        address.toLowerCase().trim().equals("enter+destination")) {
                    loc = getCurrentLocation();
                    if(loc == null)
                    {
                        throw new IOException("Location came back null");
                    }
                    return coder.getFromLocation(loc.getLatitude(), loc.getLongitude(), 5);
                } else {
                    return coder.getFromLocationName(address, 5);
                }
            }
            catch(IOException e)
            {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected List<Address> doInBackground(String... params) {
            StringBuilder stringBuilder = new StringBuilder();
            try {

                if(params.length != 2)
                {
                    throw new Exception("Expected two addresses");
                }
                Geocoder coder = new Geocoder(DriverMapsActivity.this);
                List<Address> srcAddresses;
                List<Address> destAddresses;
                Location loc;

                srcAddresses = getAddressList(coder, params[0]);
                destAddresses = getAddressList(coder, params[1]);

                if(srcAddresses ==  null ||
                        srcAddresses.size() == 0 ||
                        destAddresses == null ||
                        destAddresses.size() == 0)
                {
                    return null;
                }
                return  Arrays.asList(srcAddresses.get(0), destAddresses.get(0));
            }
            catch(Exception e)
            {
                e.printStackTrace();
                return null;
            }

            /*try {
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
            }*/
            // your network operation
            //return stringBuilder;
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


    /*Gets Current location based on gps*/
    public Location getCurrentLocation()
    {
        LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String provider = service.getBestProvider(criteria, false);
        service.requestLocationUpdates(provider, 20000, 0, this);
        return service.getLastKnownLocation(provider);
    }
}

