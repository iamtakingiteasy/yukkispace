package org.eientei.yukkispace.player;

import org.jeromq.ZContext;
import org.jeromq.ZMQ;
import org.jeromq.ZMsg;

/**
 * User: iamtakingiteasy
 * Date: 2013-08-20
 * Time: 13:18
 */
public class HeartBeat extends Thread {
    private ZContext ctx;
    private ZMQ.Socket socket;
    private boolean okToGo = true;

    public HeartBeat(ZContext ctx) {
        this.ctx = ctx;
        this.socket = ctx.createSocket(ZMQ.PUB);
        socket.bind("inproc://heart");
    }

    @Override
    public void run() {
        while (okToGo) {
            try {
                sleep(15);
            } catch (InterruptedException ignore) {
            }
            ZMsg msg = new ZMsg();
            msg.add("");
            msg.send(socket);
        }
    }
}
