package com.bangaloretalkies.iot;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "MainActivity";

    private EditText editTextPort;
    private EditText editTextIp;

    private String dynamic_ip;
    private String dynamic_port;

    SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate.");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        editTextPort = (EditText) findViewById(R.id.editTextPort);
        editTextIp = (EditText) findViewById(R.id.editTextIp);

        sharedPref = getSharedPreferences("iot-shared-pref", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("selected-ip", "127.0.0.1");
        editor.putString("selected-port", "8080");
        editor.commit();

    }

    @Override
    public void onResume(){
        super.onResume();
        //
        Log.i(TAG, "onResume invoked.");
        String selectedIp = sharedPref.getString("selected-ip", null);
        String selectedPort = sharedPref.getString("selected-port", null);
        Log.i(TAG, "Selected device: " + selectedIp + ":" + selectedPort);
        editTextIp.setText(selectedIp);
        editTextPort.setText(selectedPort);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_discover) {
            Log.i(TAG, "Discover menu item selected... Launching DiscoverActivity...");
            Intent intent = new Intent(this, DiscoverActivity.class);
            this.startActivity(intent);
        } else if (id == R.id.nav_settings) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

//    public void updateIp (String ip)
//    {
//        dynamic_ip = ip;
//
//        this.runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                // This code will always run on the UI thread, therefore is safe to modify UI elements.
//
//                if (!editTextIp.getText().toString().equals(dynamic_ip)) {
//                    editTextIp.setText(dynamic_ip);
//                }
//            }
//        });
//    }
//
//    public void updatePort (String port)
//    {
//        dynamic_port = port;
//
//        this.runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                // This code will always run on the UI thread, therefore is safe to modify UI elements.
//
//                if (!editTextPort.getText().toString().equals(dynamic_port)) {
//                    editTextPort.setText(dynamic_port);
//                }
//            }
//        });
//    }
//
//    public void updateIpV2 (String ip)
//    {
//        dynamic_ip = ip;
//        Log.i (TAG, "Updating ip: " + dynamic_ip);
//        if (!editTextIp.getText().toString().equals(dynamic_ip)) {
//            editTextIp.setText(dynamic_ip);
//        }
//    }
//
//    public void updatePortV2 (String port)
//    {
//        dynamic_port = port;
//        Log.i (TAG, "Updating port: " + dynamic_port);
//        if (!editTextPort.getText().toString().equals(dynamic_port)) {
//            editTextPort.setText(dynamic_port);
//        }
//    }
}
