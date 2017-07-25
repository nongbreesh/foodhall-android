package com.servewellsolution.app.leafood;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static com.servewellsolution.app.leafood.DatetimeHelper.convertDateUTC;
import static com.servewellsolution.app.leafood.ListItem.KEY_ITEMID;
import static com.servewellsolution.app.leafood.ListItem.KEY_LNG;
import static com.servewellsolution.app.leafood.ListItem.KEY_ORDERDETAILAMOUNT;
import static com.servewellsolution.app.leafood.SessionManagement.KEY_ADDRESS;
import static com.servewellsolution.app.leafood.SessionManagement.KEY_LAT;
import static com.servewellsolution.app.leafood.SessionManagement.KEY_USERID;

/**
 * Created by Breeshy on 9/16/2016 AD.
 */

public class Orderconfirm extends AppCompatActivity {
    private static Intent i;
    Date reqdate;
    String summary;
    private HashMap<String, String> tmp;
    String title, img, distance, isshopopen, minprice, deliveryfee, ordertime, orderrange, address;
    protected static ArrayList<HashMap<String, String>> sList;
    Integer id;
    ProgressDialog dialog;
    EditText txtaddress;
    private AsyncTask<Void, Void, Void> task;
    private HashMap<String, String> userdetail;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.orderconfirm);
        Window window = this.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(this.getResources().getColor(R.color.colorPrimary));
        SessionManagement sess = new SessionManagement(getApplicationContext());
        this.userdetail = sess.getUserDetails();
        //Log.d("this.userdetai", this.userdetail + "");
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitleTextColor(getResources().getColor(R.color.padcolor));
        setSupportActionBar(toolbar);
        dialog = new ProgressDialog(this);

        this.setTitle("ยืนยันการสั่งซื้อ");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left);
            }
        });

        this.i = getIntent();
        if (i.hasExtra("reqdate")) {
            reqdate = (Date) i.getSerializableExtra("reqdate");
            Log.d("extras", "" + reqdate);
        }
        if (i.hasExtra("cart")) {
            sList = (ArrayList<HashMap<String, String>>) i.getSerializableExtra("cart");
            Log.d("extras", "" + sList);
        }

        if (i.hasExtra("summary")) {
            summary = (String) i.getSerializableExtra("summary");
            Log.d("extras", "" + summary);
        }

        if (i.hasExtra("orderrange")) {
            orderrange = (String) i.getSerializableExtra("orderrange");
            Log.d("extras", "" + summary);
        }

        if (i.hasExtra("objs")) {
            this.tmp = (HashMap<String, String>) i.getSerializableExtra("objs");
            this.id = Integer.parseInt(tmp.get(KEY_ITEMID));
            this.title = tmp.get(ListItem.KEY_ITEMTITLE);
            this.img = tmp.get(ListItem.KEY_IMG);
            this.distance = tmp.get(ListItem.KEY_DISTANCE);
            this.isshopopen = tmp.get(ListItem.KEY_ISSHOPOPEN);
            this.minprice = tmp.get(ListItem.KEY_MINPRICE);
            this.minprice = tmp.get(ListItem.KEY_MINPRICE);
            this.deliveryfee = tmp.get(ListItem.KEY_DELIVERYFEE);
            this.ordertime = tmp.get(ListItem.KEY_ORDERTIME);
            this.address = tmp.get(ListItem.KEY_ORDERTIME);

        }
        Button btnnext = (Button) findViewById(R.id.btnnext);
        TextView txtshopname = (TextView) findViewById(R.id.txtshopname);
        TextView txtdeliverytime = (TextView) findViewById(R.id.txtdeliverytime);
        TextView txtsummary = (TextView) findViewById(R.id.txtsummary);
        TextView txtpaidtype = (TextView) findViewById(R.id.txtpaidtype);
        txtaddress = (EditText) findViewById(R.id.txtaddress);
        txtshopname.setText(this.title);
        txtshopname.setSelected(true);
        txtsummary.setText(summary);

        txtaddress.setText(this.userdetail.get(KEY_ADDRESS));
        if (orderrange != "" && orderrange != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            txtdeliverytime.setText(dateFormat.format(reqdate.getTime()) + " เวลา " + orderrange);
        } else {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy 'เวลา' HH:mm");
            txtdeliverytime.setText(dateFormat.format(reqdate.getTime()));
        }


        btnnext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                placeorder();
            }
        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left);
    }

    private void placeorder() {
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
                    submitorder();
                } catch (Throwable e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                dialog.hide();
                // Write a message to the database
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference dbRef = database.getReference("shops");
                dbRef.child(String.valueOf(id)).child("updatedate").setValue(ServerValue.TIMESTAMP);
                Intent intent = new Intent(getApplication(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra("orderconfirm", "orderconfirm");
                startActivity(intent);
            }

        };
        this.task.execute((Void[]) null);
    }

    private void submitorder() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        Log.d("UTCnow", convertDateUTC(dateFormat.format(reqdate.getTime())));
        // Create a new HttpClient and Post Header
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(getString(R.string.apiaddress) + "api/submitorder");


        try {
            // Add your data
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);

            for (int i = 0; i < sList.size(); i++) {
                String strid = sList.get(i).get(KEY_ITEMID);
                String orderamount = sList.get(i).get(KEY_ORDERDETAILAMOUNT);

                if (Integer.parseInt(orderamount) > 0) {
                    nameValuePairs.add(new BasicNameValuePair("orderlist[]", strid));
                    nameValuePairs.add(new BasicNameValuePair("amountlist[]", orderamount));
                }


            }


            if (orderrange != "" && orderrange != null) {
                nameValuePairs.add(new BasicNameValuePair("deriveryrange", orderrange));
            } else {
                nameValuePairs.add(new BasicNameValuePair("deriveryrange", ""));
            }
            nameValuePairs.add(new BasicNameValuePair("userid", this.userdetail.get(KEY_USERID)));
            nameValuePairs.add(new BasicNameValuePair("shopid", id + ""));
            nameValuePairs.add(new BasicNameValuePair("deliverytime", convertDateUTC(dateFormat.format(reqdate.getTime()))));

            nameValuePairs.add(new BasicNameValuePair("summary", summary));
            nameValuePairs.add(new BasicNameValuePair("paidtype", "CASH"));
            nameValuePairs.add(new BasicNameValuePair("lat", this.userdetail.get(KEY_LAT)));
            nameValuePairs.add(new BasicNameValuePair("lng", this.userdetail.get(KEY_LNG)));
            nameValuePairs.add(new BasicNameValuePair("address", this.txtaddress.getText().toString()));

            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));
            // Execute HTTP Post Request
            HttpResponse response = httpclient.execute(httppost);
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    response.getEntity().getContent(), "UTF-8"));
            String sResponse;
            StringBuilder s = new StringBuilder();

            while ((sResponse = reader.readLine()) != null) {
                s = s.append(sResponse);
            }

            Log.d("Response", "ResponseHtml: " + s);

            final JSONObject jsonObj = new JSONObject(s.toString());

            Log.d("Response", "ResponseAll: " + jsonObj);


        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
            Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
