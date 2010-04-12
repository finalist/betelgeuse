package com.gamaray.arex.gui;

public class MenuItem 
{
	public int id;
	public String txt;
	
	public MenuItem(int id, String txt)
	{
		this.id = id;
		this.txt = txt;
	}
	
	public String toString()
	{
		return "(" + id + "," + txt + ")";
	}
}
