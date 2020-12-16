package com.tlabs.rento.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tlabs.rento.Adapters.CycleListAdapter;
import com.tlabs.rento.Helpers.CoordinateList;
import com.tlabs.rento.Helpers.CycleList;
import com.tlabs.rento.Helpers.Drawer;
import com.tlabs.rento.Helpers.Methods;
import com.tlabs.rento.Helpers.UserDetails;
import com.tlabs.rento.R;

import java.util.ArrayList;
import java.util.Objects;

public class HomeActivity extends AppCompatActivity {
    ArrayList<CycleList> cyclelist;
    Toolbar toolbar;
    String selectedZone = "Tilak";
  ArrayList<CoordinateList> gpsList;
  RecyclerView recyclerView;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_home);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Drawer.drawerGenerator(toolbar, this, this, savedInstanceState);
        recyclerView=findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this,1));




        Spinner spinner = findViewById(R.id.showZone);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.zones, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long l) {
                if (parent.getId()==R.id.showZone) {
                    selectedZone = Methods.selectedZone(position);
                    showAvailableCycle(selectedZone);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                if (parent.getId()==R.id.showZone) {
                    selectedZone="Tilak";
                    showAvailableCycle(selectedZone);
                }

            }
        });

        showAvailableCycle(selectedZone);
    }

    private void showAvailableCycle(String selectedZone) {
        final AlertDialog progressDialog = Methods.progressDialog(this,"Looking for nearby Cycles...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("cycles").child(selectedZone);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                cyclelist = new ArrayList<>();
                gpsList=new ArrayList<>();
                // Result will be holded Here
                for (DataSnapshot dsp : snapshot.getChildren()) {
                    final String[] brand = new String[1];
                    final String[] available = new String[1];
                    final String[] cycleURL = new String[1];
                    final String[] note = new String[1];
                    final String[] phone = new String[1];
                    final String[] lat = new String[1];
                    final String[] lon = new String[1];
                    String renterUid;
                    renterUid=dsp.getKey();

                    FirebaseDatabase.getInstance().getReference("rented").child(renterUid).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String to=snapshot.child("To").getValue().toString();

                            if (to.equals("none") || to.equals(UserDetails.getUid())){
                                brand[0] =Objects.requireNonNull(dsp.child("brand").getValue()).toString();
                                available[0] =Objects.requireNonNull(dsp.child("available").getValue()).toString();
                                cycleURL[0] =Objects.requireNonNull(dsp.child("cycleURL").getValue()).toString();
                                phone[0] = Objects.requireNonNull(dsp.child("phone").getValue()).toString();
                                if (dsp.hasChild("note"))
                                    note[0] =Objects.requireNonNull(dsp.child("note").getValue()).toString();
                                else note[0] =null;
                                if (dsp.hasChild("lat") && dsp.hasChild("lon")){
                                    lat[0] =Objects.requireNonNull(dsp.child("lat").getValue()).toString();
                                    lon[0] =Objects.requireNonNull(dsp.child("lon").getValue()).toString();
                                }
                                else{
                                    lat[0] ="0.0";
                                    lon[0] ="0.0";
                                }
                                cyclelist.add(new CycleList(brand[0], "Available between "+ available[0], cycleURL[0], note[0], phone[0], lat[0], lon[0],renterUid));
                                gpsList.add(new CoordinateList(Double.parseDouble(lat[0]),
                                        Double.parseDouble(lon[0]), brand[0],"Available between "+ available[0]));

                                CycleListAdapter cycleListAdapter=new CycleListAdapter(HomeActivity.this,cyclelist);
                                recyclerView.setAdapter(cycleListAdapter);

                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });











                }
                progressDialog.dismiss();


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.map) {
            Intent intent =new Intent(HomeActivity.this, MapActivity.class);
            intent.putExtra("listCoordinates",gpsList);
            intent.putExtra("fromHomeActivity",true);
            startActivity(intent);
        }
        return true;
    }

}

