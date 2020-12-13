package com.tlabs.rento.Helpers;

import android.Manifest;
import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ExifInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

import static android.content.Context.MODE_PRIVATE;
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
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult result) {
                final Status status = result.getStatus();
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

    public static void saveRentInfo(Context context, int spinnerPosition, String brand, String From, String To, String phone,
                                    boolean isNoteChecked, String note, String uploadUri) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("rentInfo", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("spinnerPosition", spinnerPosition);
        editor.putString("brand", brand);
        editor.putString("From", From);
        editor.putString("To", To);
        editor.putString("phone", phone);
        editor.putBoolean("isNoteChecked", isNoteChecked);
        if (isNoteChecked)
            editor.putString("note", note);
        editor.putString("uploadUri", uploadUri);
        editor.apply();
    }

    public static void fillRentForm(Context context, Spinner spinner, EditText brand, EditText From, EditText To, EditText phone, CheckBox noteCheckbox,
                                    EditText note, ImageView imageView) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("rentInfo", MODE_PRIVATE);
        spinner.setSelection(sharedPreferences.getInt("spinnerPosition", 0));
        brand.setText(sharedPreferences.getString("brand", null));
        From.setText(sharedPreferences.getString("From", null));
        To.setText(sharedPreferences.getString("To", null));
        phone.setText(sharedPreferences.getString("phone", null));
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

        if (uploadUri != null && !uploadUri.equals(Uri.EMPTY)) {
            imageView.setImageURI(uploadUri);
            imageView.setVisibility(View.VISIBLE);
        }
    }

    public static void saveBookingInfo(Context context, boolean hasBooked, String uid) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("bookingInfo", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(uid, hasBooked);
        editor.apply();
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

    public static boolean hasGrantedPermission(final Context context, final String permissionString,
                                               final String permissionRationaleMessage, String launchSettingMessage,
                                               final int requestCode) {
        boolean granted = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (hasAsked(context, permissionString)){
                if (ActivityCompat.checkSelfPermission(context, permissionString) == PackageManager.PERMISSION_GRANTED) {
                    granted = true;
                } else if (shouldShowRequestPermissionRationale((Activity) context, permissionString)) {
                    AlertDialog.Builder builder = builder(context, "Grant Permission..", permissionRationaleMessage);
                    builder.setCancelable(true);
                    builder.setPositiveButton("CONTINUE", (dialog, which) ->
                            ActivityCompat.requestPermissions((Activity) context,
                                    new String[]{permissionString}, requestCode)).
                            setNegativeButton("NOT NOW", (dialog, which) -> dialog.cancel()).show();
                } else {
                    AlertDialog.Builder builder = builder(context, "Launch Settings?", launchSettingMessage);
                    builder.setCancelable(true);
                    builder.setPositiveButton("Yes", (dialog, which) ->
                            context.startActivity(new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                    Uri.parse("package:" + BuildConfig.APPLICATION_ID))))
                            .setNegativeButton("No", (dialog, which) -> dialog.cancel()).show();

                }
        }
            else {
                ActivityCompat.requestPermissions((Activity)context,new String[]{permissionString},requestCode);
                savePermissions(context,permissionString);
            }

        } else granted = true;
        return granted;
    }

    public static boolean hasGrantedLocationPermission(Context context){
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

    }


    public static String compressImage(Uri imageUri,Context context) {

        String filePath = getRealPathFromURI(imageUri,context);
        Bitmap scaledBitmap = null;

        BitmapFactory.Options options = new BitmapFactory.Options();

//      by setting this field as true, the actual bitmap pixels are not loaded in the memory. Just the bounds are loaded. If
//      you try the use the bitmap here, you will get null.
        options.inJustDecodeBounds = true;
        Bitmap bmp = BitmapFactory.decodeFile(filePath, options);

        int actualHeight = options.outHeight;
        int actualWidth = options.outWidth;

//      max Height and width values of the compressed image is taken as 816x612

        float maxHeight = 816.0f;
        float maxWidth = 612.0f;
        float imgRatio = actualWidth / actualHeight;
        float maxRatio = maxWidth / maxHeight;

//      width and height values are set maintaining the aspect ratio of the image

        if (actualHeight > maxHeight || actualWidth > maxWidth) {
            if (imgRatio < maxRatio) {
                imgRatio = maxHeight / actualHeight;
                actualWidth = (int) (imgRatio * actualWidth);
                actualHeight = (int) maxHeight;
            } else if (imgRatio > maxRatio) {
                imgRatio = maxWidth / actualWidth;
                actualHeight = (int) (imgRatio * actualHeight);
                actualWidth = (int) maxWidth;
            } else {
                actualHeight = (int) maxHeight;
                actualWidth = (int) maxWidth;

            }
        }

//      setting inSampleSize value allows to load a scaled down version of the original image

        options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight);

//      inJustDecodeBounds set to false to load the actual bitmap
        options.inJustDecodeBounds = false;

//      this options allow android to claim the bitmap memory if it runs low on memory
        options.inPurgeable = true;
        options.inInputShareable = true;
        options.inTempStorage = new byte[16 * 1024];

        try {
//          load the bitmap from its path
            bmp = BitmapFactory.decodeFile(filePath, options);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();

        }
        try {
            scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.ARGB_8888);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();
        }

        float ratioX = actualWidth / (float) options.outWidth;
        float ratioY = actualHeight / (float) options.outHeight;
        float middleX = actualWidth / 2.0f;
        float middleY = actualHeight / 2.0f;

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bmp, middleX - bmp.getWidth() / 2, middleY - bmp.getHeight() / 2, new Paint(Paint.FILTER_BITMAP_FLAG));

//      check the rotation of the image and display it properly
        ExifInterface exif;
        try {
            exif = new ExifInterface(filePath);

            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, 0);
            Log.d("EXIF", "Exif: " + orientation);
            Matrix matrix = new Matrix();
            if (orientation == 6) {
                matrix.postRotate(90);
                Log.d("EXIF", "Exif: " + orientation);
            } else if (orientation == 3) {
                matrix.postRotate(180);
                Log.d("EXIF", "Exif: " + orientation);
            } else if (orientation == 8) {
                matrix.postRotate(270);
                Log.d("EXIF", "Exif: " + orientation);
            }
            scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0,
                    scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix,
                    true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        FileOutputStream out;
        String filename = getFilename(context);
        try {
            out = new FileOutputStream(filename);

//          write the compressed bitmap at the destination specified by filename.
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return filename;

    }

    public static String getFilename(Context context) {
        File  folder = new File(context.getExternalCacheDir(),"images");
        if (!folder.exists())
            folder.mkdirs();
        String uriString = folder.getAbsolutePath() + "/" + System.currentTimeMillis() + ".jpg";
        return uriString;
    }

    private static String getRealPathFromURI(Uri contentUri, Context context) {
        Cursor cursor = context.getContentResolver().query(contentUri, null, null, null, null);
        if (cursor == null) {
            return contentUri.getPath();
        } else {
            cursor.moveToFirst();
            int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            return cursor.getString(index); }
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height/ (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = Math.min(heightRatio, widthRatio);
        }
        final float totalPixels = width * height;
        final float totalReqPixelsCap = reqWidth * reqHeight * 2;
        while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
            inSampleSize++;
        } return inSampleSize;
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




}

