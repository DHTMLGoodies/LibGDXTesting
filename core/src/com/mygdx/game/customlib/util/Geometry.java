package com.mygdx.game.customlib.util;

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
}
