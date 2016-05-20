package com.bangaloretalkies.iot;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
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
import java.util.Enumeration;
import java.util.List;

public class DiscoverActivity extends AppCompatActivity {
    private static final String TAG = "DiscoverActivity";
    private Button refreshButton;
    private ListView dicoverDevicesList;
    String[] discoveredMachines;
    List<String> discoveredMachinesList = new ArrayList<String>();

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

                discoveredMachinesList.add(ip.toString() + ":" + port.toString());

                //String[] hosts = s.split(":");
                //String[] host1params = hosts[0].split(":");

                //String[] port = hosts[1].split("/");

                //Log.d(TAG, "IP: " + hosts[0]);
                //Log.d(TAG, "Port: " + port[0]);
                //f2.updateIp(hosts[0]);
                //f2.updatePort(port[0]);
                // break;
            }
        } catch (SocketTimeoutException e) {
            Log.d(TAG, "Receive timed out");
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
                        "Position :"+itemPosition+"  ListItem : " +itemValue , Toast.LENGTH_LONG)
                        .show();

            }

        });
    }

    public class MyTask extends AsyncTask<Boolean, Integer, Boolean> {

        @Override
        protected Boolean doInBackground(Boolean... isChecked) {

            try {
                DatagramSocket discoverSocket = new DatagramSocket();
                discoverSocket.setBroadcast(true);
                discoverSocket.setSoTimeout(5000);

                sendDiscoverRequest (discoverSocket);
                listenForResponses(discoverSocket);
                discoverSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not send discovery request", e);
            }

            return null;
        }
    }

    public void updateDevicesList (String port)
    {
        // dynamic_port = port;

        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // This code will always run on the UI thread, therefore is safe to modify UI elements.

//                ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
//                        android.R.layout.simple_list_item_1, android.R.id.text1, discoveredMachinesList);


                // Assign adapter to ListView
                // dicoverDevicesList.setAdapter(adapter);

//                if (!editTextPort.getText().toString().equals(dynamic_port)) {
//                    editTextPort.setText(dynamic_port);
//                }
            }
        });
    }

}
