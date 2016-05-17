package com.recreation.recreationapp.view;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupMenu;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.recreation.recreationapp.R;
import com.recreation.recreationapp.ReCreationApplication;
import com.recreation.recreationapp.adapter.ClubDetailTimeTableListAdapter;
import com.recreation.recreationapp.model.ClubModel_New;
import com.recreation.recreationapp.model.ClubTimeTable_New;
import com.recreation.recreationapp.util.Constant;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Created by milanmanvar on 05/04/16.
 */
public class ClubTimeTableActivity extends BaseActivity {

    private ListView list;
    private ArrayList<ClubTimeTable_New> clubTimeTables;
    private String selectedClub;
    private int selectedClubPos;
    private Button btnClub;
    private ReCreationApplication reCreationApplication;
    private PopupMenu clubPopUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clubtitmetable);


        list = (ListView) findViewById(R.id.clubtimetable_list);
        btnClub = (Button) findViewById(R.id.clubtimetable_btnClub);

        reCreationApplication = (ReCreationApplication) getApplicationContext();

//        clubTimeTables = Utils.parseClubDetailXML(reCreationApplication.sharedPreferences.getString("club" + selectedClubPos, ""));
        clubPopUp = new PopupMenu(this, btnClub);
        clubPopUp.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                btnClub.setText(item.getTitle());
                SharedPreferences.Editor editor = ((ReCreationApplication) getApplication()).sharedPreferences.edit();
                editor.putString("club", item.getTitle().toString());
                editor.putInt("clubposition", item.getOrder());
                editor.commit();
                setData();

                updateUserApicall();

                return true;
            }
        });
        final ArrayList<ClubModel_New> clubs = reCreationApplication.getDatabase().getClubList();
        for (int i = 0; i < clubs.size(); i++)
            clubPopUp.getMenu().add(i, i + 1, i + 1, clubs.get(i).getName());
        btnClub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clubPopUp.show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        setData();
    }

    private void updateUserApicall() {
        final ProgressDialog pd = ProgressDialog.show(ClubTimeTableActivity.this, "", "Please wait", false, false);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Constant.updateRecreationUser,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (pd != null && pd.isShowing())
                            pd.dismiss();
                        Log.e("Sign up:", "" + response);
                        //Toast.makeText(ClubTimeTableActivity.this,"successfully call", Toast.LENGTH_LONG).show();
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

                params.put("id", reCreationApplication.sharedPreferences.getString("userguid",""));
                params.put("fullName", reCreationApplication.sharedPreferences.getString("fullname",""));
                params.put("selectedClubName", btnClub.getText().toString());
                params.put("clubsFilter", reCreationApplication.sharedPreferences.getString("clubsFilter",""));

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
        reCreationApplication.addToRequestQueue(stringRequest);
    }

    private void setData() {
        selectedClub = reCreationApplication.sharedPreferences.getString("club", "");
        selectedClubPos = reCreationApplication.sharedPreferences.getInt("clubposition", 0);
        clubTimeTables = reCreationApplication.getDatabase().getClubTimeTableFromName(selectedClub);
        btnClub.setText(selectedClub);
        setUpActionBar(selectedClub + "");
        findViewById(R.id.actionbar_layout_iv_myclass).setVisibility(View.GONE);
        ArrayList<ClubTimeTable_New> temp = new ArrayList<>();
        String currentDay = getCurrentDay();
        boolean flag = false;
        int tempI = 0;
        for (int i = 0; i < clubTimeTables.size(); i++) {
            if (clubTimeTables.get(i).getDay().equalsIgnoreCase(currentDay)) {
                ClubTimeTable_New c = clubTimeTables.get(i);
                temp.add(0, c);
                flag = true;
                tempI = tempI + 1;
            } else if (flag) {
                ClubTimeTable_New c1 = clubTimeTables.get(i);
                temp.add(tempI, c1);
                flag = false;
                tempI = tempI + 1;
            } else {
                ClubTimeTable_New c2 = clubTimeTables.get(i);
                if (tempI == 0)
                    temp.add(c2);
                else {
                    temp.add(tempI, c2);
                    tempI = tempI + 1;
                }

            }

        }
        list.setAdapter(new ClubDetailTimeTableListAdapter(this, temp));
    }

    private String getCurrentDay() {
        String weekDay;
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.US);
        Calendar calendar = Calendar.getInstance();
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        weekDay = dayFormat.format(calendar.getTime());
        Log.e("Week day:", "" + weekDay);
        return weekDay;
    }
}
