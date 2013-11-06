package it.baobab.makerlab.ld13bari;

import android.util.Log;

public class Lights {
	private static final String TAG = "Lights";
	
	private static short Lamp = 0x00, Red = 0x00, Green = 0x00, Blue = 0x00;
	
	private static Lights ref = null;
	public static  Lights getInstance(){
		if(ref == null)ref = new Lights();
		return ref;
	}	
	private Lights() {	}
		
	
	public byte[] getValues(){
		Log.d(TAG, String.format("%02X", Lamp)	+ " "
				+ String.format("%02X", Red)	+ " "
				+ String.format("%02X", Green)	+ " "
				+ String.format("%02X", Blue));
		
		return new byte[]{ 
				(byte) (Lamp&0xFF), 
				(byte) (Red&0xFF), 
				(byte) (Green&0xFF), 
				(byte) (Blue&0xFF)
				};
	}
	
	/**
	 * @param lamp the lamp to set
	 */
	public void setLamp(short lamp) {
		Lamp = lamp;
	}
	/**
	 * @return the lamp
	 */
	public short getLamp() {
		return Lamp;
	}

	/**
	 * @param red the red to set
	 */
	public void setRed(short red) {
		Red = red;
	}

	/**
	 * @param green the green to set
	 */
	public void setGreen(short green) {
		Green = green;
	}

	/**
	 * @param blue the blue to set
	 */
	public void setBlue(short blue) {
		Blue = blue;
	}
	
	
	
	

}
