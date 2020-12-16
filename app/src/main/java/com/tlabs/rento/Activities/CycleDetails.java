package com.tlabs.rento.Activities;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

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
import com.tlabs.rento.Helpers.Methods;
import com.tlabs.rento.Helpers.UserDetails;
import com.tlabs.rento.R;

import java.util.HashMap;

public class  CycleDetails extends AppCompatActivity {
    String brand,image,available,note,phone,lat,lon,renterUid,currentUid;
    ImageView cycleImage;
    Button map,call,book;
    TextView cycleBrand,cycleAvailability,cycleNote,info;
    ProgressBar progressBar;
    DatabaseReference notificationDatabase,databaseReference,renterDatabase,currentDatabase,approved;
    AlertDialog progressDialog;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        brand = getIntent().getStringExtra("brand");
        image = getIntent().getStringExtra("image");
        available = getIntent().getStringExtra("available");
        note = getIntent().getStringExtra("note");
        phone = getIntent().getStringExtra("phone");
        lat = getIntent().getStringExtra("lat");
        lon = getIntent().getStringExtra("lon");
        renterUid = getIntent().getStringExtra("renterUid");

        currentUid = UserDetails.getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        approved = databaseReference.child("approved").child(currentUid);

        notificationDatabase = databaseReference.child("notifications");
        renterDatabase = databaseReference.child("requests").child(renterUid);
        currentDatabase = databaseReference.child("users").child(currentUid);

        progressDialog=Methods.progressDialog(this,"Checking Status..");
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        matchComponents();
        initComponents();
        clickListeners();



        approvedListener();


    }

    private void clickListeners() {

        map.setOnClickListener(v -> {
            Intent intent = new Intent(CycleDetails.this, MapActivity.class);
            intent.putExtra("lat", Double.parseDouble(lat));
            intent.putExtra("lon", Double.parseDouble(lon));
            intent.putExtra("available", available);
            intent.putExtra("brand", brand);
            startActivity(intent);
        });

        call.setOnClickListener(v -> Methods.dialContactPhone(phone, CycleDetails.this));

        book.setOnClickListener(view -> currentDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild("phone")){
                    HashMap<String, String> notificationData = new HashMap<>();
                    notificationData.put("from", currentUid);
                    notificationData.put("type", "cyclebooking");
                    notificationDatabase.child(renterUid).push().
                            setValue(notificationData).
                            addOnSuccessListener(aVoid1 -> {

                                renterDatabase.child(currentUid).setValue(currentUid);
                                info.setVisibility(View.VISIBLE);
                                book.setVisibility(View.GONE);

                                new Handler(Looper.getMainLooper()).postDelayed(() -> notificationDatabase.child(renterUid).removeValue(),2000);




                            });
                }
                else {
                    AlertDialog.Builder builder=Methods.builder(CycleDetails.this,"Error","You've not added phone no." +
                            " in your profile. Phone no. helps renter to establish contact. Please update your profile and come back.");
                    builder.setPositiveButton("Ok", (dialogInterface, i) -> dialogInterface.dismiss()).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        }));



    }

    private void initComponents() {

        progressBar.setVisibility(View.VISIBLE);
        Glide.with(CycleDetails.this)
                .load(Uri.parse(image))
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
                .placeholder(R.drawable.ic_baseline_directions_bike_24)
                .into(cycleImage);
        renterDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild(currentUid)){
                    info.setVisibility(View.VISIBLE);
                    book.setVisibility(View.GONE);
                }
                else {
                    info.setVisibility(View.GONE);
                    book.setVisibility(View.VISIBLE);
                }
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressDialog.dismiss();

            }
        });


        cycleBrand.setText("Brand- "+brand);
        cycleAvailability.setText(available);
        if (note != null)
            cycleNote.setText("Renter's Note- "+note);
        else cycleNote.setVisibility(View.GONE);
        info.setVisibility(View.GONE);

        if (lat.equals("0.0") && lon.equals("0.0"))
            map.setEnabled(false);

        call.setEnabled(false);


    }

    private void matchComponents() {
        cycleImage = findViewById(R.id.cycleImage);
        progressBar = findViewById(R.id.progressbar);
        cycleBrand = findViewById(R.id.brand);
        cycleAvailability = findViewById(R.id.availability);
        cycleNote = findViewById(R.id.note);
        info=findViewById(R.id.bookedInfo);
        book = findViewById(R.id.book);
        map = findViewById(R.id.map);
        call = findViewById(R.id.call);
    }


    private void approvedListener() {
        approved.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild(renterUid)){
                    call.setEnabled(true);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
