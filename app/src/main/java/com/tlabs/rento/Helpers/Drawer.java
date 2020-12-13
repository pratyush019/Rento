package com.tlabs.rento.Helpers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.tlabs.rento.Activities.HomeActivity;
import com.tlabs.rento.Activities.MainActivity;
import com.tlabs.rento.Activities.Messaging;
import com.tlabs.rento.Activities.Profile;
import com.tlabs.rento.R;
import com.tlabs.rento.Activities.Rent;

import java.util.Objects;

public class Drawer{
    public Drawer(){

    }
    public static void drawerGenerator(final Toolbar toolbar, final Context context, final Activity activity, final Bundle savedInstanceState) {


        final Uri[] imageUri = new Uri[1];
        String[] details=new String[3];
        final com.mikepenz.materialdrawer.Drawer[] resultDrawer = new com.mikepenz.materialdrawer.Drawer[1];
        final AccountHeader[] headerResult = new AccountHeader[1];
        final ColorDrawable cd = new ColorDrawable(Color.parseColor("#2196F3"));
        AlertDialog progressDialog= Methods.progressDialog(context,"Initializing...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);
        progressDialog.show();
        Query reference = FirebaseDatabase.getInstance().getReference("users").child(UserDetails.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                details[0] = Objects.requireNonNull(snapshot.child("name").getValue()).toString();
                details[1] = Objects.requireNonNull(snapshot.child("mail").getValue()).toString();
                if (snapshot.hasChild("image")) {
                    details[2] = Objects.requireNonNull(snapshot.child("image").getValue()).toString();
                    imageUri[0] = Uri.parse(details[2]);
                }
                else {
            // imageUri[0]=Uri.parse(String.valueOf(R.drawable.ic_baseline_account_circle_24));
                }




    /*    DrawerImageLoader.init(new AbstractDrawerImageLoader() {
                    @Override
                    public void set(ImageView imageView, Uri uri, Drawable placeholder) {
                        if (!Methods.isActivityDestroyed(activity))
                            Glide.with(context)
                                    .load(imageUri)
                                    .placeholder(placeholder)
                                    .into(imageView);
                    }

                    @Override
                    public void cancel(ImageView imageView) {
                        // Glide.with(context).clear(imageView);
                    }
                }); */

                headerResult[0] = new AccountHeaderBuilder()
                        .withActivity(activity)
                        .withHeaderBackground(cd)
                        .withSelectionListEnabledForSingleProfile(false)
                        .withAlternativeProfileHeaderSwitching(false)
                        .withCompactStyle(false)
                        .withDividerBelowHeader(false)
                        .withProfileImagesVisible(true)
                        .addProfiles(new ProfileDrawerItem().withIcon(imageUri[0]).withName(details[0])
                                .withEmail(details[1]))
                        .build();
                resultDrawer[0] = new DrawerBuilder()
                        .withActivity(activity)
                        .withSelectedItem(-1)
                        .withFullscreen(true)
                        .withAccountHeader(headerResult[0])
                        .withActionBarDrawerToggle(true)
                        .withCloseOnClick(true)
                        .withMultiSelect(false)
                        .withTranslucentStatusBar(true).withToolbar(toolbar)
                        .addDrawerItems(
                                new PrimaryDrawerItem().withSelectable(false)
                                        .withIcon(R.drawable.ic_baseline_home_24)
                                        .withName("Home")
                                        .withOnDrawerItemClickListener((view, position, drawerItem) -> {
                                            activity.startActivity(new Intent(context, HomeActivity.class));
                                            activity.finish();
                                            return false;
                                        }),
                                new PrimaryDrawerItem().withSelectable(false)
                                        .withName("Messaging")
                                        .withIcon(R.drawable.ic_baseline_message_24)
                                        .withOnDrawerItemClickListener((view, position, drawerItem) -> {
                                            activity.startActivity(new Intent(context, Messaging.class));
                                            return false;
                                        }),
                                new PrimaryDrawerItem().withSelectable(false)
                                        .withName("Profile")
                                        .withIcon(R.drawable.ic_baseline_account_circle_24)
                                        .withOnDrawerItemClickListener((view, position, drawerItem) -> {
                                            Intent intent=new Intent(context, Profile.class);
                                            activity.startActivity(intent);
                                            return false;
                                        }),
                                new PrimaryDrawerItem().withSelectable(false)
                                        .withName("Rent Your Cycle")
                                        .withIcon(R.drawable.ic_baseline_directions_bike_24)
                                        .withOnDrawerItemClickListener((view, position, drawerItem) -> {
                                            activity.startActivity(new Intent(context, Rent.class));
                                            return false;
                                        }),
                                new DividerDrawerItem(),
                                new PrimaryDrawerItem().withSelectable(false)
                                        .withName("Contact Us")
                                        .withIcon(R.drawable.ic_baseline_contact_support_24)
                                        .withOnDrawerItemClickListener((view, position, drawerItem) -> {
                                            Methods.contactDeveloper(activity);
                                            return false;
                                        }),
                                new PrimaryDrawerItem().withSelectable(false)
                                        .withName("Share with friends")
                                        .withIcon(R.drawable.ic_baseline_share_24)
                                        .withOnDrawerItemClickListener((view, position, drawerItem) -> {
                                            Methods.shareApp(activity);
                                            return false;
                                        }),
                                new PrimaryDrawerItem().withSelectable(false)
                                        .withName("Privacy Policy")
                                        .withIcon(R.drawable.ic_baseline_policy_24)
                                        .withOnDrawerItemClickListener((view, position, drawerItem) -> {
                                            Methods.openPolicyPage(activity);
                                            return false;
                                        }),
                                new PrimaryDrawerItem().withSelectable(false)
                                        .withName("Rate App")
                                        .withIcon(R.drawable.ic_baseline_star_24)
                                        .withOnDrawerItemClickListener((view, position, drawerItem) -> {
                                            Methods.rateApp(context,activity);
                                            return false;
                                        }),
                                new PrimaryDrawerItem().withSelectable(false)
                                        .withName("Logout")
                                        .withIcon(R.drawable.ic_baseline_lock_24)
                                        .withOnDrawerItemClickListener((view, position, drawerItem) -> {
                                            FirebaseAuth.getInstance().signOut();
                                            activity.startActivity(new Intent(context, MainActivity.class));
                                            activity.finish();
                                            return false;
                                        })
                        ).withSavedInstance(savedInstanceState).build();
                progressDialog.dismiss();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressDialog.dismiss();

            }
        });
    }
}