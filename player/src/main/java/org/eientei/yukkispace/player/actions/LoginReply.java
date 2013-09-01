package org.eientei.yukkispace.player.actions;

import org.eientei.yukkispace.player.Controller;
import org.eientei.yukkispace.protocol.login.LoginReplies;
import org.eientei.yukkispace.protocol.login.LoginReplyStruct;
import org.jeromq.ZMsg;
import org.msgpack.MessagePack;

/**
 * User: iamtakingiteasy
 * Date: 2013-08-19
 * Time: 19:49
 */
public class LoginReply implements ClientAction {
    private MessagePack mpack = new MessagePack();

    @Override
    public void run(ZMsg imsg, Controller controller) throws Exception {
        LoginReplyStruct reply = mpack.read(imsg.remove().data(), LoginReplyStruct.class);
        switch (reply.outcome) {
            case LoginReplies.LOGIN_DENIED:
                System.out.println("Login deined for unknown reason.");
                controller.stopPlayer();
                break;
            case LoginReplies.LOGIN_OK:
                System.out.println("Login ok! Proceeding with nickname: " + reply.nickname);
                controller.setConnected(true);
                break;
            case LoginReplies.LOGIN_TOKEN_NICKNAME:
                if (!controller.isConnected()) {
                    System.out.println("Login failed: nickname was already token");
                    controller.stopPlayer();
                }
                break;
            case LoginReplies.LOGIN_WRONG_NICKNAME:
                System.out.println("Login failed: wrong nickname");
                controller.stopPlayer();
                break;
            case LoginReplies.LOGIN_WRONG_PASSWORD:
                System.out.println("Login failed: wrong password");
                controller.stopPlayer();
                break;
            case LoginReplies.LOGIN_BUSY:
                controller.retry();
                break;
            case LoginReplies.LOGIN_TIMEOUT:
                System.out.println("Login failed: timeout");
                controller.stopPlayer();
                break;
        }

    }
}
