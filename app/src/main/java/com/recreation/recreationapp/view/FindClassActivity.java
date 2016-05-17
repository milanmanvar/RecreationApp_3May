package com.recreation.recreationapp.view;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.recreation.recreationapp.R;
import com.recreation.recreationapp.ReCreationApplication;
import com.recreation.recreationapp.model.ClubModel_New;
import com.recreation.recreationapp.model.ClubTimeTable_New;
import com.recreation.recreationapp.util.Utils;

import java.util.ArrayList;

/**
 * Created by utsav.k on 07-04-2016.
 */
public class FindClassActivity extends BaseActivity {

    private static final int REQ_INCLUDE_WITH = 100;
    private static final int REQ_SEARCH_WITH = 200;
    private TextView txtSearchingWith, txtIncludingWith;
    private EditText etSearch;
    private ArrayList<ClubTimeTable_New> list;
    private LinearLayout lMorning, lLunch, lEvening;
    private String selectedClubs, selectedClass;
    private SharedPreferences appSharedPreferences;
    ReCreationApplication reCreationApplication;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_class);
        setUpActionBar("Find Class");
        appSharedPreferences = ((ReCreationApplication) getApplication()).sharedPreferences;
        txtIncludingWith = (TextView) findViewById(R.id.find_class_txtIncludingWith);
        txtSearchingWith = (TextView) findViewById(R.id.find_class_txtSearchingWithClubName);
        etSearch = (EditText) findViewById(R.id.find_class_etSearch);
        lMorning = (LinearLayout) findViewById(R.id.find_class_lMorningSearchResult);
        lLunch = (LinearLayout) findViewById(R.id.find_class_lLunchSearchResult);
        lEvening = (LinearLayout) findViewById(R.id.find_class_lEveningSearchResult);

//        etSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
//            @Override
//            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
//                if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_SEARCH) {
//
//                    setData();
//                    return true;
//                }
//                return false;
//            }
//        });
        txtIncludingWith.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent iInclude = new Intent(FindClassActivity.this, IncludingWithSearchActivity.class);
                startActivityForResult(iInclude, REQ_INCLUDE_WITH);
            }
        });
        txtSearchingWith.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent iInclude = new Intent(FindClassActivity.this, SearchingWithClubActivity.class);
                startActivityForResult(iInclude, REQ_SEARCH_WITH);
            }
        });

        //txtIncludingWith.setText(getString(R.string.include_with, appSharedPreferences.getString("selectedtype", "morning,lunchtime and evening")));
        //txtSearchingWith.setText(getString(R.string.searching_with, appSharedPreferences.getString("selectedclubs", appSharedPreferences.getString("club", ""))));
        selectedClass = appSharedPreferences.getString("selectedtype", "morning,lunchtime,evening");
//        selectedClubs = appSharedPreferences.getString("selectedclubs", appSharedPreferences.getString("club", ""));

        displaySessionText();

         reCreationApplication= (ReCreationApplication) getApplicationContext();

        final ArrayList<ClubModel_New> clubs = reCreationApplication.getDatabase().getClubList();
        ArrayList<String> selectedClub = new ArrayList<>();
        for (int i=0;i<clubs.size();i++)
            selectedClub.add(clubs.get(i).getName());
        String selected = TextUtils.join(",", selectedClub);


//        if(appSharedPreferences.getString("selectedclubs", "").length()==0){
//            txtSearchingWith.setText(getString(R.string.searching_with,"all clubs"));
//            selectedClubs = appSharedPreferences.getString("selectedclubs", selected);
//        }else{
//
//
//
//            selectedClubs = appSharedPreferences.getString("selectedclubs", selected);
//            txtSearchingWith.setText(getString(R.string.searching_with,selectedClubs));
//        }

        selectedClubs = appSharedPreferences.getString("selectedclubs", selected);
        displaySelectedClubText();


        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                setData();
            }
        });
    }

    private void setData() {
        lMorning.removeAllViews();
        lLunch.removeAllViews();
        lEvening.removeAllViews();
        if (!etSearch.getText().toString().trim().equalsIgnoreCase("")) {
            list = ((ReCreationApplication) getApplication()).getDatabase().getClubTimeTableLikeName(etSearch.getText().toString().trim(), selectedClubs, selectedClass);
            String tempM = "", tempL = "", tempE = "";
            if (list != null && list.size() > 0) {
                for (int i = 0; i < list.size(); i++) {
                    final int finalI = i;
                    if (list.get(i).getClassType().contains("morning")) {
                        View view = getLayoutInflater().inflate(R.layout.row_timetable_title, null);
                        TextView txtTitle = (TextView) view.findViewById(R.id.row_timetable_txtTitle);
                        txtTitle.setText("Morning Classes");

                        if (!tempM.equalsIgnoreCase(txtTitle.getText().toString().trim()))
                            lMorning.addView(view);
                        tempM = txtTitle.getText().toString().trim();

                        View vBody = getLayoutInflater().inflate(R.layout.row_timetable, null);
                        TextView txtDay = (TextView) vBody.findViewById(R.id.row_timetable_txtName);
                        ImageView imgCheck = (ImageView) vBody.findViewById(R.id.row_timetable_imgCheck);
                        imgCheck.setVisibility(list.get(i).getIsSaved() == 1 ? View.VISIBLE : View.INVISIBLE);
                        txtDay.setText(list.get(i).getTime() + " " + list.get(i).getClassName() + " @ " + list.get(i).getLocation());

                        vBody.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent iClass = new Intent(FindClassActivity.this, ClubClassDetailActivity.class);
                                iClass.putExtra("clubdaytime", list.get(finalI));
                                startActivity(iClass);
                            }
                        });
                        lMorning.addView(vBody);
                    } else if (list.get(i).getClassType().contains("lunchtime")) {
                        View view = getLayoutInflater().inflate(R.layout.row_timetable_title, null);
                        TextView txtTitle = (TextView) view.findViewById(R.id.row_timetable_txtTitle);
                        txtTitle.setText("Lunchtime Classes");

                        if (!tempL.equalsIgnoreCase(txtTitle.getText().toString().trim()))
                            lLunch.addView(view);
                        tempL = txtTitle.getText().toString().trim();

                        View vBody = getLayoutInflater().inflate(R.layout.row_timetable, null);
                        TextView txtDay = (TextView) vBody.findViewById(R.id.row_timetable_txtName);
                        txtDay.setText(list.get(i).getTime() + " " + list.get(i).getClassName() + " @ " + list.get(i).getLocation());
                        ImageView imgCheck = (ImageView) vBody.findViewById(R.id.row_timetable_imgCheck);
                        imgCheck.setVisibility(list.get(i).getIsSaved() == 1 ? View.VISIBLE : View.INVISIBLE);
                        vBody.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                Intent iClass = new Intent(FindClassActivity.this, ClubClassDetailActivity.class);
                                iClass.putExtra("clubdaytime", list.get(finalI));
                                startActivity(iClass);
                            }
                        });
                        lLunch.addView(vBody);
                    } else {
                        View view = getLayoutInflater().inflate(R.layout.row_timetable_title, null);
                        TextView txtTitle = (TextView) view.findViewById(R.id.row_timetable_txtTitle);
                        txtTitle.setText("Evening Classes");

                        if (!tempE.equalsIgnoreCase(txtTitle.getText().toString().trim()))
                            lEvening.addView(view);
                        tempE = txtTitle.getText().toString().trim();

                        View vBody = getLayoutInflater().inflate(R.layout.row_timetable, null);
                        TextView txtDay = (TextView) vBody.findViewById(R.id.row_timetable_txtName);
                        txtDay.setText(list.get(i).getTime() + " " + list.get(i).getClassName() + " @ " + list.get(i).getLocation());
                        ImageView imgCheck = (ImageView) vBody.findViewById(R.id.row_timetable_imgCheck);
                        imgCheck.setVisibility(list.get(i).getIsSaved() == 1 ? View.VISIBLE : View.INVISIBLE);
                        vBody.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent iClass = new Intent(FindClassActivity.this, ClubClassDetailActivity.class);
                                iClass.putExtra("clubdaytime", list.get(finalI));
                                startActivity(iClass);
                            }
                        });
                        lEvening.addView(vBody);
                    }
                }
            } else
                Utils.displayDialog("Whoops!", "No result found", FindClassActivity.this);
        }
    }


    private void displaySelectedClubText(){

        final ArrayList<ClubModel_New> clubsArray = reCreationApplication.getDatabase().getClubList();

        if (selectedClubs.contains(",")) {
            String[] clubs = selectedClubs.split(",");

            if(clubs.length == clubsArray.size()){
                txtSearchingWith.setText(getString(R.string.searching_with,"all clubs"));
            }
            else if (clubs.length > 3) {
                txtSearchingWith.setText(getString(R.string.searching_with, clubs[0] + ", " + clubs[1] + ", " + clubs[2] + " +" + String.valueOf(clubs.length - 3)+" more"));
            } else if(clubs.length == 3){
                txtSearchingWith.setText(getString(R.string.searching_with, clubs[0] + ", " + clubs[1] + ", " + clubs[2]));
            }else if(clubs.length == 2){
                txtSearchingWith.setText(getString(R.string.searching_with, clubs[0] + ", " + clubs[1]));
            }else{
                txtSearchingWith.setText(getString(R.string.searching_with, selectedClubs));
            }
        } else
            txtSearchingWith.setText(getString(R.string.searching_with, selectedClubs));

    }

    private void displaySessionText(){

        String displayText="";
        String[] sessionArray = selectedClass.split(",");
        if(sessionArray.length == 3 || sessionArray.length == 2){
            for(int i=0;i<sessionArray.length;i++){
                if(i==sessionArray.length-1){
                    displayText = displayText + " and "+ sessionArray[i];
                }else if(i==sessionArray.length-2){
                    displayText = displayText + sessionArray[i];
                }
                else {
                    displayText = displayText + sessionArray[i]+", ";
                }
            }
        }else{

            displayText = displayText +" only " + sessionArray[0];
        }


        txtIncludingWith.setText(getString(R.string.include_with, displayText));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == REQ_INCLUDE_WITH) {
                selectedClass = data.getStringExtra("selectedtype");
                Log.e("Selected type:", "" + selectedClass);
            } else if (requestCode == REQ_SEARCH_WITH) {
                selectedClubs = data.getStringExtra("selectedclubs");
                Log.e("Selected type:", "" + selectedClubs);
            }
        }


        displaySessionText();
        displaySelectedClubText();


        super.onActivityResult(requestCode, resultCode, data);
    }
}
