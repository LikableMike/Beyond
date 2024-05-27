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

    public CameraFirstPersonController(Camera cam, GameObject play, String gpName, Engine e, ProtocolClient p) {
        engine = e;
        protClient = p;
        camera = cam;
        player = play;
        setupInputs(gpName);
        updateCameraPosition();
    }

    private void setupInputs(String gp) {
        VerticalLookAction vertLook = new VerticalLookAction();
        HorizontalLookAction horzLook = new HorizontalLookAction();
        RollRightAction rightRoll = new RollRightAction();
        RollLeftAction leftRoll = new RollLeftAction();
        InputManager im = engine.getInputManager();
        im.associateAction(gp, net.java.games.input.Component.Identifier.Axis.RY, vertLook,
                InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
        im.associateAction(gp, net.java.games.input.Component.Identifier.Axis.RX, horzLook,
                InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
        im.associateAction(gp, net.java.games.input.Component.Identifier.Button._4, leftRoll,
                InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
        im.associateAction(gp, net.java.games.input.Component.Identifier.Button._5, rightRoll,
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

        /*Might Need to send all the vectors for this
        protClient.sendMoveMessage()
        */

        Vector3f up, right, fwd;
        up = player.getWorldUpVector();
        right = player.getWorldRightVector();
        fwd = player.getWorldForwardVector();
        camera.setU(right);
        camera.setV(up);
        camera.setN(fwd);

        System.out.println(xVel + " " + yVel);

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

    private class VerticalLookAction extends AbstractInputAction {
        public void performAction(float time, Event event) {
            if (event.getValue() < -0.1 || event.getValue() > 0.1) {
                if (yVel < 5f && yVel > -5f)
                    yVel += event.getValue() * turnSpeed;
            }
            // updateCameraPosition();
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
        }
    }

    private class RollRightAction extends AbstractInputAction {
        public void performAction(float time, Event event) {
            if (rollAngle < 2)
                rollAngle += rollSpeed;
        }
    }

    private class RollLeftAction extends AbstractInputAction {
        public void performAction(float time, Event event) {
            if (rollAngle > -2)
                rollAngle -= rollSpeed;
            System.out.println("Rolling Left");
        }
    }
}
