package org.eientei.yukkispace.server.world.data;

import org.eientei.yukkispace.protocol.input.KeyboardKeyStruct;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * User: iamtakingiteasy
 * Date: 2013-08-21
 * Time: 00:32
 */
public class Client {
    private String name;
    private Camera activeCamera;
    private WorldTree.Node parentNode;
    private World parentWorld;
    private Set<Byte> pressedKeys = new HashSet<Byte>();
    private Set<World> worlds = new HashSet<World>();
    private Map<Byte, Short> mouseAxis = new HashMap<Byte, Short>();

    public Client(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Camera getActiveCamera() {
        return activeCamera;
    }

    public void setActiveCamera(Camera activeCamera) {
        this.activeCamera = activeCamera;
    }

    public Set<Byte> getPressedKeys() {
        return pressedKeys;
    }

    public void setPressedKeys(Set<Byte> pressedKeys) {
        this.pressedKeys = pressedKeys;
    }

    public Set<World> getWorlds() {
        return worlds;
    }

    public void setWorlds(Set<World> worlds) {
        this.worlds = worlds;
    }

    public WorldTree.Node getParentNode() {
        return parentNode;
    }

    public void setParentNode(WorldTree.Node parentNode) {
        this.parentNode = parentNode;
    }

    public World getParentWorld() {
        return parentWorld;
    }

    public void setParentWorld(World parentWorld) {
        this.parentWorld = parentWorld;
    }

    public Map<Byte, Short> getMouseAxis() {
        return mouseAxis;
    }

    public void setMouseAxis(Map<Byte, Short> mouseAxis) {
        this.mouseAxis = mouseAxis;
    }
}
