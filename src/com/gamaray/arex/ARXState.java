package com.gamaray.arex;

import com.gamaray.arex.geo.*;

public class ARXState 
{
	public static int NOT_STARTED = 0; // No result ready, job not submitted
	public static int JOB_SUBMITTED = 1; // Job submitted
	public static int TRANSITION = 2; // Job complete, ready to switch to new result
	public static int READY = 3; // Transition complete, new result ready to use
	public static int ERROR = 4; // Transition complete, new result ready to use
	
	int retryCount = 0;
	
	// Download properties
	int nextLayerStatus = ARXState.NOT_STARTED;
	String downloadJobId;
	DownloadJobResult downloadResult;
	
	public GeoPoint curFix = new GeoPoint();
	public float curBearing, curPitch;
	public float screenWidth, screenHeight;
	String uid;
	
	public boolean launchNeeded = false;
	public String launchUrl = "";
	
	boolean radarVisible = true, messagesVisible = true;
	
	Layer layer = new Layer();
	
	boolean handleEvent(ARXContext ctx, String xmlId, String xmlOnPress)
	{
		boolean evtHandled = false;
		
		if (xmlOnPress.startsWith("refresh"))
		{
			DownloadJobRequest request = new DownloadJobRequest();
			request.format = ARXDownload.GAMADIM;
			request.url = layer.xmlRefreshUrl;
			request.cacheable = false;
			request.params = getParams("refreshOnPress", xmlId);
			downloadJobId = ctx.getARXDownload().submitJob(request);
			
			nextLayerStatus = ARXState.JOB_SUBMITTED;
			evtHandled = true;
		}
		else if (xmlOnPress.startsWith("webpage"))
		{
			try
			{
				// TODO: find a way to pass lat,lon,alt,etc. to web page
				String webpage = ARXUtil.parseAction(xmlOnPress);
				ctx.showWebPage(webpage);
			}
			catch (Exception ex)
			{
				ARXMessages.putMessage("ERROR_WEBPAGE", 
					"Error showing webpage: " + ex, ARXMessages.errorIcon, 3000);
			}
			
			evtHandled = true;
		}
		else if (xmlOnPress.startsWith("dimension"))
		{
			DownloadJobRequest request = new DownloadJobRequest();
			request.format = ARXDownload.GAMADIM;
			request.url = ARXUtil.parseAction(xmlOnPress);
			request.cacheable = false;
			request.params = getParams("init", "NULL");
			downloadJobId = ctx.getARXDownload().submitJob(request);
			
			launchUrl = request.url;
			
			nextLayerStatus = ARXState.JOB_SUBMITTED;
			evtHandled = true;
		}
		
		return evtHandled;
	}
	
	public String getParams(String event, String eventSrc)
	{
		return "event=" + event +
			"&eventSrc=" + eventSrc +
			"&lat=" + curFix.lat +
			"&lon=" + curFix.lon +
			"&alt=" + curFix.alt +
			"&bearing=" + (int) curBearing +
			"&pitch=" + (int) curPitch +
			"&width=" + (int) screenWidth +
			"&height=" + (int) screenHeight +
			"&uid=" + uid +
			"&time=" + System.currentTimeMillis(); 
	}
}
