package org.eientei.yukkispace.protocol.world;

import org.msgpack.annotation.Message;

import java.util.HashMap;
import java.util.Map;

/**
 * User: iamtakingiteasy
 * Date: 2013-08-19
 * Time: 14:01
 */
@Message
public class EntityStruct {
    public float[] model = new float[16];
    public Map<String, Float> uniforms = new HashMap<String, Float>();
    public String  modelname = "error";
}
