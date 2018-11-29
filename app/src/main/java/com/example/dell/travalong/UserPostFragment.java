package com.example.dell.travalong;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;

public class UserPostFragment extends Fragment {

    @BindView(R.id.user_create_post) EditText editTextPostDescription;
    @BindView(R.id.profile_photo) CircleImageView profilePhoto;
    @BindView(R.id.post_btn) Button postBtn;
    @BindView(R.id.upload_photo_btn) Button uploadPhotoBtn;

    private static final int Gallery_Pick = 1;
    private Uri ImageUri;

    private ProgressDialog loadingBar;

    private StorageReference mPostsImageStorageRef;
    private DatabaseReference mUserRef, mPostRef;
    private FirebaseAuth mAuth;
    private String saveCurrentDate, saveCurrentTime, postRandomName, downloadUrl, currentUserId,username,userProfilePhoto,description;



    public UserPostFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.activity_home_create_post, container, false);
        ButterKnife.bind(this,rootView);

        loadingBar = new ProgressDialog(rootView.getContext());

        mAuth = FirebaseAuth.getInstance();
        currentUserId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

        mPostsImageStorageRef = FirebaseStorage.getInstance().getReference();
        mUserRef = FirebaseDatabase.getInstance().getReference().child(getString(R.string.child_users));
        mPostRef = FirebaseDatabase.getInstance().getReference().child(getString(R.string.child_posts));
        editTextPostDescription.setHint("Hi ," + username + "! How are you feeling today!");
        Picasso.get().load(userProfilePhoto).placeholder(R.drawable.male_profile).into(profilePhoto);

        uploadPhotoBtn.setOnClickListener(v -> OpenGallery());


        postBtn.setOnClickListener(v -> ValidatePostInfo());

        return rootView;
    }

    private void ValidatePostInfo() {
        description = editTextPostDescription.getText().toString();

        if (ImageUri == null) {
            Toast.makeText(getContext(), R.string.select_post_image_message, Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(description)) {
            Toast.makeText(getContext(), R.string.image_more_info_message, Toast.LENGTH_SHORT).show();
        } else {
            loadingBar.setTitle("Add New Post");
            loadingBar.setMessage(getString(R.string.loading_post_message));
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);

            StoringImageToFirebaseStorage();
        }
    }

    private void StoringImageToFirebaseStorage() {
        Calendar calFordDate = Calendar.getInstance();
        @SuppressLint("SimpleDateFormat") SimpleDateFormat currentDate = new SimpleDateFormat(getString(R.string.date_format));
        saveCurrentDate = currentDate.format(calFordDate.getTime());

        Calendar calFordTime = Calendar.getInstance();
        @SuppressLint("SimpleDateFormat") SimpleDateFormat currentTime = new SimpleDateFormat(getString(R.string.time_format));
        saveCurrentTime = currentTime.format(calFordDate.getTime());

        postRandomName = saveCurrentDate + saveCurrentTime;


        final StorageReference filePath = mPostsImageStorageRef.child(getString(R.string.storage_child_post_images)).child(ImageUri.getLastPathSegment() + postRandomName + ".jpg");


        filePath.putFile(ImageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    Toast.makeText(getContext(), R.string.uploaded_message, Toast.LENGTH_SHORT).show();
                    filePath.getDownloadUrl().addOnSuccessListener(uri -> {
                        downloadUrl = uri.toString();

                        Log.d("TAG", downloadUrl);
                        SavingPostInformationToDatabase();
                    });

                })
                .addOnFailureListener(e -> {
                    loadingBar.dismiss();
                    Toast.makeText(getContext(), getString(R.string.failed_message) + e.getMessage(), Toast.LENGTH_SHORT).show();
                })
                .addOnProgressListener(taskSnapshot -> {
                    double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot
                            .getTotalByteCount());
                    loadingBar.setMessage(R.string.uploaded_message + (int) progress + "%");
                });
    }




    private void SavingPostInformationToDatabase()
    {
        mUserRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists())
                {
                    String userFullName = Objects.requireNonNull(dataSnapshot.child("full_name").getValue()).toString();
                    String userProfileImage;

                    if(dataSnapshot.child(getString(R.string.child_profile_image)).getValue() == null)
                    {

                        userProfileImage = "none";
                    }
                    else
                    {
                        userProfileImage = Objects.requireNonNull(dataSnapshot.child("profileimage").getValue()).toString();
                    }

                    HashMap postsMap = new HashMap();
                    postsMap.put(getString(R.string.key_uid), currentUserId);
                    postsMap.put(getString(R.string.key_date), saveCurrentDate);
                    postsMap.put(getString(R.string.key_time), saveCurrentTime);
                    postsMap.put(getString(R.string.key_description), description);
                    postsMap.put(getString(R.string.key_postimage), downloadUrl);
                    postsMap.put(getString(R.string.key_profileimage), userProfileImage);
                    postsMap.put(getString(R.string.key_full_name), userFullName);

                    mPostRef.child(currentUserId + postRandomName).updateChildren(postsMap)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    selfCallHomeActivity();
                                    Toast.makeText(getContext(), R.string.post_update_success_message, Toast.LENGTH_SHORT).show();
                                    loadingBar.dismiss();
                                } else {
                                    Toast.makeText(getContext(), R.string.post_update_error_message, Toast.LENGTH_SHORT).show();
                                    loadingBar.dismiss();
                                }
                            });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void selfCallHomeActivity() {
        Intent callHomeAct = new Intent(getContext(), HomeActivity.class);
        startActivity(callHomeAct);
    }


    private void OpenGallery() {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, Gallery_Pick);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Gallery_Pick && resultCode == RESULT_OK && data != null) {
            ImageUri = data.getData();
            profilePhoto.setImageURI(ImageUri);
        }
    }

    public void setUserName(String username) {
        this.username = username;
    }

    public void setUserProfilePhoto(String userProfilePhoto) {
        this.userProfilePhoto = userProfilePhoto;
    }


}
