package com.mygdx.game.customlib.bodyproperties;

import com.badlogic.gdx.graphics.g2d.PolygonSprite;
import com.badlogic.gdx.graphics.g2d.Sprite;

/**
 * Created by alfmagne1 on 07/12/15.
 */
public class BodyProperties {

    public Sprite mSprite;
    public PolygonSprite polygonSprite;
    public String mUserString;

    public String toString(){
        return "sprite: " + getSpriteData();
    }

    private String getSpriteData(){
        if(mSprite == null)return "null";
        return "size: " + mSprite.getHeight() + "x" + mSprite.getWidth() + ", scale " + mSprite.getScaleX() + "x" + mSprite.getScaleY()
                + "rotation: " + mSprite.getRotation();

    }
}
