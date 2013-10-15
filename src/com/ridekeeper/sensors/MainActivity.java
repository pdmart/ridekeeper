package com.ridekeeper.sensors;


import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorEvent; 
import android.hardware.SensorManager;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.RelativeLayout; 
import android.location.Location; 
import android.location.LocationListener; 
import android.location.LocationManager; 

import com.example.ridekeeper.R;
import java.io.File; 
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

public class MainActivity extends Activity implements SensorEventListener, LocationListener {
	
	// ------------------- Operation Flag ----------------------
	boolean running;

	// --------- Sensor manager and sensor objects -------------
	private SensorManager mSensorManager; 
	// acceleration (Hardware)
	private Sensor mAcc; 
	// magnetic (Hardware)
	private Sensor mMag; 
	// gyroscope (Hardware)
	private Sensor mGyr;
	// Linear acceleration (Software)
	private Sensor mAccLin; 
	// rotation vector (Software)
	private Sensor mRvec;
	// temporary storage arrays
	float[] tmpEuler 	= new float[3];
	float[] tmpOrientR 	= new float[9];
	float[] tmpOrientI 	= new float[9];
	float[] tmpMag 		= new float[3]; 
	float[] tmpAcc 		= new float[3]; 
	float[] tmpAccLin 	= new float[3];
	
	// ---------------- Location Services ----------------------
	protected LocationManager mLocationManager; 
	protected LocationListener mLocationListener; 
	protected Context context; 
	
	// ---------------- Graphical Objects ----------------------
	TextView text_numDataPoints;	
	int numDataPoints = 0;
	TextView text_status;	
	RelativeLayout layout;   
	//final Button button_begin = (Button) findViewById(R.id.button_begin);
	//final Button button_stop = (Button) findViewById(R.id.button_stop);
	
	// ------------------- Output File -------------------------
	File outputFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/ridekeeper.txt");
	
	// -------------------- On Create --------------------------
	@Override
	protected final void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		
		//Method to setContentView
		setContentView(R.layout.activity_main);
	
		// initialize sensor objects
		mSensorManager 		= (SensorManager)getSystemService(Context.SENSOR_SERVICE);
		mAcc 				= mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER); 
		mAccLin 			= mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
		mMag 				= mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
		mGyr				= mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
		mLocationManager	= (LocationManager)getSystemService(Context.LOCATION_SERVICE); 
		mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this); 
		
		// set sensor frequencies
		mSensorManager.registerListener(this, mAcc, SensorManager.SENSOR_DELAY_GAME);
		mSensorManager.registerListener(this, mMag, SensorManager.SENSOR_DELAY_GAME);
		mSensorManager.registerListener(this, mGyr, SensorManager.SENSOR_DELAY_GAME);
		
		// initialize the layout  
		layout = (RelativeLayout)findViewById(R.id.relativeLayout); 
		text_numDataPoints = (TextView) findViewById(R.id.textView_numDataPoints);
		text_status = (TextView) findViewById(R.id.textView_status);
	}
	
	// the following four methods are required by the LocationListener interface
	@Override
	public void onLocationChanged(Location location){ 
		
	}
	
	@Override 
	public void onProviderDisabled(String provider){ 
		Log.d("GPS", "gps disabled"); 
	}
	
	@Override
	public void onProviderEnabled(String provider){ 
		Log.d("GPS", "gps enabled"); 
	}
	
	public void onStatusChanged(String provider, int status, Bundle extras){ 
		Log.d("GPS", "gps status changed"); 
	}
	
	// calculate the Euler angles of the device
	public void getEulerAngles(){
		// get rotation matrix
		SensorManager.getRotationMatrix(tmpOrientR, tmpOrientI, tmpAcc, tmpMag);
		// get Euler angles
		SensorManager.getOrientation(tmpOrientR, tmpEuler);
		
	}
	
	// Write sensor buffer to storage
	public void flushBufferAndWrite(){
		try{ 
			FileOutputStream fOut = new FileOutputStream(outputFile, true);
			OutputStreamWriter fOutWriter = new OutputStreamWriter(fOut);
			fOutWriter.write("my string here\n");
			fOutWriter.close(); 
			
          } catch (Exception e) {
              Log.d("FOUT", "exception in file writing");
          }
	}
	
	// Write a single sensor line to file
	public void writeSensorData(int sensorID, float[] sensorVals){
		try{ 
			FileOutputStream fOut = new FileOutputStream(outputFile, true);
			OutputStreamWriter fOutWriter = new OutputStreamWriter(fOut);
			fOutWriter.write(sensorID + ",");
			for (float v : sensorVals){
				fOutWriter.write(v + ",");
			}
			fOutWriter.write("\n");
			fOutWriter.close(); 
			
          } catch (Exception e) {
              Log.d("FOUT", "exception in file writing");
          }
	}

	// ---------------- Sensor Manager Methods -----------------
	@Override 
	public final void onAccuracyChanged(Sensor sensor, int accuracy) {
		Log.d("SENSE", "sensor accuracy changed");	
	}
	
	@Override 
	public final void onSensorChanged(SensorEvent event) {
		
		Sensor sensor = event.sensor; 
		
		// don't do anything if we're stopped
		if (!running)
			return;
		
		switch (sensor.getType()){
		
			case Sensor.TYPE_LINEAR_ACCELERATION:
				tmpAccLin[0] = event.values[0];  
				tmpAccLin[1] = event.values[1]; 
				tmpAccLin[2] = event.values[2]; 
				break;
			
			case Sensor.TYPE_MAGNETIC_FIELD:
				tmpMag[0] = event.values[0]; 
				tmpMag[1] = event.values[1]; 
				tmpMag[2] = event.values[2]; 
				break;
			
			case Sensor.TYPE_ACCELEROMETER:
				tmpAcc[0] = event.values[0];
				tmpAcc[1] = event.values[1]; 
				tmpAcc[2] = event.values[2]; 
				writeSensorData(1,tmpAccLin);
				text_numDataPoints.setText(Integer.toString(++numDataPoints));
				break;
		}
		
		getEulerAngles(); 
	}
	
	
	// ------------------ on application resumption --------------------
	@Override 
	protected void onResume() { 
		super.onResume() ; 
		// resume the sensors here ...
	}
	
	// ------------------ on application pause --------------------
	@Override 
	protected void onPause () { 
		super.onPause(); 
		mSensorManager.unregisterListener(this); 
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	// ------------------ BEGIN button --------------------
	public void buttonPress_begin(View view){
		// TODO: start new text file
		running = true;
		text_status.setText("running");
	}
	
	// ------------------ STOP button --------------------
	public void buttonPress_stop(View view){
		running = false;
		text_status.setText("stopped");
	}
}
