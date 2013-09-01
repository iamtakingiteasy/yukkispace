package org.eientei.yukkispace.server.world.actions;

import org.eientei.yukkispace.protocol.input.InputStruct;
import org.eientei.yukkispace.protocol.input.KeyboardKeyStruct;
import org.eientei.yukkispace.protocol.input.KeyboardState;
import org.eientei.yukkispace.protocol.input.MouseAxisStruct;
import org.eientei.yukkispace.server.intercomm.InputPackStruct;
import org.eientei.yukkispace.server.world.WorldServer;
import org.eientei.yukkispace.server.world.data.Client;
import org.jeromq.ZMsg;
import org.msgpack.MessagePack;

import java.util.List;
import java.util.Map;

/**
 * User: iamtakingiteasy
 * Date: 2013-08-20
 * Time: 20:24
 */
public class InputAction implements FacadeAction {
    private MessagePack mpack = new MessagePack();

    @Override
    public void run(ZMsg imsg, WorldServer server) throws Exception {
        InputPackStruct input = mpack.read(imsg.remove().data(), InputPackStruct.class);

        for (Map.Entry<String, InputStruct> inp : input.inputs.entrySet()) {
            String username = inp.getKey();
            Client client = server.lookupClient(username);

            for (KeyboardKeyStruct k : inp.getValue().keys) {
                switch (k.state) {
                    case KeyboardState.KEYSTATE_PRESSED:
                        client.getPressedKeys().add(k.key);
                        break;
                    case KeyboardState.KEYSTATE_RELEASED:
                        client.getPressedKeys().remove(k.key);
                        break;
                }
            }

            for (MouseAxisStruct ma : inp.getValue().mouseAxis) {
                Short value = client.getMouseAxis().get(ma.axis);
                if (value == null) {
                    value = 0;
                }
                value = (short)(value + ma.value);
                client.getMouseAxis().put(ma.axis, ma.value);
            }
        }
    }
}
