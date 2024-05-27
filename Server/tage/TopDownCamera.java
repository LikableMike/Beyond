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

public class TopDownCamera {
    private Engine engine;
    private Camera camera;
    private float cameraHeight;
    private float zoomSpeed = 0.1f;
    private float panSpeed = 0.1f;

    public TopDownCamera(Camera cam, String gpName, Engine e) {
        this.engine = e;
        this.camera = cam;
        setupInputs(gpName);
        // updateCameraPosition();
    }

    private void setupInputs(String gp) {
        CameraZoomInAction ZoomIn = new CameraZoomInAction();
        CameraZoomOutAction ZoomOut = new CameraZoomOutAction();
        CameraPanAction pan = new CameraPanAction();
        InputManager im = engine.getInputManager();
        im.associateAction(gp, net.java.games.input.Component.Identifier.Axis.POV, pan,
                InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
        im.associateAction(gp, net.java.games.input.Component.Identifier.Button._4, ZoomIn,
                InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
        im.associateAction(gp, net.java.games.input.Component.Identifier.Button._5, ZoomOut,
                InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
    }

    private class CameraZoomInAction extends AbstractInputAction {

        public void performAction(float time, Event event) {

            // Up is 0.25
            // Down is 0.75
            // Left is 1
            // Right is 0.5
            if (camera.getLocation().y < 15f) {
                camera.setLocation(new Vector3f(camera.getLocation().x, camera.getLocation().y + zoomSpeed,
                        camera.getLocation().z));
            }
            System.out.println(event.getValue());
        }

    }

    private class CameraZoomOutAction extends AbstractInputAction {

        public void performAction(float time, Event event) {
            if (camera.getLocation().y > 4f) {
                camera.setLocation(new Vector3f(camera.getLocation().x, camera.getLocation().y - zoomSpeed,
                        camera.getLocation().z));
            }
        }
    }

    private class CameraPanAction extends AbstractInputAction {

        public void performAction(float time, Event event) {
            if (event.getValue() == 0.25f) {
                camera.setLocation(new Vector3f(camera.getLocation().x,
                        camera.getLocation().y,
                        camera.getLocation().z - panSpeed));
            } else if (event.getValue() == 0.75f) {
                camera.setLocation(new Vector3f(camera.getLocation().x,
                        camera.getLocation().y,
                        camera.getLocation().z + panSpeed));
            } else if (event.getValue() == 1.0f) {
                camera.setLocation(new Vector3f(camera.getLocation().x - panSpeed,
                        camera.getLocation().y,
                        camera.getLocation().z));
            } else if (event.getValue() == 0.5f) {
                camera.setLocation(new Vector3f(camera.getLocation().x + panSpeed,
                        camera.getLocation().y,
                        camera.getLocation().z));
            }
        }
    }
}