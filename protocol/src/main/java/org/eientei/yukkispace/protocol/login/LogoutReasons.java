package org.eientei.yukkispace.protocol.login;

/**
 * User: iamtakingiteasy
 * Date: 2013-08-20
 * Time: 13:03
 */
public class LogoutReasons {
    public final static byte LOGOUT_NONE    = (byte)0x00;
    public final static byte LOGOUT_TIMEOUT = (byte)0x01;
    public final static byte LOGOUT_QUIT    = (byte)0x02;
    public final static byte LOGOUT_KICK    = (byte)0x03;
    public final static byte LOGOUT_BAN     = (byte)0x04;
}
