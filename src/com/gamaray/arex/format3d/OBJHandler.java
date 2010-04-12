package com.gamaray.arex.format3d;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.util.ArrayList;
import java.util.HashMap;

import com.gamaray.arex.render3d.*;

public class OBJHandler implements MeshFormatHandler
{
	Mesh3D mesh;
	
	String mtlFolder, mtlName;
	public ArrayList vlist = new ArrayList();
	public ArrayList flist = new ArrayList();
	public MTLHandler mtl = new MTLHandler();
	
	public OBJHandler()
	{
		
	}
	
	public void setMtlLibName(String mtlNameInit)
	{
		mtlName = mtlNameInit;
	}
	
	public void load(InputStream is, SimpleVFS vfs) throws Exception
	{
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String str;
        int curMtlIdx = -1;

        while ((str = br.readLine()) != null) 
        {
        	String tokens[] = parse(str);
        	
        	if (tokens.length > 0)
        	{
	        	if (tokens[0].equals("v"))
	        	{
	        		float x = Float.parseFloat(tokens[1]);
	        		float y = Float.parseFloat(tokens[2]);
	        		float z = Float.parseFloat(tokens[3]);
	    			vlist.add(new OBJVertex(x, y, z));
	        	}
	        	else if (tokens[0].equals("f"))
	        	{
	        		int v1 = Integer.parseInt((tokens[1].split("/"))[0]);
	        		int v2 = Integer.parseInt((tokens[2].split("/"))[0]);
	        		int v3 = Integer.parseInt((tokens[3].split("/"))[0]);
	        		
	        		flist.add(new OBJFace(v1, v2, v3, curMtlIdx));
	        	}
	        	else if (tokens[0].equalsIgnoreCase("mtllib"))
	        	{
	        		InputStream mtlis = vfs.getInputStream(tokens[1].trim());
	        		mtl.load(mtlis);
	        		vfs.returnInputStream(mtlis);
	        	}
	        	else if (tokens[0].equalsIgnoreCase("usemtl"))
	        	{
	        		curMtlIdx = mtl.getMtlIdx(tokens[1].trim());
	        	}
        	}
        }
        
        mesh = new Mesh3D();
        
        mesh.vertices = new float[vlist.size() * 3];
        int vidx = 0;
        for (int i = 0; i < vlist.size(); i++)
        {
        	OBJVertex v = (OBJVertex) vlist.get(i);
        	
        	mesh.vertices[vidx++] = v.x;
        	mesh.vertices[vidx++] = v.y;
        	mesh.vertices[vidx++] = v.z;
        }
        
        mesh.faces = new short[flist.size() * 3];
        mesh.faceColors = new short[flist.size()];
        int fidx = 0;
        for (int i = 0; i < flist.size(); i++)
        {
        	OBJFace f = (OBJFace) flist.get(i);
        	
        	mesh.faces[fidx++] = (short) (f.v1 - 1);
        	mesh.faces[fidx++] = (short) (f.v2 - 1);
        	mesh.faces[fidx++] = (short) (f.v3 - 1);
        	mesh.faceColors[i] = (short) (f.midx);
        }
        
        mesh.colors = new int[mtl.mtlList.size()];
        for (int i = 0; i < mtl.mtlList.size(); i++)
        {
        	String mtlName = (String) mtl.mtlList.get(i);
        	mesh.colors[i] = mtl.getColor(mtlName);
        }
	}
	
	public void write(OutputStream os, SimpleVFS vfs) throws Exception
	{
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os));
		
		bw.write("mtllib " + mtlName + "\n\n");
		
		for (int i = 0; i < mesh.vertices.length; i+=3)
		{
			bw.write("v " + mesh.vertices[i] + " " + mesh.vertices[i+1] + " " + mesh.vertices[i+2] + "\n");
		}
		
		int curMtlIdx = -1;
		for (int i = 0; i < mesh.faces.length; i+=3)
		{
			if (curMtlIdx != mesh.faceColors[i/3])
			{
				curMtlIdx = mesh.faceColors[i/3];
				bw.write("usemtl mtl_" + mesh.faceColors[i/3] + "\n");
			}
			bw.write("f " + (mesh.faces[i] + 1) + " " + (mesh.faces[i+1] + 1) + " " + (mesh.faces[i+2] + 1) + "\n");
		}
		
		bw.flush();
		
		OutputStream mtlos = vfs.getOutputStream(mtlName);
		bw = new BufferedWriter(new OutputStreamWriter(mtlos));
		
		for (int i = 0; i < mesh.colors.length; i++)
		{
			bw.write("newmtl mtl_" + i + "\n");
			bw.write("Kd");
			bw.write(" " + ((float) Color.red(mesh.colors[i]))/255f);
			bw.write(" " + ((float) Color.green(mesh.colors[i]))/255f);
			bw.write(" " + ((float) Color.blue(mesh.colors[i]))/255f);
			bw.write("\n\n");
		}
		
		bw.flush();
		vfs.returnOutputStream(mtlos);
	}
	
	public Mesh3D getMesh()
	{
		return mesh;
	}
	
	public void setMesh(Mesh3D newMesh)
	{
		mesh = newMesh;
	}
	
	static String[] parse(String str)
	{
		ArrayList tokens = new ArrayList();
		StringBuffer buf = new StringBuffer();
		boolean inToken = false;
		
		for (int i = 0; i < str.length(); i++)
		{
			char c = str.charAt(i);
			
			if (c == ' ' || c == '\t')
			{
				if (inToken)
				{
					tokens.add(buf.toString());
					buf = new StringBuffer();
				}
				inToken = false;
			}
			else
			{
				buf.append(c);
				if (!inToken)
				{
					inToken = true;
				}
			}
		}
		if (inToken) tokens.add(buf.toString());
		
		String tokenArray[] = new String[tokens.size()];
		tokens.toArray(tokenArray);
		
		return tokenArray;
	}
}

class OBJVertex
{
	float x, y, z;
	
	public OBJVertex(float xInit, float yInit, float zInit)
	{
		x = xInit;
		y = yInit;
		z = zInit;
	} 
}

class OBJFace
{
	int v1, v2, v3;;
	int midx;
	
	public OBJFace(int v1Init, int v2Init, int v3Init, int midxInit)
	{
		v1 = v1Init;
		v2 = v2Init;
		v3 = v3Init;
		midx = midxInit;
	} 
}

class MTLHandler
{
	ArrayList mtlList = new ArrayList();
	HashMap mtlMap = new HashMap();
	
	public MTLHandler()
	{
		
	}
	
	public void load(InputStream is) throws Exception
	{
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String str, curMtl = null;

        while ((str = br.readLine()) != null) 
        {
        	String tokens[] = OBJHandler.parse(str);
        	
        	if (tokens.length > 0)
        	{
	        	if (tokens[0].equals("newmtl"))
	        	{
	        		curMtl = tokens[1].trim();
	        		mtlList.add(curMtl);
	        	}
	        	else if (tokens[0].equalsIgnoreCase("Kd"))
	        	{
	        		int rd = (int) (255f * Float.parseFloat(tokens[1]));
	        		int gd = (int) (255f * Float.parseFloat(tokens[2]));
	        		int bd = (int) (255f * Float.parseFloat(tokens[3]));
	        		
	        		mtlMap.put(curMtl, new Integer(Color.rgb(rd,gd,bd)));
	        	}
        	}
        }
	}	
		
	public int getColor(String mtlName)
	{
		Integer color = (Integer) mtlMap.get(mtlName);
		
		return color.intValue();
	}	
		
	public int getMtlIdx(String mtlName)
	{
		return mtlList.indexOf(mtlName);
	}
}
