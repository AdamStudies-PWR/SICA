package com.pwr.pjmassistant.settings;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.camera2.interop.Camera2CameraInfo;
import androidx.camera.camera2.interop.ExperimentalCamera2Interop;
import androidx.camera.core.CameraInfo;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.app.ActivityCompat;

import com.pwr.pjmassistant.R;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class SettingsActivity extends AppCompatActivity
{
    private final String TAG = "settings.SettingsActivity";
    private final String PREFERENCES_KEY = "user-prefs-key";
    private ArrayList<String> cameraList;
    private SharedPreferences settings;

    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            result -> {
                if (!result)
                {
                    Toast.makeText(this.getApplicationContext(), R.string.cameraPermissionError,
                            Toast.LENGTH_SHORT).show();
                }
                else
                {
                    getAvailableCameras();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        settings = getApplicationContext().getSharedPreferences(PREFERENCES_KEY, 0);

        getAvailableCameras();

        EditText intervalText = requireViewById(R.id.editTextTranslationInterval);
        intervalText.setText(String.valueOf(settings.getInt("interval", 500)));

        Spinner cameraSpinner = requireViewById(R.id.modeSpinner);
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

    @OptIn(markerClass = ExperimentalCamera2Interop.class)
    private void getAvailableCameras()
    {
        cameraList = new ArrayList<>();

        if (!checkPermissions())
        {
            cameraList.add("----------------");
            updateSpinner(0);
            return;
        }

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

        if(cameraList.isEmpty())
        {
            cameraList.add("----------------");
            updateSpinner(0);
        }
        else
        {
            updateSpinner(Integer.parseInt(settings.getString("CameraId", "0")));
        }
    }

    void updateSpinner(int index)
    {
        Spinner cameraSpinner = findViewById(R.id.modeSpinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, cameraList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        cameraSpinner.setAdapter(adapter);

        cameraSpinner.setSelection(index);
    }

    private int validateInterval(String interval)
    {
        try
        {
            return Integer.parseInt(interval);
        }
        catch (NumberFormatException ex)
        {
            return 500;
        }
    }

    private boolean checkPermissions()
    {
        if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED)
        {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
            return false;
        }
        return true;
    }

    @Override
    protected void onPause()
    {
        EditText intervalText = requireViewById(R.id.editTextTranslationInterval);

        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("interval", validateInterval(String.valueOf(intervalText.getText())));
        editor.apply();

        super.onPause();
    }
}