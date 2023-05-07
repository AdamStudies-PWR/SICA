package com.pwr.pjmassistant.ui;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.camera.camera2.interop.Camera2CameraInfo;
import androidx.camera.camera2.interop.ExperimentalCamera2Interop;
import androidx.camera.core.CameraInfo;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.pwr.pjmassistant.R;
import com.pwr.pjmassistant.databinding.FragmentReadBinding;
import com.pwr.pjmassistant.model.Model;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class ReadFragment extends Fragment
{
    private final String TAG = "ui.ReadFragment";

    private static boolean isReloading = true;
    private FragmentReadBinding binding;
    private EditText output;
    private String cameraId;
    private Model model;
    private boolean modelReady = false;


    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            result -> {
                if (result) { getCamera(); }
                else
                {
                    Toast.makeText(requireActivity().getApplicationContext(), R.string.cameraPermissionError,
                            Toast.LENGTH_SHORT).show();
                    handleCameraError();
                }
            }
    );

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState)
    {
        binding = FragmentReadBinding.inflate(inflater, container, false);

        model = new Model("model.tflite");

        if (model.tryLoadModel(requireContext()))
        {
            modelReady = true;
        }
        else
        {
            modelReady = false;
            Toast.makeText(requireContext(), R.string.model_failed, Toast.LENGTH_SHORT).show();
        }

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        output = requireView().findViewById(R.id.translatedText);
        output.setInputType(InputType.TYPE_NULL);
        output.setTextIsSelectable(true);

        loadSettings();
        if (checkPermissions()) getCamera();

        binding.clearButton.setOnClickListener(clearButton -> output.setText(""));

        binding.pauseButton.setOnClickListener(pauseButton -> {
            // TODO
        });

        binding.copyButton.setOnClickListener(copyButton -> {
            ClipboardManager clipboard = (ClipboardManager) requireActivity().getSystemService(
                    Context.CLIPBOARD_SERVICE);
            ClipData data = ClipData.newPlainText("translation", output.getText());
            clipboard.setPrimaryClip(data);
        });
    }

    private void loadSettings()
    {
        String PREFERENCES_KEY = "user-prefs-key";
        SharedPreferences settings = requireActivity().getApplicationContext().getSharedPreferences(PREFERENCES_KEY, 0);;
        cameraId = settings.getString("CameraId", "null");
    }

    private boolean checkPermissions()
    {
        if (ActivityCompat.checkSelfPermission(requireActivity().getApplicationContext(),
                Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED)
        {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
            return false;
        }
        return true;
    }

    @OptIn(markerClass = ExperimentalCamera2Interop.class)
    private void getCamera()
    {
        if (!requireActivity().getApplicationContext().getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY))
        {
            handleCameraError();
            return;
        }

        PreviewView cameraPreviewView = requireView().findViewById(R.id.cameraPreview);
        TextView cameraError = requireActivity().findViewById(R.id.cameraInfo);
        TextView translatedTextView = requireView().findViewById(R.id.translatedText);
        ImageView cameraView = requireView().findViewById(R.id.cameraView);
        cameraError.setVisibility(View.GONE);
        cameraView.setVisibility(View.GONE);
        translatedTextView.setVisibility(View.VISIBLE);
        cameraPreviewView.setVisibility(View.VISIBLE);

        try
        {
            ProcessCameraProvider cameraProvider = ProcessCameraProvider.getInstance(
                    requireActivity().getApplicationContext()).get();

            Preview cameraPreview = new Preview.Builder().build();
            cameraPreview.setSurfaceProvider(cameraPreviewView.getSurfaceProvider());
            CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;;

            List<CameraInfo> cameraInfos = cameraProvider.getAvailableCameraInfos();
            for (CameraInfo info : cameraInfos)
            {
                if (Camera2CameraInfo.from(info).getCameraId().equals(cameraId))
                {
                    cameraSelector = info.getCameraSelector();
                }
            }

            cameraProvider.unbindAll();
            cameraProvider.bindToLifecycle(this, cameraSelector, cameraPreview);
        }
        catch (ExecutionException | InterruptedException error)
        {
            Log.e(TAG, error.getMessage());
            handleCameraError();
        }
    }

    private void handleCameraError()
    {
        Toast.makeText(requireActivity().getApplicationContext(), R.string.cameraException,
                Toast.LENGTH_SHORT).show();
        PreviewView cameraPreviewView = requireView().findViewById(R.id.cameraPreview);
        TextView translatedTextView = requireView().findViewById(R.id.translatedText);
        TextView cameraError = requireActivity().findViewById(R.id.cameraInfo);
        ImageView cameraView = requireView().findViewById(R.id.cameraView);
        cameraError.setVisibility(View.VISIBLE);
        translatedTextView.setVisibility(View.GONE);
        cameraView.setVisibility(View.VISIBLE);
        cameraPreviewView.setVisibility(View.GONE);
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        isReloading = true;
        binding = null;
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (isReloading)
        {
            isReloading = false;
            return;
        }

        getParentFragmentManager().beginTransaction().detach(this).commit();
        getParentFragmentManager().beginTransaction().attach(this).commit();
        isReloading = true;
    }
}