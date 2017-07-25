package com.servewellsolution.app.leafood;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Breeshy on 8/23/2016 AD.
 */

public class RegisterActivity extends Activity {
    private ProgressDialog dialog;
    private AsyncTask<Void, Void, Void> task;
    EditText txttel;
    private String idfb = "";
    Date startotp;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        Intent i = getIntent();
        if (i.hasExtra("idfb")) {
            idfb = (String) i.getSerializableExtra("idfb");
        }

        dialog = new ProgressDialog(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);


        TextView linklogin = (TextView) findViewById(R.id.linklogin);
        SpannableString content = content = new SpannableString("ล๊อคอินเข้าสู่ระบบ");
        content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
        linklogin.setText(content);
        linklogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        this.txttel = (EditText) findViewById(R.id.txttel);

        Button id_submit_button = (Button) findViewById(R.id.id_submit_button);
        id_submit_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!txttel.getText().toString().equals("")) {

                    if (isValidPhoneNumber(txttel.getText().toString())) {
                        if(startotp == null){
                            startotp = new Date();
                            sendOtp(txttel.getText().toString());
                        }
                        else{
                            Date reqdate = new Date();
                            long mills = reqdate.getTime() - startotp.getTime();
                            int Hours = (int) (mills / (1000 * 60 * 60));
                            int Mins = (int) (mills % (1000 * 60 * 60));
                            Log.d("Mins", String.valueOf(Mins / 1000));
                            if ((Mins / 1000) < 60) {
                                Toast.makeText(getBaseContext(), "กรุณาลองใหม่อีกครั้งในอีก " + (60 - (Mins / 1000)) + " วินาที", Toast.LENGTH_SHORT).show();
                            } else {
                                startotp = new Date();
                                sendOtp(txttel.getText().toString());
                            }
                        }


                    } else {
                        Toast.makeText(getBaseContext(), "เบอร์โทรศัพท์ไม่ถูกต้อง", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getBaseContext(), "กรุณาระบุเบอร์โทรศัพท์", Toast.LENGTH_SHORT).show();
                }

            }

        });
    }

    private void sendOtp(final String tel) {
        this.task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                dialog.setMessage("กรุณารอสักครู่...");
                dialog.setCancelable(false);
                dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                dialog.show();

            }

            @Override
            protected Void doInBackground(Void... arg0) {
                try {
                    generateOtp(tel);
                } catch (Throwable e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                dialog.hide();

                Intent intent = new Intent(getApplication(), ConfirmOtpActivity.class);
                intent.putExtra("tel", txttel.getText().toString());
                intent.putExtra("idfb", idfb);
                startActivity(intent);

            }

        };
        this.task.execute((Void[]) null);
    }

    private void generateOtp(String tel) {
        // Create a new HttpClient and Post Header
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(getString(R.string.apiaddress) + "api/generateOtp");

        try {
            // Add your data
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
            nameValuePairs.add(new BasicNameValuePair("tel", tel));
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
            Log.d("Response", "Responses: " + s);
            final JSONObject jsonObj = new JSONObject(s.toString());

            Log.d("Response", "ResponseAll: " + jsonObj);


        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
            Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            dialog.hide();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            dialog.hide();
        } catch (JSONException e) {
            e.printStackTrace();
            dialog.hide();
        }
    }

    public static final boolean isValidPhoneNumber(CharSequence target) {
        if (target == null || TextUtils.isEmpty(target)) {
            return false;
        } else {
            if (target.length() == 10 && target.charAt(0) == '0') {
                return android.util.Patterns.PHONE.matcher(target).matches();
            } else {
                return false;
            }
        }
    }

}

