package tage;

import java.lang.Math;
import tage.input.InputManager;
import tage.input.action.AbstractInputAction;
import tage.input.action.IAction;
import tage.*;
import java.util.Vector;
import org.joml.*;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;
import net.java.games.input.Version;
import net.java.games.input.Component;
import net.java.games.input.*;

public class ShipController {
    private Engine engine;
    private ProtocolClient protClient;
    private GameObject ship;
    private float zVel;
    private float xVel;
    private float yVel;
    private float speedDropOffBase = 0.01f;
    private float slowDown = 0.01f;
    private float MAX_SPEED = 20f;

    public ShipController(GameObject playerShip, String gpName, Engine e, ProtocolClient p) {
        engine = e;
        ship = playerShip;
        protClient = p;
        setUpInputs(gpName);
    }

    public void setUpInputs(String gp) {
        FwdMoveAction fwdMov = new FwdMoveAction();
        SideMoveAction sideMov = new SideMoveAction();
        UpMoveAction upMov = new UpMoveAction();
        DownMoveAction downMov = new DownMoveAction();
        BoostAction boostMove = new BoostAction();
        BreakAction breakMov = new BreakAction();

        InputManager im = engine.getInputManager();
        im.associateAction(gp, net.java.games.input.Component.Identifier.Axis.X, sideMov,
                InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
        im.associateAction(gp, net.java.games.input.Component.Identifier.Axis.Y, fwdMov,
                InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
        im.associateAction(gp, net.java.games.input.Component.Identifier.Button._0, upMov,
                InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
        im.associateAction(gp, net.java.games.input.Component.Identifier.Button._9, downMov,
                InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
        im.associateAction(gp, net.java.games.input.Component.Identifier.Button._8, boostMove,
                InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
        im.associateAction(gp, net.java.games.input.Component.Identifier.Axis.Z, breakMov,
                InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);

    }

    public void updateLocation() {
        Vector3f loc = ship.getWorldLocation();
        Vector3f momentumVector = new Vector3f(xVel, yVel, zVel);
        loc.add(momentumVector);
        Vector3f newLocation = loc;
        ship.setLocalLocation(newLocation);
        protClient.sendMoveMessage(ship.getWorldLocation());
        // System.out.println(slowDown);
    }

    public void SlowDown() {
        if (zVel > 0) {
            zVel -= slowDown;
        }
        if (zVel < 0) {
            zVel += slowDown;
        }

        if (xVel > 0) {
            xVel -= slowDown;
        }
        if (xVel < 0) {
            xVel += slowDown;
        }

        if (yVel > 0)
            yVel -= slowDown;
        if (yVel < 0)
            yVel += slowDown;

        // if(MAX_SPEED > 3){
        // MAX_SPEED -= slowDown;
        // }

    }

    private float getMagnitude(float x, float y, float z) {
        float magnitude;
        x = (float) Math.abs(x);
        y = (float) Math.abs(y);
        z = (float) Math.abs(z);
        magnitude = (float) (Math.pow(x, 2) + Math.pow(y, 2));
        magnitude = (float) (Math.sqrt(magnitude));
        magnitude = (float) (Math.pow(magnitude, 2) + Math.pow(z, 2));
        magnitude = (float) (Math.sqrt(magnitude));
        return magnitude;
    }

    private class FwdMoveAction extends AbstractInputAction {
        public void performAction(float time, Event event) {
            float fwdSpeed = -0.05f;
            if ((event.getValue() <= -0.1f || event.getValue() >= 0.1f)) {
                Vector3f fwd = ship.getWorldForwardVector().mul(fwdSpeed * event.getValue());
                float newxVel = xVel + fwd.x;
                float newyVel = yVel + fwd.y;
                float newzVel = zVel + fwd.z;
                if (getMagnitude(newxVel, newyVel, newzVel) <= MAX_SPEED) {
                    xVel = newxVel;
                    yVel = newyVel;
                    zVel = newzVel;
                }
            }
        }
    }

    private class SideMoveAction extends AbstractInputAction {
        public void performAction(float time, Event event) {
            float sideSpeed = 0.04f;
            if ((event.getValue() <= -0.1f || event.getValue() >= 0.1f)) {
                Vector3f side = ship.getWorldRightVector().mul(sideSpeed * event.getValue());
                float newxVel = xVel + side.x;
                float newyVel = yVel + side.y;
                float newzVel = zVel + side.z;
                if (getMagnitude(newxVel, newyVel, newzVel) < MAX_SPEED) {
                    xVel = newxVel;
                    yVel = newyVel;
                    zVel = newzVel;
                }
            }
        }
    }

    private class UpMoveAction extends AbstractInputAction {
        public void performAction(float time, Event event) {
            float upSpeed = 0.05f;
            Vector3f up = ship.getWorldUpVector().mul(upSpeed);
            float newxVel = xVel + up.x;
            float newyVel = yVel + up.y;
            float newzVel = zVel + up.z;
            if (getMagnitude(newxVel, newyVel, newzVel) < MAX_SPEED) {
                xVel = newxVel;
                yVel = newyVel;
                zVel = newzVel;
            }
            System.out.println("Blastoff");
        }
    }

    private class DownMoveAction extends AbstractInputAction {
        public void performAction(float time, Event event) {
            float downSpeed = -0.05f;
            Vector3f down = ship.getWorldUpVector().mul(downSpeed);
            float newxVel = xVel + down.x;
            float newyVel = yVel + down.y;
            float newzVel = zVel + down.z;
            if (getMagnitude(newxVel, newyVel, newzVel) < MAX_SPEED) {
                xVel = newxVel;
                yVel = newyVel;
                zVel = newzVel;
            }
            System.out.println("Blastoff");
        }
    }

    public class BoostAction extends AbstractInputAction {
        public void performAction(float time, Event event) {
            float maxBoostSpeed = 5;
            float boostSpeed = 0.5f;
            Vector3f fwd = ship.getWorldForwardVector().mul(boostSpeed);
            float newxVel = xVel + fwd.x;
            float newyVel = yVel + fwd.y;
            float newzVel = zVel + fwd.z;
            if (getMagnitude(newxVel, newyVel, newzVel) < MAX_SPEED) {
                xVel = newxVel;
                yVel = newyVel;
                zVel = newzVel;
            }
        }
    }

    public class BreakAction extends AbstractInputAction {
        public void performAction(float time, Event event) {
            if (event.getValue() >= 0.1f) {
                slowDown = speedDropOffBase * (float) Math.pow((2 + event.getValue()), 2);
            } else {
                slowDown = speedDropOffBase;
            }

        }
    }

    // private class RollRightAction extends AbstractInputAction {
    // public void performAction(float time, Event event) {
    // Matrix4f rotMat = new Matrix4f();
    // rotMat.rotateZ(rollSpeed * time);
    // rotMat.mul(ship.getLocalRotation());
    // ship.setLocalRotation(rotMat);
    // System.out.println("Rolling Right " + );
    // }
    // }

    // private class RollLeftAction extends AbstractInputAction {
    // public void performAction(float time, Event event) {
    // ship.setLocalRotation(ship.getLocalRotation().rotate(-rollSpeed * time, 0, 0,
    // 1));
    // System.out.println("Rolling Left");
    // }
    // }
}
