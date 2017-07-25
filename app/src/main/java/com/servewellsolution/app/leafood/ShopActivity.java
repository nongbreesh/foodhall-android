package com.servewellsolution.app.leafood;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hrules.horizontalnumberpicker.HorizontalNumberPicker;
import com.hrules.horizontalnumberpicker.HorizontalNumberPickerListener;
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

import static com.servewellsolution.app.leafood.SessionManagement.KEY_USERID;

/**
 * Created by Breeshy on 8/23/2016 AD.
 */

public class ShopActivity extends Activity {
    private ProgressDialog dialog;
    private HashMap<String, String> tmp;
    private FloatingActionButton fab;
    String title, img, distance, isshopopen, tel, email;
    Integer id;
    private HashMap map;
    protected ArrayList<HashMap<String, String>> sList;
    private AsyncTask<Void, Void, Void> task;
    private ShopAdapter listAdpt;
    private ListView listview;
    private BottomSheetBehavior mBottomSheetBehavior;
    private SharedPreferences.Editor editor;
    private HashMap<String, String> userdetail;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shopactivity);
        View bottomSheet = findViewById(R.id.bottom_sheet);
        mBottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        Window window = this.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(this.getResources().getColor(R.color.colorPrimaryDark));
        Intent i = getIntent();
        if (i.hasExtra("objs"))
            dialog = new ProgressDialog(this);
        this.setting();

        {
            this.tmp = (HashMap<String, String>) i.getSerializableExtra("objs");
            this.id = Integer.parseInt(tmp.get(ListItem.KEY_ITEMID));
            this.title = tmp.get(ListItem.KEY_ITEMTITLE);
            this.img = tmp.get(ListItem.KEY_IMG);
            this.distance = tmp.get(ListItem.KEY_DISTANCE);
            this.isshopopen = tmp.get(ListItem.KEY_ISSHOPOPEN);
            this.tel = tmp.get(ListItem.KEY_TEL);
            this.email = tmp.get(ListItem.KEY_EMAIL);
        }

        TextView txttel = (TextView) findViewById(R.id.txttel);
        TextView txtemail = (TextView) findViewById(R.id.txtemail);
        txttel.setText(this.tel == "" ? "เบอร์โทรศัพท์ :  -" : "เบอร์โทรศัพท์ : " +  this.tel);
        txtemail.setText(this.email == "" ? "อีเมลล์ : -" : "อีเมลล์ : " + this.email);


        this.listview = (ListView) findViewById(R.id.listView);
        this.sList = new ArrayList<HashMap<String, String>>();
        this.loaditems();


        this.fab = (FloatingActionButton) this.findViewById(R.id.floatingActionButton);
        this.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("userdetail", userdetail + "");
                if (userdetail.get(KEY_USERID) == null) {
                    Intent intent = new Intent(view.getContext(), LoginActivity.class);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(view.getContext(), Ordersummary.class);
                    intent.putExtra("objs", tmp);
                    intent.putExtra("cart", sList);

                    startActivity(intent);
                    overridePendingTransition(R.anim.enter, R.anim.exit);
                }
            }
        });
    }

    private void setting() {
        SessionManagement sess = new SessionManagement(this);
        this.userdetail = sess.getUserDetails();
    }


    private void loaditems() {

        map = new HashMap<String, String>();
        map.put(ListItem.KEY_INDEX, "99");
        map.put(ListItem.KEY_HEADER, "");
        map.put(ListItem.KEY_ITEMID, id + "");
        map.put(ListItem.KEY_ITEMTITLE, title);
        map.put(ListItem.KEY_ITEMPRICE, "");
        map.put(ListItem.KEY_ITEMIMAGE, img);
        map.put(ListItem.KEY_ORDERDETAILAMOUNT, "0");

        sList.add(map);


        this.task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
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
                bindList();
            }

        };
        this.task.execute((Void[]) null);
    }

    private void bindList() {
        this.listAdpt = new ShopAdapter(this, this.sList, this.fab, this.isshopopen, mBottomSheetBehavior);
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.abc_fade_in);
        this.listview.startAnimation(animation);
        this.listview.setAdapter(this.listAdpt);

    }

    private void loadItems() {


        // Create a new HttpClient and Post Header
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(getString(R.string.apiaddress) + "api/getitemlist");

        try {
            // Add your data
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
            nameValuePairs.add(new BasicNameValuePair("shopid", this.id + ""));
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
                Log.d("Response", "ResponseObject: " + obj);

                for (int i = 0; i < obj.length(); i++) {
                    String id = obj.getJSONObject(i).getString("id");
                    String title = obj.getJSONObject(i).getString("title");
                    String price = obj.getJSONObject(i).getString("price");
                    String img = obj.getJSONObject(i).getString("img");

                    map = new HashMap<String, String>();
                    map.put(ListItem.KEY_INDEX, "99");
                    map.put(ListItem.KEY_HEADER, "");
                    map.put(ListItem.KEY_ITEMID, id);
                    map.put(ListItem.KEY_ITEMTITLE, title);
                    map.put(ListItem.KEY_ITEMPRICE, price);
                    map.put(ListItem.KEY_ITEMIMAGE, img);
                    map.put(ListItem.KEY_ORDERDETAILAMOUNT, "0");

                    sList.add(map);
                }

            }


        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

}

class ShopAdapter extends BaseAdapter {
    private HashMap<String, String> tmp;
    private ArrayList<HashMap<String, String>> data;
    private Activity activity;
    private static LayoutInflater inflater = null;
    private FloatingActionButton fab;
    String title, img, distance, isshopopen, ordertime;
    Integer id;
    private BottomSheetBehavior mBottomSheetBehavior;

    public ShopAdapter(Activity a, ArrayList<HashMap<String, String>> d, FloatingActionButton f, String isshopopen, BottomSheetBehavior bottomsheet) {
        this.data = d;
        this.activity = a;
        this.fab = f;
        this.mBottomSheetBehavior = bottomsheet;
        this.isshopopen = isshopopen;
        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public int getCount() {
        return this.data.size();
    }

    private int getAllAmount() {
        Integer allamount = 0;
        for (Integer i = 0; i < this.data.size(); i++) {
            final HashMap<String, String> tmp = this.data.get(i);
            String amount = tmp.get(ListItem.KEY_ORDERDETAILAMOUNT);
            allamount += Integer.parseInt(amount);
        }
        return allamount;
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        if (position == 0) {
            convertView = inflater.inflate(R.layout.shop, parent, false);
            Intent i = activity.getIntent();
            if (i.hasExtra("objs")) {
                this.tmp = (HashMap<String, String>) i.getSerializableExtra("objs");
                this.id = Integer.parseInt(tmp.get(ListItem.KEY_ITEMID));
                this.title = tmp.get(ListItem.KEY_ITEMTITLE);
                this.img = tmp.get(ListItem.KEY_IMG);
                this.distance = tmp.get(ListItem.KEY_DISTANCE);
                this.isshopopen = tmp.get(ListItem.KEY_ISSHOPOPEN);
                this.ordertime = tmp.get(ListItem.KEY_ORDERTIME);

                FrameLayout shopstatus = (FrameLayout) convertView.findViewById(R.id.shopstatus);
                if (this.isshopopen.equals("true") || this.isshopopen.equals("TRUE")) {
                    shopstatus.setVisibility(View.GONE);
                } else {
                    shopstatus.setVisibility(View.VISIBLE);
                }

            }
            ImageView circular_image_view = (ImageView) convertView.findViewById(R.id.circular_image_view);
            TextView txt_title = (TextView) convertView.findViewById(R.id.txt_title);
            TextView txt_distance = (TextView) convertView.findViewById(R.id.txt_distance);
            RelativeLayout idmore = (RelativeLayout) convertView.findViewById(R.id.idmore);
            Button btnmore = (Button) convertView.findViewById(R.id.btnmore);
            TextView txtordertime = (TextView) convertView.findViewById(R.id.txtordertime);
            TextView txtcash = (TextView) convertView.findViewById(R.id.txtcash);
            TextView txtdelivery = (TextView) convertView.findViewById(R.id.txtdelivery);
            ImageView idcash = (ImageView) convertView.findViewById(R.id.idcash);
            ImageView iddelivery = (ImageView) convertView.findViewById(R.id.iddelivery);
            idmore.setVisibility(View.VISIBLE);

            btnmore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                }
            });
            if (Integer.parseInt(ordertime) > 0) {
                txtordertime.setText("สั่งล่วงหน้า " + ordertime + " ชั่วโมง");
            } else {
                txtordertime.setText("ส่งได้เดี๋ยวนี้");
            }


            Picasso.with(activity).load(activity.getString(R.string.imageaddress) + this.img).into(circular_image_view);
            txt_title.setText(this.title);
            txt_title.setSelected(true);

            txt_distance.setText(this.distance + "KM");
        } else {
            final HashMap<String, String> tmp = this.data.get(position);
            String title = tmp.get(ListItem.KEY_ITEMTITLE);
            String price = tmp.get(ListItem.KEY_ITEMPRICE);
            String img = tmp.get(ListItem.KEY_ITEMIMAGE);
            String amount = tmp.get(ListItem.KEY_ORDERDETAILAMOUNT);
            convertView = inflater.inflate(R.layout.itemlist, parent, false);
            ImageView imgmenu = (ImageView) convertView.findViewById(R.id.imgmenu);
            TextView txttitle = (TextView) convertView.findViewById(R.id.title);
            TextView txtprice = (TextView) convertView.findViewById(R.id.price);

            Picasso.with(this.activity).load(this.activity.getString(R.string.imageaddress) + img).into(imgmenu);
            txttitle.setText(title);
            txtprice.setText(price + "฿");


            HorizontalNumberPicker horizontal_number_picker = (HorizontalNumberPicker) convertView.findViewById(R.id.horizontal_number_picker);
            horizontal_number_picker.getTextValueView().setTextSize(18);
            horizontal_number_picker.getTextValueView().setTextColor(activity.getResources().getColor(R.color.colorPrimary));
            horizontal_number_picker.setValue(Integer.parseInt(amount));

            if (this.isshopopen.equals("true") || this.isshopopen.equals("TRUE")) {
                horizontal_number_picker.setVisibility(View.VISIBLE);
            } else {
                horizontal_number_picker.setVisibility(View.GONE);
            }

            imgmenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(activity,ItemDetailActivity.class);
                    i.putExtra("obj",tmp);
                    activity.startActivity(i);
                }
            });


            horizontal_number_picker.setListener(new HorizontalNumberPickerListener() {
                @Override
                public void onHorizontalNumberPickerChanged(HorizontalNumberPicker horizontalNumberPicker, int value) {
                    tmp.put(ListItem.KEY_ORDERDETAILAMOUNT, value + "");
                    int allAmount = getAllAmount();
                    Log.d("onHorizontal", "" + getAllAmount());

                    Animation makeInAnimation = AnimationUtils.makeInAnimation(activity, false);
                    makeInAnimation.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationEnd(Animation animation) {
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {
                        }

                        @Override
                        public void onAnimationStart(Animation animation) {
                            fab.setVisibility(View.VISIBLE);
                        }
                    });

                    Animation makeOutAnimation = AnimationUtils.makeOutAnimation(activity, true);
                    makeOutAnimation.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationEnd(Animation animation) {
                            fab.setVisibility(View.INVISIBLE);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {
                        }

                        @Override
                        public void onAnimationStart(Animation animation) {
                        }
                    });


                    if (allAmount > 0) {
                        if (!fab.isShown()) {
                            fab.setVisibility(View.VISIBLE);
                            fab.startAnimation(makeInAnimation);
                        }
                    } else {
                        fab.setVisibility(View.GONE);
                        fab.startAnimation(makeOutAnimation);
                    }
                }
            });
        }

        return convertView;
    }


}
