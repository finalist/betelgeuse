package com.gamaray.arex;

public class Animation
{
	public static int LOC_X = 0, LOC_Y = 1, LOC_Z = 2;
	public static int ROT_X = 3, ROT_Y = 4, ROT_Z = 5;
	public static int SCALE_X = 6, SCALE_Y = 7, SCALE_Z = 8;
	public static int FRAME = 9;
	
	int xmlAttrib;
	long xmlStartTime, xmlEndTime;
	float xmlStartValue, xmlEndValue;
	float xmlRate; 
}
