package com.servewellsolution.app.leafood;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.slider.library.SliderLayout;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.servewellsolution.app.leafood.SessionManagement.KEY_USERID;

/**
 * Created by Breeshy on 9/11/2016 AD.
 */

public class OrdersFragment extends Fragment {
    View rootView;
    private HashMap map;
    protected ArrayList<HashMap<String, String>> sList;
    private HashMap<String, String> userdetail;
    private AsyncTask<Void, Void, Void> task;
    private LinearLayout noorder;
    private OrderlistAdapter listAdpt;
    private String orderdetailjson;
    private ListView listview;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        this.rootView = inflater.inflate(R.layout.fragment_orders, container, false);


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                rootView.findViewById(R.id.loadingPanel).setVisibility(View.GONE);
            }
        }, 3 * 1000);


        getActivity().setTitle("รายการอาหารที่สั่ง");
        Button btnshowshop = (Button) this.rootView.findViewById(R.id.btnshowshop);
        btnshowshop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.showshops();
            }
        });

        SessionManagement sess = new SessionManagement(getContext());
        this.userdetail = sess.getUserDetails();
        this.noorder = (LinearLayout) rootView.findViewById(R.id.noorder);
        this.listview = (ListView) this.rootView.findViewById(R.id.listView);
        this.mSwipeRefreshLayout = (SwipeRefreshLayout) this.rootView.findViewById(R.id.activity_main_swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            public void onRefresh() {
                firstload();
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });
        this.listview.setDivider(null);
        this.listview.setDividerHeight(0);


        this.firstload();

        this.listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getContext(), OrderdetailActivity.class);
                intent.putExtra("obj", sList.get(position));
                intent.putExtra("mode", "view");
                getContext().startActivity(intent);
            }
        });

        return rootView;
    }

    private void bindList() {
        this.listAdpt = new OrderlistAdapter(getActivity(), this.sList, this.userdetail);
        Animation animation = AnimationUtils.loadAnimation(getActivity(), R.anim.abc_fade_in);
        this.listview.startAnimation(animation);
        this.listview.setAdapter(this.listAdpt);
    }


    private void firstload() {
        this.sList = new ArrayList<HashMap<String, String>>();
        this.task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected Void doInBackground(Void... arg0) {
                try {
                    loadItemList();
                } catch (Throwable e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                if (sList.size() > 0) {
                    listview.setVisibility(View.VISIBLE);
                    noorder.setVisibility(View.GONE);
                    bindList();
                } else {
                    noorder.setVisibility(View.VISIBLE);
                    listview.setVisibility(View.GONE);
                }
            }

        };
        this.task.execute((Void[]) null);
    }


    private void loadItemList() {
        // Create a new HttpClient and Post Header
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(getString(R.string.apiaddress) + "api/getorderbybuyerid");

        try {
            // Add your data
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
            nameValuePairs.add(new BasicNameValuePair("buyerid", userdetail.get(KEY_USERID).toString()));
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            // Execute HTTP Post Request
            HttpResponse response = httpclient.execute(httppost);
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    response.getEntity().getContent(), "UTF-8"));
            String sResponse;
            StringBuilder s = new StringBuilder();

            while ((sResponse = reader.readLine()) != null) {
                s = s.append(sResponse);
            }

            Log.d("Response", "Respons: " + s);
            final JSONObject jsonObj = new JSONObject(s.toString());

            Log.d("Response", "ResponseAll: " + jsonObj);
            if (!jsonObj.get("result").toString().equals("")) {
                final JSONArray obj = (JSONArray) jsonObj.get("result");
                Log.d("Response", "objorder: " + obj);


                for (int i = 0; i < obj.length(); i++) {
                    String id = obj.getJSONObject(i).getString("orderid");
                    String shopid = obj.getJSONObject(i).getString("shopid");
                    String orderno = obj.getJSONObject(i).getString("orderno");
                    String status = obj.getJSONObject(i).getString("status");
                    String createdate = obj.getJSONObject(i).getString("ordercreatedate");
                    String deriverydate = obj.getJSONObject(i).getString("deriverydate");
                    String deriveryrange = obj.getJSONObject(i).getString("deriveryrange");

                    String fullname = obj.getJSONObject(i).getString("fullname");
                    String tel = obj.getJSONObject(i).getString("tel");
                    String email = obj.getJSONObject(i).getString("email");
                    String lat = obj.getJSONObject(i).getString("lat");
                    String lng = obj.getJSONObject(i).getString("lng");
                    String address = obj.getJSONObject(i).getString("address");

                    map = new HashMap<String, String>();
                    map.put(ListItem.KEY_ORDERID, id);
                    map.put(ListItem.KEY_ORDERSHOPID, shopid);
                    map.put(ListItem.KEY_ORDERORDERNO, orderno);
                    map.put(ListItem.KEY_ORDERSTATUS, status);
                    map.put(ListItem.KEY_ORDERCREATEDATE, createdate);
                    map.put(ListItem.KEY_ORDERDERIVERYDATE, deriverydate);
                    map.put(ListItem.KEY_ORDERDERIVERYRANGE, deriveryrange);


                    map.put("fullname", fullname);
                    map.put("tel", tel);
                    map.put("email", email);
                    map.put("lat", lat);
                    map.put("lng", lng);
                    map.put("address", address);


                    loadOrderdetail(id);


                    map.put(ListItem.KEY_ORDERDETAILJSON, orderdetailjson);
                    sList.add(map);
                }
                Log.d("sList", sList + "");

            }


        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    private void loadOrderdetail(String orderid) {
        orderdetailjson = "";
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(this.getString(R.string.apiaddress) + "api/getorderdetail");

        try {
            // Add your data
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
            nameValuePairs.add(new BasicNameValuePair("orderid", orderid));
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            // Execute HTTP Post Request
            HttpResponse response = httpclient.execute(httppost);
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    response.getEntity().getContent(), "UTF-8"));
            String sResponse;
            StringBuilder s = new StringBuilder();

            while ((sResponse = reader.readLine()) != null) {
                s = s.append(sResponse);
            }
            orderdetailjson = s.toString();
            Log.d("Response", "Respons: " + s);

        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }


    }

}

class OrderlistAdapter extends BaseAdapter {
    private ArrayList<HashMap<String, String>> data;
    private HashMap map;
    private Activity activity;
    private static LayoutInflater inflater = null;
    private SliderLayout mSlider;
    private String orderid;
    private HashMap<String, String> userdetail;
    private AsyncTask<Void, Void, Void> task;

    public OrderlistAdapter(Activity a, ArrayList<HashMap<String, String>> d, HashMap<String, String> udt) {
        this.data = d;
        this.activity = a;
        this.userdetail = udt;
        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public int getCount() {
        return this.data.size();
    }


    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        final HashMap<String, String> tmp = this.data.get(position);

        String orderno = tmp.get(ListItem.KEY_ORDERORDERNO);
        String ordertime = tmp.get(ListItem.KEY_ORDERCREATEDATE);
        String deriverydate = tmp.get(ListItem.KEY_ORDERDERIVERYDATE);
        String status = tmp.get(ListItem.KEY_ORDERSTATUS);


        orderid = tmp.get(ListItem.KEY_ORDERID);
        String s = tmp.get(ListItem.KEY_ORDERDETAILJSON);


        convertView = inflater.inflate(R.layout.orderlist, parent, false);
        TextView txt_orderno = (TextView) convertView.findViewById(R.id.txt_orderno);
        TextView txt_ordertime = (TextView) convertView.findViewById(R.id.txt_ordertime);
        TextView txt_orderstatus = (TextView) convertView.findViewById(R.id.txt_orderstatus);


//        Button btn_more = (Button) convertView.findViewById(R.id.btn_more);
//        btn_more.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(activity, OrderdetailActivity.class);
//                intent.putExtra("obj", tmp);
//                intent.putExtra("mode", "view");
//                activity.startActivity(intent);
//            }
//        });

        switch (Integer.parseInt(status)) {
            case -1:
                txt_orderstatus.setText("รายการถูกยกเลิก");
                txt_orderstatus.setTextColor(Color.parseColor("#FF4F4F"));
                break;
            case 1:
                txt_orderstatus.setText("รอร้านค้าตอบรับ");
                txt_orderstatus.setTextColor(Color.parseColor("#F3C42C"));
                break;
            case 2:
                txt_orderstatus.setText("รับออเดอร์เรียบร้อย");
                txt_orderstatus.setTextColor(Color.parseColor("#38C872"));
                break;
            case 3:
                txt_orderstatus.setText("จัดส่งเรียบร้อย");
                txt_orderstatus.setTextColor(Color.parseColor("#358EF7"));
                break;
        }

        txt_orderno.setText("Order no. " + orderno);
        txt_ordertime.setText(DatetimeHelper.convertDate(ordertime));

        return convertView;
    }


}
