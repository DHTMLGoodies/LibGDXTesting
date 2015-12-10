package com.mygdx.game.customlib.util;

import com.badlogic.gdx.Gdx;
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

    public static void logVertices(Vector2[] vertices, String tag){
        logVertices(toFloats(vertices), tag);
    }

    public static void logVertices(float[] vertices, String tag){
        for(int i=0;i<vertices.length; i+=2){
            Gdx.app.log(tag, tag + " " + vertices[i] + "x" + vertices[i+1]);
        }
    }

    public static void log(float value){
        Gdx.app.log("float", "float: " + value);
    }

    public static void log(Vector2 vector2,  String tag){
        Gdx.app.log(tag, tag + ": " + vector2);
    }
}
