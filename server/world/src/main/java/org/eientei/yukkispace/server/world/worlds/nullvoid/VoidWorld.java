package org.eientei.yukkispace.server.world.worlds.nullvoid;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.eientei.yukkispace.protocol.input.KeyboardKeys;
import org.eientei.yukkispace.protocol.input.MouseAxis;
import org.eientei.yukkispace.protocol.world.EntityStruct;
import org.eientei.yukkispace.protocol.world.LightStruct;
import org.eientei.yukkispace.protocol.world.SceneStruct;
import org.eientei.yukkispace.server.world.data.*;

import java.util.*;

/**
 * User: iamtakingiteasy
 * Date: 2013-08-21
 * Time: 00:43
 */
public class VoidWorld implements World {
    private WorldTree tree = new WorldTree(this);
    private float linearSpeed = 0.05f;
    private float angularSpeed = 0.25f;
    private float ticks = 0;

    public VoidWorld() {
        tree.getRoot().addEntity(new Entity("monkey"));
        Light light = new Light("light");
        light.setPosition(new Vector3D(0, -5, 0));
      //  tree.getRoot().addLight(light);
    }

    public void addClient(Client client) {
        Camera camera = new Camera("camera");
        camera.setPosition(new Vector3D(0, 0, 0));
        client.setActiveCamera(camera);
        camera.setActiveClient(client);
        WorldTree.Node node = new WorldTree.Node(client.getName(), this);
        Light light = new Light("light");
        light.setSpotCut(30);
        light.setPosition(new Vector3D(0, 0, 0));
        light.setDirection(new Vector3D(0, 1, 0));
        light.setSpecular(new Vector3D(0.5, 0.5, 0.5));
        light.setIntensity(3.0f);
        node.addLight(light);
        node.addClient(client);
        node.addEntity(new Entity("monkey"));
        node.addCamera(camera);
        node.translate(new Vector3D(0, -5, 0));
        tree.addNode(node);
    }

    @Override
    public void removeClient(Client client) {
        tree.getRoot().removeNode(client.getName());
    }

    @Override
    public void step() {
        ticks++;
        for (Camera c : tree.cameras()) {
            Client client = c.getActiveClient();
            if (client == null) {
                continue;
            }
            Vector3D direction = new Vector3D(0, 0, 0);
            for (byte key : client.getPressedKeys()) {
                switch (key) {
                    case KeyboardKeys.KEY_W:
                        direction = direction.add(new Vector3D(0, 1, 0));
                        break;
                    case KeyboardKeys.KEY_S:
                        direction = direction.add(new Vector3D(0, -1, 0));
                        break;
                    case KeyboardKeys.KEY_A:
                        direction = direction.add(new Vector3D(-1, 0, 0));
                        break;
                    case KeyboardKeys.KEY_D:
                        direction = direction.add(new Vector3D(1, 0, 0));
                        break;
                    case KeyboardKeys.KEY_Q:
                        c.getParentNode().rotate(new Vector3D(0, 1, 0), -5 * angularSpeed);
                        break;
                    case KeyboardKeys.KEY_E:
                        c.getParentNode().rotate(new Vector3D(0, 1, 0),  5 * angularSpeed);
                        break;
                    case KeyboardKeys.KEY_SPACE:
                        direction = direction.add(new Vector3D(0, 0, 1));
                        break;
                    case KeyboardKeys.KEY_LCONTROL:
                    case KeyboardKeys.KEY_RCONTROL:
                        direction = direction.add(new Vector3D(0, 0, -1));
                        break;
                }
            }

            for (Map.Entry<Byte,Short> ment : client.getMouseAxis().entrySet()) {
                switch (ment.getKey()) {
                    case MouseAxis.MOUSE_AXIS_X:
                        float dx = ment.getValue();
                        if (dx != 0) {
                            dx *= angularSpeed;
                            c.getParentNode().rotate(new Vector3D(0, 0, -1), dx);
                        }
                        break;
                    case MouseAxis.MOUSE_AXIS_Y:
                        float dy = ment.getValue();
                        if (dy != 0) {
                            dy *= angularSpeed;
                            c.getParentNode().rotate(new Vector3D(1, 0, 0), dy);
                        }
                        break;
                }
            }

            client.getMouseAxis().clear();

            if (direction.distance(Vector3D.ZERO) > 0.0f) {
                direction = direction.normalize();
                direction = c.getParentNode().getRotation().applyInverseTo(direction);
                Vector3D diff = new Vector3D(linearSpeed, direction);
                c.getParentNode().translate(diff);
            }
        }
    }

    private RealMatrix calcModelWithParent(Object3D obj) {
        RealMatrix mGroupOutter = obj.getParentNode().getModel();
        RealMatrix mGroupLocal = obj.getModel();
        return mGroupOutter.multiply(mGroupLocal);
    }

    private RealMatrix calcModelWithParentTrans(Object3D obj) {
        RealMatrix mGroupOutter = obj.getParentNode().getTranslationMatrix();
        RealMatrix mGroupLocal = obj.getTranslationMatrix();
        return mGroupOutter.multiply(mGroupLocal);
    }


    private RealMatrix calcModelWithParentRot(Object3D obj) {
        RealMatrix mGroupOutter = obj.getParentNode().getRotationMatrix();
        RealMatrix mGroupLocal = obj.getRotationMatrix();
        return mGroupOutter.multiply(mGroupLocal);
    }

    private RealMatrix calcViewWithParent(Object3D obj) {
        RealMatrix mGroupOutter = obj.getParentNode().getView();
        RealMatrix mGroupLocal = obj.getView();
        return Object3D.makeView(mGroupOutter.multiply(mGroupLocal));
    }

    private float[] flattenMatrix4x4(RealMatrix mat) {
        double[][] data = mat.getData();
        if (data.length != 4 || data[0].length != 4) {
            return null;
        }
        float[] result = new float[16];
        int i = 0;
        for (int y = 0; y < 4; y++) {
            for (int x = 0; x < 4; x++) {
                result[i++] = (float)data[x][y];
            }
        }
        return result;
    }

    private float[] doubleArrToFloatArr(double[] arr) {
        float[] result = new float[arr.length];
        for (int i = 0; i < arr.length; i++) {
            result[i] = (float) arr[i];
        }
        return result;
    }

    @Override
    public SceneStruct getScene(Camera cam) {
        SceneStruct scene = new SceneStruct();
        if (cam.getParentWorld() != this) {
            return scene;
        }

        scene.view = flattenMatrix4x4(calcViewWithParent(cam));

        Set<Light> lights = tree.getRoot().flattenLights(1);

        for (Light l : lights) {
            LightStruct ls = new LightStruct();

            RealMatrix lightModelTrans = calcModelWithParentTrans(l);
            RealMatrix lightModelRot = calcModelWithParentRot(l);
            RealMatrix lightView = calcViewWithParent(l);

            RealVector lightDirectionFour = new ArrayRealVector(4);

            lightDirectionFour.setEntry(0, l.getDirection().getX());
            lightDirectionFour.setEntry(1, l.getDirection().getY());
            lightDirectionFour.setEntry(2, l.getDirection().getZ());
            lightDirectionFour.setEntry(3, 0);

            RealVector resultPosition = lightView.multiply(lightModelTrans).operate(l.getPosition());
            RealVector resultDirectionFour = lightView.multiply(lightModelRot).operate(lightDirectionFour);
            RealVector resultDirection = new ArrayRealVector(3);

            resultDirection.setEntry(0, resultDirectionFour.getEntry(0));
            resultDirection.setEntry(1, resultDirectionFour.getEntry(1));
            resultDirection.setEntry(2, resultDirectionFour.getEntry(2));


            ls.ambient = doubleArrToFloatArr(l.getAmbient().toArray());
            ls.diffuse = doubleArrToFloatArr(l.getDiffuse().toArray());
            ls.direction = doubleArrToFloatArr(resultDirection.toArray());
            ls.position = doubleArrToFloatArr(resultPosition.toArray());
            ls.intensity = doubleArrToFloatArr(l.getIntensity().toArray());
            ls.fadeconstant = l.getFadeConstant();
            ls.fadelinear = l.getFadeLinear();
            ls.fadequadratic = l.getFadeQuadratic();
            ls.modelTrans = flattenMatrix4x4(lightModelTrans);
            ls.modelRot = flattenMatrix4x4(lightModelRot);
            ls.view = flattenMatrix4x4(lightView);
            ls.projection = flattenMatrix4x4(l.calculatePerspective());
            ls.specular = doubleArrToFloatArr(l.getSpecular().toArray());
            ls.spotcut = l.getSpotCut();
            ls.spotexp = l.getSpotExp();
            scene.lights.add(ls);
        }

        Set<Entity> entities = tree.getRoot().flattenEntities(1);
        for (Entity e : entities) {
            if (e == cam.getParentNode().getEntity("monkey")) {
                continue;
            }
            RealMatrix model = calcModelWithParent(e);

            EntityStruct es = new EntityStruct();
            es.modelname = e.getName();
            es.model = flattenMatrix4x4(model);
            es.uniforms.put("ticks", ticks);
            scene.entities.add(es);
        }
        return scene;
    }
}
