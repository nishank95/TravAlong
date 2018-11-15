package com.example.dell.travalong;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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

import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class UpdateProfileActivity extends AppCompatActivity {
    private String userID;
    private ProgressDialog progressDialog;
    private Uri imageUri;
    private FirebaseAuth mAuth;
    private DatabaseReference mUserRef;
    private StorageReference mStorageRef;
    private EditText editTextFullName, editTextUsername, editTextCountry, editTextPhone, editTextStatus;
    private CircleImageView profilePhoto;
    final static int gallery_pick_Code = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);
        editTextFullName = (EditText) findViewById(R.id.full_name_input);
        editTextCountry = (EditText) findViewById(R.id.country_input);
        editTextPhone = (EditText) findViewById(R.id.phone_input);
        editTextUsername = (EditText) findViewById(R.id.username_input);
        editTextStatus = (EditText) findViewById(R.id.status_input);
        profilePhoto = (CircleImageView) findViewById(R.id.profile_photo);

        Button proceedBtn = (Button) findViewById(R.id.proceed_btn);

        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            userID = mAuth.getCurrentUser().getUid();
        }
        mUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(userID);
        mStorageRef = FirebaseStorage.getInstance().getReference().child("Profile_Images").child(userID);

        proceedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(saveAccountInformation())
                {
                    uploadImage();
                }
                else
                {
                    Toast.makeText(UpdateProfileActivity.this, "Enter Details Correctly to Proceed!", Toast.LENGTH_SHORT).show();
                }

            }
        });

        profilePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent callGallery = new Intent();
                callGallery.setAction(Intent.ACTION_GET_CONTENT);
                callGallery.setType("image/*");
                startActivityForResult(callGallery, gallery_pick_Code);
            }
        });

        mUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild("profileimage"))
                {
                    String image = dataSnapshot.child("profileimage").getValue().toString();
                    Picasso.get().load(image).placeholder(R.drawable.male_profile).into(profilePhoto);
                }
                else
                {
                    Toast.makeText(UpdateProfileActivity.this, "Please select profile image first.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void uploadImage() {

            if(imageUri != null)
            {
                progressDialog = new ProgressDialog(UpdateProfileActivity.this);
                progressDialog.setTitle("Uploading...");
                progressDialog.show();

                mStorageRef.putFile(imageUri)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                progressDialog.dismiss();
                                Toast.makeText(UpdateProfileActivity.this, "Uploaded", Toast.LENGTH_SHORT).show();
//                                final String downloadUrl = FirebaseStorage.getInstance().getReference().child("Profile_Images").child(userID).getDownloadUrl().toString();

                                mStorageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        String downloadUrl = uri.toString();
                                        Log.d("TAG", downloadUrl);

                                        mUserRef.child("profileimage").setValue(downloadUrl)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task)
                                                    {
                                                        if(task.isSuccessful()) {
                                                            Intent selfIntent = new Intent(UpdateProfileActivity.this, UpdateProfileActivity.class);
                                                            startActivity(selfIntent);

                                                            Toast.makeText(UpdateProfileActivity.this, "Profile Image stored to Firebase Database Successfully...", Toast.LENGTH_SHORT).show();
                                                        }
                                                        else
                                                        {
                                                            String message = task.getException().getMessage();
                                                            Toast.makeText(UpdateProfileActivity.this, "Error Occured: " + message, Toast.LENGTH_SHORT).show();

                                                        }
                                                    }
                                                });
                                    }
                                    });

                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressDialog.dismiss();
                                Toast.makeText(UpdateProfileActivity.this, "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
                                        .getTotalByteCount());
                                progressDialog.setMessage("Uploaded "+(int)progress + "%");
                            }
                        });
            }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == gallery_pick_Code && resultCode == RESULT_OK && data!= null)
        {
            imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                profilePhoto.setImageBitmap(bitmap);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }


        }
    }

    private boolean saveAccountInformation() {
        String name = editTextFullName.getText().toString();
        String country = editTextCountry.getText().toString();
        String phone = editTextPhone.getText().toString();
        String username = editTextUsername.getText().toString();
        String status = editTextStatus.getText().toString();
        if (checkInput(name, country, phone, username)) {

            HashMap userData = new HashMap();
            userData.put("username", username);
            userData.put("full_name", name);
            userData.put("country", country);
            userData.put("phone", phone);
            userData.put("status", status);
            userData.put("dob", "none");
            userData.put("gender", "none");
            userData.put("relationship_status", "none");

            mUserRef.updateChildren(userData).addOnCompleteListener(new OnCompleteListener() {

                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()) {
                        callHomeActivity();
                        Toast.makeText(UpdateProfileActivity.this, "Your Details are Registered Successfully.", Toast.LENGTH_LONG).show();

                    } else {
                        String message = Objects.requireNonNull(task.getException()).getMessage();
                        Toast.makeText(UpdateProfileActivity.this, "Error Occured: " + message, Toast.LENGTH_SHORT).show();
                    }

                }

            });

         return true;
        }


        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(progressDialog !=  null)
        {
            progressDialog.dismiss();
        }
    }

    private void callHomeActivity() {
        Intent callHomeAct = new Intent(UpdateProfileActivity.this, HomeActivity.class);
        startActivity(callHomeAct);
        finish();
    }

    private boolean checkInput(String name, String country, String phone, String username) {

        if (name.isEmpty()) {
            editTextFullName.setError("Name Field Required!");
            editTextFullName.requestFocus();
            return false;
        }
        if (country.isEmpty()) {
            editTextCountry.setError("Country Required!");
            editTextCountry.requestFocus();
            return false;
        }
        if (phone.isEmpty()) {
            editTextPhone.setError("Phone Number Required!");
            editTextPhone.requestFocus();
            return false;
        }
        if (username.isEmpty()) {
            editTextUsername.setError("Username Required!");
            editTextUsername.requestFocus();
            return false;
        }
        return true;
    }

}
