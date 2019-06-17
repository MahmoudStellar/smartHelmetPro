package com.team_project.pro_smarthelmet.AnonymousV1;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.team_project.pro_smarthelmet.R;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

public class PhoneAuthActivity extends AppCompatActivity {

    //link to xml
    EditText trackerPhone, userPhone;
    Button trackNow;

    //ProgressBar and Toolbar vars
    ProgressBar progressBar;
    Toolbar toolbar;

    String trackerPh, userPh;

    boolean endSearch = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_auth);

        //set the toolbar
        toolbar = findViewById(R.id.phone_auth_app_toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Tracker");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //link to xml
        progressBar = findViewById(R.id.phone_auth_progressbar);
        progressBar.setVisibility(View.INVISIBLE);

        //link to xml
        trackNow = findViewById(R.id.track_now_btn);
        trackerPhone = findViewById(R.id.tracker_phone);
        userPhone = findViewById(R.id.user_phone);

        //when TRACK NOW is pressed
        trackNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //get the user input
                trackerPh = trackerPhone.getText().toString();
                userPh = userPhone.getText().toString();

                //-check number
                //to see if the numbers are in the firebase
                CheckNumber(trackerPh, userPh);
            }
        });
    }

    //when the user press the arrow on the toolbar
    //go back to the last activity
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            this.finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void CheckNumber(String trackerPh, String userPh) {

        //check if the input is not empty first
        if (TextUtils.isEmpty(trackerPh)) {
            Toast.makeText(PhoneAuthActivity.this, "Please write your friend number ", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(userPh)) {
            Toast.makeText(PhoneAuthActivity.this, "Please write your number ", Toast.LENGTH_SHORT).show();
        } else {
            //Checking..

            //make the loading bar visible
            progressBar.setVisibility(View.VISIBLE);

            //Get data snapshot "Users" root node
            //this wil get all the user data from the firebase
            //and store it in a Map date type
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Users");
            ref.addListenerForSingleValueEvent(
                    new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            //Get map of users in data snapshot
                            //as a parameter to collectPhoneNumbers
                            collectPhoneNumbers((Map<String, Object>) Objects.requireNonNull(dataSnapshot.getValue()));
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
        }
    }

    private void collectPhoneNumbers(Map<String, Object> users) {

        //list to store all user_phone_number
        ArrayList<String> phoneNumbers = new ArrayList<>();

        //iterate through each user, ignoring their UID
        int i = 0;
        for (Map.Entry<String, Object> entry : users.entrySet()) {

            //Get user map
            Map singleUser = (Map) entry.getValue();

            //Get phone field and add it to the list
            phoneNumbers.add((String) singleUser.get("user_phone_number"));

            //check if this number is not null first
            if (phoneNumbers.get(i) != null) {

                //if the input "friend number" or userPh was found in the firebase
                //get all the relatives numbers and user_uid
                //if the input "your number" or trackerPh equal to any
                //of the relatives numbers >> go to maps activity and
                //send the user user_uid to start tracking
                if (userPh.equals(phoneNumbers.get(i))) {
                    String num1 = (String) singleUser.get("relative1_number");
                    String num2 = (String) singleUser.get("relative2_number");
                    String num3 = (String) singleUser.get("relative3_number");
                    String u_uid = (String) singleUser.get("user_uid");

                    if (trackerPh.equals(num1) | trackerPh.equals(num2) | trackerPh.equals(num3)) {

                        //make the loading bar invisible
                        progressBar.setVisibility(View.INVISIBLE);
                        Toast.makeText(PhoneAuthActivity.this, "Match!", Toast.LENGTH_SHORT).show();

                        //go to MapsActivity by intent and send user uid to the activity
                        Intent intent = new Intent(this, MapsActivity.class);
                        intent.putExtra("user_uid", u_uid);
                        startActivity(intent);

                        //end of search
                        endSearch = true;
                    } else {

                        //make the loading bar invisible
                        progressBar.setVisibility(View.INVISIBLE);
                        Toast.makeText(PhoneAuthActivity.this, "Sorry your number is not in his contacts list!", Toast.LENGTH_SHORT).show();

                        //end of search
                        endSearch = true;
                    }
                }
            }
            i++;
        }

        //this will work only if the search is ended
        if (!endSearch) {
            Toast.makeText(PhoneAuthActivity.this, "Your friend is not a user!", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.INVISIBLE);
        }
    }
}
