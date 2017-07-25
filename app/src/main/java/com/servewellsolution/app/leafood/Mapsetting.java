package com.servewellsolution.app.leafood;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import static com.servewellsolution.app.leafood.SessionManagement.KEY_ADDRESS;
import static com.servewellsolution.app.leafood.SessionManagement.KEY_LAT;
import static com.servewellsolution.app.leafood.SessionManagement.KEY_LNG;
import static com.servewellsolution.app.leafood.SessionManagement.KEY_NEARBY;
import static com.servewellsolution.app.leafood.SessionManagement.PREF_NAME;

public class Mapsetting extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap gmap;
    private TextView txtaddress;
    private boolean isMapLoad = false;
    private FrameLayout btn_saveloc;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences pref = getApplicationContext().getSharedPreferences(PREF_NAME, 0); // 0 - for private mode
        editor = pref.edit();
        setContentView(R.layout.mapsetting);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitleTextColor(getResources().getColor(R.color.padcolor));
        setSupportActionBar(toolbar);

        this.setTitle("ระบุตำแหน่งจัดส่ง");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                savemap();
            }
        });


        this.txtaddress = (TextView) findViewById(R.id.txtaddress);
        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        this.btn_saveloc = (FrameLayout) findViewById(R.id.btn_saveloc);
        this.btn_saveloc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                savemap();
            }
        });

    }

    private void savemap() {
        Intent returnIntent = new Intent();
        returnIntent.putExtra("lat", "" + gmap.getCameraPosition().target.latitude);
        returnIntent.putExtra("lng", "" + gmap.getCameraPosition().target.longitude);

        editor.putString(KEY_LAT, String.valueOf(gmap.getCameraPosition().target.latitude));
        editor.putString(KEY_LNG, String.valueOf(gmap.getCameraPosition().target.longitude));
        editor.commit();
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }

    @Override
    public void onMapReady(GoogleMap map) {
        gmap = map;

        gmap.setOnCameraMoveStartedListener(new GoogleMap.OnCameraMoveStartedListener() {
            @Override
            public void onCameraMoveStarted(int i) {
                txtaddress.setText("Loading...");
            }
        });

        gmap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                String address = getAddress(cameraPosition.target.latitude, cameraPosition.target.longitude);
                editor.putString(KEY_ADDRESS,address);
                editor.commit();
                txtaddress.setText(address);
            }
        });


        gmap.setMyLocationEnabled(true);
        gmap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location arg0) {
                // TODO Auto-generated method stub

                if (!isMapLoad) {
                    CameraUpdate center =
                            CameraUpdateFactory.newLatLng(new LatLng(arg0.getLatitude(), arg0.getLongitude()));
                    gmap.moveCamera(center);
                    CameraUpdate zoom = CameraUpdateFactory.zoomTo(15);
                    gmap.animateCamera(zoom);
                    isMapLoad = true;
                }
            }
        });


    }

    private String getAddress(double latitude, double longitude) {
        StringBuilder result = new StringBuilder();
        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses.size() > 0) {
                Log.e("tagaddresses", "" + addresses);
                Address address = addresses.get(0);
                result.append(address.getAddressLine(0)).append("\n");
                result.append(address.getLocality()).append("\n");
                result.append(address.getAddressLine(2)).append("\n");
                result.append(address.getCountryName());
                editor.putString(KEY_NEARBY, address.getLocality());
                editor.commit();
            }
        } catch (IOException e) {
            Log.e("tag", e.getMessage());
        }

        return result.toString();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }


}
