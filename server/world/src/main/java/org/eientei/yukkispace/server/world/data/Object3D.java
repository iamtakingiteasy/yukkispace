package org.eientei.yukkispace.server.world.data;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.linear.*;

/**
 * User: iamtakingiteasy
 * Date: 2013-08-20
 * Time: 22:11
 */
public class Object3D {
    private String name;
    private World parentWorld;
    private WorldTree.Node parentNode;
    private RealVector position = new ArrayRealVector(new double[] { 0, 0, 0, 1 });
    private Rotation rotation = Rotation.IDENTITY;

    public Object3D(String name) {
        this.name = name;
    }

    public RealMatrix calculatePerspective(float aspect, float fov, float near, float far) {
        RealMatrix mat = MatrixUtils.createRealIdentityMatrix(4);

        float y_scale = 1.0f / (float)Math.tan(Math.toRadians(fov / 2.0f));
        float x_scale = y_scale / aspect;
        float frustum = far - near;

        mat.setEntry(0, 0, x_scale);
        mat.setEntry(1, 1, y_scale);
        mat.setEntry(2, 2, -((far + near) / frustum));
        mat.setEntry(3, 2, -1.0f);
        mat.setEntry(2, 3, -((2.0f * near * far) / frustum));
        mat.setEntry(3, 3, 0);

        return mat;
    }

    public static RealMatrix rhs2lhs = MatrixUtils.createRealMatrix(new double[][] {
            { 1.0f, 0.0f, 0.0f, 0.0f },
            { 0.0f, 0.0f, 1.0f, 0.0f },
            { 0.0f,-1.0f, 0.0f, 0.0f },
            { 0.0f, 0.0f, 0.0f, 1.0f }
    });

    public static RealMatrix lhs2rhs = MatrixUtils.createRealMatrix(new double[][] {
            { 1.0f, 0.0f, 0.0f, 0.0f },
            { 0.0f, 0.0f,-1.0f, 0.0f },
            { 0.0f, 1.0f, 0.0f, 0.0f },
            { 0.0f, 0.0f, 0.0f, 1.0f }
    });

    public void translate(Vector3D diff) {
        position.addToEntry(0, diff.getX());
        position.addToEntry(1, diff.getY());
        position.addToEntry(2, diff.getZ());
    }

    public void rotate(Vector3D axis, float degrees) {
        rotation = new Rotation(axis, Math.toRadians(degrees)).applyInverseTo(rotation);
    }

    public RealMatrix getRotationMatrix() {
        RealMatrix mat = MatrixUtils.createRealIdentityMatrix(4);
        double[][] rotmat = rotation.getMatrix();
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                mat.setEntry(y, x, rotmat[x][y]);
            }
        }
        return mat;
    }

    public RealMatrix getTranslationMatrix() {
        RealMatrix mat = MatrixUtils.createRealIdentityMatrix(4);
        mat.setEntry(0, 3, position.getEntry(0));
        mat.setEntry(1, 3, position.getEntry(1));
        mat.setEntry(2, 3, position.getEntry(2));
        return mat;
    }


    public void setPosition(Vector3D position, double w) {
        this.position.setEntry(0, position.getX());
        this.position.setEntry(1, position.getY());
        this.position.setEntry(2, position.getZ());
        this.position.setEntry(3, w);
    }

    public void setPosition(Vector3D position) {
        setPosition(position, 1);
    }

    public void setRotation(Rotation rotation) {
        this.rotation = rotation;
    }

    public RealVector getPosition() {
        return position;
    }

    public Rotation getRotation() {
        return rotation;
    }

    public RealMatrix getModel() {
        RealMatrix rot = getRotationMatrix();
        RealMatrix trans = getTranslationMatrix();

        return rot.multiply(trans);
    }


    public RealMatrix getView() {
        RealMatrix rot = getRotationMatrix();
        RealMatrix trans = getTranslationMatrix();

        return trans.multiply(rot);
    }

    public static RealMatrix makeView(RealMatrix model) {
        return rhs2lhs.multiply(new LUDecomposition(model).getSolver().getInverse());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public World getParentWorld() {
        return parentWorld;
    }

    public void setParentWorld(World parentWorld) {
        this.parentWorld = parentWorld;
    }

    public WorldTree.Node getParentNode() {
        return parentNode;
    }

    public void setParentNode(WorldTree.Node parentNode) {
        this.parentNode = parentNode;
    }
}

