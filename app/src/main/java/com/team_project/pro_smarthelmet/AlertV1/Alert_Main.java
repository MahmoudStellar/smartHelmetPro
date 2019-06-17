package com.team_project.pro_smarthelmet.AlertV1;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.team_project.pro_smarthelmet.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class Alert_Main extends AppCompatActivity {

    //vars firebase
    DatabaseReference userReference;
    FirebaseAuth mAuth;
    FirebaseUser currentUser;

    //relatives numbers and last location url
    String number1, number2, number3;
    String numberEmergency = "19777";
    String massage = "HELP ME! I JUST MADE AN ACCIDENT \n MY LAST LOCATION: ";
    String lastLocation;

    //the timer
    CountDownTimer CDTimer;

    //link to xml
    Button yes_btn, no_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert);

        //start the timer when the activity created
        timer();

        //firebase get instance
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        //set the alert when the activity created
        //by getting the numbers and location from firebase
        setAlert();


        //link to xml
        mControlsView = findViewById(R.id.fullscreen_content_controls);
        mContentView = findViewById(R.id.fullscreen_content);
        yes_btn = findViewById(R.id.yes_button);
        no_btn = findViewById(R.id.no_button);

        findViewById(R.id.yes_button).setOnTouchListener(mDelayHideTouchListener);
        findViewById(R.id.no_button).setOnTouchListener(mDelayHideTouchListener);

        yes_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CDTimer.cancel();
                launchAlert();
            }
        });
        no_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CDTimer.cancel();
                Toast.makeText(Alert_Main.this, "Alert mode stopped by user ", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        //make the activity show on lock screen
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        //make the buttons visible if true
        mVisible = true;

        // Set up the user interaction to manually show or hide the system UI.
        mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });
        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
    }

    //get relatives numbers and last location from database
    //and store it in number1, number2, number3 and lastLocation
    private void setAlert() {
        String users_Id = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        userReference = FirebaseDatabase.getInstance().getReference().child("Users").child(users_Id);
        userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                number1 = (String) dataSnapshot.child("relative1_number").getValue();
                number2 = (String) dataSnapshot.child("relative2_number").getValue();
                number3 = (String) dataSnapshot.child("relative3_number").getValue();
                lastLocation = (String) dataSnapshot.child("user_location_Url").getValue();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    //the timer  method
    private void timer() {
        final TextView yesText = findViewById(R.id.yes_button); // set the button xml
        CDTimer = new CountDownTimer(30 * 1000, 1000) {  // set the timer
            @SuppressLint({"SetTextI18n", "SimpleDateFormat"})
            public void onTick(long millisUntilFinished) {

                // set the button text while ticking
                yesText.setText("YES (" + new SimpleDateFormat("ss").format(new Date(millisUntilFinished)) + ")");
            }

            @SuppressLint("SetTextI18n")
            public void onFinish() {
                yesText.setText("STARTING..");
                launchAlert();
            }
        }.start();
    }

    private void launchAlert() {
        Toast.makeText(Alert_Main.this, "Alert mode starting..", Toast.LENGTH_SHORT).show();

        //get the last location
        massage += lastLocation;

        //send the massage to relatives numbers
        sms(number1, massage);
        sms(number2, massage);
        sms(number3, massage);

        //call emergency
        call(numberEmergency);
    }

    //the call method
    public void call(String phoneNum) {
        if (!TextUtils.isEmpty(phoneNum)) {
            String dial = "tel:" + phoneNum;
            //check permission
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) !=
                    PackageManager.PERMISSION_GRANTED) {
                return;
            }
            //Make an Intent object of type intent.ACTION_CALL//
            startActivity(new Intent(Intent.ACTION_CALL,
                    //Extract the telephone number from the URI//
                    Uri.parse(dial)));
        }
    }

    //the sending sms method
    public void sms(String phoneNum, String massage) {
        if (!TextUtils.isEmpty(massage) && !TextUtils.isEmpty(phoneNum)) {
            //check permission
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) !=
                    PackageManager.PERMISSION_GRANTED) {
                return;
            }
            //Get the default SmsManager//
            SmsManager smsManager = SmsManager.getDefault();
            //Send the SMS//
            smsManager.sendTextMessage(phoneNum, null, massage, null, null);
        }
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////Full screen default staff ////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////


    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private View mContentView;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private View mControlsView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            mControlsView.setVisibility(View.VISIBLE);
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mControlsView.setVisibility(View.GONE);
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in delay milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }
}
