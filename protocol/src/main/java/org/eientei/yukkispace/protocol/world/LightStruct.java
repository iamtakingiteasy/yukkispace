package org.eientei.yukkispace.protocol.world;

import org.msgpack.annotation.Message;

/**
 * User: iamtakingiteasy
 * Date: 2013-08-19
 * Time: 17:30
 */
@Message
public class LightStruct {
    public float[] modelTrans = new float[16];
    public float[] modelRot = new float[16];
    public float[] view = new float[16];
    public float[] projection = new float[16];
    public float[] intensity = new float[3];
    public float[] ambient = new float[3];
    public float[] specular = new float[3];
    public float[] diffuse = new float[3];
    public float[] direction = new float[3];
    public float spotcut;
    public float spotexp;
    public float fadeconstant;
    public float fadelinear;
    public float fadequadratic;
    public float[] position = new float[4];
}
