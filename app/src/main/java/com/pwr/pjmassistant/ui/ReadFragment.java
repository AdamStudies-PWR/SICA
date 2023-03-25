package com.pwr.pjmassistant.ui;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.pwr.pjmassistant.R;
import com.pwr.pjmassistant.databinding.FragmentReadBinding;

import java.util.Objects;

public class ReadFragment extends Fragment
{

    private FragmentReadBinding binding;
    private EditText output;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
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

        binding.clearButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                output.setText("");
            }
        });

        binding.pauseButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                // TODO
            }
        });

        binding.copyButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                ClipboardManager clipboard = (ClipboardManager) requireActivity().getSystemService(
                        Context.CLIPBOARD_SERVICE);
                ClipData data = ClipData.newPlainText("translation", output.getText());
                clipboard.setPrimaryClip(data);
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