package org.eientei.yukkispace.protocol.input;

import org.msgpack.annotation.Message;

import java.util.ArrayList;
import java.util.List;

/**
 * User: iamtakingiteasy
 * Date: 2013-08-19
 * Time: 12:29
 */
@Message
public class InputStruct {
    public List<KeyboardKeyStruct> keys = new ArrayList<KeyboardKeyStruct>();
    public List<MouseAxisStruct> mouseAxis = new ArrayList<MouseAxisStruct>();
    public List<MouseButtonStruct> mouseButtons = new ArrayList<MouseButtonStruct>();
}
