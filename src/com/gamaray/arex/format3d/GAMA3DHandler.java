package com.gamaray.arex.format3d;

import java.io.InputStream;
import java.io.OutputStream;

import java.io.DataOutputStream;
import java.io.DataInputStream;

import com.gamaray.arex.render3d.*;

public class GAMA3DHandler implements MeshFormatHandler
{
	Mesh3D mesh;
	
	public void load(InputStream is, SimpleVFS vfs) throws Exception
	{
		DataInputStream dis = new DataInputStream(is);
		
		int version = dis.readInt();
		int vertexCount = dis.readInt();
		int faceCount = dis.readInt();
		int colorCount = dis.readInt();
		int faceColorCount = dis.readInt();
		
		if (version != 1) throw new Exception("Unsuported GAMA3D version");
		
		mesh = new Mesh3D();
		
		mesh.vertices = new float[vertexCount * 3];
		for (int i = 0; i < mesh.vertices.length; i++) mesh.vertices[i] = dis.readFloat();
		
		mesh.faces = new short[faceCount * 3];
		for (int i = 0; i < mesh.faces.length; i++) mesh.faces[i] = dis.readShort();
		
		mesh.colors = new int[colorCount];
		for (int i = 0; i < mesh.colors.length; i++) mesh.colors[i] = dis.readInt();
		
		mesh.faceColors = new short[faceColorCount];
		for (int i = 0; i < mesh.faceColors.length; i++) mesh.faceColors[i] = dis.readShort();
	}
	
	public void write(OutputStream os, SimpleVFS vfs) throws Exception
	{
		DataOutputStream dos = new DataOutputStream(os);
		
		int version = 1;
		int vertexCount = mesh.vertices.length / 3;
		int faceCount = mesh.faces.length / 3;
		int colorCount = mesh.colors.length;
		int faceColorCount = mesh.faceColors.length;
		
		dos.writeInt(version);
		dos.writeInt(vertexCount);
		dos.writeInt(faceCount);
		dos.writeInt(colorCount);
		dos.writeInt(faceColorCount);
		
		for (int i = 0; i < mesh.vertices.length; i++) dos.writeFloat(mesh.vertices[i]);
		
		for (int i = 0; i < mesh.faces.length; i++) dos.writeShort(mesh.faces[i]);
		
		for (int i = 0; i < mesh.colors.length; i++) dos.writeInt(mesh.colors[i]);
		
		for (int i = 0; i < mesh.faceColors.length; i++) dos.writeShort(mesh.faceColors[i]);
	}
	
	public Mesh3D getMesh()
	{
		return mesh;
	}
	
	public void setMesh(Mesh3D newMesh)
	{
		mesh = newMesh;
	}
}
