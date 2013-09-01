package org.eientei.yukkispace.server.world.data;

/**
 * User: iamtakingiteasy
 * Date: 2013-08-21
 * Time: 00:32
 */
public class Entity extends Object3D {
    private String entityId;

    public Entity(String name) {
        super(name);
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }
}
