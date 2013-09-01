package org.eientei.yukkispace.server.world.actions;

import org.eientei.yukkispace.server.world.WorldServer;
import org.jeromq.ZMsg;

/**
 * User: iamtakingiteasy
 * Date: 2013-08-20
 * Time: 20:22
 */
public interface FacadeAction {
    public void run(ZMsg imsg, WorldServer server) throws Exception;
}
