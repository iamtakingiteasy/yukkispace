package org.eientei.yukkispace.protocol.world;

import org.msgpack.annotation.Message;

import java.util.ArrayList;
import java.util.List;

/**
 * User: iamtakingiteasy
 * Date: 2013-08-19
 * Time: 17:36
 */
@Message
public class SceneStruct {
    public List<EntityStruct> entities = new ArrayList<EntityStruct>();
    public List<LightStruct> lights = new ArrayList<LightStruct>();
    public float[] view = new float[16];
}
