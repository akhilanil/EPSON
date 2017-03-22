package com.example.akhil.epson;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.StringRes;
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
import android.view.View.OnClickListener;

import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.akhil.decode.EncryptionKeys;
import com.example.akhil.decode.RSAEncrypt;

import java.util.ArrayList;
import java.util.logging.LogRecord;

public class RemoteActivity extends AppCompatActivity {

    static SharedPreferences sharedPreferences;
    public final static String PREF_IP = "PREF_IP_ADDRESS";
    public final static String PREF_PORT = "PREF_PORT_NUMBER";

    public static EncryptionKeys encryptionKeys;




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
    private ViewPager mViewPager;
    public static String status;
    public static int signal = 0;
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

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

       sharedPreferences =
                getSharedPreferences("HTTP_HELPER_PREFS", Context.MODE_PRIVATE);

        Bundle extras = getIntent().getExtras();
        if(extras != null)
            status = extras.getString("status");



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
            return true;
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
             View rootView;

            if(RemoteActivity.status.equals("faliure")) {
                rootView = inflater.inflate(R.layout.fragment_connection_error, container, false);
                final Fragment currentFragment = this;
                Button tryagain = (Button)rootView.findViewById(R.id.tryagain);
                tryagain.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        ArrayList<String> parameterValue = new ArrayList<String>();
                        parameterValue.add(0,"init");

                        String ipAddress = sharedPreferences.getString(PREF_IP,"NULL");
                        String portNumber = sharedPreferences.getString(PREF_PORT,"NULL");

                        /*
                        * RequestType: initagain
                        * Purpose: Connection Request from USER
                        * */

                        String requestType = "initagain";

                        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                        RequestHandler requestHandler = new RequestHandler(v.getContext(), parameterValue,
                                ipAddress, portNumber,
                                requestType, fragmentTransaction, currentFragment);

                        new InitRC4Encryption(requestHandler);
                        new InitRequest(requestHandler);

                        /*new HttpRequestAsyncTask (
                                v.getContext(), parameterValue, ipAddress, portNumber,
                                requestType, fragmentTransaction, currentFragment).execute();
                        */

                        //while (task.getStatus() != HttpRequestAsyncTask.Status.FINISHED);


                    }
                });



            }


            else if(String.valueOf(getArguments().getInt(ARG_SECTION_NUMBER)).equals("1")) {
                rootView = inflater.inflate(R.layout.fragment_hand, container, false);

                initView(rootView);



            }
            else if(String.valueOf(getArguments().getInt(ARG_SECTION_NUMBER)).equals("2"))
                rootView = inflater.inflate(R.layout.fragment_voice, container, false);
            else
                rootView = inflater.inflate(R.layout.fragment_help, container, false);
            //System.out.println(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));

            //Button bt = (Button)rootView.findViewById(R.id.button);
            //bt.setText();
            return rootView;
        }

        public void initView(View rootView) {

            final View view = rootView;

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



            //rootView.setOnClickListener(new ButtonClick());
            //rootView.getRootView().setOnClickListener(new ButtonClick());


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
                    Log.d("ERROR",e.getMessage());
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
                    Log.d("ERROR",e.getMessage());
                }

            }
        }

        public class RequestHandler {


            private String requestReply,ipAddress, portNumber;
            private Context context;


            private String requestType;
            private ArrayList<String> parameterValue;
            private FragmentTransaction fragmentTransaction;
            private Fragment currentFragment;
            public int status ;

            public RequestHandler(Context context, ArrayList<String> parameterValue, String ipAddress,
                                  String portNumber, String requestType,
                                  FragmentTransaction fragmentTransaction, Fragment currentFragment) {

                this.context = context;
                this.ipAddress = ipAddress;
                this.parameterValue = parameterValue;
                this.portNumber = portNumber;
                this.requestType = requestType;
                this.fragmentTransaction =fragmentTransaction;
                this.currentFragment = currentFragment;
                this.status = 0;

            }

            public synchronized void sendInitRequest () throws InterruptedException {


                new HttpRequestAsyncTask (
                        this.context, parameterValue, ipAddress, portNumber,
                        requestType, fragmentTransaction, currentFragment).execute();
                status = 1;

                if(RemoteActivity.signal == 0) {
                    wait(7000);
                }
                notify();



            }

            public synchronized void  sendInitEncryption() throws  InterruptedException {

                while(RemoteActivity.signal == 0) {
                    wait();
                }

                if(RemoteActivity.status.equals("success")) {

                    final AlertDialog.Builder builder = new AlertDialog.Builder(context);

                    final EditText input = new EditText(context);
                    input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    builder.setView(input);
                    builder.setCancelable(false);
                    builder.setTitle("LOGIN");
                    builder.setMessage("Enter Password...");

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

                                    parameterValue.add(0,encryptedPassword);
                                    requestType = "rc4key";
                                    new HttpRequestAsyncTask(context, parameterValue, ipAddress, portNumber,
                                            requestType,fragmentTransaction,currentFragment).execute();

                                    try {Thread.sleep(7000);}catch (Exception e) {Log.d("ERROR1",e.getMessage());e.printStackTrace();}
                                    progressCircle.dismiss();
                                    if(RemoteActivity.status.equals("AUTH_SUCCESS")) {

                                        fragmentTransaction.detach(currentFragment);
                                        fragmentTransaction.attach(currentFragment);
                                        fragmentTransaction.commit();

                                    }




                                    if(RemoteActivity.status.equals("AUTH_SUCCESS")) {
                                        Log.d("Status", "Authenticated");
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
            }
        }
    }


    public static class ButtonClick implements View.OnClickListener {

        //String buttonName;
        /*public ButtonClick(String buttonName) {
            this.buttonName = buttonName;
        }*/

        public void onClick(View v) {

            Log.d("OnCLICK", "CLICKING");
            if(String.valueOf(v.getTag()).equals("text"))
                Log.d("OnCLICK", String.valueOf(v.getTag()));
            else {
                Log.d("OnCLICK", "CLICKED");
                Toast.makeText(v.getContext(), String.valueOf(v.getTag()), Toast.LENGTH_SHORT).show();
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
