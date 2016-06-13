package com.recreation.recreationapp.view;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.recreation.recreationapp.R;
import com.recreation.recreationapp.ReCreationApplication;
import com.recreation.recreationapp.model.ClubModel_New;
import com.recreation.recreationapp.util.Constant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by utsav.k on 05-04-2016.
 */
public class ClubInfoActivity extends BaseActivity {

    private TextView txtAddress;
    private LinearLayout lHoursDetail;
    private ClubModel_New clubData;
    private ArrayList<ClubModel_New> list;
    private Button btnMakeMyClub;
    private TextView tvMyClub;
    private ReCreationApplication application;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_info);
        lHoursDetail = (LinearLayout) findViewById(R.id.location_detail_lHoursDetail);
        txtAddress = (TextView) findViewById(R.id.location_detail_txtAddress);
        tvMyClub = (TextView) findViewById(R.id.location_detail_txtLblSaved);
        btnMakeMyClub = (Button) findViewById(R.id.location_detail_btnMakeMyClub);
        ImageView ivCall = (ImageView) findViewById(R.id.location_detail_imgCall);
        ivCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(clubData!=null){
                    startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("tel:"+clubData.getPhone())));
                }
            }
        });
        if (this.getIntent().hasExtra("clubdata")) {
            clubData = (ClubModel_New) this.getIntent().getSerializableExtra("clubdata");
        }
        if (clubData != null) {
            txtAddress.setText(clubData.getAddress() + "\n" + clubData.getPhone());
            setUpActionBar(clubData.getName());

            application = (ReCreationApplication) getApplicationContext();
            //btnMakeMyClub.setText(clubData.getName());
            if(application.sharedPreferences.getString("club","").equalsIgnoreCase(clubData.getName())){
                tvMyClub.setVisibility(View.VISIBLE);
                btnMakeMyClub.setVisibility(View.GONE);
            }else{
                tvMyClub.setVisibility(View.GONE);
                btnMakeMyClub.setVisibility(View.VISIBLE);
            }

            btnMakeMyClub.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updateUserApicall();
                }
            });

            list = ((ReCreationApplication) getApplication()).getDatabase().getClubDataFromName(clubData.getName());
            String temp = "";
            for (int i = 0; i < list.size(); i++) {

                View v = getLayoutInflater().inflate(R.layout.row_hours_title, null);
                TextView txtTitle = (TextView) v.findViewById(R.id.row_hours_title);
                txtTitle.setText(list.get(i).getHoursTitle());

                if (!temp.equalsIgnoreCase(txtTitle.getText().toString().trim()))
                    lHoursDetail.addView(v);
                temp = txtTitle.getText().toString().trim();

                View vBody = getLayoutInflater().inflate(R.layout.row_hours, null);
                TextView txtDay = (TextView) vBody.findViewById(R.id.row_day);
                TextView txtHours = (TextView) vBody.findViewById(R.id.row_hours);
                txtDay.setText(list.get(i).getDays());
                txtHours.setText(list.get(i).getHours());
                lHoursDetail.addView(vBody);
            }
        }
    }



    private void updateUserApicall() {
        final ProgressDialog pd = ProgressDialog.show(ClubInfoActivity.this, "", "Please wait", false, false);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Constant.updateRecreationUser,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (pd != null && pd.isShowing())
                            pd.dismiss();
                        Log.e("Sign up:", "" + response);
                        //Toast.makeText(ClubTimeTableActivity.this,"successfully call", Toast.LENGTH_LONG).show();

                        SharedPreferences.Editor editor = application.sharedPreferences.edit();
                        editor.putString("club",clubData.getName());
                        editor.commit();


                        if(application.sharedPreferences.getString("club","").equalsIgnoreCase(clubData.getName())){
                            tvMyClub.setVisibility(View.VISIBLE);
                            btnMakeMyClub.setVisibility(View.GONE);
                        }else{
                            tvMyClub.setVisibility(View.GONE);
                            btnMakeMyClub.setVisibility(View.VISIBLE);
                        }
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

                params.put("id", application.sharedPreferences.getString("userguid",""));
                params.put("fullName", application.sharedPreferences.getString("fullname",""));
                params.put("selectedClubName", clubData.getName());
                params.put("clubsFilter", application.sharedPreferences.getString("clubsFilter",""));
                params.put("hasAllowedAccessToCalendar", application.sharedPreferences.getBoolean(getString(R.string.pref_alert_access_your_calender),false)+"");
                params.put("hasAllowedNotifications", application.sharedPreferences.getBoolean(getString(R.string.pref_alert_send_you_notification),false)+"");
                params.put("searchMorningClasses", application.sharedPreferences.getBoolean(getString(R.string.pref_is_morning_classes), true)+"");
                params.put("searchLunchtimeClasses", application.sharedPreferences.getBoolean(getString(R.string.pref_is_lunch_classes), true)+"");
                params.put("searchEveningClasses", application.sharedPreferences.getBoolean(getString(R.string.pref_is_evening_classes), true)+"");

                Log.e("sign up req param:", "" + params.toString());
//                SharedPreferences.Editor e = reCreationApplication.sharedPreferences.edit();
//                e.putString("userguid", userGUid);
//                e.putString("clubsfilter", jsClub.toString());
//                e.putString("fullname", etYourName.getText().toString().trim());
//                e.commit();
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
        application.addToRequestQueue(stringRequest);
    }
    @Override
    public void setUpActionBar(String title) {

            //View actionbarView = LayoutInflater.from(this).inflate(
             //       R.layout.actionbar_layout_for_alert, null, true);

        getActionBar().setCustomView(R.layout.actionbar_layout_for_alert);
            TextView tvTitle = (TextView) findViewById(R.id.actionbar_layout_tv_title);
            tvTitle.setText(title);
            TextView tvClose = (TextView) findViewById(R.id.actionbar_layout_tv_close);
            final TextView tvAdd = (TextView) findViewById(R.id.actionbar_layout_tv_add);
            tvClose.setVisibility(View.VISIBLE);
            tvClose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        tvAdd.setVisibility(View.GONE);
            tvAdd.setText("Edit");




    }
}
