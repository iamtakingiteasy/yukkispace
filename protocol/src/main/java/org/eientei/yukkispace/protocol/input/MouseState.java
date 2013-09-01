package org.eientei.yukkispace.protocol.input;

/**
 * User: iamtakingiteasy
 * Date: 2013-08-19
 * Time: 12:42
 */
public class MouseState {
    public final static byte MOUSESTATE_NONE        = (byte)0x00;
    public final static byte MOUSESTATE_PRESSED     = (byte)0x01;
    public final static byte MOUSESTATE_RELEASED    = (byte)0x02;
    public final static byte MOUSESTATE_CLICK       = (byte)0x04;
    public final static byte MOUSESTATE_DOUBLECLICK = (byte)0x05;
}

