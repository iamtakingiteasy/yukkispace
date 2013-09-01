package org.eientei.yukkispace.server.intercomm;

/**
 * User: iamtakingiteasy
 * Date: 2013-08-20
 * Time: 18:52
 */
public class InterserverActions {
    public final static int INTERSERV_NONE         = 0x00;
    public final static int INTERSERV_LOGIN        = 0x01;
    public final static int INTERSERV_LOGOUT       = 0x02;
    public final static int INTERSERV_PING         = 0x03;
    public final static int INTERSERV_PONG         = 0x04;
    public final static int INTERSERV_USERINPUT    = 0x05;
    public final static int INTERSERV_SCENEDATA    = 0x06;
    public final static int INTERSERV_WORLD_ONLINE = 0x07;
}
