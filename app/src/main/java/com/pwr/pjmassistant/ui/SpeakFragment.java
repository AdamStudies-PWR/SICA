package com.pwr.pjmassistant.ui;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.pwr.pjmassistant.R;
import com.pwr.pjmassistant.databinding.FragmentSpeakBinding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class SpeakFragment extends Fragment
{
    private final String PREFERENCES_KEY = "user-prefs-key";
    private static boolean isReloading = true;
    private FragmentSpeakBinding binding;

    private final List<String> allowedAmerican = Arrays.asList("A", "B", "C", "D", "E", "F", "G", "H",
            "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"
    );
    private final List<String> allowedPolish = Arrays.asList("A", "Ą", "B", "C", "CH", "Ć", "CZ", "D",
            "E", "Ę", "F", "G", "H", "I", "J", "K", "L", "Ł", "M", "N", "Ń", "O", "Ó", "P",
            "R", "RZ", "S", "Ś", "SZ", "T", "U", "W", "Y", "Z", "Ź", "Ż");

    private ImageView imageView;
    private ArrayList<Integer> resourceIds = new ArrayList<>();

    private Integer index = 0;

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
        resetView(useAmerican);

        EditText source = requireView().findViewById(R.id.inputText);

        binding.translateButton.setOnClickListener(translateButton -> source.setText(""));
        binding.translateButton.setOnClickListener(
                translateButton -> parseText(String.valueOf(source.getText()), useAmerican));
        binding.nextButton.setOnClickListener(this::nextPicture);
        binding.previousButton.setOnClickListener(this::previousPicture);
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

    public void nextPicture(View view)
    {
        if (index == (resourceIds.size() - 1))
        {
            return;
        }

        index++;

        if (index == (resourceIds.size() - 1))
        {
            Button next = requireView().findViewById(R.id.nextButton);
            next.setEnabled(false);
        }

        Button previous = requireView().findViewById(R.id.previousButton);
        previous.setEnabled(true);
        displaySymbol();
    }

    public void previousPicture(View view)
    {
        if (index == 0)
        {
            return;
        }

        index--;

        if (index == 0)
        {
            Button previous = requireView().findViewById(R.id.previousButton);
            previous.setEnabled(false);
        }

        Button next = requireView().findViewById(R.id.nextButton);
        next.setEnabled(true);
        displaySymbol();
    }

    private void parseText(String input, boolean american)
    {
        LinearLayout layout = requireView().findViewById(R.id.navigationContainer);

        if (input.isEmpty() || input.equals(""))
        {
            layout.setVisibility(View.GONE);
            resetView(american);
            return;
        }

        input = input.replace(" ", "");
        resourceIds = american ? getAmerican(input) : getPolish(input);

        if (resourceIds.isEmpty())
        {
            layout.setVisibility(View.GONE);
            resetView(american);
            Toast.makeText(requireContext(), R.string.sign_not_supported,
                    Toast.LENGTH_SHORT).show();
            return;
        }

        Button button = requireView().findViewById(R.id.previousButton);
        button.setEnabled(false);

        layout.setVisibility(View.VISIBLE);
        index = 0;
        displaySymbol();
    }

    private void displaySymbol()
    {
        Toast.makeText(requireContext(), (index + 1) + "/" + resourceIds.size(),
                Toast.LENGTH_SHORT).show();

        Bitmap bitImage = BitmapFactory.decodeResource(this.getResources(), resourceIds.get(index));
        imageView.setImageBitmap(bitImage);
    }

    private ArrayList<Integer> getAmerican(String input)
    {
        char[] chars = input.toCharArray();
        ArrayList<Integer> resources = new ArrayList<>();

        for (char ch : chars)
        {
            if (allowedAmerican.contains(String.valueOf(ch).toUpperCase(Locale.ROOT)))
            {
                resources.add(charToAmerican(String.valueOf(ch).toUpperCase()));
            }
            else
            {
                return new ArrayList<>();
            }
        }

        return resources;
    }

    private ArrayList<Integer> getPolish(String input)
    {
        char[] chars = input.toCharArray();
        ArrayList<Integer> resources = new ArrayList<>();

        for (int i=0; i<chars.length; i++)
        {
            String letter = String.valueOf(chars[i]).toUpperCase(Locale.ROOT);
            if (letter.equals("C") && (i + 1) < chars.length)
            {
                String next = String.valueOf(chars[i + 1]).toUpperCase(Locale.ROOT);
                if (next.equals("H") || next.equals("Z"))
                {
                    letter = letter + next;
                    resources.add(charToPolish(letter));
                    i++;
                }
            }
            else if (letter.equals("R") && (i + 1) < chars.length)
            {
                String next = String.valueOf(chars[i + 1]).toUpperCase(Locale.ROOT);
                if (next.equals("Z"))
                {
                    letter = letter + next;
                    resources.add(charToPolish(letter));
                    i++;
                }
            }
            else if (letter.equals("S") && (i + 1) < chars.length)
            {
                String next = String.valueOf(chars[i + 1]).toUpperCase(Locale.ROOT);
                if (next.equals("Z"))
                {
                    letter = letter + next;
                    resources.add(charToPolish(letter));
                    i++;
                }
            }
            else if (allowedPolish.contains(letter))
            {
                resources.add(charToPolish(letter));
            }
            else
            {
                return new ArrayList<>();
            }
        }

        return resources;
    }

    private Integer charToAmerican(String letter)
    {
        switch (letter)
        {
            case "A": return R.raw.american_a;
            case "B": return R.raw.american_b;
            case "C": return R.raw.american_c;
            case "D": return R.raw.american_d;
            case "E": return R.raw.american_e;
            case "F": return R.raw.american_f;
            case "G": return R.raw.american_g;
            case "H": return R.raw.american_h;
            case "I": return R.raw.american_i;
            case "J": return R.raw.american_j;
            case "K": return R.raw.american_k;
            case "L": return R.raw.american_l;
            case "M": return R.raw.american_m;
            case "N": return R.raw.american_n;
            case "O": return R.raw.american_o;
            case "P": return R.raw.american_p;
            case "Q": return R.raw.american_q;
            case "R": return R.raw.american_r;
            case "S": return R.raw.american_s;
            case "T": return R.raw.american_t;
            case "U": return R.raw.american_u;
            case "V": return R.raw.american_v;
            case "W": return R.raw.american_w;
            case "X": return R.raw.american_x;
            case "Y": return R.raw.american_y;
            case "Z": return R.raw.american_z;
        }

        return R.raw.error_msg;
    }

    private Integer charToPolish(String letter)
    {
        switch (letter)
        {
            case "A": return R.raw.polish_a;
            case "Ą": return R.raw.polish_ao;
            case "B": return R.raw.polish_b;
            case "C": return R.raw.polish_c;
            case "CH": return R.raw.polish_ch;
            case "Ć": return R.raw.polish_ci;
            case "CZ": return R.raw.polish_cz;
            case "D": return R.raw.polish_d;
            case "E": return R.raw.polish_e;
            case "Ę": return R.raw.polish_eo;
            case "F": return R.raw.polish_f;
            case "G": return R.raw.polish_g;
            case "H": return R.raw.polish_h;
            case "I": return R.raw.polish_i;
            case "J": return R.raw.polish_j;
            case "K": return R.raw.polish_k;
            case "L": return R.raw.polish_l;
            case "M": return R.raw.polish_m;
            case "N": return R.raw.polish_n;
            case "O": return R.raw.polish_o;
            case "P": return R.raw.polish_p;
            case "R": return R.raw.polish_r;
            case "S": return R.raw.polish_s;
            case "Ś": return R.raw.polish_si;
            case "SZ": return R.raw.polish_sz;
            case "T": return R.raw.polish_t;
            case "U": return R.raw.polish_u;
            case "W": return R.raw.polish_w;
            case "Y": return R.raw.polish_y;
            case "Z": return R.raw.polish_z;
            case "Ź": return R.raw.polish_zi;
            case "Ż": return R.raw.polish_zz;
        }

        return R.raw.error_msg;
    }

    private void resetView(boolean american)
    {
        Bitmap bitImage = BitmapFactory.decodeResource(this.getResources(),
                american ? R.raw.american : R.raw.polish);
        imageView.setImageBitmap(bitImage);
    }
}