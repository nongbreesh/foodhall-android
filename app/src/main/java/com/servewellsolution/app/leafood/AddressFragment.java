package com.servewellsolution.app.leafood;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import static com.servewellsolution.app.leafood.ListItem.KEY_LAT;
import static com.servewellsolution.app.leafood.ListItem.KEY_LNG;
import static com.servewellsolution.app.leafood.SessionManagement.KEY_ADDRESS;
import static com.servewellsolution.app.leafood.SessionManagement.KEY_NEARBY;
import static com.servewellsolution.app.leafood.SessionManagement.PREF_NAME;

/**
 * Created by Breeshy on 9/11/2016 AD.
 */

public class AddressFragment extends Fragment {
    TextView txtcity;
    TextView txtsubdistrict;
    private ProgressDialog dialog;
    private SharedPreferences.Editor editor;
    LocationManager locationManager;
    LocationListener locationListener;
    private AsyncTask<Void, Void, Void> task;
    Geocoder geocoder;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_address, container, false);
        dialog = new ProgressDialog(getContext());
        SharedPreferences pref = getActivity().getSharedPreferences(PREF_NAME, 0); // 0 - for private mode
        editor = pref.edit();
        getActivity().setTitle("Address");
        dialog.setMessage("กรุณารอสักครู่... เรากำลังค้นหาตำแหน่งของท่าน");
        dialog.setCancelable(false);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.show();
        this.geocoder = new Geocoder(getActivity(), Locale.getDefault());
        this.txtcity = (TextView) rootView.findViewById(R.id.txtcity);
        this.txtsubdistrict = (TextView) rootView.findViewById(R.id.txtsubdistrict);
        Button btnshowshop = (Button) rootView.findViewById(R.id.btnshowshop);
        btnshowshop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (txtsubdistrict.getText().toString() != "") {
                    MainActivity.showshops();
                }

            }
        });

        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                // Called when a new location is found by the network location provider.
                Log.d("location", location.getLatitude() + "");
                MainActivity.lat = location.getLatitude();
                MainActivity.lng = location.getLongitude();
                String address = getAddress(MainActivity.lat, MainActivity.lng);
                editor.putString(KEY_LAT, String.valueOf(location.getLatitude()));
                editor.putString(KEY_LNG, String.valueOf(location.getLongitude()));
                editor.putString(KEY_ADDRESS, address);
                editor.commit();
                locationManager.removeUpdates(this);
                dialog.hide();
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };


        getCurrentLocation();

        ImageButton btnsetlocation = (ImageButton) rootView.findViewById(R.id.btnsetlocation);
        btnsetlocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getContext(), Mapsetting.class);
                startActivityForResult(intent, 1001);
            }
        });

        return rootView;
    }

    public void getCurrentLocation() {
        if (locationManager != null) {
            locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        }

    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001) {
            if (resultCode == Activity.RESULT_OK) {
                dialog.setMessage("กรุณารอสักครู่...");
                dialog.setCancelable(false);
                dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                dialog.show();
                MainActivity.lat = Double.parseDouble(data.getStringExtra("lat"));
                MainActivity.lng = Double.parseDouble(data.getStringExtra("lng"));
                this.getaddr(MainActivity.lat, MainActivity.lng);


            }
        }

    }

    private void getaddr(final double latitude, final double longitude) {
        this.task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                dialog.setMessage("กรุณารอสักครู่... ระบบกำลังส่งคำสั่งซื้อให้ร้านค้า");
                dialog.setCancelable(false);
                dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                dialog.show();
            }

            @Override
            protected Void doInBackground(Void... arg0) {
                try {
                     getAddress(latitude,longitude);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                dialog.hide();
                MainActivity.showshops();
            }

        };
        this.task.execute((Void[]) null);
    }

    @NonNull
    private String getAddress(double latitude, double longitude) {
        StringBuilder result = new StringBuilder();
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses.size() > 0) {
                Log.e("tagaddresses", "" + addresses);

                Address address = addresses.get(0);
                result.append(address.getAddressLine(0)).append("\n");
                result.append(address.getLocality()).append("\n");
                result.append(address.getAddressLine(2)).append("\n");
                result.append(address.getCountryName());
                this.txtcity.setText(address.getAddressLine(2));
                this.txtsubdistrict.setText(address.getLocality());
                editor.putString(KEY_NEARBY, address.getLocality());
                editor.commit();
            }
        } catch (IOException e) {
            Log.e("tag", e.getMessage());
        }

        return result.toString();
    }


}
