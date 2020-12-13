package com.tlabs.rento.Activities;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.tlabs.rento.Helpers.ActivityHelpers;
import com.tlabs.rento.Helpers.Drawer;
import com.tlabs.rento.Helpers.GPSHelper;
import com.tlabs.rento.Helpers.Methods;
import com.tlabs.rento.Helpers.UserDetails;
import com.tlabs.rento.R;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

import id.zelory.compressor.Compressor;

public class Rent extends AppCompatActivity implements AdapterView.OnItemSelectedListener {


    private Uri contentUri;
    private String zone = "Tilak",Latitude,Longitude;
    private DatabaseReference mDatabaseReference;

    private ImageView cycleImage;
    private String Uid;
    private int i=0;
    private Spinner zoneSpinner;
    EditText brand,From,To,phone,note;
    CheckBox noteCheckbox,gpsCheckbox;
    private boolean isActivityResult=false;




    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rent);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Drawer.drawerGenerator(toolbar, this, this, savedInstanceState);

        Uid = UserDetails.getUid();

        cycleImage = findViewById(R.id.cycle_image);


        brand = findViewById(R.id.Edit_Brand);
        From = findViewById(R.id.Edit_availible_time_From);
        To = findViewById(R.id.Edit_availible_time_To);
        phone = findViewById(R.id.Edit_Phone_Number);
        note = findViewById(R.id.note);

        zoneSpinner = findViewById(R.id.spinner);


        noteCheckbox = findViewById(R.id.noteCheckbox);
        gpsCheckbox = findViewById(R.id.gpsCheckbox);

        TextView terms = findViewById(R.id.terms);
        Button proceed = findViewById(R.id.proceed);


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


        note.setVisibility(View.INVISIBLE);
        noteCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isChecked())
                note.setVisibility(View.VISIBLE);
            else {
                note.setText("");
                note.setVisibility(View.INVISIBLE);
            }
        });

        gpsCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isChecked() && Methods.hasGrantedLocationPermission(Rent.this)){
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
            String Phone = phone.getText().toString();
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
            } else if (Phone.length() != 10) {
                phone.setError("Provide valid phone no.");
                phone.requestFocus();
            } else if (noteCheckbox.isChecked() && Note.isEmpty()) {
                note.setError("Provide availability time");
                note.requestFocus();
            } else if (contentUri != null && !contentUri.equals(Uri.EMPTY)) {

                Methods.saveRentInfo(Rent.this, i, Brand,
                        From.getText().toString(),To.getText().toString()
                        , Phone, noteCheckbox.isChecked(), Note, contentUri.toString());

                uploadToFirebase(contentUri, Brand, Phone, Note, from, to, noteCheckbox);

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
          /*  if (isAvailable) {
               // startActivity(new Intent(this, StatusActivity.class));
                finish();
            } else */
                Methods.fillRentForm(this, zoneSpinner, brand, From, To, phone, noteCheckbox, note, cycleImage);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 40:{
                if (resultCode==RESULT_OK && data!=null){
                    Uri uri=data.getData();

                    isActivityResult=true;
                    Log.d("gal",uri.toString());
                    try {
                        new Compressor(this)
                                //.setMaxWidth(640)
                               // .setMaxHeight(480)
                               // .setQuality(75)
                                .setCompressFormat(Bitmap.CompressFormat.JPEG)
                                .setDestinationDirectoryPath(new File(this.getExternalCacheDir(),"images").getAbsolutePath())
                                .compressToFile(new File(getPath(uri)));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                 /*   FileOutputStream out;
                    String filename = Methods.getFilename(this);
                    try {
                        File  compressedImageFile = new Compressor(this).compressToFile(new File(getPath(uri)));

                    } catch (IOException e) {
                        e.printStackTrace();
                    } */
                    //   String finalUri= Methods.compressImage(uri,this);
                    //    Log.d("furi",finalUri);
                    //   uploadToFirebase(uri);
                }
            }
            break;
            case 50:{
                if (resultCode==RESULT_OK){
                    isActivityResult=true;
                    // cam data is null
                }


            }
            break;
            case 60:{
                if (resultCode==RESULT_OK)
                    getCoordinates();
            }
            break;
        }

    }



    private void getCoordinates() {
        GPSHelper gpsHelper = new GPSHelper(this);
        if (gpsHelper.canGetLocation()) {
            gpsHelper.getLocation();
            if (gpsHelper.getLatitude()!=0) {
                gpsCheckbox.setChecked(true);
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

    private void uploadToFirebase(Uri uri, String Brand, String Phone, String Note, String from, String to, CompoundButton noteCheckbox) {

        AlertDialog progressDialog=Methods.progressDialog(Rent.this,"Adding your cycle to database..");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);
        progressDialog.show();

        FirebaseStorage.getInstance().getReference().child("cycles").child(zone).child(Uid)
                .putFile(uri).addOnSuccessListener(taskSnapshot ->
                Objects.requireNonNull(Objects.requireNonNull(taskSnapshot.getMetadata()).getReference()).getDownloadUrl()
                .addOnSuccessListener(uri1 -> {
                    DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference().child("users").child(Uid).child("phone");
                    databaseReference.setValue(Phone);
                    String Available = from + "-" + to;
                    mDatabaseReference = FirebaseDatabase.getInstance().getReference("cycles").child(zone)
                            .child(Uid);
                    mDatabaseReference.child("cycleURL").setValue(uri1.toString());
                    mDatabaseReference.child("brand").setValue(Brand);
                    mDatabaseReference.child("available").setValue(Available);
                    mDatabaseReference.child("phone").setValue(Phone);
                    if (noteCheckbox.isChecked())
                        mDatabaseReference.child("note").setValue(Note);
                    if (gpsCheckbox.isChecked()) {
                        mDatabaseReference.child("lat").setValue(Latitude);
                        mDatabaseReference.child("lon").setValue(Longitude);
                    }
                 //   Methods.saveAvailability(Rent.this, true);

                    Toast.makeText(Rent.this, "Your submission has been received", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                    //startActivity(new Intent(Rent.this, StatusActivity.class));
                  //  finish();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(Rent.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case 10:{
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    ActivityHelpers.launchGalleryIntent(this);
            }
            break;
            case 20:{

                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED)
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
        }
    }
    private String getPath(Uri uri) {

        String[] projection = { MediaStore.Audio.Media.DATA };
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);

    }



    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
        zone=Methods.selectedZone(position);
        i=position;
    }
    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
        zone="Tilak";
        i=0;
    }
}




