package com.developer.splash_screen;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {


    static final String PAGERDEBUG = "pager";

    @BindView(R.id.nav_view)
    NavigationView mNavigationView;
    FirebaseAuth mauth;
    Intent intent_logout;
    private FirebaseUser user;
    ImageView iv_nav_userProfile;
    private DrawerLayout mdrawerLayout;
    TextView tv_nav_name, tv_nav_email;
    Intent shareIntent, tabbedIntent, listUserIntent;
    private Uri uri_image;
    private FirebaseStorage storage;
    private StorageReference mStorageRef;
    private Bitmap bm_profilePic;


    @Override

    protected void onCreate(Bundle savedInstanceState) {
        // Toast.makeText(this, "Before Inflation", Toast.LENGTH_SHORT).show();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setTitle(getResources().getString(R.string.wary_fox));
        setSupportActionBar(toolbar);
        ButterKnife.bind(this);


        GoogleApiAvailability.getInstance().makeGooglePlayServicesAvailable(this);

        storage = FirebaseStorage.getInstance();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("MyNotifications", "MyNotifications", NotificationManager.IMPORTANCE_DEFAULT);
            ((NotificationManager) getSystemService(NotificationManager.class)).createNotificationChannel(channel);
        }else{
            Toast.makeText(this, Build.VERSION.SDK_INT + "", Toast.LENGTH_SHORT).show();
        }
        FirebaseMessaging.getInstance().subscribeToTopic("general")
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful())
                        Toast.makeText(this, "SubsSuccessfull", Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(this, "Subs NOT Successfull", Toast.LENGTH_SHORT).show();
                });

        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(task -> {
                    if(!task.isSuccessful()){
                        Toast.makeText(this, "Id Error", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String token = Objects.requireNonNull(task.getResult()).getToken();
                    Toast.makeText(this, "Token" + token, Toast.LENGTH_SHORT).show();
                    System.out.println("TokenID"+ token);
                });























        intent_logout = new Intent(this, LogInActivity.class);
        tabbedIntent = new Intent(HomeActivity.this, TabbedActivity.class);
        listUserIntent = new Intent(HomeActivity.this, ListUserActivity.class);
        mauth = FirebaseAuth.getInstance();
        user = mauth.getCurrentUser();
        mdrawerLayout = findViewById(R.id.drawer_layout);
        mNavigationView.setNavigationItemSelectedListener(this);

        tv_nav_name = mNavigationView.getHeaderView(0).findViewById(R.id.tv_nav_name);
        tv_nav_email = mNavigationView.getHeaderView(0).findViewById(R.id.tv_nav_email);
        iv_nav_userProfile = mNavigationView.getHeaderView(0).findViewById(R.id.iv_nav_userProfile);

        iv_nav_userProfile.setOnClickListener(PicListener);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, mdrawerLayout, toolbar, R.string.OpenDrawer, R.string.ClosedDrawer);
        mdrawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        iv_nav_userProfile = mNavigationView.getHeaderView(0).findViewById(R.id.iv_nav_userProfile);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show());
        if (user == null) {
            startActivity(LogInActivity.class);
        }

        setProfilePic(user.getPhotoUrl(), R.drawable.loadingicon);
        setNameEmail(user.getDisplayName(), user.getEmail());
        FirebaseUser user = mauth.getCurrentUser();

//        mauth.addAuthStateListener(firebaseAuth -> {
//            if(firebaseAuth.getCurrentUser() == null) {
//                firebaseAuth.signOut();
//                startActivity(intent_logout);
//                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
//                finish();
//
//            }
//        });
    }

    private View.OnClickListener PicListener = view -> {
        if (view.getId() == R.id.iv_nav_userProfile) {
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(HomeActivity.this);
        }
    };

    private void setNameEmail(String displayName, String email) {
        if (displayName == null)
            displayName = "NotSet";
        if (email == null)
            email = "NotSet";

        if (displayName.isEmpty())
            displayName = email.substring(0, email.indexOf('@'));
        /*tv_name.setText(displayName);
        tv_email.setText(email);*/
        tv_nav_email.setText(email);
        tv_nav_name.setText(displayName);
    }

    private void startActivity(Class targetActivity) {
        startActivity(new Intent(HomeActivity.this, targetActivity));
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }

    private void setProfilePic(@Nullable Uri uriImage, int defaultDrawableId) {
        if (uriImage != null) {
            Picasso.get()
                    .load(uriImage)
                    .memoryPolicy(MemoryPolicy.NO_CACHE)
                    .into(iv_nav_userProfile);
        } else {
            Picasso.get().load(defaultDrawableId).into(iv_nav_userProfile);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                //TODO check for null reference
                uri_image = result.getUri();
                mStorageRef = storage.getReference();
                StorageReference childRef = mStorageRef.child("Users/" + user.getUid() + ".png");
                childRef.delete().addOnCompleteListener(task -> {
                    if (!task.isSuccessful())
                        Toast.makeText(this, "Fiile Could not be deleted", Toast.LENGTH_SHORT).show();
                });

                //________________UPLOAD TASK START______________________//
                if (uri_image != null) {
                    try {
                        bm_profilePic = MediaStore.Images.Media.getBitmap(getContentResolver(), uri_image);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bm_profilePic.compress(Bitmap.CompressFormat.JPEG, 1, baos);
                byte[] Byte_data = baos.toByteArray();


                UploadTask uploadTask = childRef.putBytes(Byte_data);
                uploadTask.addOnCompleteListener(task -> {
                    Toast.makeText(HomeActivity.this, "Uploading", Toast.LENGTH_SHORT).show();
                    if (task.isSuccessful()) {
                        Toast.makeText(HomeActivity.this, "Uploaded", Toast.LENGTH_SHORT).show();
                    } else {
                        String error = task.getException().getMessage();
                        if (error != null)
                            Toast.makeText(HomeActivity.this, error, Toast.LENGTH_SHORT).show();
                        else
                            Toast.makeText(HomeActivity.this, "Error", Toast.LENGTH_SHORT).show();
                    }
                });

                //________________UPLOAD TASK END__________________//
                Picasso.get().load(uri_image).into(iv_nav_userProfile);
            } else if (resultCode == CropImage.CAMERA_CAPTURE_PERMISSIONS_REQUEST_CODE) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, CropImage.CAMERA_CAPTURE_PERMISSIONS_REQUEST_CODE);
                }
            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.homeactivitymenu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        /*
        Id of selected item
         */
        int ResId = item.getItemId();


        /*
        If logout is selected A prompt is given asking for confirmation
         */
        if (ResId == R.id.action_logout) {
            new AlertDialog.Builder(this)
                    .setIcon(R.drawable.ic_error)
                    .setTitle("LogOut? ")
                    .setMessage("Are you sure you want to logout?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        mauth.signOut();
                        startActivity(intent_logout);
                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                        finish();
                    })
                    .setNegativeButton("No", null)
                    .show();

        }
        return super.onOptionsItemSelected(item);
    }

    boolean doubleBackPressed = false;

    @Override
    public void onBackPressed() {
        if (mdrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mdrawerLayout.closeDrawer(GravityCompat.START);
            return;
        }

        if (doubleBackPressed) {
            super.onBackPressed();
            return;
        }
        Toast.makeText(this, "Press again to exit", Toast.LENGTH_SHORT).show();
        this.doubleBackPressed = true;
        new Handler().postDelayed(() -> doubleBackPressed = false, 2000);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int selectedItem = item.getItemId();
        item.setChecked(false);

        switch (selectedItem) {
            case R.id.nav_message:
                startActivity(tabbedIntent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                break;

            case R.id.nav_profile:
                startActivity(new Intent(HomeActivity.this, MapsActivity.class));
                break;

            case R.id.nav_chat:
                startActivity(listUserIntent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                break;

            case R.id.nav_share:
                shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, "Hey, download this app! " + user.getPhotoUrl());
                startActivity(shareIntent);
                break;

            case R.id.nav_send:
                Toast.makeText(this, "" + R.id.nav_send, Toast.LENGTH_SHORT).show();
                break;
        }
        mdrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}
