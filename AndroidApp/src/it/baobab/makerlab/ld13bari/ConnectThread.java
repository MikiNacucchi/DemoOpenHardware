package it.baobab.makerlab.ld13bari;

import java.io.IOException;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;

class ConnectThread extends Thread {
	private static final byte[] MAC_DEVICE = new byte[]{0x20, 0x13, 0x05, 0x15, 0x03, 0x74};// My CSR HC-06 MAC 
	private static final UUID UUID_SPP = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); //UUID standard for SPP
	   
	private final Handler mHandler;
	private final BluetoothAdapter mBluetoothAdapter;
	private final BluetoothDevice mmDevice;
	private final BluetoothSocket mmSocket;
 
    public ConnectThread(BluetoothAdapter bluetoothAdapter, Handler handler) {
        // Use a temporary object that is later assigned to mmSocket,
        // because mmSocket is final
        BluetoothSocket tmp = null;
        
        mHandler = handler;
        
        mBluetoothAdapter = bluetoothAdapter;       
        mmDevice = bluetoothAdapter.getRemoteDevice(MAC_DEVICE);        
 
        // Get a BluetoothSocket to connect with the given BluetoothDevice
        try {
            // MY_UUID is the app's UUID string, also used by the server code
            tmp = mmDevice.createInsecureRfcommSocketToServiceRecord(UUID_SPP);
        } catch (IOException e) { }
        mmSocket = tmp;
    }
 
    public void run() {
        // Cancel discovery because it will slow down the connection
        mBluetoothAdapter.cancelDiscovery();
 
        try {
            // Connect the device through the socket. This will block
            // until it succeeds or throws an exception
            mmSocket.connect();
        } catch (IOException connectException) {
            // Unable to connect; close the socket and get out
            try {
                mmSocket.close();
            } catch (IOException closeException) { }
            return;
        }
 
        // Do work to manage the connection (in a separate thread)
        new ConnectedThread(mHandler, mmSocket).start();
        
        //TODO sure?
        interrupt();
    }

	/** Will cancel an in-progress connection, and close the socket */
    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) { }
    }
}