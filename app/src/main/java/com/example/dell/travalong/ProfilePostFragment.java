package com.example.dell.travalong;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
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
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfilePostFragment extends Fragment{

    private RecyclerView profilePostList;
    private FirebaseAuth mAuth;
    private DatabaseReference mPostsRef;
    private String currentUserID;

    @SuppressLint("ValidFragment")
    ProfilePostFragment(){

    }

    @SuppressLint("ValidFragment")
    ProfilePostFragment(String currentUserID){
        this.currentUserID = currentUserID;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.activity_profile_post_list, container, false);

        profilePostList = (RecyclerView) rootView.findViewById(R.id.profile_post_list);
        profilePostList.setLayoutManager(new GridLayoutManager(getActivity(),3));


        mAuth = FirebaseAuth.getInstance();
        mPostsRef = FirebaseDatabase.getInstance().getReference().child(getString(R.string.child_posts));

        mPostsRef.keepSynced(true);
        DisplayUserPosts();
        return rootView;
    }

    private void DisplayUserPosts() {

        Query myPostQuery = mPostsRef.orderByChild("uid").startAt(currentUserID).endAt(currentUserID + "\uf8ff");

        FirebaseRecyclerAdapter<Post, ProfilePostFragment.PostViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Post, ProfilePostFragment.PostViewHolder>
                        (
                                Post.class,
                                R.layout.activity_profile_post_item,
                                ProfilePostFragment.PostViewHolder.class,
                                myPostQuery
                        )
                {
                    protected void populateViewHolder(ProfilePostFragment.PostViewHolder viewHolder, Post model, int position)
                    {
                        final String postKey = getRef(position).getKey();
                        if (postKey != null && postKey.contains(currentUserID)) {

                            viewHolder.setPostImage(model.getPostImage());
                        }

                            viewHolder.mView.setOnClickListener(view -> callPostDetailActivity(postKey));

                    }
                };
        profilePostList.setAdapter(firebaseRecyclerAdapter);


    }

    private void callPostDetailActivity(String postKey) {
        Intent callPostDetailAct = new Intent(getContext(),PostDetailActivity.class);
        callPostDetailAct.putExtra("key",postKey);
        startActivity(callPostDetailAct);
        Objects.requireNonNull(getActivity()).overridePendingTransition(R.anim.go_up,R.anim.go_down);

    }


    public static class PostViewHolder extends RecyclerView.ViewHolder
    {
        View mView;

        public PostViewHolder(View itemView)
        {
            super(itemView);
            mView = itemView;
        }



        public void setPostImage(String postimage)
        {
            ImageView PostImage = (ImageView) mView.findViewById(R.id.profile_post_image);
            Picasso.get().load(postimage).into(PostImage);
        }
    }

}

