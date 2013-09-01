package org.eientei.yukkispace.server.world.data;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.linear.RealMatrix;

/**
 * User: iamtakingiteasy
 * Date: 2013-08-21
 * Time: 00:32
 */
public class Light extends Object3D {
    private Vector3D direction = new Vector3D(0, 1, 0);
    private Vector3D ambient = new Vector3D(1, 1, 1);
    private Vector3D diffuse = new Vector3D(1, 1, 1);
    private Vector3D specular = new Vector3D(1, 1, 1);
    private Vector3D intensity = new Vector3D(1.0f , 1.0f, 1.0f);
    private float fadeConstant = 1.0f;
    private float fadeLinear = 0.0f;
    private float fadeQuadratic = 0.0f;
    private float aspect = 1.0f;
    private float fov = 60.0f;
    private float near = 0.1f;
    private float far = 100.0f;
    private float spotCut = 60.0f;
    private float spotExp = 60.0f;

    public Light(String name) {
        super(name);
    }

    public Vector3D getIntensity() {
        return intensity;
    }

    public void setIntensity(float i) {
        this.intensity = new Vector3D(i ,i, i);
    }

    public void setIntensity(Vector3D intensity) {
        this.intensity = intensity;
    }

    public Vector3D getAmbient() {
        return ambient;
    }

    public void setAmbient(Vector3D ambient) {
        this.ambient = ambient;
    }

    public Vector3D getDirection() {
        return direction;
    }

    public void setDirection(Vector3D direction) {
        this.direction = direction;
    }

    public Vector3D getDiffuse() {
        return diffuse;
    }

    public void setDiffuse(Vector3D diffuse) {
        this.diffuse = diffuse;
    }

    public float getFadeConstant() {
        return fadeConstant;
    }

    public void setFadeConstant(float fadeConstant) {
        this.fadeConstant = fadeConstant;
    }

    public float getFadeLinear() {
        return fadeLinear;
    }

    public void setFadeLinear(float fadeLinear) {
        this.fadeLinear = fadeLinear;
    }

    public float getFadeQuadratic() {
        return fadeQuadratic;
    }

    public void setFadeQuadratic(float fadeQuadratic) {
        this.fadeQuadratic = fadeQuadratic;
    }

    public float getAspect() {
        return aspect;
    }

    public void setAspect(float aspect) {
        this.aspect = aspect;
    }

    public float getFov() {
        return fov;
    }

    public void setFov(float fov) {
        this.fov = fov;
    }

    public float getNear() {
        return near;
    }

    public void setNear(float near) {
        this.near = near;
    }

    public float getFar() {
        return far;
    }

    public void setFar(float far) {
        this.far = far;
    }

    public RealMatrix calculatePerspective() {
        return calculatePerspective(aspect, fov, near, far);
    }

    public Vector3D getSpecular() {
        return specular;
    }

    public void setSpecular(Vector3D specular) {
        this.specular = specular;
    }

    public float getSpotCut() {
        return spotCut;
    }

    public void setSpotCut(float spotCut) {
        this.spotCut = spotCut;
    }

    public float getSpotExp() {
        return spotExp;
    }

    public void setSpotExp(float spotExp) {
        this.spotExp = spotExp;
    }
}

