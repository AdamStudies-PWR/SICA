package com.pwr.pjmassistant.ui;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import com.pwr.pjmassistant.R;
import com.pwr.pjmassistant.databinding.FragmentSpeakBinding;

import java.util.Objects;

public class SpeakFragment extends Fragment
{
    private FragmentSpeakBinding binding;

    private ImageView imageView;
    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState)
    {
        binding = FragmentSpeakBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        imageView = requireView().findViewById(R.id.imageView);

        Bitmap bitImage = BitmapFactory.decodeResource(this.getResources(), R.raw.pjm_placeholder);
        imageView.setImageBitmap(bitImage);

        binding.translateButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                EditText source = requireView().findViewById(R.id.inputText);
                source.setText("");
            }
        });
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        binding = null;
    }
}