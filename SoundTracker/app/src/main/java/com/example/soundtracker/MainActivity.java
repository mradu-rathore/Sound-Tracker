package com.example.soundtracker;

import android.Manifest;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.PackageManager;
import android.icu.text.DateFormat;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.icu.text.DateFormat;
import android.os.Environment;
import android.os.Handler;
//import android.support.annotation.NonNull;
//import android.support.annotation.RequiresApi;
//import android.support.v4.app.ActivityCompat;
//import android.support.v4.content.ContextCompat;
//import android.support.v7.app.AppCompatActivity;
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
    MediaPlayer mediaPlayer;
    EditText frequency;
    EditText duration;
    Button start;
    Button upload;
    TelephonyManager tm;
    String date;
    String location;
    String imei;


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
    //date = Log.e("date", new SimpleDateFormat("dd/mm/yyyy hh:mm").format(new Date()));
    //tvSampleDuration = (TextView) findViewById(R.id.editTextDuration);

    /* Code to record audio */
    public void btnStartPressed(View v) {
        try {
            int fre, dur;
            fre = Integer.parseInt(String.valueOf(frequency.getText()));
            dur = Integer.parseInt(String.valueOf(duration.getText()));
            int du = 1000*dur;
            int fr = fre*6000;

            Toast.makeText(this, "Recording has started", Toast.LENGTH_LONG).show();
            for (int i = 0; i < 5; i++) {
                System.out.println("Recording - " + i);
                recordAudio(du);
                Thread.sleep(fr);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

        private void recordAudio(int d) throws IOException {

            try{
            int du = d;
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
            }, du);
       } catch (Exception e) {
               e.printStackTrace();
           }
    }
//        private void recordVoice(int d) throws IOException {
//            int du = d;
//            mediaRecorder = new MediaRecorder();
//            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
//            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
//            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
//            File outputFile = getRecordingFile();
//            outputFiles.add(outputFile);
//            mediaRecorder.setOutputFile(outputFile.getPath());
//            mediaRecorder.prepare();
//            mediaRecorder.start();
//            new Handler().postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    if(mediaRecorder != null) {
//                        mediaRecorder.stop();
//                        mediaRecorder.release();
//                        mediaRecorder = null;
//                    }
//                }
//            }, du);
//        }
//        try {
//            mediaRecorder = new MediaRecorder();
//            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
//            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
//            File outputFile = getRecordingFile();
//            outputFiles.add(outputFile);
//            mediaRecorder.setOutputFile(outputFile.getPath());
//            //mediaRecorder.setOutputFile(getRecordingFilePath());
//            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
//            mediaRecorder.prepare();
//            mediaRecorder.start();
//
//            Toast.makeText(this, "Recording Started", Toast.LENGTH_LONG).show();
//        }
//        catch (Exception e) {
//            e.printStackTrace();
//        }
//        private Date getDateTime(){
//            long millis = System.currentTimeMillis();
//            java.sql.Date date = new java.sql.Date(millis);
//            return date;
//        }

//    }
//
//
//    /* Code to upload audio recording */
//    public void u(View v){
//        mediaRecorder.stop();
//        mediaRecorder.release();
//        mediaRecorder = null;
//
//        Toast.makeText(this, "Recording Stopped", Toast.LENGTH_LONG).show();
//
//
//
//
//    }
    /* Code to play audio recording(Testing Purpose) */
//    public void btnPlayPressed(View v){
//        try {
//            mediaPlayer = new MediaPlayer();
//            mediaPlayer.setDataSource(getRecordingFile());
//            mediaPlayer.prepare();
//            mediaPlayer.start();
//            Toast.makeText(this, "Recording Playing", Toast.LENGTH_LONG).show();
//
//        }
//        catch (Exception e){
//            e.printStackTrace();
//        }


   // }
    /* Microphone Check */
    private boolean isMicrophoneWorking(){
        if (this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_MICROPHONE)){
            return true;
        }
        else{
            return false;
        }

    }
    /* Mic Permission */
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

    
    private void getLocationPermission(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)== PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSION_CODE);

        }

    }



    /* Path to record audio */
//    private String getRecordingFilePath(){
//
//        ContextWrapper contextWrapper = new ContextWrapper(getApplicationContext());
//        File musicDirectory = contextWrapper.getExternalFilesDir(Environment.DIRECTORY_MUSIC);
//        File file = new File(musicDirectory,"testRecordingFile" + ".mp3");
//        // Create a reference to "mountains.jpg"
//       // StorageReference mountainsRef = storageRef.child(file);
//
//// Create a reference to 'images/mountains.jpg'
//       // StorageReference mountainImagesRef = storageRef.child(file);
//
//// While the file names are the same, the references point to different files
//      //  mountainsRef.getName().equals(mountainImagesRef.getName());    // true
       //mountainsRef.getPath().equals(mountainImagesRef.getPath());    // false
       //return file.getPath();
   }



