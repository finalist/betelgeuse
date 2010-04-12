package com.gamaray.arex.render3d;

public class Util3D {
    public static void lightMesh(Mesh3D mesh) {
        float r, g, b;
        int color;
        Vector3D light1 = new Vector3D();
        Vector3D light2 = new Vector3D();

        Vector3D v1 = new Vector3D();
        Vector3D v2 = new Vector3D();
        Vector3D v3 = new Vector3D();
        Vector3D normal = new Vector3D();
        Vector3D centroid = new Vector3D();

        int newColors[] = new int[mesh.faces.length / 3];

        for (int fIdx = 0; fIdx < mesh.faces.length; fIdx += 3) {
            short v1Idx = mesh.faces[fIdx + 0];
            short v2Idx = mesh.faces[fIdx + 1];
            short v3Idx = mesh.faces[fIdx + 2];
            short cIdx = mesh.faceColors[fIdx / 3];

            int baseColor = mesh.colors[cIdx];

            int v1IdxInt = v1Idx * 3;
            float v1X = mesh.vertices[v1IdxInt + 0];
            float v1Y = mesh.vertices[v1IdxInt + 1];
            float v1Z = mesh.vertices[v1IdxInt + 2];

            int v2IdxInt = v2Idx * 3;
            float v2X = mesh.vertices[v2IdxInt + 0];
            float v2Y = mesh.vertices[v2IdxInt + 1];
            float v2Z = mesh.vertices[v2IdxInt + 2];

            int v3IdxInt = v3Idx * 3;
            float v3X = mesh.vertices[v3IdxInt + 0];
            float v3Y = mesh.vertices[v3IdxInt + 1];
            float v3Z = mesh.vertices[v3IdxInt + 2];

            int pnIdx = fIdx;
            float pnX = mesh.planeNormals[pnIdx + 0];
            float pnY = mesh.planeNormals[pnIdx + 1];
            float pnZ = mesh.planeNormals[pnIdx + 2];

            v1.setTo(v1X, v1Y, v1Z);
            v2.setTo(v2X, v2Y, v2Z);
            v3.setTo(v3X, v3Y, v3Z);

            normal.setTo(pnX, pnY, pnZ);

            centroid.add(v1);
            centroid.add(v2);
            centroid.add(v3);
            centroid.divide(3f);

            light1.setTo(mesh.t1);
            light1.multiply(3f);
            light1.subtract(centroid);
            light1.normalize();

            light2.setTo(mesh.b3);
            light2.multiply(3f);
            light2.subtract(centroid);
            light2.normalize();

            float li1 = normal.getDotProduct(light1);
            float li2 = normal.getDotProduct(light2);

            float li = (2f + li1 + li2) / 4f;
            li = 0.25f + li * 0.75f;

            r = ((float) Color.red(baseColor)) * li;
            g = ((float) Color.green(baseColor)) * li;
            b = ((float) Color.blue(baseColor)) * li;

            color = Color.argb(255, (int) r, (int) g, (int) b);

            mesh.faceColors[fIdx / 3] = (short) (fIdx / 3);
            newColors[fIdx / 3] = color;
        }

        mesh.colors = newColors;
    }
}
