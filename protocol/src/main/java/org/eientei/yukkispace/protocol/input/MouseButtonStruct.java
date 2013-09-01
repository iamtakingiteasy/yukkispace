package org.eientei.yukkispace.protocol.input;


import org.msgpack.annotation.Message;

/**
 * User: iamtakingiteasy
 * Date: 2013-08-19
 * Time: 12:51
 */
@Message
public class MouseButtonStruct {
    public byte state = MouseState.MOUSESTATE_NONE;
    public byte button = MouseButtons.MOUSE_NONE;

    public MouseButtonStruct(byte state, byte button) {
        this.state = state;
        this.button = button;
    }

    public MouseButtonStruct() {

    }
}
