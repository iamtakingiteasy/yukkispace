package org.eientei.yukkispace.server.intercomm;

import org.eientei.yukkispace.protocol.world.SceneStruct;
import org.msgpack.annotation.Message;

import java.util.HashMap;
import java.util.Map;

/**
 * User: iamtakingiteasy
 * Date: 2013-08-20
 * Time: 19:00
 */
@Message
public class ScenePackStruct {
    public Map<String, SceneStruct> userScenes = new HashMap<String, SceneStruct>();
}
