package com.gamaray.arex.xml;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Iterator;

public class Element 
{
	public boolean isRoot = false;
	public String name;
	public String content;
	public HashMap attribs = new HashMap();
	
	public Element parentElement;
	public ArrayList childElements = new ArrayList();
	
	public Element getChildElement(String name)
	{
		for (int i = 0; i < childElements.size(); i++)
		{
			Element ce = (Element) childElements.get(i);
			if (ce.name.equals(name)) return ce;
		}
		
		return null;
	}
	
	public String getChildElementValue(String name)
	{
		return getChildElementValue(name, null);
	}
	
	public String getChildElementValue(String name, String defaultValue)
	{
		Element e = (Element) getChildElement(name);
		
		if (e != null)
			return e.content;
		else
			return defaultValue;
	}
	
	public String getAttribValue(String name)
	{
		return getAttribValue(name, null);
	}
	
	public String getAttribValue(String name, String defaultValue)
	{
		String value = (String) attribs.get(name);
		
		if (value != null)
			return value;
		else
			return defaultValue;
	}
	
	public String toString()
	{
		StringBuffer buf = new StringBuffer();
		print(buf);
		
		return buf.toString();
	}
	
	private void print(StringBuffer buf)
	{
		if (!isRoot)
		{
			if (attribs.size() > 0)
			{
				buf.append("<" + name + " ");
				for (Iterator itr = attribs.keySet().iterator(); itr.hasNext();)
				{
					String attribName = (String) itr.next();
					String attribValue = (String) attribs.get(attribName);
					buf.append(attribName + "=\"" + attribValue + "\"");
					if (itr.hasNext()) buf.append(" ");
				}
				buf.append(">\n");
			}
			else
			{
				buf.append("<" + name + ">\n");
			}
			
			if (content != null && content.trim().length() > 0) buf.append(content.trim() + "\n");
		}
		
		for (int i = 0; i < childElements.size(); i++)
		{
			Element ce = (Element) childElements.get(i);
			ce.print(buf);
		}
		
		if (!isRoot)
		{
			buf.append("</" + name + ">\n");
		}
	}
}
