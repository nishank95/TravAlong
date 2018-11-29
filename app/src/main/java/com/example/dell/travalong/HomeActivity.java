package com.example.dell.travalong;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class HomeActivity extends AppCompatActivity {

    private DatabaseReference mUserRef;
    private FirebaseAuth mAuth;
    private String userID;
    private String username;
    private String image;


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = item -> {
                switch (item.getItemId()) {
                    case R.id.navigation_home:
                        Intent callHomeAct = new Intent(HomeActivity.this, HomeActivity.class);
                        startActivity(callHomeAct);
                        return true;

                    case R.id.navigation_followers:
                        Intent intent = new Intent(HomeActivity.this,FollowersActivity.class);
                        startActivity(intent);
                        return true;

                    case R.id.navigation_profile:
                        Intent callProfileAct = new Intent(HomeActivity.this,UserProfileActivity.class);
                        startActivity(callProfileAct);

                        return true;
                }
                return false;
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        mAuth = FirebaseAuth.getInstance();
        userID = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        mUserRef = FirebaseDatabase.getInstance().getReference().child("Users");

        mUserRef.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    if(dataSnapshot.hasChild(getString(R.string.label_username)))
                    {
                        username = dataSnapshot.child("username").getValue(String.class);

                    }
                    if(dataSnapshot.hasChild(getString(R.string.label_profile_image)))
                        {
                        image = dataSnapshot.child("profileimage").getValue(String.class);

                    }

                    loadFragments(username,image);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });




        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }


    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_options, menu);

        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {

        //respond to menu item selection
        switch (item.getItemId()) {

            case R.id.edit_profile:
                startActivity(new Intent(this, UpdateProfileActivity.class));
                return true;

            case R.id.logout:
                mAuth.signOut();
                startActivity(new Intent(this, LoginActivity.class));
                return true;

            default:

                return super.onOptionsItemSelected(item);

        }
    }

    private void loadFragments(String username, String image) {

        FragmentManager fragmentManager = getSupportFragmentManager();
        FeedListFragment feedListFragment = new FeedListFragment();
        UserPostFragment postFragment = new UserPostFragment();
        postFragment.setUserName(username);
        postFragment.setUserProfilePhoto(image);


        fragmentManager.beginTransaction()
                .add(R.id.user_post_container, postFragment)
                .commit();

        fragmentManager.beginTransaction()
                .add(R.id.feed_container, feedListFragment)
                .commit();


    }


}
