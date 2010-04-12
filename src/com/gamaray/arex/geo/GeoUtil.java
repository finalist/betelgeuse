package com.gamaray.arex.geo;

import com.gamaray.arex.render3d.*; 

public class GeoUtil
{
	// MapQuest: http://atlas.mapquest.com/maps/latlong.adp
	
	public static double calcDistance(double lat1, double lon1, double lat2, double lon2) 
	{
		double theta = lon1 - lon2;
		double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
		dist = Math.acos(dist);
		dist = rad2deg(dist);
		dist = dist * 60 * 1.1515;
		dist = dist * 1.609344 * 1000;
		
		// temporary fix
		// x is NaN when a call from convertGeoToVector() where
		// gp is (lat=41.452184, lon=-81.476541, alt=320.0)
		// AND
		// org is (lat=41.45308332160592, lon=-81.476541, alt=320.0)
		if (Double.isNaN(dist))
			return calcDistance(lat1 + 0.000001, lon1 + 0.000001, lat2, lon2);
		else 
			return dist;
	}
	
	private static double deg2rad(double deg) 
	{
		return (deg * Math.PI / 180.0);
	}
	
	private static double rad2deg(double rad) 
	{
		return (rad * 180.0 / Math.PI);
	}
	
	public static void calcDestination(double lat1Deg, double lon1Deg, double brngDeg, double d, GeoPoint dest)
	{
		double brng = Math.toRadians(brngDeg);
		double lat1 = Math.toRadians(lat1Deg);
		double lon1 = Math.toRadians(lon1Deg);
		double R = 6371.0 * 1000.0; // m
		
		double lat2 = Math.asin( Math.sin(lat1)*Math.cos(d/R) + Math.cos(lat1)*Math.sin(d/R)*Math.cos(brng) );
		double lon2 = lon1 + Math.atan2( Math.sin(brng)*Math.sin(d/R)*Math.cos(lat1), Math.cos(d/R)-Math.sin(lat1)*Math.sin(lat2) );
                      
		dest.lat = Math.toDegrees(lat2);
		dest.lon = Math.toDegrees(lon2);
	}
	
	public static void convertGeoToVector(GeoPoint org, GeoPoint gp, Vector3D v)
	{
		// TODO: use bearing calc and sin/cos
		double z = GeoUtil.calcDistance(org.lat, org.lon, gp.lat, org.lon);
		double x = GeoUtil.calcDistance(org.lat, org.lon, org.lat, gp.lon);
		double y = gp.alt - org.alt;
		if (org.lat < gp.lat) z *= -1;
		if (org.lon > gp.lon) x *= -1;
		
		v.setTo((float) x, (float) y, (float) z);
	}
	
	public static void convertVectorToGeo(Vector3D v, GeoPoint org, GeoPoint gp)
	{
		// TODO: use bearing calc and sin/cos
		double brngNS = 0, brngEW = 90;
    	if (v.z > 0 ) brngNS = 180;
    	if (v.x < 0 ) brngEW = 270;
    	
    	GeoPoint tmp1Loc = new GeoPoint(), tmp2Loc = new GeoPoint();
    	GeoUtil.calcDestination(org.lat, org.lon, brngNS, Math.abs(v.z), tmp1Loc);
    	GeoUtil.calcDestination(tmp1Loc.lat, tmp1Loc.lon, brngEW, Math.abs(v.x), tmp2Loc);
    	
    	gp.lat = tmp2Loc.lat;
    	gp.lon = tmp2Loc.lon;
    	gp.alt = org.alt + v.y;
	}
}
