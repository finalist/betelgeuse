package com.gamaray.arex.render3d;

public class Object3D 
{
	public Mesh3D mesh;
	public Matrix3D transform;
	public Vector3D location;
	
	public Object3D()
	{
		transform = new Matrix3D();
		transform.setToIdentity();
		
		location = new Vector3D();
		location.setTo(0,0,0);
	}
}
