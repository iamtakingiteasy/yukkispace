package org.eientei.yukkispace.server.world.data;

/**
 * User: iamtakingiteasy
 * Date: 2013-08-20
 * Time: 22:11
 */
public class Camera extends Object3D {
    public Client activeClient;

    public Camera(String name) {
        super(name);
    }

    public Client getActiveClient() {
        return activeClient;
    }

    public void setActiveClient(Client activeClient) {
        this.activeClient = activeClient;
    }
}
