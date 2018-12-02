package com.example.dell.travalong;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
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
    private String CURRENT_STATE;
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
        CURRENT_STATE = getString(R.string.not_friends_label);
        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        mUserRef = FirebaseDatabase.getInstance().getReference().child(getString(R.string.child_users));
        mPostsRef = FirebaseDatabase.getInstance().getReference().child(getString(R.string.child_posts));
        mFollowReqRef = FirebaseDatabase.getInstance().getReference().child(getString(R.string.child_follow_req));
        mFollowsRef = FirebaseDatabase.getInstance().getReference().child(getString(R.string.child_followers));
        mLikesRef = FirebaseDatabase.getInstance().getReference().child(getString(R.string.child_likes));

        mAuth = FirebaseAuth.getInstance();
        currentUserId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

        if (getIntent().hasExtra(getString(R.string.receiver_id_key)))
        {
            CURRENT_USER_PROFILE = false;

            senderID = currentUserId;
            receiverID = getIntent().getStringExtra(getString(R.string.receiver_id_key));

            if (!senderID.equals(receiverID)) {
                currentUserId = receiverID;
                profileFollowBtn.setVisibility(View.VISIBLE);
                profileFollowBtn.setOnClickListener(view -> {
                    profileFollowBtn.setEnabled(false);
                    if (CURRENT_STATE.equals(getString(R.string.not_friends_label))) {
                        sendFriendRequestToPerson();
                    }
                    if (CURRENT_STATE.equals(getString(R.string.request_sent_label))) {
                        cancelFriendRequest();
                    }
                    if (CURRENT_STATE.equals(getString(R.string.request_received_label))) {
                        acceptFollowRequest();
                    }
                    if (CURRENT_STATE.equals(getString(R.string.friends_label))) {
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
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        mFollowsRef.child(receiverID).child(senderID)
                                .removeValue()
                                .addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        profileFollowBtn.setEnabled(true);
                                        CURRENT_STATE = getString(R.string.not_friends_label);
                                        profileFollowBtn.setText(R.string.follow_btn_label);
                                        profileFollowBtn.setBackgroundResource(R.drawable.button_rounded_blue);
                                        profileFollowBtn.setTextColor(getColor(R.color.white));
                                    }
                                });
                    }
                });
    }

    private void acceptFollowRequest() {
        Calendar calFordDate = Calendar.getInstance();
        @SuppressLint("SimpleDateFormat") SimpleDateFormat currentDate = new SimpleDateFormat(getString(R.string.date_format));
        saveCurrentDate = currentDate.format(calFordDate.getTime());

        mFollowsRef.child(senderID).child(receiverID).child(getString(R.string.child_date)).setValue(saveCurrentDate)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        mFollowsRef.child(receiverID).child(senderID).child(getString(R.string.child_date)).setValue(saveCurrentDate)
                                .addOnCompleteListener(task13 -> {
                                    if (task13.isSuccessful()) {
                                        mFollowReqRef.child(senderID).child(receiverID)
                                                .removeValue()
                                                .addOnCompleteListener(task12 -> {
                                                    if (task12.isSuccessful()) {
                                                        mFollowReqRef.child(receiverID).child(senderID)
                                                                .removeValue()
                                                                .addOnCompleteListener(task1 -> {
                                                                    if (task1.isSuccessful()) {
                                                                        profileFollowBtn.setEnabled(true);
                                                                        CURRENT_STATE = getString(R.string.friends_label);
                                                                        profileFollowBtn.setText(R.string.unfollow_btn_label);
                                                                        profileFollowBtn.setBackgroundResource(R.drawable.button_rounded_border_blue);
                                                                        profileFollowBtn.setTextColor(getColor(R.color.colorPrimaryDark));
                                                                    }
                                                                });
                                                    }
                                                });
                                    }
                                });
                    }
                });
    }

    private void cancelFriendRequest() {
        mFollowReqRef.child(senderID).child(receiverID)
                .removeValue()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        mFollowReqRef.child(receiverID).child(senderID)
                                .removeValue()
                                .addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        profileFollowBtn.setEnabled(true);
                                        CURRENT_STATE = getString(R.string.not_friends_label);
                                        profileFollowBtn.setText(getString(R.string.follow_btn_label));
                                        profileFollowBtn.setBackgroundResource(R.drawable.button_rounded_blue);
                                        profileFollowBtn.setTextColor(getColor(R.color.white));
                                    }
                                });
                    }
                });
    }

    private void sendFriendRequestToPerson() {

        mFollowReqRef.child(senderID).child(receiverID)
                .child(getString(R.string.request_type_label)).setValue(getString(R.string.sent_label))
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        mFollowReqRef.child(receiverID).child(senderID)
                                .child(getString(R.string.request_type_label)).setValue(getString(R.string.received_label))
                                .addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        profileFollowBtn.setEnabled(true);
                                        CURRENT_STATE = getString(R.string.request_sent_label);
                                        profileFollowBtn.setText(R.string.cancel_request_btn_label);
                                        profileFollowBtn.setBackgroundResource(R.drawable.button_rounded_border_blue);
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                            profileFollowBtn.setTextColor(getColor(R.color.colorPrimaryDark));
                                        }
                                    }
                                });
                    }
                });
    }

    @SuppressLint("CommitPrefEdits")
    private void loadProfileDetails() {

        sharedPreferences = getSharedPreferences(getString(R.string.profile_act_pref_key),MODE_PRIVATE);
        editor = sharedPreferences.edit();

        mUserRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                   new LoadProfileTask().execute(dataSnapshot);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @SuppressLint("StaticFieldLeak")
    private class LoadProfileTask extends AsyncTask<DataSnapshot, Void, String> {

        String userDisplayName, userName, userStatus, userProfileImage;

        @Override
        protected String doInBackground(DataSnapshot... params) {

            userDisplayName = Objects.requireNonNull(params[0].child(getString(R.string.child_full_name)).getValue()).toString();
                    userName = Objects.requireNonNull(params[0].child(getString(R.string.child_username)).getValue()).toString();
                    userStatus = Objects.requireNonNull(params[0].child(getString(R.string.child_status)).getValue()).toString();
                    userProfileImage = null;

                    if (params[0].child(getString(R.string.child_profile_image)).getValue() == null)
                    {
                        Picasso.get().load(R.drawable.male_profile).into(profileImage);
                    }
                    else {
                        userProfileImage = Objects.requireNonNull(params[0].child(getString(R.string.child_profile_image)).getValue()).toString();
                    }



                    if (!CURRENT_USER_PROFILE)
                    {
                        maintainRequestBtnSession();
                    }

            return getString(R.string.execute_message);
        }

        @Override
        protected void onPostExecute(String result) {
            profileDisplayName.setText(userDisplayName);
            profileUserName.setText("(@" + userName + ")");
            profileStatus.setText(userStatus);
            Picasso.get().load(userProfileImage).placeholder(R.drawable.male_profile).into(profileImage);

            editor.putString(getString(R.string.user_name_pref_key),userDisplayName);
            editor.putString(getString(R.string.profile_image_pref_key),userProfileImage);

            setPostCount();
            setLikesCount();
            setFollowersCount();
            Toast.makeText(getBaseContext(), R.string.post_execute_message, Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onProgressUpdate(Void... values) {}
    }

    private void setFollowersCount() {

        mFollowsRef.child(currentUserId).addValueEventListener(new ValueEventListener() {

            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {
                    followCount = (int) dataSnapshot.getChildrenCount();
                    profileNoOfFollowers.setText(String.valueOf(followCount));
                    if(followCount != 0)
                    {
                        setFollowersCountVariable(followCount);
                    }
                }
                else {
                    followCount = 0;
                    profileNoOfFollowers.setText(String.valueOf(followCount));
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


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
                    String request_type = Objects.requireNonNull(dataSnapshot.child(receiverID).child(getString(R.string.request_type_label)).getValue()).toString();
                    if (request_type.equals(getString(R.string.sent_label))) {
                        profileFollowBtn.setEnabled(true);
                        CURRENT_STATE = getString(R.string.request_sent_label);
                        profileFollowBtn.setText(getString(R.string.cancel_request_btn_label));
                        profileFollowBtn.setBackgroundResource(R.drawable.button_rounded_border_blue);
                        profileFollowBtn.setTextColor(getColor(R.color.colorPrimaryDark));
                    }
                    else if (request_type.equals(getString(R.string.received_label))) {
                        CURRENT_STATE = getString(R.string.request_received_label);
                        profileFollowBtn.setText(R.string.accept_request_btn_label);
                    }
                }
                else {
                    mFollowsRef.child(senderID).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.hasChild(receiverID)) {
                                CURRENT_STATE = getString(R.string.friends_label);
                                profileFollowBtn.setText(getString(R.string.unfollow_btn_label));
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
        Query myPostQuery = mPostsRef.orderByChild(getString(R.string.child_uid)).startAt(currentUserId).endAt(currentUserId + "\uf8ff");
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
        editor.putString(getString(R.string.profile_followers_count_pref_key),Integer.toString(followCount));
        editor.apply();
    }


    public void setPostCountVariable(int postCount) {
        this.postCount = postCount;
        editor.putString(getString(R.string.profile_post_count_pref_key),Integer.toString(postCount));
        editor.apply();
    }

    public void setLikesCountVariable(int likesCount) {
        this.likesCount = likesCount;
        editor.putString(getString(R.string.profile_likes_count_pref_key),Integer.toString(likesCount));
        editor.apply();
    }
}
