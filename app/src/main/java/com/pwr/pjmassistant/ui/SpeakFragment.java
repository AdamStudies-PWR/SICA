package com.pwr.pjmassistant.ui;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import com.pwr.pjmassistant.R;
import com.pwr.pjmassistant.databinding.FragmentSpeakBinding;

import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import java.util.Objects;

public class SpeakFragment extends Fragment
{
    private final String PREFERENCES_KEY = "user-prefs-key";
    private static boolean isReloading = true;
    private FragmentSpeakBinding binding;

    private ImageView imageView;
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState)
    {
        binding = FragmentSpeakBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        imageView = requireView().findViewById(R.id.imageView);

        SharedPreferences settings = requireContext().getSharedPreferences(PREFERENCES_KEY, 0);
        boolean useAmerican = settings.getString("model", "0").equals("0");

        Bitmap bitImage = BitmapFactory.decodeResource(this.getResources(), useAmerican ? R.raw.american : R.raw.polish);
        imageView.setImageBitmap(bitImage);

        binding.translateButton.setOnClickListener(translateButton -> {
            EditText source = requireView().findViewById(R.id.inputText);
            source.setText("");
        });

        EditText text = requireView().findViewById(R.id.)

        binding.translateButton.setOnClickListener(translateButton -> {

        });
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
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