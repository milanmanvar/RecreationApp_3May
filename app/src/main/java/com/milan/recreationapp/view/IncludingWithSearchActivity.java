package com.milan.recreationapp.view;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.milan.recreationapp.R;
import com.milan.recreationapp.ReCreationApplication;
import com.milan.recreationapp.util.Constant;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by utsav.k on 07-04-2016.
 */
public class IncludingWithSearchActivity extends Activity {

    private Switch switchMorning, switchLunch, switchEvening;
    private ReCreationApplication reCreationApplication;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_searchincludewith);
        getActionBar().setDisplayShowCustomEnabled(true);
        getActionBar().setDisplayShowHomeEnabled(false);
        getActionBar().setDisplayUseLogoEnabled(false);
        getActionBar().setDisplayShowTitleEnabled(false);
        reCreationApplication = (ReCreationApplication) getApplication();
        setUpActionBar("Include with");
        switchMorning = (Switch) findViewById(R.id.searchinclude_switchMorning);
        switchLunch = (Switch) findViewById(R.id.searchinclude_switchLunch);
        switchEvening = (Switch) findViewById(R.id.searchinclude_switchEvening);
        switchMorning.setChecked(((ReCreationApplication) getApplication()).sharedPreferences.getBoolean("morning", true));
        switchLunch.setChecked(((ReCreationApplication) getApplication()).sharedPreferences.getBoolean("lunchtime", true));
        switchEvening.setChecked(((ReCreationApplication) getApplication()).sharedPreferences.getBoolean("evening", true));
        switchEvening.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                createGymClassApicall(3, isChecked);
            }
        });
        switchMorning.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                createGymClassApicall(1, isChecked);
            }
        });
        switchLunch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                createGymClassApicall(2, isChecked);
            }
        });
    }

    private String getSelectedType() {
        StringBuilder result = new StringBuilder();
        SharedPreferences.Editor e = ((ReCreationApplication) getApplication()).sharedPreferences.edit();
        if (switchMorning.isChecked()) {
            result.append("morning");
            result.append(",");
            e.putBoolean("morning", true);
        } else
            e.putBoolean("morning", false);

        if (switchLunch.isChecked()) {
            result.append("lunchtime");
            result.append(",");
            e.putBoolean("lunchtime", true);
        } else
            e.putBoolean("lunchtime", false);
        if (switchEvening.isChecked()) {
            result.append("evening");
            result.append(",");
            e.putBoolean("evening", true);
        } else
            e.putBoolean("evening", false);
        e.commit();

        return result.length() > 0 ? result.substring(0, result.length() - 1) : "";
    }

    private void setUpActionBar(String title) {
        View actionbarView = LayoutInflater.from(this).inflate(
                R.layout.actionbar_layout_with_close, null, true);

        TextView tvTitle = (TextView) actionbarView.findViewById(R.id.actionbar_layout_tv_title);
        tvTitle.setText(title);
        TextView tvClose = (TextView) actionbarView.findViewById(R.id.actionbar_layout_tv_close);

        tvClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String selected = getSelectedType();
                Intent i = new Intent();
                i.putExtra("selectedtype", selected);
                setResult(RESULT_OK, i);
                SharedPreferences.Editor e = ((ReCreationApplication) getApplication()).sharedPreferences.edit();
                e.putString("selectedtype", selected);
                e.commit();
                finish();
            }
        });
        getActionBar().setCustomView(actionbarView);
    }

    private void createGymClassApicall(final int searchWith, final boolean flag) {
        final ProgressDialog pd = ProgressDialog.show(IncludingWithSearchActivity.this, "", "Please wait", false, false);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Constant.updateRecreationUser,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (pd != null && pd.isShowing())
                            pd.dismiss();
                        Log.e("Update", "" + response);
                    }
                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        if (pd != null && pd.isShowing())
                            pd.dismiss();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                String id = UUID.randomUUID().toString();
                params.put("id", reCreationApplication.sharedPreferences.getString("userguid", ""));
//                params.put("userId", reCreationApplication.sharedPreferences.getString("userguid", ""));
                params.put("selectedClubName", reCreationApplication.sharedPreferences.getString("club", ""));
                params.put("clubsFilter", reCreationApplication.sharedPreferences.getString("clubsfilter", ""));
                params.put("fullName", reCreationApplication.sharedPreferences.getString("fullname", ""));
                if (searchWith == 1)
                    params.put("searchMorningClass", String.valueOf(flag));
                else if (searchWith == 2)
                    params.put("searchLunchClass", String.valueOf(flag));
                else
                    params.put("searchEveningClass", String.valueOf(flag));
                Log.e("update selected ", "" + params.toString());
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                //  headers.put("Content-Type", "application/x-www-form-urlencoded");
                return headers;
            }

            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                //Toast.makeText(WelcomeScreen.this,""+response.toString(),Toast.LENGTH_LONG).show();
                Log.e("status code", "" + response.statusCode);
                return super.parseNetworkResponse(response);
            }

            public String getBodyContentType() {
                return "application/x-www-form-urlencoded; charset=UTF-8";
            }

        };
        reCreationApplication.addToRequestQueue(stringRequest);
    }

}
