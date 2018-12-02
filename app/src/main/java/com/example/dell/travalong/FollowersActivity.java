package com.example.dell.travalong;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class FollowersActivity extends AppCompatActivity {

    @BindView(R.id.search_follower)EditText searchFollowerEditText;
    @BindView(R.id.followers_list)RecyclerView followersList;
    @BindView(R.id.find_followers_btn)Button findFollowersBtn;

    private FirebaseAuth mAuth;
    private String currentUserID;
    private int noOfUsers;
    private DatabaseReference allUsersRef, mFollowReqRef, mFollowsRef;
    private ArrayList<String> CURRENT_STATE;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = item -> {
                switch (item.getItemId()) {

                    case R.id.navigation_home:
                        Intent callHomeAct = new Intent(FollowersActivity.this, HomeActivity.class);
                        startActivity(callHomeAct);
                        return true;

                    case R.id.navigation_followers:
                        Intent intent = new Intent(FollowersActivity.this, FollowersActivity.class);
                        startActivity(intent);
                        return true;

                    case R.id.navigation_profile:
                        Intent callProfileAct = new Intent(FollowersActivity.this, UserProfileActivity.class);
                        startActivity(callProfileAct);
                        return true;
                }
                return false;
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_followers);
        ButterKnife.bind(this);
        followersList.setLayoutManager(new LinearLayoutManager(this));

        mAuth = FirebaseAuth.getInstance();
        currentUserID = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        allUsersRef = FirebaseDatabase.getInstance().getReference().child(getString(R.string.child_users));
        mFollowReqRef = FirebaseDatabase.getInstance().getReference().child(getString(R.string.child_follow_req));
        mFollowsRef = FirebaseDatabase.getInstance().getReference().child(getString(R.string.child_followers));


        allUsersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()) {
                    noOfUsers = (int) dataSnapshot.getChildrenCount();
                    String[] arr = new String[noOfUsers];
                    CURRENT_STATE = new ArrayList<>(Arrays.asList(arr));
                    Collections.fill(CURRENT_STATE, getString(R.string.label_not_friends));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        searchUserAndFriends("");


        findFollowersBtn.setOnClickListener(view -> {
            String searchInboxInput = searchFollowerEditText.getText().toString();
            searchUserAndFriends(searchInboxInput);
        });


        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
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

    private void searchUserAndFriends(String searchInboxInput) {

        Query searchPeopleQuery;

        if (searchInboxInput.equals(""))
        {
            searchPeopleQuery = allUsersRef;
        }
        else
        {
            searchPeopleQuery = allUsersRef.orderByChild(getString(R.string.label_full_name))
                    .startAt(searchInboxInput).endAt(searchInboxInput + "\uf8ff");
        }

        FirebaseRecyclerAdapter<FindFollowers, FindFollowersViewHolder> firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<FindFollowers, FindFollowersViewHolder>
                (
                        FindFollowers.class,
                        R.layout.activity_followers_user_item,
                        FindFollowersViewHolder.class,
                        searchPeopleQuery
                )
        {
            @Override
            protected void populateViewHolder(FindFollowersViewHolder viewHolder, final FindFollowers model, final int position) {

                viewHolder.setFull_name(model.getFull_name());
                viewHolder.setProfileImage(model.getProfileImage());
                viewHolder.setStatus(model.getStatus());

                maintainRequestBtnSession(getRef(position).getKey(),position, viewHolder.mView.findViewById(R.id.activity_followers_btn));

                viewHolder.mView.setOnClickListener(view -> {
                    String receiverId = getRef(position).getKey();
                    callProfileActivity(receiverId);
                });


            }
        };
        followersList.setAdapter(firebaseRecyclerAdapter);

    }



    private void maintainRequestBtnSession(final String receiverID, final int position, final Button followBtn) {

        mFollowsRef.child(currentUserID).addListenerForSingleValueEvent(new ValueEventListener() {

            @SuppressLint("SetTextI18n")
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.hasChild(receiverID)) {
                    followBtn.setVisibility(View.VISIBLE);
                    followBtn.setText(R.string.label_followed);
                    followBtn.setBackgroundResource(R.drawable.button_rounded_border_blue);
                    followBtn.setTextColor(getColor(R.color.colorPrimaryDark));
                } else {
                    followBtn.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }

        });
    }

    private void callProfileActivity(String uid) {
        Intent callProfileAct = new Intent(this, UserProfileActivity.class);
        callProfileAct.putExtra("receiver_id", uid);
        startActivity(callProfileAct);
    }

    public static class FindFollowersViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.activity_followers_btn) Button followBtn;
        @BindView(R.id.followers_user_name) TextView followerName;
        @BindView(R.id.profile_photo) CircleImageView profilePhoto;
        @BindView(R.id.user_status) TextView followerStatus;

        View mView;


        public FindFollowersViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            ButterKnife.bind(this,mView);
        }

        public void setFull_name(String full_name) {

            followerName.setText(full_name);
        }

        public void setProfileImage(String profileimage) {

            Picasso.get().load(profileimage).placeholder(R.drawable.male_profile).into(profilePhoto);
        }

        public void setStatus(String status) {
            followerStatus.setText(status);
        }

    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putStringArrayList(getString(R.string.label_list),CURRENT_STATE);
    }
}
