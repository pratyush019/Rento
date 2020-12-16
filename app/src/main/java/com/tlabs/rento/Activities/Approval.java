package com.tlabs.rento.Activities;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.tlabs.rento.Helpers.Methods;
import com.tlabs.rento.Helpers.UserDetails;
import com.tlabs.rento.R;

import java.util.HashMap;

public class Approval extends AppCompatActivity {
    ImageView imageView;
    ProgressBar progressBar;
    TextView name,approvedInfo;
    Button approve,call,backToRent;
    CheckBox checkBox;

    String requesterUid,zone;
    String[] details=new String[3];
    String currentUid;
    DatabaseReference notificationReference,approvedReference;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_approval);
        matchComponents();

        Intent intent=getIntent();
        requesterUid=intent.getStringExtra("requesterUid");
        zone=intent.getStringExtra("zone");


        notificationReference= FirebaseDatabase.getInstance().getReference().child("notifications");
        approvedReference= FirebaseDatabase.getInstance().getReference().child("approved").
                child(requesterUid);

        currentUid= UserDetails.getUid();

        AlertDialog progressDialog=Methods.progressDialog(this,"Fetching Details..");
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();


        FirebaseDatabase.getInstance().getReference("users").child(requesterUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                details[0]=snapshot.child("name").getValue().toString();
                details[1]=snapshot.child("phone").getValue().toString();
                if (snapshot.hasChild("image"))
                    details[2]=snapshot.child("phone").getValue().toString();
                else details[2]=null;
                progressDialog.dismiss();
                initComponents();
                clickListeners();
                if (!Methods.isActivityDestroyed(Approval.this)&& details[2]!=null)
                    loadImage();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
progressDialog.dismiss();
            }
        });


    }

    private void loadImage() {
            Glide.with(this)
                    .load(Uri.parse(details[2]))
                    .placeholder(R.drawable.ic_baseline_account_circle_24)
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
                    })
                    .into(imageView);
    }

    private void clickListeners() {
        call.setOnClickListener(v -> Methods.dialContactPhone(details[1], Approval.this));


        approve.setOnClickListener(view -> {

            AlertDialog.Builder builder=Methods.builder(Approval.this,"Confirmation","Your cycle will be no longer" +
                    " available in selected zone. After confirmation call button will be enabled and your phone no. will be shared" +
                    "with requester.");
            builder.setCancelable(false);
            builder.setPositiveButton("Continue", (dialogInterface, i) -> {
                HashMap<String, String> notificationData = new HashMap<>();
                notificationData.put("from", requesterUid);
                notificationData.put("type", "approved");
                notificationReference.child(currentUid).push().
                        setValue(notificationData).
                        addOnSuccessListener(aVoid1 -> {
                            FirebaseDatabase.getInstance().getReference("rented")
                                    .child(currentUid).child("To").setValue(requesterUid);


                            approvedReference.child(currentUid).setValue(currentUid);
                            approvedInfo.setVisibility(View.VISIBLE);
                            approve.setVisibility(View.GONE);
                            call.setVisibility(View.VISIBLE);
                            checkBox.setVisibility(View.VISIBLE);
                            backToRent.setVisibility(View.VISIBLE);
                            new Handler(Looper.getMainLooper()).postDelayed(() -> notificationReference.child(currentUid).removeValue(),2000);



                        });


            }).setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.dismiss()).show();




        });
        checkBox.setOnCheckedChangeListener((compoundButton, isChecked) -> backToRent.setEnabled(compoundButton.isChecked()));


        backToRent.setOnClickListener(view -> {
            
            deleteCycleRequestNodeAndPhoto();
            FirebaseDatabase.getInstance().getReference("rented").child(currentUid).removeValue();
            FirebaseDatabase.getInstance().getReference("approved").child(requesterUid).child(currentUid).removeValue();
            startActivity(new Intent(Approval.this,Rent.class));
            finish();

        });


        }

    private void deleteCycleRequestNodeAndPhoto() {
        DatabaseReference databaseReference= FirebaseDatabase.getInstance()
                .getReference("cycles").child(zone).child(currentUid);
        databaseReference.removeValue((error, ref) -> {
            StorageReference mStorageRef = FirebaseStorage.getInstance().getReference().child("cycles").child(zone).child(currentUid);
            mStorageRef.delete().addOnSuccessListener(aVoid -> {
                FirebaseDatabase.getInstance().getReference("requests").child(currentUid).removeValue();
            }).addOnFailureListener(e -> {
                Toast.makeText(Approval.this, "Error Occurred", Toast.LENGTH_SHORT).show();
            });
        });
    }


    private void initComponents() {
        SpannableStringBuilder builder = new SpannableStringBuilder();

        SpannableString str1= new SpannableString("Requester's Name- ");
        str1.setSpan(new ForegroundColorSpan(Color.parseColor("#FF000000")), 0, str1.length(), 0);
        builder.append(str1);

        SpannableString str2= new SpannableString(details[0]);
        str2.setSpan(new ForegroundColorSpan(Color.parseColor("#ed0e0e")), 0, str2.length(), 0);
        builder.append(str2);

        name.setText(builder,TextView.BufferType.SPANNABLE);



    }

    private void matchComponents() {
        imageView=findViewById(R.id.image);
        progressBar=findViewById(R.id.progress);
        name=findViewById(R.id.name);
        approvedInfo=findViewById(R.id.approvedInfo);
        approve=findViewById(R.id.approveButton);
        call=findViewById(R.id.callButton);
        backToRent=findViewById(R.id.backToRent);
        checkBox=findViewById(R.id.checkbox);
    }



    @Override
    protected void onStart() {
        super.onStart();
        AlertDialog progressDialog=Methods.progressDialog(this,"Checking Status..");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);
        progressDialog.show();
        approvedReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild(currentUid)){
                    approvedInfo.setVisibility(View.VISIBLE);
                    approve.setVisibility(View.GONE);
                    call.setVisibility(View.VISIBLE);
                    checkBox.setVisibility(View.VISIBLE);
                    backToRent.setVisibility(View.VISIBLE);
                    backToRent.setEnabled(false);
                }
                else {
                    approvedInfo.setVisibility(View.GONE);
                    approve.setVisibility(View.VISIBLE);
                    call.setVisibility(View.GONE);
                    checkBox.setVisibility(View.GONE);
                    backToRent.setVisibility(View.GONE);
                }
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

                progressDialog.dismiss();
            }
        });




    }
}

