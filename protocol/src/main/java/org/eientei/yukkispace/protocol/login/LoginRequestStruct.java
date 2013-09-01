package org.eientei.yukkispace.protocol.login;

import org.msgpack.annotation.Message;

/**
 * User: iamtakingiteasy
 * Date: 2013-08-19
 * Time: 19:54
 */
@Message
public class LoginRequestStruct {
    public String nickname;
    public String passwordHash;

    public LoginRequestStruct() {

    }

    public LoginRequestStruct(String nickname, String passwordHash) {
        this.nickname = nickname;
        this.passwordHash = passwordHash;
    }
}
