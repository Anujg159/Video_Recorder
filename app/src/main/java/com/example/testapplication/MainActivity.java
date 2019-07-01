package com.example.testapplication;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class MainActivity extends AppCompatActivity {
    private static final int Req_Video_Capture =120;
    private Button up;
    private StorageReference mStorageRef;
    private Uri VideoUri;
    private ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        up = findViewById(R.id.upload);
        up.setEnabled(false);
        mStorageRef = FirebaseStorage.getInstance().getReference();
        progressBar= findViewById(R.id.progbar);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)== PackageManager.PERMISSION_DENIED)
            ActivityCompat.requestPermissions(this ,new String[] {Manifest.permission.CAMERA},Req_Video_Capture);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)== PackageManager.PERMISSION_DENIED)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                ActivityCompat.requestPermissions(this ,new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},11);
            }
    }

    public void StartTest(View view) {
        Intent i=new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (i.resolveActivity(getPackageManager())!=null){
            startActivityForResult(i,Req_Video_Capture);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode==RESULT_OK && requestCode==Req_Video_Capture && data!=null && data.getData()!=null) {
            up.setEnabled(true);
            VideoUri = data.getData();
            grantUriPermission(getPackageName(),VideoUri,Intent.FLAG_GRANT_READ_URI_PERMISSION);
         //   Toast.makeText(this, VideoUri.getLastPathSegment(), Toast.LENGTH_SHORT).show();
        }
    }

    public void UploadVideo(View view) {
        //    Uri file = Uri.fromFile(new File("path/to/images/rivers.jpg"));
        up.setEnabled(false);
        StorageReference VidRef = mStorageRef.child("videos/"+System.currentTimeMillis()+".mp4");
        VidRef.putFile(VideoUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Get a URL to the uploaded content
                        Toast.makeText(MainActivity.this, "Upload Successful", Toast.LENGTH_SHORT).show();
                        progressBar.setProgress(0);
                        recreate();
                        revokeUriPermission(VideoUri,Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Toast.makeText(MainActivity.this, exception.toString(), Toast.LENGTH_SHORT).show();
                        up.setEnabled(true);
                    }
                })
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        double prog = (100.0*taskSnapshot.getBytesTransferred())/taskSnapshot.getTotalByteCount();
                        System.out.println("Upload is "+prog+"% done.");
                        int crtprog=(int)prog;
                        progressBar.setProgress(crtprog);
                    }
                });
    }
}

/*
W/ExponenentialBackoff: network unavailable, sleeping.
E/StorageUtil: error getting token java.util.concurrent.ExecutionException: com.google.firebase.internal.api.FirebaseNoSignedInUserException: Please sign in before trying to get a token.
*/

