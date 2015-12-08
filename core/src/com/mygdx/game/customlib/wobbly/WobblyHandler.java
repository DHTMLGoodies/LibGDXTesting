package com.mygdx.game.customlib.wobbly;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.utils.ObjectMap;
import com.gushikustudios.rube.RubeScene;

/**
 * Wobbly Handler
 * Created by alfmagne1 on 04/12/15.
 */
public class WobblyHandler {

    private float mTimePassed;

    private ObjectMap<Body, WobblyProperties> mWobblyBodies = new ObjectMap();

    public WobblyHandler(){
        mTimePassed = 0;
    }

    public void processBody(RubeScene scene,  Body body){

        if(scene.getCustom(body, "wobbly", false) == Boolean.TRUE){
            WobblyProperties props = new WobblyProperties();
            props.horizontalRange = (float)scene.getCustom(body, "horzRange", 0);
            props.verticalRange = (float)scene.getCustom(body, "vertRange", 0);
            props.speed = (float)scene.getCustom(body, "speed", 0);
            props.basePosition = body.getPosition();
            props.currentPosition = new Vector2();
            mWobblyBodies.put(body, props);
        }
    }

    public void update(float delta){

        mTimePassed += delta;

        ObjectMap.Entries<Body, WobblyProperties> entries = mWobblyBodies.entries();

        while(entries.hasNext()){
            ObjectMap.Entry<Body, WobblyProperties> entry = entries.next();

            WobblyProperties props = entry.value;
            props.currentPosition.x = props.basePosition.x + MathUtils.cos(mTimePassed * props.speed) * props.horizontalRange;
            props.currentPosition.y = props.basePosition.y + MathUtils.sin(mTimePassed * props.speed) * props.verticalRange;

            entry.key.setTransform(props.currentPosition, 0);
        }
    }

}
