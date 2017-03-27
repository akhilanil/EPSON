package com.example.akhil.epson;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.util.Log;
import android.widget.EditText;

/**
 * Created by akhil on 26/3/17.
 */

public class SettingsActivity extends PreferenceActivity {


    public final static String PREF_IP = "PREF_IP_ADDRESS";
    public final static String PREF_PORT = "PREF_PORT_NUMBER";
    static SharedPreferences sharedPreferences;
    static SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content,
                new SettingsFragment()).commit();

        sharedPreferences = getSharedPreferences("HTTP_HELPER_PREFS", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }


    public static class SettingsFragment extends PreferenceFragment {

        EditTextPreference ipAddress;
        EditTextPreference port;
        private final String IP_EDITPREFERENCE_KEY = "ipaddress";
        private final String PORT_EDITPREFERENCE_KEY = "port";


        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.settings);
            ipAddress = (EditTextPreference) findPreference(IP_EDITPREFERENCE_KEY);
            port = (EditTextPreference) findPreference(PORT_EDITPREFERENCE_KEY);

            ipAddress.setSummary(sharedPreferences.getString(PREF_IP,"IP ADDRESS"));
            port.setSummary(sharedPreferences.getString(PREF_PORT,"PORT"));

        }

        @Override
        public void onResume() {
            super.onResume();
            getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(listener);

        }


        @Override
        public void onPause() {
            super.onPause();
            getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(listener);

        }

        SharedPreferences.OnSharedPreferenceChangeListener listener =
                new SharedPreferences.OnSharedPreferenceChangeListener() {
                    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                        if(key.equals(IP_EDITPREFERENCE_KEY)) {
                            String value = ipAddress.getText().toString().trim();
                            if(!value.equals("")) {
                                Log.d("CLICK", value);
                                editor.putString(PREF_IP,value);
                                editor.commit();
                            }
                        }
                        else if(key.equals(PORT_EDITPREFERENCE_KEY)) {
                            String value = port.getText().toString().trim();
                            if(!value.equals("")) {
                                Log.d("CLICK", value);
                                editor.putString(PREF_PORT,value);
                                editor.commit();
                            }
                        }
                    }
                };


    }

}
