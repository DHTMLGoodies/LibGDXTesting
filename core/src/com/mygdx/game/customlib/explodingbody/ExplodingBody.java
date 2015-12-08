package com.mygdx.game.customlib.explodingbody;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.PolygonRegion;
import com.badlogic.gdx.graphics.g2d.PolygonSprite;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.EarClippingTriangulator;
import com.badlogic.gdx.math.GeometryUtils;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.RayCastCallback;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.mygdx.game.customlib.bodyproperties.BodyProperties;


/**
 * Created by alfmagne1 on 06/12/15.
 */
public class ExplodingBody implements RayCastCallback {

    private static final int CHUNKS = 1;

    private Array<Body> affectedBodies = new Array<>();

    private Array<Body> affectedByLaser;
    private Array<Vector2> entryPoint = new Array<>();

    private World mWorld;

    private boolean debugMode = false;

    private Vector2 explosionCenter;
    private float explosionRadius;

    private ObjectMap<Body, PolygonSprite> mSprites;


    private Vector2 bodyMinOriginal;

    private Rectangle boundingRectangle;

    public ExplodingBody() {
        mSprites = new ObjectMap<>();
    }

    public void explode(World world, Body body) {

        mWorld = world;
        explosionCenter = body.getPosition();
        explosionRadius = 5;
        float worldScale = 1;
        affectedBodies.add(body);

        bodyMinOriginal = getBodyMin(body);
        if (body.getUserData() != null && body.getUserData() instanceof BodyProperties) {
            Gdx.app.log("body", body.getUserData().toString());
        }


        for (int i = 0; i < CHUNKS; i++) {
            float cutAngle = MathUtils.random() * MathUtils.PI2;
            float offset = 10;

            Vector2 p1 = new Vector2((explosionCenter.x + (float) i / 1000 - offset * MathUtils.cos(cutAngle)) / worldScale, (explosionCenter.y - offset * MathUtils.sin(cutAngle)) / worldScale);
            Vector2 p2 = new Vector2((explosionCenter.x + (offset * MathUtils.cos(cutAngle))) / worldScale, (explosionCenter.y + (offset * MathUtils.sin(cutAngle))) / worldScale);

            //DebugShapes.createLineSegment(mWorld, p1, p2);

            affectedByLaser = new Array<>();
            entryPoint = new Array<>();

            world.rayCast(this, p1, p2);
            world.rayCast(this, p2, p1);
        }
    }

    @Override
    public float reportRayFixture(Fixture fixture, Vector2 point, Vector2 normal, float fraction) {

        Body affectedBody = fixture.getBody();

        if (affectedBodies.contains(affectedBody, true) && fixture.getShape() instanceof PolygonShape) {

            if (this.debugMode) Gdx.app.log("received", "received point " + point);

            PolygonShape affectedPolygon = (PolygonShape) fixture.getShape();
            int fixtureIndex = affectedByLaser.indexOf(affectedBody, true);

            if (fixtureIndex == -1) {
                affectedByLaser.add(affectedBody);
                entryPoint.add(new Vector2(point.x, point.y));
            } else {
                Vector2 rayCenter = new Vector2((point.x + entryPoint.get(fixtureIndex).x) / 2, (point.y + entryPoint.get(fixtureIndex).y) / 2);
                float rayAngle = MathUtils.atan2(entryPoint.get(fixtureIndex).y - point.y, entryPoint.get(fixtureIndex).x - point.x);
                Array<Vector2> polyVertices = new Array<>();
                int vertexCount = affectedPolygon.getVertexCount();
                Vector2 currentVertex = new Vector2();
                for (int i = 0; i < vertexCount; i++) {
                    affectedPolygon.getVertex(i, currentVertex);
                    polyVertices.add(new Vector2(currentVertex.x, currentVertex.y));
                }

                Array<Vector2> newPolyVertices1 = new Array<>();
                Array<Vector2> newPolyVertices2 = new Array<>();

                if (this.debugMode) Gdx.app.log("vertices", "Count: " + polyVertices.size);
                int currentPoly = 0;
                boolean cutPlaced1 = false;
                boolean cutPlaced2 = false;

                for (int i = 0; i < polyVertices.size; i++) {

                    Vector2 worldPoint = affectedBody.getWorldPoint(polyVertices.get(i));

                    float cutAngle = MathUtils.atan2(worldPoint.y - rayCenter.y, worldPoint.x - rayCenter.x) - rayAngle;
                    if (cutAngle < MathUtils.PI * -1) {
                        cutAngle += MathUtils.PI2;
                    }

                    if (cutAngle > 0 && cutAngle <= MathUtils.PI) {
                        if (currentPoly == 2) {
                            cutPlaced1 = true;
                            newPolyVertices1.add(new Vector2(point.x, point.y));
                            newPolyVertices1.add(new Vector2(entryPoint.get(fixtureIndex).x, entryPoint.get(fixtureIndex).y));
                        }
                        newPolyVertices1.add(new Vector2(worldPoint.x, worldPoint.y));
                        currentPoly = 1;
                    } else {
                        if (currentPoly == 1) {
                            cutPlaced2 = true;
                            newPolyVertices2.add(new Vector2(entryPoint.get(fixtureIndex).x, entryPoint.get(fixtureIndex).y));
                            newPolyVertices2.add(new Vector2(point.x, point.y));
                        }
                        newPolyVertices2.add(new Vector2(worldPoint.x, worldPoint.y));
                        currentPoly = 2;

                    }
                }

                if (!cutPlaced1) {
                    newPolyVertices1.add(new Vector2(point.x, point.y));
                    newPolyVertices1.add(new Vector2(entryPoint.get(fixtureIndex).x, entryPoint.get(fixtureIndex).y));
                }
                if (!cutPlaced2) {
                    newPolyVertices2.add(new Vector2(entryPoint.get(fixtureIndex).x, entryPoint.get(fixtureIndex).y));
                    newPolyVertices2.add(new Vector2(point.x, point.y));
                }

                Vector2[] vertices1 = new Vector2[newPolyVertices1.size];
                for (int i = 0; i < newPolyVertices1.size; i++) {
                    vertices1[i] = newPolyVertices1.get(i);
                }
                Vector2[] vertices2 = new Vector2[newPolyVertices2.size];
                for (int i = 0; i < newPolyVertices2.size; i++) {
                    vertices2[i] = newPolyVertices2.get(i);
                }

                createSlice(vertices1, affectedBody);
                createSlice(vertices2, affectedBody);
                destroyBody(affectedBody);
            }
        }
        return 1;
    }

    private void destroyBody(Body body) {
        mSprites.remove(body);
        mWorld.destroyBody(body);
    }

    private boolean logged = false;

    public void update(float delta) {

        for (Body body : mSprites.keys()) {

            PolygonSprite sprite = mSprites.get(body);

            sprite.setRotation(MathUtils.radiansToDegrees * body.getAngle());
            sprite.setPosition(
                    body.getPosition().x,
                    body.getPosition().y
            );

            if (!logged) {
                Gdx.app.log("body pos", "pos: " + body.getPosition().x + "x" + body.getPosition().y);
                Gdx.app.log("sprite size", "size: " + sprite.getWidth() + "x" + sprite.getHeight() + ", " + sprite.getScaleX() + "x" + sprite.getScaleY());
                logged = true;
            }
        }
    }

    public void draw(PolygonSpriteBatch spriteBatch) {
        for (PolygonSprite sprite : mSprites.values()) {
            sprite.draw(spriteBatch);
        }
    }

    private Vector2 getBodyMin(Body body) {
        Fixture fixture = body.getFixtureList().get(0);
        PolygonShape shape = (PolygonShape) fixture.getShape();
        Float minX = null;
        Float minY = null;
        Vector2 vector2 = new Vector2();
        for (int i = 0, len = shape.getVertexCount(); i < len; i++) {
            shape.getVertex(i, vector2);
            if (minX == null) minX = vector2.x;
            else minX = Math.min(minX, vector2.x);
            if (minY == null) minY = vector2.y;
            else minY = Math.min(minY, vector2.y);
        }
        Vector2 pos = body.getPosition();
        return new Vector2(pos.x + minX, pos.y + minY);
    }

    private Vector2 getVertexMin(Vector2[] vertices) {
        Vector2 min = null;

        for (Vector2 vertex : vertices) {
            if (min == null) min = new Vector2(vertex);
            else {
                min.x = Math.min(min.x, vertex.x);
                min.y = Math.min(min.y, vertex.y);
            }
        }

        return min;

    }

    private void addSprite(Vector2[] vertexPairs, Body newBody, Body originalBody, Vector2 centre) {


        Object userData = originalBody.getUserData();
        if (userData == null || !(userData instanceof BodyProperties)) return;

        BodyProperties properties = (BodyProperties) originalBody.getUserData();
        if (properties.mSprite != null) {
            Texture texture = properties.mSprite.getTexture();

            float[] vertices = new float[vertexPairs.length * 2];
            float[] plainVertices = new float[vertexPairs.length * 2];
            float scaleX = properties.mSprite.getScaleX();
            float scaleY = properties.mSprite.getScaleY();
            int index = 0;

            Vector2 bodyPos = getBodyMin(newBody).sub(bodyMinOriginal).sub(getVertexMin(vertexPairs));
            //Vector2 minOffset = getVertexMin(vertexPairs);

            //DebugShapes.createCross(mWorld, getBodyMin(newBody), 1.5f);

            // DebugShapes.createCross(mWorld, getVertexMin(vertexPairs), 2f);

            for (int i = 0; i < vertexPairs.length; i++) {

                vertices[i * 2] = (bodyPos.x + vertexPairs[i].x) / scaleX;
                vertices[i * 2 + 1] = (bodyPos.y + vertexPairs[i].y) / scaleY;

                Gdx.app.log("pos", "pos: " + vertices[i * 2] + "x" + vertices[i * 2 + 1]);
            }

            TextureRegion[] regions = TextureRegion.split(texture, (int) properties.mSprite.getWidth(), (int) properties.mSprite.getHeight())[0];
            TextureRegion region = regions[0];

            short triangles[] = new EarClippingTriangulator()
                    .computeTriangles(vertices)
                    .toArray();

            PolygonRegion polygonRegion = new PolygonRegion(region, vertices, triangles);
            PolygonSprite sprite = new PolygonSprite(polygonRegion);

            Vector2 origin = new Vector2(centre).sub(bodyPos);

            sprite.setOrigin(-(bodyPos.x),-(bodyPos.y));
            sprite.setScale(scaleX, scaleY);
            mSprites.put(newBody, sprite);

            BodyProperties newProperties = new BodyProperties();
            newProperties.polygonSprite = sprite;
            newProperties.mSprite = properties.mSprite;
            newBody.setUserData(newProperties);

            Gdx.app.log("sprite", "scale: " + sprite.getScaleX() + "x" + sprite.getScaleY());
        }
    }

    public void dispose() {

    }

    private void createSlice(Vector2[] vertices, Body fromBody) {

        if (getArea(vertices) > 0.05f) {

            Vector2 centre = findCentroid(vertices);

            // DebugShapes.createCircle(mWorld, centre);

            BodyDef sliceBodyDef = new BodyDef();
            sliceBodyDef.type = BodyDef.BodyType.StaticBody;
            Body sliceBody = mWorld.createBody(sliceBodyDef);

            PolygonShape slicePoly = new PolygonShape();

            for (Vector2 vertice : vertices) {
                vertice.sub(centre);
            }

            slicePoly.set(vertices);
            sliceBody.createFixture(slicePoly, 1);
            sliceBody.setTransform(centre, 0);

            addSprite(vertices, sliceBody, fromBody, centre);


            float distX = centre.x - explosionCenter.x;

            if (distX < 0) {
                if (distX < -explosionRadius) {
                    distX = 0;
                } else {
                    distX = -explosionRadius - distX;
                }
            } else {
                if (distX > explosionRadius) {
                    distX = 0;
                } else {
                    distX = 50 - distX;
                }
            }
            float distY = centre.y - explosionCenter.y;
            if (distY < 0) {
                if (distY < -explosionRadius) {
                    distY = 0;
                } else {
                    distY = -explosionRadius - distY;
                }
            } else {
                if (distY > explosionRadius) {
                    distY = 0;
                } else {
                    distY = explosionRadius - distY;
                }
            }
            distX *= 0.25;
            distY *= 0.25;
            sliceBody.setLinearVelocity(new Vector2(distX, distY));
            affectedBodies.add(sliceBody);

        } else {
            Gdx.app.log("vertices", "Not creating, Area : ");
        }
    }


    private void positionVerticesAndBody(Body body, Vector2[] vertices) {

        Vector2 centre = findCentroid(vertices);
        for (Vector2 vertex : vertices) {
            vertex.sub(centre);
        }
        body.setTransform(centre, 0);
    }


    private float getArea(Vector2[] vs) {

        if (vs.length < 3) return 0;
        float[] vertices = new float[vs.length * 2];
        int index = 0;
        for (Vector2 v : vs) {
            vertices[index++] = v.x;
            vertices[index++] = v.y;
        }

        return GeometryUtils.polygonArea(vertices, 0, vertices.length);

    }

    private Vector2 findCentroid(Vector2[] vs) {
        if (vs.length < 3) {
            return new Vector2(0, 0);
        }
        float[] vertices = new float[vs.length * 2];
        int index = 0;

        for (Vector2 v : vs) {
            vertices[index++] = v.x;
            vertices[index++] = v.y;
        }
        Vector2 centre = new Vector2();
        GeometryUtils.polygonCentroid(vertices, 0, vertices.length, centre);
        return centre;
    }
}
