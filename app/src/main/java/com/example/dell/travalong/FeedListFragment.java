package com.example.dell.travalong;

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
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class FeedListFragment extends Fragment {


    private RecyclerView postList;
    private FirebaseAuth mAuth;
    private DatabaseReference mPostsRef;
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

        postList = rootView.findViewById(R.id.users_post_list);
        //postList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(rootView.getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        postList.setLayoutManager(linearLayoutManager);

        DisplayAllUsersPosts();

        return rootView;
    }

    private void DisplayAllUsersPosts()
    {
        FirebaseRecyclerAdapter<Post, PostViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Post, PostViewHolder>
                        (
                                Post.class,
                                R.layout.activity_home_post_item,
                                PostViewHolder.class,
                                mPostsRef
                        )
                {
                    protected void populateViewHolder(PostViewHolder viewHolder, Post model, int position)
                    {
                        // final String postKey = getRef(position).getKey();

                        if(model.getFullName() != null) {
                            Log.d("Username", model.getFullName());
                            viewHolder.setFullName(model.getFullName());
                        }
                            viewHolder.setTime(model.getTime());
                            viewHolder.setDate(model.getDate());
                            viewHolder.setDescription(model.getDescription());
                            viewHolder.setProfileImage(model.getProfileImage());
                            viewHolder.setPostImage(model.getPostImage());

//                            viewHolder.mView.setOnClickListener(new View.OnClickListener() {
//                                @Override
//                                public void onClick(View view) {
//                                    callPostDetailActivity();
//                                }
//                            });
                    }
                };
        postList.setAdapter(firebaseRecyclerAdapter);
    }

//    private void callPostDetailActivity() {
//        Intent callPostDetailAct = new Intent(getContext(),PostDetailActivity.class);
//        startActivity(callPostDetailAct);
//    }

    public static class PostViewHolder extends RecyclerView.ViewHolder
    {
        View mView;

        public PostViewHolder(View itemView)
        {
            super(itemView);
            mView = itemView;
        }

        public void setFullName(String full_name)
        {
            TextView username = (TextView) mView.findViewById(R.id.post_user_name);
            if(username != null)
            {
                Log.d("Username",full_name);
                username.setText(full_name);
            }
        }


        public void setProfileImage(String profileimage)
        {
            CircleImageView image = (CircleImageView) mView.findViewById(R.id.profile_photo);
            Picasso.get().load(profileimage).into(image);
        }

        public void setTime(String time)
        {
            TextView PostTime = (TextView) mView.findViewById(R.id.post_time);
            PostTime.setText(time);
        }

        public void setDate(String date)
        {
            TextView PostDate = (TextView) mView.findViewById(R.id.post_date);
            PostDate.setText(date);
        }

        public void setDescription(String description)
        {
            TextView PostDescription = (TextView) mView.findViewById(R.id.post_description);
            PostDescription.setText(description);
        }

        public void setPostImage(String postimage)
        {
            ImageView PostImage = (ImageView) mView.findViewById(R.id.post_display_image);
            Picasso.get().load(postimage).into(PostImage);
        }
    }

}


