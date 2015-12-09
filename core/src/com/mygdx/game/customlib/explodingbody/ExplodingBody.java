package com.mygdx.game.customlib.explodingbody;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.PolygonRegion;
import com.badlogic.gdx.graphics.g2d.PolygonSprite;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.EarClippingTriangulator;
import com.badlogic.gdx.math.GeometryUtils;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.RayCastCallback;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.gushikustudios.rube.loader.serializers.utils.RubeImage;
import com.mygdx.game.SpriteGenerator;
import com.mygdx.game.customlib.bodyproperties.BodyProperties;

import net.dermetfan.gdx.physics.box2d.Box2DUtils;


/**
 * Exploding body
 * Created by alfmagne1 on 06/12/15.
 */
public class ExplodingBody implements RayCastCallback {

    private static final int CHUNKS = 1;

    private Array<Body> affectedBodies = new Array<>();

    private Array<Body> affectedByLaser;
    private Array<Vector2> entryPoint = new Array<>();

    private World mWorld;

    private Vector2 explosionCenter;
    private float explosionRadius;

    private Array<Body> mSlices;
    private Array<Body> mBodies;

    private Vector2 bodyMinOriginal;

    private EarClippingTriangulator mEarClippingTriangulator;

    public ExplodingBody() {
        mSlices = new Array<>();
        mBodies = new Array<>();
        mEarClippingTriangulator = new EarClippingTriangulator();
    }

    public void add(RubeImage rubeImage, AssetManager assetManager){
        Sprite sprite = SpriteGenerator.generateSprite(assetManager, rubeImage);
        add(rubeImage.body, sprite);
    }

    public void add(Body body, Sprite sprite) {
        Gdx.app.log("exploding", "Adding exploding");

        BodyProperties properties;
        if (body.getUserData() != null) {
            properties = (BodyProperties) body.getUserData();
        } else {
            properties = new BodyProperties();
        }

        properties.sprite = sprite;
        body.setUserData(properties);

        mBodies.add(body);
    }

    public void explode(World world, Body body) {

        Gdx.app.log("explode", "explode");

        mWorld = world;
        explosionCenter = body.getPosition();
        explosionRadius = 5;
        float worldScale = 1;
        affectedBodies.add(body);

        bodyMinOriginal = getBodyMin(body);

        for (int i = 0; i < CHUNKS; i++) {
            float cutAngle = MathUtils.random() * MathUtils.PI2;
            float offset = 10;

            Vector2 p1 = new Vector2((explosionCenter.x + (float) i / 1000 - offset * MathUtils.cos(cutAngle)) / worldScale, (explosionCenter.y - offset * MathUtils.sin(cutAngle)) / worldScale);
            Vector2 p2 = new Vector2((explosionCenter.x + (offset * MathUtils.cos(cutAngle))) / worldScale, (explosionCenter.y + (offset * MathUtils.sin(cutAngle))) / worldScale);

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
        int index = mSlices.indexOf(body, true);
        if (index >= 0) {
            mSlices.removeIndex(index);
        }
        index = mBodies.indexOf(body, true);
        if(index >= 0){
            mBodies.removeIndex(index);
        }
        mWorld.destroyBody(body);
    }

    private float debugAngle = 0;

    public void update(float delta) {

        debugAngle += 1;
        debugAngle = debugAngle % 360f;


        BodyProperties properties;

        for (Body body : mSlices) {

            Object userData = body.getUserData();
            if (userData != null) {
                properties = (BodyProperties) userData;

                if (properties.polygonSprite != null) {

                    PolygonSprite sprite = properties.polygonSprite;

                    Vector2 origin =new Vector2(properties.polygonSpriteOrigin);
                    origin.rotate(body.getAngle() * MathUtils.radiansToDegrees);

                    Vector2 position = new Vector2(body.getPosition());

                    sprite.setPosition(
                            position.x + origin.x - properties.polygonSpriteOrigin.x,
                            position.y + origin.y - properties.polygonSpriteOrigin.y
                    );
                    sprite.setRotation(body.getAngle() * MathUtils.radiansToDegrees);
                }
            }
        }

        for(Body body : mBodies){
            Object userData = body.getUserData();
            if (userData != null) {
                properties = (BodyProperties) userData;
                if (properties.sprite != null) {
                    properties.sprite.setPosition(
                            body.getPosition().x - properties.sprite.getWidth() / 2f,
                            body.getPosition().y - properties.sprite.getHeight() / 2f);
                    properties.sprite.setRotation(MathUtils.radiansToDegrees * (body.getAngle()));
                }
            }
        }
    }

    public void draw(SpriteBatch batch){

        BodyProperties properties;

        for (Body body : mBodies) {
            if (body.getUserData() != null) {

                properties = (BodyProperties) body.getUserData();

                 if (properties.sprite != null) {
                    properties.sprite.draw(batch);
                }
            }
        }
    }

    public void draw(PolygonSpriteBatch spriteBatch) {
        BodyProperties properties;

        for (Body body : mSlices) {
            if (body.getUserData() != null) {
                properties = (BodyProperties) body.getUserData();
                if (properties.polygonSprite != null) {
                    properties.polygonSprite.draw(spriteBatch);
                }
            }
        }

    }


    private void addSprite(Vector2[] vertexPairs, Body newBody, Body originalBody) {


        Object userData = originalBody.getUserData();
        if (userData == null || !(userData instanceof BodyProperties)) return;

        BodyProperties properties = (BodyProperties) originalBody.getUserData();
        if (properties.sprite != null) {
            Texture texture = properties.sprite.getTexture();

            float[] vertices = new float[vertexPairs.length * 2];

            float scaleX = properties.sprite.getScaleX();
            float scaleY = properties.sprite.getScaleY();

            Vector2 bodyPos = getBodyMin(newBody).sub(bodyMinOriginal).sub(getVertexMin(vertexPairs));

            for (int i = 0; i < vertexPairs.length; i++) {
                vertices[i * 2] = (bodyPos.x + vertexPairs[i].x) / scaleX;
                vertices[i * 2 + 1] = (bodyPos.y + vertexPairs[i].y) / scaleY;
            }

            TextureRegion region = new TextureRegion(texture, (int) properties.sprite.getWidth(), (int) properties.sprite.getHeight());

            short triangles[] = mEarClippingTriangulator
                    .computeTriangles(vertices)
                    .toArray();

            PolygonRegion polygonRegion = new PolygonRegion(region, vertices, triangles);

            PolygonSprite sprite = new PolygonSprite(polygonRegion);

            Vector2 origin = new Vector2();
            origin.x = -bodyPos.x;
            origin.y = -bodyPos.y;

            sprite.setOrigin(-(bodyPos.x), -(bodyPos.y));

            sprite.setScale(scaleX, scaleY);
            mSlices.add(newBody);

            BodyProperties newProperties = new BodyProperties();
            newProperties.polygonSprite = sprite;
            newProperties.sprite = properties.sprite;
            newProperties.polygonSpriteOrigin = origin;
            newBody.setUserData(newProperties);
        }
    }



    private void createSlice(Vector2[] vertices, Body fromBody) {

        if (getArea(vertices) > 0.05f) {

            Vector2 centre = findCentroid(vertices);

            BodyDef sliceBodyDef = new BodyDef();
            sliceBodyDef.type = BodyDef.BodyType.DynamicBody;
            Body sliceBody = mWorld.createBody(sliceBodyDef);

            PolygonShape slicePoly = new PolygonShape();

            for (Vector2 vertice : vertices) {
                vertice.sub(centre);
            }

            slicePoly.set(vertices);
            sliceBody.createFixture(slicePoly, 1);
            sliceBody.setTransform(centre, 0);

            addSprite(vertices, sliceBody, fromBody);


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

            slicePoly.dispose();

        }
    }


    private Vector2 getBodyMin(Body body) {
        float[] vertices = Box2DUtils.vertices(body.getFixtureList().get(0));
        Vector2 pos = new Vector2(
                net.dermetfan.gdx.math.GeometryUtils.minX(vertices),
                net.dermetfan.gdx.math.GeometryUtils.minY(vertices)
        );
        Vector2 bodyPos = body.getPosition();
        return new Vector2(bodyPos.x + pos.x, bodyPos.y + pos.y);
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

    public void dispose() {

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
