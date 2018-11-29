package com.example.dell.travalong;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class PostDetailActivity extends AppCompatActivity {

    @BindView(R.id.delete_post_btn)Button deletePostBtn;
    @BindView(R.id.post_like_btn)Button likePostBtn;
    @BindView(R.id.post_user_name)TextView postUsername;
    @BindView(R.id.post_description)TextView postDescription;
    @BindView(R.id.post_time)TextView postTime;
    @BindView(R.id.post_date)TextView postDate;
    @BindView(R.id.post_likes)TextView postNoOfLikes;
    @BindView(R.id.post_display_image)ImageView postImage;
    @BindView(R.id.profile_photo)CircleImageView profilePhoto;

    private boolean like_status = false;
    int likesCount;

    private FirebaseAuth mAuth;
    private DatabaseReference mPostsRef, mClickPostRef,mLikesRef;
    private String currentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);
        ButterKnife.bind(this);

        final String postKey = this.getIntent().getStringExtra("key");
        mClickPostRef = FirebaseDatabase.getInstance().getReference().child(getString(R.string.child_posts)).child(postKey);
        mLikesRef = FirebaseDatabase.getInstance().getReference().child(getString(R.string.child_likes));

        mAuth = FirebaseAuth.getInstance();
        currentUserID = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        mPostsRef = FirebaseDatabase.getInstance().getReference().child(getString(R.string.child_posts));
        mPostsRef.keepSynced(true);

        setLikesCount(postKey);
        DisplayUserPost(postKey);

        likePostBtn.setOnClickListener(view -> likeCurrentPost(postKey));


        deletePostBtn.setOnClickListener(view -> deleteCurrentPost());

    }

    private void setLikesCount(final String postKey) {

        mLikesRef.addValueEventListener(new ValueEventListener() {

            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.child(postKey).hasChild(currentUserID)) {
                    likesCount = (int) dataSnapshot.child(postKey).getChildrenCount();
                    postNoOfLikes.setText(Integer.toString(likesCount) + getString(R.string.likes_label));
                    likePostBtn.setText(getString(R.string.value_liked));
                } else {
                    likesCount = (int) dataSnapshot.child(postKey).getChildrenCount();
                    postNoOfLikes.setText(Integer.toString(likesCount) + getString(R.string.likes_label));
                    likePostBtn.setText(getString(R.string.value_like));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void likeCurrentPost(final String postKey) {

        like_status = true;
        mLikesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (like_status) {
                    if (postKey != null) {
                        if (dataSnapshot.child(postKey).hasChild(currentUserID)) {
                            mLikesRef.child(postKey).child(currentUserID).removeValue();
                            like_status = false;

                        } else {
                            mLikesRef.child(postKey).child(currentUserID).setValue(true);
                            like_status = false;
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void deleteCurrentPost() {

        mClickPostRef.removeValue();
        sendUserToProfileActivity();
        Toast.makeText(PostDetailActivity.this, R.string.post_deleted_message,
                Toast.LENGTH_SHORT).show();
    }

    private void sendUserToProfileActivity() {
        Intent callProfileAct = new Intent(this, UserProfileActivity.class);
        startActivity(callProfileAct);
    }


    private void DisplayUserPost(String postKey) {
        mPostsRef.child(postKey).addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String userDisplayName = Objects.requireNonNull(dataSnapshot.child(getString(R.string.child_full_name)).getValue()).toString();
                    String userProfileImage = Objects.requireNonNull(dataSnapshot.child(getString(R.string.child_profile_image)).getValue()).toString();
                    String userPostImage = Objects.requireNonNull(dataSnapshot.child(getString(R.string.child_post_image)).getValue()).toString();
                    String userPostDate = Objects.requireNonNull(dataSnapshot.child(getString(R.string.child_date)).getValue()).toString();
                    String userPostTime = Objects.requireNonNull(dataSnapshot.child(getString(R.string.child_time)).getValue()).toString();
                    String userPostDescription = Objects.requireNonNull(dataSnapshot.child(getString(R.string.child_description)).getValue()).toString();

                    postUsername.setText(userDisplayName);
                    if(userProfileImage.equals("none"))
                    {
                        Picasso.get().load(R.drawable.male_profile).into(profilePhoto);
                    }
                    else
                    {
                        Picasso.get().load(userProfileImage).into(profilePhoto);
                    }
                    Picasso.get().load(userPostImage).into(postImage);
                    postDate.setText(userPostDate);
                    postDescription.setText(userPostDescription);
                    postTime.setText(userPostTime);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.go_down,R.anim.go_up);
    }
}
