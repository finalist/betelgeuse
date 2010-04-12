package com.gamaray.arex.gui;

import java.util.ArrayList;

import java.text.BreakIterator;
import java.util.Locale;

import com.gamaray.arex.render3d.*;

public class TextBlock implements Drawable
{
	String txt;
	float fontSize;
	float width, height;
	float txtAreaWidth, txtAreaHeight;
	String lines[];
	float lineWidths[];
	float lineHeight;
	float maxLineWidth;
	float pad;
	int borderColor, bgColor, textColor;
	
	public TextBlock(String txtInit, float fontSizeInit, float maxWidth, DrawWindow dw)
	{
		this(txtInit, fontSizeInit, maxWidth, Color.rgb(255,255,255), Color.rgb(0,0,0), Color.rgb(255,255,255), dw.getTextAscent() / 2, dw);
	}
	
	public TextBlock(String txtInit, float fontSizeInit, float maxWidth, int borderColor, int bgColor, int textColor, float pad, DrawWindow dw)
	{
		this.pad = pad;
		this.bgColor = bgColor;
		this.borderColor = borderColor;
		this.textColor = textColor;
		
		try
		{
			parseText(txtInit, fontSizeInit, maxWidth, dw);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			parseText("TEXT PARSE ERROR", 12, 200, dw);
		}
	}
	
	private void parseText(String txtInit, float fontSizeInit, float maxWidth, DrawWindow dw)
	{
		dw.setFontSize(fontSizeInit);
		
		txt = txtInit;
		fontSize = fontSizeInit;
		txtAreaWidth = maxWidth - pad * 2;
		lineHeight = dw.getTextAscent() + dw.getTextDescent() + dw.getTextLeading();
		
		ArrayList lineList = new ArrayList();
		
		BreakIterator boundary = BreakIterator.getWordInstance();
		boundary.setText(txt);
        
		int start = boundary.first();
		int end = boundary.next();
		int prevEnd = start;
		while (end != BreakIterator.DONE)
		{
			String line = txt.substring(start, end);
			String prevLine = txt.substring(start, prevEnd);
			float lineWidth = dw.getTextWidth(line);
			
			if (lineWidth > txtAreaWidth)
			{
				lineList.add(prevLine);
				
				start = prevEnd;
			}
			
			prevEnd = end;
			end = boundary.next();
		}
		String line = txt.substring(start, prevEnd);
		lineList.add(line);
		
		lines = new String[lineList.size()];
		lineWidths = new float[lineList.size()];
		lineList.toArray(lines);
		
		maxLineWidth = 0;
		for (int i = 0; i < lines.length; i++)
		{
			lineWidths[i] = dw.getTextWidth(lines[i]);
			if (maxLineWidth < lineWidths[i]) maxLineWidth = lineWidths[i];
		}
		txtAreaWidth = maxLineWidth;
		txtAreaHeight = lineHeight * lines.length;
		
		width = txtAreaWidth + pad * 2;
		height = txtAreaHeight + pad * 2;
	}
	
	public void draw(DrawWindow dw)
	{
		dw.setFontSize(fontSize);
		
		dw.setFill(true);
		dw.setColor(bgColor);
		dw.drawRectangle(0, 0, width, height);
		
		dw.setFill(false);
		dw.setColor(borderColor);
		dw.drawRectangle(0, 0, width, height);
		
		dw.setColor(textColor);
		for (int i = 0; i < lines.length; i++)
		{
			String line = lines[i];
			
			dw.drawText(pad, pad + lineHeight * i + dw.getTextAscent(), line);
		}
	}
	
	public float getWidth()
	{
		return width;
	}
	
	public float getHeight()
	{
		return height;
	}
}
