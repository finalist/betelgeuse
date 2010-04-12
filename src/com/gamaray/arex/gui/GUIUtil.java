package com.gamaray.arex.gui;

import java.io.InputStream;

import com.gamaray.arex.*;

public class GUIUtil 
{
	public static Bitmap loadIcon(String name, ARXContext ctx)
	{
		Bitmap bmp = null;
		try
		{
			InputStream isIcon = ctx.getResourceInputStream(name);
			bmp = ctx.createBitmap(isIcon);
			ctx.returnResourceInputStream(isIcon);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		
		return bmp;
	}
}
