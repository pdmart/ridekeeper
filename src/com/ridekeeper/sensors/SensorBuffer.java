package com.ridekeeper.sensors;

/*
 * Used to keep a buffer of all sensor data until we write
 * to a file and purge the buffer
 * 		- Designed for RideKeeper package
 */
public class SensorBuffer {
	// Class variables
	private int size;
	private float[][] bufAcc;
	private float[][] bufMag;
	private float[][] bufGyro;
	private float[][] bufRvec;
	
	
	// Constructor
	public SensorBuffer(int size){
		
	}
	
	// 
}
