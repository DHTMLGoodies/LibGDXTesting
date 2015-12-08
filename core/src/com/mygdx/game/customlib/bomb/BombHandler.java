package com.mygdx.game.customlib.bomb;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.gushikustudios.rube.RubeScene;


/**
 * Created by alfmagne1 on 05/12/15.
 */
public class BombHandler {
    private Body mBomb;

    private static final int BOMB_PARTICLES = 30;

    private float lastBomb;
    private float elapsed;

    private Array<Body> mBodies;
    private Array<Sprite> mSprites;


    public BombHandler() {
        mSprites = new Array<>();
        mBodies = new Array<>();
    }

    public void processBomb(RubeScene scene, Body body) {
        if (scene.getCustom(body, "bomb", false) == Boolean.TRUE) {
            body.setUserData("isbomb");
        }
    }


    public void explode(Body bomb) {
        mBomb = bomb;
    }

    public void update(float delta, World world, AssetManager assetManager) {

        if (mBomb != null) {
            elapsed = 0;
            for (int i = 0; i < BOMB_PARTICLES; i++) {
                getBombParticle(world, mBomb.getWorldCenter(), assetManager);
            }
            world.destroyBody(mBomb);
            mBomb = null;
        }

        if(mSprites == null)return;

        elapsed += delta;

        if(elapsed > 4 && mBodies.size > 0){
            Body body = mBodies.get(0);
            world.destroyBody(body);
            mSprites.get(0).setAlpha(0);
            mSprites.removeIndex(0);
            mBodies.removeIndex(0);
        }

        for(int i=0;i<mBodies.size; i++){
            Body body = mBodies.get(i);
            mSprites.get(i).setPosition(body.getPosition().x, body.getPosition().y);
        }


    }

    private Body getBombParticle(World world, Vector2 position, AssetManager assetManager) {

        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;

        float radius = 0.05f;

        CircleShape shape = new CircleShape();
        shape.setRadius(radius);

        Body body = world.createBody(bodyDef);
        body.createFixture(shape, 1000);
        body.setBullet(true);
        body.setTransform(position, MathUtils.random() * MathUtils.PI2);
        float velocityX = MathUtils.random() * 10f * (MathUtils.random() > 0.5f ? -1 : 1);
        float velocityY = MathUtils.random() * 10f * (MathUtils.random() > 0.5f ? -1 : 1);
        body.setLinearVelocity(velocityX, velocityY);
        shape.dispose();

        Sprite sprite = new Sprite(assetManager.get("bomb_particle.png", Texture.class));
        sprite.setOrigin(-radius, -radius);
        float scale = (radius * 2) / sprite.getWidth();
        sprite.setScale(scale, scale);
        sprite.setColor(1f, MathUtils.random(), 0f, 1f);
        mBodies.add(body);
        mSprites.add(sprite);
        return body;
    }

    public void draw(SpriteBatch batch) {
        if(mSprites == null)return;
        for(Sprite sprite : mSprites){
            sprite.draw(batch);
        }
    }
}
