package org.eientei.yukkispace.player.data;

import java.nio.FloatBuffer;

/**
 * User: iamtakingiteasy
 * Date: 2013-08-02
 * Time: 23:50
 */
public class VertexData {
    private float[] xyzw    = new float[] { 0f, 0f, 0f, 1f };
    private float[] norm    = new float[] { 0f, 0f, 0f };
    private float[] texture = new float[] { 0f, 0f };
    private float material = 0;

    private final static int bytesPerFloat = 4;

    public final static int xyzw_count = 4;
    public final static int norm_count = 3;
    public final static int texture_count = 2;
    public final static int material_count = 1;

    public final static int xyzw_size = xyzw_count * bytesPerFloat;
    public final static int norm_size = norm_count * bytesPerFloat;
    public final static int texture_size   = texture_count * bytesPerFloat;
    public final static int material_size   = material_count * bytesPerFloat;

    public final static int xyzw_offset = 0;
    public final static int norm_offset = xyzw_offset  + xyzw_size;
    public final static int texture_offset = norm_offset + norm_size;
    public final static int material_offset = texture_offset + texture_size;

    public final static int total_count = xyzw_count + norm_count + texture_count + material_count;
    public final static int total_size  = xyzw_size + norm_size + texture_size + material_size;

    public VertexData() {
    }

    public VertexData(float x, float y, float z, float w) {
        setXYZW(x,y,z,w);
    }

    public VertexData(float x, float y, float z) {
        setXYZ(x, y, z);
    }

    public VertexData setXYZW(float x, float y, float z, float w) {
        xyzw = new float[] { x, y, z, w };
        return this;
    }

    public VertexData setTexture(float s, float t) {
        texture = new float[] { s, t };
        return this;
    }

    public VertexData setXYZ(float x, float y, float z) {
        return setXYZW(x, y, z, 1f);
    }

    public VertexData setNorm(float x, float y, float z) {
        norm = new float[] { x, y, z };
        return this;
    }

    public VertexData setMaterial(float id) {
        material = id;
        return this;
    }

    public float[] getXYZW() {
        return xyzw;
    }

    public float[] getTexture() {
        return texture;
    }

    public float[] getXYZ() {
        return new float[] { xyzw[0], xyzw[1], xyzw[2] };
    }

    public float[] getNorm() {
        return norm;
    }

    public float getMaterial() {
        return material;
    }

    public float[] getArray() {
        float out[] = new float[total_count];
        int i = 0;
        out[i++] = xyzw[0];
        out[i++] = xyzw[1];
        out[i++] = xyzw[2];
        out[i++] = xyzw[3];

        out[i++] = norm[0];
        out[i++] = norm[1];
        out[i++] = norm[2];

        out[i++] = texture[0];
        out[i++] = texture[1];

        out[i++] = material;

        return out;
    }

    public void putIn(FloatBuffer fb) {
        fb.put(getArray());
    }
}
