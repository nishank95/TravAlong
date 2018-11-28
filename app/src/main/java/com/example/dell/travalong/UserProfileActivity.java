package com.example.dell.travalong;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class UserProfileActivity extends AppCompatActivity {

    @BindView(R.id.display_name) TextView profileDisplayName;
    @BindView(R.id.user_name) TextView profileUserName;
    @BindView(R.id.user_status) TextView profileStatus;
    @BindView(R.id.posts_value) TextView profileNoOfPost;
    @BindView(R.id.likes_value) TextView profileNoOfLikes;
    @BindView(R.id.followers_value) TextView profileNoOfFollowers;
    @BindView(R.id.follow_btn) Button profileFollowBtn;
    @BindView(R.id.profile_user_photo) CircleImageView profileImage;

    private String receiverID, senderID, currentUserId, saveCurrentDate;
    private String CURRENT_STATE = "not_friends";
    int postCount, likesCount, followCount;
    private boolean CURRENT_USER_PROFILE = true;

    private DatabaseReference mUserRef, mPostsRef, mFollowReqRef, mFollowsRef, mLikesRef;
    private FirebaseAuth mAuth;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = item -> {
                switch (item.getItemId()) {
                    case R.id.navigation_home:
                        Intent callHomeAct = new Intent(UserProfileActivity.this, HomeActivity.class);
                        startActivity(callHomeAct);
                        return true;

                    case R.id.navigation_followers:
                        Intent intent = new Intent(UserProfileActivity.this, FollowersActivity.class);
                        startActivity(intent);
                        return true;

                    case R.id.navigation_profile:
                        Intent callSelf = new Intent(UserProfileActivity.this, UserProfileActivity.class);
                        startActivity(callSelf);
                        return true;
                }
                return false;
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        ButterKnife.bind(this);
        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        mUserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        mPostsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        mFollowReqRef = FirebaseDatabase.getInstance().getReference().child("Follow-Requests");
        mFollowsRef = FirebaseDatabase.getInstance().getReference().child("Followers");
        mLikesRef = FirebaseDatabase.getInstance().getReference().child("Likes");

        mAuth = FirebaseAuth.getInstance();
        currentUserId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

        if (getIntent().hasExtra("receiver_id"))
        {
            CURRENT_USER_PROFILE = false;

            senderID = currentUserId;
            receiverID = getIntent().getStringExtra("receiver_id");

            if (!senderID.equals(receiverID)) {
                currentUserId = receiverID;
                profileFollowBtn.setVisibility(View.VISIBLE);
                profileFollowBtn.setOnClickListener(view -> {
                    profileFollowBtn.setEnabled(false);
                    if (CURRENT_STATE.equals("not_friends")) {
                        sendFriendRequestToPerson();
                    }
                    if (CURRENT_STATE.equals("request_sent")) {
                        cancelFriendRequest();
                    }
                    if (CURRENT_STATE.equals("request_received")) {
                        acceptFollowRequest();
                    }
                    if (CURRENT_STATE.equals("friends")) {
                        unFollowExistingFriend();
                    }
                });
            }

        }

        loadProfileDetails();
        loadFragments();

        editor.apply();

    }

    public boolean onCreateOptionsMenu(Menu menu) {
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

    private void unFollowExistingFriend() {
        mFollowsRef.child(senderID).child(receiverID)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            mFollowsRef.child(receiverID).child(senderID)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {

                                        @RequiresApi(api = Build.VERSION_CODES.M)
                                        @SuppressLint({"SetTextI18n", "ResourceAsColor"})
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                profileFollowBtn.setEnabled(true);
                                                CURRENT_STATE = "not_friends";
                                                profileFollowBtn.setText("Follow +");
                                                profileFollowBtn.setBackgroundResource(R.drawable.button_rounded_blue);
                                                profileFollowBtn.setTextColor(getColor(R.color.white));
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void acceptFollowRequest() {
        Calendar calFordDate = Calendar.getInstance();
        @SuppressLint("SimpleDateFormat") SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
        saveCurrentDate = currentDate.format(calFordDate.getTime());

        mFollowsRef.child(senderID).child(receiverID).child("date").setValue(saveCurrentDate)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            mFollowsRef.child(receiverID).child(senderID).child("date").setValue(saveCurrentDate)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                mFollowReqRef.child(senderID).child(receiverID)
                                                        .removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    mFollowReqRef.child(receiverID).child(senderID)
                                                                            .removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @RequiresApi(api = Build.VERSION_CODES.M)
                                                                                @SuppressLint({"SetTextI18n", "ResourceAsColor"})
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    if (task.isSuccessful()) {
                                                                                        profileFollowBtn.setEnabled(true);
                                                                                        CURRENT_STATE = "friends";
                                                                                        profileFollowBtn.setText("UNFOLLOW");
                                                                                        profileFollowBtn.setBackgroundResource(R.drawable.button_rounded_border_blue);
                                                                                        profileFollowBtn.setTextColor(getColor(R.color.colorPrimaryDark));
                                                                                    }
                                                                                }
                                                                            });
                                                                }
                                                            }
                                                        });
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void cancelFriendRequest() {
        mFollowReqRef.child(senderID).child(receiverID)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            mFollowReqRef.child(receiverID).child(senderID)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {

                                        @RequiresApi(api = Build.VERSION_CODES.M)
                                        @SuppressLint({"SetTextI18n", "ResourceAsColor"})
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                profileFollowBtn.setEnabled(true);
                                                CURRENT_STATE = "not_friends";
                                                profileFollowBtn.setText("Follow +");
                                                profileFollowBtn.setBackgroundResource(R.drawable.button_rounded_blue);
                                                profileFollowBtn.setTextColor(getColor(R.color.white));
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void sendFriendRequestToPerson() {

        mFollowReqRef.child(senderID).child(receiverID)
                .child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            mFollowReqRef.child(receiverID).child(senderID)
                                    .child("request_type").setValue("received")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {

                                        @RequiresApi(api = Build.VERSION_CODES.M)
                                        @SuppressLint({"SetTextI18n", "ResourceAsColor"})
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                profileFollowBtn.setEnabled(true);
                                                CURRENT_STATE = "request_sent";
                                                profileFollowBtn.setText("Cancel Request");
                                                profileFollowBtn.setBackgroundResource(R.drawable.button_rounded_border_blue);
                                                profileFollowBtn.setTextColor(getColor(R.color.colorPrimaryDark));
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    @SuppressLint("CommitPrefEdits")
    private void loadProfileDetails() {

        sharedPreferences = getSharedPreferences("USER_PROFILE_PREF",MODE_PRIVATE);
        editor = sharedPreferences.edit();

        mUserRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String userDisplayName = Objects.requireNonNull(dataSnapshot.child("full_name").getValue()).toString();
                    String userName = Objects.requireNonNull(dataSnapshot.child("username").getValue()).toString();
                    String userStatus = Objects.requireNonNull(dataSnapshot.child("status").getValue()).toString();
                    String userProfileImage = null;
                    if (dataSnapshot.child("profileimage").getValue() == null) {
                        Picasso.get().load(R.drawable.male_profile).into(profileImage);
                    } else {
                        userProfileImage = Objects.requireNonNull(dataSnapshot.child("profileimage").getValue()).toString();
                        Picasso.get().load(userProfileImage).placeholder(R.drawable.male_profile).into(profileImage);
                    }
                    profileDisplayName.setText(userDisplayName);
                    profileUserName.setText("(@" + userName + ")");
                    profileStatus.setText(userStatus);
                    setPostCount();
                    setLikesCount();
                    setFollowersCount();

                    editor.putString("USER_NAME",userDisplayName);
                    editor.putString("USER_PROFILE_IMAGE",userProfileImage);


                    if (!CURRENT_USER_PROFILE)
                    {
                        maintainRequestBtnSession();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setFollowersCount() {

        mFollowsRef.child(currentUserId).addValueEventListener(new ValueEventListener() {

            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {
                    followCount = (int) dataSnapshot.getChildrenCount();
                    profileNoOfFollowers.setText(String.valueOf(followCount));
                    Log.d("Followers", Integer.toString(followCount));
                    if(followCount != 0)
                    {
                        setFollowersCountVariable(followCount);
                        Log.d("Followers", Integer.toString(followCount));

                    }
                }
                else {
                    followCount = 0;
                    profileNoOfFollowers.setText(String.valueOf(followCount));
                    Log.d("Followers", Integer.toString(followCount));
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        Log.d("Inset", Integer.toString(followCount));
    }

    private void setLikesCount() {

        mLikesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snap : dataSnapshot.getChildren()) {
                    if (snap.hasChildren())
                    {
                        String str =snap.getKey();
                        if (str != null && str.contains(currentUserId))
                        {
                            Log.e(snap.getKey(), snap.getChildrenCount() + "");
                            likesCount += snap.getChildrenCount();
                        }
                    }
                    else
                        {
                        Log.d("Likes", "0 Likes");
                        }
                }
                profileNoOfLikes.setText(Integer.toString(likesCount));
                setLikesCountVariable(likesCount);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void maintainRequestBtnSession() {
        mFollowReqRef.child(senderID).addListenerForSingleValueEvent(new ValueEventListener() {

            @SuppressLint("SetTextI18n")
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(receiverID)) {
                    String request_type = Objects.requireNonNull(dataSnapshot.child(receiverID).child("request_type").getValue()).toString();
                    if (request_type.equals("sent")) {
                        profileFollowBtn.setEnabled(true);
                        CURRENT_STATE = "request_sent";
                        profileFollowBtn.setText("Cancel Request");
                        profileFollowBtn.setBackgroundResource(R.drawable.button_rounded_border_blue);
                        profileFollowBtn.setTextColor(getColor(R.color.colorPrimaryDark));
                    }
                    else if (request_type.equals("received")) {
                        CURRENT_STATE = "request_received";
                        profileFollowBtn.setText("ACCEPT REQUEST");
                    }
                }
                else {
                    mFollowsRef.child(senderID).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.hasChild(receiverID)) {
                                CURRENT_STATE = "friends";
                                profileFollowBtn.setText("UNFOLLOW");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setPostCount() {
        Query myPostQuery = mPostsRef.orderByChild("uid").startAt(currentUserId).endAt(currentUserId + "\uf8ff");
        myPostQuery.addValueEventListener(new ValueEventListener() {

            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postCount = (int) dataSnapshot.getChildrenCount();
                profileNoOfPost.setText(Integer.toString(postCount));
                if(postCount != 0)
                {
                    setPostCountVariable(postCount);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void loadFragments() {

        FragmentManager fragmentManager = getSupportFragmentManager();
        ProfilePostFragment profilePostFragment = new ProfilePostFragment(currentUserId);

        fragmentManager.beginTransaction()
                .add(R.id.profile_post_container, profilePostFragment)
                .commit();


    }

    public void setFollowersCountVariable(int followCount) {
        this.followCount = followCount;
        editor.putString("USER_PROFILE_FOLLOWERS_COUNT",Integer.toString(followCount));
        Log.d("Check", String.valueOf(followCount));
        editor.apply();
    }


    public void setPostCountVariable(int postCount) {
        this.postCount = postCount;
        editor.putString("USER_PROFILE_POST_COUNT",Integer.toString(postCount));
        Log.d("Check", String.valueOf(postCount));
        editor.apply();


    }

    public void setLikesCountVariable(int likesCount) {
        this.likesCount = likesCount;
        editor.putString("USER_PROFILE_LIKES_COUNT",Integer.toString(likesCount));
        Log.d("Check", String.valueOf(likesCount));
        editor.apply();
    }
}
