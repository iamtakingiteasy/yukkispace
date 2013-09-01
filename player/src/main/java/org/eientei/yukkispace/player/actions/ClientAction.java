package org.eientei.yukkispace.player.actions;

import org.eientei.yukkispace.player.Controller;
import org.eientei.yukkispace.player.Controller;
import org.jeromq.ZMsg;

import java.io.IOException;

/**
 * User: iamtakingiteasy
 * Date: 2013-08-19
 * Time: 19:47
 */
public interface ClientAction {
    public void run(ZMsg imsg, Controller controller) throws Exception;
}
