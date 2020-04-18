package com.developer.splash_screen;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class SignInActivity extends AppCompatActivity {


    public static final String DATABASE = "Database";

    private FirebaseUser user;
    private Bitmap bm_profilePic;
    private CircleImageView iv_signupLogo;
    private FirebaseUser currentUser;
    private Intent intent, intent_login;
    private Button bt_signup, bt_cancel;
    private Uri uri_image = null, uri_downloadUri;
    private ByteArrayOutputStream ba_OutputStream;
    private TextInputLayout til_email, til_password, til_confirm_password;
    private EditText et_email_signup, et_password_signup, et_confirm_password_signup;
    private UserProfileChangeRequest profileChangeRequest;
    private FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
    ProgressBar progressBar;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference mRefName, mRefEmail, mRefPhotoUrl;

    /*
    FireBase Auth
     */
    FirebaseAuth auth;
    /*
    Reference to Activity to Pass to Crop Activity
     */
    Activity currentActivity = this;
    /*
    SharedPreference to Save Sessions
     */
    SharedPreferences sessions_prefereces;
    String password = null, email = null;
    /*
    Pattern for Password During Sign-Up
     */
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^.{8,}$");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        ButterKnife.bind(this);

        firebaseDatabase = FirebaseDatabase.getInstance();

        Objects.requireNonNull(getSupportActionBar()).hide();
        /*
        Views Initialization
         */
        et_confirm_password_signup = findViewById(R.id.et_confirm_password_signup);
        et_email_signup = findViewById(R.id.et_email_signup);
        et_password_signup = findViewById(R.id.et_password_signup);
        bt_signup = findViewById(R.id.bt_signup);
        bt_cancel = findViewById(R.id.bt_cancel);
        til_email = findViewById(R.id.textInputLayout);
        til_confirm_password = findViewById(R.id.textInputLayout_confirmpassword);
        til_password = findViewById(R.id.textInputLayout2);
        iv_signupLogo = findViewById(R.id.logo_signup);
        progressBar = findViewById(R.id.prgbar_loading);
        /*
        OutPut Stream. Used to send stream of Image data to server
         */
        ba_OutputStream = new ByteArrayOutputStream();
        /*
        Intent to Store data and intent to Home Activity
         */
        intent = new Intent(getApplicationContext(), HomeActivity.class);
        intent_login = new Intent(this, LogInActivity.class);
        /*
        Get Shared Preference Using Application Package as Preference key and Mode set to private
         */
        sessions_prefereces = getSharedPreferences(getResources().getString(R.string.sharedpreferencekey), MODE_PRIVATE);
        auth = FirebaseAuth.getInstance();
        bt_cancel.setOnClickListener(view -> {
            startActivity(intent_login);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            finish();
        });
        /*
        Sign-Up Process
        Credentials are Stored in Shared Preference
         */
        bt_signup.setOnClickListener(view -> {
            password = et_password_signup.getText().toString();
            email = et_email_signup.getText().toString().toLowerCase();
            if (!(et_email_signup.getText().toString().isEmpty() || et_password_signup.getText().toString().isEmpty() || et_confirm_password_signup.getText().toString().isEmpty())) {
                /*
                Put Email and Password in Extra Before setting Result for Activity
                 */
                if (checkPassword(password) && Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    String confirmPassword = et_confirm_password_signup.getText().toString();
                    if (password.equals(confirmPassword)) { //Password Confimation Check
                        intent.putExtra(Intent.EXTRA_EMAIL, et_email_signup.getText().toString());
                        intent.putExtra(Intent.EXTRA_TEXT, et_password_signup.getText().toString());
                        if (uri_image != null) {
                            progressBar.setVisibility(View.VISIBLE);
                            auth.createUserWithEmailAndPassword(email, password)
                                    .addOnCompleteListener(currentActivity, task -> {
                                        if (task.isSuccessful()) {
                                            // Sign in success, update UI with the signed-in user's information
                                            Log.d("DEBUG", "createUserWithEmail:success");
                                            //if (uri_image != null) {
                                            user = auth.getCurrentUser();
                                            if (user != null) {
                                                user.sendEmailVerification().addOnCompleteListener(
                                                        task1 -> {
                                                            if (task1.isSuccessful()) {
                                                                String path = "Users/" + user.getUid() + ".png";
                                                                UploadPhoto(uri_image, path);
                                                                Toast.makeText(currentActivity, "Verify Email Address", Toast.LENGTH_SHORT).show();
                                                                Toast.makeText(currentActivity, path, Toast.LENGTH_SHORT).show();
                                                            } else {
                                                                Toast.makeText(currentActivity, Objects.requireNonNull(task1.getException()).getMessage(), Toast.LENGTH_SHORT)
                                                                        .show();
                                                                progressBar.setVisibility(View.INVISIBLE);
                                                            }
                                                        }
                                                );
                                            }
                                        } else {
                                            Toast.makeText(currentActivity, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT)
                                                    .show();
                                            progressBar.setVisibility(View.INVISIBLE);
                                        }
                                    });
                        } else {
                            Toast.makeText(currentActivity, "Set Profile Pic Please", Toast.LENGTH_SHORT).show();
                        }

                    }
                } else {
                    if (!checkPassword(password))
                        til_password.setError(getResources().getString(R.string.invalid_password));
                    if ((!Patterns.EMAIL_ADDRESS.matcher(email).matches()))
                        til_email.setError(getResources().getString(R.string.invalid_email));
                    if (et_confirm_password_signup.getText().toString().isEmpty())
                        til_confirm_password.setError("Invalid Password");
                }
            } else {
                if (!checkPassword(password))
                    til_password.setError(getResources().getString(R.string.invalid_password));
                if ((!Patterns.EMAIL_ADDRESS.matcher(email).matches()))
                    til_email.setError(getResources().getString(R.string.invalid_email));
                if (et_confirm_password_signup.getText().toString().isEmpty())
                    til_confirm_password.setError("Invalid Password");
            }
        });
        /*
        Image Selector
         */
        iv_signupLogo.setOnClickListener(view -> CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1, 1)
                .start(currentActivity));
        /*
        Set Text Change Listener to Clear error
         */
        et_email_signup.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                til_email.setError(null);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        et_password_signup.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                til_password.setError(null);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
            /*
            Crop Activity Result
             */
                CropImage.ActivityResult result = CropImage.getActivityResult(data);

            /*
            Get Uri from Result. This Uri is used to get Image in IM.View Using Picasso
             */
                //TODO check for null reference
                uri_image = result.getUri();


            /*
            Picasso Library used to Load Image into ImageView
             */
                Picasso.get().load(uri_image).into(iv_signupLogo);
            } else if (resultCode == CropImage.CAMERA_CAPTURE_PERMISSIONS_REQUEST_CODE) {

                /*
                If OS build is Above MarshMallow or 16 api it will Request for Camera Permission Declared in Android Manifest file
                 */
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, CropImage.CAMERA_CAPTURE_PERMISSIONS_REQUEST_CODE);
                }
            }
        }

    }

    /*
        OverRide BackPressed Functionality
         */
    @Override
    public void onBackPressed() {
        /*
        Set Result to Cancel & Finish Activity if Back is Pressed
         */
        setResult(RESULT_CANCELED);
        endActivity();

    }

    /*
    This Function Finishes the Activity and Override Ending Animation to fade-out
     */
    public void endActivity() {
        finish();
        /*
        Fade-out Animation when Activity is Finished
         */
        overridePendingTransition(0, R.anim.fade_out);
    }

    /*
    Checks Password for Pattern Matching
     */
    public boolean checkPassword(String password) {
        Matcher MATCHER = PASSWORD_PATTERN.matcher(password);
        return MATCHER.matches();
    }

    private void UploadPhoto(Uri imageUri, String path) {

        if (imageUri != null) {
            Toast.makeText(this, imageUri.toString(), Toast.LENGTH_SHORT).show();
            try {
                bm_profilePic = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
            } catch (IOException e) {
                e.printStackTrace();
            }
            //  UploadPhoto(bm_profilePic);
        }

        bm_profilePic.compress(Bitmap.CompressFormat.PNG, 10, ba_OutputStream);
        byte[] dataArray = ba_OutputStream.toByteArray();

        //String path = "Users/" + UUID.randomUUID() + ".png";
        StorageReference storageReference = firebaseStorage.getReference(path);
        UploadTask uploadTask = storageReference.putBytes(dataArray);
        uploadTask.addOnCompleteListener(task -> {
            Toast.makeText(currentActivity, "Uploading", Toast.LENGTH_SHORT).show();
            if (task.isSuccessful()) {
                Toast.makeText(currentActivity, "Uploaded", Toast.LENGTH_SHORT).show();
            } else {
                String error = task.getException().getMessage();
                if (error != null)
                    Toast.makeText(currentActivity, error, Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(currentActivity, "Error", Toast.LENGTH_SHORT).show();
            }
        });
        Task<Uri> getDownloadUrl = uploadTask.continueWithTask(task -> {
            if (!task.isSuccessful())
                throw task.getException();
            else
                return storageReference.getDownloadUrl();
        }).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                uri_downloadUri = task.getResult();
                String name;
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT)
                    name = user.getEmail().substring(0, Objects.requireNonNull(user.getEmail(), "null").indexOf('@'));
                else
                    name = user.getEmail().substring(0, Objects.requireNonNull(user.getEmail()).indexOf('@'));

                profileChangeRequest = new UserProfileChangeRequest.Builder()
                        .setPhotoUri(Uri.parse(uri_downloadUri.toString()))
                        .setDisplayName(name).build();
                Log.d("SignUpProcess", name);
                user.updateProfile(profileChangeRequest);

                mRefName = firebaseDatabase.getReference("Users/" + user.getUid() + "/Name");
                mRefName.setValue(name);
                mRefPhotoUrl = firebaseDatabase.getReference("Users/" + user.getUid() + "/PhotoUrl");
                mRefPhotoUrl.setValue(uri_downloadUri.toString());
                mRefEmail = firebaseDatabase.getReference("Users/" + user.getUid() + "/Email");
                mRefEmail.setValue(email);


                startActivity(intent_login);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                finish();

            }
        });

    }
}// End Bracket
