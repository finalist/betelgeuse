package com.gamaray.arex.format3d;

import java.io.*;

public interface SimpleVFS 
{
	public InputStream getInputStream(String file)
		throws Exception;
	public void returnInputStream(InputStream is)
		throws Exception;
	public OutputStream getOutputStream(String file)
		throws Exception;
	public void returnOutputStream(OutputStream os)
		throws Exception;
}
