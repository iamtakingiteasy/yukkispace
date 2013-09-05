package org.eientei.yukkispace.player.data;

import org.eientei.yukkispace.player.Player;
import org.eientei.yukkispace.player.utils.OBJUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.util.vector.Matrix3f;
import org.lwjgl.util.vector.Matrix4f;

import java.io.File;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.Map;

/**
 * User: iamtakingiteasy
 * Date: 2013-08-20
 * Time: 15:08
 */
public class Entity {
    private String name;
    private File assetsDir;
    private ShaderProgram shaderProgram;
    private int vbo;
    private int vbi;
    private int vbiSize;
    private int materialTexture;
    private float materialTextureSize;
    private int diffuseTexture;
    private boolean loaded;
    private static Matrix4f biasMatrix = new Matrix4f();

    static {
        float[] bias = new float[] {
                0.5f, 0.0f, 0.0f, 0.0f,
                0.0f, 0.5f, 0.0f, 0.0f,
                0.0f, 0.0f, 0.5f, 0.0f,
                0.5f, 0.5f, 0.5f, 1.0f
        };

        biasMatrix.load(FloatBuffer.wrap(bias));
    }

    public Entity(String name, File assetsDir) throws IOException, LWJGLException {
        this.name = name;
        this.assetsDir = assetsDir;

        File asset = new File(assetsDir, name);
        if (asset.exists()) {
            loadFromDir(asset);
        }
    }

    public void loadFromDir(File asset) throws IOException, LWJGLException {
        File geometry = new File(asset, name + ".obj");
        if (!geometry.exists()) {
            throw new IOException(geometry.getAbsolutePath() + " do not exists!");
        }
        OBJUtils.loadObj(geometry, this);
        File vertexShaderFile = new File(asset, name + ".vert");
        File fragmentShaderFile = new File(asset, name + ".frag");
        if (!vertexShaderFile.exists()) {
            throw new IOException(vertexShaderFile.getAbsolutePath() + " do not exists!");
        }
        if (!fragmentShaderFile.exists()) {
            throw new IOException(fragmentShaderFile.getAbsolutePath() + " do not exists!");
        }

        ShaderObject vertexShader = new ShaderObject(vertexShaderFile, GL20.GL_VERTEX_SHADER);
        ShaderObject fragmentShader = new ShaderObject(fragmentShaderFile, GL20.GL_FRAGMENT_SHADER);

        shaderProgram = new ShaderProgram(vertexShader, fragmentShader);
        shaderProgram.load();
        loaded = true;
    }

    public void renderDepth(Matrix4f model, Matrix4f view, Matrix4f proj, ShaderProgram depthProgram, Map<String, Float> uniforms) {
        GL11.glCullFace(GL11.GL_FRONT);
        Matrix4f modelView = Matrix4f.mul(view, model, new Matrix4f());
        Matrix4f mvp = Matrix4f.mul(proj, modelView, new Matrix4f());

        depthProgram.bind();
        depthProgram.setUniform("MVP", mvp);

        for (Map.Entry<String, Float> en : uniforms.entrySet()) {
            depthProgram.setUniform(en.getKey(), en.getValue());
        }

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, vbi);

        GL20.glEnableVertexAttribArray(0);

        GL20.glVertexAttribPointer(0, VertexData.xyzw_count, GL11.GL_FLOAT, false, VertexData.total_size, VertexData.xyzw_offset);

        GL11.glDrawElements(GL11.GL_TRIANGLES, vbiSize, GL11.GL_UNSIGNED_INT, 0);

        GL20.glDisableVertexAttribArray(0);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);

        depthProgram.unbind();
    }

    public void render(Matrix4f model, Matrix4f view, Matrix4f proj, Light[] lights, Map<String,Float> uniforms) {
        GL11.glCullFace(GL11.GL_BACK);
        shaderProgram.bind();

        Matrix4f modelView = Matrix4f.mul(view, model, new Matrix4f());
        Matrix4f mvp = Matrix4f.mul(proj, modelView, new Matrix4f());
        Matrix3f viewInverse = new Matrix3f();

        viewInverse.m00 = modelView.m00; viewInverse.m01 = modelView.m01; viewInverse.m02 = modelView.m02;
        viewInverse.m10 = modelView.m10; viewInverse.m11 = modelView.m11; viewInverse.m12 = modelView.m12;
        viewInverse.m20 = modelView.m20; viewInverse.m21 = modelView.m21; viewInverse.m22 = modelView.m22;

        viewInverse.invert();
        viewInverse.transpose();

        shaderProgram.setUniform("MVP", mvp);
        shaderProgram.setUniform("M", model);
        shaderProgram.setUniform("V", view);
        shaderProgram.setUniform("P", proj);
        shaderProgram.setUniform("VI", viewInverse);
        shaderProgram.setUniform("texstep", 1.0f/(materialTextureSize * 4));
        shaderProgram.setUniform("texoff",  1.0f/materialTextureSize);
        shaderProgram.setUniform("texels", materialTextureSize);

        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glEnable(GL11.GL_TEXTURE_1D);
        GL11.glBindTexture(GL11.GL_TEXTURE_1D, materialTexture);
        shaderProgram.setUniform("tex_materials", 0);

        for (int i = 0; i < lights.length; i++) {
            if (!lights[i].isActive()) {
                 break;
            }
            Matrix4f lightModelView = Matrix4f.mul(Player.mat4FromArr(lights[i].getData().view), model, new Matrix4f());
            Matrix4f lightMVP = Matrix4f.mul(Player.mat4FromArr(lights[i].getData().projection), lightModelView, new Matrix4f());
            Matrix4f biasedMVP = Matrix4f.mul(biasMatrix, lightMVP, new Matrix4f());

            GL13.glActiveTexture(GL13.GL_TEXTURE1 + i);
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, lights[i].getDepthmapTextureId());
            shaderProgram.setUniform("tex_depthmap" + (i+1), i+1);
            shaderProgram.setUniform("lights[" + i + "]", lights[i], biasedMVP);
        }

        for (Map.Entry<String, Float> en : uniforms.entrySet()) {
            shaderProgram.setUniform(en.getKey(), en.getValue());
        }

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, vbi);

        GL20.glEnableVertexAttribArray(0);
        GL20.glEnableVertexAttribArray(1);
        GL20.glEnableVertexAttribArray(2);
        GL20.glEnableVertexAttribArray(3);

        GL20.glVertexAttribPointer(0, VertexData.xyzw_count, GL11.GL_FLOAT, false, VertexData.total_size, VertexData.xyzw_offset);
        GL20.glVertexAttribPointer(1, VertexData.norm_count, GL11.GL_FLOAT, false, VertexData.total_size, VertexData.norm_offset);
        GL20.glVertexAttribPointer(2, VertexData.texture_count, GL11.GL_FLOAT, false, VertexData.total_size, VertexData.texture_offset);
        GL20.glVertexAttribPointer(3, VertexData.material_count, GL11.GL_FLOAT, false, VertexData.total_size, VertexData.material_offset);

        GL11.glDrawElements(GL11.GL_TRIANGLES, vbiSize, GL11.GL_UNSIGNED_INT, 0);

        GL20.glDisableVertexAttribArray(3);
        GL20.glDisableVertexAttribArray(2);
        GL20.glDisableVertexAttribArray(1);
        GL20.glDisableVertexAttribArray(0);


        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);

        GL11.glBindTexture(GL11.GL_TEXTURE_1D, 0);
        shaderProgram.unbind();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getVbo() {
        return vbo;
    }

    public void setVbo(int vbo) {
        this.vbo = vbo;
    }

    public int getVbi() {
        return vbi;
    }

    public void setVbi(int vbi) {
        this.vbi = vbi;
    }

    public int getVbiSize() {
        return vbiSize;
    }

    public void setVbiSize(int vbiSize) {
        this.vbiSize = vbiSize;
    }

    public ShaderProgram getShaderProgram() {
        return shaderProgram;
    }

    public void setShaderProgram(ShaderProgram shaderProgram) {
        this.shaderProgram = shaderProgram;
    }

    public int getMaterialTexture() {
        return materialTexture;
    }

    public void setMaterialTexture(int materialTexture) {
        this.materialTexture = materialTexture;
    }

    public int getDiffuseTexture() {
        return diffuseTexture;
    }

    public void setDiffuseTexture(int diffuseTexture) {
        this.diffuseTexture = diffuseTexture;
    }

    public float getMaterialTextureSize() {
        return materialTextureSize;
    }

    public void setMaterialTextureSize(float materialTextureSize) {
        this.materialTextureSize = materialTextureSize;
    }

    public boolean isLoaded() {
        return loaded;
    }
}
