package com.developer.splash_screen;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.firebase.ui.auth.IdpResponse;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

public class LogInActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 3;
    @SuppressWarnings("unused")
    private static final String EMAIL = "email";
    @SuppressWarnings("unused")
    private static final String PASSWORD = "password";
    private static final String LIFECYCLE = "lifecycle";
    TextView et_email, et_password;
    TextInputLayout til_email_layout, til_password;
    TextView tv_signup;
    MaterialButton bt_login;
    Intent intent;
    @SuppressWarnings("unused")
    FirebaseAuth mauth;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mauth = FirebaseAuth.getInstance();
        if (mauth.getCurrentUser() != null && mauth.getCurrentUser().isEmailVerified()) {
            Log.d("SignUpProcess", "UserNotNull");
            startActivity(HomeActivity.class);
        } else {
        }
        Log.d("SignUpProcess", "NullUser");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Log.d(LIFECYCLE, "OnCreate");
        Objects.requireNonNull(getSupportActionBar()).hide();

        et_email = findViewById(R.id.et_email);
        et_password = findViewById(R.id.et_password);
        bt_login = findViewById(R.id.bt_login);
        tv_signup = findViewById(R.id.tv_signup);
        til_email_layout = findViewById(R.id.abcd);
        til_password = findViewById(R.id.layoutTextInput);
        progressBar = findViewById(R.id.prgbar_login);


        FloatingActionButton fab = findViewById(R.id.fab_signup);
        fab.setOnClickListener(view -> {
            startActivity(SignInActivity.class);
        });

        bt_login.setOnClickListener(view -> {
            progressBar.setVisibility(View.VISIBLE);
            String email = et_email.getText().toString();
            String password = et_password.getText().toString();
            if (email.isEmpty())
                til_email_layout.setError(getResources().getString(R.string.empty_field));
            if (password.isEmpty())
                til_password.setError(getResources().getString(R.string.empty_field));
            if (!email.isEmpty() && !password.isEmpty()) {
                mauth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(LogInActivity.this, task -> {
                            if (task.isSuccessful()) {
                                FirebaseUser user = mauth.getCurrentUser();
                                if (user != null && user.isEmailVerified()) {
                                    startActivity(HomeActivity.class);
                                } else {
                                    progressBar.setVisibility(View.INVISIBLE);
                                    Toast.makeText(this, "Verify Email First", Toast.LENGTH_SHORT).show();
                                    if (user != null)
                                        user.sendEmailVerification();
                                }
                            } else {
                                progressBar.setVisibility(View.INVISIBLE);
                                String errorString = task.getException().getMessage();
                                if (errorString != null)
                                    Toast.makeText(this, errorString, Toast.LENGTH_SHORT).show();
                                else
                                    Toast.makeText(this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }else
                progressBar.setVisibility(View.INVISIBLE);
        });

         /*
        Start Sign-Up Activity
         */
        tv_signup.setOnClickListener(view -> {
            startActivity(SignInActivity.class);
        });
        et_email.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                til_email_layout.setError(null);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        et_password.addTextChangedListener(new TextWatcher() {
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
        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);
            if (resultCode == RESULT_OK) {

            } else {
                if (response == null)
                    Toast.makeText(this, "BackPressed", Toast.LENGTH_LONG).show();
                else
                    Toast.makeText(this, response.getError().getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onStart() {
        Log.d(LIFECYCLE, "OnStart");
        super.onStart();


    }

    @Override
    protected void onResume() {
        Log.d(LIFECYCLE, "OnResume");
        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.d(LIFECYCLE, "OnPause");
        super.onPause();
    }

    @Override
    protected void onStop() {
        Log.d(LIFECYCLE, "OnStop");
        super.onStop();
    }

    @Override
    protected void onRestart() {
        Log.d(LIFECYCLE, "OnRestart");
        super.onRestart();
    }

    @Override
    protected void onDestroy() {
        Log.d(LIFECYCLE, "OnDestroy");
        super.onDestroy();
    }

    @SuppressWarnings("unused")
    private void startActivity(Class cl) {
        intent = new Intent(LogInActivity.this, cl);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }
}
