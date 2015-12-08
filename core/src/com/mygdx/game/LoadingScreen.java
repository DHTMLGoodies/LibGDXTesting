package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.PolygonRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.EarClippingTriangulator;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.gushikustudios.rube.RubeScene;
import com.gushikustudios.rube.loader.serializers.utils.RubeImage;


/**
 * Created by alfmagne1 on 03/12/15.
 */
public class LoadingScreen extends ScreenAdapter {

    private static final float WORLD_WIDTH = 960;
    private static final float WORLD_HEIGHT = 544;
    private static final float PROGRESS_BAR_WIDTH = 100;
    private static final float PROGRESS_BAR_HEIGHT = 25;

    private ShapeRenderer shapeRenderer;
    private Viewport viewport;
    private OrthographicCamera
            camera;
    private float progress = 0;
    private RubeTestingGame mGame;

    private String mMap;
    private RubeScene mScene;

    private boolean mSceneProcessed;

    public LoadingScreen(RubeTestingGame game, int level) {
        mGame = game;
        mMap = "level_" + level + ".json";
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        viewport.update(width, height);
    }

    @Override
    public void show() {

        mSceneProcessed = false;

        camera = new OrthographicCamera();
        camera.position.set(WORLD_WIDTH / 2, WORLD_HEIGHT / 2, 0);
        camera.update();
        viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        shapeRenderer = new ShapeRenderer();

        mGame.getAssetManager().load("bomb_particle.png", Texture.class);
        mGame.getAssetManager().load(mMap, RubeScene.class);
        mGame.getAssetManager().load("texture_200x200.png", Texture.class);


    }



    private void processScene() {

        mScene = mGame.getAssetManager().get(mMap, RubeScene.class);

        Array<RubeImage> images = mScene.getImages();
        AssetManager assetManager = mGame.getAssetManager();
        if (images != null) {

            for (RubeImage image : images) {
                Gdx.app.log("laoding", "Loading texture " + image.file + " ");
                assetManager.load(image.file, Texture.class);
            }
        }


    }

    @Override
    public void render(float delta) {
        super.render(delta);
        update();
        clearScreen();
        draw();
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
    }

    private void update() {
        if (mGame.getAssetManager().update()) {

            if (!mSceneProcessed) {
                mSceneProcessed = true;
                processScene();
            } else {
                Gdx.app.log("loading", "Loading game screen");
                mGame.setScreen(new GameScreen(mGame, mMap));
            }
        } else {
            progress = mGame.getAssetManager().getProgress();
        }
    }

    private void clearScreen() {
        Gdx.gl.glClearColor(Color.BLUE.r, Color.BLUE.g, Color.BLUE.b, Color.BLUE.a);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    }

    private void draw() {
        shapeRenderer.setProjectionMatrix(camera.projection);
        shapeRenderer.setTransformMatrix(camera.view);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.rect(
                (WORLD_WIDTH - PROGRESS_BAR_WIDTH) / 2, WORLD_HEIGHT / 2 - PROGRESS_BAR_HEIGHT / 2,
                progress * PROGRESS_BAR_WIDTH, PROGRESS_BAR_HEIGHT);
        shapeRenderer.end();
    }
}
