package com.example.soundtracker;

import android.Manifest;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;
import java.text.SimpleDateFormat;
import java.util.Date;


public class MainActivity extends AppCompatActivity {

    private static int MICROPHONE_PERMISSION_CODE = 200;
    private static int LOCATION_PERMISSION_CODE = 201;
    private ArrayList<File> outputFiles = new ArrayList<>();
    StorageReference audioStorageRef;

    private MediaRecorder mediaRecorder;
    EditText frequency;
    EditText duration;
    Button start;
    Button upload;
    TelephonyManager tm;
    String date;




    //private TextView tvSampleDuration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        audioStorageRef = storageRef.child("audio");
        frequency = findViewById(R.id.editTextFrequency);
        duration = findViewById(R.id.editTextDuration);
        start = (Button) findViewById(R.id.buttonStart);
        upload = (Button) findViewById(R.id.buttonUpload);
        int permI = ContextCompat.checkSelfPermission(this,Manifest.permission.READ_PHONE_STATE);
        int d = Log.e("date", new SimpleDateFormat("dd/mm/yyyy hh:mm").format(new Date()));
        date = getDateTime().toString();


        getLocationPermission();

        if (permI == PackageManager.PERMISSION_GRANTED){

            tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        }
        else {

            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_PHONE_STATE}, 123);
        }

        if (isMicrophoneWorking()){
            getMicrophonePermission();

        }
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnStartPressed(v);
            }

        });
        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnUploadPressed(v);
            }
        });
    }
    /* Code to record audio */
    public void btnStartPressed(View v) {
        try {
            int fre, dur;
            fre = Integer.parseInt(String.valueOf(frequency.getText()));
            dur = Integer.parseInt(String.valueOf(duration.getText()));
            int du = 1000*dur;
            int fr = fre*6000;

            Toast.makeText(this, "Recording has started", Toast.LENGTH_LONG).show();
            for (int i = 0; i < fre; i++) {
                System.out.println("Recording - " + i);
                recordAudio(du);
                Thread.sleep(fr);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
/* Code to record audio */
        private void recordAudio(int d) throws IOException {

            try{
            int duration = d;
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            File outputFile = getRecordingFile();
            outputFiles.add(outputFile);
            mediaRecorder.setOutputFile(outputFile.getPath());
            mediaRecorder.prepare();
            mediaRecorder.start();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mediaRecorder != null) {
                        mediaRecorder.stop();
                        mediaRecorder.release();
                        mediaRecorder = null;
                    }
                }
            }, duration);
       } catch (Exception e) {
               e.printStackTrace();
           }
    }
    /* Microphone Check */
    private boolean isMicrophoneWorking(){
        if (this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_MICROPHONE)){
            return true;
        }
        else{
            return false;
        }

    }
    // Mic Permission
    private void getMicrophonePermission(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)== PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, MICROPHONE_PERMISSION_CODE);

        }
        
    }
    private File getRecordingFile(){
        ContextWrapper contextWrapper;
        contextWrapper = new ContextWrapper(getApplicationContext());
        File musicDirectory =  contextWrapper.getExternalFilesDir(Environment.DIRECTORY_MUSIC);
        return new File(musicDirectory, UUID.randomUUID().toString() + ".mp3");
    }
    private Date getDateTime(){
        long millis=System.currentTimeMillis();
        java.sql.Date date = new java.sql.Date(millis);
        return date;
    }
    private void btnUploadPressed(View view){
        uploadFileToDB(this, outputFiles);
    }
// Code to upload files to Firebase Database
    private void uploadFileToDB(Context cx, ArrayList<File> recordedFiles) {

        for (int i = 0; i < recordedFiles.size(); i++) {
            StorageMetadata metadata = new StorageMetadata.Builder()
                    .setContentType("audio/mpeg")
                    .build();
            File recordedFile = recordedFiles.get(i);
            Uri file = Uri.fromFile(new File(recordedFile.getPath()));
            StorageReference riversRef = audioStorageRef.child(file.getLastPathSegment());
            UploadTask uploadTask = riversRef.putFile(file, metadata);

            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Toast.makeText(cx, "Failed to upload file", Toast.LENGTH_SHORT).show();
                    System.out.println(exception.getMessage());
                    System.out.println(exception.getStackTrace());
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Toast.makeText(cx, "Uploaded file successfully", Toast.LENGTH_SHORT).show();
                    recordedFiles.remove(recordedFile);
                }
            });
        }
    }

    // Code to get Location Permission
    private void getLocationPermission(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)== PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSION_CODE);

        }

    }

   }



