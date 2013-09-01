package org.eientei.yukkispace.player.data;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * User: iamtakingiteasy
 * Date: 2013-08-03
 * Time: 08:48
 */
public class ShaderObject {
    private final File sourcePath;
    private String log;
    private int id;
    private int type;

    public ShaderObject(File source, int type) {
        this.type = type;
        this.sourcePath = source;
    }

    public int load() throws LWJGLException {
        if (id != 0) {
            return id;
        }
        StringBuilder builder = new StringBuilder();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(sourcePath));
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line).append("\n");
            }
            reader.close();
        } catch (IOException ignore) {
            System.out.println("Failure loading shader on path: " + sourcePath);
            System.exit(-1);
        }

        id = GL20.glCreateShader(type);
        GL20.glShaderSource(id, builder);
        GL20.glCompileShader(id);

        int length = GL20.glGetShaderi(id, GL20.GL_INFO_LOG_LENGTH);

        log = GL20.glGetShaderInfoLog(id, length);
        if (log != null) {
            log = log.trim();
        } else {
            log = "";
        }

        if (GL20.glGetShaderi(id, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
            throw new LWJGLException("Error compiling shader: " + sourcePath);
        }

        return id;
    }

    public String getLog() {
        return log;
    }

    public int getId() {
        return id;
    }

    public File getSourcePath() {
        return sourcePath;
    }

    public void cleanup() {
        GL20.glDeleteShader(id);
        id = 0;
    }

    public int getType() {
        return type;
    }
}
