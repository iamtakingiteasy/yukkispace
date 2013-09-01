package org.eientei.yukkispace.server.intercomm;

import org.msgpack.annotation.Message;

/**
 * User: iamtakingiteasy
 * Date: 2013-08-21
 * Time: 20:30
 */
@Message
public class ClientDescriptionStruct {
    public String name;

    public ClientDescriptionStruct() {

    }

    public ClientDescriptionStruct(String name) {
        this.name = name;
    }
}
