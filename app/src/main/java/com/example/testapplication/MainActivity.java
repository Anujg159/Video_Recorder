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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final int Req_Video_Capture =120;
    private static final int Req_Sign_In=209;
    private Button up;
    private StorageReference mStorageRef;
    private FirebaseAuth mAuth;
    private Uri VideoUri;
    private TextView uname;
    private Button sign;
    private Button signout;
    private ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        up = findViewById(R.id.upload);
        sign=findViewById(R.id.signin);
        signout=findViewById(R.id.signout);
        uname=findViewById(R.id.user);
        up.setEnabled(false);
        mAuth = FirebaseAuth.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference();
        progressBar= findViewById(R.id.progbar);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)== PackageManager.PERMISSION_DENIED)
            ActivityCompat.requestPermissions(this ,new String[] {Manifest.permission.CAMERA},Req_Video_Capture);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)== PackageManager.PERMISSION_DENIED)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                ActivityCompat.requestPermissions(this ,new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},11);
            }
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    private void updateUI(FirebaseUser currentUser) {
        if (currentUser !=null){
            String st ="Hello "+currentUser.getDisplayName();
            uname.setText(st);
            sign.setVisibility(View.GONE);
            signout.setVisibility(View.VISIBLE);
        }
        else {
            String string="Signed Out";
            uname.setText(string);
            sign.setVisibility(View.VISIBLE);
            signout.setVisibility(View.GONE);
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
        if (requestCode == Req_Sign_In){
            if (resultCode==RESULT_OK){
                FirebaseUser user=FirebaseAuth.getInstance().getCurrentUser();
                updateUI(user);
            }
            else {
                Toast.makeText(this, "Sign In Failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void UploadVideo(View view) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser==null){
            Toast.makeText(this, "Sign In First", Toast.LENGTH_SHORT).show();
            return;
        }
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

    public void SignIn(View view) {
        List<AuthUI.IdpConfig> providers= Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.PhoneBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build());
        startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder().setAvailableProviders(providers).build(),Req_Sign_In);
    }

    public void SignOut(View view) {
        AuthUI.getInstance().signOut(this).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                updateUI(null);
            }
        });
    }
}

/*
W/ExponenentialBackoff: network unavailable, sleeping.
E/StorageUtil: error getting token java.util.concurrent.ExecutionException: com.google.firebase.internal.api.FirebaseNoSignedInUserException: Please sign in before trying to get a token.
*/

