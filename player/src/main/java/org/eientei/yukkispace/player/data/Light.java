package org.eientei.yukkispace.player.data;

import org.eientei.yukkispace.protocol.world.LightStruct;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.vector.Matrix4f;

import java.nio.FloatBuffer;

/**
 * User: iamtakingiteasy
 * Date: 2013-08-20
 * Time: 15:55
 */
public class Light {
    private LightStruct data;
    private boolean active = false;
    private int depthmapSideSize = 512;
    private int depthmapTextureId;
    private int framebufferObjectId;

    public Light() {
        depthmapTextureId = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, depthmapTextureId);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_DEPTH_COMPONENT, depthmapSideSize, depthmapSideSize, 0, GL11.GL_DEPTH_COMPONENT, GL11.GL_FLOAT, (FloatBuffer)null);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);


        framebufferObjectId = GL30.glGenFramebuffers();
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, framebufferObjectId);
        GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, GL11.GL_TEXTURE_2D, depthmapTextureId, 0);
        if (GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER) != GL30.GL_FRAMEBUFFER_COMPLETE) {
            System.out.println("Framebuffer incomplete!");
        }
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
    }

    public int getDepthmapSideSize() {
        return depthmapSideSize;
    }

    public void setDepthmapSideSize(int depthmapSideSize) {
        this.depthmapSideSize = depthmapSideSize;
    }

    public int getDepthmapTextureId() {
        return depthmapTextureId;
    }

    public void setDepthmapTextureId(int depthmapTextureId) {
        this.depthmapTextureId = depthmapTextureId;
    }

    public int getFramebufferObjectId() {
        return framebufferObjectId;
    }

    public void setFramebufferObjectId(int framebufferObjectId) {
        this.framebufferObjectId = framebufferObjectId;
    }

    public boolean isActive() {
        return active;
    }

    public void activate() {
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
    }

    public LightStruct getData() {
        return data;
    }

    public void setData(LightStruct data) {
        this.data = data;
    }
}
