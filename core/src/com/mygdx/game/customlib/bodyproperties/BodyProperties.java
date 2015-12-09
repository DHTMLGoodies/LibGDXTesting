package com.mygdx.game.customlib.bodyproperties;

import com.badlogic.gdx.graphics.g2d.PolygonSprite;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;

/**
 * Created by alfmagne1 on 07/12/15.
 */
public class BodyProperties {

    public Sprite sprite;
    public PolygonSprite polygonSprite;
    public String userString;
    public Vector2 polygonSpriteOrigin;

    public String toString(){
        return "sprite: " + getSpriteData();
    }

    private String getSpriteData(){
        if(sprite == null)return "null";
        return "size: " + sprite.getHeight() + "x" + sprite.getWidth() + ", scale " + sprite.getScaleX() + "x" + sprite.getScaleY()
                + "rotation: " + sprite.getRotation();

    }
}
