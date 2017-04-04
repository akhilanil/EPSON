package com.example.akhil.epson;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.util.Log;
import android.widget.EditText;

import com.example.akhil.decode.RSAEncrypt;

import java.util.ArrayList;

/**
 * Created by akhil on 26/3/17.
 */

public class SettingsActivity extends PreferenceActivity {


    public final static String PREF_IP = "PREF_IP_ADDRESS";
    public final static String PREF_PORT = "PREF_PORT_NUMBER";

    @Override
    public Context getApplicationContext() {
        return super.getApplicationContext();
    }

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

    @Override
    protected void onDestroy() {
        Log.d("DONE","Settings1");
        super.onDestroy();
    }

    public static class SettingsFragment extends PreferenceFragment {

        EditTextPreference ipAddress;
        EditTextPreference port;
        EditTextPreference changekey;

        private final String IP_EDITPREFERENCE_KEY = "ipaddress";
        private final String PORT_EDITPREFERENCE_KEY = "port";
        public final static String CHANGE_KEY = "changekey";

        @Override
        public Context getContext() {
            return super.getContext();
        }

        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.settings);
            ipAddress = (EditTextPreference) findPreference(IP_EDITPREFERENCE_KEY);
            port = (EditTextPreference) findPreference(PORT_EDITPREFERENCE_KEY);
            changekey = (EditTextPreference) findPreference(CHANGE_KEY);

            ipAddress.setSummary(sharedPreferences.getString(PREF_IP,"IP ADDRESS"));
            port.setSummary(sharedPreferences.getString(PREF_PORT,"PORT"));


            if(!RemoteActivity.isRC4Initialized && RemoteActivity.connectionStatus.equals(ConnectionStatus.AUTH_FAIL)) {

                changekey.setEnabled(true);

            }
            else {
                changekey.setEnabled(false);
                if (RemoteActivity.connectionStatus.equals(ConnectionStatus.SUCCESS))
                    changekey.setSummary("SUCCESSFULLE AUTHENTICATED");
                else
                    changekey.setSummary("CHECK CONNECTION");
            }




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
        @Override
        public void onDestroy() {
            Log.d("DONE","Settings2");
            super.onDestroy();
        }

        SharedPreferences.OnSharedPreferenceChangeListener listener =
                new SharedPreferences.OnSharedPreferenceChangeListener() {
                    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                        if(key.equals(IP_EDITPREFERENCE_KEY)) {
                            String value = ipAddress.getText().toString().trim();
                            if(!value.equals("")) {
                                Log.d("CLICK", value);
                                ipAddress.setSummary(value);
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
                        else if(key.equals(CHANGE_KEY)) {

                            String value = changekey.getText().toString();
                            if(!value.equals("")) {
                                changeRC4key(value);
                            }
                        }
                    }
                };

        public void changeRC4key(String newKey) {

            final String password = newKey;
            final ArrayList<String> parameterValue = new ArrayList<String>();

            final ProgressDialog progressCircle = new ProgressDialog(getContext());
            progressCircle.setCancelable(false);
            progressCircle.setMessage("Please Wait...");
            progressCircle.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressCircle.show();

            new Thread(new Runnable() {
                @Override
                public void run() {

                    String encryptedPassword;
                    RequestMode requestType;
                    String ipAddress = sharedPreferences.getString(PREF_IP, "");
                    String portNumber = sharedPreferences.getString(PREF_PORT, "");

                    RSAEncrypt rsaEncrypt = new RSAEncrypt(RemoteActivity.encryptionKeys);
                    encryptedPassword = rsaEncrypt.encryptPassword(password);


                    parameterValue.add(0, ParameterFactory.getMode(RequestMode.RC4KEY));
                    parameterValue.add(1,encryptedPassword);
                    requestType = RequestMode.RC4KEY;

                    RemoteActivity.signal = 0;
                    new HttpRequestAsyncTask(parameterValue, ipAddress, portNumber,
                            requestType).execute();

                    while(RemoteActivity.signal == 0) {
                        try {
                            Thread.sleep(1000);
                        } catch (Exception e) {
                            Log.d("ERROR1", e.getMessage());
                            e.printStackTrace();
                        }
                    }
                    if(RemoteActivity.connectionStatus.equals(ConnectionStatus.AUTH_SUCCESS)) {
                        RemoteActivity.change = true;
                        RemoteActivity.isRC4Initialized = true;

                    }
                    else

                    progressCircle.dismiss();


                }
            }).start();





        }

    }

}
