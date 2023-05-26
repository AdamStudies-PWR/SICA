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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class SpeakFragment extends Fragment
{
    private final String PREFERENCES_KEY = "user-prefs-key";
    private static boolean isReloading = true;
    private FragmentSpeakBinding binding;

    private List<String> allowedAmerican = Arrays.asList("A", "B", "C", "D", "E", "F", "G", "H",
            "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"
    );
    private List<String> allowedPolish = Arrays.asList("A", "Ą", "B", "C", "CH", "Ć", "CZ", "D",
            "E", "Ę", "F", "G", "H", "I", "J", "K", "L", "Ł", "M", "N", "Ń", "O", "Ó", "P",
            "R", "RZ", "S", "Ś", "SZ", "T", "U", "W", "Y", "Z", "Ź", "Ż");

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

        EditText source = requireView().findViewById(R.id.inputText);
        binding.translateButton.setOnClickListener(translateButton -> {
            source.setText("");
        });

        binding.translateButton.setOnClickListener(translateButton -> {
            parseText(String.valueOf(source.getText()));
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

    public void parseText(String input)
    {
        if (input.isEmpty() || input.equals(""))
        {
            return;
        }


    }
}