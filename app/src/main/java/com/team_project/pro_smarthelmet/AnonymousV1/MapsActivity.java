package com.team_project.pro_smarthelmet.AnonymousV1;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.team_project.pro_smarthelmet.R;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    //map vars
    Marker marker;
    private GoogleMap mMap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        //get a variable "user_uid" from PhoneAuthActivity
        Bundle extras = getIntent().getExtras();
        assert extras != null;
        String user_uidS = extras.getString("user_uid");

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //get user latitude, longitude and fullname from the firebase
        assert user_uidS != null;
        DatabaseReference userReference = FirebaseDatabase.getInstance().getReference().child("Users").child(user_uidS);
        userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                double latitude = (double) dataSnapshot.child("user_location_latitude").getValue();
                double longitude = (double) dataSnapshot.child("user_location_longitude").getValue();
                String name = (String) dataSnapshot.child("full_name").getValue();

                //mover the marker every time the data changes
                markerMove(latitude, longitude, name);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    //marker move
    //if their is a maker >> remove maker and make a new one
    //if not > make a new marker
    private void markerMove(double latitude, double longitude, String name) {
        LatLng latLng = new LatLng(latitude, longitude);
        if (marker != null) {
            marker.remove();
            marker = mMap.addMarker(new MarkerOptions().position(latLng).title(name));
            mMap.setMaxZoomPreference(20);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12.0f));
        } else {
            marker = mMap.addMarker(new MarkerOptions().position(latLng).title(name));
            mMap.setMaxZoomPreference(20);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 21.0f));
        }
    }

    //set mMap when the map is ready
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }
}
