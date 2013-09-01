package org.eientei.yukkispace.server.facade.actions;

import org.eientei.yukkispace.server.facade.Client;
import org.eientei.yukkispace.server.facade.FacadeServer;
import org.eientei.yukkispace.server.facade.World;
import org.eientei.yukkispace.server.intercomm.WorldDescriptionStruct;
import org.jeromq.ZMsg;
import org.msgpack.MessagePack;

import java.nio.ByteBuffer;

/**
 * User: iamtakingiteasy
 * Date: 2013-08-21
 * Time: 20:13
 */
public class WorldOnline implements WorldAction {
    private MessagePack mpack = new MessagePack();

    @Override
    public void run(ByteBuffer id, ZMsg imsg, World inWorld, FacadeServer facade) throws Exception {
        if (inWorld == null) {
            WorldDescriptionStruct description = mpack.read(imsg.remove().data(), WorldDescriptionStruct.class);
            System.out.println("World " + description.name + " coming in!");
            inWorld = new World(id);
            facade.addWorld(id, inWorld);

            for (Client c : facade.listClients()) {
                facade.loginClient(c);
            }
        }
    }
}
