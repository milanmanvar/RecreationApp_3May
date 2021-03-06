package com.recreation.recreationapp.view;

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
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.recreation.recreationapp.R;
import com.recreation.recreationapp.ReCreationApplication;
import com.recreation.recreationapp.util.Constant;

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
        setUpActionBar("Session Times");
        switchMorning = (Switch) findViewById(R.id.searchinclude_switchMorning);
        switchLunch = (Switch) findViewById(R.id.searchinclude_switchLunch);
        switchEvening = (Switch) findViewById(R.id.searchinclude_switchEvening);
        switchMorning.setChecked(((ReCreationApplication) getApplication()).sharedPreferences.getBoolean("morning", true));
        switchLunch.setChecked(((ReCreationApplication) getApplication()).sharedPreferences.getBoolean("lunchtime", true));
        switchEvening.setChecked(((ReCreationApplication) getApplication()).sharedPreferences.getBoolean("evening", true));
        switchEvening.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //createGymClassApicall(3, isChecked);
            }
        });
        switchMorning.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //createGymClassApicall(1, isChecked);
            }
        });
        switchLunch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //createGymClassApicall(2, isChecked);
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

                if(switchMorning.isChecked() || switchLunch.isChecked() || switchEvening.isChecked()){
                    updateUserApicall();
                }else{
                    Toast.makeText(IncludingWithSearchActivity.this, "Please select at least one session time to search ", Toast.LENGTH_SHORT).show();
                }


            }
        });
        getActionBar().setCustomView(actionbarView);
    }

    private void updateUserApicall() {
        final ProgressDialog pd = ProgressDialog.show(IncludingWithSearchActivity.this, "", "Please wait", false, false);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Constant.updateRecreationUser,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (pd != null && pd.isShowing())
                            pd.dismiss();
                        Log.e("Update", "" + response);

                        String selected = getSelectedType();
                        Intent i = new Intent();
                        i.putExtra("selectedtype", selected);
                        setResult(RESULT_OK, i);
                        SharedPreferences.Editor e = ((ReCreationApplication) getApplication()).sharedPreferences.edit();
                        e.putString("selectedtype", selected);
                        e.commit();
                        finish();
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
                params.put("clubsFilter", reCreationApplication.sharedPreferences.getString("clubsFilter", ""));
                params.put("fullName", reCreationApplication.sharedPreferences.getString("fullname", ""));
                //if (searchWith == 1)
                    params.put("searchMorningClasses", switchMorning.isChecked()+"");
                //else if (searchWith == 2)
                    params.put("searchLunchtimeClasses", switchLunch.isChecked()+"");
                //else
                    params.put("searchEveningClasses", switchEvening.isChecked()+"");

                params.put("hasAllowedAccessToCalendar", reCreationApplication.sharedPreferences.getBoolean(getString(R.string.pref_alert_access_your_calender),false)+"");
                params.put("hasAllowedNotifications", reCreationApplication.sharedPreferences.getBoolean(getString(R.string.pref_alert_send_you_notification),false)+"");

                Log.e("update selected ", "" + params.toString());


                SharedPreferences.Editor e = reCreationApplication.sharedPreferences.edit();
                e.putBoolean(getString(R.string.pref_is_morning_classes), switchMorning.isChecked());
                e.putBoolean(getString(R.string.pref_is_lunch_classes), switchLunch.isChecked());
                e.putBoolean(getString(R.string.pref_is_evening_classes), switchEvening.isChecked());
                e.commit();

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
