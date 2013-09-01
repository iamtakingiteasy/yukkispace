package org.eientei.yukkispace.server.facade;

import org.eientei.yukkispace.protocol.enumeration.Actions;
import org.eientei.yukkispace.protocol.input.InputStruct;
import org.eientei.yukkispace.protocol.login.LogoutReasons;
import org.eientei.yukkispace.protocol.login.LogoutStruct;
import org.eientei.yukkispace.protocol.login.PingPongStruct;
import org.eientei.yukkispace.protocol.world.SceneStruct;
import org.eientei.yukkispace.server.facade.actions.*;
import org.eientei.yukkispace.server.intercomm.ClientDescriptionStruct;
import org.eientei.yukkispace.server.intercomm.InputPackStruct;
import org.eientei.yukkispace.server.intercomm.InterserverActions;
import org.jeromq.*;
import org.msgpack.MessagePack;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;

/**
 * User: iamtakingiteasy
 * Date: 2013-08-19
 * Time: 21:28
 */
public class FacadeServer {
    private ZContext ctx = new ZContext();
    private ZMQ.Socket socket = ctx.createSocket(ZMQ.ROUTER);
    private ZMQ.Socket heart = ctx.createSocket(ZMQ.SUB);
    private ZMQ.Socket world = ctx.createSocket(ZMQ.ROUTER);
    private HeartBeat heartBeat = new HeartBeat(ctx);
    private MessagePack mpack = new MessagePack();
    private String bindAddrPort = "*:50765";
    private String worldAddrPort = "127.0.0.1:60777";
    private Map<Integer, ClientAction> actions = new HashMap<Integer, ClientAction>();
    private Map<Integer, WorldAction> worldActions = new HashMap<Integer, WorldAction>();
    private boolean okToGo = true;
    private Map<ByteBuffer, Client> clients = new HashMap<ByteBuffer, Client>();
    private Map<ByteBuffer, World> worlds = new HashMap<ByteBuffer, World>();


    public FacadeServer(String[] args) throws InterruptedException {
        if (args.length > 0) {
            bindAddrPort = args[0];
        }

        heartBeat.start();

        heart.subscribe("");

        actions.put(Actions.ACTION_LOGIN, new LoginRequest());
        actions.put(Actions.ACTION_LOGOUT, new LogoutRequest());
        actions.put(Actions.ACTION_INPUT, new InputAction());

        worldActions.put(InterserverActions.INTERSERV_SCENEDATA, new WorldScenePackUpdate());
        worldActions.put(InterserverActions.INTERSERV_WORLD_ONLINE, new WorldOnline());

        heart.connect("inproc://heart");
        socket.bind("tcp://" + bindAddrPort);
        world.bind("tcp://" + worldAddrPort);

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                try {
                    disconnectAll("Server is shutting down");
                } catch (IOException ignore) {
                }
            }
        });

    }

    public static void main(String[] args) throws Exception {
        FacadeServer server = new FacadeServer(args);
        server.serve();
    }

    private void serve() throws Exception {
        ZLoop.IZLoopHandler clientHandler = new ZLoop.IZLoopHandler() {
            @Override
            public int handle(ZLoop loop, ZMQ.PollItem item, Object arg) {
                ZMsg msg = ZMsg.recvMsg(socket);
                if (msg == null) {
                    return -1;
                }
                ByteBuffer id = ByteBuffer.wrap(msg.remove().data());
                Client sender = clients.get(id);
                if (sender != null && !sender.isLoggingOut()) {
                    sender.setLastPong(System.currentTimeMillis());
                }
                byte[] rawAction = msg.remove().data();
                Integer actionNo;
                try {
                    actionNo = mpack.read(rawAction, Integer.class);
                } catch (IOException ignore) {
                    return -1;
                }
                ClientAction action = actions.get(actionNo);
                if (action != null) {
                    try {
                        action.run(id, msg, sender, FacadeServer.this);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return (okToGo) ? 0 : -1;
            }
        };

        ZLoop.IZLoopHandler heartBeatHandler = new ZLoop.IZLoopHandler() {
            @Override
            public int handle(ZLoop loop, ZMQ.PollItem item, Object arg) {
                ZMsg.recvMsg(heart);
                InputPackStruct input = new InputPackStruct();
                for (Client c : clients.values()) {
                    long diff = System.currentTimeMillis() - c.getLastPong();
                    if (diff > 5000) {
                        try {
                            if (c.getLoggingOutMessage() == null) {
                                c.setLoggingOutMessage("Timeout");
                            }
                            disconnectUser(c);
                        } catch (IOException ignore) {
                        }
                        continue;
                    } else if (diff > 1000) {
                        try {
                            sendPing(c);
                            c.setLastSend(System.currentTimeMillis());
                        } catch (IOException ignore) {
                        }
                    }
                    while (!c.getScenes().isEmpty()) {
                        SceneStruct scene = c.getScenes().remove();
                        byte[] action = new byte[0];
                        byte[] data = new byte[0];
                        try {
                            action = mpack.write(Actions.ACTION_SCENE);
                            data = mpack.write(scene);
                        } catch (IOException ignore) {
                        }
                        ZMsg msg = new ZMsg();
                        msg.add(c.getId().array());
                        msg.add(action);
                        msg.add(data);
                        msg.send(socket);
                        c.setLastSend(System.currentTimeMillis());
                    }

                    if (System.currentTimeMillis() - c.getLastSend() > 100) {
                        try {
                            sendPing(c);
                            c.setLastSend(System.currentTimeMillis());
                        } catch (IOException ignore) {
                        }
                    }

                    InputStruct is = input.inputs.get(c.getNickname());
                    while (!c.getInput().isEmpty()) {
                        if (is == null) {
                            is = new InputStruct();
                            input.inputs.put(c.getNickname(), is);
                        }
                        InputStruct us = c.getInput().remove();
                        is.keys.addAll(us.keys);
                        is.mouseAxis.addAll(us.mouseAxis);
                        is.mouseButtons.addAll(us.mouseButtons);
                    }
                }
                byte[] action = new byte[0];
                byte[] data = new byte[0];
                try {
                    action = mpack.write(InterserverActions.INTERSERV_USERINPUT);
                    data = mpack.write(input);
                } catch (IOException e) {
                }

                for (ByteBuffer  id : worlds.keySet()) {
                    ZMsg msg = new ZMsg();
                    msg.add(id.array());
                    msg.add(action);
                    msg.add(data);
                    msg.send(world);
                }

                return (okToGo) ? 0 : -1;
            }
        };

        ZLoop.IZLoopHandler worldHandler = new ZLoop.IZLoopHandler() {
            @Override
            public int handle(ZLoop loop, ZMQ.PollItem item, Object arg) {
                ZMsg msg = ZMsg.recvMsg(world);
                if (msg == null) {
                    return -1;
                }

                ByteBuffer id = ByteBuffer.wrap(msg.remove().data());

                World world = worlds.get(id);

                byte[] rawAction = msg.remove().data();
                Integer actionNo;
                try {
                    actionNo = mpack.read(rawAction, Integer.class);
                } catch (IOException ignore) {
                    return -1;
                }

                WorldAction action = worldActions.get(actionNo);
                if (action != null) {
                    try {
                        action.run(id, msg, world, FacadeServer.this);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return (okToGo) ? 0 : -1;
            }
        };

        ZLoop loop = ZLoop.instance();
        loop.poller(new ZMQ.PollItem(socket, ZMQ.POLLIN), clientHandler, null);
        loop.poller(new ZMQ.PollItem(heart, ZMQ.POLLIN), heartBeatHandler, null);
        loop.poller(new ZMQ.PollItem(world, ZMQ.POLLIN), worldHandler, null);
        loop.start();
        disconnectAll("Server is shutting down");
        ctx.destroy();
    }

    private void sendPing(Client client) throws IOException {
        String serverName = "server";
        byte[] action = mpack.write(Actions.ACTION_PING);
        byte[] data = mpack.write(new PingPongStruct(serverName));
        ZMsg omsg = new ZMsg();
        omsg.add(client.getId().array());
        omsg.add(action);
        omsg.add(data);
        omsg.send(socket);
    }

    public void disconnectUser(Client client) throws IOException {
        String message = client.getLoggingOutMessage();
        byte reason = LogoutReasons.LOGOUT_QUIT;
        if (message == null || message.isEmpty()) {
            message = "Timeout";
            reason = LogoutReasons.LOGOUT_TIMEOUT;
        }
        LogoutStruct logout = new LogoutStruct(reason, message);

        byte[] action = mpack.write(Actions.ACTION_LOGOUT);
        byte[] data = mpack.write(logout);

        clients.remove(client.getId());
        System.out.println("Disconnecting user " + client.getNickname() + " with message \"" + client.getLoggingOutMessage() + "\"");

        ZMsg omsg = new ZMsg();
        omsg.add(client.getId().array());
        omsg.add(action);
        omsg.add(data);
        omsg.send(socket);

        logoutClient(client);
    }

    private void logoutClient(Client client) throws IOException {
        ClientDescriptionStruct description = new ClientDescriptionStruct(client.getNickname());

        byte[] action = mpack.write(InterserverActions.INTERSERV_LOGOUT);
        byte[] data = mpack.write(description);

        for (World w : worlds.values()) {
            ZMsg omsg = new ZMsg();
            omsg.add(w.getId().array());
            omsg.add(action);
            omsg.add(data);
            omsg.send(world);
        }
    }

    public void loginClient(Client client) throws IOException {
        ClientDescriptionStruct description = new ClientDescriptionStruct(client.getNickname());

        byte[] action = mpack.write(InterserverActions.INTERSERV_LOGIN);
        byte[] data = mpack.write(description);

        for (World w : worlds.values()) {
            ZMsg omsg = new ZMsg();
            omsg.add(w.getId().array());
            omsg.add(action);
            omsg.add(data);
            omsg.send(world);
        }
    }

    private void disconnectAll(String message) throws IOException {
        for (Client c : clients.values()) {
            c.setLoggingOutMessage(message);
            disconnectUser(c);
        }
    }

    public void stop() {
        okToGo = false;
    }

    public void addClient(ByteBuffer id, Client client) {
        clients.put(id, client);
        try {
            loginClient(client);
        } catch (IOException ignore) {
        }
    }

    public void addWorld(ByteBuffer id, World world) {
        worlds.put(id, world);
    }

    public Client lookupClient(String username) {
        for (Client c : clients.values()) {
            if (c.getNickname().equals(username)) {
                return c;
            }
        }
        return null;
    }

    public ZMQ.Socket getSocket() {
        return socket;
    }

    public Collection<Client> listClients() {
        return clients.values();
    }
}
