package com.gamaray.arex.geo;

public class GeoPoint 
{
	public double lat;
	public double lon;
	public double alt;
	
	public GeoPoint()
	{
		
	}
	
	public GeoPoint(GeoPoint loc)
	{
		this.setTo(loc.lat, loc.lon, loc.alt);
	}
	
	public GeoPoint(double lat, double lon, double alt)
	{
		this.setTo(lat, lon, alt);
	}
	
	public void setTo(double lat, double lon, double alt)
	{
		this.lat = lat;
		this.lon = lon;
		this.alt = alt;
	}
	
	public void setTo(GeoPoint newPt)
	{
		this.lat = newPt.lat;
		this.lon = newPt.lon;
		this.alt = newPt.alt;
	}
	
	public String toString()
	{
		return "(lat=" + lat + ", lon=" + lon + ", alt=" + alt + ")";
	}
}
