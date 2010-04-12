package com.gamaray.arex.gui;


public class Vector2D 
{
	public float x, y;
	
	
	public Vector2D()
	{
		setTo(0, 0);
	}
	
	public Vector2D(float x, float y)
	{
		setTo(x, y);
	}
	
	public void setTo(float x, float y)
	{
		this.x = x;
		this.y = y;
	}
	
	public void rotate(double theta)
	{
		float xp = (float) Math.cos(theta) * x - (float) Math.sin(theta) * y;
		float yp = (float) Math.sin(theta) * x + (float) Math.cos(theta) * y;
			
		x = xp;
		y = yp;
	}
	
	public void add(float x, float y)
	{
		this.x += x;
		this.y += y;
	}
}
