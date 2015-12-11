package com.mygdx.game.customlib;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.EdgeShape;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.mygdx.game.customlib.util.Geometry;

/**
 * Created by alfmagne1 on 07/12/15.
 */
public class Box2DDebug {


    public static void createPolygon(World world, PolygonShape shape, Vector2 atPosition) {

        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        Body body = world.createBody(bodyDef);
        body.createFixture(shape, 1);
        body.setTransform(atPosition, 0);
    }

    public static void createPolygon(World world, Vector2[] vertices, Vector2 atPosition) {

        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        Body body = world.createBody(bodyDef);

        PolygonShape shape = new PolygonShape();
        shape.set(vertices);

        body.createFixture(shape, 1);
        body.setTransform(atPosition, 0);

        shape.dispose();
    }



    public static void createCross(World world, Vector2 atOrigin, float size){


        Vector2 from = new Vector2(atOrigin.x - size, atOrigin.y);
        Vector2 to = new Vector2(atOrigin.x + size, atOrigin.y);
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        Body body = world.createBody(bodyDef);

        EdgeShape edgeShape = new EdgeShape();
        edgeShape.set(from, to);
        body.createFixture(edgeShape, 1);

        from = new Vector2(atOrigin.x, atOrigin.y - size);
        to = new Vector2(atOrigin.x, atOrigin.y + size);
        edgeShape = new EdgeShape();
        edgeShape.set(from, to);
        body.createFixture(edgeShape, 1);

        edgeShape.dispose();


    }

    public static void createLineSegment(World world, Vector2 from, Vector2 to) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        Body body = world.createBody(bodyDef);

        EdgeShape edgeShape = new EdgeShape();
        edgeShape.set(from, to);
        body.createFixture(edgeShape, 1);
    }

    public static void createCircle(World world, Vector2[] points){
        for(Vector2 point : points){
            createCircle(world, point, 0.4f);
        }
    }


    public static void createCircle(World world, Vector2 atOrigin) {
        createCircle(world, atOrigin, 0.1f);
    }

    public static Body createCircle(World world, Vector2 atOrigin, float radius){

        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        Body body = world.createBody(bodyDef);

        CircleShape shape = new CircleShape();
        shape.setRadius(radius);
        shape.setPosition(new Vector2(radius / 2, radius / 2));

        body.createFixture(shape, 1);
        body.setTransform(atOrigin, 0);

        return body;

    }


    public static void logVertices(Vector2[] vertices, String tag){
        logVertices(Geometry.toFloats(vertices), tag);
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
