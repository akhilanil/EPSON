package com.example.akhil.epson;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Image;
import android.provider.Settings;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class RemoteActivity extends AppCompatActivity {

    static SharedPreferences sharedPreferences;
    public final static String PREF_IP = "PREF_IP_ADDRESS";
    public final static String PREF_PORT = "PREF_PORT_NUMBER";
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
            this.status = extras.getString("status");

        Log.d("START",this.status);


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
             View rootView = null;

            if(RemoteActivity.status.equals("faliure")) {
                rootView = inflater.inflate(R.layout.fragment_connection_error, container, false);
                final Fragment currentFragment = this;
                //initView(rootView);
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

                        new HttpRequestAsyncTask(
                                v.getContext(), parameterValue, ipAddress, portNumber,
                                requestType, fragmentTransaction, currentFragment).execute();




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

            ImageButton power = (ImageButton) rootView.findViewById(R.id.powerButton);
            power.setOnClickListener(new ButtonClick("power"));



        }

    }


    public static class ButtonClick implements View.OnClickListener {

        String buttonName;
        public ButtonClick(String buttonName) {
            this.buttonName = buttonName;
        }

        public void onClick(View v) {

            Log.d("OnCLICK", buttonName);
            Toast.makeText(v.getContext(), buttonName, Toast.LENGTH_SHORT).show();
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
