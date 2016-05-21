package com.bangaloretalkies.iot;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
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
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "MainActivity";

    private EditText editTextPort;
    private EditText editTextIp;

    private String dynamic_ip;
    private String dynamic_port;

    SharedPreferences sharedPref;
    private static final int SERVER_PORT = 10000;
    private static final int TIMEOUT_MS = 2500;
    private int dynamic_timeout_ms;
    private Integer dynamic_server_port;
    private boolean isSwitchChecked = false;


    private InetAddress getIpAddress() throws IOException {

        return InetAddress.getByName(editTextIp.getText().toString());
    }

    private void sendOnRequest(DatagramSocket socket) throws IOException {
        String data = String.format("on");
        Log.d(TAG, "Sending data " + data);

        if (null != editTextPort && null != editTextPort.getText()) {
            Integer dsp = new Integer(editTextPort.getText().toString());
            dynamic_server_port = dsp.intValue();
            if (dynamic_server_port > 65535) {
                dynamic_server_port = SERVER_PORT;
                editTextPort.setText("10000");
            }
        }
        else {
            dynamic_server_port = SERVER_PORT;
        }
        DatagramPacket packet = new DatagramPacket(data.getBytes(), data.length(),
                getIpAddress(), dynamic_server_port);
        socket.send(packet);
    }

    private void sendOffRequest(DatagramSocket socket) throws IOException {
        String data = String.format("off");
        Log.d(TAG, "Sending data " + data);

        if (null != editTextPort && null != editTextPort.getText()) {
            Integer dsp = new Integer(editTextPort.getText().toString());
            dynamic_server_port = dsp.intValue();
            if (dynamic_server_port > 65535) {
                dynamic_server_port = SERVER_PORT;
                editTextPort.setText("10000");
            }
        }
        else {
            dynamic_server_port = SERVER_PORT;
        }
        DatagramPacket packet = new DatagramPacket(data.getBytes(), data.length(),
                getIpAddress(), dynamic_server_port);
        socket.send(packet);
    }



    private void listenForResponses(DatagramSocket socket) throws IOException {
        byte[] buf = new byte[1024];
        try {
            while (true) {
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);
                String s = new String(packet.getData(), 0, packet.getLength());
                Log.d(TAG, "Received response " + s);
                break;
            }
        } catch (SocketTimeoutException e) {
            Log.d(TAG, "Receive timed out");
        }
    }

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

        Switch onOffSwitch = (Switch)  findViewById(R.id.lightcontrolswitch1);

        onOffSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.v("Switch State=", "" + isChecked);
                isSwitchChecked = isChecked;
                new MyTask().execute(isChecked);
            }

        });
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

    public class MyTask extends AsyncTask<Boolean, Integer, Boolean> {

        @Override
        protected Boolean doInBackground(Boolean... isChecked) {

            try {
                DatagramSocket socket = new DatagramSocket();
                //socket.setBroadcast(true);
                socket.setSoTimeout(TIMEOUT_MS);

                if (isSwitchChecked)
                {
                    sendOnRequest(socket);
                }
                else
                {
                    sendOffRequest(socket);
                }

                listenForResponses(socket);

                socket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not send discovery request", e);
            }

            return null;
        }
    }
}
