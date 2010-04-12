package com.gamaray.arex.format3d;

import java.io.InputStream;
import java.io.OutputStream;

import com.gamaray.arex.render3d.*;

public interface MeshFormatHandler
{
	public void load(InputStream is, SimpleVFS vfs) throws Exception;
	public void write(OutputStream os, SimpleVFS vfs) throws Exception;
	public Mesh3D getMesh();
	public void setMesh(Mesh3D mesh);
}
