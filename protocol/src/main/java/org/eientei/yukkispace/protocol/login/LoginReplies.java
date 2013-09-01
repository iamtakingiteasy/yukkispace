package org.eientei.yukkispace.protocol.login;

import org.msgpack.annotation.Message;

/**
 * User: iamtakingiteasy
 * Date: 2013-08-19
 * Time: 20:00
 */
@Message
public class LoginReplies {
    public final static byte LOGIN_DENIED         = (byte)0x00;
    public final static byte LOGIN_OK             = (byte)0x01;
    public final static byte LOGIN_TOKEN_NICKNAME = (byte)0x02;
    public final static byte LOGIN_WRONG_NICKNAME = (byte)0x03;
    public final static byte LOGIN_WRONG_PASSWORD = (byte)0x04;
    public final static byte LOGIN_BUSY           = (byte)0x05;
    public final static byte LOGIN_TIMEOUT        = (byte)0x06;
}
