package org.eientei.yukkispace.player.utils;

import org.eientei.yukkispace.player.data.Entity;
import org.eientei.yukkispace.player.data.MaterialData;
import org.eientei.yukkispace.player.data.VertexData;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL15;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.*;

/**
 * User: iamtakingiteasy
 * Date: 2013-08-04
 * Time: 08:58
 */
public class OBJUtils {
    public static void loadObj(File file, Entity target) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;
        List<Vector4f> geometry = new ArrayList<Vector4f>();
        List<Vector3f> normals = new ArrayList<Vector3f>();
        List<Vector2f> textures = new ArrayList<Vector2f>();
        List<VertexData> vertices = new ArrayList<VertexData>();

        Map<String, Integer> indicesMap = new HashMap<String, Integer>();
        List<Integer> indices = new ArrayList<Integer>();

        Map<String, Float> materialIndexMap = new HashMap<String, Float>();
        List<MaterialData> materials = new ArrayList<MaterialData>();

        MaterialData md = new MaterialData();
        md.setAmbient(1,1,1).setDiffuse(1,1,1).setSpecular(1,1,1).setSpecularCoeff(85);
        materialIndexMap.put("", (float)materials.size());
        materials.add(md);

        Float matId = 0.0f;

        while ((line = reader.readLine()) != null) {
            line = line.trim();
            String[] parts = line.split(" +");
            if (parts[0].equals("#")) {
                continue;
            } else if (parts[0].equalsIgnoreCase("mtllib")) {
                if (parts.length == 1) {
                    continue;
                }
                String mtllibFilename = parts[1];
                MTLUtils.loadMtl(new File(file.getParentFile(), mtllibFilename), materialIndexMap, materials);
            } else if (parts[0].equalsIgnoreCase("usemtl")) {
                matId = materialIndexMap.get(parts[1]);
            } else if (parts[0].equalsIgnoreCase("v")) {
                float x = 0;
                float y = 0;
                float z = 0;
                float w = 1;
                if (parts.length > 4) {
                    w = Float.parseFloat(parts[4]);
                }
                if (parts.length > 3) {
                    z = Float.parseFloat(parts[3]);
                }
                if (parts.length > 2) {
                    y = Float.parseFloat(parts[2]);
                }
                if (parts.length > 1) {
                    x = Float.parseFloat(parts[1]);
                }
                geometry.add(new Vector4f(x, y, z, w));
            } else if (parts[0].equalsIgnoreCase("vt")) {
                float s = 0;
                float t = 0;
                if (parts.length > 2) {
                    t = Float.parseFloat(parts[2]);
                }
                if (parts.length > 1) {
                    s = Float.parseFloat(parts[1]);
                }
                textures.add(new Vector2f(s,t));
            } else if (parts[0].equalsIgnoreCase("vn")) {
                float x = 0;
                float y = 0;
                float z = 0;
                if (parts.length > 3) {
                    z = Float.parseFloat(parts[3]);
                }
                if (parts.length > 2) {
                    y = Float.parseFloat(parts[2]);
                }
                if (parts.length > 1) {
                    x = Float.parseFloat(parts[1]);
                }
                Vector3f n = new Vector3f(x, y, z);
                //n.normalise();
                normals.add(n);
            } else if (parts[0].equalsIgnoreCase("vp")) {
                // unsupported yet
            } else if (parts[0].equalsIgnoreCase("f")) {
                if (parts.length == 4) {
                    for (int i = 1; i < 4; i++) {
                        Integer idx = indicesMap.get(parts[i]);
                        if (idx != null) {
                            indices.add(idx);
                            continue;
                        }
                        String[] p = parts[i].split("/");

                        int g = 0;
                        int t = 0;
                        int n = 0;

                        if (p.length > 2 && !p[2].isEmpty()) {
                            n = Integer.parseInt(p[2]);
                        }
                        if (p.length > 1 && !p[1].isEmpty()) {
                            t = Integer.parseInt(p[1]);
                        }
                        if (p.length > 0 && !p[0].isEmpty()) {
                            g = Integer.parseInt(p[0]);
                        }

                        VertexData vertexData = new VertexData();
                        if (g > 0) {
                            Vector4f gv = geometry.get(g-1);
                            vertexData.setXYZW(gv.getX(), gv.getY(), gv.getZ(), gv.getW());
                        }
                        if (t > 0) {
                            Vector2f tv = textures.get(t-1);
                            vertexData.setTexture(tv.getX(), tv.getY());
                        }
                        if (n > 0) {
                            Vector3f nv = normals.get(n-1);
                            vertexData.setNorm(nv.getX(), nv.getY(), nv.getZ());
                        }

                        vertexData.setMaterial(matId);

                        vertices.add(vertexData);

                        idx = indicesMap.size();
                        indicesMap.put(parts[i], idx);

                        indices.add(idx);
                    }
                }
            } else if (parts[0].equalsIgnoreCase("o")) {
                // unsupported yet
            } else if (parts[0].equalsIgnoreCase("g")) {
                // unsupported yet
            } else if (parts[0].equalsIgnoreCase("s")) {
                // unsupported yet
            }

        }

        FloatBuffer fbuf = BufferUtils.createFloatBuffer(vertices.size() * VertexData.total_count);
        IntBuffer ibuf = BufferUtils.createIntBuffer(indices.size());

        for (VertexData vd : vertices) {
            vd.putIn(fbuf);
        }
        for (Integer id : indices) {
            ibuf.put(id);
        }

        fbuf.flip();
        ibuf.flip();

        int vbo = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, fbuf, GL15.GL_STATIC_DRAW);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

        int vbi = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, vbi);
        GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, ibuf, GL15.GL_STATIC_DRAW);
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);

        target.setVbo(vbo);
        target.setVbi(vbi);
        target.setVbiSize(indices.size());

        //materials.clear();
        //materials.add(new MaterialData().setAmbient(1,0,0).setDiffuse(0,1,0).setSpecular(0,0,1));


        FloatBuffer mtbuf = BufferUtils.createFloatBuffer(materials.size() * MaterialData.total_count);
        for (MaterialData m : materials) {
            m.putIn(mtbuf);
        }

        mtbuf.flip();

        int materialTexture = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_1D, materialTexture);
        GL11.glTexParameteri(GL11.GL_TEXTURE_1D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_1D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_1D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexImage1D(GL11.GL_TEXTURE_1D, 0, GL11.GL_RGBA, materials.size() * 4, 0, GL11.GL_RGBA, GL11.GL_FLOAT, mtbuf);
        GL11.glBindTexture(GL11.GL_TEXTURE_1D, 0);

        target.setMaterialTexture(materialTexture);
        target.setMaterialTextureSize(materials.size());
    }
}
