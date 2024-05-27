package tage;

import org.joml.*;
import java.lang.Math;
import tage.input.action.AbstractInputAction;
import tage.input.action.IAction;
import tage.input.*;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;
import net.java.games.input.Version;
import net.java.games.input.Component;
import net.java.games.input.*;

public class CameraOrbit3D {
    private Engine engine;
    private Camera camera; // the camera being controlled
    private GameObject avatar; // the target avatar the camera looks at
    private float cameraAzimuth; // rotation around target Y axis
    private float cameraElevation; // elevation of camera above target
    private float cameraRadius; // distance between camera and target

    public CameraOrbit3D(Camera cam, GameObject av, String gpName, Engine e) {
        engine = e;
        camera = cam;
        avatar = av;
        cameraAzimuth = 0.0f; // start BEHIND and ABOVE the target
        cameraElevation = 20.0f; // elevation is in degrees
        cameraRadius = 2.0f; // distance from camera to avatar
        setupInputs(gpName);
        updateCameraPosition();
    }

    private void setupInputs(String gp) {
        OrbitAzimuthAction azmAction = new OrbitAzimuthAction();
        InputManager im = engine.getInputManager();
        im.associateAction(gp,
                net.java.games.input.Component.Identifier.Axis.RX, azmAction,
                InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);

        OrbitElevationAction elevationAction = new OrbitElevationAction();
        im.associateAction(gp, net.java.games.input.Component.Identifier.Axis.RY, elevationAction,
                InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);

        OrbitRadiusAction radiusAction = new OrbitRadiusAction();
        im.associateAction(gp,
                net.java.games.input.Component.Identifier.Axis.Z, radiusAction,
                InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
    }

    // Compute the camera’s azimuth, elevation, and distance, relative to
    // the target in spherical coordinates, then convert to world Cartesian
    // coordinates and set the camera position from that.
    public void updateCameraPosition() {
        Vector3f avatarRot = avatar.getWorldForwardVector();
        double avatarAngle = Math
                .toDegrees((double) avatarRot.angleSigned(new Vector3f(0, 0, -1), new Vector3f(0, 1, 0)));
        float totalAz = cameraAzimuth - (float) avatarAngle;
        double theta = Math.toRadians(cameraAzimuth);
        double phi = Math.toRadians(cameraElevation);
        float x = cameraRadius * (float) (Math.cos(phi) * Math.sin(theta));
        float y = cameraRadius * (float) (Math.sin(phi));
        float z = cameraRadius * (float) (Math.cos(phi) * Math.cos(theta));
        camera.setLocation(new Vector3f(x, y, z).add(avatar.getWorldLocation()));
        camera.lookAt(avatar);
    }

    private class OrbitAzimuthAction extends AbstractInputAction {

        public void performAction(float time, Event event) {
            float rotAmount;
            if (event.getValue() < -0.2) {
                rotAmount = 1f;
            } else {
                if (event.getValue() > 0.2) {
                    rotAmount = -1f;
                } else {
                    rotAmount = 0.0f;
                }
            }
            cameraAzimuth += rotAmount;
            cameraAzimuth = cameraAzimuth % 360;
            updateCameraPosition();
        }
    }

    private class OrbitElevationAction extends AbstractInputAction {

        public void performAction(float time, Event event) {
            float elevationAmount;
            if (event.getValue() < -0.2) {
                elevationAmount = -1f;
            } else {
                if (event.getValue() > 0.2) {
                    elevationAmount = 1f;
                } else {
                    elevationAmount = 0.0f;
                }
            }
            cameraElevation += elevationAmount;
            if (cameraElevation < 0f)
                cameraElevation = 0f;
            if (cameraElevation > 89f)
                cameraElevation = 89f;
            updateCameraPosition();
        }
    }

    public class OrbitRadiusAction extends AbstractInputAction {

        public void performAction(float time, Event event) {
            float radiusAmount;
            if (event.getValue() < -0.2) {
                radiusAmount = -0.1f;
            } else {
                if (event.getValue() > 0.2) {
                    radiusAmount = 0.1f;
                } else {
                    radiusAmount = 0f;
                }
            }

            cameraRadius += radiusAmount;
            if (cameraRadius > 10.0f)
                cameraRadius = 10.0f;
            if (cameraRadius < 2.7f)
                cameraRadius = 2.7f;
            updateCameraPosition();
        }
    }
}