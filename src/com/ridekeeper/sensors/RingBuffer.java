/*
 * Ring Buffer class for temporary sensor value storage
 * 		- for use with RideKeeper software
 */

package com.ridekeeper.sensors;

public class RingBuffer {
	// Private variables
	private int head, tail; 
	private int numEntries; 
	private final int size;
	private float[] buffer; 

	// Public constructor
	public RingBuffer(int size) {
		this.size = size;
		buffer = new float[size];
		head = 0; 
		tail = 0; 
		numEntries = 0; 
	}
	
	// add an entry to the buffer
	public void push( float input ){
		if(numEntries < size){
			numEntries++; 
			buffer[head] = input; 
			head = (head + 1)%size; 
		}else{
			buffer[head] = input; 
			head = (head + 1)%size; 
			tail = (tail + 1)%size; 
		}
		
	}
	
	// pop an entry from the tail of the buffer
	public float pop(){
		if(numEntries > 0){
			float value = buffer[tail];
			tail = (tail + 1)%size;
			numEntries = numEntries -1; 
			return value; 
		}else{ 
			return 0; 
		}
	}
	
	// peek at the next value to be read
	public float peek(){
		if( numEntries > 0)
			return buffer[tail];
		return 0;
	}
	
	// return the number of elements currently in buffer
	public int getNumEntries(){ 
		return numEntries; 
	}
	
	
}

