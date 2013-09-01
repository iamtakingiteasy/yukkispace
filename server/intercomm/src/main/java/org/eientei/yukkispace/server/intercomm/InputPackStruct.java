package org.eientei.yukkispace.server.intercomm;

import org.eientei.yukkispace.protocol.input.InputStruct;
import org.msgpack.annotation.Message;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: iamtakingiteasy
 * Date: 2013-08-20
 * Time: 19:08
 */
@Message
public class InputPackStruct {
    public Map<String,InputStruct> inputs = new HashMap<String, InputStruct>();
}
