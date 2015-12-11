package com.mygdx.game.customlib.util;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

/**
 * Created by alfmagne1 on 09/12/15.
 */
public class Geometry {

    public static float[] toFloats(Vector2[] vertices){
        float[] floats = new float[vertices.length * 2];
        int index = 0;
        for(Vector2 vertex : vertices){
            floats[index++] = vertex.x;
            floats[index++] = vertex.y;
        }
        return floats;
    }

    public static Vector2[] rotateVertices(Vector2[] vertices, Vector2 center, float angle){

        Vector2[] result = new Vector2[vertices.length];

        for(int i=0;i<vertices.length;i++){
            result[i] = rotateVector2(vertices[i], center, angle);
        }

        return result;

    }

    public static Vector2 rotateVector2(Vector2 pt, Vector2 center, float angle) {

        float cosAngle = MathUtils.cos(angle);
        float sinAngle = MathUtils.sin(angle);
        float dx = (pt.x - center.x);
        float dy = (pt.y - center.y);

        pt.x = center.x + (int) (dx * cosAngle - dy * sinAngle);
        pt.y = center.y + (int) (dx * sinAngle + dy * cosAngle);
        return pt;

    }

}
