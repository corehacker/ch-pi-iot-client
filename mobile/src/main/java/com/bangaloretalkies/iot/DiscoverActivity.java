package com.bangaloretalkies.iot;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DiscoverActivity extends AppCompatActivity {
    private static final String TAG = "DiscoverActivity";
    private Button refreshButton;
    private ListView dicoverDevicesList;
    String[] discoveredMachines;
    List<String> discoveredMachinesList = new ArrayList<String>();
    Set<String> discoveredMachinesSet = new LinkedHashSet<>();
    ArrayAdapter<String> adapter;
    SharedPreferences sharedPref;
    private static final int TIMEOUT_MS = 250;
    private int dynamic_timeout_ms;

    private void sendDiscoverRequest(DatagramSocket socket) throws IOException {
        String data = String.format("discover");

        DatagramPacket packet = new DatagramPacket(data.getBytes(), data.length(),
                getBroadcastAddress(), 8080);
        socket.send(packet);
    }

    public static InetAddress getBroadcastAddress() throws SocketException {
        System.setProperty("java.net.preferIPv4Stack", "true");
        for (Enumeration<NetworkInterface> niEnum = NetworkInterface.getNetworkInterfaces(); niEnum.hasMoreElements();) {
            NetworkInterface ni = niEnum.nextElement();
            if (!ni.isLoopback()) {
                for (InterfaceAddress interfaceAddress : ni.getInterfaceAddresses()) {
                    Log.e(TAG, "Broadcast Address: " + interfaceAddress.getBroadcast());

                    if (null != interfaceAddress.getBroadcast())
                        return interfaceAddress.getBroadcast();
                }
            }
        }
        return null;
    }

    public boolean isIPValid(String text) {
        Pattern p = Pattern.compile("^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$");
        Matcher m = p.matcher(text);
        return m.find();
    }

    private void listenForResponses(DatagramSocket socket) throws IOException {
        byte[] buf = new byte[1024];
        try {
            while (true) {
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);
                String s = new String(packet.getData(), 0, packet.getLength());
                Log.d(TAG, "Received response " + s);

                JSONObject jObject = null;
                try {
                    jObject = new JSONObject(s);
                }
                catch (JSONException e) {
                   //  e.printStackTrace();
                    Log.d(TAG, "Received response not JSON: " + e + ", Rejecting it.");

                    String[] hosts = s.split(":");

                    String[] port = hosts[1].split("/");

                    Log.d(TAG, "IP: " + hosts[0]);
                    Log.d(TAG, "Port: " + port[0]);
                    //if (isIPValid(hosts[0].toString())) {
                        Log.i (TAG, "Valid ip address.");
                        if (!discoveredMachinesSet.contains( hosts[0].toString() + ":" +  port[0].toString())) {
                            discoveredMachinesList.add(hosts[0].toString() + ":" + port[0].toString());
                            discoveredMachinesSet.add(hosts[0].toString() + ":" + port[0].toString());
                        }
                    //}
                    continue;
                }

                JSONObject config = null;
                try {
                    config = jObject.getJSONObject("config");
                }
                catch (JSONException e) {
                    continue;
                }
                Log.i(TAG, "config: " + config.toString());

                JSONObject http = null;
                try {
                    http = config.getJSONObject("http");
                }
                catch (JSONException e) {
                    continue;
                }
                Log.i(TAG, "http: " + http);

                String ip = null;
                try {
                    ip = http.getString("ip");
                }
                catch (JSONException e) {
                    continue;
                }

                String port = null;
                try {
                    port = http.getString("port");
                }
                catch (JSONException e) {
                    continue;
                }

                Log.i(TAG, "ip: " + ip.toString());
                Log.i(TAG, "port: " + port.toString());

                if (!discoveredMachinesSet.contains(ip.toString() + ":" + port.toString())) {
                    discoveredMachinesList.add(ip.toString() + ":" + port.toString());
                    discoveredMachinesSet.add(ip.toString() + ":" + port.toString());
                }
            }
        } catch (SocketTimeoutException e) {
            Log.d(TAG, "Receive timed out");
        }

        if (discoveredMachinesList.isEmpty()) {
            Log.d(TAG, "Running discover again...");
            new MyTask().execute();
        }
        else {
            Log.d(TAG, "Finite devices discovered. Not running discover.");
            adapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_list_item_1, android.R.id.text1, discoveredMachinesList);
            updateDevicesList();
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate.");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discover);

        refreshButton = (Button) this.findViewById(R.id.refreshbutton);

        refreshButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d(TAG, "Discover devices button clicked.");
                new MyTask().execute();
            }
        });

        dicoverDevicesList = (ListView) findViewById(R.id.discoverdeviceslist);

        dicoverDevicesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                // ListView Clicked item index
                int itemPosition     = position;

                // ListView Clicked item value
                String  itemValue    = (String) dicoverDevicesList.getItemAtPosition(position);

                // Show Alert
                Toast.makeText(getApplicationContext(),
                        "Position: " + itemPosition + "  ListItem: " + itemValue , Toast.LENGTH_SHORT)
                        .show();

                String[] selectedHost = itemValue.split(":");
                Log.d (TAG, "Setting main activity ip and port");

                sharedPref = getSharedPreferences("iot-shared-pref", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("selected-ip", selectedHost[0]);
                editor.putString("selected-port", selectedHost[1]);
                editor.commit();
                finish();
            }

        });

        SeekBar seekBar = (SeekBar) findViewById(R.id.discoverTimeoutSlider);
        seekBar.setMax(2000);
        seekBar.setProgress(TIMEOUT_MS);
        dynamic_timeout_ms = TIMEOUT_MS;

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progresValue, boolean fromUser) {
                Log.v("Progress=", "" + progresValue);
                dynamic_timeout_ms = progresValue;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        new MyTask().execute();
    }

    public class MyTask extends AsyncTask<Boolean, Integer, Boolean> {

        @Override
        protected Boolean doInBackground(Boolean... isChecked) {

            try {
                DatagramSocket discoverSocket = new DatagramSocket();
                discoverSocket.setBroadcast(true);
                discoverSocket.setSoTimeout(dynamic_timeout_ms);

                sendDiscoverRequest (discoverSocket);
                listenForResponses(discoverSocket);
                discoverSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not send discovery request", e);
            }

            return null;
        }
    }

    public void updateDevicesList ()
    {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // This code will always run on the UI thread, therefore is safe to modify UI elements.
                // Assign adapter to ListView
                dicoverDevicesList.setAdapter(adapter);
            }
        });
    }

}
