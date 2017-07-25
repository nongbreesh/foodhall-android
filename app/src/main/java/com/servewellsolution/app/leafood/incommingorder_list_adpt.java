package com.servewellsolution.app.leafood;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Breeshy on 8/17/2016 AD.
 */

public class incommingorder_list_adpt extends BaseAdapter {
    public int getCount() {
        return this.data.size();
    }

    ArrayList<HashMap<String, String>> data;
    private Activity activity;
    private LinearLayout listOrderLayout;
    private String orderid;
    private static LayoutInflater inflater = null;

    public incommingorder_list_adpt(Activity a, LinearLayout listOrderLayout, ArrayList<HashMap<String, String>> d) {
        this.activity = a;
        this.data = d;
        this.listOrderLayout = listOrderLayout;
        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        HashMap<String, String> tmp = this.data.get(position);
        String img = tmp.get(ListItem.KEY_ORDERDETAILIMAGE);
        String title = tmp.get(ListItem.KEY_ORDERDETAILTITLE);
        String price = tmp.get(ListItem.KEY_ORDERDETAILPRICE);
        String moredetail = tmp.get(ListItem.KEY_ORDERDETAILMOREDETAIL);
        String amount = tmp.get(ListItem.KEY_ORDERDETAILAMOUNT);

        convertView = inflater.inflate(R.layout.incommingorder_list, parent, false);
        ViewGroup.LayoutParams params = this.listOrderLayout.getLayoutParams();
        params.height = 350 * this.data.size();
        listOrderLayout.setLayoutParams(params);

        TextView txttitle = (TextView) convertView.findViewById(R.id.title);
        TextView txtprice = (TextView) convertView.findViewById(R.id.price);
        TextView txttxtmoredetail = (TextView) convertView.findViewById(R.id.txtmoredetail);

        txttitle.setText(title + " (" + amount + ")");
        txtprice.setText(price + " บาท");
        txttxtmoredetail.setText(moredetail);

        return convertView;
    }
}
