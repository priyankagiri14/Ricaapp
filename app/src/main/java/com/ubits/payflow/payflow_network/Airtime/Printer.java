package com.ubits.payflow.payflow_network.Airtime;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.ubits.payflow.payflow_network.R;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class Printer extends AppCompatActivity {
    public static final String BUNDLE_EXTRA_TEXT = "text";

    // will show the statuses like bluetooth open, close or data sent
    TextView myLabel;

    // will enable user to enter any text to be printed
    EditText myTextbox;

    // android built in classes for bluetooth operations
    BluetoothAdapter mBluetoothAdapter;
    BluetoothSocket mmSocket;
    BluetoothDevice mmDevice;

    // needed for communication to bluetooth device / network
    OutputStream mmOutputStream;
    InputStream mmInputStream;
    Thread workerThread;

    byte[] readBuffer;
    int readBufferPosition;
    volatile boolean stopWorker;

    private TextView tvTile;
    private ImageView backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_printer);

        // we are going to have three buttons for specific functions
        Button openButton = (Button) findViewById(R.id.open);
        Button sendButton = (Button) findViewById(R.id.send);
        Button closeButton = (Button) findViewById(R.id.close);

// text label and input box
        myLabel = (TextView) findViewById(R.id.label);
        myTextbox = (EditText) findViewById(R.id.entry);

        tvTile = (TextView) findViewById(R.id.tv_titlebar);
        tvTile.setText("PrinterUtil");
        backButton = (ImageView) findViewById(R.id.back_btn);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        // open bluetooth connection
        openButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    findBT();
                    openBT();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });


        // send data typed by the user to be printed
        sendButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    sendData();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });


        // close bluetooth connection
        closeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    closeBT();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
        if (getIntent().getExtras() != null) {
            String text = getIntent().getStringExtra(BUNDLE_EXTRA_TEXT);
            myTextbox.setText(text);
        }

    }

    // close the connection to bluetooth printer.
    void closeBT() throws IOException {
        try {
            stopWorker = true;
            mmOutputStream.close();
            mmInputStream.close();
            mmSocket.close();
            myLabel.setText("Bluetooth Closed");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // this will send text data to be printed by the bluetooth printer
    void sendData() throws IOException {
        try {

            // the text typed by the user
            String msg = myTextbox.getText().toString();
            msg += "\n";

            mmOutputStream.write(msg.getBytes());

            // tell the user data were sent
            myLabel.setText("Data sent.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // this will find a bluetooth printer device
    void findBT() {

        try {
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

            if (mBluetoothAdapter == null) {
                myLabel.setText("No bluetooth adapter available");
            }

            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBluetooth, 0);
            }

            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

            if (pairedDevices.size() > 0) {
                for (BluetoothDevice device : pairedDevices) {

                    // RPP300 is the name of the bluetooth printer device
                    // we got this name from the list of paired devices
                    if (device.getName().equals("PrinterUtil")) {
                        mmDevice = device;
                        break;
                    }
                }
            }

            myLabel.setText("Bluetooth device found.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // tries to open a connection to the bluetooth printer device
    void openBT() throws IOException {
        try {

            // Standard SerialPortService ID
            UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
            mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);
            mmSocket.connect();
            mmOutputStream = mmSocket.getOutputStream();
            mmInputStream = mmSocket.getInputStream();

            beginListenForData();

            myLabel.setText("Bluetooth Opened");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /*
     * after opening a connection to bluetooth printer device,
     * we have to listen and check if a data were sent to be printed.
     */
    void beginListenForData() {
        try {
            final Handler handler = new Handler();

            // this is the ASCII code for a newline character
            final byte delimiter = 10;

            stopWorker = false;
            readBufferPosition = 0;
            readBuffer = new byte[1024];

            workerThread = new Thread(new Runnable() {
                public void run() {

                    while (!Thread.currentThread().isInterrupted() && !stopWorker) {

                        try {

                            int bytesAvailable = mmInputStream.available();

                            if (bytesAvailable > 0) {

                                byte[] packetBytes = new byte[bytesAvailable];
                                mmInputStream.read(packetBytes);

                                for (int i = 0; i < bytesAvailable; i++) {

                                    byte b = packetBytes[i];
                                    if (b == delimiter) {

                                        byte[] encodedBytes = new byte[readBufferPosition];
                                        System.arraycopy(
                                                readBuffer, 0,
                                                encodedBytes, 0,
                                                encodedBytes.length
                                        );

                                        // specify US-ASCII encoding
                                        final String data = new String(encodedBytes, "US-ASCII");
                                        readBufferPosition = 0;

                                        // tell the user data were sent to bluetooth printer device
                                        handler.post(new Runnable() {
                                            public void run() {
                                                myLabel.setText(data);
                                            }
                                        });

                                    } else {
                                        readBuffer[readBufferPosition++] = b;
                                    }
                                }
                            }

                        } catch (IOException ex) {
                            stopWorker = true;
                        }

                    }
                }
            });

            workerThread.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
