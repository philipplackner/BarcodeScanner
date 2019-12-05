package com.androiddevs.barcodescanner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_BARCODE = 0;
    private static final String TAG = "MainActivity";
    private SurfaceView svCamera;

    private CameraSource cameraSource;
    private TextView tvBarcode;

    private BarcodeDetector detector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        svCamera = findViewById(R.id.svCamera);
        tvBarcode = findViewById(R.id.tvBarcode);

        detector = new BarcodeDetector.Builder(this).build();
        cameraSource = new CameraSource.Builder(this, detector)
                .setAutoFocusEnabled(true).build();

        checkCameraPermission();

        // this is for showing our camera in the surface view
        svCamera.getHolder().addCallback(surfaceCallback);

        detector.setProcessor(processor);
    }

    private SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder surfaceHolder) {
            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "surfaceCreated: CREATED SURFACE");
                try {
                    cameraSource.start(surfaceHolder);
                } catch (IOException e) {
                    Toast.makeText(MainActivity.this,
                            "Oops, something went wrong.", Toast.LENGTH_SHORT).show();
                }
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) { }

        @Override
        public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
            cameraSource.stop();
        }
    };

    private Detector.Processor processor = new Detector.Processor<Barcode>() {
        @Override
        public void release() {
        }

        @Override
        public void receiveDetections(Detector.Detections<Barcode> detections) {
            SparseArray<Barcode> qrCodes = detections.getDetectedItems();
            if (qrCodes.size() > 0) {
                final Barcode barcode = qrCodes.valueAt(0);

                // we need to use post because we cannot change views from
                // another thread than the UI thread
                tvBarcode.post(new Runnable() {
                    @Override
                    public void run() {
                        tvBarcode.setText(barcode.displayValue);
                    }
                });
            } else {
                tvBarcode.post(new Runnable() {
                    @Override
                    public void run() {
                        tvBarcode.setText("");
                    }
                });
            }
        }
    };

    private void checkCameraPermission() {
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CODE_BARCODE);
            }

        }
    }
}
