package com.pwr.pjmassistant.ui;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.pwr.pjmassistant.R;
import com.pwr.pjmassistant.databinding.FragmentReadBinding;
import com.pwr.pjmassistant.model.Model;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ReadFragment extends Fragment
{
    private final String TAG = "ui.ReadFragment";

    private static boolean isReloading = true;
    private FragmentReadBinding binding;
    private ImageCapture capture;
    private EditText output;
    private String cameraId;
    private Model model;
    private int interval;
    private boolean modelReady = false;
    private Executor executor = Executors.newSingleThreadExecutor();
    private boolean started = false;
    private static Handler threadHandler;

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

        threadHandler = new Handler(Looper.getMainLooper());

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        output = requireView().findViewById(R.id.translatedText);
        output.setInputType(InputType.TYPE_NULL);
        output.setTextIsSelectable(true);

        loadSettings();
        if (checkPermissions())
        {
            getCamera();
        }

        binding.clearButton.setOnClickListener(this::clearView);
        binding.useButton.setOnClickListener(this::recognitionController);

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
        interval = settings.getInt("interval", 500);
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

            prepareImageCapture();

            cameraProvider.unbindAll();
            cameraProvider.bindToLifecycle(this, cameraSelector, capture, cameraPreview);
        }
        catch (ExecutionException | InterruptedException error)
        {
            Log.e(TAG, error.getMessage());
            handleCameraError();
        }
    }

    private void prepareImageCapture()
    {
        capture = new ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .setTargetRotation(requireView().getDisplay().getRotation())
                .build();
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

    @OptIn(markerClass = ExperimentalGetImage.class)
    private void getImageLoop()
    {
        if (!started)
        {
            return;
        }
        capture.takePicture(executor, new ImageCapture.OnImageCapturedCallback()
        {
            @Override
            public void onCaptureSuccess(@NonNull ImageProxy proxy)
            {
                Image image = proxy.getImage();
                assert image != null;
                ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                byte[] bytes = new byte[buffer.capacity()];
                buffer.get(bytes);
                getImage(BitmapFactory.decodeByteArray(bytes, 0, bytes.length, null));
                super.onCaptureSuccess(proxy);
            }
        });

        threadHandler.postDelayed(this::getImageLoop, interval);
    }

    private void getImage(Bitmap image)
    {
        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        image = Bitmap.createBitmap(image, 0, 0, image.getWidth(), image.getHeight(), matrix, true);
        String symbol = model.recognizeSymbol(image);

        requireActivity().runOnUiThread(() -> {
            EditText result = requireView().findViewById(R.id.translatedText);
            String text = String.valueOf(result.getText());
            text = text + symbol;
            result.setText(text);
        });
    }

    private void recognitionController(View view)
    {
        if (!modelReady)
        {
            return;
        }

        Button button = requireActivity().findViewById(R.id.useButton);
        if (started)
        {
            stopRecognition();
            button.setText(R.string.startText);
        }
        else
        {
            startRecognition();
            button.setText(R.string.stopText);
        }
    }

    private void startRecognition()
    {
        output.setText("");
        threadHandler.postDelayed(this::getImageLoop, interval);
        started = true;
    }

    private void clearView(View view)
    {
        EditText text = requireActivity().findViewById(R.id.translatedText);
        text.setText("");
    }

    private void stopRecognition()
    {
        started = false;
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