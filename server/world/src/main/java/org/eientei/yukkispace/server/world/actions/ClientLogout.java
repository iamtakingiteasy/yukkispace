package org.eientei.yukkispace.server.world.actions;

import org.eientei.yukkispace.server.intercomm.ClientDescriptionStruct;
import org.eientei.yukkispace.server.world.WorldServer;
import org.jeromq.ZMsg;
import org.msgpack.MessagePack;

/**
 * User: iamtakingiteasy
 * Date: 2013-08-21
 * Time: 21:09
 */
public class ClientLogout implements FacadeAction {
    private MessagePack mpack = new MessagePack();

    @Override
    public void run(ZMsg imsg, WorldServer server) throws Exception {
        ClientDescriptionStruct clientDescription = mpack.read(imsg.remove().data(), ClientDescriptionStruct.class);
        System.out.println("User " + clientDescription.name + " logged out!");
        server.removeClient(clientDescription.name);
    }
}
