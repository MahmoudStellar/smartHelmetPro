package com.team_project.pro_smarthelmet;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.team_project.pro_smarthelmet.AnonymousV1.PhoneAuthActivity;

public class LoginActivity extends AppCompatActivity {


    //link to xml
    TextView signUp, forgotPassword, tracker;
    EditText userEmail, userPassword;
    Button btn_login;

    ProgressBar progressBar;

    //firebase var
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //firebase get instance
        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();

        //link to Xml
        userEmail = findViewById(R.id.login_Email);
        userPassword = findViewById(R.id.login_password);

        //link to Xml
        progressBar = findViewById(R.id.login_progressbar);
        progressBar.setVisibility(View.INVISIBLE);

        //link to Xml
        signUp = findViewById(R.id.login_Signup);
        tracker = findViewById(R.id.login_track);

        forgotPassword = findViewById(R.id.login_ForgotPassword);

        //link to Xml
        btn_login = findViewById(R.id.login_btn);

        //When sign up button is pressed
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent register_intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(register_intent);
            }
        });

        //When track now button is pressed
        tracker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent register_intent = new Intent(LoginActivity.this, PhoneAuthActivity.class);
                startActivity(register_intent);
            }
        });

        //When login button is pressed
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = userEmail.getText().toString();
                String password = userPassword.getText().toString();

                LoginUserAccount(email, password);
            }
        });


    }

    // Login user Account and check Text
    private void LoginUserAccount(String email, String password) {
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(LoginActivity.this, "Please write your email ", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(password)) {
            Toast.makeText(LoginActivity.this, "Please write your password ", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(LoginActivity.this, "Please wait ,verifying ..", Toast.LENGTH_SHORT).show();

            //make the loading bar visible
            progressBar.setVisibility(View.VISIBLE);

            //login now
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                progressBar.setVisibility(View.INVISIBLE);
                                //login successful
                                Intent next_Chat = new Intent(LoginActivity.this, CenterActivity.class);
                                startActivity(next_Chat);
                            } else {
                                Toast.makeText(LoginActivity.this, "Wrong email or password , please write your valid email and password .", Toast.LENGTH_SHORT).show();
                            }
                            progressBar.setVisibility(View.INVISIBLE);
                        }

                    });
        }
    }
}

