package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.gushikustudios.rube.RubeScene;
import com.gushikustudios.rube.loader.serializers.utils.RubeImage;
import com.mygdx.game.customlib.bodyproperties.sprites.BodySpriteRenderer;

/**
 * Created by alfmagne1 on 09/12/15.
 */
public class TextureTestScreen extends ScreenAdapter {

    private static final float WORLD_WIDTH = 960;
    private static final float WORLD_HEIGHT = 544;

    private static float UNITS_PER_METER = 32F;
    private static float UNIT_WIDTH = WORLD_WIDTH / UNITS_PER_METER;
    private static float UNIT_HEIGHT = WORLD_HEIGHT / UNITS_PER_METER;

    private BodySpriteRenderer mBodySpriteRenderer;

    private OrthographicCamera camera;
    private OrthographicCamera box2dCam;
    private World mWorld;
    private Box2DDebugRenderer debugRenderer;
    private Viewport viewport;
    private RubeScene mScene;

    private RubeTestingGame mGame;
    private String mMap;

    private Array<Sprite> mStaticSprites = new Array<>();
    private SpriteBatch mSpriteBatch;

    private Body mBody;

    public TextureTestScreen(RubeTestingGame game, String map){
        mGame = game;
        mMap = map;
    }


    @Override
    public void show() {
        super.show();

        mBodySpriteRenderer = new BodySpriteRenderer();

        camera = new OrthographicCamera();
        viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        viewport.apply(true);

        mSpriteBatch = new SpriteBatch();


        debugRenderer = new Box2DDebugRenderer();

        box2dCam = new OrthographicCamera(UNIT_WIDTH, UNIT_HEIGHT);


        mScene = mGame.getAssetManager().get(mMap, RubeScene.class);
        mWorld = mScene.getWorld();
        processScene();

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


    private void update(float delta){
        mWorld.step(delta, 6, 2);
        box2dCam.position.set(UNIT_WIDTH / 2, UNIT_HEIGHT / 2, 0);
        box2dCam.update();

        if(mBody != null){
            float angle = mBody.getAngle();
            angle += 1 / MathUtils.radiansToDegrees;
            mBody.setTransform(mBody.getPosition(), angle);
        }
    }

    private void draw(){
        mSpriteBatch.setProjectionMatrix(box2dCam.projection);
        mSpriteBatch.setTransformMatrix(box2dCam.view);

        mSpriteBatch.begin();
        for (Sprite sprite : mStaticSprites) {
            sprite.draw(mSpriteBatch);
        }

        mBodySpriteRenderer.draw(mSpriteBatch);
        mSpriteBatch.end();
    }

    private void debugDrawTexture(){



        /** Draws a rectangle with the bottom left corner at x,y and stretching the region to cover the given width and height. The
         * rectangle is offset by originX, originY relative to the origin. Scale specifies the scaling factor by which the rectangle
         * should be scaled around originX, originY. Rotation specifies the angle of counter clockwise rotation of the rectangle around
         * originX, originY.
         public void draw (TextureRegion region,
         float x,
         float y,
         float originX,
         float originY,
         float width,
         float height,
         float scaleX,
         float scaleY,
         float rotation);

         */
    }

    private void drawDebug(){
        debugRenderer.render(mWorld, box2dCam.combined);
    }

    private void clearScreen() {
        Gdx.gl.glClearColor(Color.BLUE.r, Color.BLUE.g, Color.BLUE.b, Color.BLUE.a);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    }

    private void processScene(){

        Array<Body> bodies = mScene.getBodies();
        if(bodies.size > 0){
            mBody = bodies.get(0);
        }
        Array<RubeImage> images = mScene.getImages();
        if (images != null) {

            for (RubeImage image : images) {

                Sprite sprite = SpriteGenerator.generateSprite(mGame.getAssetManager(), image);
                if (sprite != null) {
                    if (image.body == null) {
                        mStaticSprites.add(sprite);
                    }else{
                        mBodySpriteRenderer.add(image, mGame.getAssetManager());
                    }
                }
            }
        }
    }
}
