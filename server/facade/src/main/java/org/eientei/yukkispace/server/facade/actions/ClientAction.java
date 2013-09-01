package org.eientei.yukkispace.server.facade.actions;

import org.eientei.yukkispace.server.facade.Client;
import org.eientei.yukkispace.server.facade.FacadeServer;
import org.jeromq.ZMsg;

import java.nio.ByteBuffer;

/**
 * User: iamtakingiteasy
 * Date: 2013-08-20
 * Time: 09:39
 */
public interface ClientAction {
    public void run(ByteBuffer id, ZMsg imsg, Client inClient, FacadeServer facade) throws Exception;
}
