package com.pwr.pjmassistant.settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import com.pwr.pjmassistant.R;

import java.util.ArrayList;

public class SettingsActivity extends AppCompatActivity
{
    private final String TAG = "settings.SettingsActivity";
        private final String PREFERENCES_KEY = "user-prefs-key";
    private ArrayList<String> cameraList;
    private SharedPreferences settings;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        settings = getApplicationContext().getSharedPreferences(PREFERENCES_KEY, 0);

        EditText appearanceText = requireViewById(R.id.editTextAppearance);
        appearanceText.setText(String.valueOf(settings.getInt("threshold", 10)));

        Spinner modelSpinner = requireViewById(R.id.modeSpinner);
        modelSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l)
            {
                SharedPreferences.Editor editor = settings.edit();
                editor.putString("model", String.valueOf(i));
                editor.apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        modelSpinner.setSelection(Integer.parseInt(settings.getString("model", "0")));
    }

    private int validateThreshold(String interval)
    {
        try
        {
            return Integer.parseInt(interval);
        }
        catch (NumberFormatException ex)
        {
            return 10;
        }
    }

    @Override
    protected void onPause()
    {
        EditText appearanceText = requireViewById(R.id.editTextAppearance);

        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("threshold", validateThreshold(String.valueOf(appearanceText.getText())));
        editor.apply();

        super.onPause();
    }
}