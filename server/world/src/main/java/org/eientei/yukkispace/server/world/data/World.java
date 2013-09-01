package org.eientei.yukkispace.server.world.data;

import org.eientei.yukkispace.protocol.world.SceneStruct;
import org.eientei.yukkispace.server.world.data.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * User: iamtakingiteasy
 * Date: 2013-08-21
 * Time: 00:35
 */
public interface World {
    public void step();
    public SceneStruct getScene(Camera cam);
    public void addClient(Client client);
    public void removeClient(Client client);
}
