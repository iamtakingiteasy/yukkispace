package org.eientei.yukkispace.player.utils;

import org.eientei.yukkispace.player.data.MaterialData;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * User: iamtakingiteasy
 * Date: 2013-08-10
 * Time: 16:31
 */
public class MTLUtils {
    public static void loadMtl(File file, Map<String, Float> materialIndexMap, List<MaterialData> materials) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;

        MaterialData md = null;

        while ((line = reader.readLine()) != null) {
            String[] parts = line.split(" +");
            if (parts[0].equals("#")) {
                continue;
            } else if (parts[0].equals("newmtl")) {
                md = new MaterialData();
                String name = parts[1];
                materialIndexMap.put(name, (float)materials.size());
                materials.add(md);
            } else if (parts[0].equals("Ka")) {
                float r = 0f;
                float g = 0f;
                float b = 0f;

                if (parts.length > 3) {
                    b = Float.parseFloat(parts[3]);
                }
                if (parts.length > 2) {
                    g = Float.parseFloat(parts[2]);
                }
                if (parts.length > 1) {
                    r = Float.parseFloat(parts[1]);
                }

                if (md != null) {
                    md.setAmbient(r, g, b);
                }
            } else if (parts[0].equals("Kd")) {
                float r = 0f;
                float g = 0f;
                float b = 0f;

                if (parts.length > 3) {
                    b = Float.parseFloat(parts[3]);
                }
                if (parts.length > 2) {
                    g = Float.parseFloat(parts[2]);
                }
                if (parts.length > 1) {
                    r = Float.parseFloat(parts[1]);
                }

                if (md != null) {
                    md.setDiffuse(r, g, b);
                }
            } else if (parts[0].equals("Ks")) {
                float r = 0f;
                float g = 0f;
                float b = 0f;

                if (parts.length > 3) {
                    b = Float.parseFloat(parts[3]);
                }
                if (parts.length > 2) {
                    g = Float.parseFloat(parts[2]);
                }
                if (parts.length > 1) {
                    r = Float.parseFloat(parts[1]);
                }

                if (md != null) {
                    md.setSpecular(r, g, b);
                }
            } else if (parts[0].equals("Ns")) {
                float coeff = 10.0f;
                if (parts.length > 1) {
                    coeff = Float.parseFloat(parts[1]);
                }
                if (md != null) {
                    md.setSpecularCoeff(coeff);
                }
            } else if (parts[0].equals("d") || parts[0].equals("Tr")) {
                float dissolve = 1.0f;
                if (parts.length > 1) {
                    dissolve = Float.parseFloat(parts[1]);
                }
                if (md != null) {
                    md.setDissolve(dissolve);
                }
            }
        }
    }
}
