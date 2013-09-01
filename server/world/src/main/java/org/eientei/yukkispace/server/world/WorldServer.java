package org.eientei.yukkispace.server.world;

import org.eientei.yukkispace.server.intercomm.InterserverActions;
import org.eientei.yukkispace.server.intercomm.ScenePackStruct;
import org.eientei.yukkispace.server.intercomm.WorldDescriptionStruct;
import org.eientei.yukkispace.server.world.actions.ClientLogin;
import org.eientei.yukkispace.server.world.actions.ClientLogout;
import org.eientei.yukkispace.server.world.actions.FacadeAction;
import org.eientei.yukkispace.server.world.actions.InputAction;
import org.eientei.yukkispace.server.world.data.Camera;
import org.eientei.yukkispace.server.world.data.Client;
import org.eientei.yukkispace.server.world.data.World;
import org.eientei.yukkispace.server.world.worlds.nullvoid.VoidWorld;
import org.jeromq.ZContext;
import org.jeromq.ZLoop;
import org.jeromq.ZMQ;
import org.jeromq.ZMsg;
import org.msgpack.MessagePack;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * User: iamtakingiteasy
 * Date: 2013-08-20
 * Time: 17:32
 */
public class WorldServer {
    private MessagePack mpack = new MessagePack();
    private String bindAddrPort = "127.0.0.1:60777";
    private ZContext ctx = new ZContext();
    private HeartBeat heartBeat = new HeartBeat(ctx);
    private ZMQ.Socket socket = ctx.createSocket(ZMQ.DEALER);
    private ZMQ.Socket heart = ctx.createSocket(ZMQ.SUB);
    private Map<Integer, FacadeAction> actions = new HashMap<Integer, FacadeAction>();
    private boolean okToGo = true;
    private List<World> worlds = new CopyOnWriteArrayList<World>();
    private Map<String, Client> clients = new HashMap<String, Client>();
    private long timeout = 1000;
    private long lastMessage = System.currentTimeMillis();

    public WorldServer(String[] args) throws IOException {
        if (args.length > 0) {
            bindAddrPort = args[0];
        }

        heartBeat.start();
        heart.subscribe("");
        heart.connect("inproc://heart");

        socket.connect("tcp://" + bindAddrPort);

        initHandshake();

        actions.put(InterserverActions.INTERSERV_LOGIN, new ClientLogin());
        actions.put(InterserverActions.INTERSERV_LOGOUT, new ClientLogout());
        actions.put(InterserverActions.INTERSERV_USERINPUT, new InputAction());

        World voidWorld = new VoidWorld();

        worlds.add(voidWorld);
    }

    private void initHandshake() throws IOException {
        ZMsg zmsg = new ZMsg();
        byte[] action = mpack.write(InterserverActions.INTERSERV_WORLD_ONLINE);
        byte[] data = mpack.write(new WorldDescriptionStruct("WorldServer #0"));

        zmsg.add(action);
        zmsg.add(data);
        zmsg.send(socket);
    }


    public static void main(String[] args) throws IOException {
        WorldServer server = new WorldServer(args);
        server.serve();
    }

    private void serve() {
        ZLoop.IZLoopHandler facadeHandler = new ZLoop.IZLoopHandler() {
            @Override
            public int handle(ZLoop loop, ZMQ.PollItem item, Object arg) {
                lastMessage = System.currentTimeMillis();
                ZMsg msg = ZMsg.recvMsg(socket);
                if (msg == null) {
                    return -1;
                }

                byte[] rawAction = msg.remove().data();
                Integer actionNo;
                try {
                    actionNo = mpack.read(rawAction, Integer.class);
                } catch (IOException ignore) {
                    return -1;
                }
                FacadeAction action = actions.get(actionNo);
                if (action != null) {
                    try {
                        action.run(msg, WorldServer.this);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return (okToGo) ? 0 : -1;
            }
        };

        ZLoop.IZLoopHandler heartbeatHandler = new ZLoop.IZLoopHandler() {
            @Override
            public int handle(ZLoop loop, ZMQ.PollItem item, Object arg) {
                if (System.currentTimeMillis() - lastMessage > timeout) {
                    try {
                        initHandshake();
                    } catch (IOException ignore) {
                    }
                }
                ZMsg.recvMsg(heart);
                try {
                    simulationStep();
                } catch (IOException ignore) {
                }
                return (okToGo) ? 0 : -1;
            }
        };

        ZLoop loop = ZLoop.instance();
        loop.poller(new ZMQ.PollItem(socket, ZMQ.POLLIN), facadeHandler, null);
        loop.poller(new ZMQ.PollItem(heart, ZMQ.POLLIN), heartbeatHandler, null);
        loop.start();
    }

    private void simulationStep() throws IOException {
        for (World w: worlds) {
            w.step();
        }

        ScenePackStruct scenePack = new ScenePackStruct();
        for (Client c : clients.values()) {
            Camera cam = c.getActiveCamera();
            scenePack.userScenes.put(c.getName(), cam.getParentWorld().getScene(cam));
        }
        if (!scenePack.userScenes.isEmpty()) {
            byte[] action = mpack.write(InterserverActions.INTERSERV_SCENEDATA);
            byte[] data = mpack.write(scenePack);

            ZMsg zmsg = new ZMsg();
            zmsg.add(action);
            zmsg.add(data);
            zmsg.send(socket);
        }
    }

    public Client lookupClient(String name) {
        return clients.get(name);
    }

    public void addClient(Client client) {
        clients.put(client.getName(), client);
        for (World w : worlds) {
            w.addClient(client);
        }
    }

    public void removeClient(String name) {
        Client client = clients.remove(name);
        for (World w : worlds) {
            w.removeClient(client);
        }
    }
}
