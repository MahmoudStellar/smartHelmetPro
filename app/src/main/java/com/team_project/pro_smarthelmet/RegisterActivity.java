package com.team_project.pro_smarthelmet;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class RegisterActivity extends AppCompatActivity {

    final static int Gallery_pick = 1;

    Toolbar toolbar;
    ProgressBar progressBar;

    CircleImageView profileCircleImage;

    //vars Firebase
    Uri imageUri;
    FirebaseAuth mAuth;
    DatabaseReference databaseStoreUserReference;
    StorageReference usersProfileImagesRef;

    // vars to link to XML
    EditText user_name, full_name, user_password, user_Email;
    Button btn_CreateAccount;

    int checkprofile = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //progressbar settings
        progressBar = findViewById(R.id.lreg_progressbar);
        progressBar.setVisibility(View.INVISIBLE);

        //toolbar setting
        toolbar = findViewById(R.id.loginRegister_app_toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Sign Up");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Link to firebase
        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();
        databaseStoreUserReference = FirebaseDatabase.getInstance().getReference().child("Users");
        usersProfileImagesRef = FirebaseStorage.getInstance().getReference().child("user_profile_image");

        //link to XML
        user_Email = findViewById(R.id.register_Email);
        user_password = findViewById(R.id.register_Password);
        user_name = findViewById(R.id.register_username);
        full_name = findViewById(R.id.register_userFullname);
        profileCircleImage = findViewById(R.id.register_userImageCircle);
        btn_CreateAccount = findViewById(R.id.register_btn_CreateAccount);

        //When profile image is pressed
        profileCircleImage.setOnClickListener(new View.OnClickListener() {
            //going the Gallery
            @SuppressLint("IntentReset")
            @Override
            public void onClick(View view) {
                checkprofile = 1;
                Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                        MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, Gallery_pick);
            }
        });

        //When create account button is pressed
        btn_CreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //get the user input
                String name = user_name.getText().toString();
                String fullname = full_name.getText().toString();
                String email = user_Email.getText().toString();
                String password = user_password.getText().toString();
                Register_Account(name, email, password, fullname);
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

    //image crop
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Android Image Cropper
        if (requestCode == Gallery_pick && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            // start picker to get image for cropping and then use the image in cropping activity
            CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1);
            Picasso.with(RegisterActivity.this).load(imageUri.toString()).into(profileCircleImage);
        }
    }


    private void Register_Account(final String phone_number, final String email, final String password, final String fullname) {

        //check Text not empty
        if (TextUtils.isEmpty(phone_number)) {
            Toast.makeText(RegisterActivity.this, "Please write your number", Toast.LENGTH_LONG).show();
        } else if (TextUtils.isEmpty(fullname)) {
            Toast.makeText(RegisterActivity.this, "Please write your fullname", Toast.LENGTH_LONG).show();
        } else if (TextUtils.isEmpty(email)) {
            Toast.makeText(RegisterActivity.this, "Please write your email", Toast.LENGTH_LONG).show();
        } else if (TextUtils.isEmpty(password)) {
            Toast.makeText(RegisterActivity.this, "Please write your password", Toast.LENGTH_LONG).show();
        } else if (checkprofile == 0) {
            Toast.makeText(RegisterActivity.this, "Please fill your profile", Toast.LENGTH_LONG).show();
        } else {

            //loading Bar show
            Toast.makeText(RegisterActivity.this, "Please Wait , while we are creating account for you", Toast.LENGTH_LONG).show();
            progressBar.setVisibility(View.VISIBLE);

            //Sign up user
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                //saving data on firebase
                                final String current_user_id = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
                                databaseStoreUserReference = FirebaseDatabase.getInstance().getReference().child("Users").child(current_user_id);
                                databaseStoreUserReference.child("user_uid").setValue(current_user_id);
                                databaseStoreUserReference.child("user_phone_number").setValue(phone_number);
                                databaseStoreUserReference.child("full_name").setValue(fullname);
                                databaseStoreUserReference.child("user_email").setValue(email);
                                databaseStoreUserReference.child("user_password").setValue(password)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    StorageProfileImage(current_user_id);
                                                    NextPageCenterActivity();
                                                }
                                            }
                                        });
                            } else {
                                Toast.makeText(RegisterActivity.this, "Error Occurred , Try Again...", Toast.LENGTH_LONG).show();
                            }
                            //make the loading bar invisible
                            progressBar.setVisibility(View.INVISIBLE);
                        }
                    });
        }
    }

    //store the image to firebase
    private void StorageProfileImage(final String currentUsersID) {

        final StorageReference filePath = usersProfileImagesRef.child(currentUsersID + ".jpg");

        //add file on Firebase and get Download Link
        filePath.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        databaseStoreUserReference.child("user_profile_image").setValue(uri.toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (!task.isSuccessful()) {
                                    String message = Objects.requireNonNull(task.getException()).getMessage();
                                    Toast.makeText(RegisterActivity.this, "Upload Error Occurred:" + message, Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                });
            }
        });
    }

    //Move to center Activity page
    private void NextPageCenterActivity() {
        Intent intent = new Intent(RegisterActivity.this, CenterActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
