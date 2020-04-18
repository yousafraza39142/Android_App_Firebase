package com.developer.splash_screen;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ListUserActivity extends AppCompatActivity {

    ProgressDialog dialog;
    RecViewAdapter adapter;
    private FirebaseAuth mAuth;
    RecyclerView recyclerViewUsers;
    ArrayList<UserModel> userModels;
    SwipeRefreshLayout swipeRefreshLayout;
    private FirebaseDatabase storage;
    private DatabaseReference mRootRef;
    private DatabaseReference mUsersRef;
    private static final String FIREBASEDATABASE = "database";
    ChildEventListener eventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_user);
        dialog = new ProgressDialog(this);
        dialog.setTitle("Loading!");
        dialog.setMessage("Loading Data Please Wait");
        dialog.show();

        userModels = new ArrayList<>(3);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout_list_uers);
        swipeRefreshLayout.setOnRefreshListener(() -> {
//            ListUserActivity.this.onRestart();
//            recyclerViewUsers.
//            clearRecyclerView();
            mUsersRef.removeEventListener(eventListener);
            userModels.clear();
            getDataObject();
            adapter.notifyDataSetChanged();
            new Handler().postDelayed(() -> {
                swipeRefreshLayout.setRefreshing(false);
            },1000);

        });


        recyclerViewUsers = findViewById(R.id.rv_users);
        recyclerViewUsers.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecViewAdapter(userModels);
        recyclerViewUsers.setAdapter(adapter);
        getDataObject();
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    @Nullable
    public List<UserModel> getDataObject() {


        Toast.makeText(this, "inSideGetObject", Toast.LENGTH_SHORT).show();
        mAuth = FirebaseAuth.getInstance();
        storage = FirebaseDatabase.getInstance();
        mRootRef = storage.getReference();
        mUsersRef = mRootRef.child("Users/");


        eventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (!userModels.isEmpty())
                    dialog.hide();
                HashMap<String, String> data = (HashMap<String, String>) dataSnapshot.getValue();
                UserModel userData = new UserModel();
                userData.setName(data.get("Name"));
                userData.setEmail(data.get("Email"));
                userData.setProfilePic(data.get("PhotoUrl"));
                userModels.add(userData);
                adapter.notifyDataSetChanged();
                System.out.println("UserArryList--------------------->" + userModels.toString());
                if (!userModels.isEmpty())
                    dialog.hide();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                adapter.notifyDataSetChanged();
                dialog.hide();
            }
        };
        mUsersRef.addChildEventListener(eventListener);
        System.out.println("-------------------------------INSIDEGETOBJECT" + userModels.toString());
        return userModels;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            onBackPressed();
        }
        return true;
    }
}
