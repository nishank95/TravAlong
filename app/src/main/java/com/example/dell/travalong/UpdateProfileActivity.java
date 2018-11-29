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

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class UpdateProfileActivity extends AppCompatActivity {
    @BindView(R.id.full_name_input)EditText editTextFullName;
    @BindView(R.id.username_input)EditText editTextUsername;
    @BindView(R.id.country_input)EditText editTextCountry;
    @BindView(R.id.phone_input)EditText editTextPhone;
    @BindView(R.id.status_input)EditText editTextStatus;
    @BindView(R.id.profile_photo)CircleImageView profilePhoto;
    @BindView(R.id.proceed_btn)Button proceedBtn;
    private Uri imageUri;
    final static int gallery_pick_Code = 1;
    private String userID;
    private ProgressDialog progressDialog;

    private FirebaseAuth mAuth;
    private DatabaseReference mUserRef;
    private StorageReference mStorageRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);
        ButterKnife.bind(this);

        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            userID = mAuth.getCurrentUser().getUid();
        }
        mUserRef = FirebaseDatabase.getInstance().getReference().child(getString(R.string.child_users)).child(userID);
        mStorageRef = FirebaseStorage.getInstance().getReference().child(getString(R.string.child_profile_image)).child(userID);

        proceedBtn.setOnClickListener(view -> {

            if(saveAccountInformation())
            {
                uploadImage();
            }
            else
            {
                Toast.makeText(UpdateProfileActivity.this, R.string.details_correct_message, Toast.LENGTH_SHORT).show();
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
                if(dataSnapshot.hasChild(getString(R.string.child_profile_image)))
                {
                    String image = dataSnapshot.child(getString(R.string.child_profile_image)).getValue().toString();
                    Picasso.get().load(image).placeholder(R.drawable.male_profile).into(profilePhoto);
                }
                else
                {
                    Toast.makeText(UpdateProfileActivity.this, R.string.select_profile_image_message, Toast.LENGTH_SHORT).show();
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
                progressDialog.setTitle(getString(R.string.uploading_message));
                progressDialog.show();

                mStorageRef.putFile(imageUri)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                progressDialog.dismiss();
                                Toast.makeText(UpdateProfileActivity.this, R.string.uploaded_message, Toast.LENGTH_SHORT).show();

                                mStorageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        String downloadUrl = uri.toString();
                                        Log.d("TAG", downloadUrl);

                                        mUserRef.child(getString(R.string.child_profile_image)).setValue(downloadUrl)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task)
                                                    {
                                                        if(task.isSuccessful()) {
                                                            Intent selfIntent = new Intent(UpdateProfileActivity.this, UpdateProfileActivity.class);
                                                            startActivity(selfIntent);

                                                            Toast.makeText(UpdateProfileActivity.this, R.string.image_stored_success_message, Toast.LENGTH_SHORT).show();
                                                        }
                                                        else
                                                        {
                                                            String message = task.getException().getMessage();
                                                            Toast.makeText(UpdateProfileActivity.this, getString(R.string.image_store_error) + message, Toast.LENGTH_SHORT).show();

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
            userData.put(getString(R.string.key_username), username);
            userData.put(getString(R.string.key_full_name), name);
            userData.put(getString(R.string.key_country), country);
            userData.put(getString(R.string.key_phone), phone);
            userData.put(getString(R.string.key_status), status);
            userData.put(getString(R.string.key_dob), "none");
            userData.put(getString(R.string.key_gender), "none");
            userData.put(getString(R.string.key_relationship_stas), "none");

            mUserRef.updateChildren(userData).addOnCompleteListener(new OnCompleteListener() {

                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()) {
                        callHomeActivity();
                        Toast.makeText(UpdateProfileActivity.this, R.string.details_stored_success_message, Toast.LENGTH_LONG).show();

                    } else {
                        String message = Objects.requireNonNull(task.getException()).getMessage();
                        Toast.makeText(UpdateProfileActivity.this, getString(R.string.details_store_error_message) + message, Toast.LENGTH_SHORT).show();
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
            editTextFullName.setError(getString(R.string.name_required_message));
            editTextFullName.requestFocus();
            return false;
        }
        if (country.isEmpty()) {
            editTextCountry.setError(getString(R.string.country_required_message));
            editTextCountry.requestFocus();
            return false;
        }
        if (phone.isEmpty()) {
            editTextPhone.setError(getString(R.string.phn_required_message));
            editTextPhone.requestFocus();
            return false;
        }
        if (username.isEmpty()) {
            editTextUsername.setError(getString(R.string.username_required_message));
            editTextUsername.requestFocus();
            return false;
        }
        return true;
    }

}
