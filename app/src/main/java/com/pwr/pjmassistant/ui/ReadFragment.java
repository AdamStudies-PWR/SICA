package com.pwr.pjmassistant.ui;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
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
import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.view.PreviewView;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.pwr.pjmassistant.R;
import com.pwr.pjmassistant.databinding.FragmentReadBinding;

import java.util.concurrent.ExecutionException;

public class ReadFragment extends Fragment
{
    private final String TAG = "ui.ReadFragment";

    private FragmentReadBinding binding;
    private EditText output;

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
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        output = requireView().findViewById(R.id.translatedText);
        output.setInputType(InputType.TYPE_NULL);
        output.setTextIsSelectable(true);

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
            CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

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
        binding = null;
    }

}