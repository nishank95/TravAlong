package com.example.dell.travalong;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
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

public class FeedListFragment extends Fragment {


    private RecyclerView postList;
    private FirebaseAuth mAuth;
    private DatabaseReference mPostsRef;
    private DatabaseReference mLikesRef;
    private boolean like_status = false;
    private String currentUserID;

    public FeedListFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.activity_home_users_post_list, container, false);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        mPostsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        mLikesRef = FirebaseDatabase.getInstance().getReference().child("Likes");

        postList = rootView.findViewById(R.id.users_post_list);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(rootView.getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        postList.setLayoutManager(linearLayoutManager);

        displayAllUsersPosts();

        return rootView;
    }

    private void displayAllUsersPosts() {
        FirebaseRecyclerAdapter<Post, PostViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Post, PostViewHolder>
                        (
                                Post.class,
                                R.layout.activity_home_post_item,
                                PostViewHolder.class,
                                mPostsRef
                        ) {
                    protected void populateViewHolder(PostViewHolder viewHolder, final Post model, int position) {
                        final String postKey = getRef(position).getKey();

                        if (model.getFull_name() != null)
                        {
                            Log.d("Username", model.getFull_name());
                            viewHolder.setFull_name(model.getFull_name());
                        }
                        viewHolder.setTime(model.getTime());
                        viewHolder.setDate(model.getDate());
                        viewHolder.setDescription(model.getDescription());
                        viewHolder.setProfileImage(model.getProfileImage());
                        viewHolder.setPostImage(model.getPostImage());
                        viewHolder.setLikesCount(postKey);


                        viewHolder.postLikeBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
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
                        });


                        viewHolder.profileViewBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                callProfileActivity(model.getUid());
                            }
                        });



                    }
                };
        postList.setAdapter(firebaseRecyclerAdapter);
    }

    private void callProfileActivity(String uid) {
        Intent callProfileAct = new Intent(getContext(),UserProfileActivity.class);
        callProfileAct.putExtra("receiver_id",uid);
        startActivity(callProfileAct);
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {
        View mView;

        int likesCount;
        String currentUserId;
        DatabaseReference likesRef;

        @BindView(R.id.post_like_btn) Button postLikeBtn;
        @BindView(R.id.view_profile_btn) Button profileViewBtn;
        @BindView(R.id.post_likes) TextView postNoOfLikes;

        public PostViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            ButterKnife.bind(this,mView);

            likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
            currentUserId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

        }

        public void setFull_name(String full_name) {
            TextView username = (TextView) mView.findViewById(R.id.post_user_name);
            if (username != null) {
                Log.d("Username", full_name);
                username.setText(full_name);
            }
        }


        public void setProfileImage(String profileimage) {
            CircleImageView profilePhoto = (CircleImageView) mView.findViewById(R.id.profile_photo);
            if(profileimage.equals("none"))
            {
                Picasso.get().load(R.drawable.male_profile).into(profilePhoto);
            }
            else
            {
                Picasso.get().load(profileimage).into(profilePhoto);
            }
        }

        public void setTime(String time) {
            TextView postTime = (TextView) mView.findViewById(R.id.post_time);
            postTime.setText(time);
        }

        public void setDate(String date) {
            TextView postDate = (TextView) mView.findViewById(R.id.post_date);
            postDate.setText(date);
        }

        public void setDescription(String description) {
            TextView postDescription = (TextView) mView.findViewById(R.id.post_description);
            postDescription.setText(description);
        }

        public void setPostImage(String postimage) {
            ImageView postImage = (ImageView) mView.findViewById(R.id.post_display_image);
            Picasso.get().load(postimage).into(postImage);
        }

        public void setLikesCount(final String postKey) {
            likesRef.addValueEventListener(new ValueEventListener() {

                @SuppressLint("SetTextI18n")
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.child(postKey).hasChild(currentUserId)) {
                        likesCount = (int) dataSnapshot.child(postKey).getChildrenCount();
                        postNoOfLikes.setText(Integer.toString(likesCount) + " Likes");
                        postLikeBtn.setText("Liked!");
                    }
                    else
                    {
                        likesCount = (int) dataSnapshot.child(postKey).getChildrenCount();
                        postNoOfLikes.setText(Integer.toString(likesCount) + " Likes");
                        postLikeBtn.setText("Like");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

}


