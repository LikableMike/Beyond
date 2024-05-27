package tage;

import org.joml.*;

import java.lang.Math;
import tage.*;
import tage.input.action.AbstractInputAction;
import tage.input.action.IAction;
import tage.input.*;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;
import net.java.games.input.Version;
import net.java.games.input.Component;
import net.java.games.input.*;
import tage.audio.*;

public class CameraFirstPersonController {
    private Engine engine;
    private ProtocolClient protClient;
    private Camera camera;
    private GameObject player;
    private float xVel;
    private float yVel;
    private float rollAngle;
    private float slowDown = 0.01f;
    private float rollSpeed = 0.03f;
    private float turnSpeed = 0.03f;

    private ShipController ship;
    // Sounds
    private Sound leftRoll, rightRoll;

    public CameraFirstPersonController(Camera cam, GameObject play, String gpName, String mn, Engine e, ProtocolClient p) {
        engine = e;
        protClient = p;
        camera = cam;
        player = play;
        setupInputs(gpName, mn);
        updateCameraPosition();
    }

    private void setupInputs(String gp, String mn) {
        VerticalLookAction vertLook = new VerticalLookAction();
        HorizontalLookAction horzLook = new HorizontalLookAction();
        RollRightAction rightRoll = new RollRightAction();
        RollLeftAction leftRoll = new RollLeftAction();
        LeftLookAction leftLook = new LeftLookAction();
        RightLookAction rightLook = new RightLookAction();
        UpLookAction upLook = new UpLookAction();
        DownLookAction downLook = new DownLookAction();

        InputManager im = engine.getInputManager();
        if(gp != null){
            im.associateAction(gp, net.java.games.input.Component.Identifier.Axis.RY, vertLook,
                    InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
            im.associateAction(gp, net.java.games.input.Component.Identifier.Axis.RX, horzLook,
                    InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
            im.associateAction(gp, net.java.games.input.Component.Identifier.Button._4, leftRoll,
                    InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
            im.associateAction(gp, net.java.games.input.Component.Identifier.Button._5, rightRoll,
                    InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
        }
        im.associateAction(mn, net.java.games.input.Component.Identifier.Axis.Y, upLook,
                InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
        im.associateAction(mn, net.java.games.input.Component.Identifier.Key.DOWN, downLook,
                InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
        im.associateAction(mn, net.java.games.input.Component.Identifier.Axis.X, rightLook,
                InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
        im.associateAction(mn, net.java.games.input.Component.Identifier.Key.LEFT, leftLook,
                InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
        im.associateAction(net.java.games.input.Component.Identifier.Key.Q, leftRoll,
                InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
        im.associateAction(net.java.games.input.Component.Identifier.Key.E, rightRoll,
                InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
    }

    public void updateCameraPosition() {
        Matrix4f rotMat = new Matrix4f();
        yVel = yVel % 360;
        xVel = xVel % 360;
        rotMat.rotate((float) Math.toRadians(rollAngle), player.getWorldForwardVector());
        rotMat.rotate((float) Math.toRadians(-xVel), player.getWorldUpVector());
        rotMat.rotate((float) Math.toRadians(-yVel), player.getWorldRightVector());
        rotMat.mul(player.getLocalRotation());
        player.setLocalRotation(rotMat);

        // System.out.println(player.getWorldForwardVector().toString());
        // System.out.println(player.getWorldUpVector());
        // System.out.println(player.getWorldRightVector() + "\n");

        // Physics camera controller
        Vector3f physXVelNorm = player.getWorldForwardVector();
        physXVelNorm.cross(player.getWorldUpVector());
        physXVelNorm.mul(-yVel);
        Vector3f physYVelNorm = player.getWorldUpVector();
        physYVelNorm.cross(player.getWorldRightVector());
        physYVelNorm.mul(rollAngle);
        Vector3f physZVelNorm = player.getWorldForwardVector();
        physZVelNorm.cross(player.getWorldRightVector());
        physZVelNorm.mul(xVel);

        float[] physTotalVel = { physXVelNorm.x() + physYVelNorm.x() + physZVelNorm.x(),
                physXVelNorm.y() + physYVelNorm.y() + physZVelNorm.y(),
                physXVelNorm.z() + physYVelNorm.z() + physZVelNorm.z() };

        this.player.getPhysicsObject().setAngularVelocity(physTotalVel);

        double[] hold = this.player.getPhysicsObject().getTransform();
        Matrix4f physRotation = new Matrix4f((float) hold[0], (float) hold[1], (float) hold[2], 0, (float) hold[4],
                (float) hold[5], (float) hold[6], 0, (float) hold[8], (float) hold[9], (float) hold[10], 0, 0, 0, 0,
                1f);

        player.setLocalRotation(physRotation);

        /*
         * Might Need to send all the vectors for this
         * protClient.sendMoveMessage()
         */

        Vector3f up, right, fwd;
        up = player.getWorldUpVector();
        right = player.getWorldRightVector();
        fwd = player.getWorldForwardVector();
        camera.setU(right);
        camera.setV(up);
        camera.setN(fwd);

        // System.out.println(xVel + " " + yVel);

    }

    public float getXVel() {
        return this.xVel;
    }

    public float getYVel() {
        return this.yVel;
    }

    public void setXVel(float x) {
        this.xVel = x;
    }

    public void setYVel(float y) {
        this.yVel = y;
    }

    public void SlowDown() {
        if (xVel != 0) {
            if (xVel < 0)
                xVel += slowDown;
            if (xVel > 0)
                xVel -= slowDown;
        }

        if (yVel != 0) {
            if (yVel < 0)
                yVel += slowDown;
            if (yVel > 0)
                yVel -= slowDown;
        }

        if (rollAngle > 0) {
            rollAngle -= slowDown;
        }
        if (rollAngle < 0) {
            rollAngle += slowDown;
        }

        // System.out.println("XVel: " + xVel + " YVel: " + yVel + " Roll Speed: " +
        // rollSpeed);
    }

    public void setProtClient(ProtocolClient prot) {
        this.protClient = prot;
    }

    public void setShip(ShipController ship) {
        this.ship = ship;
    }

    private class VerticalLookAction extends AbstractInputAction {
        public void performAction(float time, Event event) {
            if (event.getValue() < -0.1 || event.getValue() > 0.1) {
                if (yVel < 5f && yVel > -5f)

                    yVel += event.getValue() * turnSpeed;
            }
            // updateCameraPosition();
            protClient.sendRotationMessage(player.getLocalRotation());
        }

    }

    private class HorizontalLookAction extends AbstractInputAction {
        public void performAction(float time, Event event) {
            if (event.getValue() < -0.1 || event.getValue() > 0.1) {
                if (xVel < 5f && xVel > -5f) {
                    xVel += event.getValue() * turnSpeed;
                }
            }
            // updateCameraPosition();
            protClient.sendRotationMessage(player.getLocalRotation());

        }
    }

    private class RollRightAction extends AbstractInputAction {
        public void performAction(float time, Event event) {
            if (rollAngle < 2)
                rollAngle += rollSpeed;
            protClient.sendRotationMessage(player.getLocalRotation());
            // if (!ship.getBoosterSound().getIsPlaying()) {
            // ship.getBoosterSound().setLocation(ship.getLeftSpeaker().getWorldLocation());
            // ship.getBoosterSound().play();
            // }

        }
    }

    private class RollLeftAction extends AbstractInputAction {
        public void performAction(float time, Event event) {
            if (rollAngle > -2)
                rollAngle -= rollSpeed;
            protClient.sendRotationMessage(player.getLocalRotation());
            // if (!ship.getBoosterSound().getIsPlaying()) {
            // ship.getBoosterSound().setLocation(ship.getRightSpeaker().getWorldLocation());
            // ship.getBoosterSound().play();
            // }
        }
    }

    //---------KEYBOARD SPECIFIC CONTROLLS---------
    private class UpLookAction extends AbstractInputAction {
        public void performAction(float time, Event event) {
            if (event.getValue() < -0.1 || event.getValue() > 0.1) {
                if (yVel < 5f && yVel > -5f)

                    yVel += event.getValue() / 10 * turnSpeed;
            }
            // updateCameraPosition();
            protClient.sendRotationMessage(player.getLocalRotation());
        }

    }

    private class DownLookAction extends AbstractInputAction {
        public void performAction(float time, Event event) {
            if (event.getValue() < -0.1 || event.getValue() > 0.1) {
                if (yVel < 5f && yVel > -5f)

                    yVel += event.getValue()/ 10 * turnSpeed;
            }
            // updateCameraPosition();
            protClient.sendRotationMessage(player.getLocalRotation());
        }

    }

    private class LeftLookAction extends AbstractInputAction {
        public void performAction(float time, Event event) {
            if (event.getValue() < -0.1 || event.getValue() > 0.1) {
                if (xVel < 5f && xVel > -5f) {
                    xVel -= event.getValue() / 10 * turnSpeed;
                }
            }
            // updateCameraPosition();
            protClient.sendRotationMessage(player.getLocalRotation());

        }
    }

    private class RightLookAction extends AbstractInputAction {
        public void performAction(float time, Event event) {
            if (event.getValue() < -0.1 || event.getValue() > 0.1) {
                if (xVel < 5f && xVel > -5f) {
                    xVel += event.getValue() / 10 * turnSpeed;
                }
            }
            // updateCameraPosition();
            protClient.sendRotationMessage(player.getLocalRotation());

        }
    }
}
