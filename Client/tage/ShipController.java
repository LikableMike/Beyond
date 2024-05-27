package tage;

import tage.shapes.*;

import java.lang.Math;
import tage.input.InputManager;
import tage.input.action.AbstractInputAction;
import tage.input.action.IAction;
import tage.nodeControllers.ExplosionController;
import tage.nodeControllers.RotationController;
import tage.physics.JBullet.JBulletPhysicsObject;
import tage.*;
import tage.audio.*;

import java.util.Vector;

import javax.swing.border.LineBorder;

import org.joml.*;

import net.java.games.input.*;
import com.bulletphysics.collision.dispatch.CollisionWorld.ClosestRayResultCallback;
import com.bulletphysics.collision.dispatch.CollisionWorld;
import tage.nodeControllers.RotationController;
import myGame.GhostAvatar;
import myGame.MyGame;

public class ShipController {
    private Engine engine;
    private MyGame game;
    private ProtocolClient protClient;
    private GameObject ship;
    private GameObject camAnchor;
    private GameObject HealthBar, SpeedBar, ResourceBar;
    private GameObject beamL, beamR, beamBall, lastMissle, explosion;
    private ObjShape blastS, beamLShape, beamRShape, healthBarShape, speedBarShape, resourceBarShape, MissleS,
            ExplosionS;
    private TextureImage explosionT;
    private float zVel;
    private float xVel;
    private float yVel;
    private float speedDropOffBase = 400f;
    private float slowDown = 400f;
    private float MAX_SPEED = 2000f;
    private int MAX_STORAGE = 2000;
    private float health = 100f;
    private int playerScore = 0;
    public RotationController blastEffect;
    private boolean TEAM = true;
    private double[] spawnPoint;
    private CameraFirstPersonController cam;
    private boolean firing, missleCharged;
    private long timeSinceLastShot;
    private long lastShotTime = 0;
    private ShootAction shootAction = new ShootAction();

    private float currSpeed, prevSpeed = 0;

    private boolean alive = true;

    // Speakers
    private GameObject backBooster, frontBooster, leftBooster, rightBooster, bottomBooster, topBooster, lazers;
    private Sound booster;

    public ShipController(MyGame game, Engine e) {

        this.game = game;
        this.engine = e;
        this.TEAM = game.getTeam();

    }

    public void setObj(ObjShape shape, ObjShape missle, ObjShape explosion) {
        this.blastS = shape;
        this.beamLShape = new Cube();
        this.beamRShape = new Cube();
        this.healthBarShape = new Cube();
        this.resourceBarShape = new Cube();
        this.speedBarShape = new Cube();
        this.MissleS = missle;
        this.ExplosionS = explosion;

    }

    public void setTextures(TextureImage explosion) {
        this.explosionT = new TextureImage("ExplosionTexture.png");
    }

    public void buildShip(GameObject ship, GameObject anchor) {
        this.ship = ship;
        this.camAnchor = anchor;
        setUpSpeakers();

        beamL = new GameObject(this.ship, beamLShape);
        beamL.setLocalTranslation((new Matrix4f()).translation(-2f, 1.1f, 70f));
        beamL.setLocalScale((new Matrix4f()).scaling(0, 0, 0f));
        beamL.setLocalRotation((new Matrix4f()).rotateY((float) Math.toRadians(1)));
        beamL.applyParentRotationToPosition(true);
        beamL.applyParentScaleToPosition(true);

        beamR = new GameObject(this.ship, beamRShape);
        beamR.setLocalTranslation((new Matrix4f()).translation(2f, 1.1f, 70f));
        beamR.setLocalScale((new Matrix4f()).scaling(0, 0, 0f));
        beamR.setLocalRotation((new Matrix4f()).rotateY((float) Math.toRadians(-1)));
        beamR.applyParentRotationToPosition(true);
        beamR.applyParentScaleToPosition(true);
        blastS.setMatAmb(Utils.BeamAmbient());
        beamBall = new GameObject(ship, blastS);
        beamBall.applyParentRotationToPosition(true);
        beamBall.setLocalScale(new Matrix4f().scaling(1f));
        beamBall.getRenderStates().disableRendering();
        blastEffect = new RotationController(engine, ship.getWorldForwardVector(), .05f);
        (engine.getSceneGraph()).addNodeController(blastEffect);
        blastEffect.addTarget(beamBall);
        blastEffect.toggle();

        HealthBar = new GameObject(camAnchor, healthBarShape);
        HealthBar.setLocalScale((new Matrix4f()).scaling(4f, .3f, .5f));
        HealthBar.setLocalRotation((new Matrix4f()).rotateX((float) Math.toRadians(15)));
        HealthBar.setLocalTranslation((new Matrix4f()).translation(0, -4.6f, 15f));
        HealthBar.applyParentRotationToPosition(true);
        HealthBar.applyParentScaleToPosition(true);
        HealthBar.getShape().setMatAmb(Utils.crystalAmbient());
        HealthBar.getShape().setMatSpe(Utils.crystalSpecular());
        HealthBar.getShape().setMatDif(Utils.crystalDiffuse());

        SpeedBar = new GameObject(camAnchor, speedBarShape);
        SpeedBar.setLocalScale((new Matrix4f()).scaling(1f, 0.01f, .1f));
        SpeedBar.setLocalRotation((new Matrix4f()).rotateX((float) Math.toRadians(16)));
        SpeedBar.setLocalTranslation((new Matrix4f()).translation(1.4f, -8f, 17f));
        SpeedBar.applyParentRotationToPosition(true);
        SpeedBar.applyParentScaleToPosition(true);
        SpeedBar.getShape().setMatAmb(Utils.BeamAmbient());
        SpeedBar.getShape().setMatSpe(Utils.BeamSpecular());
        SpeedBar.getShape().setMatDif(Utils.BeamDiffuse());

        ResourceBar = new GameObject(camAnchor, resourceBarShape);
        ResourceBar.setLocalScale((new Matrix4f()).scaling(0.6f, 0.01f, .1f));
        ResourceBar.setLocalRotation((new Matrix4f()).rotateX((float) Math.toRadians(16)));
        ResourceBar.setLocalTranslation((new Matrix4f()).translation(-0.6f, -8f, 17f));
        ResourceBar.applyParentRotationToPosition(true);
        ResourceBar.applyParentScaleToPosition(true);
        ResourceBar.getShape().setMatAmb(Utils.blueOreAmbient());
        ResourceBar.getShape().setMatSpe(Utils.blueOreSpecular());
        ResourceBar.getShape().setMatDif(Utils.blueOreDiffuse());

    }

    public void setUpInputs(String gp, String mn) {
        FwdMoveAction fwdMov = new FwdMoveAction();
        SideMoveAction sideMov = new SideMoveAction();
        UpMoveAction upMov = new UpMoveAction();
        DownMoveAction downMov = new DownMoveAction();
        BoostAction boostMove = new BoostAction();
        BreakAndShootAction breakAndShoot = new BreakAndShootAction();
        Respawn respawn = new Respawn();
        LeftMoveAction leftMove = new LeftMoveAction();
        RightMoveAction rightMove = new RightMoveAction();
        ForwardMoveAction forwardMove = new ForwardMoveAction();
        BackwardMoveAction backwardMove = new BackwardMoveAction();
        LaunchMissle fireMissle = new LaunchMissle();
        Emote emote = new Emote();

        EndShootAction endShootAction = new EndShootAction();

        InputManager im = engine.getInputManager();
        if (gp != null) {
            im.associateAction(gp, net.java.games.input.Component.Identifier.Axis.X, sideMov,
                    InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
            im.associateAction(gp, net.java.games.input.Component.Identifier.Axis.Y, fwdMov,
                    InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
            im.associateAction(gp, net.java.games.input.Component.Identifier.Button._0, upMov,
                    InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
            im.associateAction(gp, net.java.games.input.Component.Identifier.Button._2, respawn,
                    InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
            im.associateAction(gp, net.java.games.input.Component.Identifier.Button._9, downMov,
                    InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
            im.associateAction(gp, net.java.games.input.Component.Identifier.Button._8, boostMove,
                    InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
            im.associateAction(gp, net.java.games.input.Component.Identifier.Axis.Z, breakAndShoot,
                    InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
            im.associateAction(gp, net.java.games.input.Component.Identifier.Button._1, fireMissle,
                    InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
            im.associateAction(gp, net.java.games.input.Component.Identifier.Axis.POV, emote,
                    InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);

        }
        im.associateAction(net.java.games.input.Component.Identifier.Key.A, leftMove,
                InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
        im.associateAction(net.java.games.input.Component.Identifier.Key.D, rightMove,
                InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
        im.associateAction(net.java.games.input.Component.Identifier.Key.W, forwardMove,
                InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
        im.associateAction(net.java.games.input.Component.Identifier.Key.S, backwardMove,
                InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
        im.associateAction(net.java.games.input.Component.Identifier.Key.SPACE, upMov,
                InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
        im.associateAction(net.java.games.input.Component.Identifier.Key.X, respawn,
                InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
        im.associateAction(net.java.games.input.Component.Identifier.Key.LCONTROL, downMov,
                InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
        im.associateAction(net.java.games.input.Component.Identifier.Key.LSHIFT, boostMove,
                InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
        im.associateAction(mn, net.java.games.input.Component.Identifier.Button.LEFT, shootAction,
                InputManager.INPUT_ACTION_TYPE.ON_PRESS_AND_RELEASE);
        im.associateAction(mn, net.java.games.input.Component.Identifier.Button.RIGHT, fireMissle,
                InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);

    }

    public void setUp(String gpName, String mn, ProtocolClient pc, CameraFirstPersonController cam) {
        this.protClient = pc;
        this.cam = cam;
        this.cam.setShip(this);
        setUpInputs(gpName, mn);
        setSpawn(this.ship.getPhysicsObject().getTransform());
    }

    // Called within the game loop to continually updated the ships location based
    // on its physics object
    public void updateLocation() {
        timeSinceLastShot = System.currentTimeMillis() - lastShotTime;
        ((JBulletPhysicsObject) ship.getPhysicsObject()).getRigidBody().activate(true);

        float[] currentVel = ship.getPhysicsObject().getLinearVelocity();
        currSpeed = getMagnitude(currentVel[0], currentVel[1], currentVel[2]);

        if (prevSpeed - currSpeed > 200) {
            health -= (prevSpeed - currSpeed) / 50;
            game.getCrashSound().play();
            if (health < 0) {
                health = 0;
                this.alive = false;
                game.getHullDamageAlarm().setLocation(camAnchor.getLocalLocation());
                game.getHullDamageAlarm().play();
            }
            HealthBar.setLocalScale((new Matrix4f()).scaling(4f * (this.health / 100), .3f, .5f));
        }

        // System.out.println("prevSpeed: " + prevSpeed + " currSpeed: " + currSpeed);

        Vector3f physLoc = new Vector3f((float) (this.ship.getPhysicsObject().getTransform())[12],
                (float) this.ship.getPhysicsObject().getTransform()[13],
                (float) this.ship.getPhysicsObject().getTransform()[14]);

        // FOV Change depending on speed "Not really acurate but feels nice"

        (engine.getRenderSystem()).setFov(60 + currSpeed / 700);

        ship.setLocalLocation(physLoc);
        checkDeposit();
        updateResourceBar();
        if (protClient != null) {
            protClient.sendMoveMessage(ship.getWorldLocation());
        }
        if (firing) {
            shootAction.performAction(-1, null);
        }
        if (timeSinceLastShot > 3000) {
            missleCharged = true;
        }
        if (lastMissle != null) {
            updateProjectile();
        }

    }

    public void updateProjectile() {
        Vector3f physLoc = new Vector3f((float) lastMissle.getPhysicsObject().getTransform()[12],
                (float) lastMissle.getPhysicsObject().getTransform()[13],
                (float) lastMissle.getPhysicsObject().getTransform()[14]);
        lastMissle.setLocalLocation(physLoc);

    }

    public void updateResourceBar() {
        ResourceBar.setLocalScale(
                (new Matrix4f()).scaling(0.6f, (float) 2 * ((float) playerScore / (float) MAX_STORAGE), .1f));
    }

    // This function is called within the gameloop. It gradually slows down the
    // players ship to mitigate players sailing off into space.
    public void SlowDown() {
        float[] currentVel = ship.getPhysicsObject().getLinearVelocity();
        currentVel[0] += -currentVel[0] / slowDown;
        currentVel[1] += -currentVel[1] / slowDown;
        currentVel[2] += -currentVel[2] / slowDown;
        ship.getPhysicsObject().setLinearVelocity(currentVel);
        SpeedBar.setLocalScale((new Matrix4f()).scaling(1f,
                (getMagnitude(currentVel[0], currentVel[1], currentVel[2]) / this.MAX_SPEED) * 0.3f, .1f));
        prevSpeed = getMagnitude(currentVel[0], currentVel[1], currentVel[2]);
    }

    public void setProtClient(ProtocolClient prot) {
        this.protClient = prot;
    }

    // Gets the ships total inertial magnitude or velocity. Used to set a maximum
    // speed
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

    public float getHealth() {
        return this.health;
    }

    public boolean isAlive() {
        return this.alive;
    }

    public void setHealth(float newHealth) {
        this.health = newHealth;
    }

    public boolean getTeam() {
        return this.TEAM;
    }

    // Checks if the player is close enough to deposit his current resources.
    public void checkDeposit() {
        Vector3f currLocation = ship.getWorldLocation();

        if (TEAM) {
            Vector3f stationLocation = game.getSpaceStationTeam1().getWorldLocation();
            float xyDist = (float) Math.abs(Math.sqrt(Math.pow(stationLocation.x() - currLocation.x(), 2)
                    + Math.pow(stationLocation.y() - currLocation.y(), 2)));
            float zDist = Math.abs(stationLocation.z() - currLocation.z() + 1300);
            if (xyDist < 2500 && zDist < 200) {
                game.AddScoreTeam1(playerScore);
                playerScore = 0;
            }
        } else {
            Vector3f stationLocation = game.getSpaceStationTeam2().getWorldLocation();
            float xyDist = (float) Math.abs(Math.sqrt(Math.pow(stationLocation.x() - currLocation.x(), 2)
                    + Math.pow(stationLocation.y() - currLocation.y(), 2)));
            float zDist = Math.abs(stationLocation.z() - currLocation.z() - 1300);
            if (xyDist < 2500 && zDist < 200) {
                game.AddScoreTeam2(playerScore);
                playerScore = 0;
            }
        }
    }

    public void detonateMissle() {
        // Create new node controll for specific xpolsion object and start it.
        GameObject explosion = new GameObject(GameObject.root(), ExplosionS, explosionT);
        explosion.setLocalTranslation(this.lastMissle.getLocalTranslation());
        ExplosionController explosionEffect = new ExplosionController(this.engine, this.game,
                System.currentTimeMillis());
        explosionEffect.addTarget(explosion);
        explosionEffect.toggle();
        (this.engine.getSceneGraph()).removePhysicsObject(this.lastMissle.getPhysicsObject());
        (this.engine.getSceneGraph()).removeGameObject(this.lastMissle);
        (this.engine.getSceneGraph()).addNodeController(explosionEffect);
        game.getExplosionSound().setLocation(explosion.getLocalLocation());
        game.getExplosionSound().play();

        for (int i = 0; i < game.getGhostManager().getGhosts().size(); i++) {
            float distance = getDistance(explosion.getLocalLocation(),
                    game.getGhostManager().getGhosts().get(i).getLocalLocation());
            if (distance < 3500f) {
                protClient.sendShootMessage(game.getGhostManager().getGhosts().get(i).getID(),
                        (875f / distance) * 100f);
                playerScore += 250f;
            }
        }
        for (int i = 0; i < game.getDrones().length; i++) {
            if (getDistance(explosion.getLocalLocation(), game.getDrones()[i].getLocalLocation()) < 3500f) {
                game.getDrones()[i].hitDrone(50f);
            }
        }
        game.getLight2().setLocation(explosion.getWorldLocation());

        protClient.sendExplosionMessage(explosion.getLocalLocation());
    }

    public float getDistance(Vector3f a, Vector3f b) {
        float xDif = (float) Math.abs(a.x - b.x);
        float yDif = (float) Math.abs(a.y - b.y);
        float zDif = (float) Math.abs(a.z - b.z);

        float distance = (float) (Math.pow(xDif, 2) + Math.pow(yDif, 2));
        distance += (float) (Math.pow(zDif, 2));
        distance = (float) (Math.sqrt(distance));

        return distance;

    }

    public void hitShip(float damage) {
        this.health -= damage;
        if (this.health < 0) {
            this.health = 0;
            if (this.alive) {
                kill();
            }
            this.alive = false;
        }
        HealthBar.setLocalScale((new Matrix4f()).scaling(4f * (this.health / 100), .3f, .5f));
        if (damage > 1) {
            game.getCrashSound().play();
        }
    }

    public GameObject getBeamHit() {
        return this.beamBall;
    }

    public int getPlayerScore() {
        return this.playerScore;
    }

    private void kill() {
        game.getHullDamageAlarm().setLocation(camAnchor.getLocalLocation());
        game.getHullDamageAlarm().play();
        this.ship.getShape().setMatAmb(Utils.crystalAmbient());
        this.ship.getShape().setMatDif(Utils.crystalDiffuse());
        this.ship.getShape().setMatSpe(Utils.crystalSpecular());
    }

    public void setSpawn(double[] spawn) {
        this.spawnPoint = spawn;
    }

    public void setUpSpeakers() {
        AudioResource boosterR;
        boosterR = engine.getAudioManager().createAudioResource("assets/sounds/BackWardRocket.wav",
                AudioResourceType.AUDIO_STREAM);
        booster = new Sound(boosterR, SoundType.SOUND_EFFECT, 100, true);
        booster.initialize(engine.getAudioManager());
        booster.setMaxDistance(300f);
        booster.setMinDistance(0.5f);
        booster.setRollOff(0.01f);

        backBooster = new GameObject(ship, blastS);
        backBooster.setLocalLocation(new Vector3f(0f, -1f, -200f));
        backBooster.getRenderStates().disableRendering();

        frontBooster = new GameObject(ship, blastS);
        frontBooster.setLocalLocation(new Vector3f(0f, -1f, 200f));
        frontBooster.getRenderStates().disableRendering();

        leftBooster = new GameObject(ship, blastS);
        leftBooster.setLocalLocation(new Vector3f(-100f, -1f, 0f));
        leftBooster.getRenderStates().disableRendering();

        rightBooster = new GameObject(ship, blastS);
        rightBooster.setLocalLocation(new Vector3f(100f, -1f, 0f));
        rightBooster.getRenderStates().disableRendering();
    }

    public Sound getBoosterSound() {
        return this.booster;
    }

    public GameObject getLeftSpeaker() {
        return this.leftBooster;
    }

    public GameObject getRightSpeaker() {
        return this.rightBooster;
    }

    // Controlls the players forward and backward movement. This is based off of the
    // players current forward vector
    private class FwdMoveAction extends AbstractInputAction {
        public void performAction(float time, Event event) {
            float fwdSpeed = -40f;
            if ((event.getValue() <= -0.1f || event.getValue() >= 0.1f)) {
                Vector3f fwd = ship.getWorldForwardVector().mul(fwdSpeed * event.getValue());
                float newxVel = fwd.x;
                float newyVel = fwd.y;
                float newzVel = fwd.z;
                if (getMagnitude(newxVel, newyVel, newzVel) <= MAX_SPEED) {
                    xVel = newxVel;
                    yVel = newyVel;
                    zVel = newzVel;

                }
                float[] momentumVector = { xVel, yVel, zVel };
                float[] currentVel = ship.getPhysicsObject().getLinearVelocity();
                currentVel[0] += momentumVector[0];
                currentVel[1] += momentumVector[1];
                currentVel[2] += momentumVector[2];
                if (alive) {
                    ship.getPhysicsObject().setLinearVelocity(currentVel);
                }
                if (!(game.getSlowRocket().getIsPlaying()) && event.getValue() >= 0.1f) {
                    game.getSlowRocket().setLocation(frontBooster.getWorldLocation());
                    game.getSlowRocket().play();
                } else if (!(game.getBackwardRocket().getIsPlaying()) && event.getValue() <= 0.1f) {
                    game.getSlowRocket().setLocation(backBooster.getWorldLocation());
                    game.getBackwardRocket().play();
                }

            } else {
                game.getSlowRocket().stop();
                game.getBackwardRocket().stop();
                game.getBoostRocket().stop();
            }
            // protClient.sendMoveMessage(ship.getWorldLocation());
        }
    }

    // Controlls the players left and right movement based off their Right Vector
    private class SideMoveAction extends AbstractInputAction {
        public void performAction(float time, Event event) {
            float sideSpeed = 15f;
            if ((event.getValue() <= -0.1f || event.getValue() >= 0.1f)) {
                Vector3f side = ship.getWorldRightVector().mul(sideSpeed * event.getValue());
                float newxVel = side.x;
                float newyVel = side.y;
                float newzVel = side.z;
                if (getMagnitude(newxVel, newyVel, newzVel) < MAX_SPEED) {
                    xVel = newxVel;
                    yVel = newyVel;
                    zVel = newzVel;
                }
                float[] momentumVector = { xVel, yVel, zVel };
                float[] currentVel = ship.getPhysicsObject().getLinearVelocity();
                currentVel[0] += momentumVector[0];
                currentVel[1] += momentumVector[1];
                currentVel[2] += momentumVector[2];
                if (alive) {
                    ship.getPhysicsObject().setLinearVelocity(currentVel);
                    if (event.getValue() >= 0.1f) {
                        booster.setLocation(rightBooster.getWorldLocation());
                    } else if (event.getValue() <= 0.1f) {
                        booster.setLocation(leftBooster.getWorldLocation());
                    }
                    if (!booster.getIsPlaying()) {
                        booster.play();
                    }
                }

            } else {
                booster.stop();
            }
            // protClient.sendMoveMessage(ship.getWorldLocation());
        }
    }

    // Controlls the players up and down movement based off of their current Up
    // Vector
    private class UpMoveAction extends AbstractInputAction {
        public void performAction(float time, Event event) {
            float upSpeed = 15f;
            Vector3f up = ship.getWorldUpVector().mul(upSpeed);
            float newxVel = up.x;
            float newyVel = up.y;
            float newzVel = up.z;
            if (getMagnitude(newxVel, newyVel, newzVel) < MAX_SPEED) {
                xVel = newxVel;
                yVel = newyVel;
                zVel = newzVel;
            }
            float[] momentumVector = { xVel, yVel, zVel };
            float[] currentVel = ship.getPhysicsObject().getLinearVelocity();
            currentVel[0] += momentumVector[0];
            currentVel[1] += momentumVector[1];
            currentVel[2] += momentumVector[2];

            if (alive) {
                ship.getPhysicsObject().setLinearVelocity(currentVel);
            }

            // protClient.sendMoveMessage(ship.getWorldLocation());
        }
    }

    private class DownMoveAction extends AbstractInputAction {
        public void performAction(float time, Event event) {
            float downSpeed = -15f;
            Vector3f down = ship.getWorldUpVector().mul(downSpeed);
            float newxVel = down.x;
            float newyVel = down.y;
            float newzVel = down.z;
            if (getMagnitude(newxVel, newyVel, newzVel) < MAX_SPEED) {
                xVel = newxVel;
                yVel = newyVel;
                zVel = newzVel;
            }
            float[] momentumVector = { xVel, yVel, zVel };
            float[] currentVel = ship.getPhysicsObject().getLinearVelocity();
            currentVel[0] += momentumVector[0];
            currentVel[1] += momentumVector[1];
            currentVel[2] += momentumVector[2];

            if (alive) {
                ship.getPhysicsObject().setLinearVelocity(currentVel);
            }

            // protClient.sendMoveMessage(ship.getWorldLocation());
        }
    }

    public class BoostAction extends AbstractInputAction {
        public void performAction(float time, Event event) {
            float maxBoostSpeed = 80f;
            float boostSpeed = 40f;
            Vector3f fwd = ship.getWorldForwardVector().mul(boostSpeed);
            float newxVel = fwd.x;
            float newyVel = fwd.y;
            float newzVel = fwd.z;
            if (getMagnitude(newxVel, newyVel, newzVel) < MAX_SPEED) {
                xVel = newxVel;
                yVel = newyVel;
                zVel = newzVel;
            }
            float[] momentumVector = { xVel, yVel, zVel };
            float[] currentVel = ship.getPhysicsObject().getLinearVelocity();
            currentVel[0] += momentumVector[0];
            currentVel[1] += momentumVector[1];
            currentVel[2] += momentumVector[2];

            if (alive) {
                ship.getPhysicsObject().setLinearVelocity(currentVel);

            }
            if (!game.getBoostRocket().getIsPlaying()) {
                game.getInitiateBoostSound().setLocation(ship.getWorldLocation());
                game.getInitiateBoostSound().play();
                game.getBoostRocket().play();
            }

            // protClient.sendMoveMessage(ship.getWorldLocation());
        }
    }

    // Controlls when the player breaks and fires their weapon as they are both
    // associated with the same device axis
    public class BreakAndShootAction extends AbstractInputAction {
        public void performAction(float time, Event event) {
            // Breaking is accomplished by increasing the slowdown factor if the left
            // trigger is being held down
            if (event.getValue() >= 0.1f) {
                if (alive) {
                    slowDown = 100f;
                }
            } else {
                slowDown = speedDropOffBase;
            }

            // Shooting is currently represented by to cubes that are children of the
            // players ship.
            // These cubes are elongated when the right trigger is being held down to
            // represent and lazer beam
            // There is also some raycasting currently being used to determine if the lazers
            // are hitting anything.
            Matrix4f Active, Inactive;
            Inactive = (new Matrix4f()).scaling(0, 0, 0);
            Active = (new Matrix4f()).scaling(0.1f, 0.1f, 70f);
            if (alive) {
                if (event.getValue() <= -0.1f) {
                    beamL.setLocalScale(Active);
                    beamR.setLocalScale(Active);
                    beamBall.getRenderStates().enableRendering();
                    protClient.sendFiringMessage(true);

                    // BEAM RAY CASTING
                    Vector3f beamEndHold = new Vector3f();
                    ship.getWorldForwardVector().mul(16000, beamEndHold);

                    // The end point of the ray is 10000 units in front of the players ship via
                    // forward vector.
                    javax.vecmath.Vector3f beamEnd = new javax.vecmath.Vector3f(
                            ship.getWorldLocation().x() + beamEndHold.x(),
                            ship.getWorldLocation().y() + beamEndHold.y(),
                            ship.getWorldLocation().z() + beamEndHold.z());

                    // The starting point of the ray is approximately where the players camera is.
                    // Since the camera is 2 local units above the ships origin, I used the fact
                    // that the ships world
                    // up vector is always unit length to determine where 2 units "above" the ship
                    // is located no matter of its orientation
                    javax.vecmath.Vector3f locationHold = new javax.vecmath.Vector3f(
                            ship.getWorldLocation().x() + Math.abs(ship.getWorldUpVector().x() * 2),
                            ship.getWorldLocation().y() + Math.abs(ship.getWorldUpVector().y() * 2),
                            ship.getWorldLocation().z() + Math.abs(ship.getWorldUpVector().z() * 2));

                    // Raycasting stuff
                    ClosestRayResultCallback beamRay = new ClosestRayResultCallback(
                            locationHold, beamEnd);
                    engine.getSceneGraph().getPhysicsEngine().getDynamicsWorld().rayTest(locationHold, beamEnd,
                            beamRay);

                    // The Ship casts a ray forward and determines the first object it hits. Then it
                    // reduces the "Asteroid Ore" shape's scale for the hit asteroid
                    if (beamRay.hasHit()) {
                        beamBall.getRenderStates().enableRendering();
                        if (!(beamRay.collisionObject.getCollisionShape().getUserPointer() == null)) {

                            if (((GameObject) beamRay.collisionObject.getCollisionShape().getUserPointer())
                                    .getType() == GameObject.OBJECT_TYPE.ORE &&
                                    playerScore < MAX_STORAGE) {
                                AsteroidOre hitAsteroid = ((AsteroidOre) beamRay.collisionObject.getCollisionShape()
                                        .getUserPointer());
                                Matrix4f currScale = hitAsteroid.getLocalScale();
                                // Check If asteroid still has ore to mine (based on scale of ore)
                                if (hitAsteroid.getInitialScale() / currScale.get(0, 0) < 1.6f) {
                                    hitAsteroid.setLocalScale(currScale.scale(new Vector3f(.999f, .999f, .999f)));
                                    // System.out.println(hitAsteroid.getInitialScale() + " / " + currScale.get(0,
                                    // 0));
                                    protClient.sendAsteroidShrinkMessage(hitAsteroid.getIndex());
                                    playerScore++;
                                }
                            } else if (((GameObject) beamRay.collisionObject.getCollisionShape().getUserPointer())
                                    .getType() == GameObject.OBJECT_TYPE.ENEMY) {
                                protClient.sendShootMessage(
                                        ((GhostAvatar) beamRay.collisionObject.getCollisionShape().getUserPointer())
                                                .getID(),
                                        1f);
                                System.out.println("Enemy Hit"
                                        + ((GhostAvatar) beamRay.collisionObject.getCollisionShape().getUserPointer())
                                                .getID()
                                                .toString());
                                playerScore += 2;
                            } else if (((GameObject) beamRay.collisionObject.getCollisionShape().getUserPointer())
                                    .getType() == GameObject.OBJECT_TYPE.ENEMY_DRONE) {
                                ((Drone) beamRay.collisionObject.getCollisionShape()
                                        .getUserPointer()).hitDrone(1);
                            }
                        }
                        beamBall.setLocalTranslation(new Matrix4f().translation(0,
                                15f, 10000 * beamRay.closestHitFraction));

                        game.getLazerImpact().setLocation(beamBall.getWorldLocation());
                        if (!game.getLazerImpact().getIsPlaying()) {
                            game.getLazerImpact().play();
                        }
                    } else {
                        // Debugg ball to show where the beams end point is while the trigger is down.
                        beamBall.setLocalTranslation(
                                new Matrix4f().translation(0, 15f, 10000));
                        // beamBall.getRenderStates().disableRendering();
                        game.getLazerImpact().stop();
                    }

                    if (!game.getLazerClose().getIsPlaying()) {
                        game.getLazerClose().play();
                    }

                    // If the trigger is released the cubes return to their small shape.
                } else {
                    beamL.setLocalScale(Inactive);
                    beamR.setLocalScale(Inactive);
                    beamBall.getRenderStates().disableRendering();
                    protClient.sendFiringMessage(false);
                    game.getLazerImpact().stop();
                    game.getLazerClose().stop();
                }
            }
            // protClient.sendMoveMessage(ship.getWorldLocation());

        }
    }

    public class Respawn extends AbstractInputAction {
        public void performAction(float time, Event event) {
            if (!alive) {
                alive = true;
                health = 100f;
                HealthBar.setLocalScale((new Matrix4f()).scaling(4f, .3f, .5f));
                playerScore = 0;
                ship.getPhysicsObject().setLinearVelocity(new float[] { 0f, 0f, 0f });
                ship.getPhysicsObject().setAngularVelocity(new float[] { 0f, 0f, 0f });
                ship.getPhysicsObject().setTransform(spawnPoint);
                game.getHullDamageAlarm().stop();
            }
        }

    }

    public class LaunchMissle extends AbstractInputAction {
        public void performAction(float time, Event event) {
            if (missleCharged) {
                System.out.println("PEW");
                lastShotTime = System.currentTimeMillis();
                GameObject Missle = new GameObject(GameObject.root(), MissleS, explosionT);
                Vector3f bulletSpawn = ship.getLocalLocation();
                Vector3f bulletSpawnOffset = (new Vector3f(ship.getWorldForwardVector())).mul(250f);
                bulletSpawn.add(bulletSpawnOffset);
                Missle.setLocalLocation(bulletSpawnOffset);
                Missle.setLocalScale((new Matrix4f()).scaling(35f));
                Missle.setLocalRotation(ship.getLocalRotation());
                double[] misslePhysSpawn = ship.getPhysicsObject().getTransform();
                misslePhysSpawn[12] = (double) bulletSpawn.x;
                misslePhysSpawn[13] = (double) bulletSpawn.y;
                misslePhysSpawn[14] = (double) bulletSpawn.z;
                Missle.setPhysicsObject(
                        (engine.getSceneGraph()).addPhysicsSphere(1, misslePhysSpawn, 50f));
                (engine.getSceneGraph()).getPhysicsEngine().getObjects().lastElement().getCollisionShape()
                        .setUserPointer(Missle);
                Missle.setType(GameObject.OBJECT_TYPE.MY_MISSLE);
                float[] shootVelocity = { ship.getLocalForwardVector().mul(20000f).x,
                        ship.getLocalForwardVector().mul(20000f).y,
                        ship.getLocalForwardVector().mul(20000f).z };
                Missle.getPhysicsObject().setLinearVelocity(shootVelocity);
                if (lastMissle != null) {
                    (engine.getSceneGraph()).removePhysicsObject(lastMissle.getPhysicsObject());
                    (engine.getSceneGraph()).removeGameObject(lastMissle);
                }
                lastMissle = Missle;
                missleCharged = false;
            }
        }
    }

    public class Emote extends AbstractInputAction {
        public void performAction(float time, Event event) {
            if (event.getValue() == 0.75) {
                game.getRoboArm().stopAnimation();
                game.getRoboArm().playAnimation("FINGERGUN", 0.5f, AnimatedShape.EndType.STOP, 0);
            }
            System.out.println(event.getValue());
        }
    }

    // ----------KEYBOARD SPECIFIC CONTROLLS----------
    private class LeftMoveAction extends AbstractInputAction {
        public void performAction(float time, Event event) {
            float sideSpeed = -15f;
            if (event.getValue() >= 0.1f) {
                Vector3f side = ship.getWorldRightVector().mul(sideSpeed * event.getValue());
                float newxVel = side.x;
                float newyVel = side.y;
                float newzVel = side.z;
                if (getMagnitude(newxVel, newyVel, newzVel) < MAX_SPEED) {
                    xVel = newxVel;
                    yVel = newyVel;
                    zVel = newzVel;
                }
                float[] momentumVector = { xVel, yVel, zVel };
                float[] currentVel = ship.getPhysicsObject().getLinearVelocity();
                currentVel[0] += momentumVector[0];
                currentVel[1] += momentumVector[1];
                currentVel[2] += momentumVector[2];
                if (alive) {
                    ship.getPhysicsObject().setLinearVelocity(currentVel);
                    if (event.getValue() >= 0.1f) {
                        booster.setLocation(rightBooster.getWorldLocation());
                    } else if (event.getValue() <= 0.1f) {
                        booster.setLocation(leftBooster.getWorldLocation());
                    }
                    if (!booster.getIsPlaying()) {
                        booster.play();
                    }
                }

            } else {
                booster.stop();
            }
            // protClient.sendMoveMessage(ship.getWorldLocation());
        }
    }

    private class RightMoveAction extends AbstractInputAction {
        public void performAction(float time, Event event) {
            float sideSpeed = 15f;
            if (event.getValue() >= 0.1f) {
                Vector3f side = ship.getWorldRightVector().mul(sideSpeed * event.getValue());
                float newxVel = side.x;
                float newyVel = side.y;
                float newzVel = side.z;
                if (getMagnitude(newxVel, newyVel, newzVel) < MAX_SPEED) {
                    xVel = newxVel;
                    yVel = newyVel;
                    zVel = newzVel;
                }
                float[] momentumVector = { xVel, yVel, zVel };
                float[] currentVel = ship.getPhysicsObject().getLinearVelocity();
                currentVel[0] += momentumVector[0];
                currentVel[1] += momentumVector[1];
                currentVel[2] += momentumVector[2];
                if (alive) {
                    ship.getPhysicsObject().setLinearVelocity(currentVel);
                    if (event.getValue() >= 0.1f) {
                        booster.setLocation(rightBooster.getWorldLocation());
                    } else if (event.getValue() <= 0.1f) {
                        booster.setLocation(leftBooster.getWorldLocation());
                    }
                    if (!booster.getIsPlaying()) {
                        booster.play();
                    }
                }

            } else {
                booster.stop();
            }
            // protClient.sendMoveMessage(ship.getWorldLocation());
        }
    }

    private class ForwardMoveAction extends AbstractInputAction {
        public void performAction(float time, Event event) {
            float fwdSpeed = 40f;
            if (event.getValue() >= 0.1f) {
                Vector3f fwd = ship.getWorldForwardVector().mul(fwdSpeed * event.getValue());
                float newxVel = fwd.x;
                float newyVel = fwd.y;
                float newzVel = fwd.z;
                if (getMagnitude(newxVel, newyVel, newzVel) <= MAX_SPEED) {
                    xVel = newxVel;
                    yVel = newyVel;
                    zVel = newzVel;

                }
                float[] momentumVector = { xVel, yVel, zVel };
                float[] currentVel = ship.getPhysicsObject().getLinearVelocity();
                currentVel[0] += momentumVector[0];
                currentVel[1] += momentumVector[1];
                currentVel[2] += momentumVector[2];
                if (alive) {
                    ship.getPhysicsObject().setLinearVelocity(currentVel);
                }
                if (!(game.getSlowRocket().getIsPlaying()) && event.getValue() >= 0.1f) {
                    game.getSlowRocket().setLocation(frontBooster.getWorldLocation());
                    game.getSlowRocket().play();
                } else if (!(game.getBackwardRocket().getIsPlaying()) && event.getValue() <= 0.1f) {
                    game.getSlowRocket().setLocation(backBooster.getWorldLocation());
                    game.getBackwardRocket().play();
                }

            } else {
                game.getSlowRocket().stop();
                game.getBackwardRocket().stop();
                game.getBoostRocket().stop();
            }
            // protClient.sendMoveMessage(ship.getWorldLocation());
        }
    }

    private class BackwardMoveAction extends AbstractInputAction {
        public void performAction(float time, Event event) {
            float fwdSpeed = -40f;
            if (event.getValue() >= 0.1f) {
                Vector3f fwd = ship.getWorldForwardVector().mul(fwdSpeed * event.getValue());
                float newxVel = fwd.x;
                float newyVel = fwd.y;
                float newzVel = fwd.z;
                if (getMagnitude(newxVel, newyVel, newzVel) <= MAX_SPEED) {
                    xVel = newxVel;
                    yVel = newyVel;
                    zVel = newzVel;

                }
                float[] momentumVector = { xVel, yVel, zVel };
                float[] currentVel = ship.getPhysicsObject().getLinearVelocity();
                currentVel[0] += momentumVector[0];
                currentVel[1] += momentumVector[1];
                currentVel[2] += momentumVector[2];
                if (alive) {
                    ship.getPhysicsObject().setLinearVelocity(currentVel);
                }
                if (!(game.getSlowRocket().getIsPlaying()) && event.getValue() >= 0.1f) {
                    game.getSlowRocket().setLocation(frontBooster.getWorldLocation());
                    game.getSlowRocket().play();
                } else if (!(game.getBackwardRocket().getIsPlaying()) && event.getValue() <= 0.1f) {
                    game.getSlowRocket().setLocation(backBooster.getWorldLocation());
                    game.getBackwardRocket().play();
                }

            } else {
                game.getSlowRocket().stop();
                game.getBackwardRocket().stop();
                game.getBoostRocket().stop();
            }
            // protClient.sendMoveMessage(ship.getWorldLocation());
        }
    }

    private class ShootAction extends AbstractInputAction {
        public void performAction(float time, Event event) {
            float value;
            if (event == null) {
                firing = true;
            } else if (event.getValue() == 1) {
                value = 1f;
                firing = true;
            } else {
                firing = false;
            }
            Matrix4f Active, Inactive;
            Inactive = (new Matrix4f()).scaling(0, 0, 0);
            Active = (new Matrix4f()).scaling(0.1f, 0.1f, 70f);
            if (alive) {
                if (firing) {
                    beamL.setLocalScale(Active);
                    beamR.setLocalScale(Active);
                    beamBall.getRenderStates().enableRendering();
                    protClient.sendFiringMessage(true);

                    // BEAM RAY CASTING
                    Vector3f beamEndHold = new Vector3f();
                    ship.getWorldForwardVector().mul(16000, beamEndHold);

                    // The end point of the ray is 10000 units in front of the players ship via
                    // forward vector.
                    javax.vecmath.Vector3f beamEnd = new javax.vecmath.Vector3f(
                            ship.getWorldLocation().x() + beamEndHold.x(),
                            ship.getWorldLocation().y() + beamEndHold.y(),
                            ship.getWorldLocation().z() + beamEndHold.z());

                    // The starting point of the ray is approximately where the players camera is.
                    // Since the camera is 2 local units above the ships origin, I used the fact
                    // that the ships world
                    // up vector is always unit length to determine where 2 units "above" the ship
                    // is located no matter of its orientation
                    javax.vecmath.Vector3f locationHold = new javax.vecmath.Vector3f(
                            ship.getWorldLocation().x() + Math.abs(ship.getWorldUpVector().x() * 2),
                            ship.getWorldLocation().y() + Math.abs(ship.getWorldUpVector().y() * 2),
                            ship.getWorldLocation().z() + Math.abs(ship.getWorldUpVector().z() * 2));

                    // Raycasting stuff
                    ClosestRayResultCallback beamRay = new ClosestRayResultCallback(
                            locationHold, beamEnd);
                    engine.getSceneGraph().getPhysicsEngine().getDynamicsWorld().rayTest(locationHold, beamEnd,
                            beamRay);

                    // The Ship casts a ray forward and determines the first object it hits. Then it
                    // reduces the "Asteroid Ore" shape's scale for the hit asteroid
                    if (beamRay.hasHit()) {
                        beamBall.getRenderStates().enableRendering();
                        if (!(beamRay.collisionObject.getCollisionShape().getUserPointer() == null)) {

                            if (((GameObject) beamRay.collisionObject.getCollisionShape().getUserPointer())
                                    .getType() == GameObject.OBJECT_TYPE.ORE &&
                                    playerScore < MAX_STORAGE) {
                                AsteroidOre hitAsteroid = ((AsteroidOre) beamRay.collisionObject.getCollisionShape()
                                        .getUserPointer());
                                Matrix4f currScale = hitAsteroid.getLocalScale();
                                // Check If asteroid still has ore to mine (based on scale of ore)
                                if (hitAsteroid.getInitialScale() / currScale.get(0, 0) < 1.6f) {
                                    hitAsteroid.setLocalScale(currScale.scale(new Vector3f(.999f, .999f, .999f)));
                                    // System.out.println(hitAsteroid.getInitialScale() + " / " + currScale.get(0,
                                    // 0));
                                    protClient.sendAsteroidShrinkMessage(hitAsteroid.getIndex());
                                    playerScore++;
                                }
                            } else if (((GameObject) beamRay.collisionObject.getCollisionShape().getUserPointer())
                                    .getType() == GameObject.OBJECT_TYPE.ENEMY) {
                                protClient.sendShootMessage(
                                        ((GhostAvatar) beamRay.collisionObject.getCollisionShape().getUserPointer())
                                                .getID(),
                                        1f);
                                System.out.println("Enemy Hit"
                                        + ((GhostAvatar) beamRay.collisionObject.getCollisionShape().getUserPointer())
                                                .getID()
                                                .toString());
                                playerScore += 2;
                            } else if (((GameObject) beamRay.collisionObject.getCollisionShape().getUserPointer())
                                    .getType() == GameObject.OBJECT_TYPE.ENEMY_DRONE) {
                                ((Drone) beamRay.collisionObject.getCollisionShape()
                                        .getUserPointer()).hitDrone(1);
                            }
                        }
                        beamBall.setLocalTranslation(new Matrix4f().translation(0,
                                15f, 10000 * beamRay.closestHitFraction));

                        game.getLazerImpact().setLocation(beamBall.getWorldLocation());
                        if (!game.getLazerImpact().getIsPlaying()) {
                            game.getLazerImpact().play();
                        }
                    } else {
                        // Debugg ball to show where the beams end point is while the trigger is down.
                        beamBall.setLocalTranslation(
                                new Matrix4f().translation(0, 15f, 10000));
                        beamBall.getRenderStates().disableRendering();
                        game.getLazerImpact().stop();
                    }

                    if (!game.getLazerClose().getIsPlaying()) {
                        game.getLazerClose().play();
                    }

                    // If the trigger is released the cubes return to their small shape.
                } else {
                    firing = false;
                    beamL.setLocalScale(Inactive);
                    beamR.setLocalScale(Inactive);
                    beamBall.getRenderStates().disableRendering();
                    protClient.sendFiringMessage(false);
                    game.getLazerImpact().stop();
                    game.getLazerClose().stop();
                }
            }
        }
    }

    private class EndShootAction extends AbstractInputAction {
        public void performAction(float time, Event event) {
            Matrix4f Active, Inactive;
            Inactive = (new Matrix4f()).scaling(0, 0, 0);
            if (alive) {
                if (event.getValue() >= 0.1f) {
                    beamL.setLocalScale(Inactive);
                    beamR.setLocalScale(Inactive);
                    beamBall.getRenderStates().disableRendering();
                    protClient.sendFiringMessage(false);
                    game.getLazerImpact().stop();
                    game.getLazerClose().stop();
                }
            }
        }
    }
}
