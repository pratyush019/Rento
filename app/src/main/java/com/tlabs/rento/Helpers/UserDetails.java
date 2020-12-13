package com.tlabs.rento.Helpers;



import android.net.Uri;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class UserDetails {
    private UserDetails(){}
   static FirebaseUser firebaseUser=FirebaseAuth.getInstance().getCurrentUser();
    static DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference("users").child(getUid());




    public static String getUid(){
        return firebaseUser.getUid();
    }
    public static boolean isEmailVerified(){
        return firebaseUser.isEmailVerified();
    }

    public static String getName() {
        return firebaseUser.getDisplayName();
    }

    public static String getMail() {
        return firebaseUser.getEmail();
    }

    public static Uri getImageUrl() {

        try {
            return firebaseUser.getPhotoUrl();
        }
        catch (Exception e){
            return null;
        }

    }

    public static String getDeviceToken() {
        final String[] deviceToken = new String[1];
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild("token"))
                    deviceToken[0] =snapshot.child("token").getValue().toString();
                else deviceToken[0] =null;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        return deviceToken[0];
    }

    public static String getPhone() {
        final String[] phone = new String[1];
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild("phone"))
                    phone[0] =snapshot.child("phone").getValue().toString();
                else phone[0] =null;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        return phone[0];
    }
    public static FirebaseUser getCurrentUser(){
        return firebaseUser;
    }

    public static void setName(String name) {
        databaseReference.child("name").setValue(name);
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(name).build();
        firebaseUser.updateProfile(profileUpdates);
    }

    public static void setMail(String mail) {
        databaseReference.child("mail").setValue(mail);
    }

    public static void setImageUrl(String imageUrl) {
        databaseReference.child("image").setValue(imageUrl);
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setPhotoUri(Uri.parse(imageUrl)).build();
        firebaseUser.updateProfile(profileUpdates);
    }

    public static void setPassword(String password) {
        databaseReference.child("password").setValue(password);
    }

    public static void setDeviceToken(String deviceToken) {
        databaseReference.child("token").setValue(deviceToken);
    }

    public static void setPhone(String phone) {
        databaseReference.child("phone").setValue(phone);
    }
    public static void setMailVerified(String status){
        databaseReference.child("email verified").setValue(status);
    }
    public static void setLastLogin(String time){
        databaseReference.child("last login").setValue(time);
    }
}
