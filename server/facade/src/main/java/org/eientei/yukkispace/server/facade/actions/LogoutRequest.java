package org.eientei.yukkispace.server.facade.actions;

import org.eientei.yukkispace.protocol.login.LogoutStruct;
import org.eientei.yukkispace.server.facade.Client;
import org.eientei.yukkispace.server.facade.FacadeServer;
import org.jeromq.ZMsg;
import org.msgpack.MessagePack;

import java.nio.ByteBuffer;

/**
 * User: iamtakingiteasy
 * Date: 2013-08-20
 * Time: 12:32
 */
public class LogoutRequest implements ClientAction {
    private MessagePack mpack = new MessagePack();

    @Override
    public void run(ByteBuffer id, ZMsg imsg, Client inClient, FacadeServer facade) throws Exception {
        if (inClient != null) {
            LogoutStruct request = mpack.read(imsg.remove().data(), LogoutStruct.class);
            inClient.setLoggingOutMessage(request.message);
            inClient.setLoggingOut(true);
            facade.disconnectUser(inClient);
        }
    }
}
