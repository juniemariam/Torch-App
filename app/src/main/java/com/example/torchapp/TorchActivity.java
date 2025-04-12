package com.example.torchapp;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class TorchActivity extends AppCompatActivity implements SensorEventListener {

    private static final int CAMERA_PERMISSION_CODE = 101;

    private SensorManager sensorManager;
    private Sensor lightSensor;
    private boolean isLightOn = false;
    private String cameraId;
    private CameraManager camManager;

    private TextView sensorReading;
    private TextView torchStatus;
    private ImageView torchImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sensorReading = findViewById(R.id.sensor_reading);
        torchStatus = findViewById(R.id.on_off_status);
        torchImage = findViewById(R.id.light_img);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

        camManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            cameraId = camManager.getCameraIdList()[0];
        } catch (CameraAccessException e) {
            Log.e("TorchApp", "Camera access error: " + e.getMessage());
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        }

        if (lightSensor == null) {
            Toast.makeText(this, "No light sensor found!", Toast.LENGTH_LONG).show();
        } else {
            sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (lightSensor != null) {
            sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    private void setTorch(boolean status) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            try {
                camManager.setTorchMode(cameraId, status);
                isLightOn = status;
                updateUI();
            } catch (CameraAccessException e) {
                Log.e("TorchApp", "Error toggling torch: " + e.getMessage());
            }
        }
    }

    private void updateUI() {
        if (isLightOn) {
            torchStatus.setText("On");
            torchImage.setImageResource(R.drawable.light_on);
        } else {
            torchStatus.setText("Off");
            torchImage.setImageResource(R.drawable.light_off);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float lux = event.values[0];
        sensorReading.setText(String.format("%.2f lux", lux));
        setTorch(lux < 25);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Optional: ignore
    }

    public void clickClose(View view) {
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Camera permission granted", Toast.LENGTH_SHORT).show();
                // You can now enable the torch if needed
            } else {
                Toast.makeText(this, "Camera permission is required to use the torch", Toast.LENGTH_LONG).show();
                finish(); // Exit if permission denied
            }
        }
    }
}
