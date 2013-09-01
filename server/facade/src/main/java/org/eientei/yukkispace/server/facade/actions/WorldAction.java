package org.eientei.yukkispace.server.facade.actions;

import org.eientei.yukkispace.server.facade.Client;
import org.eientei.yukkispace.server.facade.FacadeServer;
import org.eientei.yukkispace.server.facade.World;
import org.jeromq.ZMsg;

import java.nio.ByteBuffer;

/**
 * User: iamtakingiteasy
 * Date: 2013-08-20
 * Time: 19:12
 */
public interface WorldAction {
    public void run(ByteBuffer id, ZMsg imsg, World inWorld, FacadeServer facade) throws Exception;
}
