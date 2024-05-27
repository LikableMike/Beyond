package tage.nodeControllers;

import tage.GameObject;
import tage.NodeController;
import tage.rml.Matrix4f;
import tage.input.action.AbstractInputAction;
import tage.input.action.IAction;
import tage.rml.Matrix4f;
import tage.*;
import java.util.Vector;
import org.joml.*;

public class DoorController extends NodeController {
    private Vector3f slideVector = new Vector3f(0, 0, .1f);
    private Vector3f curLocation, newLocation;
    private float slideSpeed = .01f;
    private Engine engine;
    boolean open = false;

    public DoorController() {
        super();
    }

    public DoorController(Engine e, float speed) {
        super();
        this.engine = e;
        this.slideVector = new Vector3f(0,0,speed);
    }

    public void setSpeed(float s) {
        this.slideSpeed = s;
    }

    public void setOpen(boolean b) {
        this.open = b;
    }

    public void apply(GameObject go) {
        float elapsedTime = super.getElapsedTime();
        curLocation = go.getLocalLocation();
        float slideAmount = slideSpeed;
        System.out.println("applying door");

        newLocation = curLocation.add(slideVector);

        if (newLocation.z < 2.8f && newLocation.z > 0f) {
            System.out.println("Trying to Open " + newLocation.z);
            go.setLocalLocation(newLocation);
        } else if (newLocation.z > 2.8f) {
            this.open = true;
            go.setLocalLocation(new Vector3f(0, 0, 2.8f));
            slideVector.mul(-1);
            this.toggle();
        } else {
            this.open = false;
            go.setLocalLocation(new Vector3f(0, 0, 0.01f));
            slideVector.mul(-1);
            this.toggle();
        }

    }
}
