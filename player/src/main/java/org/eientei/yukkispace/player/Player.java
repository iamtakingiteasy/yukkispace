package org.eientei.yukkispace.player;

import org.eientei.yukkispace.player.data.Entity;
import org.eientei.yukkispace.player.data.Light;
import org.eientei.yukkispace.player.data.ShaderObject;
import org.eientei.yukkispace.player.data.ShaderProgram;
import org.eientei.yukkispace.protocol.input.*;
import org.eientei.yukkispace.protocol.world.EntityStruct;
import org.eientei.yukkispace.protocol.world.LightStruct;
import org.eientei.yukkispace.protocol.world.SceneStruct;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.*;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import java.io.File;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * User: iamtakingiteasy
 * Date: 2013-08-20
 * Time: 14:00
 */
public class Player {
    private final File assetsDir = new File("data");
    private final Light[] lights = new Light[8];
    private final static String WINDOW_TITLE = "Yukkispace";
    private int width = 800;
    private int height = 600;
    private float fov = 60.0f;
    private Queue<SceneStruct> frames = new ArrayBlockingQueue<SceneStruct>(4);
    private Map<String, Entity> registry = new HashMap<String, Entity>();
    private SceneStruct scene = new SceneStruct();
    private Matrix4f projectionMatrix;
    private ShaderProgram depthProgram;
    private boolean okToGo = true;
    private Controller controller;

    public Player() throws LWJGLException {
        PixelFormat pixelFormat = new PixelFormat();
        ContextAttribs contextAttribs = new ContextAttribs();

        try {
            Display.setDisplayMode(new DisplayMode(width, height));
            Display.create(pixelFormat, contextAttribs);
        } catch (LWJGLException ignore) {
        }
        Display.setTitle(WINDOW_TITLE);

        //Mouse.setGrabbed(true);

        GL11.glClearColor(0.2f, 0.2f, 0.2f, 1.0f);
        GL11.glViewport(0, 0, width, height);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthFunc(GL11.GL_LEQUAL);
        GL11.glClearDepth(1.0f);

        GL11.glShadeModel(GL11.GL_SMOOTH);

        for (int i = 0; i < lights.length; i++) {
            lights[i] = new Light();
        }

        File stock = new File(assetsDir, "stock");

        ShaderObject vertexShader = new ShaderObject(new File(stock, "depth.vert"), GL20.GL_VERTEX_SHADER);
        ShaderObject fragmentShader = new ShaderObject(new File(stock, "depth.frag"), GL20.GL_FRAGMENT_SHADER);

        depthProgram = new ShaderProgram(vertexShader, fragmentShader);
        depthProgram.load();
    }

    public void pushScene(SceneStruct scene) {
        frames.offer(scene);
    }

    public static void main(String[] args) throws LWJGLException {
        Player player = new Player();
        Controller controller = new Controller(args, player);
        controller.start();
        player.setController(controller);
        player.run();
    }

    private void updateProjection() {
        projectionMatrix = new Matrix4f();
        float aspect = (float) width / height;
        float far = 100.0f;
        float near = 0.1f;
        float y_scale = 1.0f / (float)Math.tan(Math.toRadians(fov / 2.0f));
        float x_scale = y_scale / aspect;
        float frustum = far - near;

        projectionMatrix.m00 = x_scale;
        projectionMatrix.m11 = y_scale;
        projectionMatrix.m22 = -((far + near) / frustum);
        projectionMatrix.m23 = -1.0f;
        projectionMatrix.m32 = -((2.0f * near * far) / frustum);
        projectionMatrix.m33 = 0.0f;
    }

    public static Matrix4f mat4FromArr(float[] arr) {
        Matrix4f mat = new Matrix4f();
        mat.load(FloatBuffer.wrap(arr));
        return mat;
    }

    public static Vector4f vec4FromArr(float[] arr) {
        return new Vector4f(arr[0], arr[1], arr[2], arr[3]);
    }

    public static Vector3f vec3FromArr(float[] arr) {
        return new Vector3f(arr[0], arr[1], arr[2]);
    }


    public void stopPlayer() {
        okToGo = false;
    }

    public void run() {
        while (!Display.isCloseRequested() && okToGo) {
            pollInput();
            try {
                step();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (LWJGLException e) {
                e.printStackTrace();
            }
            Display.sync(60);
            Display.update();
        }
        Display.destroy();
        controller.disconnect("Quit!");
        controller.stopPlayer();
    }

    private void step() throws IOException, LWJGLException {
        if (!frames.isEmpty()) {
            scene = frames.remove();
        }

        Matrix4f cameraView = mat4FromArr(scene.view);

        // ensure load-up entities
        for (EntityStruct es : scene.entities) {
            if (!registry.containsKey(es.modelname)) {
                Entity entity = new Entity(es.modelname, assetsDir);
                if (entity.isLoaded()) {
                    registry.put(es.modelname, entity);
                } else {
                    System.out.println("Failed to load: " + es.modelname);
                }
            }
        }

        updateProjection();

        // render to the depth
        int totalLights = scene.lights.size();
        for (int i = 0; i < lights.length; i++) {
            if (i < totalLights) {
                lights[i].activate();
                lights[i].setData(scene.lights.get(i));
                Matrix4f lightView = mat4FromArr(scene.lights.get(i).view);
                Matrix4f lightProj = mat4FromArr(scene.lights.get(i).projection);

                GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, lights[i].getFramebufferObjectId());
                GL11.glViewport(0, 0, lights[i].getDepthmapSideSize(), lights[i].getDepthmapSideSize());
                GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

                for (EntityStruct es : scene.entities) {
                    Entity entity = registry.get(es.modelname);
                    if (entity == null) {
                        continue;
                    }
                    Matrix4f entityModel = mat4FromArr(es.model);

                    entity.renderDepth(entityModel, lightView, lightProj, depthProgram, es.uniforms);
                }

                GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
            } else {
                lights[i].deactivate();
            }
        }


        // render to the screeen
        GL11.glViewport(0, 0, width, height);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        for (EntityStruct es : scene.entities) {
            Entity entity = registry.get(es.modelname);
            if (entity == null) {
                continue;
            }
            Matrix4f entityModel = mat4FromArr(es.model);

            entity.render(entityModel, cameraView, projectionMatrix, lights, es.uniforms);
        }
    }

    private void pollInput() {
        List<KeyboardKeyStruct> keys = new ArrayList<KeyboardKeyStruct>();
        while (Keyboard.next()) {
            KeyboardKeyStruct key = new KeyboardKeyStruct();
            key.key = (byte)(Keyboard.getEventKey() & 0xFF);
            if (Keyboard.getEventKeyState()) {
                key.state = KeyboardState.KEYSTATE_PRESSED;
            } else {
                key.state = KeyboardState.KEYSTATE_RELEASED;
            }
            if (key.key == KeyboardKeys.KEY_ESCAPE) {
                Mouse.setGrabbed(false);
            }
            keys.add(key);
        }
        if (!keys.isEmpty()) {
            controller.enqueueKeys(keys);
        }

        List<MouseAxisStruct> maxis = new ArrayList<MouseAxisStruct>();

        while (Mouse.next()) {
            int button = Mouse.getEventButton();
            if (Mouse.isGrabbed()) {
                short dx = (short)Mouse.getDX();
                short dy = (short)Mouse.getDY();

                if (dx != 0) {
                    MouseAxisStruct maxi = new MouseAxisStruct();
                    maxi.axis = MouseAxis.MOUSE_AXIS_X;
                    maxi.value = dx;
                    maxis.add(maxi);
                }

                if (dy != 0) {
                    MouseAxisStruct maxi = new MouseAxisStruct();
                    maxi.axis = MouseAxis.MOUSE_AXIS_Y;
                    maxi.value = dy;
                    maxis.add(maxi);
                }
            }
            if (button != -1) {
                Mouse.setGrabbed(true);
            }
        }

        if (!maxis.isEmpty()) {
            controller.enqueueMAxis(maxis);
        }
    }

    public void setController(Controller controller) {
        this.controller = controller;
    }

    public Controller getController() {
        return controller;
    }
}
