package com.servewellsolution.app.leafood;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.design.widget.Snackbar;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
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

public class LoginActivity extends Activity {
    private ProgressDialog dialog;
    private SharedPreferences.Editor editor;
    private EditText txtemail;
    private EditText txtpassword;
    private LoginButton loginButton;
    private CallbackManager callbackManager;
    private ProgressDialog progressDialog;
    User user;
    String registerResult, telResult;
    private AsyncTask<Void, Void, Void> task;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.login);
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        dialog = new ProgressDialog(this);
        FacebookSdk.sdkInitialize(getApplicationContext());
        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "com.servewellsolution.app.leafood",  // replace with your unique package name
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.e("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("KeyHash:", e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            Log.e("KeyHash:", e.getMessage());
        }
        Button id_submit_button = (Button) findViewById(R.id.id_submit_button);
        loginButton = (LoginButton) findViewById(R.id.id_submit_fb);

        TextView linkregister = (TextView) findViewById(R.id.linkregister);
        SpannableString content = new SpannableString("สมัครสมาชิกเพื่อใช้บริการ");
        content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
        linkregister.setText(content);
        linkregister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(i);
            }
        });

        SharedPreferences pref = getApplicationContext().getSharedPreferences(PREF_NAME, 0); // 0 - for private mode
        editor = pref.edit();


        id_submit_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txtemail = (EditText) findViewById(R.id.txtemail);
                txtpassword = (EditText) findViewById(R.id.txtpassword);
                if (txtemail.getText().toString().equals("") || txtpassword.getText().toString().equals("")) {
                    Snackbar.make(v, "กรุณาระบุ Username และ Password", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                } else {

                    dialog.setMessage("กรุณารอสักครู่...");
                    dialog.setCancelable(false);
                    dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    dialog.show();


                    // Create a new HttpClient and Post Header
                    HttpClient httpclient = new DefaultHttpClient();
                    HttpPost httppost = new HttpPost(getString(R.string.apiaddress) + "api/buyer_login");

                    try {
                        // Add your data
                        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
                        nameValuePairs.add(new BasicNameValuePair("email", txtemail.getText().toString().trim()));
                        nameValuePairs.add(new BasicNameValuePair("password", txtpassword.getText().toString().trim()));
                        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs,"UTF-8"));
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
                            editor.putString(IS_LOGIN, "1");
                            try {
                                editor.putString(KEY_USERID, obj.get("id").toString());
                                editor.putString(KEY_EMAIL, obj.get("email").toString());
                                editor.putString(KEY_TEL, obj.get("tel").toString());
                                editor.putString(KEY_FBID, obj.get("fbid").toString());

                                editor.commit();
                                dialog.hide();

                                Intent intent = new Intent(getApplication(), MainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            } catch (JSONException e) {
                                dialog.hide();
                                e.printStackTrace();
                            }
                        } else {
                            dialog.hide();
                            Toast.makeText(getBaseContext(), "ไม่สามารถเข้าสู่ระบบได้... กรุณาตรวจสอบข้อมูลใหม่อีกครั้ง", Toast.LENGTH_SHORT).show();
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
        });


    }

    @Override
    protected void onResume() {
        super.onResume();


        callbackManager = CallbackManager.Factory.create();

        loginButton = (LoginButton) findViewById(R.id.id_submit_fb);

        loginButton.setReadPermissions("public_profile", "email", "user_friends");
        loginButton.registerCallback(callbackManager, mCallBack);

//        btnLogin = (TextView) findViewById(R.id.btnLogin);
//        btnLogin.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                progressDialog = new ProgressDialog(LoginActivity.this);
//                progressDialog.setMessage("Loading...");
//                progressDialog.show();
//
//                loginButton.performClick();
//
//                loginButton.setPressed(true);
//
//                loginButton.invalidate();
//
//                loginButton.registerCallback(callbackManager, mCallBack);
//
//                loginButton.setPressed(false);
//
//                loginButton.invalidate();
//
//            }
//        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private FacebookCallback<LoginResult> mCallBack = new FacebookCallback<LoginResult>() {
        @Override
        public void onSuccess(LoginResult loginResult) {

            // App code
            GraphRequest request = GraphRequest.newMeRequest(
                    loginResult.getAccessToken(),
                    new GraphRequest.GraphJSONObjectCallback() {
                        @Override
                        public void onCompleted(
                                JSONObject object,
                                GraphResponse response) {

                            Log.e("response: ", response + "");
                            try {
                                user = new User();
                                user.facebookID = object.getString("id").toString();
                                user.email = object.getString("email").toString();
                                user.name = object.getString("name").toString();
                                user.gender = object.getString("gender").toString();
                                Log.d("user", user + "");
//                                PrefUtils.setCurrentUser(user,LoginActivity.this);

                            } catch (Exception e) {
                                Log.d("user", e.getMessage() + "");
                                e.printStackTrace();
                            }
                            //Toast.makeText(LoginActivity.this, "welcome " + user.name, Toast.LENGTH_LONG).show();
//                            Intent intent=new Intent(LoginActivity.this,LogoutActivity.class);
//                            startActivity(intent);
//                            finish();
                            registeruser();
                        }

                    });

            Bundle parameters = new Bundle();
            parameters.putString("fields", "id,name,email,gender, birthday");
            request.setParameters(parameters);
            request.executeAsync();
        }

        @Override
        public void onCancel() {
            progressDialog.dismiss();
        }

        @Override
        public void onError(FacebookException e) {
            progressDialog.dismiss();
        }
    };

    private void registeruser() {
        this.task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
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
                    if (telResult.equals("")) {
                        LoginManager.getInstance().logOut();
                        LoginManager.getInstance().logOut();
                        Intent i = new Intent(LoginActivity.this, RegisterActivity.class);
                        i.putExtra("idfb",user.facebookID);
                        startActivity(i);
                    } else {
                        dologin();
                    }

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
            nameValuePairs.add(new BasicNameValuePair("tel", ""));
            nameValuePairs.add(new BasicNameValuePair("fbid", user.facebookID));
            nameValuePairs.add(new BasicNameValuePair("name", user.name));
            nameValuePairs.add(new BasicNameValuePair("gender", user.gender));
            nameValuePairs.add(new BasicNameValuePair("email", user.email));
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs,"UTF-8"));
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
            telResult = jsonObj.get("tel").toString();

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
        Log.d("Responselogin", "ResponseAll: " + user.facebookID);
        try {
            // Add your data
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
            nameValuePairs.add(new BasicNameValuePair("fbid", user.facebookID));
            nameValuePairs.add(new BasicNameValuePair("email", ""));
            nameValuePairs.add(new BasicNameValuePair("password", ""));
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs,"UTF-8"));
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
}