package org.eientei.yukkispace.protocol.login;

import org.eientei.yukkispace.protocol.enumeration.Actions;
import org.msgpack.annotation.Message;

/**
 * User: iamtakingiteasy
 * Date: 2013-08-19
 * Time: 19:58
 */
@Message
public class LoginReplyStruct {
    public byte outcome = LoginReplies.LOGIN_DENIED;
    public String nickname;
    public String message;

    public LoginReplyStruct() {

    }

    public LoginReplyStruct(byte outcome, String nickname, String message) {
        this.outcome = outcome;
        this.nickname = nickname;
        this.message = message;
    }
}
