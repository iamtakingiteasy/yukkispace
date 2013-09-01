package org.eientei.yukkispace.protocol.input;

import org.msgpack.annotation.Message;

/**
 * User: iamtakingiteasy
 * Date: 2013-08-19
 * Time: 12:33
 */
@Message
public class KeyboardKeyStruct {
    public byte state = KeyboardState.KEYSTATE_NONE;
    public byte key = KeyboardKeys.KEY_NONE;

    public KeyboardKeyStruct(byte state, byte key) {
        this.state = state;
        this.key = key;
    }

    public KeyboardKeyStruct() {

    }
}
