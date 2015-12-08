package com.mygdx.game.customlib.elevator;

import com.badlogic.gdx.physics.box2d.Joint;
import com.badlogic.gdx.physics.box2d.joints.PrismaticJoint;
import com.badlogic.gdx.utils.ObjectMap;
import com.gushikustudios.rube.RubeScene;

/**
 * Created by alfmagne1 on 05/12/15.
 */
public class ElevatorHandler {
    private ObjectMap<PrismaticJoint, ElevatorProperties> elevators;

    public ElevatorHandler(){
        elevators = new ObjectMap<>();
    }

    public void processJoints(RubeScene scene,  Joint joint){
        if(joint instanceof PrismaticJoint && scene.getCustom(joint, "elevator", false) == Boolean.TRUE){
            PrismaticJoint elevator = (PrismaticJoint)joint;
            ElevatorProperties props = new ElevatorProperties();
            props.elevatorStatus = ElevatorProperties.ELEVATOR_MOVING;
            props.elevatorWaitTime = (float)scene.getCustom(joint, "elevatorWaitTime", 2000f);
            props.motorSpeed = elevator.getMotorSpeed();
            elevators.put(elevator, props);
        }
    }

    public void update(float delta){


        ObjectMap.Entries<PrismaticJoint, ElevatorProperties> entries = elevators.entries();

        while(entries.hasNext()){

            ObjectMap.Entry<PrismaticJoint, ElevatorProperties> entry = entries.next();

            ElevatorProperties props = entry.value;
            PrismaticJoint elevator = entry.key;

            if(props.elevatorStatus == ElevatorProperties.ELEVATOR_MOVING){
                if(elevator.getMotorSpeed() > 0 && elevator.getJointTranslation() > elevator.getUpperLimit()){
                    elevator.setMotorSpeed(0);
                    props.elevatorStatus = ElevatorProperties.ELEVATOR_STANDING_STILL;
                    props.motorSpeed *= -1;
                    props.waited = 0;
                }else if(elevator.getMotorSpeed() < 0 && elevator.getJointTranslation() <= elevator.getLowerLimit()){
                    elevator.setMotorSpeed(0);
                    props.elevatorStatus = ElevatorProperties.ELEVATOR_STANDING_STILL;
                    props.waited = 0;
                    props.motorSpeed *= -1;
                }

            }else{

                props.waited += delta;

                if(props.waited > props.elevatorWaitTime){
                    elevator.setMotorSpeed(props.motorSpeed);
                    props.elevatorStatus = ElevatorProperties.ELEVATOR_MOVING;
                }
            }
        }

    }
}
