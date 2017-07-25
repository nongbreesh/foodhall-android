package com.servewellsolution.app.leafood;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hrules.horizontalnumberpicker.HorizontalNumberPicker;
import com.hrules.horizontalnumberpicker.HorizontalNumberPickerListener;
import com.squareup.picasso.Picasso;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.RadialPickerLayout;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import static com.servewellsolution.app.leafood.ListItem.KEY_ITEMID;
import static com.servewellsolution.app.leafood.ListItem.KEY_ITEMIMAGE;
import static com.servewellsolution.app.leafood.ListItem.KEY_ITEMPRICE;
import static com.servewellsolution.app.leafood.ListItem.KEY_ITEMTITLE;
import static com.servewellsolution.app.leafood.ListItem.KEY_ORDERDETAILAMOUNT;

public class Ordersummary extends AppCompatActivity implements TimePickerDialog.OnTimeSetListener, DatePickerDialog.OnDateSetListener {
    private OrderAdapter listAdpt;
    private ListView listview;
    private ProgressDialog dialog;
    private HashMap<String, String> tmp;
    String title, img, distance, isshopopen, minprice, deliveryfee, ordertime;
    Integer id;
    private AsyncTask<Void, Void, Void> task;
    private static Intent i;
    static ArrayList<HashMap<String, String>> sList;
    private static HashMap map;
    TextView txtsummary, txtminorder;
    RelativeLayout idminorder;
    private HashMap<String, Double> summary;
    Calendar deliverytime;
    String formattedDateString;
    AlertDialog.Builder builderSingle;

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ordersummary);
        Window window = this.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(this.getResources().getColor(R.color.colorPrimary));
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitleTextColor(getResources().getColor(R.color.padcolor));
        setSupportActionBar(toolbar);

        this.setTitle("รายการอาหาร");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        this.summary = new HashMap<>();
        this.listview = (ListView) findViewById(R.id.listView);
        this.txtsummary = (TextView) findViewById(R.id.txtsummary);
        this.txtminorder = (TextView) findViewById(R.id.txtminorder);
        this.idminorder = (RelativeLayout) findViewById(R.id.idminorder);
        Button btnnext = (Button) findViewById(R.id.btnnext);


        this.sList = new ArrayList<HashMap<String, String>>();


        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left);
            }
        });

        this.i = getIntent();
        if (i.hasExtra("objs")) {
            this.tmp = (HashMap<String, String>) i.getSerializableExtra("objs");
            this.id = Integer.parseInt(tmp.get(KEY_ITEMID));
            this.title = tmp.get(ListItem.KEY_ITEMTITLE);
            this.img = tmp.get(ListItem.KEY_IMG);
            this.distance = tmp.get(ListItem.KEY_DISTANCE);
            this.isshopopen = tmp.get(ListItem.KEY_ISSHOPOPEN);
            this.minprice = tmp.get(ListItem.KEY_MINPRICE);
            this.deliveryfee = tmp.get(ListItem.KEY_DELIVERYFEE);
            this.ordertime = tmp.get(ListItem.KEY_ORDERTIME);

            Log.d("this.tmp", this.tmp + "");
            if (Integer.parseInt(this.minprice) > 0) {
                DecimalFormat formatter = new DecimalFormat("#,###,###.##");
                String minpriceformat = formatter.format(Integer.parseInt(this.minprice));
                this.txtminorder.setText("คุณต้องมีค่าอาหารมากกว่า " + minpriceformat + " บาทขึ้นไป");
            }
        }

        //หาเวลที่ต้องส่ง

        deliverytime = Calendar.getInstance();
        deliverytime.add(Calendar.HOUR, Integer.parseInt(ordertime));
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        formattedDateString = dateFormat.format(deliverytime.getTime());


        btnnext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (idminorder.getVisibility() == View.VISIBLE) {
                    Toast.makeText(getBaseContext(), "ค่าอาหารต้องมากกว่า " + minprice + " บาท", Toast.LENGTH_SHORT).show();
                } else {
                    String strtitle = "";
                    if (DateUtils.isToday(deliverytime.getTime().getTime())) {
                        strtitle = "ต้องการให้ส่งวันนี้เวลา?";
                    } else if (isTomorrow(deliverytime.getTime().getTime())) {
                        strtitle = "ต้องการให้ส่งพรุ่งนี้เวลา?";
                    } else {
                        strtitle = "ต้องการให้ส่งหลังวันที่ " + formattedDateString + " เวลา?";
                    }
                    Calendar now = Calendar.getInstance();
                    TimePickerDialog tpd;
                    if (DateUtils.isToday(deliverytime.getTime().getTime())) {
                        now.add(Calendar.HOUR, Integer.parseInt(ordertime));
                        tpd = TimePickerDialog.newInstance(
                                Ordersummary.this,
                                now.get(Calendar.HOUR_OF_DAY),
                                now.get(Calendar.MINUTE),
                                false
                        );
                        //tpd.setAccentColor(Color.parseColor("#38C872"));
                        tpd.setTimeInterval(1, 5);
                        tpd.setTitle(strtitle);

                        tpd.setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialogInterface) {
                                Log.d("TimePicker", "Dialog was cancelled");
                            }
                        });
                        tpd.show(getFragmentManager(), "Timepickerdialog");
                    } else {
//                        tpd = TimePickerDialog.newInstance(
//                                Ordersummary.this,
//                                now.get(Calendar.HOUR_OF_DAY),
//                                now.get(Calendar.MINUTE),
//                                false
//                        );

                        builderSingle = new AlertDialog.Builder(Ordersummary.this);
                        builderSingle.setIcon(R.drawable.ic_time);
                        builderSingle.setTitle("ต้องการให้ส่งพรุ่งนี้เวลา?");
                        builderSingle.setNegativeButton(
                                "cancel",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });

                        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                                Ordersummary.this,
                                android.R.layout.select_dialog_singlechoice);
                        arrayAdapter.add("08:00น. - 11:00น.");
                        arrayAdapter.add("13:00น. - 17:00น.");

                        builderSingle.setAdapter(
                                arrayAdapter,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        String orderrange = arrayAdapter.getItem(which);
                                        Date reqdate = new Date();
                                        Intent intent = new Intent(Ordersummary.this, Orderconfirm.class);
                                        intent.putExtra("reqdate", reqdate);
                                        intent.putExtra("orderrange", orderrange);
                                        intent.putExtra("objs", tmp);
                                        intent.putExtra("cart", sList);
                                        intent.putExtra("summary", txtsummary.getText());
                                        startActivity(intent);
                                        overridePendingTransition(R.anim.enter, R.anim.exit);
                                    }
                                });
                        builderSingle.show();

                    }


                }

            }
        });

        loaditems();
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {

    }

    @Override
    public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute, int second) {
        if (DateUtils.isToday(deliverytime.getTime().getTime())) {
            Date reqdate = new Date();
            reqdate.setHours(hourOfDay);
            reqdate.setMinutes(minute);
            reqdate.setSeconds(second);
            long mills = reqdate.getTime() - deliverytime.getTime().getTime();
            int Mins = (int) TimeUnit.MILLISECONDS.toMinutes(mills);
            if (Mins >= 0) {
                Intent intent = new Intent(this, Orderconfirm.class);
                intent.putExtra("reqdate", reqdate);
                intent.putExtra("objs", this.tmp);
                intent.putExtra("cart", this.sList);
                intent.putExtra("summary", this.txtsummary.getText());
                startActivity(intent);
                overridePendingTransition(R.anim.enter, R.anim.exit);
            } else {
                if (Integer.parseInt(this.ordertime) > 0) {
                    Toast.makeText(getBaseContext(), "ต้องสั่งล่วงหน้าอย่างน้อย " + this.ordertime + " ชั่วโมง", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getBaseContext(), "ไม่สามารถตั้งเวลาส่งน้อยกว่าเวลาปัจจุบัน", Toast.LENGTH_SHORT).show();
                }

            }
        } else {
            Date reqdate = new Date();
            reqdate.setHours(hourOfDay);
            reqdate.setMinutes(minute);
            reqdate.setSeconds(second);
            Intent intent = new Intent(this, Orderconfirm.class);
            intent.putExtra("reqdate", reqdate);
            intent.putExtra("objs", this.tmp);
            intent.putExtra("cart", this.sList);
            intent.putExtra("summary", this.txtsummary.getText());
            startActivity(intent);
            overridePendingTransition(R.anim.enter, R.anim.exit);
        }

    }

    public static boolean isTomorrow(long date) {
        Calendar now = Calendar.getInstance();
        Calendar cdate = Calendar.getInstance();
        cdate.setTimeInMillis(date);

        now.add(Calendar.DATE, +1);

        return now.get(Calendar.YEAR) == cdate.get(Calendar.YEAR)
                && now.get(Calendar.MONTH) == cdate.get(Calendar.MONTH)
                && now.get(Calendar.DATE) == cdate.get(Calendar.DATE);
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left);
    }

    public void loaditems() {

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
        this.listAdpt = new OrderAdapter(this, this.sList, this.summary, this.txtsummary, this.idminorder, this.minprice, this.deliveryfee);
        this.listview.setAdapter(this.listAdpt);

    }

    private static void loadItems() {
        if (i.hasExtra("cart")) {
            ArrayList<HashMap<String, String>> cart = (ArrayList<HashMap<String, String>>) i.getSerializableExtra("cart");
            for (int i = 0; i < cart.size(); i++) {
                String strid = cart.get(i).get(KEY_ITEMID);
                String title = cart.get(i).get(KEY_ITEMTITLE);
                String price = cart.get(i).get(KEY_ITEMPRICE);
                String img = cart.get(i).get(KEY_ITEMIMAGE);
                String orderamount = cart.get(i).get(KEY_ORDERDETAILAMOUNT);

                if (Integer.parseInt(orderamount) > 0) {
                    map = new HashMap<String, String>();
                    map.put(ListItem.KEY_INDEX, "99");
                    map.put(ListItem.KEY_HEADER, "");
                    map.put(ListItem.KEY_ITEMID, strid);
                    map.put(ListItem.KEY_ITEMTITLE, title);
                    map.put(ListItem.KEY_ITEMPRICE, price);
                    map.put(ListItem.KEY_ITEMIMAGE, img);
                    map.put(ListItem.KEY_ORDERDETAILAMOUNT, orderamount);
                    sList.add(map);
                }


            }


        }

        map = new HashMap<String, String>();
        map.put(ListItem.KEY_INDEX, "100");
        map.put(ListItem.KEY_HEADER, "");
        map.put(KEY_ITEMID, "0");
        map.put(ListItem.KEY_ITEMTITLE, "summary");
        map.put(ListItem.KEY_ITEMPRICE, "0");
        map.put(ListItem.KEY_ITEMIMAGE, "0");
        map.put(ListItem.KEY_ORDERDETAILAMOUNT, "0");
        sList.add(map);
    }


}


class OrderAdapter extends BaseAdapter {
    private HashMap<String, String> tmp;
    private ArrayList<HashMap<String, String>> data;
    private Activity activity;
    private static LayoutInflater inflater = null;
    private FloatingActionButton fab;
    String title, img, distance, isshopopen, ordertime;
    Integer id;
    private HashMap<String, Double> summary;
    TextView txtsummary;
    RelativeLayout idminorder;
    String minorder;
    String deliveryfee;
    private static double cartsummary;

    public OrderAdapter(Activity a, ArrayList<HashMap<String, String>> d, HashMap<String, Double> s, TextView txtsummary, RelativeLayout idminorder, String minorder, String deliveryfee) {
        this.data = d;
        this.activity = a;
        this.summary = s;
        this.txtsummary = txtsummary;
        this.minorder = minorder;
        this.idminorder = idminorder;
        this.deliveryfee = deliveryfee;
        this.reloadsummary();
        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public int getCount() {
        return this.data.size();
    }

    private HashMap<String, Double> getAllAmount() {
        Double allamount = 0.0;
        Double total = 0.0;
        Double summary = 0.0;
        HashMap<String, Double> result = new HashMap<>();
        for (Integer i = 0; i < this.data.size(); i++) {
            final HashMap<String, String> tmp = this.data.get(i);
            String amount = tmp.get(ListItem.KEY_ORDERDETAILAMOUNT);
            String price = tmp.get(ListItem.KEY_ITEMPRICE);
            Log.d("price", price + "");
            allamount += Double.parseDouble(amount);
            summary += Double.parseDouble(price) * Double.parseDouble(amount);
        }
        result.put("total", summary);
        summary += Double.parseDouble(this.deliveryfee);
        result.put("allamount", allamount);
        result.put("summary", summary);
        return result;
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    public void reloadsummary() {
        summary = getAllAmount();
        Log.d("onHorizontal", "" + getAllAmount());
        DecimalFormat formatter = new DecimalFormat("#,###,###.##");
        String yourFormattedString = formatter.format(summary.get("summary"));

        cartsummary = summary.get("summary") - Double.parseDouble(this.deliveryfee);
        txtsummary.setText(yourFormattedString + "฿");

        Log.d("summary", "total" + summary.get("total"));
        Log.d("summary", "min" + Double.parseDouble(this.minorder));
        if (Double.parseDouble(this.minorder) <= summary.get("total")) {
            this.idminorder.setVisibility(View.GONE);
        } else {
            this.idminorder.setVisibility(View.VISIBLE);
        }
        this.notifyDataSetChanged();
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        final HashMap<String, String> tmp = this.data.get(position);
        String title = tmp.get(ListItem.KEY_ITEMTITLE);
        String price = tmp.get(ListItem.KEY_ITEMPRICE);
        String img = tmp.get(ListItem.KEY_ITEMIMAGE);
        String amount = tmp.get(ListItem.KEY_ORDERDETAILAMOUNT);
        if (title == "summary") {
            //cartsummary = getAllAmount().get("summary");
            convertView = inflater.inflate(R.layout.itemsummary, parent, false);
            TextView txttotal = (TextView) convertView.findViewById(R.id.txttotal);
            TextView txtdeliveryfee = (TextView) convertView.findViewById(R.id.txtdeliveryfee);
            txtdeliveryfee.setText(this.deliveryfee + "฿");
            txttotal.setText(cartsummary + "฿");

        } else {
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

            horizontal_number_picker.setVisibility(View.VISIBLE);


            horizontal_number_picker.setListener(new HorizontalNumberPickerListener() {
                @Override
                public void onHorizontalNumberPickerChanged(HorizontalNumberPicker horizontalNumberPicker, int value) {
                    tmp.put(ListItem.KEY_ORDERDETAILAMOUNT, value + "");
                    data.get(position).putAll(tmp);
                    reloadsummary();
                }
            });
        }


        return convertView;
    }


}
