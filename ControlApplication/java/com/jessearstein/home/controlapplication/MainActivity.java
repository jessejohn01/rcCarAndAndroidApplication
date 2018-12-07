package com.jessearstein.home.controlapplication;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.os.ParcelUuid;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Set;
import java.util.UUID;


import io.github.controlwear.virtual.joystick.android.JoystickView;

;

public class MainActivity extends AppCompatActivity {
    private static final UUID MY_UUID_INSECURE =
            UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

    private static final int REQUEST_ENABLE_BT = 1;
    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private BluetoothDevice mmDevice;
    private UUID deviceUUID;
    ConnectedThread mConnectedThread;
    private Handler handler;

    String TAG = "MainActivity";



    public void pairDevice() {

        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        Log.e("MAinActivity", "" + pairedDevices.size());
        if (pairedDevices.size() > 0) {
            Object[] devices = pairedDevices.toArray();
            BluetoothDevice device = (BluetoothDevice) devices[0];
            for(BluetoothDevice iterator : pairedDevices)
            {
                if(iterator.getAddress().equals(DEVICE_ADDRESS))
                {
                    device = iterator;
                    Toast.makeText(getApplicationContext(), "Bluetooth Connection Successful", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Successfully Connected to right device.");
                    break;
                }
            }
            //ParcelUuid[] uuid = device.getUuids();
            Log.e("MAinActivity", "" + device);
            //Log.e("MAinActivity", "" + uuid)

            ConnectThread connect = new ConnectThread(device, MY_UUID_INSECURE);
            connect.start();

        }
    }

    private class ConnectThread extends Thread {
        private BluetoothSocket mmSocket;

        public ConnectThread(BluetoothDevice device, UUID uuid) {
            Log.d(TAG, "ConnectThread: started.");
            mmDevice = device;
            deviceUUID = uuid;
        }

        public void run() {
            BluetoothSocket tmp = null;
            Log.i(TAG, "RUN mConnectThread ");

            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try {
                Log.d(TAG, "ConnectThread: Trying to create InsecureRfcommSocket using UUID: "
                        + MY_UUID_INSECURE);
                tmp = mmDevice.createRfcommSocketToServiceRecord(MY_UUID_INSECURE);
            } catch (IOException e) {
                Log.e(TAG, "ConnectThread: Could not create InsecureRfcommSocket " + e.getMessage());
            }

            mmSocket = tmp;

            // Make a connection to the BluetoothSocket

            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                mmSocket.connect();

            } catch (IOException e) {
                // Close the socket
                try {
                    mmSocket.close();
                    Log.d(TAG, "run: Closed Socket.");
                } catch (IOException e1) {
                    Log.e(TAG, "mConnectThread: run: Unable to close connection in socket " + e1.getMessage());
                }
                Log.d(TAG, "run: ConnectThread: Could not connect to UUID: " + MY_UUID_INSECURE);
            }

            //will talk about this in the 3rd video
            connected(mmSocket);
        }

        public void cancel() {
            try {
                Log.d(TAG, "cancel: Closing Client Socket.");
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "cancel: close() of mmSocket in Connectthread failed. " + e.getMessage());
            }
        }
    }

    private void connected(BluetoothSocket mmSocket) {
        Log.d(TAG, "connected: Starting.");

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(mmSocket);
        mConnectedThread.start();
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            Log.d(TAG, "ConnectedThread: Starting.");

            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;


            try {
                tmpIn = mmSocket.getInputStream();
                tmpOut = mmSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run(){
            byte[] buffer = new byte[1024];  // buffer store for the stream

            int bytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                // Read from the InputStream
                try {
                    bytes = mmInStream.read(buffer);
                    final String incomingMessage = new String(buffer, 0, bytes);
                    Log.d(TAG, "InputStream: " + incomingMessage);

                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            //view_data.setText(incomingMessage);
                        }
                    });


                } catch (IOException e) {
                    Log.e(TAG, "write: Error reading Input Stream. " + e.getMessage() );
                    break;
                }
            }
        }


        public void write(byte[] bytes) {
            String text = new String(bytes, Charset.defaultCharset());
            Log.d(TAG, "write: Writing to outputstream: " + text);
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
                Log.e(TAG, "write: Error writing to output stream. " + e.getMessage());
            }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
            }
        }
    }


    private TextView mTextViewAngleLeft;
    private TextView mTextViewStrengthLeft;

    private TextView mTextViewAngleRight;
    private TextView mTextViewStrengthRight;
    private TextView mTextViewCoordinateRight;
    private TextView reverseText;
    private Button stopButton;
    private Button reverseButton;

    int minMotor = 1450;
    int maxMotor = 1510;
    int reverseMotorMax = 1340;
    int reverseMotorMin = 1240;
    int motorRange = maxMotor - minMotor;
    int currentMotor = 1350;
    int count = 0;
    int minSteering = 95;
    int maxSteering = 165;
    int steeringRange = maxSteering - minSteering;
    int currentSteering = 125;

    boolean reversed = false;

    private final String DEVICE_ADDRESS = "98:D3:31:FD:95:AF";
    private final UUID PORT_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    private static final int REQUEST_ENABLED = 0;
    private static final int REQUEST_DISCOVERABLE = 0;

    String sendValue;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (bluetoothAdapter != null && !bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new
                    Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        Start_Server();
        pairDevice();

        mTextViewAngleLeft = (TextView) findViewById(R.id.textView_angle_left);
        mTextViewStrengthLeft = (TextView) findViewById(R.id.textView_strength_left);
        reverseText = (TextView) findViewById(R.id.reverseText);
        stopButton = (Button) findViewById(R.id.button);
        reverseButton = (Button) findViewById(R.id.button2);
        JoystickView joystickLeft = (JoystickView) findViewById(R.id.joystickView_left);
        joystickLeft.setOnMoveListener(new JoystickView.OnMoveListener() {
            @Override
            public void onMove(int angle, int strength) {
                mTextViewStrengthLeft.setText(strength + "%");
                mTextViewAngleLeft.setText(currentMotor + "");
                if(reversed == false) {
                    reverseText.setText("Drive");
                    currentMotor = (((strength - 0) * motorRange) / 100) + minMotor;

                    sendValue = (Integer.toString(currentMotor) + "." + Integer.toString(currentSteering) + '\n');
                    mConnectedThread.write(sendValue.getBytes());
                }

                if(reversed == true){
                    reverseText.setText("In Reverse");
                    currentMotor = reverseMotorMax - strength;
                    sendValue = (Integer.toString(currentMotor) + "." + Integer.toString(currentSteering) + '\n');
                    mConnectedThread.write(sendValue.getBytes());
                }
            }
        });


        mTextViewAngleRight = (TextView) findViewById(R.id.textView_angle_right);
        mTextViewStrengthRight = (TextView) findViewById(R.id.textView_strength_right);
        mTextViewCoordinateRight = findViewById(R.id.textView_coordinate_right);

        final JoystickView joystickRight = (JoystickView) findViewById(R.id.joystickView_right);
        joystickRight.setOnMoveListener(new JoystickView.OnMoveListener() {
            @SuppressLint("DefaultLocale")
            @Override
            public void onMove(int angle, int strength) {
                mTextViewAngleRight.setText(angle + "°");
                mTextViewStrengthRight.setText(strength + "%");
                mTextViewCoordinateRight.setText(
                        String.format("x%03d:y%03d",
                                joystickRight.getNormalizedX(),
                                joystickRight.getNormalizedY())
                );
                int steeringX = joystickRight.getNormalizedX();
                currentSteering = (((steeringX - 0) * steeringRange) / 100) + minSteering;
                sendValue = (Integer.toString(currentMotor) + "." + Integer.toString(currentSteering) + '\n');
                mConnectedThread.write(sendValue.getBytes());





            }


        });




        stopButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                sendValue = (Integer.toString(1300) + "." + Integer.toString(currentSteering) + '\n');
                mConnectedThread.write(sendValue.getBytes());
            }
        });
        reverseButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                sendValue = (Integer.toString(1300) + "." + Integer.toString(currentSteering) + '\n');
                mConnectedThread.write(sendValue.getBytes());
                reversed = !reversed;
            }
        });
       /* final Handler handler = new Handler();
        final int delay = 50; //milliseconds

        handler.postDelayed(new Runnable(){
            public void run(){

                    //mConnectedThread.write(sendValue.getBytes());

                handler.postDelayed(this, delay);
            }
        }, delay);*/


        /*
        motorValue = (SeekBar) findViewById(R.id.Motor); //Sets our motor by its xml id
        steeringValue = (SeekBar) findViewById(R.id.Steering); // Same for Steering
        motorTitle = (TextView) findViewById(R.id.MotorPower); // Text motor title id
        steeringTitle = (TextView) findViewById(R.id.SteeringDirection);

        motorValue.setMax(maxMotor); //Sets the max for the motor seekbar.
        motorValue.setMin(minMotor);
        motorValue.setProgress(currentMotor); //Sets progress for motor.

        steeringValue.setMax(maxSteering);
        steeringValue.setMin(minSteering);
        steeringValue.setProgress(currentSteering);

        motorTitle.setText("" + currentMotor);
        steeringTitle.setText("" + currentSteering);

        motorValue.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    currentMotor = progress;
                    motorTitle.setText("" + currentMotor);

                    sendValue = (Integer.toString(currentMotor) + "." + Integer.toString(currentSteering) + '\n');
                    outputStream.write(sendValue.getBytes());
                } catch (IOException e) {
                    Toast.makeText(getApplicationContext(), "Error on Motor Send", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }


        });
        steeringValue.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                try {

                    currentSteering = progress;
                    steeringTitle.setText("" + currentSteering);

                    sendValue = (Integer.toString(currentMotor) + "." + Integer.toString(currentSteering) + '\n');
                    outputStream.write(sendValue.getBytes());

                } catch (IOException e) {
                    Toast.makeText(getApplicationContext(), "Error on Steering Send", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });*/


    }
    public void Start_Server() {

        AcceptThread accept = new AcceptThread();
        accept.start();

    }
    private class AcceptThread extends Thread {

        // The local server socket
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread(){
            BluetoothServerSocket tmp = null ;

            // Create a new listening server socket
            try{
                tmp = bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord("appname", MY_UUID_INSECURE);

                Log.d(TAG, "AcceptThread: Setting up Server using: " + MY_UUID_INSECURE);
            }catch (IOException e){
                Log.e(TAG, "AcceptThread: IOException: " + e.getMessage() );
            }

            mmServerSocket = tmp;
        }

        public void run(){
            Log.d(TAG, "run: AcceptThread Running.");

            BluetoothSocket socket = null;

            try{
                // This is a blocking call and will only return on a
                // successful connection or an exception
                Log.d(TAG, "run: RFCOM server socket start.....");

                socket = mmServerSocket.accept();

                Log.d(TAG, "run: RFCOM server socket accepted connection.");

            }catch (IOException e){
                Log.e(TAG, "AcceptThread: IOException: " + e.getMessage() );
            }


            if(socket != null){
                connected(socket);
            }

            Log.i(TAG, "END mAcceptThread ");
        }

        public void cancel() {
            Log.d(TAG, "cancel: Canceling AcceptThread.");
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "cancel: Close of AcceptThread ServerSocket failed. " + e.getMessage() );
            }
        }


}
}






