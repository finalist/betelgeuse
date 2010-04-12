package com.gamaray.arex;

import java.util.HashMap;
import java.util.ArrayList;

import com.gamaray.arex.render3d.*;

public class ARXRender implements Runnable
{
	private boolean stop = false, pause = false, action = false;
	public static int NOT_STARTED = 0, WAITING = 1, WORKING = 2, PAUSED = 3, STOPPED = 4;
	private int state = NOT_STARTED;
	
	private static int MAX_CAM_WIDTH = 256, MAX_CAM_HEIGHT = 256;
	
	int id = 0;
	HashMap workingList = new HashMap();
	HashMap completedList = new HashMap();
	
	ArrayList camPool = new ArrayList();
	
	Vector3D tmp1 = new Vector3D();
	Vector3D tmp2 = new Vector3D();
	Vector3D worldUp = new Vector3D(0,1,0);
	Vector3D unitVector = new Vector3D(1,0,0);
	Vector3D signPostMark = new Vector3D();
	Vector3D centerMark = new Vector3D();
	Vector3D bptMark = new Vector3D();
	
	Camera sizingCam = new Camera(0,0,false);
	Vector3D sizingCenterMark = new Vector3D();
	
	ARXContext ctx;
	
	public ARXRender(ARXContext ctx)
	{
		this.ctx = ctx;
	}
	
	public void run()
	{
		String jobId;
		RenderJobRequest request;
		RenderJobResult result;
			
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
						request = (RenderJobRequest) workingList.get(jobId);
						action = true;
					}
				}
				
				// Do action
				if (action)
				{
					state = WORKING;
					result = processRequest(request);
							
					synchronized (this)
					{
						workingList.remove(jobId);
						completedList.put(jobId, result);
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
	
	private RenderJobResult processRequest(RenderJobRequest request)
	{
		RenderJobResult result = new RenderJobResult();
		
		try
		{
			Camera sizingCam = new Camera(0,0,false);
			sizingCam.setViewAngle(request.screenWidth, request.screenHeight, request.viewAngle);
			sizingCam.location.setTo(request.camLocation);
			sizingCam.transform.setTo(request.camTransform);
			
			// Determine bounds
			ArrayList bpts = request.obj.mesh.boundingPoints;
			float maxX = Float.NEGATIVE_INFINITY, minX = Float.POSITIVE_INFINITY;
			float maxY = Float.NEGATIVE_INFINITY, minY = Float.POSITIVE_INFINITY;
			for (int i = 0; i < bpts.size(); i++)
			{
				Vector3D bpt = (Vector3D) bpts.get(i);
				
				tmp1.setTo(bpt);
				tmp1.transform(request.obj.transform);
				tmp1.add(request.obj.location);
				tmp1.subtract(sizingCam.location);
				tmp1.transform(sizingCam.transform);
				sizingCam.projectPoint(tmp1, tmp2);
				bptMark.setTo(tmp2);
				
				if (bptMark.x < minX) minX = bptMark.x;
				if (bptMark.x > maxX) maxX = bptMark.x;
				if (bptMark.y < minY) minY = bptMark.y;
				if (bptMark.y > maxY) maxY = bptMark.y;
			}
			tmp1.setTo(request.obj.mesh.center);
			tmp1.transform(request.obj.transform);
			tmp1.add(request.obj.location);
			tmp1.subtract(sizingCam.location);
			tmp1.transform(sizingCam.transform);
			sizingCam.projectPoint(tmp1, tmp2);
			sizingCenterMark.setTo(tmp2);
			
			// Size new camera from bounds
			float bx = minX, by = minY, bw = (maxX-minX), bh = (maxY-minY);
			float newCamWidth = Math.max(sizingCenterMark.x - minX, maxX - sizingCenterMark.x) * 2 + 1;
			float newCamHeight = Math.max(sizingCenterMark.y - minY, maxY - sizingCenterMark.y) * 2 + 1;
			boolean camMaxed = false;
			int scaleFactor = 1;
			if (newCamWidth > MAX_CAM_WIDTH)
			{
				newCamWidth = MAX_CAM_WIDTH;
				camMaxed = true;
			}
			if (newCamHeight > MAX_CAM_HEIGHT)
			{
				newCamHeight = MAX_CAM_HEIGHT;
				camMaxed = true;
			}
			if (camMaxed)
			{
				scaleFactor = 2;
			}
			
			// Create new camera
			Camera cam = new Camera((int) newCamWidth, (int) newCamHeight);
			cam.setViewAngle(request.screenWidth / scaleFactor, request.screenHeight / scaleFactor, request.viewAngle);
			cam.location.setTo(request.camLocation);
			cam.transform.setTo(request.camTransform);
			cam.clearBuffers();
			cam.render(request.obj);
			
			// Extract scale factor
			tmp1.setTo(unitVector);
			tmp1.transform(request.obj.transform);
			float scale = tmp1.length();
			
			// TODO: handle case when looking straight up
			tmp1.setTo(request.obj.mesh.center);
			tmp1.transform(request.obj.transform);
			tmp1.add(request.obj.location);
			tmp2.setToCrossProduct(tmp1, worldUp);
			tmp2.normalize();
			tmp2.multiply(request.obj.mesh.boundingRadius * scale);
			tmp2.add(request.obj.mesh.center);
			
			result.signPost = new Vector3D(tmp2);
			result.center = new Vector3D(request.obj.mesh.center);
			
			// Determine center mark
			tmp1.setTo(result.center);
			tmp1.transform(request.obj.transform);
			tmp1.add(request.obj.location);
			tmp1.subtract(cam.location);
			tmp1.transform(cam.transform);
			cam.projectPoint(tmp1, tmp2);
			centerMark.setTo(tmp2);
			
			// Determine sign post mark
			tmp1.setTo(result.signPost);
			tmp1.add(request.obj.location);
			tmp1.subtract(cam.location);
			tmp1.transform(cam.transform);
			cam.projectPoint(tmp1, tmp2);
			signPostMark.setTo(tmp2);
			
			result.renderLocation = new Vector3D(request.obj.location);
			result.centerX = centerMark.x;
			result.centerY = centerMark.y;
			result.signPostX = signPostMark.x;
			result.signPostY = signPostMark.y;
			result.scaleFactor = (float) scaleFactor;
			result.cam = cam;
			result.error = false;
			result.errorMsg = null;
		}
		catch (Exception ex)
		{
			result.cam = null;
			result.error = true;
			result.errorMsg = ex.getMessage();
			
			ex.printStackTrace();
		}
		
		return result;
	}
	
	public synchronized void clearLists()
	{
		workingList.clear();
		completedList.clear();
	}
	
	public synchronized String submitJob(RenderJobRequest job)
	{
		String jobId = "ID_" + (id++);
		workingList.put(jobId, job);
		
		return jobId;
	}
	
	public synchronized boolean isJobComplete(String jobId)
	{
		return completedList.containsKey(jobId);
	}
	
	public synchronized RenderJobResult getJobResult(String jobId)
	{
		RenderJobResult result = (RenderJobResult) completedList.get(jobId);
		completedList.remove(jobId);
		
		return result;
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

class RenderJobRequest
{
	public Matrix3D camTransform;
	public Vector3D camLocation;
	public int screenWidth, screenHeight;
	public float viewAngle;
	public Object3D obj;
}

class RenderJobResult
{
	public Camera cam;
	public Vector3D renderLocation;
	public Vector3D center;
	public Vector3D signPost;
	public float signPostX, signPostY;
	public float centerX, centerY;
	public float scaleFactor;
	
	public boolean error;
	public String errorMsg;
}
