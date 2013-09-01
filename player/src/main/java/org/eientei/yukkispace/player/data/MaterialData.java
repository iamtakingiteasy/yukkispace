package org.eientei.yukkispace.player.data;

import java.nio.FloatBuffer;

/**
 * User: iamtakingiteasy
 * Date: 2013-08-09
 * Time: 12:53
 */
public class MaterialData {
    private float[] ambient = { 0.0f, 0.0f, 0.0f };
    private float[] diffuse = { 0.0f, 0.0f, 0.0f };
    private float[] specular = { 0.0f, 0.0f, 0.0f };
    private float specular_coeff = 0.0f;
    private float dissolve = 0.0f;

    public final static int ambient_count = 3;
    public final static int diffuse_count = 3;
    public final static int specular_count = 3;

    public final static int specular_coeff_count = 1;
    public final static int dissolve_count = 1;
    public final static int pad = 1;

    public final static int total_count = 4*4;

    public MaterialData setAmbient(float r, float g, float b) {
        ambient = new float[] { r, g, b };
        return this;
    }

    public MaterialData setDiffuse(float r, float g, float b) {
        diffuse = new float[] { r, g, b };
        return this;
    }

    public MaterialData setSpecular(float r, float g, float b) {
        specular = new float[] { r, g, b };
        return this;
    }

    public MaterialData setSpecularCoeff(float v) {
        specular_coeff = v;
        return this;
    }

    public MaterialData setDissolve(float v) {
        dissolve = v;
        return this;
    }

    public float[] getArray() {
        float out[] = new float[total_count];
        int i = 0;
        out[i++] = ambient[0];
        out[i++] = ambient[1];
        out[i++] = ambient[2];
        out[i++] = 1;

        out[i++] = diffuse[0];
        out[i++] = diffuse[1];
        out[i++] = diffuse[2];
        out[i++] = 1;

        out[i++] = specular[0];
        out[i++] = specular[1];
        out[i++] = specular[2];
        out[i++] = 1;

        out[i++] = specular_coeff;
        out[i++] = dissolve;
        out[i++] = 0;
        out[i++] = 1;

        return out;
    }

    public void putIn(FloatBuffer fb) {
        fb.put(getArray());
    }

    public float getDissolve() {
        return dissolve;
    }

    public float[] getAmbient() {
        return ambient;
    }

    public float[] getDiffuse() {
        return diffuse;
    }

    public float[] getSpecular() {
        return specular;
    }

    public float getSpecularCoeff() {
        return specular_coeff;
    }
}
