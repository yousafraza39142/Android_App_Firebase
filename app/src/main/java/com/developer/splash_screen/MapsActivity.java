package com.developer.splash_screen;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LocationManager mLocationManager;
    public static final int LOCATION_REFRESH_TIME = 30;
    public static final float LOCATION_REFRESH_DISTANCE = 1;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 2;
    private LatLng mLatLng;
    ProgressDialog dialog;
    String MAPS = "maps";
    LatLng mLatLng_LongPress = null;
    Button btnShare;
    Button btnGetLocation;
    Criteria criteria;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps_layout);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        btnShare = findViewById(R.id.maps_share);
        btnShare.setOnClickListener(view -> {
            if (mLatLng_LongPress != null) {
                shareTextIntent("My Location\nhttp://maps.google.com/maps?addr=" + mLatLng_LongPress.latitude + "," + mLatLng_LongPress.longitude);
            } else
                Toast.makeText(this, "Nothing to Share", Toast.LENGTH_SHORT).show();
        });
        dialog = new ProgressDialog(this);
        dialog.setTitle("Loading");
        dialog.setMessage("Fetching Location");
        dialog.setCanceledOnTouchOutside(false);
        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        btnGetLocation = findViewById(R.id.btn_location);
        btnGetLocation.setOnClickListener(view -> {
            Toast.makeText(this, "Handled", Toast.LENGTH_SHORT).show();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    requestLocationPermission();
                    return;
                } else {
                    Toast.makeText(this, "Have Permission", Toast.LENGTH_SHORT).show();
                    fetchLocation();
                }
            } else {
                Toast.makeText(this, "Permissions Not Required", Toast.LENGTH_SHORT).show();
                fetchLocation();
            }
        });

    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Location location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            LatLng temp = new LatLng(location.getLongitude(),location.getLatitude());
            ShowMarker(temp,"Last Know Location",true,true);
        } else {
            requestLocationPermission();
        }
        mMap.setOnMapLongClickListener(latLng -> {

            showShareBtn();
            mLatLng_LongPress = new LatLng(latLng.latitude, latLng.longitude);
            ShowMarker(new LatLng(latLng.latitude, latLng.longitude),
                    "Selected Location",
                    false,
                    false);
        });
        mMap.setOnMapClickListener(latLng -> {
            mMap.clear();
            hideShareBtn();
        });

    }

    @SuppressLint("MissingPermission")
    private void fetchLocation() {

        criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        mLocationManager.requestSingleUpdate(mLocationManager.getBestProvider(criteria, true), mLocationListener, null);
        dialog.show();
    }

    private void shareTextIntent(String shareText) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        sendIntent.setType("text/plain");
        startActivity(sendIntent);
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(MapsActivity.this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                MY_PERMISSIONS_REQUEST_LOCATION);
    }

    private void showShareBtn() {
        if (btnShare.getVisibility() == View.INVISIBLE) {
            btnShare.setVisibility(View.VISIBLE);
            ObjectAnimator animator = ObjectAnimator.ofFloat(btnGetLocation, "Y", btnGetLocation.getY() - btnShare.getHeight());
            animator.setDuration(500);
            animator.setInterpolator(new DecelerateInterpolator());
            animator.start();
        }
    }

    private void hideShareBtn() {
        if (btnShare.getVisibility() == View.VISIBLE) {
            btnShare.setVisibility(View.INVISIBLE);
            ObjectAnimator animator = ObjectAnimator.ofFloat(btnGetLocation, "Y", btnGetLocation.getY() + btnShare.getHeight());
            animator.setDuration(500);
            animator.setInterpolator(new DecelerateInterpolator());
            animator.start();
        }
    }

    private LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            Toast.makeText(MapsActivity.this, "onLocationUpdated", Toast.LENGTH_SHORT).show();

            mLatLng = new LatLng(location.getLatitude(), location.getLongitude());
            ShowMarker(mLatLng, "Your Current Location", true, true);
            if (dialog.isShowing())
                dialog.hide();
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

    private void ShowMarker(LatLng mLatLng, String marker_msg, boolean moveCamera, boolean addCircle) {
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(mLatLng).title(marker_msg + mLatLng.toString()));
        if (addCircle) {
            mMap.addCircle(new CircleOptions()
                    .center(mLatLng)
                    .clickable(false)
                    .fillColor(getResources().getColor(R.color.maps_innerCircle))
                    .radius(20)
                    .strokeWidth(1)
                    .strokeColor(getResources().getColor(R.color.colorPrimary)));
        }
        if (moveCamera) {
            mMap.moveCamera(CameraUpdateFactory.newLatLng(mLatLng));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mLatLng, 18), 5000, new GoogleMap.CancelableCallback() {
                @Override
                public void onFinish() {

                }

                @Override
                public void onCancel() {

                }
            });
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_PERMISSIONS_REQUEST_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                //PermissionGranted
                mLocationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, mLocationListener, null);
            dialog.show();
        }
    }

}
