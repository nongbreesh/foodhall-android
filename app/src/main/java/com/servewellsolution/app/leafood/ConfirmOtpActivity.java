package com.servewellsolution.app.leafood;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
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

import static com.servewellsolution.app.leafood.SessionManagement.IS_LOGIN;
import static com.servewellsolution.app.leafood.SessionManagement.KEY_EMAIL;
import static com.servewellsolution.app.leafood.SessionManagement.KEY_FBID;
import static com.servewellsolution.app.leafood.SessionManagement.KEY_FULLNAME;
import static com.servewellsolution.app.leafood.SessionManagement.KEY_TEL;
import static com.servewellsolution.app.leafood.SessionManagement.KEY_USERID;
import static com.servewellsolution.app.leafood.SessionManagement.PREF_NAME;

/**
 * Created by Breeshy on 8/23/2016 AD.
 */

public class ConfirmOtpActivity extends Activity {
    private ProgressDialog dialog;
    private SharedPreferences.Editor editor;
    private EditText txtotp;
    private String tel;
    private AsyncTask<Void, Void, Void> task;
    private Boolean isvalidotp = false;
    private String idfb = "";
    Date startotp;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.confirmotp);
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        SharedPreferences pref = getApplicationContext().getSharedPreferences(PREF_NAME, 0); // 0 - for private mode
        editor = pref.edit();

        TextView txtsubtitle = (TextView) findViewById(R.id.txtsubtitle);
        txtotp = (EditText) findViewById(R.id.txtotp);

        Intent intent = getIntent();
        if (intent.hasExtra("tel")) {
            tel = intent.getStringExtra("tel");
            txtsubtitle.setText("พิมพ์รหัสที่ได้จาก SMS เบอร์ " + tel + " (รหัสใช้ได้เพียงครั้งเดียว)");
        }

        if (intent.hasExtra("idfb")) {
            idfb = (String) intent.getSerializableExtra("idfb");
        }


        dialog = new ProgressDialog(this);

        Button id_resend_button = (Button) findViewById(R.id.id_resend_button);
        Button id_submit_button = (Button) findViewById(R.id.id_submit_button);

        java.text.DateFormat df = new java.text.SimpleDateFormat("hh:mm:ss");
        this.startotp = new Date();
        id_resend_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //resend otp
                Date reqdate = new Date();
                long mills = reqdate.getTime() - startotp.getTime();
                int Hours = (int) (mills / (1000 * 60 * 60));
                int Mins = (int) (mills % (1000 * 60 * 60));
                Log.d("Mins", String.valueOf(Mins / 1000));
                if ((Mins / 1000) < 60) {
                    Toast.makeText(getBaseContext(), "กรุณาลองใหม่อีกครั้งในอีก " + (60 - (Mins / 1000)) + " วินาที", Toast.LENGTH_SHORT).show();
                } else {
                    sendOtp(tel);
                }

            }
        });

        id_submit_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //checking otp
                checkOtp(tel);
            }
        });


    }

    private void checkOtp(final String tel) {
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
                    verifyOtp(tel);
                } catch (Throwable e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                dialog.hide();
                if (isvalidotp) {
                    Log.d("idfb", idfb);
                    if (!idfb.equals("")) {
                        dologin();
                    } else {
                        Intent intent = new Intent(getApplication(), LastestRegisterActivity.class);
                        intent.putExtra("tel", tel);
                        startActivity(intent);
                    }

                } else {
                    Toast.makeText(getBaseContext(), "รหัสไม่ถูกต้อง", Toast.LENGTH_SHORT).show();
                }
            }

        };
        this.task.execute((Void[]) null);
    }

    private void dologin() {
        this.task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected Void doInBackground(Void... arg0) {
                try {
                    login();
                } catch (Throwable e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {

                Intent intent = new Intent(getApplication(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }

        };
        this.task.execute((Void[]) null);
    }

    private void login() {
        Log.d("Responselogin", "login: ");
        // Create a new HttpClient and Post Header
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(getString(R.string.apiaddress) + "api/buyer_login");
        Log.d("Responselogin", "ResponseAll: " + idfb);
        try {
            // Add your data
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
            nameValuePairs.add(new BasicNameValuePair("fbid", idfb));
            nameValuePairs.add(new BasicNameValuePair("email", ""));
            nameValuePairs.add(new BasicNameValuePair("password", ""));
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

            final JSONObject jsonObj = new JSONObject(s.toString());

            Log.d("Responselogin", "ResponseAll: " + jsonObj);
            if (!jsonObj.get("result").toString().equals("")) {
                final JSONObject obj = (JSONObject) jsonObj.get("result");
                Log.d("Responselogin", "ResponseObject: " + obj);

                //create login session
                editor.putString(IS_LOGIN, "1");
                try {
                    editor.putString(KEY_USERID, obj.get("id").toString());
                    editor.putString(KEY_EMAIL, obj.get("email").toString());
                    editor.putString(KEY_TEL, obj.get("tel").toString());
                    editor.putString(KEY_FBID, obj.get("fbid").toString());
                    editor.putString(KEY_FULLNAME, obj.get("fullname").toString());
                    editor.commit();
                    dialog.hide();
                } catch (JSONException e) {
                    dialog.hide();
                    e.printStackTrace();
                }


            }


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
                Toast.makeText(getBaseContext(), "ส่งรหัสใหม่เรียบร้อยแล้ว", Toast.LENGTH_SHORT).show();
            }

        };
        this.task.execute((Void[]) null);
    }

    private JSONObject generateOtp(String tel) {
        // Create a new HttpClient and Post Header
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(getString(R.string.apiaddress) + "api/generateOtp");
        JSONObject jsonObj = null;
        try {
            // Add your data
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
            nameValuePairs.add(new BasicNameValuePair("tel", tel));
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

            jsonObj = new JSONObject(s.toString());

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
        return jsonObj;
    }


    private void verifyOtp(String tel) {
        // Create a new HttpClient and Post Header
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(getString(R.string.apiaddress) + "api/verifyOtp");

        try {
            // Add your data
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
            nameValuePairs.add(new BasicNameValuePair("tel", tel));
            nameValuePairs.add(new BasicNameValuePair("fbid", idfb));
            nameValuePairs.add(new BasicNameValuePair("otp", txtotp.getText().toString().trim()));

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

            final JSONObject jsonObj = new JSONObject(s.toString());

            Log.d("Response", "ResponseAll: " + jsonObj);
            if (jsonObj.get("result").toString().equals("true")) {
                isvalidotp = true;
            } else {
                isvalidotp = false;
            }


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
}