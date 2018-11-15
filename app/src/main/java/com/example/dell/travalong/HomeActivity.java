package com.example.dell.travalong;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.Objects;

public class HomeActivity extends AppCompatActivity {

    private DatabaseReference mUserRef;
    private FirebaseAuth mAuth;
    private String userID;
    private String username;
    private String image;


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    return true;
                case R.id.navigation_popular_places:
                    return true;
                case R.id.navigation_profile:
                    Intent intent = new Intent(HomeActivity.this,UpdateProfileActivity.class);
                    startActivity(intent);

                    return true;
            }
            return false;
        }
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
                    if(dataSnapshot.hasChild("username"))
                    {
                        username = dataSnapshot.child("username").getValue(String.class);
                        Log.d("HomeAct",username);
                    }
                    if(dataSnapshot.hasChild("profileimage"))
                        {
                        image = dataSnapshot.child("profileimage").getValue(String.class);
                        Log.d("HomeAct",image);
                    }
                    else
                    {
                        Toast.makeText(HomeActivity.this, "Profile name do not exists...", Toast.LENGTH_SHORT).show();
                    }
                    loadFragments(username,image);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });




        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
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
