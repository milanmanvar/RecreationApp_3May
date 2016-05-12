package com.milan.recreationapp.view;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.milan.recreationapp.R;
import com.milan.recreationapp.ReCreationApplication;
import com.milan.recreationapp.adapter.ClubLocationListAdapter;
import com.milan.recreationapp.model.ClubModel_New;

import java.util.ArrayList;

/**
 * Created by utsav.k on 04-04-2016.
 */
public class OurClubActivity extends BaseActivity {
    // Google Map
    private GoogleMap googleMap;
    private ArrayList<ClubModel_New> clubModels;
    private SharedPreferences appPreferences;
    private ListView listLocations;

    private ArrayList<Marker> markerClick = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ourclub);
        appPreferences = ((ReCreationApplication) getApplication()).sharedPreferences;
        setUpActionBar("Our Clubs");
        findViewById(R.id.actionbar_layout_iv_myclass).setVisibility(View.GONE);
        clubModels = (((ReCreationApplication) getApplication()).getDatabase().getClubListWithShowAll());
        listLocations = (ListView) findViewById(R.id.listLocation);
        listLocations.setAdapter(new ClubLocationListAdapter(this, clubModels));
        try {
            initilizeMap();
        } catch (Exception e) {
            e.printStackTrace();
        }



    }

    /**
     * function to load map. If map is not created it will create it for you
     */
    private void initilizeMap() {
        if (googleMap == null) {
            googleMap = ((MapFragment) getFragmentManager().findFragmentById(
                    R.id.map)).getMap();

            // check if map is created successfully or not
            if (googleMap == null) {
                Toast.makeText(getApplicationContext(),
                        "Sorry! unable to create maps", Toast.LENGTH_SHORT)
                        .show();
            }
            googleMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                @Override
                public void onMapLoaded() {
                    // create marker
                    LatLngBounds.Builder builder = new LatLngBounds.Builder();

                    for (ClubModel_New c : clubModels) {
                        MarkerOptions marker = new MarkerOptions().position(new LatLng(c.getLat(), c.getLng())).title(c.getName()).snippet(c.getAddress());


                        //marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE));
                        marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker));
                        builder.include(marker.getPosition());
                        if (c.getId() != -1){
                            Marker marker1 = googleMap.addMarker(marker);

                            markerClick.add(marker1);
                        }


                    }
//                    LatLngBounds bounds = builder.build();
//
//                    CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 0);
//                    googleMap.animateCamera(cu);




                    CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(clubModels.get(0).getLat(), clubModels.get(0).getLng())).zoom(7).build();
                    googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));



                }
            });


            googleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                @Override
                public View getInfoWindow(Marker marker) {
                    return null;
                }

                @Override
                public View getInfoContents(Marker marker) {

                    View v = getLayoutInflater().inflate(R.layout.row_map_info_window, null);
                    TextView tvClubName = (TextView) v.findViewById(R.id.club_name);
                    TextView tvclubAddress = (TextView) v.findViewById(R.id.club_address);

                    tvClubName.setText(marker.getTitle());
                    tvclubAddress.setText(marker.getSnippet());


//                    tvleft.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//
//                        }
//                    });

                    // Getting the position from the marker
                    LatLng latLng = marker.getPosition();


                    return v;
                }
            });


            googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                @Override
                public void onInfoWindowClick(Marker marker) {

                    int position = markerClick.indexOf(marker);

                    //Toast.makeText(OurClubActivity.this,"test",Toast.LENGTH_LONG).show();
                    Intent iDetail = new Intent(OurClubActivity.this, ClubInfoActivity.class);
                    iDetail.putExtra("clubdata", clubModels.get(position));
                    startActivity(iDetail);
                }
            });

        }
    }

    public void changeLocation(double lat, double lng, String name,int position) {
        Marker marker = markerClick.get(position);
        marker.showInfoWindow();
        CameraPosition cameraPosition = new CameraPosition.Builder().target(marker.getPosition()).zoom(12).build();
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    public void mapZoomOut() {

        CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(clubModels.get(0).getLat(), clubModels.get(0).getLng())).zoom(7).build();
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }


}
