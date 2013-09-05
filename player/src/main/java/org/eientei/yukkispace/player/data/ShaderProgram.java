package org.eientei.yukkispace.player.data;

import org.eientei.yukkispace.player.Player;
import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.util.vector.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: iamtakingiteasy
 * Date: 2013-08-03
 * Time: 09:06
 */
public class ShaderProgram {

    Logger logger = LoggerFactory.getLogger(ShaderProgram.class);

    private int id;
    private String log;
    private List<ShaderObject> objects;
    private String formattedLog;
    private Map<String,Integer> uniforms = new HashMap<String, Integer>();

    public ShaderProgram(ShaderObject ... objects) {
        this.objects = Arrays.asList(objects);
    }

    public ShaderProgram(List<ShaderObject> objects) {
        this.objects = objects;
    }

    public void provide(ShaderObject ... objects) {
        this.objects = Arrays.asList(objects);
    }

    public void provide(List<ShaderObject> objects) {
        this.objects = objects;
    }

    public int load() throws LWJGLException {
        if (id != 0) {
            return id;
        }

        Exception ex = null;

        id = GL20.glCreateProgram();

        StringBuilder builder = new StringBuilder();
        builder.append("Program #").append(id).append(":\n");


        for (ShaderObject obj : objects) {
            builder.append("  ");
            if (obj.getType() == GL20.GL_VERTEX_SHADER) {
                builder.append("(VRTX) ");
            } else if (obj.getType() == GL20.GL_FRAGMENT_SHADER) {
                builder.append("(FGMT) ");
            } else {
                builder.append("(????) ");
            }
            builder.append(obj.getSourcePath()).append("\n");
            builder.append("    Status: ");
            try {
                int objId = obj.load();
                GL20.glAttachShader(id, objId);
                builder.append("Compiled (").append(objId).append(")\n");
            } catch (LWJGLException e) {
                builder.append("Failed to compile: \n");
                BufferedReader reader = new BufferedReader(new StringReader(obj.getLog()));
                String line;
                try {
                    while ((line = reader.readLine()) != null) {
                        builder.append("      ").append(line).append("\n");
                    }
                } catch (IOException ignore) {
                }
                ex = e;
            }
        }

        GL20.glBindAttribLocation(id, 0, "in_position");
        GL20.glBindAttribLocation(id, 1, "in_normal");
        GL20.glBindAttribLocation(id, 2, "in_texture");
        GL20.glBindAttribLocation(id, 3, "in_material");

        GL20.glLinkProgram(id);
        GL20.glValidateProgram(id);

        int totalUniforms = GL20.glGetProgrami(id, GL20.GL_ACTIVE_UNIFORMS);
        int uniformMaxLen = GL20.glGetProgrami(id, GL20.GL_ACTIVE_UNIFORM_MAX_LENGTH);

        for (int i = 0; i < totalUniforms; i++) {
            String name  = GL20.glGetActiveUniform(id, i, uniformMaxLen);
            int location = GL20.glGetUniformLocation(id, name);
            if (location > -1) {
                uniforms.put(name, location);
            }
        }

        for (ShaderObject obj : objects) {
            GL20.glDetachShader(id, obj.getId());
        }

        int length = GL20.glGetProgrami(id, GL20.GL_INFO_LOG_LENGTH);

        log = GL20.glGetProgramInfoLog(id, length);
        if (log != null) {
            log = log.trim();
        } else {
            log = "";
        }

        if (!log.isEmpty()) {
            builder.append("  Program log:\n");
            BufferedReader reader = new BufferedReader(new StringReader(log));
            String line;
            try {
                while ((line = reader.readLine()) != null) {
                    builder.append("    ").append(line).append("\n");
                }
            } catch (IOException ignore) {
            }
        }

        formattedLog = builder.toString();

        if ((GL20.glGetProgrami(id, GL20.GL_LINK_STATUS) == GL11.GL_FALSE) || ex != null) {
            throw new LWJGLException("\n" + formattedLog, ex);
        }

        return id;
    }

    public Integer getUniform(String name) {
        return uniforms.get(name);
    }

    public void cleanup() {
        for (ShaderObject obj : objects) {
            obj.cleanup();
        }
        uniforms.clear();
        GL20.glDeleteProgram(id);
        id = 0;
    }

    public int getId() {
        return id;
    }

    public String getLog() {
        return log;
    }

    public String getFormattedLog() {
        return formattedLog;
    }

    public boolean setUniform(String name, Matrix4f value) {
        Integer id = getUniform(name);
        if (id == null) {
            logger.debug("Missing uniform name in porgram #" + this.id + ": " + name);
            return false;
        }
        FloatBuffer fb = BufferUtils.createFloatBuffer(16);
        value.store(fb);
        fb.flip();
        GL20.glUniformMatrix4(id, false, fb);
        return true;
    }

    public boolean setUniform(String name, Matrix3f value) {
        Integer id = getUniform(name);
        if (id == null) {
            logger.debug("Missing uniform name in porgram #" + this.id + ": " + name);
            return false;
        }
        FloatBuffer fb = BufferUtils.createFloatBuffer(9);
        value.store(fb);
        fb.flip();
        GL20.glUniformMatrix3(id, false, fb);
        return true;
    }

    public boolean setUniform(String name, Matrix2f value) {
        Integer id = getUniform(name);
        if (id == null) {
            logger.debug("Missing uniform name in porgram #" + this.id + ": " + name);
            return false;
        }
        FloatBuffer fb = BufferUtils.createFloatBuffer(4);
        value.store(fb);
        fb.flip();
        GL20.glUniformMatrix2(id, false, fb);
        return true;
    }

    public boolean setUniform(String name, Vector4f vec) {
        Integer id = getUniform(name);
        if (id == null) {
            logger.debug("Missing uniform name in porgram #" + this.id + ": " + name);
            return false;
        }
        GL20.glUniform4f(id, vec.x, vec.y, vec.z, vec.w);
        return true;
    }

    public boolean setUniform(String name, Vector3f vec) {
        Integer id = getUniform(name);
        if (id == null) {
            logger.debug("Missing uniform name in porgram #" + this.id + ": " + name);
            return false;
        }
        GL20.glUniform3f(id, vec.x, vec.y, vec.z);
        return true;
    }

    public boolean setUniform(String name, Vector2f vec) {
        Integer id = getUniform(name);
        if (id == null) {
            logger.debug("Missing uniform name in porgram #" + this.id + ": " + name);
            return false;
        }
        GL20.glUniform2f(id, vec.x, vec.y);
        return true;
    }

    public boolean setUniform(String name, float value) {
        Integer id = getUniform(name);
        if (id == null) {
            logger.debug("Missing uniform name in porgram #" + this.id + ": " + name);
            return false;
        }
        GL20.glUniform1f(id, value);
        return true;
    }

    public boolean setUniform(String name, int value) {
        Integer id = getUniform(name);
        if (id == null) {
            logger.debug("Missing uniform name in porgram #" + this.id + ": " + name);
            return false;
        }
        GL20.glUniform1i(id, value);
        return true;
    }


    public void setUniform(String base, Light light, Matrix4f lightMVP) {
        setUniform(base + ".enabled", light.isActive());
        setUniform(base + ".MVP", lightMVP);
        setUniform(base + ".intensity", light.getData().intensity);
        setUniform(base + ".fade_const", light.getData().fadeconstant);
        setUniform(base + ".fade_linear", light.getData().fadelinear);
        setUniform(base + ".fade_quadratic", light.getData().fadequadratic);
        setUniform(base + ".spot_exp", light.getData().spotexp);
        setUniform(base + ".spot_cut", light.getData().spotcut);
        setUniform(base + ".ambient", light.getData().ambient);
        setUniform(base + ".diffuse", light.getData().diffuse);
        setUniform(base + ".specular", light.getData().specular);
        setUniform(base + ".direction", light.getData().direction);
        setUniform(base + ".position", light.getData().position);
    }

    public boolean setUniform(String name, float[] arr) {
        Integer id = getUniform(name);
        if (id == null) {
            logger.debug("Missing uniform name in porgram #" + this.id + ": " + name);
            return false;
        }
        if (arr.length == 2) {
            GL20.glUniform2f(id, arr[0], arr[1]);
        } else if (arr.length == 3) {
            GL20.glUniform3f(id, arr[0], arr[1], arr[2]);
        } else if (arr.length == 4) {
            GL20.glUniform4f(id, arr[0], arr[1], arr[2], arr[3]);
        } else if (arr.length == 16) {
            FloatBuffer fb = BufferUtils.createFloatBuffer(16);
            fb.put(arr);
            fb.flip();
            GL20.glUniformMatrix4(id, false, fb);
        } else {
            return false;
        }
        return true;
    }

    /*
    public boolean setUniform(String base, List<Light> value) {
        for (int i = 0; i < YukkispaceConstants.lights_no; i++) {
            String name = base + "[" + i + "]";
            if (i >= value.size()) {
                setUniform(name + ".enabled", false);
                continue;
            }
            Light v = value.get(i);
            setUniform(name + ".enabled", true);
            setUniform(name + ".fade_const", v.getFadeConst());
            setUniform(name + ".fade_linear", v.getFadeLinear());
            setUniform(name + ".fade_quadratic", v.getFadeQuadratic());

            setUniform(name + ".spot_exp", v.getSpotExp());
            setUniform(name + ".spot_cut", v.getSpotCut());

            setUniform(name + ".position", v.getPosition());
            setUniform(name + ".diffuse", v.getDiffuse());
            setUniform(name + ".specular", v.getSpecular());
            setUniform(name + ".spot_dir", v.getSpotDir());

            setUniform(name + ".MVP", v.getMVP());
            setUniform(name + ".V", v.getView());
            setUniform(name + ".P", v.getProj());
        }
        return true;
    }
    */

    private boolean setUniform(String name, boolean value) {
        Integer id = getUniform(name);
        if (id == null) {
            logger.debug("Missing uniform name in porgram #" + this.id + ": " + name);
            return false;
        }
        GL20.glUniform1i(id, (value) ? 1 : 0);
        return true;
    }

    public void bind() {
        GL20.glUseProgram(id);
    }

    public void unbind() {
        GL20.glUseProgram(0);
    }
}
