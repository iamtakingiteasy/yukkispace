package org.eientei.yukkispace.server.facade;

import java.nio.ByteBuffer;

/**
 * User: iamtakingiteasy
 * Date: 2013-08-21
 * Time: 19:54
 */
public class World {
    private ByteBuffer id;

    public World(ByteBuffer id) {
        this.id = id;
    }

    public ByteBuffer getId() {
        return id;
    }

    public void setId(ByteBuffer id) {
        this.id = id;
    }
}
