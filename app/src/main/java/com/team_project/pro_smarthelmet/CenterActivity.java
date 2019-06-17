package com.team_project.pro_smarthelmet;


import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.team_project.pro_smarthelmet.AnonymousV1.PhoneAuthActivity;
import com.team_project.pro_smarthelmet.BluetoothV1.Select;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;


public class CenterActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    //permission vars
    static final int PICK_CONTACT_REQUEST = 99;
    static final int PERMISSION_ALL = 1;

    //current time var
    Date currentTime = Calendar.getInstance().getTime();

    //location vars
    LocationManager locationManager;
    LocationListener locationListener;

    //vars firebase
    FirebaseUser currentUser;
    DatabaseReference userReference;
    FirebaseAuth mAuth;
    String users_Id;

    //vars Navigator
    NavigationView navigationView;
    private CircleImageView NavProfileImage;
    private TextView NavProfileUserName, NavProfileEmail;
    private DrawerLayout drawer;

    //toolbar
    Toolbar toolbar;

    //contacts XML vars
    TextView friendName1, friendName2, friendName3, friendNumber1, friendNumber2, friendNumber3;

    //Action menu vars
    private FloatingActionMenu floatingActionMenu;
    private FloatingActionButton floatingActionButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_center);

        initUi(); // more code in this method

        //when the floatingActionButton "add friend button is pressed"
        floatingActionMenu.setVisibility(View.VISIBLE);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("IntentReset")
            @Override
            public void onClick(android.view.View view) {
                //select contacts from phone
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_PICK);
                sendIntent.setData(Uri.parse("content://contacts/people/"));
                sendIntent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
                startActivityForResult(sendIntent, PICK_CONTACT_REQUEST);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // also select contacts from phone
        if (requestCode == PICK_CONTACT_REQUEST) {
            if (resultCode == RESULT_OK) {
                Uri contactUri = data.getData();

                assert contactUri != null;
                @SuppressLint("Recycle") Cursor cursor = getContentResolver().query(contactUri, null, null, null, null);
                assert cursor != null;
                cursor.moveToFirst();

                int column = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                final String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                String number = cursor.getString(column);

                //replace +2, - and space
                number = number.replaceAll("\\s", "").replaceAll("\\+2", "").replaceAll("-", "");

                savingDataUsers(number, name);
            }
        }
    }

    private void savingDataUsers(final String getnumber, final String getname) {
        //get the names
        final String name1 = friendName1.getText().toString();
        final String name2 = friendName2.getText().toString();
        final String name3 = friendName3.getText().toString();

        //see which name is empty
        //and store the new number and name in the firebase
        if (TextUtils.isEmpty(name1)) {
            userReference.child("relative1_name").setValue(getname);
            userReference.child("relative1_number").setValue(getnumber);

        } else if (TextUtils.isEmpty(name2)) {
            userReference.child("relative2_name").setValue(getname);
            userReference.child("relative2_number").setValue(getnumber);


        } else if (TextUtils.isEmpty(name3)) {
            userReference.child("relative3_name").setValue(getname);
            userReference.child("relative3_number").setValue(getnumber);
        } else {
            //if all names are not empty
            Toast.makeText(CenterActivity.this, "Sorry, only 3 numbers are allowed.", Toast.LENGTH_SHORT).show();
        }
    }

    //more code on onCreate method
    private void initUi() {

        //firebase get activity Instance
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        //Link to XML
        friendName1 = findViewById(R.id.friendName1);
        friendName2 = findViewById(R.id.friendName2);
        friendName3 = findViewById(R.id.friendName3);
        friendNumber1 = findViewById(R.id.friendNumber1);
        friendNumber2 = findViewById(R.id.friendNumber2);
        friendNumber3 = findViewById(R.id.friendNumber3);

        //Link to floating button XML
        floatingActionMenu = findViewById(R.id.menuAction);
        floatingActionButton = findViewById(R.id.fab_btn);

        //toolbar settings
        toolbar = findViewById(R.id.center_app_toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Smart Helmet");

        //drawer settings
        drawer = findViewById(R.id.drawer_layout_center);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        //Link to navigator XML
        navigationView = findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);

        //Link to navigator header "profile" XML
        View navView = navigationView.getHeaderView(0);
        NavProfileImage = navView.findViewById(R.id.nav_circle_img);
        NavProfileUserName = navView.findViewById(R.id.nav_username);
        NavProfileEmail = navView.findViewById(R.id.nav_email);

        //if the current user is not null
        //get relatives names and numbers from firebase
        //get profile name, image and email from firebase
        if (currentUser != null) {
            users_Id = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
            userReference = FirebaseDatabase.getInstance().getReference().child("Users").child(users_Id);
            userReference.keepSynced(true);

            userReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String name1 = (String) dataSnapshot.child("relative1_name").getValue();
                    if (name1 != null) friendName1.setText(name1);
                    String name2 = (String) dataSnapshot.child("relative2_name").getValue();
                    if (name2 != null) friendName2.setText(name2);
                    String name3 = (String) dataSnapshot.child("relative3_name").getValue();
                    if (name3 != null) friendName3.setText(name3);
                    String number1 = (String) dataSnapshot.child("relative1_number").getValue();
                    if (number1 != null) friendNumber1.setText(number1);
                    String number2 = (String) dataSnapshot.child("relative2_number").getValue();
                    if (number2 != null) friendNumber2.setText(number2);
                    String number3 = (String) dataSnapshot.child("relative3_number").getValue();
                    if (number3 != null) friendNumber3.setText(number3);

                    if (dataSnapshot.hasChild("user_email")) {
                        String fullname = Objects.requireNonNull(dataSnapshot.child("user_email").getValue()).toString();
                        NavProfileEmail.setText(fullname);
                    }
                    if (dataSnapshot.hasChild("full_name")) {
                        String fullname = Objects.requireNonNull(dataSnapshot.child("full_name").getValue()).toString();
                        NavProfileUserName.setText(fullname);
                    }
                    if (dataSnapshot.hasChild("user_profile_image")) {
                        String image = Objects.requireNonNull(dataSnapshot.child("user_profile_image").getValue()).toString();
                        Picasso.with(CenterActivity.this).load(image).placeholder(R.drawable.profile).into(NavProfileImage);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

    }

    //when the activity starts
    //if their is no user >> go to login page
    //if not >> ask user for the permissions ,check permissions every time app opens
    //and start tracking the location "trackingMethod"
    @Override
    protected void onStart() {
        super.onStart();
        currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            //go to login activity by intent
            Intent StartPageInt = new Intent(CenterActivity.this, LoginActivity.class);
            StartPageInt.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(StartPageInt);
            finish();
        } else {
            checkPermissions();
            trackingMethod();
        }
    }

    //check permissions
    //if (CALL or SMS or GPS) not have access
    //ask user for permissions
    private void checkPermissions() {
        // The request code used in ActivityCompat.requestPermissions()
        // and returned in the Activity's onRequestPermissionsResult()

        String[] PERMISSIONS = {
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.CALL_PHONE,
                android.Manifest.permission.SEND_SMS};

        if (!hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }
    }

    //(CALL, SMS and GPS)
    //see who have access
    //and who do not have access
    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    //user permissions result handler
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_ALL:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(CenterActivity.this,
                            "Permissions accepted", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(CenterActivity.this,
                            "Permission denied", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }


    //get the location latitude/longitude and update it to firebase
    private void trackingMethod() {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                //get the location name from latitude and longitude
                Geocoder geocoder = new Geocoder(getApplicationContext());
                try {
                    List<Address> addresses =
                            geocoder.getFromLocation(latitude, longitude, 1);

                    locationUpdate(latitude, longitude);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        };

        //check permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED) {
            return;
        }

        //min time and min distance to upload
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 100, 0, locationListener);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 0, locationListener);
    }

    //if the current user is not null
    //upload to firebase
    //location latitude/longitude
    //google url and the current time
    private void locationUpdate(double latitude, double longitude) {
        currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            users_Id = currentUser.getUid();
            userReference = FirebaseDatabase.getInstance().getReference().child("Users").child(users_Id);
            String locationUrl = "google.com/maps/search/?api=1&query=" + latitude + "," + longitude;
            userReference.child("user_location_latitude").setValue(latitude);
            userReference.child("user_location_longitude").setValue(longitude);
            userReference.child("user_location_Url").setValue(locationUrl);
            userReference.child("user_last_seen").setValue(currentTime.toString());
        }
    }

    //when the user press the back
    //if the drawer is open >> close it first
    //if not >> call super onBack
    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    //Navigator menu actions
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {

            case R.id.nav_track:
                //in transit from page to tracking page by Intent
                startActivity(new Intent(CenterActivity.this, PhoneAuthActivity.class));
                break;

            case R.id.nav_settings:
                //settings
                Toast.makeText(this, "settings", Toast.LENGTH_SHORT).show();
                break;

            case R.id.nav_connect:
                //in transit from page to bluetooth page by Intent
                startActivity(new Intent(CenterActivity.this, Select.class));
                break;

            case R.id.nav_logout:
                //call the logout method
                logoutuser();
                break;
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    //logout user
    private void logoutuser() {
        //stop the tracking
        locationManager.removeUpdates(locationListener);
        //logout from the firebase
        mAuth.signOut();
        //in transit from page to Login page by Intent
        Intent StartPageInt = new Intent(CenterActivity.this, LoginActivity.class);
        StartPageInt.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(StartPageInt);
        finish();
    }
}





