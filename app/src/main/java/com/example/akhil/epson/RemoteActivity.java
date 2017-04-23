package com.example.akhil.epson;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;


import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.bumptech.glide.signature.StringSignature;
import com.example.akhil.decode.EncryptCode;
import com.example.akhil.decode.EncryptionKeys;
import com.example.akhil.decode.InitRC4;
import com.example.akhil.decode.RSAEncrypt;
import com.example.akhil.decode.RemoteCode;
import com.example.akhil.voice.RequestType;
import com.example.akhil.voice.ResponseStatus;
import com.example.akhil.voice.ResponseVoice;
import com.example.akhil.voice.ResponseWrapper;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;


public class RemoteActivity extends AppCompatActivity {

    static SharedPreferences sharedPreferences;
    public final static String PREF_IP = "PREF_IP_ADDRESS";
    public final static String PREF_PORT = "PREF_PORT_NUMBER";

    public static EncryptionKeys encryptionKeys;
    public static InitRC4 initRC4;
    public static RemoteCode remoteCode;
    public static EncryptCode encryptCode;

    public static String finalipAddress,finalportNumber;

    public static TextToSpeech textToSpeech;

    private static boolean isBackButtonPressed = false;
    public static final int REQ_CODE_SPEECH_INPUT = 100;

    public static ImageView voiceResponseGif;
    public static TextView voiceResponseText;

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private static ViewPager mViewPager;

    /*Variables for cntrolling apps' functioning*/
    public static boolean change;
    public static ConnectionStatus connectionStatus;
    public static int signal = 0; // Indicates whether change in page is needed
    /*Variable used to check whether RC4 key is initialized */
    public static boolean isRC4Initialized;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remote);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

                if(RemoteActivity.change) {
                    RemoteActivity.change = !RemoteActivity.change;
                    mViewPager.getAdapter().notifyDataSetChanged();
                }

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });



        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        sharedPreferences =
                getSharedPreferences("HTTP_HELPER_PREFS", Context.MODE_PRIVATE);

        //set default values
        setDefaults();

        Bundle extras = getIntent().getExtras();
        if(extras != null)
            connectionStatus = (ConnectionStatus) extras.get("status");

        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    textToSpeech.setLanguage(Locale.UK);
                }
            }
        });



        startService(new Intent(this, NetworkDisconnect.class));




    }


    @Override
    protected void onDestroy() {
        Log.d("DONE","Remote");
        //disconnectRemote();
        textToSpeech.stop();
        textToSpeech.shutdown();
        super.onDestroy();
    }


    @Override
    public void onBackPressed() {

        if(!isBackButtonPressed) {
            isBackButtonPressed = !isBackButtonPressed;
            Toast.makeText(RemoteActivity.this, "Press Again to Exit", Toast.LENGTH_SHORT).show();
        }
        else {
            disconnectRemote();
            finish();
        }
    }


    private void disconnectRemote () {

        new Thread(new Runnable() {
            @Override
            public void run() {
                ArrayList<String> parameterValue = new ArrayList<String>();
                String ipAddress = sharedPreferences.getString(PREF_IP,"192.168.2.101");
                String port = sharedPreferences.getString(PREF_PORT,"80");
                parameterValue.add(0,ParameterFactory.getMode(RequestMode.FINISH));

                RemoteActivity.signal = 0;
                new HttpRequestAsyncTask(parameterValue, ipAddress, port, RequestMode.FINISH).execute();
                while(RemoteActivity.signal == 0) {
                    try{Thread.sleep(1000);}catch (Exception e){}
                }
                Log.d("DONE","DESTROYED");

            }
        }).start();

    }
    /*Sets all controlling values to default  */
    public static void setDefaults() {


        Log.d("Steps","DEFAULT" );
        RemoteActivity.connectionStatus = ConnectionStatus.FAIL;
        RemoteActivity.signal = 0;
        RemoteActivity.change = false;
        RemoteActivity.isRC4Initialized = false;

        //RemoteActivity.connectionStatus = ConnectionStatus.SUCCESS;
        //RemoteActivity.isRC4Initialized = true;

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_remote, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            //On Selecting Settings
            Intent settings = new Intent(RemoteActivity.this, SettingsActivity.class);
            startActivity(settings);
            return true;
        }

        if(id == R.id.action_reload) {


            /*Sending new Http request and reloading the Activity */

            final ArrayList<String> parameterValue = new ArrayList<String>();

            parameterValue.add(0,ParameterFactory.getMode(RequestMode.INIT));
            final String ipAddress = sharedPreferences.getString(PREF_IP,"192.168.2.101");
            final String portNumber = sharedPreferences.getString(PREF_PORT,"80");
            final RequestMode requestType = RequestMode.RELOAD;
            RemoteActivity.signal = 0;


            final ProgressDialog progressCircle = new ProgressDialog(RemoteActivity.this);
            progressCircle.setCancelable(false);
            progressCircle.setMessage("Reloading...");
            progressCircle.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressCircle.show();


            new Thread(new Runnable() {
                @Override
                public void run() {

                    new HttpRequestAsyncTask(
                            parameterValue, ipAddress, portNumber,
                            requestType).execute();
                    while(RemoteActivity.signal == 0) {
                        Log.d("STEP",String.valueOf(RemoteActivity.signal));
                        try{Thread.sleep(1000);}catch (Exception e){e.printStackTrace();}
                    }
                    progressCircle.dismiss();

                    Intent refreshIntent = getIntent();
                    refreshIntent.putExtra("status",RemoteActivity.connectionStatus);
                    setDefaults();
                    finish();
                    startActivity(refreshIntent);
                }
            }).start();

        }

        if(id == R.id.action_signout ) {

            final ArrayList<String> parameterValue = new ArrayList<String>();
            final String ipAddress = sharedPreferences.getString(PREF_IP,"192.168.2.101");
            final String port = sharedPreferences.getString(PREF_PORT,"80");
            parameterValue.add(0,ParameterFactory.getMode(RequestMode.FINISH));

            final ProgressDialog progressCircle = new ProgressDialog(RemoteActivity.this);
            progressCircle.setCancelable(false);
            progressCircle.setMessage("Signing Out...");
            progressCircle.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressCircle.show();

            new Thread(new Runnable() {
                @Override
                public void run() {

                    RemoteActivity.signal = 0;
                    new HttpRequestAsyncTask(parameterValue, ipAddress, port, RequestMode.FINISH).execute();
                    while(RemoteActivity.signal == 0) {
                        try{Thread.sleep(1000);}catch (Exception e){}
                    }
                    progressCircle.dismiss();
                    try{Thread.sleep(500);}catch (Exception e){}
                    finish();
                    Log.d("STEP","END");
                }
            }).start();
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment{
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {

        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
             View rootView = null;


            if(RemoteActivity.connectionStatus.equals(ConnectionStatus.FAIL)
                    || RemoteActivity.connectionStatus.equals(ConnectionStatus.AUTH_FAIL)) {

                rootView = inflater.inflate(R.layout.fragment_connection_error, container, false);
                Log.d("VALUES","FAIL");
                final Fragment currentFragment = this;
                final Button tryagain = (Button)rootView.findViewById(R.id.tryagain);
                if(RemoteActivity.connectionStatus.equals(ConnectionStatus.AUTH_FAIL)) {

                    TextView errorText  = (TextView) rootView.findViewById(R.id.errortext);
                    if(errorText == null )
                        Log.d("Error","NULL");
                    errorText.setText(getString(R.string.auth_error));

                }

                tryagain.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        tryagain.setEnabled(false);
                        ArrayList<String> parameterValue = new ArrayList<String>();
                        parameterValue.add(0,ParameterFactory.getMode(RequestMode.INIT));

                        String ipAddress = sharedPreferences.getString(PREF_IP,"NULL");
                        String portNumber = sharedPreferences.getString(PREF_PORT,"NULL");

                        /*
                        * RequestType: initagain
                        * Purpose: Connection Request from USER
                        * */

                        RequestMode requestType = RequestMode.INIT_AGAIN;

                        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                        RequestHandler requestHandler = new RequestHandler(v, parameterValue,
                                ipAddress, portNumber,
                                requestType, fragmentTransaction, currentFragment, tryagain);

                        new InitRC4Encryption(requestHandler);
                        new InitRequest(requestHandler);
                        Log.d("Status","CHECK");
                    }
                });
            }
            else if(String.valueOf(getArguments().getInt(ARG_SECTION_NUMBER)).equals("1")) {
                rootView = inflater.inflate(R.layout.fragment_hand, container, false);

                if(!isRC4Initialized) {

                    ArrayList<String> parameterValue = new ArrayList<String>();
                    String ipAddress = sharedPreferences.getString(PREF_IP, "NULL");
                    String portNumber = sharedPreferences.getString(PREF_PORT, "NULL");
                    RequestMode requestType = RequestMode.RC4KEY;

                    final Fragment currentFragment = this;
                    FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    RemoteActivity.signal = 1;
                    RequestHandler requestHandler = new RequestHandler(rootView, parameterValue,
                            ipAddress, portNumber,
                            requestType, fragmentTransaction, currentFragment);

                    new InitRC4Encryption(requestHandler);

                }
                initRemoteView(rootView);
            }
            else if(String.valueOf(getArguments().getInt(ARG_SECTION_NUMBER)).equals("2")) {
                rootView = inflater.inflate(R.layout.fragment_voice, container, false);

                voiceResponseGif = (ImageView) rootView.findViewById(R.id.voiceResponseGif);
                voiceResponseText = (TextView) rootView.findViewById(R.id.voiceResponseText);
                initVoiceView(rootView);
            }
            else if(String.valueOf(getArguments().getInt(ARG_SECTION_NUMBER)).equals("3"))
                rootView = inflater.inflate(R.layout.fragment_help, container, false);


            Log.d("VALUES",String.valueOf(getArguments().getInt(ARG_SECTION_NUMBER)));
            return rootView;
        }



        /*Initialises Listener for voice button*/
        public void initVoiceView(View rootView) {

            ImageButton btnSpeak = (ImageButton) rootView.findViewById(R.id.voice);
            btnSpeak.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    promptSpeechInput();
                }
            });

        }


        private void promptSpeechInput() {


            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                    getString(R.string.speech_prompt));
            try {
                startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
            } catch (ActivityNotFoundException a) {
                Toast.makeText(this.getContext(), "SPEECH NOT SUPORTED", Toast.LENGTH_SHORT).show();
            }

        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {

            super.onActivityResult(requestCode, resultCode, data);

            switch (requestCode) {
                case REQ_CODE_SPEECH_INPUT:

                    final String responseSpeech;
                    String code;
                    ResponseWrapper responseWrapper;
                    ResponseStatus responseStatus;
                    RequestType responseRequestType;

                    if(resultCode == RESULT_OK &&  data != null) {
                        ArrayList<String> result = data
                                .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                        Log.d("SPEECH", result.get(0));

                        responseWrapper = ResponseVoice.getResponse(result.get(0));
                        responseSpeech = responseWrapper.getResponseString();
                        responseStatus = responseWrapper.getResponseStatus();
                        responseRequestType = responseWrapper.getRequestType();
                        code = responseWrapper.getCode();

                        if(responseStatus.equals(ResponseStatus.RESPONSE_SUCCESS)) {

                            if(responseRequestType.equals(RequestType.PROJECTOR_STATE)) {
                                code = getString(R.string.powerButton);
                                code = encryptCode.getCode(code, DeviceList.PROJECTOR);
                                ArrayList<String> parameterValue = new ArrayList<String>();
                                RequestMode requestType = RequestMode.NORMAL;
                                RemoteActivity.connectionStatus = ConnectionStatus.REMOTEREQUEST;
                                RemoteActivity.signal = 0;


                                if(!code.equals("XX")) {

                                    Toast.makeText(getContext(), code, Toast.LENGTH_SHORT).show();
                                    parameterValue.add(0, ParameterFactory.getMode(requestType)); // request mode
                                    parameterValue.add(1, code);
                                    new HttpRequestAsyncTask(parameterValue, finalipAddress, finalportNumber,
                                            requestType).execute();

                                    final GlideDrawableImageViewTarget logoImageViewTarget =
                                            new GlideDrawableImageViewTarget(voiceResponseGif);

                                    Glide.with(this)
                                            .load(R.raw.waiting)
                                            .signature(new StringSignature(String.valueOf(System.currentTimeMillis())))
                                            .into(logoImageViewTarget);
                                    voiceResponseText.setText(getString(R.string.waiting));


                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            speakResult("Please Wait");

                                            while (RemoteActivity.signal == 0) {
                                                try {
                                                    Thread.sleep(1000);
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                            speakResult(responseSpeech);
                                            if(connectionStatus.equals(ConnectionStatus.SUCCESS)) {

                                                logoImageViewTarget.setDrawable(voiceResponseGif.getDrawable());

                                                final Handler handler = new Handler(Looper.getMainLooper());

                                                handler.post(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        Glide.with(PlaceholderFragment.this)
                                                                .load(R.raw.checkmark)
                                                                .signature(new StringSignature(String.valueOf(System.currentTimeMillis())))
                                                                .into(logoImageViewTarget);
                                                        voiceResponseText.setText(getString(R.string.successPrjector));
                                                    }
                                                });

                                                Log.d("SPEECH", getString(R.string.successPrjector));

                                            }
                                            else if(connectionStatus.equals(ConnectionStatus.FAIL)){
                                                logoImageViewTarget.setDrawable(voiceResponseGif.getDrawable());

                                                final Handler handler = new Handler(Looper.getMainLooper());

                                                handler.post(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        Glide.with(PlaceholderFragment.this)
                                                                .load(R.raw.waiting)
                                                                .signature(new StringSignature(String.valueOf(System.currentTimeMillis())))
                                                                .into(logoImageViewTarget);
                                                        voiceResponseText.setText(getString(R.string.failProjector));

                                                    }
                                                });
                                                Log.d("SPEECH", getString(R.string.failProjector));

                                            }

                                        }
                                    }).start();



                                }
                                else {
                                    speakResult("Sorry This Service is Unavailable");
                                    Toast.makeText(getContext(), "Code not Found", Toast.LENGTH_SHORT).show();
                                }
                            }
                            else if(responseRequestType.equals(RequestType.LIGHT)) {

                                RequestMode requestType = RequestMode.LIGHT;
                                RemoteActivity.connectionStatus = ConnectionStatus.REMOTEREQUEST;
                                RemoteActivity.signal = 0;
                                //code = encryptCode.getCode(code, DeviceList.LIGHT);

                                ArrayList<String> parameterValue = new ArrayList<String>();
                                parameterValue.add(0, ParameterFactory.getMode(requestType));
                                parameterValue.add(1, code);

                                new HttpRequestAsyncTask(parameterValue, finalipAddress, finalportNumber,
                                        requestType).execute();
                                final GlideDrawableImageViewTarget logoImageViewTarget =
                                        new GlideDrawableImageViewTarget(voiceResponseGif);
                                Glide.with(this)
                                        .load(R.raw.waiting)
                                        .signature(new StringSignature(String.valueOf(System.currentTimeMillis())))
                                        .into(logoImageViewTarget);
                                voiceResponseText.setText(getString(R.string.waiting));


                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        speakResult("Please Wait");

                                        while (RemoteActivity.signal == 0) {
                                            try {
                                                Thread.sleep(1000);
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        }
                                        speakResult(responseSpeech);
                                        if(connectionStatus.equals(ConnectionStatus.SUCCESS)) {

                                            logoImageViewTarget.setDrawable(voiceResponseGif.getDrawable());

                                            final Handler handler = new Handler(Looper.getMainLooper());

                                            handler.post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Glide.with(PlaceholderFragment.this)
                                                            .load(R.raw.checkmark)
                                                            .signature(new StringSignature(String.valueOf(System.currentTimeMillis())))
                                                            .into(logoImageViewTarget);
                                                    voiceResponseText.setText(getString(R.string.successLight));
                                                }
                                            });

                                            Log.d("SPEECH", getString(R.string.successLight));

                                        }
                                        else if(connectionStatus.equals(ConnectionStatus.FAIL)){
                                            logoImageViewTarget.setDrawable(voiceResponseGif.getDrawable());

                                            final Handler handler = new Handler(Looper.getMainLooper());

                                            handler.post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Glide.with(PlaceholderFragment.this)
                                                            .load(R.raw.waiting)
                                                            .signature(new StringSignature(String.valueOf(System.currentTimeMillis())))
                                                            .into(logoImageViewTarget);
                                                    voiceResponseText.setText(getString(R.string.failLight));

                                                }
                                            });
                                            Log.d("SPEECH", getString(R.string.failLight));

                                        }
                                    }
                                }).start();

                            }
                        }
                        else {
                            speakResult(responseSpeech);
                        }
                    }
                    break;
            }
        }

        public void speakResult(String responseSpeech) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                textToSpeech.speak(responseSpeech,
                        TextToSpeech.QUEUE_FLUSH, null, null);
            }
            else {
                textToSpeech.speak(responseSpeech,
                        TextToSpeech.QUEUE_FLUSH, null);
            }
        }


        /*Initialises listeners for all button in the remote*/
        public void initRemoteView(View rootView) {


            ((ImageButton) rootView.findViewById(R.id.power)).setOnClickListener(new ButtonClick());
            ((ImageButton) rootView.findViewById(R.id.up)).setOnClickListener(new ButtonClick());
            ((ImageButton) rootView.findViewById(R.id.left)).setOnClickListener(new ButtonClick());
            ((ImageButton) rootView.findViewById(R.id.right)).setOnClickListener(new ButtonClick());
            ((ImageButton) rootView.findViewById(R.id.down)).setOnClickListener(new ButtonClick());
            ((ImageButton) rootView.findViewById(R.id.enter)).setOnClickListener(new ButtonClick());
            ((ImageButton) rootView.findViewById(R.id.pageinc)).setOnClickListener(new ButtonClick());
            ((ImageButton) rootView.findViewById(R.id.pagedec)).setOnClickListener(new ButtonClick());
            ((ImageButton) rootView.findViewById(R.id.zoominc)).setOnClickListener(new ButtonClick());
            ((ImageButton) rootView.findViewById(R.id.zoomdec)).setOnClickListener(new ButtonClick());
            ((ImageButton) rootView.findViewById(R.id.volinc)).setOnClickListener(new ButtonClick());
            ((ImageButton) rootView.findViewById(R.id.voldec)).setOnClickListener(new ButtonClick());

            ((Button) rootView.findViewById(R.id.source)).setOnClickListener(new ButtonClick());
            ((Button) rootView.findViewById(R.id.mute)).setOnClickListener(new ButtonClick());
            ((Button) rootView.findViewById(R.id.computer)).setOnClickListener(new ButtonClick());
            ((Button) rootView.findViewById(R.id.video)).setOnClickListener(new ButtonClick());
            ((Button) rootView.findViewById(R.id.usb)).setOnClickListener(new ButtonClick());
            ((Button) rootView.findViewById(R.id.lan)).setOnClickListener(new ButtonClick());
            ((Button) rootView.findViewById(R.id.menu)).setOnClickListener(new ButtonClick());
            ((Button) rootView.findViewById(R.id.esc)).setOnClickListener(new ButtonClick());
            ((Button) rootView.findViewById(R.id.user)).setOnClickListener(new ButtonClick());
            ((Button) rootView.findViewById(R.id.pointer)).setOnClickListener(new ButtonClick());
            ((Button) rootView.findViewById(R.id.help)).setOnClickListener(new ButtonClick());
            ((Button) rootView.findViewById(R.id.freeze)).setOnClickListener(new ButtonClick());
            ((Button) rootView.findViewById(R.id.num0)).setOnClickListener(new ButtonClick());
            ((Button) rootView.findViewById(R.id.num1)).setOnClickListener(new ButtonClick());
            ((Button) rootView.findViewById(R.id.num2)).setOnClickListener(new ButtonClick());
            ((Button) rootView.findViewById(R.id.num3)).setOnClickListener(new ButtonClick());
            ((Button) rootView.findViewById(R.id.num4)).setOnClickListener(new ButtonClick());
            ((Button) rootView.findViewById(R.id.num5)).setOnClickListener(new ButtonClick());
            ((Button) rootView.findViewById(R.id.num6)).setOnClickListener(new ButtonClick());
            ((Button) rootView.findViewById(R.id.num7)).setOnClickListener(new ButtonClick());
            ((Button) rootView.findViewById(R.id.num8)).setOnClickListener(new ButtonClick());
            ((Button) rootView.findViewById(R.id.num9)).setOnClickListener(new ButtonClick());
            ((Button) rootView.findViewById(R.id.num)).setOnClickListener(new ButtonClick());

        }



        private class InitRequest implements Runnable {

            Thread t;
            RequestHandler requestHandler;
            public InitRequest(RequestHandler requestHandler){
                t = new Thread(this,"Init Request");
                this.requestHandler = requestHandler;
                t.start();

            }

            @Override
            public void run() {

                try {
                    requestHandler.sendInitRequest();
                }catch(InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        private class InitRC4Encryption implements Runnable {

            Thread t;
            RequestHandler requestHandler;
            public InitRC4Encryption(RequestHandler requestHandler) {

                t = new Thread(this,"Init Request");
                this.requestHandler = requestHandler;
                t.start();

            }
            @Override
            public void run() {
                try {

                    requestHandler.sendInitEncryption();
                }catch(InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }

        public class RequestHandler {


            private String requestReply,ipAddress, portNumber;
            private Context context;


            private RequestMode requestType;
            private ArrayList<String> parameterValue;
            private FragmentTransaction fragmentTransaction;
            private Fragment currentFragment;
            private Button tryAgain;
            private View rootView;

            public RequestHandler(View rootView, ArrayList<String> parameterValue, String ipAddress,
                                  String portNumber, RequestMode requestType,
                                  FragmentTransaction fragmentTransaction, Fragment currentFragment) {

                this.rootView = rootView;
                this.context = rootView.getContext();
                this.ipAddress = ipAddress;
                this.parameterValue = parameterValue;
                this.portNumber = portNumber;
                this.requestType = requestType;
                this.fragmentTransaction =fragmentTransaction;
                this.currentFragment = currentFragment;
                this.tryAgain = null;

            }

            public RequestHandler(View rootView, ArrayList<String> parameterValue, String ipAddress,
                                  String portNumber, RequestMode requestType,
                                  FragmentTransaction fragmentTransaction, Fragment currentFragment,
                                  Button tryAgain) {

                this.rootView = rootView;
                this.context = rootView.getContext();
                this.ipAddress = ipAddress;
                this.parameterValue = parameterValue;
                this.portNumber = portNumber;
                this.requestType = requestType;
                this.fragmentTransaction =fragmentTransaction;
                this.currentFragment = currentFragment;
                this.tryAgain = tryAgain;

            }



            public synchronized void sendInitRequest () throws InterruptedException {

                new HttpRequestAsyncTask (
                        parameterValue, ipAddress, portNumber,
                        requestType).execute();

                 RemoteActivity.signal = 0;

                while(RemoteActivity.signal == 0){
                    try{Thread.sleep(1000);}catch (Exception e){e.printStackTrace();}
                }
                notifyAll();
            }

            public synchronized void  sendInitEncryption() throws  InterruptedException {

                while(RemoteActivity.signal == 0) {
                    wait();
                }

                if(RemoteActivity.connectionStatus.equals(ConnectionStatus.SUCCESS)) {

                    finalipAddress = this.ipAddress;
                    finalportNumber = this.portNumber;
                    RemoteActivity.signal = 0;
                    final AlertDialog.Builder builder = new AlertDialog.Builder(context);

                    final EditText input = new EditText(context);
                    input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    builder.setView(input);
                    builder.setCancelable(false);
                    builder.setTitle("Enter Password...");
                    //builder.setMessage("");

                    builder.setPositiveButton("LOGIN", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        final String password = input.getText().toString().trim();


                            dialog.cancel();

                            final ProgressDialog progressCircle = new ProgressDialog(context);
                            progressCircle.setCancelable(false);
                            progressCircle.setMessage("Please Wait...");
                            progressCircle.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                            progressCircle.show();

                            encryptionKeys = new EncryptionKeys();

                            new Thread(new Runnable() {
                                @Override
                                public void run() {

                                    String encryptedPassword;
                                    RSAEncrypt rsaEncrypt = new RSAEncrypt(encryptionKeys);
                                    encryptedPassword = rsaEncrypt.encryptPassword(password);

                                    parameterValue.add(0,ParameterFactory.getMode(RequestMode.RC4KEY));
                                    parameterValue.add(1,encryptedPassword);
                                    requestType = RequestMode.RC4KEY;

                                    new HttpRequestAsyncTask(parameterValue, ipAddress, portNumber,
                                            requestType).execute();
                                    while(RemoteActivity.signal == 0) {
                                        try {
                                            Thread.sleep(1000);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    progressCircle.dismiss();

                                    if(RemoteActivity.connectionStatus.equals(ConnectionStatus.AUTH_SUCCESS)) {

                                        encryptionKeys.setRC4key(password);
                                        fragmentTransaction.detach(currentFragment);
                                        fragmentTransaction.attach(currentFragment);
                                        fragmentTransaction.commit();

                                        initRC4 = new InitRC4(encryptionKeys);
                                        initRC4.initialiseRC4();
                                        remoteCode = new RemoteCode(context.getAssets());
                                        encryptCode = new EncryptCode(initRC4, remoteCode);

                                        RemoteActivity.change = true;
                                        isRC4Initialized = true;

                                    }
                                    else if(RemoteActivity.connectionStatus.equals(ConnectionStatus.AUTH_FAIL)) {

                                        fragmentTransaction.detach(currentFragment);
                                        fragmentTransaction.attach(currentFragment);
                                        fragmentTransaction.commit();

                                        RemoteActivity.change = true;

                                    }

                            }
                            }).start();

                        }

                    });

                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });



                    final Handler handler = new Handler(Looper.getMainLooper());

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            builder.show();
                        }
                    });
                }
                final Handler handler = new Handler(Looper.getMainLooper());

                handler.post(new Runnable() {
                    @Override
                    public void run() {

                        if(tryAgain != null)
                            tryAgain.setEnabled(true);

                    }
                });
                RemoteActivity.signal = 0;
            }
        }
    }


    public static class ButtonClick implements View.OnClickListener {



        public void onClick(View v) {

            ArrayList<String> parameterValue = new ArrayList<String>();

            String code = String.valueOf(v.getTag());
            RequestMode requestType = RequestMode.NORMAL;

            RemoteActivity.connectionStatus = ConnectionStatus.REMOTEREQUEST;
            RemoteActivity.signal = 0;

            Log.d("RC4TEXT",code);

            code = encryptCode.getCode(code, DeviceList.PROJECTOR);

            Log.d("RC4CIPHER",code);

            if(!code.equals("XX")) {

                Toast.makeText(v.getContext(), code, Toast.LENGTH_SHORT).show();
                parameterValue.add(0, ParameterFactory.getMode(RequestMode.NORMAL)); // request mode
                parameterValue.add(1, code);
                new HttpRequestAsyncTask(parameterValue, finalipAddress, finalportNumber,
                        requestType).execute();

                final ProgressDialog progressCircle = new ProgressDialog(v.getContext());
                progressCircle.setCancelable(false);
                progressCircle.setMessage("Please Wait...");
                progressCircle.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressCircle.show();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while (RemoteActivity.signal == 0) {
                            try {
                                Thread.sleep(1000);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        progressCircle.dismiss();
                    }
                }).start();

            }
            else {
                Toast.makeText(v.getContext(), "Code not Found", Toast.LENGTH_SHORT).show();
            }


        }


    }
    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }


        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "REMOTE";
                case 1:
                    return "VOICE CONTROL";
                case 2:
                    return "HELP";
            }
            return null;
        }
    }
}
