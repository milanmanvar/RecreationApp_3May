package com.recreation.recreationapp.view;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.recreation.recreationapp.R;
import com.recreation.recreationapp.ReCreationApplication;
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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * Created by milanmanvar on 16/03/16.
 */
public class SplashActivity extends Activity {

    private ReCreationApplication application;
    private ArrayList<ClubModel> clubList;
    public static ArrayList<String> filledClub = new ArrayList<>();
    //public static int totalClub;
    public static boolean isLoading = true;

    private int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        application = (ReCreationApplication) this.getApplication();


        try {
            PackageInfo info = getPackageManager().getPackageInfo("com.recreation.recreationapp",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();

        } catch (NoSuchAlgorithmException e) {

        }

        //showAnimation();

        downloadClubList();

    }

    private void downloadClubList() {
        //  final ProgressDialog pd = ProgressDialog.show(WelcomeScreen.this, "", "Please wait", false, false);
        application.getDatabase().deleteTableData();
        isLoading = true;
        filledClub.clear();
        StringRequest reqDownloadClub = new StringRequest(Constant.clubUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e("Response:", "" + response);
                SharedPreferences.Editor editor = ((ReCreationApplication) getApplication()).sharedPreferences.edit();
                editor.putString("clublist", response);
                editor.commit();
                parseClubListXML(response);



                if (application.sharedPreferences.getString("club", "").equalsIgnoreCase("")) {
                    startActivity(new Intent(SplashActivity.this, WelcomeScreen.class));
                } else {

                    try {
                        callClubsDataApi();
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }

                    startActivity(new Intent(SplashActivity.this, HomeScreen.class));
                }
                finish();

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

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
            JSONArray jsClub = new JSONArray();
            for (ClubModel model : clubList) {
                ClubModel clubModel = model;
                for (ClubSection section : clubModel.getClubSection()) {
                    ClubSection clubSection = section;
                    for (ClubSectionBody body : clubSection.getClubSectionBodies()) {
                        ClubSectionBody clubSectionBody = body;
                        application.getDatabase().insertClub(clubModel.getName(), clubModel.getAddress(), clubModel.getPhone(), clubModel.getLat(), clubModel.getLng(), clubSection.getTitle(), clubSectionBody.getDays(), clubSectionBody.getHours(), clubModel.is24Hour());
                    }
                }
//                callAllClubsDataApi(model.getName(), i + 1);
                //clubPopUp.getMenu().add(i, i + 1, i + 1, model.getName());
                //clubs.add(model.getName());
                jsClub.put(model.getName());
                i = i + 1;
            }
            Log.w("AndroidParseXMLActivity", "Done");

            if(application.sharedPreferences.getString("club", "").length()>0){

                int position = 0;
                for(int j= 0;j<clubList.size();j++ ){
                    if(clubList.get(j).getName().equals(application.sharedPreferences.getString("club", ""))){
                        position = j;
                        break;
                    }
                }

                clubList.add(0,clubList.get(position));
                clubList.remove(position+1);

            }

            SharedPreferences.Editor e = application.sharedPreferences.edit();
            e.putString("clubsFilter", jsClub.toString());
            e.commit();

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
                        application.getDatabase().insertClubData(Utils.getClubName(), clubDayTime.getClassName(), clubDayTime.getInstructorName(), clubDayTime.getClassDuration(), clubDayTime.getClassTime(), clubTimeTable.getDay(), "morning", clubClassDescriptionModel.getDescription(), clubClassDescriptionModel.getLocation());
                        application.getDatabase().insertOrReplaceMyClubData(Utils.getClubName(), clubDayTime.getClassName(), clubDayTime.getInstructorName(), clubDayTime.getClassDuration(), clubDayTime.getClassTime(), clubTimeTable.getDay(), "morning", clubClassDescriptionModel.getDescription(), clubClassDescriptionModel.getLocation());
                    }
                    for (ClubDayTime dayTime : clubTimeTable.getLunchtimeClasses()) {
                        ClubDayTime clubDayTime = dayTime;
                        ClubClassDescriptionModel clubClassDescriptionModel = Utils.getClubClassDescriptionModelArrayList().get(clubDayTime.getClassName());
                        application.getDatabase().insertClubData(Utils.getClubName(), clubDayTime.getClassName(), clubDayTime.getInstructorName(), clubDayTime.getClassDuration(), clubDayTime.getClassTime(), clubTimeTable.getDay(), "lunchtime", clubClassDescriptionModel.getDescription(), clubClassDescriptionModel.getLocation());
                        application.getDatabase().insertOrReplaceMyClubData(Utils.getClubName(), clubDayTime.getClassName(), clubDayTime.getInstructorName(), clubDayTime.getClassDuration(), clubDayTime.getClassTime(), clubTimeTable.getDay(), "lunchtime", clubClassDescriptionModel.getDescription(), clubClassDescriptionModel.getLocation());
                    }
                    for (ClubDayTime dayTime : clubTimeTable.getEveningClasses()) {
                        ClubDayTime clubDayTime = dayTime;
                        ClubClassDescriptionModel clubClassDescriptionModel = Utils.getClubClassDescriptionModelArrayList().get(clubDayTime.getClassName());
                        application.getDatabase().insertClubData(Utils.getClubName(), clubDayTime.getClassName(), clubDayTime.getInstructorName(), clubDayTime.getClassDuration(), clubDayTime.getClassTime(), clubTimeTable.getDay(), "evening", clubClassDescriptionModel.getDescription(), clubClassDescriptionModel.getLocation());
                        application.getDatabase().insertOrReplaceMyClubData(Utils.getClubName(), clubDayTime.getClassName(), clubDayTime.getInstructorName(), clubDayTime.getClassDuration(), clubDayTime.getClassTime(), clubTimeTable.getDay(), "evening", clubClassDescriptionModel.getDescription(), clubClassDescriptionModel.getLocation());
                    }
                }


                if(i==clubList.size()-1)
                    isLoading = false;
                filledClub.add(clubList.get(i).getName());
                sendBroadcast(new Intent("com.recreation.recreationapp.action"));

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //application.getDatabase().insertDataOfMyClubDetail();



    }
    private void showAnimation() {
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {

                if (application.sharedPreferences.getString("club", "").equalsIgnoreCase("")) {
                    startActivity(new Intent(SplashActivity.this, WelcomeScreen.class));
                } else {
                    startActivity(new Intent(SplashActivity.this, HomeScreen.class));
                }
                finish();

            }
        }, 2000);
    }


    /*private class AsyncSplashLoader extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            finish();
            if (((ReCreationApplication) getApplication()).sharedPreferences.getString("club", "").equalsIgnoreCase("")) {
                startActivity(new Intent(SplashActivity.this, WelcomeScreen.class));
            } else {
                startActivity(new Intent(SplashActivity.this, HomeScreen.class));

            }
        }
    }*/

}
