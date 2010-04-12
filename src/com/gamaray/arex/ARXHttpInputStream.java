package com.gamaray.arex;

import java.io.InputStream;
import java.io.IOException;

public class ARXHttpInputStream extends InputStream 
{
	public static boolean killMode = false;
	
	int contentLength;
	int totalBytesRead;
	InputStream is;
	
	public ARXHttpInputStream(InputStream is, int contentLength)
	{
		this.is = is;
		this.contentLength = contentLength;
	}
	
	public float pctDownloadComplete()
	{
		float pct = (totalBytesRead / (float) contentLength);
		
		if (pct > 100f) pct = 100f;
		if (pct < 0f) pct = 0f;
		
		return pct;
	}
	
	public int available()
		throws IOException
	{
		if (killMode) kill();
		
		return is.available();
	}
	
	public void close()
		throws IOException
	{
		if (killMode) kill();
		
		is.close();
	}
	
	public void mark(int readlimit)
	{
		is.mark(readlimit);
	}
	
	public boolean markSupported()
	{
		return is.markSupported();
	}
	
	public int read(byte[] b, int offset, int length)
		throws IndexOutOfBoundsException, IOException
	{
		if (killMode) kill();
		
		int bytesRead = is.read(b, offset, length);
		
		if (bytesRead != -1) totalBytesRead += bytesRead;
		
		return bytesRead;
	}
	
	public int read(byte[] b)
		throws IOException
	{
		if (killMode) kill();
		
		int bytesRead = is.read(b);
		
		if (bytesRead != -1) totalBytesRead += bytesRead;
		
		return bytesRead;
	}
	
	public int read()
		throws IOException
	{
		if (killMode) kill();
		
		int bytesRead = is.read();
		
		if (bytesRead != -1) totalBytesRead += 1;
		
		return bytesRead;
	}
	
	public synchronized void reset()
		throws IOException
	{
		if (killMode) kill();
		
		is.reset();
	}
	
	public long skip(long n)
		throws IOException
	{
		if (killMode) kill();
		
		return is.skip(n);
	}
	
	private void kill()
		throws IOException
	{
		is.close();
		throw new IOException("KILL MODE");
	}
}
