package com.tlabs.rento.Helpers;

import android.Manifest;
import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.AnyRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.tlabs.rento.BuildConfig;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

import id.zelory.compressor.Compressor;

import static android.content.Context.MODE_PRIVATE;
import static androidx.core.app.ActivityCompat.requestPermissions;
import static androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale;

public  final  class Methods {

    public static final String SAVE_TAG = "sharedPreferences";

    private Methods() {
    }


    //Utility method to obtain system date in required format
    public static String DateHelper() {
        return (new SimpleDateFormat("dd-MMM-yyyy hh:mm:ss a", Locale.getDefault()))
                .format(Calendar.getInstance().getTime());
    }


    //Utility method to store UserData if authentication successful and remember me checked
    public static void saveData(Context context, String Email, String Password, boolean setRemember) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SAVE_TAG, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("email", Email);
        editor.putString("password", Password);
        editor.putBoolean("switch", setRemember);
        editor.apply();
    }

    // Utility method to retrieve user shared preference data if was stored last time
    // Note :Remember to edit : RemoveSavedData() also while putting extra code in this method

    public static void retrieveData(Context context, EditText email_in, EditText pwd_in, CheckBox checkBox) {

        SharedPreferences sharedPreferences = context.getSharedPreferences(SAVE_TAG, MODE_PRIVATE);
        email_in.setText(sharedPreferences.getString("email", ""));
        pwd_in.setText(sharedPreferences.getString("password", ""));
        checkBox.setChecked(sharedPreferences.getBoolean("switch", false));
    }

    //Utility method to check if user input email is correct input  format
    public static boolean isValidEmail(String mail) {
        if (mail == null)
            return false;
        return android.util.Patterns.EMAIL_ADDRESS.matcher(mail).matches();
    }

    //TODO: implement a method to hide keyboard when user touches somewhere on the view


    // Note: This is linked to retrieveData ()
    public static void RemoveSavedData(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SAVE_TAG, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (sharedPreferences.contains("switch")) {
            editor.clear().apply();
        }
    }

    // to check if activity is destroyed
    // glide crash problem on activity recreation
    public static boolean isActivityDestroyed(Activity activity) {
        return activity != null && activity.isDestroyed();
    }

    //ZONE selection string array
    public static String selectedZone(int position) {
        String[] options = new String[]{"Tilak", "Patel", "Tandon", "Malviya", "Tagore", "Raman", "KNGH", "SNGH", "IH", "EE Gate", "GS Gate",
                "ME Gate", "EDC", "MP Hall"};
        return options[position];
    }


    //Utility Method to check Internet connectivity
    public static boolean checkInternetAvailable(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        return (activeNetwork != null && activeNetwork.isConnected());
    }

    //This is a utility method can be reused
    public static void useTimePicker(EditText edit, Context context) {
        int mHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        int mMinute = Calendar.getInstance().get(Calendar.MINUTE);
        TimePickerDialog timePickerDialog = new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                edit.setText(hourOfDay + ":" + minute);
            }
        }, mHour, mMinute, false);
        timePickerDialog.show();
    }


    public static void displayLocationSettingsRequest(final Context context, final Activity activity) {
        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API).build();
        googleApiClient.connect();
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(10000 / 2);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
        result.setResultCallback(result1 -> {
            final Status status = result1.getStatus();
            switch (status.getStatusCode()) {
                case LocationSettingsStatusCodes.SUCCESS:
                    Log.i("Location Change Status", "All location settings are satisfied.");
                    break;
                case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                    Log.i("Location Change Status", "Location settings are not satisfied. Show the user a dialog to upgrade location settings ");

                    try {
                        // Show the dialog by calling startResolutionForResult(), and check the result
                        // in onActivityResult().
                        status.startResolutionForResult(activity, 60);

                    } catch (IntentSender.SendIntentException e) {
                        Log.i("Location Change Status", "Pending Intent unable to execute request.");
                    }
                    break;
                case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                    Log.i("Location Change Status", "Location settings are inadequate, and cannot be fixed here. Dialog not created.");
                    break;
            }
        });
    }


    public static void dialContactPhone(final String phoneNumber, Activity activity) {
        activity.startActivity(new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phoneNumber, null)));
    }


    public static void saveAvailability(Context context, boolean isAvailable) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("availability", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isAvailable", isAvailable);
        editor.apply();
    }

    public static boolean retrieveAvailability(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("availability", MODE_PRIVATE);
        return sharedPreferences.getBoolean("isAvailable", false);
    }

    public static void saveRentInfo(Context context, int spinnerPosition, String brand, String From, String To,
                                    boolean isNoteChecked, String note, String uploadUri) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("rentInfo", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("spinnerPosition", spinnerPosition);
        editor.putString("brand", brand);
        editor.putString("From", From);
        editor.putString("To", To);
        editor.putBoolean("isNoteChecked", isNoteChecked);
        if (isNoteChecked)
            editor.putString("note", note);
        editor.putString("uploadUri", uploadUri);
        editor.apply();
    }

    public static void fillRentForm(Context context, Spinner spinner, EditText brand, EditText From, EditText To, CheckBox noteCheckbox,
                                    EditText note, ImageView imageView) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("rentInfo", MODE_PRIVATE);
        spinner.setSelection(sharedPreferences.getInt("spinnerPosition", 0));
        brand.setText(sharedPreferences.getString("brand", null));
        From.setText(sharedPreferences.getString("From", null));
        To.setText(sharedPreferences.getString("To", null));
        if (sharedPreferences.getBoolean("isNoteChecked", false)) {
            noteCheckbox.setChecked(true);
            note.setText(sharedPreferences.getString("note", null));
        }
        Uri uploadUri = null;
        try {
            uploadUri = Uri.parse(sharedPreferences.getString("uploadUri", null));
        } catch (NullPointerException e) {
            Log.d("uploaduri", "no uri found");
        }

        if (uploadUri != null && !uploadUri.equals(Uri.EMPTY) && checkURIResource(context,uploadUri)) {
           imageView.setImageURI(uploadUri);
        }
    }

    public static void saveBookingInfo(Context context, boolean hasBooked, String uid) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("bookingInfo", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(uid, hasBooked);
        editor.apply();
    }

    public static void saveUriInfo(Context context, String uriString) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("uriInfo", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("uri", uriString);
        editor.apply();
    }
    public static Uri getCameraUri(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("uriInfo", MODE_PRIVATE);
        return Uri.parse(sharedPreferences.getString("uri",null));
    }


    public static boolean retrieveBookInfo(Context context, String uid) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("bookingInfo", MODE_PRIVATE);
        return sharedPreferences.getBoolean(uid, false);
    }

    public static void saveRemainingRequests(Context context, int i) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("remainingRequests", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("remaining", i);
        editor.apply();
    }

    public static int retrieveRemainingRequests(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("remainingRequests", MODE_PRIVATE);
        return sharedPreferences.getInt("remaining", 5);
    }

    public static AlertDialog progressDialog(Context context, String message) {

        int padding = 30;
        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        linearLayout.setPadding(padding, padding, padding, padding);
        linearLayout.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER;
        linearLayout.setLayoutParams(params);

        ProgressBar progressBar = new ProgressBar(context);
        progressBar.setIndeterminate(true);
        progressBar.setPadding(0, 0, padding, 0);
        progressBar.setLayoutParams(params);

        params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER;
        TextView tvText = new TextView(context);
        tvText.setText(message);
        tvText.setTextColor(Color.parseColor("#000000"));
        tvText.setLayoutParams(params);

        linearLayout.addView(progressBar);
        linearLayout.addView(tvText);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(true);
        builder.setView(linearLayout);

        AlertDialog dialog = builder.create();
        Window window = dialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            layoutParams.copyFrom(dialog.getWindow().getAttributes());
            layoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT;
            layoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT;
            dialog.getWindow().setAttributes(layoutParams);
        }
        return dialog;
    }

    public static AlertDialog.Builder builder(Context context, String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(context));
        builder.setTitle(title).setMessage(message);
        return builder;
    }


    public static void shareApp(Activity activity) {
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        String shareMessage = "\nHi\nFound an awesome application you might be interested in\n\n";
        shareMessage = shareMessage + "https://play.google.com/store/apps/details?id=" +
                BuildConfig.APPLICATION_ID + "\n\n";
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareMessage);
        activity.startActivity(Intent.createChooser(sharingIntent, "Choose a Sharing Client"));
    }

    public static void rateApp(Context context, Activity activity) {
        final String appPackageName = context.getPackageName(); // getPackageName() from Context or Activity object
        try {
            activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
        } catch (android.content.ActivityNotFoundException anfe) {
            activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
        }
    }

    public static void openPolicyPage(Activity activity) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse("https://team-tlabs.github.io/cycleappprivacy.html"));
        activity.startActivity(i);
    }

    public static void contactDeveloper(Activity activity) {
        Intent mailIntent = new Intent(Intent.ACTION_VIEW);
        Uri data = Uri.parse("mailto:?subject=" + "Regarding Rento application" +
                "&body=" + "## If your mail is about any error then do send us screenshots/screenvideo of error with your device name and model\n\n" + "&to=" + "care4tlabs@gmail.com");
        mailIntent.setData(data);
        activity.startActivity(Intent.createChooser(mailIntent, "Send mail..."));
    }

    public static boolean hasGrantedPermission(final Context context, final String[] permissionString,
                                               final String permissionRationaleMessage, String launchSettingMessage,
                                               final int requestCode,final String preferenceString) {
        int permissionLength=permissionString.length;
        Boolean[] grantResults=new Boolean[permissionLength];
        Boolean[] shouldShowPermissionRationale=new Boolean[permissionLength];
        boolean hasGranted=false,rationale=false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (hasAsked(context, preferenceString)){

                for (int i=0;i<permissionLength;i++) {
                    grantResults[i] = ActivityCompat.checkSelfPermission(
                            context, permissionString[i]) == PackageManager.PERMISSION_GRANTED;
                    if (!grantResults[i]){
                        hasGranted=false;
                        break;
                    }
                    else {
                        hasGranted=true;
                    }
                }
                for (int i=0;i<permissionLength;i++){
                    shouldShowPermissionRationale[i]=shouldShowRequestPermissionRationale((Activity)context,permissionString[i]);
                    if (shouldShowPermissionRationale[i]){
                        rationale=true;
                        break;
                    }
                }
                if (hasGranted)
                    return true;
                else if(rationale)
                     showPermissionRationaleDialog(context,permissionRationaleMessage,permissionString,requestCode);
                else showLaunchSettingsDialog(context, launchSettingMessage);


                    } else {
                requestPermissions((Activity)context,permissionString,requestCode);
                savePermissions(context,preferenceString);
            }


                }

         else hasGranted = true;
        return hasGranted;
    }

  /*  public static boolean hasGrantedLocationPermission(Context context){
        boolean granted=false;
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            if(Methods.hasAsked(context,"location")) {


                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                    granted = true;
            }
                else if (shouldShowRequestPermissionRationale((Activity) context, Manifest.permission.ACCESS_FINE_LOCATION) ||
                shouldShowRequestPermissionRationale((Activity) context, Manifest.permission.ACCESS_COARSE_LOCATION)){
                    AlertDialog.Builder builder = builder(context, "Grant Permission..", "We need to access this device location." +
                            "Click continue to grant permission");
                    builder.setCancelable(true);
                    builder.setPositiveButton("CONTINUE", (dialog, which) ->
                            ActivityCompat.requestPermissions((Activity)context,new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION},30)).
                            setNegativeButton("NOT NOW", (dialog, which) -> dialog.cancel()).show();
                } else {
                    AlertDialog.Builder builder = builder(context, "Launch Settings?", "We need to access this device location." +
                            "You can give location permission from settings");
                    builder.setCancelable(true);
                    builder.setPositiveButton("Yes", (dialog, which) ->
                            context.startActivity(new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                    Uri.parse("package:" + BuildConfig.APPLICATION_ID))))
                            .setNegativeButton("No", (dialog, which) -> dialog.cancel()).show();

                }

            }
            else {
                ActivityCompat.requestPermissions((Activity)context,new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION},30);
                savePermissions(context,"location");
            }
        }
        else granted=true;

        return granted;

    } */


    public static String getRealPathFromURI(Uri contentUri, Context context) {
        Cursor cursor = context.getContentResolver().query(contentUri, null, null, null, null);
        if (cursor == null) {
            return contentUri.getPath();
        } else {
            cursor.moveToFirst();
            int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            return cursor.getString(index); }
    }

    public static void savePermissions(Context context,String permission){
        SharedPreferences sharedPreferences = context.getSharedPreferences("permissions", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(permission, true);
        editor.apply();
    }
    public static boolean hasAsked(Context context,String permission) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("permissions", MODE_PRIVATE);
        return sharedPreferences.getBoolean(permission, false);
    }

    // returns saved file after compressing image, pass only content uri, else may not work

    public static File getCompressedFile(Context context, Uri uri){
        try {
           return new Compressor(context)
                    //.setMaxWidth(640)
                    // .setMaxHeight(480)
                    // .setQuality(75)
                    .setCompressFormat(Bitmap.CompressFormat.JPEG)
                    .setDestinationDirectoryPath(new File(context.getExternalCacheDir(),"images").getAbsolutePath())
                    .compressToFile(getFile(uri,context))
                   ;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

    }
    // returns content uri, pass absolute file path

    public static Uri getImageContentUri(Context context, String absPath) {
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                , new String[] { MediaStore.Images.Media._ID }
                , MediaStore.Images.Media.DATA + "=? "
                , new String[] { absPath }, null);

        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
            return Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI , Integer.toString(id));

        } else if (!absPath.isEmpty()) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DATA, absPath);
            return context.getContentResolver().insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        } else {
            return null;
        }
    }


    private static File getFile(Uri uri, Context context) {

          /*  String[] projection = { MediaStore.Audio.Media.DATA };
            Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index); */
        String filePath;
        try {
            if (uri != null && "content".equals(uri.getScheme())) {
                Cursor cursor = context.getContentResolver().query(uri,
                        new String[]{android.provider.MediaStore.Images.ImageColumns.DATA}, null, null, null);
                cursor.moveToFirst();
                filePath = cursor.getString(0);
                cursor.close();
            } else {
                filePath = uri.getPath();
            }
            return new File(filePath);
        } catch (Exception e){
            return null;
        }

    }
    public static boolean checkURIResource(Context context, Uri uri) {
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
        boolean doesExist= (cursor != null && cursor.moveToFirst());
        if (cursor != null) {
            cursor.close();
        }
        return doesExist;
    }
    public static  Uri getUriToDrawable(@NonNull Context context,
                                             @AnyRes int drawableId) {
        Uri uri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
                "://" + context.getResources().getResourcePackageName(drawableId)
                + '/' + context.getResources().getResourceTypeName(drawableId)
                + '/' + context.getResources().getResourceEntryName(drawableId));
        return uri;
    }
    public static void showPermissionRationaleDialog(Context context,String permissionRationaleMessage,String[] permissionString,int requestCode){
        AlertDialog.Builder builder = builder(context, "Grant Permission..", permissionRationaleMessage);
        builder.setCancelable(true);
        builder.setPositiveButton("CONTINUE", (dialog, which) ->
                ActivityCompat.requestPermissions((Activity) context,
                        permissionString, requestCode)).
                setNegativeButton("NOT NOW", (dialog, which) -> dialog.cancel()).show();
    }
    public static void showLaunchSettingsDialog(Context context,String launchSettingMessage){
        AlertDialog.Builder builder = builder(context, "Launch Settings?", launchSettingMessage);
        builder.setCancelable(true);
        builder.setPositiveButton("Yes", (dialog, which) ->
                context.startActivity(new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.parse("package:" + BuildConfig.APPLICATION_ID))))
                .setNegativeButton("No", (dialog, which) -> dialog.cancel()).show();
    }


}

