package com.smsreceive.collarsmsserver;

public class SMSData 
{
	// Ecollar phone number
	private String phoneNumber;
	
	// Date and Time at which sms has been received
	private int year;
	private int month;
	private int date;
	
	private int hours;
	private int min;
	private int sec;
	
	// Collar latitude, longitude and altitude
	private double latitude;
	private double longitude;
	private int altitude;
	
	// Collar status- voltage, sms failure count and last sms failure type
	private float voltage;
	private int gsmFailureCount;
	private int lastGsmFailureType;
	
	
	
	
	public String getPhoneNumber() {
		return phoneNumber;
	}
	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}
	public int getYear() {
		return year;
	}
	public void setYear(int year) {
		this.year = year;
	}
	public int getMonth() {
		return month;
	}
	public void setMonth(int month) {
		this.month = month;
	}
	public int getDate() {
		return date;
	}
	public void setDate(int date) {
		this.date = date;
	}
	public int getHours() {
		return hours;
	}
	public void setHours(int hours) {
		this.hours = hours;
	}
	public int getMin() {
		return min;
	}
	public void setMin(int min) {
		this.min = min;
	}
	public int getSec() {
		return sec;
	}
	public void setSec(int sec) {
		this.sec = sec;
	}
	public double getLatitude() {
		return latitude;
	}
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}
	public double getLongitude() {
		return longitude;
	}
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
	public double getAltitude() {
		return altitude;
	}
	public void setAltitude(int altitude) {
		this.altitude = altitude;
	}
	public float getVoltage() {
		return voltage;
	}
	public void setVoltage(float voltage) {
		this.voltage = voltage;
	}
	public int getGsmFailureCount() {
		return gsmFailureCount;
	}
	public void setGsmFailureCount(int gsmFailureCount) {
		this.gsmFailureCount = gsmFailureCount;
	}
	public int getLastGsmFailureType() {
		return lastGsmFailureType;
	}
	public void setLastGsmFailureType(int lastGsmFailureType) {
		this.lastGsmFailureType = lastGsmFailureType;
	}
	@Override
	public String toString() {
		return "SMSData [getPhoneNumber()=" + getPhoneNumber() + ", getYear()="
				+ getYear() + ", getMonth()=" + getMonth() + ", getDate()="
				+ getDate() + ", getHours()=" + getHours() + ", getMin()="
				+ getMin() + ", getSec()=" + getSec() + ", getLatitude()="
				+ getLatitude() + ", getLongitude()=" + getLongitude()
				+ ", getAltitude()=" + getAltitude() + ", getVoltage()="
				+ getVoltage() + ", getGsmFailureCount()="
				+ getGsmFailureCount() + ", getLastGsmFailureType()="
				+ getLastGsmFailureType() + "]";
	}
	
}
