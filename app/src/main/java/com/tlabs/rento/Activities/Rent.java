package com.tlabs.rento.Activities;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.tlabs.rento.Helpers.ActivityHelpers;
import com.tlabs.rento.Helpers.Drawer;
import com.tlabs.rento.Helpers.GPSHelper;
import com.tlabs.rento.Helpers.Methods;
import com.tlabs.rento.Helpers.UserDetails;
import com.tlabs.rento.R;

import java.io.File;
import java.util.Objects;

import static com.tlabs.rento.Helpers.Methods.checkURIResource;

public class Rent extends AppCompatActivity {


    private Uri contentUri;
    private String zone = "Tilak",Latitude,Longitude,Phone;
    DatabaseReference databaseReference;
    private ImageView cycleImage;
    private final String Uid=UserDetails.getUid();
    private int i=0;
    private Spinner zoneSpinner;
    EditText brand,From,To,note;
    CheckBox noteCheckbox,gpsCheckbox;
    private boolean isActivityResult=false;






    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rent);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Drawer.drawerGenerator(toolbar, this, this, savedInstanceState);


        cycleImage = findViewById(R.id.cycle_image);


        brand = findViewById(R.id.Edit_Brand);
        From = findViewById(R.id.Edit_availible_time_From);
        To = findViewById(R.id.Edit_availible_time_To);
        note = findViewById(R.id.note);

        zoneSpinner = findViewById(R.id.spinner);


        noteCheckbox = findViewById(R.id.noteCheckbox);
        gpsCheckbox = findViewById(R.id.gpsCheckbox);

        TextView terms = findViewById(R.id.terms);
        Button proceed = findViewById(R.id.proceed);

        databaseReference = FirebaseDatabase.getInstance().getReference();

        cycleImage.setOnClickListener(v -> ActivityHelpers.createChooser(Rent.this));

        From.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus)
                Methods.useTimePicker((EditText)v,Rent.this);
        });


        To.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus)
                Methods.useTimePicker((EditText)v,Rent.this);
        });
        From.setOnClickListener(v -> Methods.useTimePicker((EditText)v,Rent.this));
        To.setOnClickListener(v -> Methods.useTimePicker((EditText)v,Rent.this));

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.zones, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        zoneSpinner.setAdapter(adapter);
        zoneSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                zone=Methods.selectedZone(position);
                i=position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                zone="Tilak";
                i=0;

            }
        });


        note.setVisibility(View.INVISIBLE);
        noteCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isChecked()) {
                note.setVisibility(View.VISIBLE);
                note.requestFocus();
            }
            else {
                note.setText("");
                note.setVisibility(View.INVISIBLE);
            }
        });

        gpsCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isChecked() && Methods.hasGrantedPermission(Rent.this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION},
                    "We need to access this device location. Click continue to grant permission",
                    "We need to access this device location.You can give location permission from settings",
                    30,"location")){
                getCoordinates();
            }

            else {
                gpsCheckbox.setChecked(false);
                Latitude=null;
                Longitude=null;
            }
        });








        terms.setOnClickListener(v -> {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse("https://team-tlabs.github.io/cycleappterms.html"));
            startActivity(i);
        });



        proceed.setOnClickListener(v -> {
            String Brand = brand.getText().toString();
            String Note = note.getText().toString();
            String from = From.getText().toString();
            String to = To.getText().toString();

            if (Brand.isEmpty()) {
                brand.setError("Please provide brand");
                brand.requestFocus();
            } else if (from.isEmpty()) {
                From.setError("Provide availability time");
                From.requestFocus();
            } else if (to.isEmpty()) {
                To.setError("Provide availability time");
                To.requestFocus();
            } else  if (noteCheckbox.isChecked() && Note.isEmpty()) {
                note.setError("Provide availability time");
                note.requestFocus();
            } else if (contentUri != null && !contentUri.equals(Uri.EMPTY)) {

                databaseReference.child("users").child(Uid).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.hasChild("phone")){
                            Phone=snapshot.child("phone").getValue().toString();
                            uploadToFirebase(contentUri, Brand, Note, from, to, noteCheckbox);
                        }
                        else {
                            AlertDialog.Builder builder=Methods.builder(Rent.this,"Error","You've not added phone no." +
                                    " in your profile. Phone no. helps to establish contact. Please update your profile and come back.");
                            builder.setPositiveButton("Ok", (dialogInterface, i) -> dialogInterface.dismiss()).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                Methods.saveRentInfo(Rent.this, i, Brand,
                        From.getText().toString(),To.getText().toString()
                        , noteCheckbox.isChecked(), Note, contentUri.toString());



            } else {
                Toast.makeText(Rent.this, "No photo selected", Toast.LENGTH_SHORT).show();
            }
        });
    }


    //needs rework

    @Override
    protected void onStart() {
        super.onStart();
                    if (!isActivityResult) {

                        AlertDialog progressDialog = Methods.progressDialog(Rent.this, "Checking Status...");
                        progressDialog.setCancelable(false);
                        progressDialog.setCanceledOnTouchOutside(false);
                        progressDialog.show();


                        databaseReference.child("rented").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.hasChild(Uid)) {
                                    progressDialog.dismiss();

                                    String to=snapshot.child(Uid).child("To").getValue().toString();
                                    if (to.equals("none")) {
                                        startActivity(new Intent(Rent.this, StatusActivity.class));
                                    }
                                    else {
                                        Intent intent=new Intent(Rent.this,Approval.class);
                                        intent.putExtra("requesterUid",to);
                                        intent.putExtra("zone",snapshot.child(Uid).child("zone").getValue().toString());
                                        startActivity(intent);
                                    }
                                    finish();
                                } else {
                                    progressDialog.dismiss();

                                    if (Methods.hasGrantedPermission(Rent.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                                                    Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                            "To upload image we need storage permission. Click Continue to give them.",
                                            "To upload image we need storage permission. You can give them from settings.",
                                            70, "gallery")){
                                        fillForm();
                                }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });


                    }




    }

    private void fillForm() {
        Methods.fillRentForm(Rent.this, zoneSpinner, brand, From, To, noteCheckbox, note, cycleImage);
        Uri uploadUri = null;
        SharedPreferences sharedPreferences = Rent.this.getSharedPreferences("rentInfo", MODE_PRIVATE);
        i = sharedPreferences.getInt("spinnerPosition", 0);
        try {
            uploadUri = Uri.parse(sharedPreferences.getString("uploadUri", null));
        } catch (NullPointerException e) {
            Log.d("uploaduri", "no uri found");
        }

        if (uploadUri != null && !uploadUri.equals(Uri.EMPTY) && checkURIResource(Rent.this, uploadUri)) {
            contentUri = uploadUri;
        }
    }


    private void getCoordinates() {
        GPSHelper gpsHelper = new GPSHelper(this);
        if (gpsHelper.canGetLocation()) {
          //  gpsHelper.getLocation();
            if (gpsHelper.getLatitude()!=0) {
                Latitude = String.valueOf(gpsHelper.getLatitude());
                Longitude = String.valueOf(gpsHelper.getLongitude());
            }
            else {
                getCoordinates();
            }
        }
        else {
            gpsCheckbox.setChecked(false);
            Methods.displayLocationSettingsRequest(this,this);
        }
    }

    private void uploadToFirebase(Uri uri, String Brand,  String Note, String from, String to, CompoundButton noteCheckbox) {

        AlertDialog progressDialog=Methods.progressDialog(Rent.this,"Adding your cycle to database..");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);
        progressDialog.show();

        FirebaseStorage.getInstance().getReference().child("cycles").child(zone).child(Uid)
                .putFile(uri).addOnSuccessListener(taskSnapshot ->
                Objects.requireNonNull(Objects.requireNonNull(taskSnapshot.getMetadata()).getReference()).getDownloadUrl()
                .addOnSuccessListener(uri1 -> {

                    databaseReference.child("rented").child(Uid).child("To").setValue("none");
                    databaseReference.child("rented").child(Uid).child("zone").setValue(zone);
                    String Available = from + "-" + to;
                  DatabaseReference  mDatabaseReference =databaseReference.child("cycles").child(zone)
                            .child(Uid);
                    mDatabaseReference.child("cycleURL").setValue(uri1.toString());
                    mDatabaseReference.child("brand").setValue(Brand);
                    mDatabaseReference.child("phone").setValue(Phone);
                    mDatabaseReference.child("available").setValue(Available);
                    if (noteCheckbox.isChecked())
                        mDatabaseReference.child("note").setValue(Note);
                    if (gpsCheckbox.isChecked()) {
                        mDatabaseReference.child("lat").setValue(Latitude);
                        mDatabaseReference.child("lon").setValue(Longitude);
                    }
                 //   Methods.saveAvailability(Rent.this, true);


                    progressDialog.dismiss();
                    startActivity(new Intent(Rent.this, StatusActivity.class));
                   finish();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(Rent.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 40:{
                if (resultCode==RESULT_OK && data!=null){
                    Uri uri=data.getData();
                    isActivityResult=true;
                    File compressedFile=Methods.getCompressedFile(this,uri);
                    contentUri=Methods.getImageContentUri(this,compressedFile.getAbsolutePath());
                    cycleImage.setImageURI(contentUri);

                }
            }
            break;
            case 50:{
                if (resultCode==RESULT_OK){
                    isActivityResult=true;
                    // cam data is null
                    Uri uri=Methods.getCameraUri(this);
                    Log.d("uri",uri.toString());
                    File compressedFile=Methods.getCompressedFile(this,uri);
                    contentUri=Methods.getImageContentUri(this,compressedFile.getAbsolutePath());
                    cycleImage.setImageURI(contentUri);


                }


            }
            break;
            case 60:{
                if (resultCode==RESULT_OK) {
                    getCoordinates();
                    gpsCheckbox.setChecked(true);
                }
                else gpsCheckbox.setChecked(false);
            }
            break;
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
            case 30:{
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED){
                    getCoordinates();

                }
                else {
                    gpsCheckbox.setChecked(false);
                }
            }
            break;
            case 70:{
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED){
                    fillForm();

                }
            }
            break;
        }
    }


}




