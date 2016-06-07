package com.recreation.recreationapp.view;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ListView;
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
import com.recreation.recreationapp.adapter.SearchWithListAdapter;
import com.recreation.recreationapp.model.SearchWithModel;
import com.recreation.recreationapp.util.Constant;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by utsav.k on 07-04-2016.
 */
public class SearchingWithClubActivity extends Activity {

    private ListView list;
    private ArrayList<SearchWithModel> clubs;
    private ArrayList<String> checkedClubs;
    private ReCreationApplication reCreationApplication;
    private SearchWithListAdapter searchWithListAdapter;
    private LinearLayout linearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_list);
        getActionBar().setDisplayShowCustomEnabled(true);
        getActionBar().setDisplayShowHomeEnabled(false);
        getActionBar().setDisplayUseLogoEnabled(false);
        getActionBar().setDisplayShowTitleEnabled(false);
        setUpActionBar("Clubs");
        checkedClubs = new ArrayList<>();
        reCreationApplication = (ReCreationApplication) getApplication();
//        clubs = ((ReCreationApplication) getApplication()).getDatabase().getClubListForSearch();
//        list = (ListView) findViewById(R.id.listView);
        linearLayout = (LinearLayout) findViewById(R.id.layout);
//        searchWithListAdapter = new SearchWithListAdapter(this, clubs);
//        list.setAdapter(searchWithListAdapter);
        setData();
    }

    private void setData() {
        clubs = ((ReCreationApplication) getApplication()).getDatabase().getClubListForSearch();
        for (int i = 0; i < clubs.size(); i++) {
            View vMorning = getLayoutInflater().inflate(R.layout.row_searchwith, null);
            Switch aSwitch = (Switch) vMorning.findViewById(R.id.row_searchwith_switch);
            aSwitch.setText(clubs.get(i).getName());
            final int finalI = i;
            if (reCreationApplication.sharedPreferences.getString("selectedclubs", "").contains(clubs.get(i).getName())) {
                aSwitch.setChecked(true);
                addToCheckList(clubs.get(finalI).getName(), true);
            } else {
                if(reCreationApplication.sharedPreferences.getString("selectedclubs", "").length()>0){
                    aSwitch.setChecked(false);
                    addToCheckList(clubs.get(finalI).getName(), false);
                }else{
                    aSwitch.setChecked(true);
                    addToCheckList(clubs.get(finalI).getName(), true);
                }
            }

            aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    addToCheckList(clubs.get(finalI).getName(), isChecked);
                }
            });
            linearLayout.addView(vMorning);
        }
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
//                String selected = TextUtils.join(",", searchWithListAdapter.getDataSet());
                String selected = TextUtils.join(",", checkedClubs);
                if (!selected.toString().trim().equalsIgnoreCase(""))
                    createGymClassApicall(selected);
                else
                    Toast.makeText(SearchingWithClubActivity.this, "Please select at least one club to search", Toast.LENGTH_SHORT).show();
            }
        });
        getActionBar().setCustomView(actionbarView);
    }

    public void addToCheckList(String club, boolean checked) {
        if (checked) {
            if (!checkedClubs.contains(club))
                checkedClubs.add(club);
        } else {
            if (checkedClubs.contains(club))
                checkedClubs.remove(club);
        }
    }

    private void createGymClassApicall(final String clubs) {
        final ProgressDialog pd = ProgressDialog.show(SearchingWithClubActivity.this, "", "Please wait", false, false);
        final int status;
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Constant.updateRecreationUser,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (pd != null && pd.isShowing())
                            pd.dismiss();
                        Log.e("Update", "" + response);
                        Intent i = new Intent();
                        i.putExtra("selectedclubs", clubs);
                        setResult(RESULT_OK, i);
                        SharedPreferences.Editor e = ((ReCreationApplication) getApplication()).sharedPreferences.edit();
                        e.putString("selectedclubs", clubs);
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
                SharedPreferences.Editor e = reCreationApplication.sharedPreferences.edit();

                JSONArray jsonArray = new JSONArray();
                String[] clubsArray = clubs.split(",");
                for (int i=0;i<clubsArray.length;i++)
                    jsonArray.put(clubsArray[i]);

                e.putString("clubsfilter", jsonArray.toString());
                e.commit();
                params.put("clubsFilter", reCreationApplication.sharedPreferences.getString("clubsfilter", ""));
                params.put("fullName", reCreationApplication.sharedPreferences.getString("fullname", ""));
                params.put("hasAllowedAccessToCalendar", reCreationApplication.sharedPreferences.getBoolean(getString(R.string.pref_alert_access_your_calender),false)+"");
                params.put("hasAllowedNotifications", reCreationApplication.sharedPreferences.getBoolean(getString(R.string.pref_alert_send_you_notification),false)+"");
                Log.e("update selected clubs", "" + params.toString());

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
