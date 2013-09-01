package org.eientei.yukkispace.player.actions;

import org.eientei.yukkispace.player.Controller;
import org.eientei.yukkispace.protocol.login.LogoutReasons;
import org.eientei.yukkispace.protocol.login.LogoutStruct;
import org.jeromq.ZMsg;
import org.msgpack.MessagePack;

/**
 * User: iamtakingiteasy
 * Date: 2013-08-20
 * Time: 12:45
 */
public class LogoutReply implements ClientAction {
    private MessagePack mpack = new MessagePack();

    @Override
    public void run(ZMsg imsg, Controller controller) throws Exception {
        LogoutStruct reply = mpack.read(imsg.remove().data(), LogoutStruct.class);
        switch (reply.reason) {
            case LogoutReasons.LOGOUT_NONE:
                break;
            case LogoutReasons.LOGOUT_TIMEOUT:
            case LogoutReasons.LOGOUT_QUIT:
                System.out.println("We have been disconnected with message: " + reply.message);
                break;
            case LogoutReasons.LOGOUT_KICK:
                System.out.println("We have been kicked with message: " + reply.message);
                break;
            case LogoutReasons.LOGOUT_BAN:
                System.out.println("We have been banned with message: " + reply.message);
                break;
        }
        controller.stopPlayer();
    }
}
