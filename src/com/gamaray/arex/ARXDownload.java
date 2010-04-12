package com.gamaray.arex;

import java.io.InputStream;
import java.io.OutputStream;

import java.util.HashMap;

import com.gamaray.arex.render3d.*; 
import com.gamaray.arex.format3d.*; 
import com.gamaray.arex.xml.*; 

public class ARXDownload implements Runnable
{
	public static int NONE = 0, GAMADIM = 1, GAMA3D = 2, OBJ = 3, PNG = 4, JPG = 4;
	
	private boolean stop = false, pause = false, action = false;
	public static int NOT_STARTED = 0, WAITING = 1, WORKING = 2, PAUSED = 3, STOPPED = 4;
	private int state = NOT_STARTED;
	
	private int id = 0;
	private HashMap workingList = new HashMap();
	private HashMap completedList = new HashMap();
	ARXHttpInputStream is;
	
	private String activeJobId = null;
	
	private HashMap cache = new HashMap();
	
	ARXContext ctx;
	
	public ARXDownload(ARXContext ctx)
	{
		this.ctx = ctx;
	}
	
	public void run()
	{
		String jobId;
		DownloadJobRequest request;
		DownloadJobResult result;
			
		stop = false;
		pause = false;
		action = false;
		state = WAITING;
		
		while (!stop)
		{	
			jobId = null;
			request = null;
			result = null;
			
			// Wait for action
			while (!stop && !pause)
			{
				synchronized (this)
				{
					if (workingList.size() > 0)
					{
						jobId = getNextRequestId();
						request = (DownloadJobRequest) workingList.get(jobId);
						action = true;
					}
				}
				
				// Do action
				if (action)
				{
					state = WORKING;
					activeJobId = jobId;
					result = processRequest(request);
							
					synchronized (this)
					{
						workingList.remove(jobId);
						
						if (!ARXHttpInputStream.killMode)
						{
							completedList.put(jobId, result);
						}
						else
						{
							workingList.put(jobId, request);	
						}
						
						action = false;
					}
				}
				state = WAITING;
				
				if (!stop && !pause) sleep(100);
			}
			
			// Do pause
			while (!stop && pause)
			{
				state = PAUSED;
				sleep(100);
			}
			state = WAITING;
		}
		
		// Do stop
		state = STOPPED;
		//clearLists();
	}
	
	private void sleep(long ms)
	{
		try
		{
			Thread.sleep(ms);
		}
		catch (java.lang.InterruptedException ex)
		{
			
		}
	}
	
	private String getNextRequestId()
	{
		return (String) workingList.keySet().iterator().next();
	}
	
	boolean emptyCache = false;
	public void emptyCache()
	{
		emptyCache = true;
	}
	
	private DownloadJobResult processRequest(DownloadJobRequest request)
	{
		DownloadJobResult result = new DownloadJobResult();
		
		try
		{
			if (emptyCache)
			{
				cache.clear();
				emptyCache = false;
			}
			
			if (request.cacheable && cache.containsKey(request.url))
			{
				return (DownloadJobResult) cache.get(request.url);
			}
			
			if (request.format == GAMA3D)
			{
				is = ctx.getHttpGETInputStream(request.url);
				
				GAMA3DHandler handler = new GAMA3DHandler();
				handler.load(is, null);
				handler.getMesh().calc();
			
				result.format = request.format;
				result.obj = handler.getMesh();
				result.error = false;
				result.errorMsg = null;
			}
			else if (request.format == PNG || request.format == JPG)
			{
				is = ctx.getHttpGETInputStream(request.url);
				
				result.format = request.format;
				result.obj = ctx.createBitmap(is);
				result.error = false;
				result.errorMsg = null;
			} 
			else if (request.format == GAMADIM)
			{
				is = ctx.getHttpPOSTInputStream(request.url, request.params);
				
				Element root = ctx.parseXML(is);
				
				Layer layer = new Layer();
				layer.load(root);
				
				result.format = request.format;
				result.obj = layer;
				result.error = false;
				result.errorMsg = null;
			}
			
			ctx.returnHttpInputStream(is);
			is = null;
			
			if (request.cacheable) cache.put(request.url, result);
		}
		catch (Exception ex)
		{
			result.format = NONE;
			result.obj = null;
			result.error = true;
			result.errorMsg = ex.getMessage();
			result.errorRequest = request;
				
			try { ctx.returnHttpInputStream(is); } catch (Exception ignore) { }
			
			ex.printStackTrace();
		}
		
		activeJobId = null;
		
		return result;
	}
	
	public synchronized void clearLists()
	{
		workingList.clear();
		completedList.clear();
	}
	
	public synchronized String submitJob(DownloadJobRequest job)
	{
		String jobId = "ID_" + (id++);
		workingList.put(jobId, job);
		
		return jobId;
	}
	
	public synchronized boolean isJobComplete(String jobId)
	{
		return completedList.containsKey(jobId);
	}
	
	public synchronized DownloadJobResult getJobResult(String jobId)
	{
		DownloadJobResult result = (DownloadJobResult) completedList.get(jobId);
		completedList.remove(jobId);
		
		return result;
	}
	
	public String getActiveJobId()
	{
		return activeJobId;	
	}
	
	public float getActiveJobPctComplete()
	{
		return (is != null) ? is.pctDownloadComplete() : 0;
	}
	
	public void pause()
	{
		pause = true;
	}
	
	public void restart()
	{
		pause = false;
	}
	
	public void stop()
	{
		stop = true;
	}
	
	public int getState()
	{
		return state;
	}
	
	public String getStateString()
	{
		if (state == NOT_STARTED)
			return "NOT_STARTED";
		else if (state == WAITING)
			return "WAITING";
		else if (state == WORKING)
			return "WORKING";
		else if (state == PAUSED)
			return "PAUSED";
		else if (state == STOPPED)
			return "STOPPED";
		else 
			return "ERROR";
	}
}

class DownloadJobRequest
{
	int format;
	String url;
	String params;
	boolean cacheable;
}

class DownloadJobResult
{
	int format;
	Object obj;
	
	boolean error;
	String errorMsg;
	DownloadJobRequest errorRequest;
}