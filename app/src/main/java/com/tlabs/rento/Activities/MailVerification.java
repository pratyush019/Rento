package com.tlabs.rento.Activities;


import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessaging;
import com.tlabs.rento.Helpers.Methods;
import com.tlabs.rento.Helpers.UserDetails;
import com.tlabs.rento.R;

import java.util.Objects;

import static com.tlabs.rento.Helpers.Methods.DateHelper;
import static com.tlabs.rento.Helpers.UserDetails.isEmailVerified;

public class MailVerification extends AppCompatActivity {

    private FirebaseAuth mFirebaseAuth;
    private Handler mHandler;
    private int flag = 0;
    String token;
    String userMail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mail_verification);

        //Hook up an object to the Firebase Authentication Instance
        mFirebaseAuth = FirebaseAuth.getInstance();

        //Find and Register Listeners for the TextViews
        TextView resend = findViewById(R.id.resend_mail);
        TextView registerAgain = findViewById(R.id.register_again);
        ImageView imageView = findViewById(R.id.imageView);
        TextView TextDescription =findViewById(R.id.text_description);
        userMail= UserDetails.getMail();

        SpannableStringBuilder builder = new SpannableStringBuilder();

        SpannableString str1= new SpannableString("Our team has sent you an email with a verification link at ");
        str1.setSpan(new ForegroundColorSpan(Color.parseColor("#FF000000")), 0, str1.length(), 0);
        builder.append(str1);

        SpannableString str2= new SpannableString(userMail);
        str2.setSpan(new ForegroundColorSpan(Color.parseColor("#ed0e0e")), 0, str2.length(), 0);
        builder.append(str2);

        SpannableString str3= new SpannableString(" please click on the verification link and come back to this page");
        str3.setSpan(new ForegroundColorSpan(Color.parseColor("#FF000000")), 0, str3.length(), 0);
        builder.append(str3);

        TextDescription.setText( builder, TextView.BufferType.SPANNABLE);


        String register = "<font color='#ed0e0e'>Register Again</font>";
        registerAgain.setText(Html.fromHtml("Not "+userMail +"? " + register));

        generateToken();






        Glide.with(this).asGif().transform(new CircleCrop())
                .load(R.raw.cad).into(imageView);

        //When User Enters a wrong email and wants to change it
        registerAgain.setOnClickListener(v -> deleteAndReturn());

        //resend the verification email to the user
        resend.setOnClickListener(v -> UserVerificationMail());

        //responsible for sending the verification mail the first time
        if (flag == 0) {
            UserVerificationMail();
            flag = 1;
        }

        //Thread for Automatic background checking of user verification
        mHandler = new Handler();
        mStatusChecker.run();
    }

    private void generateToken() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        token=task.getResult();
                        Log.d("token", "generated");
                        Log.d("value",token);
                    }
                    else {
                        generateToken();
                        Log.d("token", "failed generation");
                    }
                });
    }

    Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            // 5 seconds by default, can be changed later
            int mInterval = 5000;
            VerificationHelper();
            mHandler.postDelayed(mStatusChecker, mInterval);
        }
    };

    //Called if user wants to reset and sign up again
    private void deleteAndReturn() {
        mFirebaseAuth.signOut();
        Intent i = new Intent(MailVerification.this, MainActivity.class);
        i.putExtra("fragment", 1);
        startActivity(i);
        finish();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(mStatusChecker);
    }

    // Method to check if user verified his mail upto  now or not
    private void VerificationHelper() {

        Objects.requireNonNull(UserDetails.getCurrentUser().reload().addOnCompleteListener(task -> {
            if (isEmailVerified()) {
                mHandler.removeCallbacks(mStatusChecker);

                UserDetails.setLastLogin(DateHelper());
                UserDetails.setDeviceToken(token);
                UserDetails.setMailVerified("Yes");


                AlertDialog.Builder builder=new AlertDialog.Builder(this);
                builder.setMessage("We've Verified your mail.");
                builder.setCancelable(false);
                builder.setPositiveButton("Continue", (dialogInterface, i) -> {
                    startActivity(new Intent(MailVerification.this, HomeActivity.class));
                    finish();
                    dialogInterface.dismiss();
                }).show();

            }
        })
        );
    }


    // Method to send a verification mail to the user
    private void UserVerificationMail() {
        AlertDialog progressDialog= Methods.progressDialog(this,"Preparing to send verification mail");
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();


        UserDetails.getCurrentUser().sendEmailVerification()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        progressDialog.dismiss();
                        AlertDialog.Builder builder=new AlertDialog.Builder(this);
                        builder.setTitle("Error..");
                        builder.setMessage(task.getException().getMessage());
                        builder.setCancelable(false);
                        builder.setPositiveButton("Resend", (dialogInterface, i) -> {
                            UserVerificationMail();
                            dialogInterface.dismiss();
                        }).setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.dismiss());
                        builder.show();

                    }
                    else {
                        progressDialog.dismiss();
                        AlertDialog.Builder builder=new AlertDialog.Builder(this);
                        builder.setMessage("Verification mail sent");
                        builder.setCancelable(false);
                        builder.setPositiveButton("Close", (dialogInterface, i) -> dialogInterface.dismiss());
                        builder.show();
                    }

                });
    }
}
