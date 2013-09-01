package org.eientei.yukkispace.server.facade.actions;

import org.eientei.yukkispace.protocol.enumeration.Actions;
import org.eientei.yukkispace.protocol.login.LoginReplies;
import org.eientei.yukkispace.protocol.login.LoginReplyStruct;
import org.eientei.yukkispace.protocol.login.LoginRequestStruct;
import org.eientei.yukkispace.server.facade.Client;
import org.eientei.yukkispace.server.facade.FacadeServer;
import org.jeromq.ZMsg;
import org.msgpack.MessagePack;

import java.nio.ByteBuffer;

/**
 * User: iamtakingiteasy
 * Date: 2013-08-20
 * Time: 09:57
 */
public class LoginRequest implements ClientAction {
    private MessagePack mpack = new MessagePack();

    @Override
    public void run(ByteBuffer id, ZMsg imsg, Client inClient, FacadeServer facade) throws Exception {
        LoginRequestStruct request = mpack.read(imsg.remove().data(), LoginRequestStruct.class);
        for (Client c : facade.listClients()) {
            if (c.getNickname().equals(request.nickname)) {
                LoginReplyStruct reply = new LoginReplyStruct(LoginReplies.LOGIN_TOKEN_NICKNAME, request.nickname, "Nickname has already benn taken");

                byte[] action = mpack.write(Actions.ACTION_LOGIN);
                byte[] data = mpack.write(reply);

                ZMsg omsg = new ZMsg();
                omsg.add(id.array());
                omsg.add(action);
                omsg.add(data);
                omsg.send(facade.getSocket());
                return;
            }
        }

        Client client = new Client(id, request.nickname, request.passwordHash);
        client.setLastPong(System.currentTimeMillis());
        client.setLastSend(System.currentTimeMillis());
        facade.addClient(id, client);

        LoginReplyStruct reply = new LoginReplyStruct(LoginReplies.LOGIN_OK, request.nickname, "Welcome to our server!");

        byte[] action = mpack.write(Actions.ACTION_LOGIN);
        byte[] data = mpack.write(reply);

        System.out.println(request.nickname + " logged in");

        ZMsg omsg = new ZMsg();
        omsg.add(id.array());
        omsg.add(action);
        omsg.add(data);
        omsg.send(facade.getSocket());
    }
}
