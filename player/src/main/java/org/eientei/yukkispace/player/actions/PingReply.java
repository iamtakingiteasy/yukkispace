package org.eientei.yukkispace.player.actions;

import org.eientei.yukkispace.player.Controller;
import org.eientei.yukkispace.player.actions.ClientAction;
import org.eientei.yukkispace.protocol.enumeration.Actions;
import org.eientei.yukkispace.protocol.login.LogoutStruct;
import org.eientei.yukkispace.protocol.login.PingPongStruct;
import org.jeromq.ZMsg;
import org.msgpack.MessagePack;

/**
 * User: iamtakingiteasy
 * Date: 2013-08-20
 * Time: 13:49
 */
public class PingReply implements ClientAction {
    private MessagePack mpack = new MessagePack();

    @Override
    public void run(ZMsg imsg, Controller controller) throws Exception {
        PingPongStruct popipo = mpack.read(imsg.remove().data(), PingPongStruct.class);

        byte[] head = mpack.write(Actions.ACTION_PONG);
        byte[] data = mpack.write(popipo);

        ZMsg omsg = new ZMsg();
        omsg.add(head);
        omsg.add(data);
        omsg.send(controller.getSocket());
    }
}
