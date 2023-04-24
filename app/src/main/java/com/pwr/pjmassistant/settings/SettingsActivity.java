package com.pwr.pjmassistant.settings;

import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.camera2.interop.Camera2CameraInfo;
import androidx.camera.camera2.interop.ExperimentalCamera2Interop;
import androidx.camera.core.CameraInfo;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.app.ActivityCompat;
import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;
import com.pwr.pjmassistant.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class SettingsActivity extends AppCompatActivity
{
    private final String TAG = "settings.SettingsActivity";
    private final String PREFERENCES_KEY = "user-prefs-key";
    ArrayList<String> cameraList;

    private SharedPreferences settings;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        if (checkPermissions()) return;

        getAvailableCameras();

        if(cameraList.isEmpty())
        {
            cameraList.add("----------------");
        }

        Spinner cameraSpinner = findViewById(R.id.modeSpinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, cameraList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        cameraSpinner.setAdapter(adapter);

        settings = getApplicationContext().getSharedPreferences(PREFERENCES_KEY, 0);

        cameraSpinner.setSelection(Integer.parseInt(settings.getString("CameraId", "0")));

        cameraSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l)
            {
                SharedPreferences.Editor editor = settings.edit();
                editor.putString("CameraId", String.valueOf(i));
                editor.apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });
    }

    private boolean checkPermissions()
    {
        return ActivityCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED;
    }

    @OptIn(markerClass = ExperimentalCamera2Interop.class)
    private void getAvailableCameras()
    {
        cameraList = new ArrayList<>();
        try
        {
            ProcessCameraProvider provider = ProcessCameraProvider.getInstance(
                    getApplicationContext()).get();

            List<CameraInfo> cameraInfos = provider.getAvailableCameraInfos();

            for (CameraInfo info : cameraInfos)
            {
                String cameraId = Camera2CameraInfo.from(info).getCameraId();
                cameraList.add("Camera " + cameraId);
            }
        }
        catch (ExecutionException | InterruptedException error)
        {
            Toast.makeText(getApplicationContext(), "PLACEHOLDER", Toast.LENGTH_SHORT).show();
            Log.e(TAG, error.toString());
        }
    }
}