package com.servewellsolution.app.leafood;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import java.util.List;

import static com.servewellsolution.app.leafood.SessionManagement.*;

/**
 * Created by Breeshy on 8/23/2016 AD.
 */

public class LastestRegisterActivity extends Activity {
    private ProgressDialog dialog;
    private AsyncTask<Void, Void, Void> task;
    EditText txtemail;
    EditText txtpassword;
    EditText txtconfirmpassword;
    EditText txttel;
    String tel, registerResult;
    private SharedPreferences.Editor editor;

    public final static boolean isValidEmail(CharSequence target) {
        if (target == null) {
            return false;
        } else {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lastestregister);
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        dialog = new ProgressDialog(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        Intent intent = getIntent();
        if (intent.hasExtra("tel")) {
            tel = intent.getStringExtra("tel");
        }


        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        this.txtemail = (EditText) findViewById(R.id.txtemail);
        this.txtpassword = (EditText) findViewById(R.id.txtpassword);
        this.txtconfirmpassword = (EditText) findViewById(R.id.txtconfirmpassword);

        Button id_submit_button = (Button) findViewById(R.id.id_submit_button);
        id_submit_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!txtpassword.getText().toString().equals("")
                        && !txtemail.getText().toString().equals("")
                        && !txtconfirmpassword.getText().toString().equals("")) {

                    if (txtpassword.getText().toString().equals(txtconfirmpassword.getText().toString())) {
                        if (isValidEmail(txtemail.getText().toString())) {
                            registeruser();
                        } else {
                            Toast.makeText(getBaseContext(), "กรุณากรอกอีเมลล์ให้ถูกต้อง", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getBaseContext(), "รหัสผ่านไม่ตรงกัน", Toast.LENGTH_SHORT).show();
                    }

                } else {
                    Toast.makeText(getBaseContext(), "กรุณากรอกข้อมูลให้ครบ", Toast.LENGTH_SHORT).show();
                }
            }

        });
    }

    private void registeruser() {
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
                    register();
                } catch (Throwable e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {

                dialog.hide();
                if (registerResult.equals("success")) {
                    Toast.makeText(getBaseContext(), "สมัครสมาชิกเรียบร้อยแล้วกรุณารอสักครู่", Toast.LENGTH_SHORT).show();

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            dologin();
                        }
                    }, 3 * 1000);
                } else {
                    Toast.makeText(getBaseContext(), "อีเมลล์ซ้ำในระบบ", Toast.LENGTH_SHORT).show();
                }

            }

        };
        this.task.execute((Void[]) null);
    }

    private void register() {

        // Create a new HttpClient and Post Header
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(getString(R.string.apiaddress) + "api/buyer_register");

        try {
            // Add your data
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
            nameValuePairs.add(new BasicNameValuePair("password", txtpassword.getText().toString().trim()));
            nameValuePairs.add(new BasicNameValuePair("tel", tel));
            nameValuePairs.add(new BasicNameValuePair("email", txtemail.getText().toString().trim()));
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

            JSONObject jsonObj = new JSONObject(s.toString());
            Log.d("Response", "ResponseAll: " + jsonObj);
            Log.d("Response", "ResponseJson: " + jsonObj.get("result"));
            registerResult = jsonObj.get("result").toString();

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

    private void dologin() {
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
                    login();
                } catch (Throwable e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                dialog.hide();
                Intent intent = new Intent(getApplication(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }

        };
        this.task.execute((Void[]) null);
    }


    private void login() {

        // Create a new HttpClient and Post Header
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(getString(R.string.apiaddress) + "api/buyer_login");

        try {
            // Add your data
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
            nameValuePairs.add(new BasicNameValuePair("email", txtemail.getText().toString().trim()));
            nameValuePairs.add(new BasicNameValuePair("password", txtpassword.getText().toString().trim()));
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

            Log.d("Response", "ResponseAll: " + jsonObj);
            if (!jsonObj.get("result").toString().equals("")) {
                final JSONObject obj = (JSONObject) jsonObj.get("result");
                Log.d("Response", "ResponseObject: " + obj);

                //create login session
                editor.putString(IS_LOGIN, "1");
                try {
                    editor.putString(KEY_USERID, obj.get("id").toString());
                    editor.putString(KEY_EMAIL, obj.get("email").toString());
                    editor.putString(KEY_TEL, obj.get("tel").toString());
                    editor.putString(KEY_FBID, obj.get("fbid").toString());
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


}

