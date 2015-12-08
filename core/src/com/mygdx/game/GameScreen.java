package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.PolygonRegion;
import com.badlogic.gdx.graphics.g2d.PolygonSprite;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.EarClippingTriangulator;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Joint;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJoint;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.gushikustudios.rube.RubeScene;
import com.gushikustudios.rube.loader.serializers.utils.RubeImage;
import com.mygdx.game.customlib.bodyproperties.BodyProperties;
import com.mygdx.game.customlib.bomb.BombHandler;
import com.mygdx.game.customlib.elevator.ElevatorHandler;
import com.mygdx.game.customlib.explodingbody.ExplodingBody;
import com.mygdx.game.customlib.wobbly.WobblyHandler;

import box2dLight.PointLight;
import box2dLight.RayHandler;

/**
 * Created by alfmagne1 on 03/12/15.
 */
public class GameScreen extends ScreenAdapter {

    private static final float WORLD_WIDTH = 960;
    private static final float WORLD_HEIGHT = 544;

    private static float UNITS_PER_METER = 32F;
    private static float UNIT_WIDTH = WORLD_WIDTH / UNITS_PER_METER;
    private static float UNIT_HEIGHT = WORLD_HEIGHT / UNITS_PER_METER;

    private RubeTestingGame mGame;

    private Viewport viewport;
    private OrthographicCamera camera;
    private RubeScene mScene;

    private OrthographicCamera box2dCam;

    private World mWorld;

    private Box2DDebugRenderer debugRenderer;
    private String mMap;
    private SpriteBatch mSpriteBatch;

    private Array<Sprite> mStaticSprites;
    private ObjectMap<RubeImage, Sprite> mBodySprites = new ObjectMap<>();

    private Body mBall;


    private WobblyHandler mWobblyHandler = new WobblyHandler();
    private ElevatorHandler mElevatorHandler = new ElevatorHandler();
    private static final int STATE_SETUP = 1;
    private static final int STATE_RUN = 2;

    private float cameraScale;

    private int mCurrentState;
    private Array<Joint> mBreakableJoint = new Array<>();

    private RayHandler mRayHandler;

    private Array<RevoluteJoint> mWheelDrives = new Array<>();

    private float wheelPowerUpdate;

    private Array<Body> mBombs = new Array<>();
    private BombHandler mBombHandler = new BombHandler();

    public GameScreen(RubeTestingGame game, String map) {
        mGame = game;
        mMap = map;
    }

    @Override
    public void show() {
        super.show();
        cameraScale = 1f;

        camera = new OrthographicCamera();
        viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        viewport.apply(true);

        mCurrentState = STATE_SETUP;

        debugRenderer = new Box2DDebugRenderer();

        mScene = mGame.getAssetManager().get(mMap, RubeScene.class);
        box2dCam = new OrthographicCamera(UNIT_WIDTH * cameraScale, UNIT_HEIGHT * cameraScale);
        mSpriteBatch = new SpriteBatch();

        mStaticSprites = new Array<>();
        mWorld = mScene.getWorld();

        mWorld.setContactListener(new BombContactListener());

        processScene();


        Gdx.input.setInputProcessor(new InputAdapter() {

            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.LEFT) {
                    wheelPowerUpdate = 5f;
                } else if (keycode == Input.Keys.RIGHT) {
                    wheelPowerUpdate = -5f;
                }
                return true;
            }

            @Override
            public boolean touchDragged(int screenX, int screenY, int pointer) {

                return true;
            }

            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                moveSensor(screenX, screenY);
                /*
                Vector3 pos = new Vector3(screenX, screenY, 0);
                box2dCam.unproject(pos);
                mBombHandler.click(new Vector2(pos.x, pos.y));
                */
                return true;
            }

            @Override
            public boolean touchUp(int screenX, int screenY, int pointer, int button) {
                breakJoint();
                return true;
            }

        });
    }

    private Body mSensorBody;

    private void moveSensor(int screenX, int screenY){
        if(mSensorBody != null){
            mWorld.destroyBody(mSensorBody);
            mSensorBody = null;
        }

        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.KinematicBody;
        CircleShape shape = new CircleShape();
        shape.setRadius(0.001f);
        mSensorBody = mWorld.createBody(bodyDef);
        mSensorBody.createFixture(shape, 0);
        mSensorBody.getFixtureList().get(0).setSensor(true);
        shape.dispose();


        Vector3 pos = new Vector3(screenX, screenY, 0);
        box2dCam.unproject(pos);
        mSensorBody.setTransform(pos.x, pos.y, 0);
    }

    private void applyLight() {
        RayHandler.setGammaCorrection(true);
        RayHandler.useDiffuseLight(false);

        mRayHandler = new RayHandler(mWorld);
        mRayHandler.setAmbientLight(0.5f, 0.5f, 0.5f, 0.3f);
        mRayHandler.setBlurNum(3);


        PointLight light = new PointLight(
                mRayHandler, 20, Color.WHITE, 1f, 0f, 0f);
        light.attachToBody(mBall, 1f, 1f);
        light.setColor(
                1f,
                1f,
                1f,
                1f);
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        viewport.update(width, height);
    }

    @Override
    public void render(float delta) {
        super.render(delta);
        update(delta);
        clearScreen();
        draw();
        drawDebug();

    }

    private void breakJoint() {
        if (mBreakableJoint != null) {
            for (Joint joint : mBreakableJoint) {
                mWorld.destroyJoint(joint);
            }
        }

        if (mBall != null) {
            mBall.applyForce(new Vector2(0, 40), mBall.getWorldCenter(), true);
            mBall.applyTorque(mBall.getLinearVelocity().x > 0 ? -20 : 20, true);
        }
        mBreakableJoint = null;
    }

    private void processScene() {
        Array<Body> bodies = mScene.getBodies();

        if(bodies != null) {
            for (Body body : bodies) {
                if (mScene.getCustom(body, "actor", false) == Boolean.TRUE) {
                    mBall = body;
                }
                mBombHandler.processBomb(mScene, body);
                mWobblyHandler.processBody(mScene, body);

                if(mMap.equals("level_3.json") && mScene.getCustom(body, "exploding", false) == Boolean.TRUE){
                    debugTextureRegion(body);
                }

            }


        }

        Array<Joint> joints = mScene.getJoints();
        if (joints != null) {
            for (Joint joint : joints) {
                if (mScene.getCustom(joint, "breakableJoint", false) == Boolean.TRUE) {
                    mBreakableJoint.add(joint);
                }
                if(joint instanceof RevoluteJoint && mScene.getCustom(joint, "wheeldrive", false) == Boolean.TRUE){
                    mWheelDrives.add((RevoluteJoint) joint);
                }
                mElevatorHandler.processJoints(mScene, joint);
            }

        }


        Array<RubeImage> images = mScene.getImages();
        if (images != null) {

            for (RubeImage image : images) {

                Sprite sprite = SpriteGenerator.generateSprite(mGame.getAssetManager(), image);
                if (sprite != null) {
                    if (image.body != null) {
                        mBodySprites.put(image, sprite);

                        BodyProperties properties = new BodyProperties();
                        properties.mSprite = sprite;
                        image.body.setUserData(properties);

                    } else {
                        mStaticSprites.add(sprite);
                    }
                }
            }
        }

        /*

        * scene.getWorld(): This method returns the Box2D physics world.  After loading, it is populated with the bodies, joints, and fixtures from the JSON file.
* scene.getBodies(): This method returns an array of bodies created
* scene.getFixtures(): This method returns an array of fixtures created
* scene.getJoints(): This method returns an array of joints created
* scene.getImages(): This method returns an array of RubeImages defined in the JSON file.  Note: it is up to the app to perform all rendering
* scene.getMappedImage(): This method returns an array of all RubeImages associated with a particular Body.
* scene.getCustom(): This method allows you to retrieve custom property info from an object.
* scene.getNamed(): This method allows you to retrieve a scene object based on name.  Since multiple objects can have the same name, this returns an Array<> type.
*
         */
    }

    PolygonSpriteBatch polyBatch;
    PolygonSprite poly;
    Body mExpBody;

    private static final float PIXELS_PER_METER = 10;
    private void debugTextureRegion(Body body){
        mExpBody = body;
        Texture texture = mGame.getAssetManager().get("texture_200x200.png", Texture.class);
        TextureRegion[] regions = TextureRegion.split(texture, 200, 200)[0];
        TextureRegion textureRegion = regions[0];

        Fixture fixture = body.getFixtureList().get(0);
        PolygonShape shape = (PolygonShape) fixture.getShape();
        int vertexCount = shape.getVertexCount();
        float[] vertices = new float[vertexCount * 2];
        float[] plainVertices = new float[vertexCount * 2];
        Vector2 mTmp = new Vector2();
        for (int k = 0; k < vertexCount; k++) {
            shape.getVertex(k, mTmp);

            plainVertices[k * 2] = mTmp.x;
            plainVertices[k * 2 + 1] = mTmp.y;

            mTmp.rotate(body.getAngle()* MathUtils.radiansToDegrees);
            mTmp.add(body.getPosition());
            vertices[k * 2] = mTmp.x * PIXELS_PER_METER;
            vertices[k * 2 + 1] = mTmp.y * PIXELS_PER_METER;
        }
        short triangles[] = new EarClippingTriangulator()
                .computeTriangles(vertices)
                .toArray();
        PolygonRegion region = new PolygonRegion(
                textureRegion, plainVertices, triangles);
        poly = new PolygonSprite(region);
        polyBatch = new PolygonSpriteBatch();
    }

    private ExplodingBody mExplodingBody;
    private boolean mBodyExploded;

    private void update(float delta) {
        mWorld.step(delta, 6, 2);

        box2dCam.position.set(UNIT_WIDTH / 2, UNIT_HEIGHT / 2, 0);

        float halfWidth = box2dCam.viewportWidth / 2;
        float halfHeight = box2dCam.viewportHeight / 2;
        float x = UNIT_WIDTH / 2;
        float y = UNIT_HEIGHT / 2;
        if (mBall != null) {
            x = MathUtils.clamp(mBall.getPosition().x, halfWidth, UNIT_WIDTH - halfWidth);
            y = MathUtils.clamp(mBall.getPosition().y, halfHeight, UNIT_HEIGHT - halfHeight);
        }

        box2dCam.position.set(x, y, 0);

        box2dCam.update();

        updateSpritePositions();

        mWobblyHandler.update(delta);
        mElevatorHandler.update(delta);
        mBombHandler.update(delta, mWorld, mGame.getAssetManager());

        if(wheelPowerUpdate != 0){
            for(RevoluteJoint wheel : mWheelDrives){
                wheel.setMotorSpeed(wheel.getMotorSpeed() + wheelPowerUpdate);
            }
            wheelPowerUpdate = 0;
        }

        if(!mBodyExploded && mExpBody != null){
            mBodyExploded = true;
            mExplodingBody = new ExplodingBody();
            mExplodingBody.explode(mWorld, mExpBody);
        }

        mExplodingBody.update(delta);


        /*
        box2dCam.position.set(UNIT_WIDTH / 2, UNIT_HEIGHT / 2, 0);
        box2dCam.update();
        updateSpritePositions();
        */
    }

    private void updateSpritePositions() {
        for (RubeImage image : mBodySprites.keys()) {
            Sprite sprite = mBodySprites.get(image);

            sprite.setPosition(
                    image.body.getPosition().x - sprite.getWidth() / 2f,
                    image.body.getPosition().y - sprite.getHeight() / 2f);
            sprite.setRotation(MathUtils.radiansToDegrees * (image.angleInRads + image.body.getAngle()));
        }
    }

    private void draw() {
        mSpriteBatch.setProjectionMatrix(box2dCam.projection);
        mSpriteBatch.setTransformMatrix(box2dCam.view);


        mSpriteBatch.begin();
        for (Sprite sprite : mStaticSprites) {
            sprite.draw(mSpriteBatch);
        }

        for (Sprite sprite : mBodySprites.values()) {
           // sprite.draw(mSpriteBatch);
        }
        mBombHandler.draw(mSpriteBatch);

        mSpriteBatch.end();


        if(poly != null){
            polyBatch.setProjectionMatrix(box2dCam.projection);
            polyBatch.setTransformMatrix(box2dCam.view);


            polyBatch.begin();

           // poly.draw(polyBatch);

            mExplodingBody.draw(polyBatch);

            polyBatch.end();
        }

        /*
        mRayHandler.setCombinedMatrix(box2dCam);
        mRayHandler.updateAndRender();
        */

    }

    @Override
    public void dispose() {
        super.dispose();
        mRayHandler.dispose();
        mWorld.dispose();

        mExplodingBody.dispose();
    }

    private void drawDebug() {
        debugRenderer.render(mWorld, box2dCam.combined);
    }

    private void clearScreen() {
        Gdx.gl.glClearColor(Color.BLUE.r, Color.BLUE.g, Color.BLUE.b, Color.BLUE.a);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    }


    private class BombContactListener implements ContactListener {



        @Override
        public void beginContact(Contact contact) {
            if (contact.isTouching()) {
                Fixture attacker = contact.getFixtureA();
                Fixture defender = contact.getFixtureB();

                boolean attackerIsSensor = attacker.isSensor();
                boolean defenderIsSensor = defender.isSensor();

                if(attackerIsSensor){
                    Body body = defender.getBody();
                    if(body.getUserData() != null && body.getUserData().equals("isbomb")){
                        mBombHandler.explode(body);
                    }
                }else if(defenderIsSensor){
                    Body body = attacker.getBody();
                    if(body.getUserData() != null && body.getUserData().equals("isbomb")){
                        mBombHandler.explode(body);
                    }

                }
            }
        }

        @Override
        public void endContact(Contact contact) {

        }

        @Override
        public void preSolve(Contact contact, Manifold oldManifold) {

        }

        @Override
        public void postSolve(Contact contact, ContactImpulse impulse) {

        }
    }

}
