package org.eientei.yukkispace.server.facade.actions;

import org.eientei.yukkispace.protocol.input.InputStruct;
import org.eientei.yukkispace.server.facade.Client;
import org.eientei.yukkispace.server.facade.FacadeServer;
import org.jeromq.ZMsg;
import org.msgpack.MessagePack;

import java.nio.ByteBuffer;

/**
 * User: iamtakingiteasy
 * Date: 2013-08-20
 * Time: 19:22
 */
public class InputAction implements ClientAction {
    private MessagePack mpack = new MessagePack();

    @Override
    public void run(ByteBuffer id, ZMsg imsg, Client inClient, FacadeServer facade) throws Exception {
        if (inClient != null) {
            InputStruct input = mpack.read(imsg.remove().data(), InputStruct.class);
            inClient.getInput().offer(input);
        }
    }
}
