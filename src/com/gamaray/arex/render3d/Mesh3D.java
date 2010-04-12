package com.gamaray.arex.render3d;

import java.util.ArrayList;

public class Mesh3D 
{
	public float vertices[];
	public short faces[];
	public int colors[];
	public short faceColors[];
	
	public float planeNormals[];
	public Vector3D t1, t2, t3, t4;
    public Vector3D b1, b2, b3, b4;
    public ArrayList boundingPoints;
	public Vector3D center;
	public float boundingRadius;
	
	public void calc()
	{
		// Bounds
		float maxX = Float.MIN_VALUE, maxY = Float.MIN_VALUE, maxZ = Float.MIN_VALUE;
        float minX = Float.MAX_VALUE, minY = Float.MAX_VALUE, minZ = Float.MAX_VALUE;
		
		for (int vIdx = 0; vIdx < vertices.length; vIdx+=3)
		{
			float x = vertices[vIdx+0];
			float y = vertices[vIdx+1];
			float z = vertices[vIdx+2];
			
			maxX = Math.max(maxX, x);
			maxY = Math.max(maxY, y);
			maxZ = Math.max(maxZ, z);
			minX = Math.min(minX, x);
			minY = Math.min(minY, y);
			minZ = Math.min(minZ, z);
		}
		
		t1 = new Vector3D(minX, maxY, minZ);
        t2 = new Vector3D(maxX, maxY, minZ);
        t3 = new Vector3D(maxX, maxY, maxZ);
        t4 = new Vector3D(minX, maxY, maxZ);
    	b1 = new Vector3D(minX, minY, minZ);
        b2 = new Vector3D(maxX, minY, minZ);
        b3 = new Vector3D(maxX, minY, maxZ);
        b4 = new Vector3D(minX, minY, maxZ); 
        	
        boundingPoints = new ArrayList();
        boundingPoints.add(t1);
        boundingPoints.add(t2);
        boundingPoints.add(t3);
        boundingPoints.add(t4);
        boundingPoints.add(b1);
        boundingPoints.add(b2);
        boundingPoints.add(b3);
        boundingPoints.add(b4);
		
		center = new Vector3D();
		center.x = (minX + maxX) / 2f;
		center.y = (minY + maxY) / 2f;
		center.z = (minZ + maxZ) / 2f;
		
		boundingRadius = Math.max(Math.max(maxX - minX, maxY - minY), maxZ - minZ) / 2f;
		
		// Plane normals
		Vector3D v1 = new Vector3D();
		Vector3D v2 = new Vector3D();
		Vector3D v3 = new Vector3D();
		Vector3D temp1 = new Vector3D();
		Vector3D temp2 = new Vector3D();
		Vector3D normal = new Vector3D();
		
		planeNormals = new float[faces.length*3];
		
		for (int fIdx = 0; fIdx < faces.length; fIdx+=3)
		{
			short v1Idx = faces[fIdx+0];
			short v2Idx = faces[fIdx+1];
			short v3Idx = faces[fIdx+2];
			
			int v1IdxInt = v1Idx * 3; 
			float v1X = vertices[v1IdxInt+0];
			float v1Y = vertices[v1IdxInt+1];
			float v1Z = vertices[v1IdxInt+2];
			
			int v2IdxInt = v2Idx * 3; 
			float v2X = vertices[v2IdxInt+0];
			float v2Y = vertices[v2IdxInt+1];
			float v2Z = vertices[v2IdxInt+2];
			
			int v3IdxInt = v3Idx * 3; 
			float v3X = vertices[v3IdxInt+0];
			float v3Y = vertices[v3IdxInt+1];
			float v3Z = vertices[v3IdxInt+2];
			
			v1.setTo(v1X, v1Y, v1Z);
			v2.setTo(v2X, v2Y, v2Z);
			v3.setTo(v3X, v3Y, v3Z);
			
			temp1.setTo(v3);
	        temp1.subtract(v2);
	        temp2.setTo(v1);
	        temp2.subtract(v2);
	        normal.setToCrossProduct(temp1, temp2);
	        normal.normalize();
			
			int pnIdx = fIdx;
			planeNormals[pnIdx+0] = normal.x;
			planeNormals[pnIdx+1] = normal.y;
			planeNormals[pnIdx+2] = normal.z;
		}
	}
}
