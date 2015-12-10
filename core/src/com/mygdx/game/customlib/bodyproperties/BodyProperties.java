package com.mygdx.game.customlib.bodyproperties;

import com.badlogic.gdx.graphics.g2d.PolygonRegion;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;

/**
 * Created by alfmagne1 on 07/12/15.
 */
public class BodyProperties {

    public Sprite sprite;
    public String userString;

    public TextureRegion textureRegion;

    public Vector2 bodyOrigin;
    public Vector2 bodySize;

    public PolygonRegion polygonRegion;
    public float regionScale;
    public float angleOnExplosion;

    public String toString(){
        return "sprite: " + getSpriteData();
    }

    private String getSpriteData(){
        if(sprite == null)return "null";
        return "size: " + sprite.getHeight() + "x" + sprite.getWidth() + ", scale " + sprite.getScaleX() + "x" + sprite.getScaleY()
                + "rotation: " + sprite.getRotation();

    }

    public static BodyProperties get(Body body){
        Object userData = body.getUserData();
        if(userData != null && userData instanceof BodyProperties)return (BodyProperties)userData;
        return null;
    }
}
