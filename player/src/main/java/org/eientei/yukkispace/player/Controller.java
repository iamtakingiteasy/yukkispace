package org.eientei.yukkispace.player;

import org.eientei.yukkispace.player.actions.*;
import org.eientei.yukkispace.protocol.enumeration.Actions;
import org.eientei.yukkispace.protocol.input.InputStruct;
import org.eientei.yukkispace.protocol.input.KeyboardKeyStruct;
import org.eientei.yukkispace.protocol.input.MouseAxisStruct;
import org.eientei.yukkispace.protocol.login.LoginRequestStruct;
import org.eientei.yukkispace.protocol.login.LogoutReasons;
import org.eientei.yukkispace.protocol.login.LogoutStruct;
import org.eientei.yukkispace.protocol.world.SceneStruct;
import org.jeromq.ZContext;
import org.jeromq.ZLoop;
import org.jeromq.ZMQ;
import org.jeromq.ZMsg;
import org.msgpack.MessagePack;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * User: iamtakingiteasy
 * Date: 2013-08-19
 * Time: 13:32
 */
public class Controller extends Thread {
    private ZContext ctx = new ZContext();
    private HeartBeat heartBeat = new HeartBeat(ctx);
    private ZMQ.Socket heart = ctx.createSocket(ZMQ.SUB);
    private ZMQ.Socket socket = ctx.createSocket(ZMQ.DEALER);
    private Map<Integer, ClientAction> actions = new HashMap<Integer, ClientAction>();
    private List<KeyboardKeyStruct> keys = new CopyOnWriteArrayList<KeyboardKeyStruct>();
    private List<MouseAxisStruct> maxis = new CopyOnWriteArrayList<MouseAxisStruct>();
    private MessagePack mpack = new MessagePack();
    private boolean connected = false;
    private boolean okToGo = true;
    private int retries = 0;
    private String serverPort = "127.0.0.1:50765";
    private String nickname = "Guest";
    private String password = "password";
    private long timeout = 1000;
    private long lastMessage = System.currentTimeMillis();
    private Player player;

    public Controller(String[] args, Player player) {
        if (args.length > 0) {
            serverPort = args[0];
        }

        if (args.length > 1) {
            nickname = args[1];
        }

        heartBeat.setDaemon(true);
        heartBeat.start();

        heart.subscribe("");
        heart.connect("inproc://heart");

        this.player = player;

        actions.put(Actions.ACTION_LOGIN, new LoginReply());
        actions.put(Actions.ACTION_LOGOUT, new LogoutReply());
        actions.put(Actions.ACTION_PING, new PingReply());
        actions.put(Actions.ACTION_SCENE, new SceneUpdate());
    }

    public void run() {
        socket.connect("tcp://" + serverPort);
        ZMQ.Poller poller = ctx.getContext().poller();
        poller.register(socket, ZMQ.Poller.POLLIN);

        try {
            initHandshake();
        } catch (NoSuchAlgorithmException ignore) {
        } catch (IOException ignore) {
        }

        ZLoop.IZLoopHandler serverHandler = new ZLoop.IZLoopHandler() {
            @Override
            public int handle(ZLoop loop, ZMQ.PollItem item, Object arg) {
                lastMessage = System.currentTimeMillis();
                ZMsg msg = ZMsg.recvMsg(socket);
                if (msg == null) {
                    return -1;
                }

                byte[] rawAction = msg.remove().data();
                Integer actionNo = null;
                try {
                    actionNo = mpack.read(rawAction, Integer.class);
                } catch (IOException ignore) {
                }

                ClientAction action = actions.get(actionNo);
                if (action != null) {
                    try {
                        action.run(msg, Controller.this);
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
                ZMsg ignoreMsg = ZMsg.recvMsg(heart);

                if ((System.currentTimeMillis() - lastMessage) > timeout * 2) {
                    if (connected) {
                        setConnected(false);
                    }
                    retry();
                    lastMessage = System.currentTimeMillis();
                }

                InputStruct inputs = new InputStruct();

                if (!keys.isEmpty()) {
                    inputs.keys.addAll(keys);
                    keys.clear();
                }

                if (!maxis.isEmpty()) {
                    inputs.mouseAxis.addAll(maxis);
                    maxis.clear();
                }

                // TODO: mouse buttons

                if (!inputs.keys.isEmpty() || !inputs.mouseAxis.isEmpty() || !inputs.mouseButtons.isEmpty()) {
                    byte[] head = new byte[0];
                    byte[] data = new byte[0];
                    try {
                        head = mpack.write(Actions.ACTION_INPUT);
                        data = mpack.write(inputs);
                    } catch (IOException ignore) {
                    }

                    ZMsg msg = new ZMsg();
                    msg.add(head);
                    msg.add(data);
                    msg.send(socket);
                }
                return (okToGo) ? 0 : -1;
            }
        };

        ZLoop loop = ZLoop.instance();
        loop.poller(new ZMQ.PollItem(socket, ZMQ.POLLIN), serverHandler, null);
        loop.poller(new ZMQ.PollItem(heart, ZMQ.POLLIN), heartBeatHandler, null);
        loop.start();

        stopPlayer();
        ctx.destroy();
    }

    public void disconnect(String reason) {
        LogoutStruct logout = new LogoutStruct(LogoutReasons.LOGOUT_QUIT, "Quit");

        byte[] head = new byte[0];
        byte[] data = new byte[0];
        try {
            head = mpack.write(Actions.ACTION_LOGOUT);
            data = mpack.write(logout);
        } catch (IOException e) {
        }

        ZMsg msg = new ZMsg();
        msg.add(head);
        msg.add(data);

        msg.send(socket);
    }

    private void initHandshake() throws NoSuchAlgorithmException, IOException {
        if (connected) {
            return;
        }
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(password.getBytes("UTF-8"));
        byte[] bytes = md.digest();
        StringBuffer hexString = new StringBuffer();
        for (int i=0;i<bytes.length;i++) {
            hexString.append(Integer.toHexString(0xFF & bytes[i]));
        }
        LoginRequestStruct loginRequest = new LoginRequestStruct(nickname, hexString.toString());

        byte[] head = mpack.write(Actions.ACTION_LOGIN);
        byte[] data = mpack.write(loginRequest);


        ZMsg msg = new ZMsg();
        msg.add(head);
        msg.add(data);

        msg.send(socket);
        retries++;
    }

    public void stopPlayer() {
        okToGo = false;
        player.stopPlayer();
    }

    public void retry() {
        if (retries > 5) {
            System.out.println("Server is not responding");
            stopPlayer();
            return;
        }
        System.out.println("Login-server is busy, retrying in one second..." + retries);

        try {
            initHandshake();
        } catch (NoSuchAlgorithmException ignore) {
        } catch (IOException ignore) {
        }
    }


    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean val) {
        connected = val;
        if (connected) {
            retries = 1;
            timeout = 1000;
        } else {
            timeout = 1000;
        }
    }

    public ZMQ.Socket getSocket() {
        return socket;
    }

    public void enqueueKeys(List<KeyboardKeyStruct> keys) {
        this.keys.addAll(keys);
    }

    public void enqueueMAxis(List<MouseAxisStruct> maxis) {
        this.maxis.addAll(maxis);
    }

    public void enqueueScene(SceneStruct scene) {
        player.pushScene(scene);
    }
}
