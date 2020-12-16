package com.tlabs.rento.Activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.documentfile.provider.DocumentFile;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.tlabs.rento.Helpers.ActivityHelpers;
import com.tlabs.rento.Helpers.Drawer;
import com.tlabs.rento.Helpers.Methods;
import com.tlabs.rento.Helpers.UserDetails;
import com.tlabs.rento.R;

import java.io.File;
import java.util.Objects;

public class Profile extends AppCompatActivity {
    private ImageView profileImage;
    TextView name,email,password,phone;
    ProgressBar progressBar;
    Uri contentUri;
   // READ_EXTERNAL_STORAGE_CODE 10
   // CAMERA_PERMISSION_CODE 20
   // ACCESS_LOCATION_CODE 30
  // PICK_IMAGE_REQUEST 40
    // CAPTURE_IMAGE_REQUEST 50
    //LOCATION_SETTINGS_REQUEST 60



    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Drawer.drawerGenerator(toolbar, this, this, savedInstanceState);

        profileImage = findViewById(R.id.profileImage);
        ImageView camera = findViewById(R.id.profileImageChange);
        name=findViewById(R.id.username);
        email=findViewById(R.id.usermail);
        phone=findViewById(R.id.phone);
        password=findViewById(R.id.changepwd);
        progressBar=findViewById(R.id.profileProgress);
        progressBar.setVisibility(View.GONE);

        String[] details=new String[4];

        Query reference = FirebaseDatabase.getInstance().getReference("users").child(UserDetails.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                details[0] = Objects.requireNonNull(snapshot.child("name").getValue()).toString();
                details[1] = Objects.requireNonNull(snapshot.child("mail").getValue()).toString();
                if (snapshot.hasChild("image")) {
                    details[2] = Objects.requireNonNull(snapshot.child("image").getValue()).toString();
                    loadImage(details[2]);

                }
                if (snapshot.hasChild("phone")){
                    details[3]= Objects.requireNonNull(snapshot.child("phone").getValue()).toString();
                    phone.setText(details[3]);
                }
                else phone.setText("Phone no. not added");

                name.setText(details[0]);
                email.setText(details[1]);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });






        name.setOnClickListener(v -> setUsername());

        password.setOnClickListener(v -> changePasswordDialog());

        phone.setOnClickListener(view -> updatePhoneDialog());

        camera.setOnClickListener(view -> ActivityHelpers.createChooser(Profile.this));



    }

    private void updatePhoneDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.editname, null);
        EditText editText = view.findViewById(R.id.editTextDialogUserInput);
        TextView message=view.findViewById(R.id.message);
        message.setText("Please Enter your 10 digit phone no.");
        editText.setInputType(InputType.TYPE_CLASS_NUMBER);
        AlertDialog alertDialog = new AlertDialog.Builder(Profile.this)
                .setTitle("Update Phone")
                .setView(view)
                .setPositiveButton("OK", (dialog, which) -> {
                    String string=editText.getText().toString();
                    if (string.length()==10) {
                        UserDetails.setPhone(string);
                        Toast.makeText(Profile.this, "Updated", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(Profile.this, "Invalid Phone", Toast.LENGTH_SHORT).show();

                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.cancel())
                .create();
        alertDialog.show();
    }

    //triggered when user tries to change
    // his name
    private void setUsername() {

        View view = LayoutInflater.from(this).inflate(R.layout.editname, null);
        EditText editText = view.findViewById(R.id.editTextDialogUserInput);
        TextView message=view.findViewById(R.id.message);
        message.setText("Please Enter your new Username");
        AlertDialog alertDialog = new AlertDialog.Builder(Profile.this)
                .setTitle("Change Username")
                .setView(view)
                .setPositiveButton("OK", (dialog, which) -> {
                   UserDetails.setName(editText.getText().toString());
                    Toast.makeText(Profile.this, "Updated", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.cancel())
                .create();
        alertDialog.show();
    }

    //triggered when user tries to change his password
    private void changePasswordDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.updatepwd, null);
        EditText oldPassword = view.findViewById(R.id.oldpwd);
        EditText newPassword = view.findViewById(R.id.newpwd);
        EditText RePassword = view.findViewById(R.id.renewpwd);

        AlertDialog dialog = new AlertDialog.Builder(Profile.this)
                .setTitle("Change Password")
                .setView(view)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {

                    String oldpass, newpass, repass;

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        oldpass = oldPassword.getText().toString();
                        newpass = newPassword.getText().toString();
                        repass = RePassword.getText().toString();
                        if (!(newpass.equals(repass)) || newpass.length() < 8) {
                            Toast.makeText(Profile.this, "Password either too short or " +
                                    "does not match confirm password ", Toast.LENGTH_SHORT).show();
                        } else {
                            changePassword(oldpass, newpass);
                        }
                    }
                })
                .setNegativeButton("Cancel", (dialog1, which) -> dialog1.dismiss())
                .create();
        dialog.show();
    }

    private void changePassword(String oldpass, String newpass) {
        FirebaseUser mFirebaseUser=FirebaseAuth.getInstance().getCurrentUser();
        AuthCredential credential = EmailAuthProvider.getCredential(UserDetails.getMail(), oldpass);
        assert mFirebaseUser != null;
        mFirebaseUser.reauthenticate(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                mFirebaseUser.updatePassword(newpass).addOnCompleteListener(task1 -> {
                    UserDetails.setPassword(newpass);
                    Toast.makeText(Profile.this, "Password Updated "
                            , Toast.LENGTH_SHORT).show();
                });
            } else {
                Toast.makeText(Profile.this, "Incorrect old Password"
                        , Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode){
            case 40:{
                if (resultCode==RESULT_OK && data!=null){
                    Uri uri=data.getData();
                    File compressedFile=Methods.getCompressedFile(this,uri);
                    contentUri=Methods.getImageContentUri(this,compressedFile.getAbsolutePath());
                    uploadToFirebase(contentUri);

                }
            }
            break;
            case 50:{
                if (resultCode==RESULT_OK){
                    Uri uri=Methods.getCameraUri(this);
                    File compressedFile=Methods.getCompressedFile(this,uri);
                    contentUri=Methods.getImageContentUri(this,compressedFile.getAbsolutePath());
                    new File(uri.getPath()).delete();
                    uploadToFirebase(contentUri);
                }


            }
            break;
        }

    }

    private void uploadToFirebase(Uri uri) {
        AlertDialog progressDialog= Methods.progressDialog(this,"Updating Details..");
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        StorageReference mStorageRef = FirebaseStorage.getInstance().getReference().child("users").child(UserDetails.getUid()).child("profileimage");
        StorageReference riversRef = mStorageRef.child(Objects.requireNonNull(uri.getLastPathSegment()));
        riversRef.putFile(uri).addOnSuccessListener(taskSnapshot ->
                Objects.requireNonNull(Objects.requireNonNull(taskSnapshot.getMetadata()).getReference()).getDownloadUrl()
                .addOnSuccessListener(uri1 -> {
                    UserDetails.setImageUrl(uri1.toString());
                    progressDialog.dismiss();
                    Toast.makeText(Profile.this, "Details Updated", Toast.LENGTH_SHORT).show();
                })).addOnFailureListener(e -> Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show());

    }

    private void loadImage(String url) {
        Uri imageUri=Uri.parse(url);
        if (imageUri!=null && !Methods.isActivityDestroyed(this)) {
            progressBar.setVisibility(View.VISIBLE);
            Glide.with(this)
                    .load(imageUri)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            progressBar.setVisibility(View.GONE);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            progressBar.setVisibility(View.GONE);
                            return false;
                        }
                    }).placeholder(R.drawable.ic_baseline_account_circle_24)
                    .transform(new CircleCrop())
                    .into(profileImage);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case 10:{
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED)
                        ActivityHelpers.launchGalleryIntent(this);
            }
            break;
            case 20:{

                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED
                        && grantResults[2] == PackageManager.PERMISSION_GRANTED)
                    ActivityHelpers.launchCameraIntent(this,this);
            }
            break;
        }
    }
}