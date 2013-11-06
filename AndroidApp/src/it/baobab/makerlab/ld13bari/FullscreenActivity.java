/*
Linux Day 2013 Bari
OpenHardware: il futuro di Arduino + Arduino incontra Android 
http://ld13bari.github.io/

Author: Alessandro Ruggieri - Miki Nacucchi
Description: 
Applicazione Android per dimostrazione di tipologie di comunicazione con Arduino:

* Rilevamento della temperatura ricevuta tramite bluetooth 

* Pilotaggio Strip LED RGB e White LED tramite USB HOST
* * basato su Android USB Host + Arduino: How to communicate without rooting your Android Tablet or Phone
* * http://android.serverbox.ch/?p=549

//TODO 
 * Demodulazione dati ricevuti su jack audio
 * * (SoftModem https://code.google.com/p/arms22/downloads/detail?name=SoftModem-005.zip)
 
*/

package it.baobab.makerlab.ld13bari;

import it.baobab.makerlab.ld13bari.util.SystemUiHider;
import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.TextView;
import ch.serverbox.android.usbcontroller.IUsbConnectionHandler;
import ch.serverbox.android.usbcontroller.L;
import ch.serverbox.android.usbcontroller.UsbController;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class FullscreenActivity extends Activity {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * If set, will toggle the system UI visibility upon interaction. Otherwise,
     * will show the system UI visibility upon interaction.
     */
    private static final boolean TOGGLE_ON_CLICK = true;

    /**
     * The flags to pass to {@link SystemUiHider#getInstance}.
     */
    private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;


    /**
     * The instance of the {@link SystemUiHider} for this activity.
     */
    private SystemUiHider mSystemUiHider;

	private String TAG = "FullscreenActivity";
	
	private static final int VID = 0x2341;
	private static final int PID = 0x0042;//<-- Arduino Mega
	private static UsbController sUsbController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fullscreen);
        
        if(sUsbController == null){
	        sUsbController = new UsbController(this, mConnectionHandler, VID, PID);
        }
        sUsbController = new UsbController(FullscreenActivity.this, mConnectionHandler, VID, PID);

        final View controlsView = findViewById(R.id.fullscreen_content_controls);
        final View contentView = findViewById(R.id.fullscreen_content);
        
        
        
        ///// Bluetooth Sensor //////////////////////////////////////////////////
        final TextView txtTemperature = ((TextView)findViewById(R.id.txtTemperature));
        
        Typeface digiType = Typeface.createFromAsset(getAssets(), "fonts/DS-DIGI.TTF");
        txtTemperature.setTypeface(digiType);
        
        
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG , "Bluetooth is not supported on this device!");
        }
        else{
			 new ConnectThread(mBluetoothAdapter, new Handler(){
			 	@Override
				public void handleMessage(Message msg) {
			 		if(msg.what == ConnectedThread.MESSAGE_NEWBYTES){
			 			txtTemperature.setText((String) msg.obj);	
			 		}
			 		
			 		else if(msg.what == ConnectedThread.MESSAGE_NOCONNECTION) {
			 			txtTemperature.setText(getString(R.string.no_temperature));			 			
			 		}			 		
			 		
					super.handleMessage(msg);
				}        	
			 }).start();
        }        
        ///// End Bluetooth Sensor //////////////////////////////////////////////////
        
        
        
        final Lights statusLights = Lights.getInstance();
        
        ///// ColorPicker //////////////////////////////////////////////////
        final ImageView colorPicker = (ImageView)findViewById(R.id.imageColorPicker);
        
        colorPicker.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {	
				float MAX_X = colorPicker.getWidth()/2;
	        	float MAX_Y = colorPicker.getHeight()/2;
	        	
				float relativeX = event.getAxisValue(MotionEvent.AXIS_X) - MAX_X;
				float relativeY = event.getAxisValue(MotionEvent.AXIS_Y) - MAX_Y;
				
				Log.d(TAG, "colorPickerEvent: " 
						+ "x=" + relativeX
						+ "y=" + relativeY);
				
				if(relativeX >= 0 && relativeY >= 0){
					statusLights.setRed(	(short) 0	);
					statusLights.setGreen(	f(relativeY, MAX_Y ));
					statusLights.setBlue(	f(relativeX, MAX_X ));	
				}
				else if(relativeX >= 0 && relativeY < 0){
					statusLights.setRed(	f(relativeY * -1, MAX_Y )	);
					statusLights.setGreen(	(short) 0	);
					statusLights.setBlue(	f(relativeX, MAX_X ));	
				}
				else if(relativeX < 0 && relativeY < 0){
					statusLights.setRed(	f(relativeY * -1, MAX_Y )	);
					statusLights.setGreen(	f(relativeX * -1, MAX_X )	);
					statusLights.setBlue(	(short) 0	);	
				}
				else{
					statusLights.setRed(	f(relativeX * -1, MAX_X )	);
					statusLights.setGreen(	f(relativeY, MAX_Y ));
					statusLights.setBlue(	(short) 0);		
				}
				
				sUsbController.send(statusLights.getValues());
				
				return false;
			}
			
			short f(float k, float max){
				return (short) ((k/max * 255) % 255);
			}
		});        
        ///// End ColorPicker //////////////////////////////////////////////////       
        ///// Lamp ///////////////////////////////////////////////////////////////////// 
        final ImageView switchLamp = (ImageView)findViewById(R.id.imageSwitchLamp);
        final int[] bkGrSwitch = {R.drawable.on, R.drawable.off};             
        
        switchLamp.setOnClickListener(new OnClickListener() {
        	int iBkGr = 0;			
        	
			@Override
			public void onClick(View v) {				
				short i = (short) (++iBkGr % bkGrSwitch.length);
				
				switchLamp.setImageDrawable(getResources().getDrawable(bkGrSwitch[i]));
				
				statusLights.setLamp((short) (i == 0	?	0x01 : 0x00));
				
				sUsbController.send(statusLights.getValues());
			}
		});
        ///// End Lamp //////////////////////////////////////////////////
        
        
        
        
        ///////////////////////////////// AUTO-GENERATED CODE /////////////////////////////////

        // Set up an instance of SystemUiHider to control the system UI for
        // this activity.
        mSystemUiHider = SystemUiHider.getInstance(this, contentView, HIDER_FLAGS);
        mSystemUiHider.setup();
        mSystemUiHider
                .setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener() {
                    // Cached values.
                    int mControlsHeight;
                    int mShortAnimTime;

                    @Override
                    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
                    public void onVisibilityChange(boolean visible) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
                            // If the ViewPropertyAnimator API is available
                            // (Honeycomb MR2 and later), use it to animate the
                            // in-layout UI controls at the bottom of the
                            // screen.
                            if (mControlsHeight == 0) {
                                mControlsHeight = controlsView.getHeight();
                            }
                            if (mShortAnimTime == 0) {
                                mShortAnimTime = getResources().getInteger(
                                        android.R.integer.config_shortAnimTime);
                            }
                            controlsView.animate()
                                    .translationY(visible ? 0 : mControlsHeight)
                                    .setDuration(mShortAnimTime);
                        } else {
                            // If the ViewPropertyAnimator APIs aren't
                            // available, simply show or hide the in-layout UI
                            // controls.
                            controlsView.setVisibility(visible ? View.VISIBLE : View.GONE);
                        }

                        if (visible && AUTO_HIDE) {
                            // Schedule a hide().
                            delayedHide(AUTO_HIDE_DELAY_MILLIS);
                        }
                    }
                });

        // Set up the user interaction to manually show or hide the system UI.
        contentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TOGGLE_ON_CLICK) {
                    mSystemUiHider.toggle();
                } else {
                    mSystemUiHider.show();
                }
            }
        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        findViewById(R.id.dummy_button).setOnTouchListener(mDelayHideTouchListener);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }


    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    Handler mHideHandler = new Handler();
    Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            mSystemUiHider.hide();
        }
    };

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }
    
    
    private final IUsbConnectionHandler mConnectionHandler = new IUsbConnectionHandler() {
		@Override
		public void onUsbStopped() {
			L.e("Usb stopped!");
		}
		
		@Override
		public void onErrorLooperRunningAlready() {
			L.e("Looper already running!");
		}
		
		@Override
		public void onDeviceNotFound() {
			if(sUsbController != null){
				sUsbController.stop();
				sUsbController = null;
			}
		}
	};

		
	
}
