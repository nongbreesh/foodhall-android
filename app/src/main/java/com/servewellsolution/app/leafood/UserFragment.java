package com.servewellsolution.app.leafood;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.login.LoginManager;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import static com.servewellsolution.app.leafood.SessionManagement.KEY_EMAIL;
import static com.servewellsolution.app.leafood.SessionManagement.KEY_FBID;
import static com.servewellsolution.app.leafood.SessionManagement.KEY_FULLNAME;
import static com.servewellsolution.app.leafood.SessionManagement.KEY_TEL;
import static com.servewellsolution.app.leafood.SessionManagement.KEY_USERID;
import static com.servewellsolution.app.leafood.SessionManagement.PREF_NAME;

/**
 * Created by Breeshy on 9/11/2016 AD.
 */

public class UserFragment extends Fragment {
    TextView txtusername;
    TextView txttel;
    TextView txtemail;
    ImageView imguser;
    Button btnlogout;
    private ProgressDialog dialog;
    private SharedPreferences.Editor editor;
    private HashMap<String, String> userdetail;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_user, container, false);
        dialog = new ProgressDialog(getContext());
        SharedPreferences pref = getContext().getSharedPreferences(PREF_NAME, 0); // 0 - for private mode
        editor = pref.edit();
        this.setting();
        this.txtusername = (TextView) rootView.findViewById(R.id.txtusername);
        this.txtemail = (TextView) rootView.findViewById(R.id.txtemail);
        this.txttel = (TextView) rootView.findViewById(R.id.txttel);
        this.imguser = (ImageView) rootView.findViewById(R.id.imguser);
        if (userdetail.get(KEY_USERID) != null) {
            this.txtemail.setText(userdetail.get(KEY_EMAIL).toString());

            Log.d("userdetail", userdetail.get(KEY_TEL) + "");
            if (userdetail.get(KEY_TEL) != null) {
                this.txttel.setText("เบอร์โทรศัพท์ " + userdetail.get(KEY_TEL).toString());
                txttel.setVisibility(View.VISIBLE);
            }
            if (userdetail.get(KEY_FBID) != null) {
                imguser.setVisibility(View.VISIBLE);
                if (userdetail.get(KEY_FULLNAME) != null) {

                    txtusername.setVisibility(View.VISIBLE);
                    txtusername.setText(userdetail.get(KEY_FULLNAME).toString());
                }
                Picasso.with(getContext()).load("https://graph.facebook.com/" + userdetail.get(KEY_FBID).toString() + "/picture?type=large&redirect=true&width=500&height=500").transform(new CircleTransform()).into(imguser);
            }
        }

        Button btnlogout = (Button) rootView.findViewById(R.id.btnlogout);
        btnlogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(getContext())
                        .setTitle("แจ้งเตือน")
                        .setMessage("คุณต้องการออกจากระบบใช่หรือไม่?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int whichButton) {
                                LoginManager.getInstance().logOut();
                                editor.clear();
                                editor.commit();
                                Intent i = new Intent(getContext(), MainActivity.class);

                                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                                // Staring Login Activity
                                startActivity(i);
                            }
                        })
                        .setNegativeButton(android.R.string.no, null).show();
            }
        });

        return rootView;
    }

    private void setting() {
        SessionManagement sess = new SessionManagement(getContext());
        this.userdetail = sess.getUserDetails();
    }


}
