package com.servewellsolution.app.leafood;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

public class MerchantFragment extends Fragment {
    View rootView;

    public MerchantFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        this.rootView = inflater.inflate(R.layout.fragment_merchant, container, false);
        WebView webview = (WebView) this.rootView.findViewById(R.id.webview);


        // Inflate the layout for this fragment

        // Makes Progress bar Visible
        getActivity().getWindow().setFeatureInt(Window.FEATURE_PROGRESS, Window.PROGRESS_VISIBILITY_ON);

        webview.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                //Make the bar disappear after URL is loaded, and changes string to Loading...
                getActivity().setTitle("Loading...");
                getActivity().setProgress(progress * 100); //Make the bar disappear after URL is loaded

                // Return the app name after finish loading
                if (progress == 100)
                    getActivity().setTitle("เข้ามาร่วมเปิดร้านกับเรา");
            }
        });
        //webview.setWebViewClient(new HelloWebViewClient());
        webview.getSettings().setJavaScriptEnabled(true);
        webview.loadUrl("http://leafood.servewellsolution.com/becometomerchant/index");

        return rootView;

    }


}

