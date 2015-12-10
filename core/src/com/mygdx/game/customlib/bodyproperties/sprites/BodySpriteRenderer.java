package com.mygdx.game.customlib.bodyproperties.sprites;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.utils.Array;
import com.gushikustudios.rube.loader.serializers.utils.RubeImage;
import com.mygdx.game.customlib.bodyproperties.BodyProperties;

import net.dermetfan.gdx.physics.box2d.Box2DUtils;

/**
 * Rendering sprite on Box2D body
 * Created by alfmagne1 on 09/12/15.
 */
public class BodySpriteRenderer {

    private Array<Body> mBodies;

    public BodySpriteRenderer() {
        mBodies = new Array<>();
    }

    public void add(RubeImage image, AssetManager assetManager) {
        if (image.body == null) return;
        add(image.body, assetManager.get(image.file, Texture.class));
    }

    public void add(Body body, Texture texture) {
        add(body, new TextureRegion(texture));
    }

    public void add(Body body, TextureRegion region) {
        BodyProperties properties;
        Object userData = body.getUserData();
        if (userData != null && userData instanceof BodyProperties) {
            properties = (BodyProperties) userData;
        } else {
            properties = new BodyProperties();
        }

        properties.bodyOrigin = new Vector2(
                Box2DUtils.minX(body), Box2DUtils.minY(body)
        );

        properties.bodySize = new Vector2(
                Box2DUtils.maxX(body) - properties.bodyOrigin.x,
                Box2DUtils.maxY(body) - properties.bodyOrigin.y
        );
        properties.regionScale = region.getRegionWidth() / properties.bodySize.x;
        properties.textureRegion = region;
        body.setUserData(properties);
        mBodies.add(body);
    }

    public void draw(SpriteBatch batch, Body body) {
        BodyProperties properties = getBodyProperties(body);
        if (properties == null) return;

        Vector2 bodyOrigin = properties.bodyOrigin;
        Vector2 bodyPos = body.getPosition();
        Vector2 bodySize = properties.bodySize;

        float x = bodyPos.x + bodyOrigin.x;
        float y = bodyPos.y + bodyOrigin.y;
        float originX = -bodyOrigin.x;
        float originY = -bodyOrigin.y;
        float width = bodySize.x;
        float height = bodySize.y;
        float scaleX = 1f;
        float scaleY = 1f;
        float angle = body.getAngle() * MathUtils.radiansToDegrees;

        batch.draw(properties.textureRegion, x, y, originX, originY, width, height, scaleX, scaleY, angle);
    }

    public void destroy(Body body) {
        int index = mBodies.indexOf(body, true);
        if (index >= 0) {
            mBodies.removeIndex(index);
        }
    }

    public void dispose() {
        mBodies = new Array<>();
    }

    public void draw(SpriteBatch batch) {
        for (Body body : mBodies) {
            draw(batch, body);
        }
    }


    private BodyProperties getBodyProperties(Body body) {
        Object userData = body.getUserData();
        if (userData != null && userData instanceof BodyProperties) {
            return (BodyProperties) userData;
        }
        return null;
    }
}
