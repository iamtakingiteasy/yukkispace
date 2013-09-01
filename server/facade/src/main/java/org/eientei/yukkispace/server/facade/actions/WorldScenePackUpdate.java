package org.eientei.yukkispace.server.facade.actions;

import org.eientei.yukkispace.protocol.enumeration.Actions;
import org.eientei.yukkispace.protocol.world.SceneStruct;
import org.eientei.yukkispace.server.facade.Client;
import org.eientei.yukkispace.server.facade.FacadeServer;
import org.eientei.yukkispace.server.facade.World;
import org.eientei.yukkispace.server.intercomm.ScenePackStruct;
import org.jeromq.ZMsg;
import org.msgpack.MessagePack;

import java.nio.ByteBuffer;
import java.util.Map;

/**
 * User: iamtakingiteasy
 * Date: 2013-08-20
 * Time: 19:13
 */
public class WorldScenePackUpdate implements WorldAction  {
    private MessagePack mpack = new MessagePack();

    @Override
    public void run(ByteBuffer id, ZMsg imsg, World inWorld, FacadeServer facade) throws Exception {
        ScenePackStruct scenePack = mpack.read(imsg.remove().data(), ScenePackStruct.class);
        for (Map.Entry<String, SceneStruct> entry : scenePack.userScenes.entrySet()) {
            Client client = facade.lookupClient(entry.getKey());

            if (client == null) {
                continue;
            }

            byte[] action = mpack.write(Actions.ACTION_SCENE);
            byte[] data = mpack.write(entry.getValue());

            ZMsg omsg = new ZMsg();
            omsg.add(client.getId().array());
            omsg.add(action);
            omsg.add(data);
            omsg.send(facade.getSocket());

            client.setLastSend(System.currentTimeMillis());
        }
    }
}
