package com.servewellsolution.app.leafood;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

import static com.servewellsolution.app.leafood.SessionManagement.PREF_NAME;

public class OrderdetailActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap gmap;
    private static Double lat;
    private static Double lng;
    private Marker gmarker;
    private SharedPreferences.Editor editor;
    private ProgressDialog dialog;
    private MapFragment mapFragment;
    private AsyncTask<Void, Void, Void> task;
    private double summary = 0;
    private HashMap map;
    private ArrayList<HashMap<String, String>> sList;
    private String phoneno;
    private HashMap<String, String> userdetail;
    private DatabaseReference dbRef;
    private String orderid = "";
    private String mode = "";
    private FirebaseDatabase database;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.orderdetail);
        this.database = FirebaseDatabase.getInstance();
        SessionManagement sess = new SessionManagement(getBaseContext());
        this.userdetail = sess.getUserDetails();
        Intent intent = getIntent();
        String orderno = "";
        String ordertime = "";
        String fullname = "";
        String tel = "";
        String email = "";
        String lat = "";
        String lng = "";
        String address = "";
        String s = "";
        String deliverytime = "";
        String deriveryrange = "";
        if (intent.hasExtra("obj")) {
            HashMap<String, String> tmp = (HashMap<String, String>) intent.getSerializableExtra("obj");
            Log.d("tmp =", tmp + "");
            orderno = tmp.get(ListItem.KEY_ORDERORDERNO);
            ordertime = tmp.get(ListItem.KEY_ORDERCREATEDATE);
            deliverytime = tmp.get(ListItem.KEY_ORDERDERIVERYDATE);
            deriveryrange = tmp.get(ListItem.KEY_ORDERDERIVERYRANGE);
            orderid = tmp.get(ListItem.KEY_ORDERID);

            fullname = tmp.get("fullname");
            tel = tmp.get("tel");
            email = tmp.get("email");


            lat = tmp.get("lat");
            lng = tmp.get("lng");
            this.lat = Double.parseDouble(lat);
            this.lng = Double.parseDouble(lng);
            address = tmp.get("address");

            s = tmp.get(ListItem.KEY_ORDERDETAILJSON);

            this.mode = (String) intent.getSerializableExtra("mode");
        }
        SharedPreferences pref = getApplicationContext().getSharedPreferences(PREF_NAME, 0); // 0 - for private mode
        editor = pref.edit();
        dialog = new ProgressDialog(this);

        LinearLayout listOrderLayout = (LinearLayout) findViewById(R.id.listOrderLayout);
        TextView txt_ordertime = (TextView) findViewById(R.id.txt_ordertime);
        TextView txt_deliverytime = (TextView) findViewById(R.id.txt_deliverytime);
        TextView txtsummary = (TextView) findViewById(R.id.txtsummary);
        ListView incommingorder_list = (ListView) findViewById(R.id.listOrder);

        TextView txtname = (TextView) findViewById(R.id.txtname);
        TextView txttel = (TextView) findViewById(R.id.txttel);
        TextView txtaddress = (TextView) findViewById(R.id.txtaddress);

        Button btn_accept = (Button) findViewById(R.id.btn_accept);
        Button btn_cancel = (Button) findViewById(R.id.btn_cancel);

        if(this.mode.equals("view")){
            btn_accept.setVisibility(View.GONE);
            btn_cancel.setVisibility(View.GONE);
        }

        // Write a message to the database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        dbRef = database.getReference("shops");




        this.setTitle("Order #" + orderno);

        txtname.setText("ชื่อผู้สั่ง : " + fullname);
        txttel.setText("เบอร์โทรติดต่อ : " + tel);
        txtaddress.setText("ที่อยู่สำหรับจัดส่ง : " + address);
        phoneno = tel;
        this.mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);


        setting();

        summary = 0;
        this.sList = new ArrayList<HashMap<String, String>>();
        try {
            final JSONObject jsonObj = new JSONObject(s);
            if (!jsonObj.get("result").toString().equals("")) {
                JSONArray obj = (JSONArray) jsonObj.get("result");
                for (int i = 0; i < obj.length(); i++) {
                    String amount = obj.getJSONObject(i).getString("amount");
                    String moredetail = obj.getJSONObject(i).getString("moredetail");
                    String title = obj.getJSONObject(i).getString("title");
                    String img = obj.getJSONObject(i).getString("img");
                    String price = obj.getJSONObject(i).getString("price");

                    map = new HashMap<String, String>();
                    map.put(ListItem.KEY_ORDERDETAILTITLE, title);
                    map.put(ListItem.KEY_ORDERDETAILIMAGE, img);
                    map.put(ListItem.KEY_ORDERDETAILAMOUNT, amount);
                    map.put(ListItem.KEY_ORDERDETAILMOREDETAIL, moredetail);
                    map.put(ListItem.KEY_ORDERDETAILPRICE, price);

                    summary += (Double.parseDouble(amount) * Double.parseDouble(price));
                    sList.add(map);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String xsummary = new DecimalFormat("#,###.00").format(summary).toString();
        txtsummary.setText("ราคารวม " + xsummary + " บาท");
        BaseAdapter incomminglist_adpt = new incommingorder_list_adpt(this, listOrderLayout, sList);
        incommingorder_list.setAdapter(incomminglist_adpt);


        txt_ordertime.setText("เวลาสั่ง " + DatetimeHelper.convertDate(ordertime));
        if(!deriveryrange.equals("")){
            txt_deliverytime.setText("เวลาส่ง " + DatetimeHelper.convertDate2(deliverytime)+ " เวลา " + deriveryrange);
        }
        else{
            txt_deliverytime.setText("เวลาส่ง " + DatetimeHelper.convertDate(deliverytime));
        }

    }

    private void setting() {
        this.mapFragment.getMapAsync(this);

    }





    @Override
    public void onMapReady(GoogleMap map) {
        gmap = map;
        gmap.setMyLocationEnabled(false);
        gmap.getUiSettings().setScrollGesturesEnabled(false);
        gmarker = gmap.addMarker(new MarkerOptions()
                .position(new LatLng(lat, lng))
                .title("ตำแหน่งลูกค้า"));

        CameraUpdate center =
                CameraUpdateFactory.newLatLng(new LatLng(lat, lng));
        gmap.moveCamera(center);
        gmap.animateCamera(CameraUpdateFactory.zoomTo(17), 1000, null);
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }


}
