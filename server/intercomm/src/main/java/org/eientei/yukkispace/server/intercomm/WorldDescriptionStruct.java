package org.eientei.yukkispace.server.intercomm;

import org.msgpack.annotation.Message;

/**
 * User: iamtakingiteasy
 * Date: 2013-08-21
 * Time: 20:11
 */
@Message
public class WorldDescriptionStruct {
    public String name;

    public WorldDescriptionStruct() {

    }

    public WorldDescriptionStruct(String name) {
        this.name = name;
    }
}
