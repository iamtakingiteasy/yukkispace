package org.eientei.yukkispace.protocol.login;

import org.msgpack.annotation.Message;

/**
 * User: iamtakingiteasy
 * Date: 2013-08-20
 * Time: 10:04
 */
@Message
public class LogoutStruct {
    public byte reason;
    public String message;

    public LogoutStruct() {

    }

    public LogoutStruct(byte reason, String message) {
        this.reason = reason;
        this.message = message;
    }
}
