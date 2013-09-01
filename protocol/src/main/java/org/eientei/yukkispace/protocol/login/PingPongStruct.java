package org.eientei.yukkispace.protocol.login;

import org.msgpack.annotation.Message;

/**
 * User: iamtakingiteasy
 * Date: 2013-08-20
 * Time: 13:34
 */
@Message
public class PingPongStruct {
    public String server;

    public PingPongStruct() {

    }

    public PingPongStruct(String server) {
        this.server = server;
    }
}
