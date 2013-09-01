package org.eientei.yukkispace.protocol.input;

import org.msgpack.annotation.Message;

/**
 * User: iamtakingiteasy
 * Date: 2013-08-19
 * Time: 12:40
 */
@Message
public class MouseAxisStruct {
    public byte axis  = MouseAxis.MOUSE_AXIS_NONE;
    public short value = 0;

    public MouseAxisStruct(byte axis, short value) {
        this.axis = axis;
        this.value = value;
    }

    public MouseAxisStruct() {

    }
}
