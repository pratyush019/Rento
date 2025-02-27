package com.tlabs.rento.Helpers;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;

import java.io.File;

public class ActivityHelpers {

    private ActivityHelpers(){}

    public static void createChooser(Context context) {
        String[] options = {"Take Picture", "Choose from Gallery"};
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle("Set Profile Image")
                .setItems(options, (dialog1, which) -> {
                    switch (which) {
                        case 0:
                                if (Methods.hasGrantedPermission(context, new String[]{Manifest.permission.CAMERA,
                                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                                Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                        "To capture image we need to use device Camera and to save captured image we" +
                                                " also need to access your storage." +
                                                " Click Continue to give permission.",
                                        "To capture image we need use device Camera and to save captured image we" +
                                                " also need to access your storage." +
                                                ". You can give Camera and Storage permission from settings.",
                                        20,"cameraAndGallery"))
                                    launchCameraIntent(context,(Activity)context);
                            break;
                        case 1:
                                if (Methods.hasGrantedPermission(context,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                                                Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                        "To upload image we need storage permission. Click Continue to give them.",
                                        "To upload image we need storage permission. You can give them from settings.",
                                        10,"gallery"))
                                    launchGalleryIntent((Activity) context);

                            break;
                    }
                }).create();
        dialog.show();

    }

    public static void launchGalleryIntent(Activity activity) {
        Intent pickImage=new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
       activity.startActivityForResult(pickImage,40);

    }

    public static void launchCameraIntent(Context context,Activity activity) {
        File path = new File(context.getExternalCacheDir(), "camera");
        if (!path.exists())
            path.mkdirs();
        File cameraFile = new File(path, System.currentTimeMillis() + ".jpg");
        Uri cameraUri = FileProvider.getUriForFile(context, "com.tlabs.rento.fileprovider", cameraFile);
        Uri contentUri=Methods.getImageContentUri(context,cameraFile.getAbsolutePath());


        Log.d("cfile",cameraFile.toString());
        Log.d("curi",cameraUri.toString());

      // Uri.fromFile(new File(path,System.currentTimeMillis() + ".jpg"));
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraUri);
        Methods.saveUriInfo(context,contentUri.toString());
        activity.startActivityForResult(cameraIntent, 50);
    }

}
