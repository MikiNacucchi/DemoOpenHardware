package it.baobab.makerlab.ld13bari;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

class ConnectedThread extends Thread {    
	private static final String TAG = "ConnectedThread";
	
    public static final int MESSAGE_NOCONNECTION	=	-1;
    public static final int MESSAGE_NEWBYTES		=	0;
    
	private final BluetoothSocket mmSocket;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;
    
	private Handler mHandler;
 
    public ConnectedThread(Handler handler, BluetoothSocket socket) {
    	mHandler = handler;
        mmSocket = socket;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;
 
        // Get the input and output streams, using temp objects because
        // member streams are final
        try {
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        } catch (IOException e) { }
 
        mmInStream = tmpIn;
        mmOutStream = tmpOut;
    }
 
    public void run() {
        byte[] buffer = new byte[1024];  // buffer store for the stream
        int bytes; // bytes returned from read()
        
        final char CHAR_CR	= '\r';
		final char CHAR_LF	= '\n';
		final char CHAR_DOT	= '.';
		
		final short INIT		= 0;
		final short START_TEMP	= 1;
		final short END_TEMP	= 2;
		short state	=	INIT;
		
		String temp = new String();
		boolean hideDot = false;	
 
        // Keep listening to the InputStream until an exception occurs
        while (true) {
            try {
                // Read from the InputStream
                bytes = mmInStream.read(buffer);
                
                for (int i = 0; i < bytes; i++) {
	 				//Log.d(TAG, String.format("%02X", buffer[i]));
	 				
	 				/*Finite State Machine for build message 
	 				 * Retrieve ... 0x0D xx xx . xx xx 0x0A ... 
	 				 * 					[ temperature ]
	 				 * */
                	switch (state) {
					case INIT:
						if(buffer[i] == CHAR_LF){
							state = START_TEMP;									
						}
						break;
					case START_TEMP:								
						if(buffer[i] != CHAR_CR && buffer[i] != CHAR_LF){					
							if(buffer[i] == CHAR_DOT && hideDot) buffer[i] = ' ';
							
							Character c = new Character((char) buffer[i]);
							
							temp = temp.concat(Character.toString(c));
							
							hideDot = !hideDot;
						}
						else{
							state = END_TEMP;
						}
						
						break;
					case END_TEMP:
						// Send the obtained bytes to the UI activity		                
		                mHandler.obtainMessage(MESSAGE_NEWBYTES, temp)
		                        .sendToTarget();		                
						
						//Log.d(TAG, temp);	
						
						temp = new String();								
						state = INIT;
						
						break;
					}
                }
                
                
                
                
            } catch (IOException e) {
            	mHandler.obtainMessage(MESSAGE_NOCONNECTION).sendToTarget();
                break; //TODO Reconnect
            }
        }
    }
 
    /* Call this from the main activity to send data to the remote device */
    public void write(byte[] bytes) {
        try {
            mmOutStream.write(bytes);
        } catch (IOException e) { }
    }
 
    /* Call this from the main activity to shutdown the connection */
    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) { }
    }
}