package org.eientei.yukkispace.player.actions;

import org.eientei.yukkispace.player.Controller;
import org.eientei.yukkispace.protocol.world.SceneStruct;
import org.jeromq.ZMsg;
import org.msgpack.MessagePack;

/**
 * User: iamtakingiteasy
 * Date: 2013-08-22
 * Time: 20:36
 */
public class SceneUpdate implements ClientAction {
    private MessagePack mpack = new MessagePack();

    @Override
    public void run(ZMsg imsg, Controller controller) throws Exception {
        SceneStruct scene = mpack.read(imsg.remove().data(), SceneStruct.class);
        controller.enqueueScene(scene);
    }
}
