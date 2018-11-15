package com.example.dell.travalong;

import android.annotation.SuppressLint;
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

import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;

public class UserPostFragment extends Fragment {

    private String username = "";
    private String userProfilePhoto;
    private EditText editTextPostDescription;
    private CircleImageView profilePhoto;
    private Button postBtn, uploadPhotoBtn;
    private static final int Gallery_Pick = 1;
    private Uri ImageUri;
    private String Description;
    private ProgressDialog loadingBar;

    private StorageReference mPostsImageStorageRef;
    private DatabaseReference mUserRef, mPostRef;
    private FirebaseAuth mAuth;
    private String saveCurrentDate, saveCurrentTime, postRandomName, downloadUrl, currentUserId;


    public UserPostFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.activity_home_create_post, container, false);
        editTextPostDescription = (EditText) rootView.findViewById(R.id.user_create_post);
        profilePhoto = (CircleImageView) rootView.findViewById(R.id.profile_photo);
        postBtn = (Button) rootView.findViewById(R.id.post_btn);
        uploadPhotoBtn = (Button) rootView.findViewById(R.id.upload_photo_btn);
        loadingBar = new ProgressDialog(rootView.getContext());

        mAuth = FirebaseAuth.getInstance();
        currentUserId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

        mPostsImageStorageRef = FirebaseStorage.getInstance().getReference();
        mUserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        mPostRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        editTextPostDescription.setHint("Hi ," + username + "! How are you feeling today!");
        Picasso.get().load(userProfilePhoto).placeholder(R.drawable.male_profile).into(profilePhoto);

        uploadPhotoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OpenGallery();
            }
        });


        postBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ValidatePostInfo();
            }
        });

        return rootView;
    }

    private void ValidatePostInfo() {
        Description = editTextPostDescription.getText().toString();

        if (ImageUri == null) {
            Toast.makeText(getContext(), "Please select post image...", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(Description)) {
            Toast.makeText(getContext(), "Please say something about your image...", Toast.LENGTH_SHORT).show();
        } else {
            loadingBar.setTitle("Add New Post");
            loadingBar.setMessage("Please wait, while we are updating your new post...");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);

            StoringImageToFirebaseStorage();
        }
    }

    private void StoringImageToFirebaseStorage() {
        Calendar calFordDate = Calendar.getInstance();
        @SuppressLint("SimpleDateFormat") SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
        saveCurrentDate = currentDate.format(calFordDate.getTime());

        Calendar calFordTime = Calendar.getInstance();
        @SuppressLint("SimpleDateFormat") SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
        saveCurrentTime = currentTime.format(calFordDate.getTime());

        postRandomName = saveCurrentDate + saveCurrentTime;


        final StorageReference filePath = mPostsImageStorageRef.child("Post Images").child(ImageUri.getLastPathSegment() + postRandomName + ".jpg");


        filePath.putFile(ImageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Toast.makeText(getContext(), "Uploaded", Toast.LENGTH_SHORT).show();
                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                downloadUrl = uri.toString();

                                Log.d("TAG", downloadUrl);
                                SavingPostInformationToDatabase();
                            }
                        });

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        loadingBar.dismiss();
                        Toast.makeText(getContext(), "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot
                                .getTotalByteCount());
                        loadingBar.setMessage("Uploaded " + (int) progress + "%");
                    }
                });
    }




    private void SavingPostInformationToDatabase() {
        mUserRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String userFullName = Objects.requireNonNull(dataSnapshot.child("full_name").getValue()).toString();
                    String userProfileImage = Objects.requireNonNull(dataSnapshot.child("profileimage").getValue()).toString();

                    HashMap postsMap = new HashMap();
                    postsMap.put("uid", currentUserId);
                    postsMap.put("date", saveCurrentDate);
                    postsMap.put("time", saveCurrentTime);
                    postsMap.put("description", Description);
                    postsMap.put("postimage", downloadUrl);
                    postsMap.put("profileimage", userProfileImage);
                    postsMap.put("full_name", userFullName);

                    mPostRef.child(currentUserId + postRandomName).updateChildren(postsMap)
                            .addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    if (task.isSuccessful()) {
                                        selfCallHomeActivity();
                                        Toast.makeText(getContext(), "New Post is updated successfully.", Toast.LENGTH_SHORT).show();
                                        loadingBar.dismiss();
                                    } else {
                                        Toast.makeText(getContext(), "Error Occured while updating your post.", Toast.LENGTH_SHORT).show();
                                        loadingBar.dismiss();
                                    }
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
