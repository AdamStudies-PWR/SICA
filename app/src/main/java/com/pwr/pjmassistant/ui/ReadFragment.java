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
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.pwr.pjmassistant.R;
import com.pwr.pjmassistant.databinding.FragmentReadBinding;
import com.pwr.pjmassistant.model.HandDetection;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

public class ReadFragment extends Fragment implements CameraBridgeViewBase.CvCameraViewListener2
{
    private final String TAG = "ui.ReadFragment";

    private static boolean isReloading = true;
    private FragmentReadBinding binding;
    private EditText output;
    private HandDetection handDetection;
    private boolean modelReady = false;
    private boolean started = false;

    private Mat mRgba;

    private CameraBridgeViewBase cameraBridgeViewBase;
    private BaseLoaderCallback loaderCallback;

    private final ActivityResultLauncher<String> requestPermissionLauncher
            = registerForActivityResult(new ActivityResultContracts.RequestPermission(), result -> {
                if (result) { startCamera(); }
                else
                {
                    Toast.makeText(requireActivity().getApplicationContext(),
                            R.string.cameraPermissionError, Toast.LENGTH_SHORT).show();
                    handleCameraError();
                }
            });

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState)
    {
        binding = FragmentReadBinding.inflate(inflater, container, false);

        loaderCallback = new BaseLoaderCallback(requireContext())
        {
            @Override
            public void onManagerConnected(int status)
            {
                if (status == LoaderCallbackInterface.SUCCESS)
                {
                    Log.i(TAG, "OpenCv loaded");
                    cameraBridgeViewBase.enableView();
                }

                super.onManagerConnected(status);
            }
        };

        handDetection = new HandDetection("model.tflite", 300);

        if (handDetection.tryLoadModel(requireContext().getAssets()))
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
        requireActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        cameraBridgeViewBase = requireView().findViewById(R.id.cameraPreview);

        output = requireView().findViewById(R.id.translatedText);
        output.setInputType(InputType.TYPE_NULL);
        output.setTextIsSelectable(true);

        if (checkPermissions())
        {
            startCamera();
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

        if (OpenCVLoader.initDebug())
        {
            Log.d(TAG, "OpenCv is initialized");
            loaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
        else
        {
            Log.d(TAG, "OpenCv not loaded, attempting to reinitialize");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0, requireContext(), loaderCallback);
        }

        if (isReloading)
        {
            isReloading = false;
            return;
        }

        getParentFragmentManager().beginTransaction().detach(this).commit();
        getParentFragmentManager().beginTransaction().attach(this).commit();
        isReloading = true;
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (cameraBridgeViewBase != null)
        {
            cameraBridgeViewBase.disableView();
        }
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        if (cameraBridgeViewBase != null)
        {
            cameraBridgeViewBase.disableView();
        }
    }

    @Override
    public void onCameraViewStarted(int width, int height)
    {
        Log.i(TAG, "cameraView was started");
        mRgba = new Mat(height, width, CvType.CV_8UC4);
    }

    @Override
    public void onCameraViewStopped()
    {
        mRgba.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame)
    {
        mRgba = inputFrame.rgba();

        if (modelReady && started)
        {
            mRgba = handDetection.getHand(mRgba);
        }

        return mRgba;
    }

    private void startCamera()
    {
        Log.i(TAG, "start camera called");

        TextView cameraError = requireView().findViewById(R.id.cameraInfo);
        TextView translatedTextView = requireView().findViewById(R.id.translatedText);
        ImageView cameraView = requireView().findViewById(R.id.cameraView);
        cameraError.setVisibility(View.GONE);
        cameraView.setVisibility(View.GONE);
        translatedTextView.setVisibility(View.VISIBLE);

        cameraBridgeViewBase.setVisibility(View.VISIBLE);
        cameraBridgeViewBase.setCameraPermissionGranted();
        cameraBridgeViewBase.setCvCameraViewListener(this);
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

    private void handleCameraError()
    {
        Toast.makeText(requireActivity().getApplicationContext(), R.string.cameraException,
                Toast.LENGTH_SHORT).show();
        TextView translatedTextView = requireView().findViewById(R.id.translatedText);
        TextView cameraError = requireActivity().findViewById(R.id.cameraInfo);
        ImageView cameraView = requireView().findViewById(R.id.cameraView);
        cameraError.setVisibility(View.VISIBLE);
        translatedTextView.setVisibility(View.GONE);
        cameraView.setVisibility(View.VISIBLE);
        cameraBridgeViewBase.setVisibility(View.GONE);
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
        started = true;
        // TODO: do something here to start grabing hands
    }

    private void clearView(View view)
    {
        EditText text = requireView().findViewById(R.id.translatedText);
        text.setText("");
    }

    private void stopRecognition()
    {
        started = false;
        // TODO: This should stop searching for hands
    }
}