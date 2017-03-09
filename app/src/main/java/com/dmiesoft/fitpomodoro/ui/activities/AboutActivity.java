package com.dmiesoft.fitpomodoro.ui.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dmiesoft.fitpomodoro.R;
import com.dmiesoft.fitpomodoro.utils.LicensesDataProvider;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class AboutActivity extends AppCompatActivity {

    private LinearLayout aboutLayout;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        aboutLayout = (LinearLayout) findViewById(R.id.aboutLayout);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        LicensesDataProvider ldp = new LicensesDataProvider();

        HashMap<String, String> map = ldp.getLicenses();

        LinearLayout.LayoutParams paramsHeader = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        LinearLayout.LayoutParams paramsLicense = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        paramsLicense.setMargins(0, 0, 0, (int) getResources().getDimension(R.dimen.license_margin_bottom));

        for (Map.Entry<String, String> entry : map.entrySet()) {
            String header = entry.getKey();
            String license = entry.getValue();
            TextView tvHeader = new TextView(this);
            tvHeader.setText(header);
            tvHeader.setLayoutParams(paramsHeader);
            tvHeader.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.header_text_size));
            aboutLayout.addView(tvHeader);

            TextView tvLicense = new TextView(this);
            tvLicense.setText(license);
            tvLicense.setLayoutParams(paramsLicense);
            tvLicense.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.license_text_size));
            aboutLayout.addView(tvLicense);
        }

    }
}
