package com.mygdx.game;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.gushikustudios.rube.loader.serializers.utils.RubeImage;


public class SpriteGenerator {

    private static final boolean USING_BOX2D_CAMERA = true;

    public static Sprite generateSprite(AssetManager assetManager, RubeImage image) {
        Sprite sprite = new Sprite(assetManager.get(image.file, Texture.class));
        sprite.setOrigin(sprite.getWidth() / 2 + image.center.x, sprite.getHeight() / 2 + image.center.y);

        float multiplier = USING_BOX2D_CAMERA ? 1 : Constants.UNITS_PER_METER;

        float size = multiplier * image.width;
        float scaleX = size / sprite.getWidth();

        float sizeY = multiplier * image.height;
        float scaleY = sizeY / sprite.getHeight();

        /*

        Gdx.app.log("sprite", "======================================================");
        Gdx.app.log("sprite", image.file + " scale " + image.scale);
        Gdx.app.log("sprite", "Sprite size " + sprite.getWidth() + "x" + sprite.getHeight());
        Gdx.app.log("sprite", "Image size: "+ image.width + " x " + image.height);
        Gdx.app.log("sprite", "Image position: "+ image.center.x + " x " + image.center.y);
        Gdx.app.log("sprite", "Size " + size + " gives scale: " + scaleX + "x" + scaleY);
*/


        if (image.body != null) {
            sprite.setScale(scaleX, scaleY);
        } else {
            float x = (multiplier * image.center.x) - (image.width / 2 * multiplier);
            float y = (multiplier * image.center.y) - (image.height / 2 * multiplier);
            float width = image.width * multiplier;
            float height = image.height * multiplier;
            sprite.setBounds(x, y, width, height);
            //Gdx.app.log("sprite", "New pos: " + x + " x "+ y + " size: " + width + " x " + height);
        }
        /*
        Gdx.app.log("sprite", "======================================================");
        Gdx.app.log("sprite", "   ");
*/

        return sprite;
    }

}
