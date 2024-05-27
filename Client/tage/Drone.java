package tage;

import myGame.MyGame;
import tage.physics.JBullet.JBulletPhysicsObject;
import tage.shapes.Sphere;

import java.lang.Math;
import org.joml.*;
import java.util.UUID;

import tage.GameObject.OBJECT_TYPE;
import tage.audio.*;

public class Drone extends GameObject {
    private ProtocolClient protClient;
    private Engine engine;
    private GameObject target;
    private float slowDown = 2000f;
    private float health = 5f;
    private boolean alive = false;
    private int id;
    private Sound beepBoop;
    private ObjShape projectileS;
    private TextureImage projectileT;
    private GameObject lastProjectile;
    private long lastShotTime = 0;
    private long timeSinceLastShot;

    public Drone(int droneID, ObjShape droneS, TextureImage droneT, ObjShape projectileShape,
            TextureImage projectileTexture, ProtocolClient p,
            Engine e) {
        super(GameObject.root(), droneS, droneT);
        protClient = p;
        this.engine = e;
        this.setType(GameObject.OBJECT_TYPE.ENEMY_DRONE);
        id = droneID;
        projectileS = projectileShape;
        projectileT = projectileTexture;

    }

    public void setProtClient(ProtocolClient client) {
        this.protClient = client;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void awaken() {
        this.alive = true;
    }

    public void setUpSound() {
        AudioResource beepBoopR;
        beepBoopR = engine.getAudioManager().createAudioResource("assets/sounds/DroneSound.wav",
                AudioResourceType.AUDIO_STREAM);
        beepBoop = new Sound(beepBoopR, SoundType.SOUND_EFFECT, 100, true);
        beepBoop.initialize(engine.getAudioManager());
        beepBoop.setMaxDistance(600f);
        beepBoop.setMinDistance(0.5f);
        beepBoop.setRollOff(0.1f);
    }

    public void updateLocation() {
        timeSinceLastShot = System.currentTimeMillis() - lastShotTime;
        ((JBulletPhysicsObject) this.getPhysicsObject()).getRigidBody().activate(true);

        Vector3f physLoc = new Vector3f((float) (this.getPhysicsObject().getTransform())[12],
                (float) this.getPhysicsObject().getTransform()[13],
                (float) this.getPhysicsObject().getTransform()[14]);

        this.setLocalLocation(physLoc);

        beepBoop.setLocation(this.getLocalLocation());
        beepBoop.setVelocity(getVelocity());
        if (timeSinceLastShot > 1000 && isNearTarget() && alive) {
            shootProjectile();
        }
        if (this.lastProjectile != null) {
            updateProjectile();
        }
        SlowDown();
    }

    public void shootProjectile() {
        System.out.println("PEW");
        lastShotTime = System.currentTimeMillis();
        GameObject projectile = new GameObject(GameObject.root(), projectileS, projectileT);
        Vector3f bulletSpawn = this.getLocalLocation();
        Vector3f bulletSpawnOffset = (new Vector3f(this.getLocalForwardVector())).mul(300f);

        bulletSpawn.add(bulletSpawnOffset);
        double[] misslePhysSpawn = this.getPhysicsObject().getTransform();
        misslePhysSpawn[12] = (double) bulletSpawn.x;
        misslePhysSpawn[13] = (double) bulletSpawn.y;
        misslePhysSpawn[14] = (double) bulletSpawn.z;
        projectile.setLocalLocation(bulletSpawn);
        projectile.setLocalScale((new Matrix4f()).scaling(35f));
        projectile.setPhysicsObject(
                (engine.getSceneGraph()).addPhysicsSphere(1, misslePhysSpawn, 50f));
        (engine.getSceneGraph()).getPhysicsEngine().getObjects().lastElement().getCollisionShape()
                .setUserPointer(projectile);
        projectile.setType(GameObject.OBJECT_TYPE.BULLET);
        float[] shootVelocity = { this.getLocalForwardVector().mul(10000f).x,
                this.getLocalForwardVector().mul(10000f).y,
                this.getLocalForwardVector().mul(10000f).z };
        projectile.getPhysicsObject().setLinearVelocity(shootVelocity);
        if (this.lastProjectile != null) {
            (engine.getSceneGraph()).removePhysicsObject(this.lastProjectile.getPhysicsObject());
            (engine.getSceneGraph()).removeGameObject(this.lastProjectile);
        }
        if (target != null)
            projectile.lookAt(target);
        this.lastProjectile = projectile;
    }

    public boolean isNearTarget() {
        if (getDistance(this, target) < 10000f) {
            return true;
        }
        return false;
    }

    public void updateProjectile() {
        Vector3f physLoc = new Vector3f((float) lastProjectile.getPhysicsObject().getTransform()[12],
                (float) lastProjectile.getPhysicsObject().getTransform()[13],
                (float) lastProjectile.getPhysicsObject().getTransform()[14]);
        lastProjectile.setLocalLocation(physLoc);

    }

    private float getDistance(GameObject drone, GameObject target) {
        float magnitude;
        if (target != null) {
            float x = (float) Math.abs(target.getWorldLocation().x - drone.getWorldLocation().x);
            float y = (float) Math.abs(target.getWorldLocation().y - drone.getWorldLocation().y);
            float z = (float) Math.abs(target.getWorldLocation().z - drone.getWorldLocation().z);
            magnitude = (float) (Math.pow(x, 2) + Math.pow(y, 2));
            magnitude = (float) (Math.sqrt(magnitude));
            magnitude = (float) (Math.pow(magnitude, 2) + Math.pow(z, 2));
            magnitude = (float) (Math.sqrt(magnitude));
            return magnitude;
        } else {
            return 1;
        }

    }

    private Vector3f getVelocity() {
        float[] magnitude = this.getPhysicsObject().getLinearVelocity();
        Vector3f velocity = new Vector3f(magnitude[0] / 100f, magnitude[1] / 100f, magnitude[2] / 100f);

        return velocity;
    }

    public void move() {
        if (alive) {
            float speed = 10f;
            this.lookAt(target);
            Vector3f fwd = this.getWorldForwardVector().mul(speed);
            float xVel = fwd.x;
            float yVel = fwd.y;
            float zVel = fwd.z;
            float[] momentumVector = { xVel, yVel, zVel };
            float[] currentVel = this.getPhysicsObject().getLinearVelocity();
            currentVel[0] += momentumVector[0];
            currentVel[1] += momentumVector[1];
            currentVel[2] += momentumVector[2];
            this.getPhysicsObject().setLinearVelocity(currentVel);
        } else {
            double[] rotationHold = this.getPhysicsObject().getTransform();
            Matrix4f rotation = new Matrix4f((float) rotationHold[0], (float) rotationHold[1], (float) rotationHold[2],
                    0f,
                    (float) rotationHold[4], (float) rotationHold[5], (float) rotationHold[6], 0f,
                    (float) rotationHold[8], (float) rotationHold[9], (float) rotationHold[10], 0f, 0f, 0f, 0f, 1f);
            this.setLocalRotation(rotation);
        }
        updateLocation();

        if (protClient != null) {
            protClient.sendDroneUpdateMessage(this.id, this.getWorldLocation(), this.getLocalRotation());
        }
    }

    public void SlowDown() {
        float[] currentVel = this.getPhysicsObject().getLinearVelocity();
        currentVel[0] += -currentVel[0] / slowDown;
        currentVel[1] += -currentVel[1] / slowDown;
        currentVel[2] += -currentVel[2] / slowDown;
        this.getPhysicsObject().setLinearVelocity(currentVel);
    }

    public void hitDrone(float damage) {
        this.health -= damage;
        if (this.health < 0) {
            this.health = 0;
            if (this.alive) {
                kill();
            }
            this.alive = false;
        }
    }

    public void kill() {
        float[] deathSpin = { 5f, 4f, 3f };
        beepBoop.stop();
        this.getPhysicsObject().setAngularVelocity(deathSpin);
        if (this.lastProjectile != null) {
            (engine.getSceneGraph()).removePhysicsObject(this.lastProjectile.getPhysicsObject());
            (engine.getSceneGraph()).removeGameObject(this.lastProjectile);
        }
        if (protClient != null) {
            protClient.sendKillDroneMessage(this.id);
        }

    }

    public void spawn(double[] spawnLocation, int id, GameObject target) {
        this.getRenderStates().enableRendering();
        this.getPhysicsObject().setTransform(spawnLocation);
        this.alive = true;
        this.id = id;
        this.target = target;
        Vector3f physLoc = new Vector3f((float) (this.getPhysicsObject().getTransform())[12],
                (float) this.getPhysicsObject().getTransform()[13],
                (float) this.getPhysicsObject().getTransform()[14]);

        beepBoop.play();
        updateLocation();

    }

}
