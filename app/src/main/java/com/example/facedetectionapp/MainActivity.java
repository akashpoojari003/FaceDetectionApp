package com.example.facedetectionapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.facedetectionapp.Helper.GraphicOverlay;
import com.example.facedetectionapp.Helper.RectOverlay;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;
import com.wonderkiln.camerakit.CameraKitError;
import com.wonderkiln.camerakit.CameraKitEvent;
import com.wonderkiln.camerakit.CameraKitEventListener;
import com.wonderkiln.camerakit.CameraKitImage;
import com.wonderkiln.camerakit.CameraKitVideo;
import com.wonderkiln.camerakit.CameraView;

import java.util.List;

import dmax.dialog.SpotsDialog;

import static com.camerakit.CameraKit.FACING_BACK;
import static com.camerakit.CameraKit.FACING_FRONT;
import static com.wonderkiln.camerakit.CameraKit.*;

public class MainActivity extends AppCompatActivity {

    private Button faceDetectionButton;
    private GraphicOverlay graphicOverlay;
    private CameraView cameraView;
    AlertDialog alertDialog;
    private TextView mySmile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        faceDetectionButton = findViewById(R.id.detect_face_btn);
        graphicOverlay = findViewById(R.id.graphic_overlay);
        cameraView = findViewById(R.id.camera_view);
        mySmile = findViewById(R.id.textView);

        //AlertDialog when the app processes the image
        alertDialog = new SpotsDialog.Builder()
                .setContext(this)
                .setMessage("Please Wait ....  Processing....")
                .setCancelable(false)
                .build();

        //This is the button listener where the user will press detect and detection
        // process will start.
        faceDetectionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraView.start();
                cameraView.captureImage();
                graphicOverlay.clear();
            }
        });

        cameraView.addCameraKitListener(new CameraKitEventListener() {
            @Override
            public void onEvent(CameraKitEvent cameraKitEvent) {

            }

            @Override
            public void onError(CameraKitError cameraKitError) {

            }

            //Getting the image from camera and and sending it to 'processFaceDetection'
            @Override
            public void onImage(CameraKitImage cameraKitImage) {
                alertDialog.show();
                Bitmap bitmap = cameraKitImage.getBitmap();
                bitmap = Bitmap.createScaledBitmap(bitmap, cameraView.getWidth(), cameraView.getHeight(), false);
                cameraView.stop();
                processFaceDetection(bitmap);
            }

            @Override
            public void onVideo(CameraKitVideo cameraKitVideo) {

            }
        });
    }

    private void processFaceDetection(Bitmap bitmap) {
        FirebaseVisionImage firebaseVisionImage = FirebaseVisionImage.fromBitmap(bitmap);
        FirebaseVisionFaceDetectorOptions firebaseVisionFaceDetectorOptions = new FirebaseVisionFaceDetectorOptions.Builder().setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS).build();
        FirebaseVisionFaceDetector firebaseVisionFaceDetector = FirebaseVision.getInstance().getVisionFaceDetector(firebaseVisionFaceDetectorOptions);

        firebaseVisionFaceDetector.detectInImage(firebaseVisionImage)
                .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionFace>>() {
                    @Override
                    public void onSuccess(@NonNull List<FirebaseVisionFace> firebaseVisionFaces) {
                        // If the detection is succeeded and face is not found
                        if (firebaseVisionFaces.size() == 0) {
                            Toast.makeText(MainActivity.this, "Face not Found", Toast.LENGTH_SHORT).show();

                        }
                        // If the detection is succeeded and face is found
                        getFaceResults(firebaseVisionFaces);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //If any error occurs
                Toast.makeText(MainActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void getFaceResults(List<FirebaseVisionFace> firebaseVisionFaces) {


        int counter = 0;
        float smileProb = 0;
        for (FirebaseVisionFace face : firebaseVisionFaces) {

            // To set the rectangle around the face
            Rect rect = face.getBoundingBox();
            //To show the smiling probability
            if (face.getSmilingProbability() != FirebaseVisionFace.UNCOMPUTED_PROBABILITY ) {
                 smileProb = face.getSmilingProbability();

            }
            mySmile.setText("Smile- "+smileProb*100);
            RectOverlay rectOverlay = new RectOverlay(graphicOverlay, rect);
            graphicOverlay.add(rectOverlay);
            counter = counter + 1;
        }
        alertDialog.dismiss();
    }

    @Override
    protected void onPause() {
        super.onPause();
        cameraView.stop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraView.start();
    }

    //To change the camera to front or back
    public void Change(View view) {
        if (cameraView.getFacing() == Constants.FACING_FRONT)
            cameraView.setFacing(Constants.FACING_BACK);
        else
            cameraView.setFacing(Constants.FACING_FRONT);
    }
}