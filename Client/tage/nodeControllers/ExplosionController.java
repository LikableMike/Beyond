package tage.nodeControllers;

import tage.*;

import org.joml.*;

import myGame.MyGame;

public class ExplosionController extends NodeController {
    private Engine engine;
    private Matrix4f currScale = new Matrix4f();
    private float currScaleFactor = 30f;
    private float scaleDirection = 0f;
    private long explosionTime = 0;
    private MyGame game;

    public ExplosionController(Engine e, MyGame g, long startingTime) {
        super();
        this.engine = e;
        this.game = g;
    }

    public void apply(GameObject go) {
        float elapsedTime = (super.getElapsedTime());
        currScale = go.getLocalScale();
        currScale.scale(currScaleFactor);
        currScaleFactor *= (1 + scaleDirection + 0.9f);
        elapsedTime += elapsedTime;
        if (currScaleFactor > 2000f) {
            scaleDirection = -1f;

        }
        float brightness = currScaleFactor / 2000f;
        game.getLight2().setAmbient(1 * brightness, 0, .04f * brightness);
        game.getLight2().setDiffuse(1 * brightness, 0, 0.05f * brightness);
        game.getLight2().setSpecular(1f * brightness, 0, 0.05f * brightness);
        go.setLocalScale((new Matrix4f()).scaling(currScaleFactor));

        if (currScaleFactor < 25f && this.isEnabled()) {
            this.toggle();
            (engine.getSceneGraph()).removeGameObject(go);
            this.removeTarget(go);
            game.getLight2().setAmbient(0, 0, 0);
            game.getLight2().setDiffuse(0, 0, 0);
            game.getLight2().setSpecular(0, 0, 0);
        }

    }

}
