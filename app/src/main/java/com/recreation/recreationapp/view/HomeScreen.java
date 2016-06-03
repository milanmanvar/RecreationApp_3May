package com.recreation.recreationapp.view;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.recreation.recreationapp.R;
import com.recreation.recreationapp.ReCreationApplication;
import com.recreation.recreationapp.database.DBHelper;
import com.recreation.recreationapp.model.ClubClassDescriptionModel;
import com.recreation.recreationapp.model.ClubDayTime;
import com.recreation.recreationapp.model.ClubModel;
import com.recreation.recreationapp.model.ClubSection;
import com.recreation.recreationapp.model.ClubSectionBody;
import com.recreation.recreationapp.model.ClubTimeTable;
import com.recreation.recreationapp.util.Constant;
import com.recreation.recreationapp.util.Utils;
import com.recreation.recreationapp.xml.ItemXMLHandler;

import org.json.JSONArray;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * Created by milanmanvar on 18/03/16.
 */
public class HomeScreen extends Activity {


    private TextView txtClubName;
    ProgressDialog pd;
    private int count = 0;
    private ReCreationApplication application;
    private ArrayList<ClubModel> clubList;
    private JSONArray jsClub;
    private DBHelper myDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homescreen);
        txtClubName = (TextView) findViewById(R.id.home_clubName);


        application = (ReCreationApplication) this.getApplication();
        myDbHelper = application.getDatabase();
        myDbHelper.deleteTableData();
        pd = ProgressDialog.show(HomeScreen.this, "", "Please wait", false, false);
        downloadClubData();

    }

    private void downloadClubData() {
        //  final ProgressDialog pd = ProgressDialog.show(WelcomeScreen.this, "", "Please wait", false, false);
        StringRequest reqDownloadClub = new StringRequest(Constant.clubUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e("Response:", "" + response);
                SharedPreferences.Editor editor = ((ReCreationApplication) getApplication()).sharedPreferences.edit();
                editor.putString("clublist", response);
                editor.commit();
                parseClubListXML(response);
//                if (pd != null && pd.isShowing())
//                    pd.dismiss();
                try {
                    callClubsDataApi();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (pd != null && pd.isShowing())
                    pd.dismiss();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> hashMap = new HashMap<String, String>();
                hashMap.put("Content-Type", "application/xml; charset=utf-8");

                return hashMap;
            }
        };
        application.addToRequestQueue(reqDownloadClub);
    }

    private void parseClubListXML(String xml) {

        String parsedData = "";

        try {
            /** Handling XML */
            SAXParserFactory spf = SAXParserFactory.newInstance();
            SAXParser sp = spf.newSAXParser();
            XMLReader xr = sp.getXMLReader();

            ItemXMLHandler myXMLHandler = new ItemXMLHandler();
            xr.setContentHandler(myXMLHandler);
            InputSource inStream = new InputSource();

            inStream.setCharacterStream(new StringReader(xml));

            xr.parse(inStream);

            clubList = myXMLHandler.getClubList();
            int i = 0;
            //clubs = new ArrayList<>();
            jsClub = new JSONArray();
            for (ClubModel model : clubList) {
                ClubModel clubModel = model;
                for (ClubSection section : clubModel.getClubSection()) {
                    ClubSection clubSection = section;
                    for (ClubSectionBody body : clubSection.getClubSectionBodies()) {
                        ClubSectionBody clubSectionBody = body;
                        myDbHelper.insertClub(clubModel.getName(), clubModel.getAddress(), clubModel.getPhone(), clubModel.getLat(), clubModel.getLng(), clubSection.getTitle(), clubSectionBody.getDays(), clubSectionBody.getHours(), clubModel.is24Hour());
                    }
                }
//                callAllClubsDataApi(model.getName(), i + 1);
                //clubPopUp.getMenu().add(i, i + 1, i + 1, model.getName());
                //clubs.add(model.getName());
                jsClub.put(model.getName());
                i = i + 1;
            }
            Log.w("AndroidParseXMLActivity", "Done");
        } catch (Exception e) {
            Log.w("AndroidParseXMLActivity", e);
        }


    }



    private void callClubsDataApi() throws UnsupportedEncodingException {
//        final ProgressDialog pd = ProgressDialog.show(WelcomeScreen.this, "", "Please wait", false, false);
//        for (int i = 0; i < clubList.size(); i++) {
        String url = Constant.clubDataUrl + clubList.get(count).getName().replaceAll(" ", "%20") + ".xml";
        final int finalI = count;
        StringRequest reqDownloadClub = new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(final String response) {
                SharedPreferences.Editor editor = application.sharedPreferences.edit();
                editor.putString("club" + finalI, response);
                editor.commit();
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                try {
//                    ArrayList<ClubTimeTable> clubTimeTables = Utils.parseClubDetailXML(response);
//                    for (ClubTimeTable timeTable : clubTimeTables) {
//                        ClubTimeTable clubTimeTable = timeTable;
//                        for (ClubDayTime dayTime : clubTimeTable.getMorningClasses()) {
//                            ClubDayTime clubDayTime = dayTime;
//                            ClubClassDescriptionModel clubClassDescriptionModel = Utils.getClubClassDescriptionModelArrayList().get(clubDayTime.getClassName());
//                            myDbHelper.insertClubData(Utils.getClubName(), clubDayTime.getClassName(), clubDayTime.getInstructorName(), clubDayTime.getClassDuration(), clubDayTime.getClassTime(), clubTimeTable.getDay(), "morning", clubClassDescriptionModel.getDescription(), clubClassDescriptionModel.getLocation());
//                        }
//                        for (ClubDayTime dayTime : clubTimeTable.getLunchtimeClasses()) {
//                            ClubDayTime clubDayTime = dayTime;
//                            ClubClassDescriptionModel clubClassDescriptionModel = Utils.getClubClassDescriptionModelArrayList().get(clubDayTime.getClassName());
//                            myDbHelper.insertClubData(Utils.getClubName(), clubDayTime.getClassName(), clubDayTime.getInstructorName(), clubDayTime.getClassDuration(), clubDayTime.getClassTime(), clubTimeTable.getDay(), "lunchtime", clubClassDescriptionModel.getDescription(), clubClassDescriptionModel.getLocation());
//                        }
//                        for (ClubDayTime dayTime : clubTimeTable.getEveningClasses()) {
//                            ClubDayTime clubDayTime = dayTime;
//                            ClubClassDescriptionModel clubClassDescriptionModel = Utils.getClubClassDescriptionModelArrayList().get(clubDayTime.getClassName());
//                            myDbHelper.insertClubData(Utils.getClubName(), clubDayTime.getClassName(), clubDayTime.getInstructorName(), clubDayTime.getClassDuration(), clubDayTime.getClassTime(), clubTimeTable.getDay(), "evening", clubClassDescriptionModel.getDescription(), clubClassDescriptionModel.getLocation());
//                        }
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                        }
//                    });

                if (finalI == clubList.size() - 1) {
                    //if (pd != null && pd.isShowing())
                    //    pd.dismiss();
                    new AsyncStoreDataOnDb().execute();
                } else {
                    count = count + 1;
                    try {
                        callClubsDataApi();
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("error:", "" + error);
                if (finalI == clubList.size() - 1) {
                    if (pd != null && pd.isShowing())
                        pd.dismiss();
                } else {
                    count = count + 1;
                    try {
                        callClubsDataApi();
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }

            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> hashMap = new HashMap<String, String>();
                hashMap.put("Content-Type", "application/xml; charset=utf-8");
                return hashMap;
            }
        };
        application.addToRequestQueue(reqDownloadClub);
//        }

    }

    private class AsyncStoreDataOnDb extends AsyncTask<Void, Void, Void> {


        // ProgressDialog pd;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // pd = ProgressDialog.show(WelcomeScreen.this, "", "Please wait", false);
        }

        @Override
        protected Void doInBackground(Void... params) {
            insertDetailData();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if (pd != null && pd.isShowing())
                pd.dismiss();
        }
    }

    private void insertDetailData() {

        for (int i = 0; i < clubList.size(); i++) {
            try {
                ArrayList<ClubTimeTable> clubTimeTables = Utils.parseClubDetailXML(application.sharedPreferences.getString(("club" + i), ""));
                for (ClubTimeTable timeTable : clubTimeTables) {
                    ClubTimeTable clubTimeTable = timeTable;
                    for (ClubDayTime dayTime : clubTimeTable.getMorningClasses()) {
                        ClubDayTime clubDayTime = dayTime;
                        ClubClassDescriptionModel clubClassDescriptionModel = Utils.getClubClassDescriptionModelArrayList().get(clubDayTime.getClassName());
                        myDbHelper.insertClubData(Utils.getClubName(), clubDayTime.getClassName(), clubDayTime.getInstructorName(), clubDayTime.getClassDuration(), clubDayTime.getClassTime(), clubTimeTable.getDay(), "morning", clubClassDescriptionModel.getDescription(), clubClassDescriptionModel.getLocation());
                    }
                    for (ClubDayTime dayTime : clubTimeTable.getLunchtimeClasses()) {
                        ClubDayTime clubDayTime = dayTime;
                        ClubClassDescriptionModel clubClassDescriptionModel = Utils.getClubClassDescriptionModelArrayList().get(clubDayTime.getClassName());
                        myDbHelper.insertClubData(Utils.getClubName(), clubDayTime.getClassName(), clubDayTime.getInstructorName(), clubDayTime.getClassDuration(), clubDayTime.getClassTime(), clubTimeTable.getDay(), "lunchtime", clubClassDescriptionModel.getDescription(), clubClassDescriptionModel.getLocation());
                    }
                    for (ClubDayTime dayTime : clubTimeTable.getEveningClasses()) {
                        ClubDayTime clubDayTime = dayTime;
                        ClubClassDescriptionModel clubClassDescriptionModel = Utils.getClubClassDescriptionModelArrayList().get(clubDayTime.getClassName());
                        myDbHelper.insertClubData(Utils.getClubName(), clubDayTime.getClassName(), clubDayTime.getInstructorName(), clubDayTime.getClassDuration(), clubDayTime.getClassTime(), clubTimeTable.getDay(), "evening", clubClassDescriptionModel.getDescription(), clubClassDescriptionModel.getLocation());
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        txtClubName.setText(((ReCreationApplication) getApplication()).sharedPreferences.getString("club", "").toUpperCase() + "\n TIMETABLE");
    }

    public void onHomeMenuClick(View v) {
        switch (v.getId()) {
            case R.id.lTimeTable:
                Intent intent = new Intent(HomeScreen.this, ClubTimeTableActivity.class);
                startActivity(intent);
                break;
            case R.id.lMyClass:
                Intent iSaved = new Intent(HomeScreen.this, SavedClassActivity.class);
                startActivity(iSaved);
                break;
            case R.id.lOurClub:
                Intent iClub = new Intent(HomeScreen.this, OurClubActivity.class);
                startActivity(iClub);
                break;
            case R.id.lFindClass:
                Intent iFind = new Intent(HomeScreen.this, FindClassActivity.class);
                startActivity(iFind);
                break;
        }
    }

}
