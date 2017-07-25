package com.servewellsolution.app.leafood;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.HashMap;

import static com.servewellsolution.app.leafood.ListItem.KEY_ITEMIMAGE;
import static com.servewellsolution.app.leafood.ListItem.KEY_ITEMPRICE;
import static com.servewellsolution.app.leafood.ListItem.KEY_ITEMTITLE;

public class ItemDetailActivity extends AppCompatActivity {


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.itemdetail);
        Window window = this.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(this.getResources().getColor(R.color.colorPrimaryDark));
        ImageView imgmenu = (ImageView) findViewById(R.id.imgmenu);
        TextView txttitle= (TextView) findViewById(R.id.txttitle);
        TextView txtprice= (TextView) findViewById(R.id.txtprice);
        Intent i = this.getIntent();
        if (i.hasExtra("obj")) {
            HashMap<String, String> tmp = (HashMap<String, String>) i.getSerializableExtra("obj");
            Log.d("tmp",""+tmp);
            Picasso.with(this).load(this.getString(R.string.imageaddress) + tmp.get(KEY_ITEMIMAGE)).into(imgmenu);
            txttitle.setText(tmp.get(KEY_ITEMTITLE));
            txtprice.setText("ราคา " + tmp.get(KEY_ITEMPRICE) + " บาท");
        }

    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }


}
