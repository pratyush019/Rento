package com.tlabs.rento.Activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import com.tlabs.rento.Adapters.RequestAdapter;
import com.tlabs.rento.Helpers.Drawer;
import com.tlabs.rento.Helpers.Methods;
import com.tlabs.rento.Helpers.UserDetails;
import com.tlabs.rento.R;

import java.util.ArrayList;
import java.util.Objects;

public class StatusActivity extends AppCompatActivity {
    TextView zoneInfo;
    SwitchCompat switchCompat;
    Button button;
    String Uid=UserDetails.getUid();
    String selectedZone;
    Toolbar toolbar;
    ArrayList<String> requesterUid;
    DatabaseReference requestDatabaseReference= FirebaseDatabase.getInstance().getReference();
    RecyclerView recyclerView;
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);
        toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Drawer.drawerGenerator(toolbar,this,this,savedInstanceState);
        zoneInfo=findViewById(R.id.infoZone);
        switchCompat=findViewById(R.id.switchCompat);
        switchCompat.setChecked(false);
        button=findViewById(R.id.button);
        button.setEnabled(false);
        recyclerView=findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        SharedPreferences sharedPreferences =getSharedPreferences("rentInfo", MODE_PRIVATE);
        int position=sharedPreferences.getInt("spinnerPosition",0);
        selectedZone= Methods.selectedZone(position);
        zoneInfo.setText("Your Cycle is available in "+selectedZone+" zone");




        final DatabaseReference databaseReference= FirebaseDatabase.getInstance()
                .getReference("cycles").child(selectedZone).child(Uid); // point towards current user provided cycle in zone

        showRequests();

        switchCompat.setOnCheckedChangeListener((buttonView, isChecked) -> button.setEnabled(buttonView.isChecked()));
        button.setOnClickListener(v -> {
            AlertDialog progressDialog=Methods.progressDialog(StatusActivity.this,"Removing your Cycle...");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();

            databaseReference.removeValue((error, ref) -> {
                StorageReference mStorageRef = FirebaseStorage.getInstance().getReference().child("cycles").child(selectedZone).child(Uid);
                mStorageRef.delete().addOnSuccessListener(aVoid -> {
                    FirebaseDatabase.getInstance().getReference("rented").child(Uid).removeValue();
                    progressDialog.dismiss();
                    Toast.makeText(StatusActivity.this, "Removed Your Cycle", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(StatusActivity.this,Rent.class));
                    finish();
                }).addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(StatusActivity.this, "Error Occurred", Toast.LENGTH_SHORT).show();
                });
            });
        });

    }

    private void showRequests() {
        final AlertDialog progressDialog = Methods.progressDialog(this,"Checking Status...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);
        progressDialog.show();
        requesterUid=new ArrayList<>();
        requestDatabaseReference.child("requests").child(Uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dsp : snapshot.getChildren()) {
                       requesterUid.add(dsp.getKey());
                        // point towards current users available requests
                        // dsp contains requester's Uid
                    RequestAdapter requestAdapter=new RequestAdapter(StatusActivity.this,requesterUid,selectedZone);
                    recyclerView.setAdapter(requestAdapter);

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