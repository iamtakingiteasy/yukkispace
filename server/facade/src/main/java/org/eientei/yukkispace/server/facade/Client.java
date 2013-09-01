package org.eientei.yukkispace.server.facade;

import org.eientei.yukkispace.protocol.input.InputStruct;
import org.eientei.yukkispace.protocol.world.SceneStruct;

import java.nio.ByteBuffer;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * User: iamtakingiteasy
 * Date: 2013-08-20
 * Time: 09:58
 */
public class Client {
    private ByteBuffer id;
    private String nickname;
    private String passwordhash;
    private long lastPong = System.currentTimeMillis();
    private long lastSend = System.currentTimeMillis();
    private boolean loggingOut = false;
    private String loggingOutMessage;
    private Queue<SceneStruct> scenes = new ArrayBlockingQueue<SceneStruct>(4);
    private Queue<InputStruct> input = new ArrayBlockingQueue<InputStruct>(64);

    public Client(ByteBuffer id, String nickname, String passwordhash) {
        this.id = id;
        this.nickname = nickname;
        this.passwordhash = passwordhash;
    }

    public ByteBuffer getId() {
        return id;
    }

    public void setId(ByteBuffer id) {
        this.id = id;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getPasswordhash() {
        return passwordhash;
    }

    public void setPasswordhash(String passwordhash) {
        this.passwordhash = passwordhash;
    }

    public long getLastPong() {
        return lastPong;
    }

    public void setLastPong(long lastPong) {
        this.lastPong = lastPong;
    }

    public boolean isLoggingOut() {
        return loggingOut;
    }

    public void setLoggingOut(boolean loggingOut) {
        this.loggingOut = loggingOut;
    }

    public String getLoggingOutMessage() {
        return loggingOutMessage;
    }

    public void setLoggingOutMessage(String loggingOutMessage) {
        this.loggingOutMessage = loggingOutMessage;
    }

    public Queue<SceneStruct> getScenes() {
        return scenes;
    }

    public Queue<InputStruct> getInput() {
        return input;
    }

    public void setLastSend(long lastSend) {
        this.lastSend = lastSend;
    }

    public long getLastSend() {
        return lastSend;
    }
}
