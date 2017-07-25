package com.servewellsolution.app.leafood;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

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

import static com.servewellsolution.app.leafood.SessionManagement.KEY_NEARBY;

/**
 * Created by Breeshy on 9/11/2016 AD.
 */

public class ShopsFragment extends Fragment {
    View rootView;
    private HashMap map;
    protected ArrayList<HashMap<String, String>> sList;
    private AsyncTask<Void, Void, Void> task;
    private ShopsAdapter listAdpt;
    private ListView listview;
    private LinearLayout noorder;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    ArrayAdapter<String> arrayAdapter;
    AlertDialog.Builder builderSingle;
    private String cateName = "";
    private SharedPreferences.Editor editor;
    private HashMap<String, String> userdetail;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        this.rootView = inflater.inflate(R.layout.fragment_shops, container, false);
        this.setting();
        setHasOptionsMenu(true);

        this.firstload();
        this.listview = (ListView) rootView.findViewById(R.id.listView);
        this.noorder = (LinearLayout) rootView.findViewById(R.id.noorder);
        this.mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.activity_main_swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            public void onRefresh() {
                firstload();
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });

        this.listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getContext(), ShopActivity.class);
                intent.putExtra("objs", sList.get(position));
                startActivity(intent);
            }
        });


        return rootView;
    }

    private void setting() {
        SessionManagement sess = new SessionManagement(getContext());
        this.userdetail = sess.getUserDetails();
        getActivity().setTitle(this.userdetail.get(KEY_NEARBY));
    }

    private void bindList() {
        this.listAdpt = new ShopsAdapter(this.getActivity(), this.sList);
        Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.abc_fade_in);
        this.listview.startAnimation(animation);
        this.listview.setAdapter(this.listAdpt);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // TODO Add your menu entries here
        inflater.inflate(R.menu.category, menu);  // Use filter.xml from step 1
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_cate) {
            this.arrayAdapter = new ArrayAdapter<String>(
                    getActivity(),
                    android.R.layout.simple_expandable_list_item_1);
            showcate();
            return true;
        }

        return false;
    }

    private void showcate() {
        builderSingle = new AlertDialog.Builder(getContext());
        builderSingle.setIcon(R.drawable.ic_dish);
        builderSingle.setTitle("Select Category:-");
        loadcate();
    }

    private void loadcate() {
        this.task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected Void doInBackground(Void... arg0) {
                try {
                    getcate();
                } catch (Throwable e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {

                builderSingle.setNegativeButton(
                        "cancel",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });

                builderSingle.setAdapter(
                        arrayAdapter,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                cateName = arrayAdapter.getItem(which);
                                firstload();
                            }
                        });
                builderSingle.show();
            }

        };
        this.task.execute((Void[]) null);
    }

    private void getcate() {
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(getString(R.string.apiaddress) + "api/getcategory");

        try {
            // Add your data
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
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

            final JSONObject jsonObj = new JSONObject(s.toString());
            if (!jsonObj.get("result").toString().equals("")) {
                final JSONArray obj = (JSONArray) jsonObj.get("result");
                arrayAdapter.add("ทั้งหมด/All");
                for (int i = 0; i < obj.length(); i++) {
                    String id = obj.getJSONObject(i).getString("id");
                    String title = obj.getJSONObject(i).getString("title");
                    arrayAdapter.add(title);
                }


            }


        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
            Toast.makeText(this.getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            Toast.makeText(this.getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void firstload() {
        this.task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                sList = new ArrayList<HashMap<String, String>>();
            }

            @Override
            protected Void doInBackground(Void... arg0) {
                try {
                    loadItems();
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

    private void loadItems() {
        Log.d("cateName", cateName + "");
        // Create a new HttpClient and Post Header
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(getString(R.string.apiaddress) + "api/getshop");

        try {
            // Add your data
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
            nameValuePairs.add(new BasicNameValuePair("lat", MainActivity.lat + ""));
            nameValuePairs.add(new BasicNameValuePair("lng", MainActivity.lng + ""));
            nameValuePairs.add(new BasicNameValuePair("cateName", cateName));

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

            Log.d("Response", "Respons: " + s);
            final JSONObject jsonObj = new JSONObject(s.toString());

            Log.d("Response", "ResponseAll: " + jsonObj);
            if (!jsonObj.get("result").toString().equals("")) {
                final JSONArray obj = (JSONArray) jsonObj.get("result");
                Log.d("Response", "ResponseObject: " + obj);

                for (int i = 0; i < obj.length(); i++) {
                    String id = obj.getJSONObject(i).getString("id");
                    String title = obj.getJSONObject(i).getString("title");
                    String isshopopen = obj.getJSONObject(i).getString("isshopopen");
                    String lat = obj.getJSONObject(i).getString("lat");
                    String lng = obj.getJSONObject(i).getString("lng");
                    String radius = obj.getJSONObject(i).getString("radius");
                    String ordertime = obj.getJSONObject(i).getString("ordertime");
                    String distanceinkm = obj.getJSONObject(i).getString("distance_in_km");
                    String img = obj.getJSONObject(i).getString("img");
                    String minprice = obj.getJSONObject(i).getString("minprice");
                    String minamout = obj.getJSONObject(i).getString("minamout");
                    String deriveryfee = obj.getJSONObject(i).getString("deriveryfee");
                    String tel = obj.getJSONObject(i).getString("tel");
                    String email = obj.getJSONObject(i).getString("email");

                    map = new HashMap<String, String>();
                    map.put(ListItem.KEY_ITEMID, id);
                    map.put(ListItem.KEY_ITEMTITLE, title);
                    map.put(ListItem.KEY_ISSHOPOPEN, isshopopen);
                    map.put(ListItem.KEY_LAT, lat);
                    map.put(ListItem.KEY_LNG, lng);
                    map.put(ListItem.KEY_RADIUS, radius);
                    map.put(ListItem.KEY_ORDERTIME, ordertime);
                    map.put(ListItem.KEY_IMG, img);
                    map.put(ListItem.KEY_DISTANCE, distanceinkm);
                    map.put(ListItem.KEY_MINPRICE, minprice);
                    map.put(ListItem.KEY_MINAMOUNT, minamout);
                    map.put(ListItem.KEY_DELIVERYFEE, deriveryfee);
                    map.put(ListItem.KEY_TEL, tel);
                    map.put(ListItem.KEY_EMAIL, email);
                    sList.add(map);
                }

            }


        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
            Toast.makeText(this.getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            Toast.makeText(this.getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }
}


class ShopsAdapter extends BaseAdapter {
    private ArrayList<HashMap<String, String>> data;
    private HashMap map;
    private Activity activity;
    private static LayoutInflater inflater = null;

    public ShopsAdapter(Activity a, ArrayList<HashMap<String, String>> d) {
        this.data = d;
        this.activity = a;
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
        HashMap<String, String> tmp = this.data.get(position);

        int id = Integer.parseInt(tmp.get(ListItem.KEY_ITEMID));
        String title = tmp.get(ListItem.KEY_ITEMTITLE);
        String img = tmp.get(ListItem.KEY_IMG);
        String isshopopen = tmp.get(ListItem.KEY_ISSHOPOPEN);
        String lat = tmp.get(ListItem.KEY_LAT);
        String lng = tmp.get(ListItem.KEY_LNG);
        String radius = tmp.get(ListItem.KEY_RADIUS);
        String ordertime = tmp.get(ListItem.KEY_ORDERTIME);
        String distance = tmp.get(ListItem.KEY_DISTANCE);


        convertView = inflater.inflate(R.layout.shop, parent, false);

        ImageView circular_image_view = (ImageView) convertView.findViewById(R.id.circular_image_view);
        TextView txt_title = (TextView) convertView.findViewById(R.id.txt_title);
        TextView txt_distance = (TextView) convertView.findViewById(R.id.txt_distance);
        FrameLayout shopstatus = (FrameLayout) convertView.findViewById(R.id.shopstatus);

        TextView txtordertime = (TextView) convertView.findViewById(R.id.txtordertime);
        TextView txtcash = (TextView) convertView.findViewById(R.id.txtcash);
        TextView txtdelivery = (TextView) convertView.findViewById(R.id.txtdelivery);
        ImageView idcash = (ImageView) convertView.findViewById(R.id.idcash);
        ImageView iddelivery = (ImageView) convertView.findViewById(R.id.iddelivery);

        if (isshopopen.equals("false") || isshopopen.equals("FALSE")) {
            shopstatus.setVisibility(View.VISIBLE);
        } else {
            shopstatus.setVisibility(View.GONE);
        }

        if (Integer.parseInt(ordertime) > 0) {
            txtordertime.setText("สั่งล่วงหน้า " + ordertime + " ชั่วโมง");
        } else {
            txtordertime.setText("ส่งได้เดี๋ยวนี้");
        }


        Picasso.with(activity).load(activity.getString(R.string.imageaddress) + img).into(circular_image_view);
        txt_title.setText(title);
        txt_title.setSelected(true);
        txt_distance.setText(distance + "KM");
        return convertView;
    }


}
