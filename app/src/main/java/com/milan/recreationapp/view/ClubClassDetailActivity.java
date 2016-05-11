package com.milan.recreationapp.view;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
import com.milan.recreationapp.R;
import com.milan.recreationapp.ReCreationApplication;
import com.milan.recreationapp.model.ClubTimeTable_New;
import com.milan.recreationapp.util.Constant;
import com.twitter.sdk.android.tweetcomposer.TweetComposer;

import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by utsav.k on 06-04-2016.
 */
public class ClubClassDetailActivity extends BaseActivity {

    private TextView txtTime, txtDuration, txtInst, txtClub, txtLocation, txtDesc;
    private Button btnSave;
    private ReCreationApplication reCreationApplication;
    //    private ClubDayTime clubDayTime;
    private ClubTimeTable_New clubDayTime;
    //        private ClubClassDescriptionModel clubClassDescriptionModel;
    private TextView txtLblSaved;
    private ImageView imgFb, imgTwitter;
    private ShareDialog shareDialog;
    private int selectedDay, hour, min,timeBefore;
    private long _eventId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_class_detail);
        shareDialog = new ShareDialog(this);
        txtClub = (TextView) findViewById(R.id.class_detail_txtClub);
        txtDesc = (TextView) findViewById(R.id.class_detail_txtDesc);
        txtDuration = (TextView) findViewById(R.id.class_detail_txtDuration);
        txtInst = (TextView) findViewById(R.id.class_detail_txtInst);
        txtLocation = (TextView) findViewById(R.id.class_detail_txtLocation);
        txtTime = (TextView) findViewById(R.id.class_detail_txtTime);
        btnSave = (Button) findViewById(R.id.class_detail_btnSaveToMyClass);
        txtLblSaved = (TextView) findViewById(R.id.class_detail_txtLblSaved);
        imgFb = (ImageView) findViewById(R.id.class_detail_imgFb);
        imgTwitter = (ImageView) findViewById(R.id.class_detail_imgTwitter);

        txtClub.setText(((ReCreationApplication) getApplication()).sharedPreferences.getString("club", ""));
        reCreationApplication = (ReCreationApplication) getApplication();
        if (this.getIntent().hasExtra("clubdaytime")) {

            clubDayTime = (ClubTimeTable_New) this.getIntent().getSerializableExtra("clubdaytime");
            setUpActionBar(clubDayTime.getClassName());
            txtDuration.setText(clubDayTime.getDuration());
            txtInst.setText(clubDayTime.getInstructor());
            txtTime.setText(clubDayTime.getDay().toString().trim().substring(0, 1).toUpperCase() + clubDayTime.getDay().toString().trim().substring(1) + " " + clubDayTime.getTime());
            txtDesc.setText(clubDayTime.getDesc());
            txtLocation.setText(clubDayTime.getLocation());
            if (clubDayTime.getIsSaved() == 1) {
                btnSave.setVisibility(View.GONE);
                txtLblSaved.setVisibility(View.VISIBLE);
            } else {
                btnSave.setVisibility(View.VISIBLE);
                txtLblSaved.setVisibility(View.GONE);
            }

            btnSave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    reCreationApplication.getDatabase().saveToMyClass(clubDayTime.getId());
                    btnSave.setVisibility(View.GONE);
                    txtLblSaved.setVisibility(View.VISIBLE);
                    //confirmationMessage();
                    //createGymClassApicall();
                    timeBefore = reCreationApplication.sharedPreferences.getInt(getString(R.string.pref_alert_prior),-1);
                    if(timeBefore == -1)
                        confirmationMessage();
                    else
                        saveClassBaseOnAlertPrio();
                }
            });
            if (clubDayTime.getEventId() == 0) {
                txtLblSaved.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        confirmationMessage();
                    }
                });
            }
        }
        imgFb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShareLinkContent content = new ShareLinkContent.Builder()
                        .setContentUrl(Uri.parse("https://developers.facebook.com"))
                        .build();
                if (ShareDialog.canShow(ShareLinkContent.class))
                    shareDialog.show(content);
            }
        });
        imgTwitter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TweetComposer.Builder builder = null;
                try {
                    builder = new TweetComposer.Builder(ClubClassDetailActivity.this).text(getString(R.string.app_name)).url(new URL("https://www.google.com"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                builder.show();
            }
        });

    }

    private void saveClassBaseOnAlertPrio(){
        addEvent();
        createGymClassApicall();
    }

    private void getHourAndMin() {
        try {
            String time = clubDayTime.getTime().toString().trim().substring(0, clubDayTime.getTime().toString().trim().length() - 2).replace(".", ":");
            String ampm = clubDayTime.getTime().toString().trim().substring(clubDayTime.getTime().toString().trim().length() - 2, clubDayTime.getTime().toString().trim().length()).toString().toUpperCase();
            SimpleDateFormat displayFormat = new SimpleDateFormat("HH:mm");
            SimpleDateFormat parseFormat = new SimpleDateFormat("hh:mm a");
            Date date = parseFormat.parse(time + " " + ampm);
            String parsedDate = displayFormat.format(date);
            hour = Integer.parseInt(parsedDate.split(":")[0]);
            min = Integer.parseInt(parsedDate.split(":")[1]);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public void addEvent() {


        if (clubDayTime.getDay().contains("sunday"))
            selectedDay = 1;
        else if (clubDayTime.getDay().contains("monday"))
            selectedDay = 2;
        else if (clubDayTime.getDay().contains("tuesday"))
            selectedDay = 3;
        else if (clubDayTime.getDay().contains("wednesday"))
            selectedDay = 4;
        else if (clubDayTime.getDay().contains("thursday"))
            selectedDay = 5;
        else if (clubDayTime.getDay().contains("friday"))
            selectedDay = 6;
        else if (clubDayTime.getDay().contains("saturday"))
            selectedDay = 7;
        getHourAndMin();

        Calendar calDate = new GregorianCalendar();
        calDate.set(Calendar.DAY_OF_WEEK, selectedDay);
        calDate.set(Calendar.HOUR_OF_DAY, hour);
        calDate.set(Calendar.MINUTE, min);
        calDate.set(Calendar.SECOND, 0);
        calDate.set(Calendar.MILLISECOND, 0);
        Log.e("time milli:", "" + calDate.getTimeInMillis());
        try {
            ContentResolver cr = this.getContentResolver();
            ContentValues values = new ContentValues();
            values.put(CalendarContract.Events.DTSTART, calDate.getTimeInMillis());
            values.put(CalendarContract.Events.DTEND, calDate.getTimeInMillis() + (Integer.parseInt(clubDayTime.getDuration())) * 60 * 1000);
            values.put(CalendarContract.Events.TITLE, clubDayTime.getClassName());
            values.put(CalendarContract.Events.EVENT_LOCATION, clubDayTime.getLocation());
            values.put(CalendarContract.Events.DESCRIPTION, clubDayTime.getDesc());
            values.put(CalendarContract.Events.CALENDAR_ID, 1);
            values.put(CalendarContract.Events.RRULE, "FREQ=WEEKLY");
            values.put(CalendarContract.Events.EVENT_TIMEZONE, Calendar.getInstance()
                    .getTimeZone().getID());
            System.out.println(Calendar.getInstance().getTimeZone().getID());
            Uri uri = cr.insert(CalendarContract.Events.CONTENT_URI, values);

            // Save the eventId into the Task object for possible future delete.

            _eventId = Long.parseLong(uri.getLastPathSegment());
            // Add a 5 minute, 1 hour and 1 day reminders (3 reminders)
            setReminder(cr, _eventId, timeBefore);

            ((ReCreationApplication) getApplication()).getDatabase().saveEventId(clubDayTime.getId(), _eventId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setReminder(ContentResolver cr, long eventID, int timeBefore) {
        try {
            ContentValues values = new ContentValues();
            values.put(CalendarContract.Reminders.MINUTES, timeBefore);
            values.put(CalendarContract.Reminders.EVENT_ID, eventID);
            values.put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT);
            Uri uri = cr.insert(CalendarContract.Reminders.CONTENT_URI, values);
//            Cursor c = CalendarContract.Reminders.query(cr, eventID,
//                    new String[]{CalendarContract.Reminders.MINUTES});
//            if (c.moveToFirst()) {
//                System.out.println("calendar"
//                        + c.getInt(c.getColumnIndex(CalendarContract.Reminders.MINUTES)));
//            }
//            c.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateUserApicall(final boolean hasAllowedAccessToCalendar, final boolean hasAllowedNotifications) {
        // final ProgressDialog pd = ProgressDialog.show(AlertClassActivity.this, "", "Please wait", false, false);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Constant.updateRecreationUser,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        Log.e("update user", "" + response);
                        //Toast.makeText(ClubTimeTableActivity.this,"successfully call", Toast.LENGTH_LONG).show();
                        //finish();
                    }
                },


                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {


                        error.printStackTrace();

                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();

                params.put("id", reCreationApplication.sharedPreferences.getString("userguid",""));
                params.put("fullName", reCreationApplication.sharedPreferences.getString("fullname",""));
                params.put("selectedClubName", reCreationApplication.sharedPreferences.getString("club",""));
                params.put("clubsFilter", reCreationApplication.sharedPreferences.getString("clubsFilter",""));
                params.put("hasAllowedAccessToCalendar", hasAllowedAccessToCalendar+"");
                params.put("hasAllowedNotifications", hasAllowedNotifications+"");

                Log.e("update user req param:", "" + params.toString());
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

    private void createGymClassApicall() {
        final ProgressDialog pd = ProgressDialog.show(ClubClassDetailActivity.this, "", "Please wait", false, false);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Constant.saveMyClassUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (pd != null && pd.isShowing())
                            pd.dismiss();


                        if(timeBefore != -1){
                            updateUserApicall(true,true);
                        }else{
                            updateUserApicall(false,false);
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
                String id = UUID.randomUUID().toString();
                params.put("id", id);
                params.put("userId", reCreationApplication.sharedPreferences.getString("userguid", ""));
                params.put("name", clubDayTime.getClassName());
                params.put("clubName", reCreationApplication.sharedPreferences.getString("club", ""));
                params.put("duration", clubDayTime.getDuration());
                params.put("location", clubDayTime.getLocation());
                params.put("instructor", clubDayTime.getInstructor());
                params.put("gymClassDescription", clubDayTime.getDesc());
                params.put("dayString", clubDayTime.getDay().substring(0,1).toUpperCase()+clubDayTime.getDay().substring(1,clubDayTime.getDay().length()));
                if(timeBefore != -1){
                    params.put("alertPrior", timeBefore+"");
                    params.put("calendarAlertEventIdentifier", _eventId+"");
                }


                Log.e("create class param:", "" + params.toString());

                reCreationApplication.getDatabase().saveToMyClassWithClassId(clubDayTime.getId(),id);

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

    private void confirmationMessage() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Are you sure?");
        builder.setMessage("Successfully added to My Classes. Would you like to be alerted before this class starts?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                Intent iAlert = new Intent(ClubClassDetailActivity.this, AlertClassActivity.class);
                iAlert.putExtra("clubalert", clubDayTime);
                startActivity(iAlert);
            }
        });
        builder.setNegativeButton("No thanks", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                createGymClassApicall();
                dialog.dismiss();
            }
        });
        builder.show();
    }

}
