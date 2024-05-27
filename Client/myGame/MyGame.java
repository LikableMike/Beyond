package myGame;

import tage.*;
import tage.Light.LightType;
import tage.shapes.*;
import tage.input.*;
import tage.input.action.*;
import tage.nodeControllers.DoorController;
import tage.nodeControllers.ExplosionController;
import tage.nodeControllers.RotationController;

import java.lang.Math;
import java.util.ArrayList;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import org.joml.*;
import java.util.UUID;
import java.util.Vector;
import java.util.Random;
import java.net.InetAddress;

import tage.physics.JBullet.JBulletPhysicsEngine;
import tage.physics.JBullet.JBulletPhysicsObject;

import java.net.UnknownHostException;
import tage.audio.*;

//Controller Imports
import net.java.games.input.*;
import net.java.games.input.Component.Identifier.*;
import tage.networking.IGameConnection.ProtocolType;

public class MyGame extends VariableFrameRateGame {
	private static Engine engine;
	private InputManager im;
	private GhostManager gm;

	private boolean paused = false;
	private int counter = 0;
	private double startFrameTime, endFrameTime, elapsTime;

	// Game objects and rednering stuff
	private GameObject beamL, beamR, Ship, Asteroid, Ore, SpaceStation1, SpaceStation2, Missle;
	private ObjShape beamS, ShipS, AsteroidS, SpaceStationS, FakeAsteroid, MissleS, ExplosionS;
	private GameObject[] AsteroidCluster, FakeAsteroids;
	private AsteroidOre[] AsteroidOres;
	private TextureImage AsteroidT, AsteroidH, OreT, ShipT, SpaceStationT;
	private Light light1, light2, headLight;
	private boolean playerOnHomeTeam;

	// CockPit/HUD
	private GameObject CockPit, crossHair;
	private ObjShape CockPitS, crossHairS;
	private TextureImage CockPitT;
	private GameObject Health, Resources, Speed;

	// Asteroid Ores
	private ObjShape BlueOreS, RedOreS, GoldOreS, SilverOreS, BronzeOreS, GreenOreS;
	// Camera Stuff
	private GameObject camAnchor;
	private ObjShape camAnchorS;
	private Camera cam, shipView;
	private Vector3f loc, fwd, up, right, newLocation;
	// ---Hud---
	private GameObject HUD;
	private ObjShape HUDS;
	private TextureImage HUDT;

	// Ghost and Multiplyaer stuff
	private ObjShape ghostS;
	private TextureImage ghostT;
	private String serverAddress;
	private int serverPort;
	private ProtocolType serverProtocol;
	private ProtocolClient protClient;
	private boolean isClientConnected = false;
	private TextureImage explosionT;
	private CameraFirstPersonController fpv;
	private ShipController shipControls;

	// Score counter
	private int teamScore;
	private int playerScore;
	private int Team1Score, Team2Score;

	// SkyBox
	private int stars;

	// Terrain Map requirement
	private TextureImage terrain;
	private ObjShape terrS;
	private GameObject terrainObject;

	// Force Field
	private GameObject forceFieldContactPoint, forceFieldLayer1, forceFieldLayer2;
	private ObjShape forceFieldContactPointS, forceFieldS;
	private TextureImage forceFieldT;

	// Animated Robo Arm
	private GameObject roboArm;
	private AnimatedShape roboArmS;

	// Drone NPC
	private GameObject drone;
	private ObjShape droneS, droneProjectileS;
	private Drone[] droneNPCs;
	private TextureImage droneT, droneProjectileT;
	private ArrayList<Integer> myDrones = new ArrayList<Integer>();

	// Sounds
	private IAudioManager audioMgr;
	private Sound slowRocket, boostRocket, sideRocket, backwardRocket, droneSound, lazerClose, lazerImpact, crashSound,
			hullDamageAlarm, initiateBoost, backgroundRumble, explosionSound;

	private RotationController rc, LeftBeamController, RightBeamController, forceFieldSpin;

	public MyGame(String serverAddress, int serverPort, String protocol) {
		super();
		gm = new GhostManager(this);
		this.serverAddress = serverAddress;
		this.serverPort = serverPort;
		if (protocol.toUpperCase().compareTo("TCP") == 0) {
			this.serverProtocol = ProtocolType.TCP;
		} else {
			this.serverProtocol = ProtocolType.UDP;
		}
	}

	public static void main(String[] args) {
		MyGame game = new MyGame(args[0], Integer.parseInt(args[1]), args[2]);
		engine = new Engine(game);
		game.initializeSystem();
		game.game_loop();
	}

	@Override
	public void loadShapes() {
		beamS = new Cube();
		beamS.setMatAmb(Utils.BeamAmbient());
		beamS.setMatDif(Utils.BeamDiffuse());
		beamS.setMatSpe(Utils.BeamSpecular());
		beamS.setMatShi(70f);

		ShipS = new ImportedModel("SpaceShipUnwrapped.obj");
		ShipS.setMatAmb(Utils.silverAmbient());
		ShipS.setMatDif(Utils.silverDiffuse());
		ShipS.setMatSpe(Utils.silverSpecular());
		ShipS.setMatShi(Utils.silverShininess());
		camAnchorS = new Sphere();
		CockPitS = new ImportedModel("CockPitWithScreen.obj");
		crossHairS = new ImportedModel("CrossHair.obj");

		AsteroidS = new ImportedModel("Asteroid.obj");
		AsteroidS.setMatShi(2);
		FakeAsteroid = new ImportedModel("FakeAsteroid.obj");

		ghostS = new ImportedModel("SpaceShip.obj");
		SpaceStationS = new ImportedModel("SpaceStation.obj");

		terrS = new TerrainPlane(1000);
		terrS.setMatAmb(Utils.silverAmbient());
		terrS.setMatDif(Utils.silverDiffuse());
		terrS.setMatShi(Utils.silverShininess());
		terrS.setMatSpe(Utils.silverSpecular());

		// ForceField
		forceFieldContactPointS = new Sphere();
		forceFieldS = new ImportedModel("NewForceField.obj");

		// Different Ores
		RedOreS = new ImportedModel("Ore.obj");
		RedOreS.setMatAmb(Utils.crystalAmbient());
		RedOreS.setMatDif(Utils.crystalDiffuse());
		RedOreS.setMatShi(Utils.crystalShininess());
		RedOreS.setMatSpe(Utils.crystalSpecular());
		BlueOreS = new ImportedModel("Ore.obj");
		BlueOreS.setMatAmb(Utils.blueOreAmbient());
		BlueOreS.setMatDif(Utils.blueOreDiffuse());
		BlueOreS.setMatShi(Utils.blueOreShininess());
		BlueOreS.setMatSpe(Utils.blueOreSpecular());
		GoldOreS = new ImportedModel("Ore.obj");
		GoldOreS.setMatAmb(Utils.goldAmbient());
		GoldOreS.setMatDif(Utils.goldDiffuse());
		GoldOreS.setMatShi(Utils.goldShininess());
		GoldOreS.setMatSpe(Utils.goldSpecular());
		SilverOreS = new ImportedModel("Ore.obj");
		SilverOreS.setMatAmb(Utils.silverAmbient());
		SilverOreS.setMatDif(Utils.silverDiffuse());
		SilverOreS.setMatShi(Utils.silverShininess());
		SilverOreS.setMatSpe(Utils.silverSpecular());
		BronzeOreS = new ImportedModel("Ore.obj");
		BronzeOreS.setMatAmb(Utils.bronzeAmbient());
		BronzeOreS.setMatDif(Utils.bronzeDiffuse());
		BronzeOreS.setMatShi(Utils.bronzeShininess());
		BronzeOreS.setMatSpe(Utils.bronzeSpecular());

		// Robo Arm
		roboArmS = new AnimatedShape("FingerGunFixed.rkm", "FingerGunFixed.rks");
		roboArmS.loadAnimation("FINGERGUN", "FingerGunFixed.rka");

		// Drone
		droneS = new ImportedModel("Drone.obj");
		droneProjectileS = new ImportedModel("DroneProjectile.obj");

		ObjShape allyShell = new ImportedModel("ShipTeamShell.obj");
		ObjShape enemyShell = new ImportedModel("ShipTeamShell.obj");
		gm.setShells(allyShell, enemyShell);

		ObjShape blastS = new ImportedModel("BeamBlast.obj");
		ObjShape MissleS = new ImportedModel("Missle.obj");
		ExplosionS = new ImportedModel("Explosion.obj");
		shipControls = new ShipController(this, engine);
		shipControls.setObj(blastS, MissleS, ExplosionS);
	}

	@Override
	public void loadTextures() {
		AsteroidT = new TextureImage("AsteroidTexture.png");
		AsteroidH = new TextureImage("Asteroid_01_ao.png");
		terrain = new TextureImage("Terrain.jpg");
		OreT = new TextureImage("OreTextureHighRes.jpg");
		CockPitT = new TextureImage("CockPitWithScreenTexture.png");
		ShipT = new TextureImage("ShapeShipTexture1.png");
		ghostT = ShipT;
		droneT = new TextureImage("DroneTexture.jpg");
		droneProjectileT = new TextureImage("ProjectileTexture.jpg");
		forceFieldT = new TextureImage("ForceFieldTexture.jpg");
		SpaceStationT = new TextureImage("SpaceStationTexture.png");
		explosionT = new TextureImage("ExplosionTexture.png");
		shipControls.setTextures(explosionT);

	}

	@Override
	public void loadSkyBoxes() {
		stars = (engine.getSceneGraph()).loadCubeMap("lightblue");
		(engine.getSceneGraph()).setActiveSkyBoxTexture(stars);
		(engine.getSceneGraph()).setSkyBoxEnabled(true);
	}

	@Override
	public void loadSounds() {
		AudioResource slowRocketR, boostRocketR, sideRocketR, backwardRocketR, droneSoundR, lazerCloseR, lazerImpactR,
				crashSoundR, hullDamageAlarmR, initiateBoostR, explosionSoundR;
		audioMgr = engine.getAudioManager();
		slowRocketR = audioMgr.createAudioResource("assets/sounds/SlowForwardRocket.wav",
				AudioResourceType.AUDIO_STREAM);
		boostRocketR = audioMgr.createAudioResource("assets/sounds/BoostRocket.wav", AudioResourceType.AUDIO_SAMPLE);
		backwardRocketR = audioMgr.createAudioResource("assets/sounds/SideRickets.wav", AudioResourceType.AUDIO_SAMPLE);
		droneSoundR = audioMgr.createAudioResource("assets/sounds/DroneSound.wav", AudioResourceType.AUDIO_SAMPLE);
		lazerCloseR = audioMgr.createAudioResource("assets/sounds/lazerClose.wav", AudioResourceType.AUDIO_SAMPLE);
		lazerImpactR = audioMgr.createAudioResource("assets/sounds/lazerimpact.wav", AudioResourceType.AUDIO_SAMPLE);
		crashSoundR = audioMgr.createAudioResource("assets/sounds/Crash.wav", AudioResourceType.AUDIO_SAMPLE);
		hullDamageAlarmR = audioMgr.createAudioResource("assets/sounds/HullDamageAlarm.wav",
				AudioResourceType.AUDIO_SAMPLE);
		initiateBoostR = audioMgr.createAudioResource("assets/sounds/InitiateBoostSound.wav",
				AudioResourceType.AUDIO_SAMPLE);
		explosionSoundR = audioMgr.createAudioResource("assets/sounds/ExplosionSound.wav",
				AudioResourceType.AUDIO_SAMPLE);

		slowRocket = new Sound(slowRocketR, SoundType.SOUND_EFFECT, 70, true);
		boostRocket = new Sound(boostRocketR, SoundType.SOUND_EFFECT, 80, true);
		backwardRocket = new Sound(backwardRocketR, SoundType.SOUND_EFFECT, 90, true);
		droneSound = new Sound(droneSoundR, SoundType.SOUND_EFFECT, 100, true);
		lazerClose = new Sound(lazerCloseR, SoundType.SOUND_EFFECT, 15, true);
		lazerImpact = new Sound(lazerImpactR, SoundType.SOUND_EFFECT, 10, true);
		crashSound = new Sound(crashSoundR, SoundType.SOUND_EFFECT, 80, false);
		hullDamageAlarm = new Sound(hullDamageAlarmR, SoundType.SOUND_EFFECT, 35, true);
		initiateBoost = new Sound(initiateBoostR, SoundType.SOUND_EFFECT, 60, false);
		backgroundRumble = new Sound(slowRocketR, SoundType.SOUND_EFFECT, 15, true);
		explosionSound = new Sound(explosionSoundR, SoundType.SOUND_EFFECT, 400, false);

		slowRocket.initialize(audioMgr);
		boostRocket.initialize(audioMgr);
		backwardRocket.initialize(audioMgr);
		droneSound.initialize(audioMgr);
		lazerClose.initialize(audioMgr);
		lazerImpact.initialize(audioMgr);
		crashSound.initialize(audioMgr);
		hullDamageAlarm.initialize(audioMgr);
		initiateBoost.initialize(audioMgr);
		backgroundRumble.initialize(audioMgr);
		explosionSound.initialize(audioMgr);

		slowRocket.setMaxDistance(500f);
		slowRocket.setMinDistance(0.5f);
		slowRocket.setRollOff(0.01f);

		boostRocket.setMaxDistance(60f);
		boostRocket.setMinDistance(0.5f);
		boostRocket.setRollOff(0.0f);

		backwardRocket.setMaxDistance(500f);
		backwardRocket.setMinDistance(0.5f);
		backwardRocket.setRollOff(0.01f);

		lazerClose.setMaxDistance(60f);
		lazerClose.setMinDistance(0.5f);
		lazerClose.setRollOff(1.0f);

		lazerImpact.setMaxDistance(2000f);
		lazerImpact.setMinDistance(0.5f);
		lazerImpact.setRollOff(0.01f);

		crashSound.setMaxDistance(20f);
		crashSound.setMinDistance(0.5f);
		crashSound.setRollOff(1.0f);

		hullDamageAlarm.setMaxDistance(30f);
		hullDamageAlarm.setMinDistance(0.5f);
		hullDamageAlarm.setRollOff(0.0f);

		initiateBoost.setMaxDistance(30f);
		initiateBoost.setMinDistance(0.5f);
		initiateBoost.setRollOff(2.0f);

		backgroundRumble.setMaxDistance(40000f);
		backgroundRumble.setRollOff(0);

		explosionSound.setMaxDistance(6000f);
		explosionSound.setRollOff(0);

	}

	@Override
	public void buildObjects() {
		Matrix4f initialTranslation, initialScale, initialRotation;

		// ------- RANDOMLY GENERATED ASTEROID CLUSTER------------
		Random random = new Random();
		long seed = 12121212;
		random.setSeed(seed);

		AsteroidCluster = new GameObject[220];
		AsteroidOres = new AsteroidOre[70];
		float scalingFactor;
		for (int i = 0; i < AsteroidCluster.length; i++) {
			AsteroidCluster[i] = new GameObject(GameObject.root(), AsteroidS, AsteroidT);
			if (i < 70) {
				if ((i % 5) == 0) {
					AsteroidOres[i] = new AsteroidOre(GameObject.root(), RedOreS, OreT, i);
				} else if ((i % 5) == 1) {
					AsteroidOres[i] = new AsteroidOre(GameObject.root(), BlueOreS, OreT, i);
				} else if ((i % 5) == 2) {
					AsteroidOres[i] = new AsteroidOre(GameObject.root(), GoldOreS, OreT, i);
				} else if ((i % 5) == 3) {
					AsteroidOres[i] = new AsteroidOre(GameObject.root(), SilverOreS, OreT, i);
				} else {
					AsteroidOres[i] = new AsteroidOre(GameObject.root(), BronzeOreS, OreT, i);
				}
			}

			scalingFactor = 100f + (random.nextFloat() * 300f);
			initialScale = (new Matrix4f()).scaling(scalingFactor);

			initialRotation = (new Matrix4f()).rotateXYZ((float) random.nextFloat(), (float) random.nextFloat(),
					(float) random.nextFloat());

			float randX = (float) (-12000 + (random.nextFloat() * 24000));
			float randY = (float) (-7000 + (random.nextFloat() * 14000));
			float randZ = (float) (-6000 + (random.nextFloat() * 12000));
			initialTranslation = (new Matrix4f()).translation(randX, randY, randZ);

			AsteroidCluster[i].setLocalTranslation(initialTranslation);
			AsteroidCluster[i].setLocalRotation(initialRotation);
			AsteroidCluster[i].setLocalScale(initialScale);
			AsteroidCluster[i].setType(GameObject.OBJECT_TYPE.ASTEROID);
			initialScale = (new Matrix4f()).scaling(scalingFactor * 1.6f);
			if (i < 70) {
				AsteroidOres[i].setLocalScale(initialScale);
				AsteroidOres[i].setInitialScale(initialScale.get(0, 0));
				AsteroidOres[i].setLocalRotation(initialRotation);
				AsteroidOres[i].setLocalTranslation(initialTranslation);
				AsteroidOres[i].setType(GameObject.OBJECT_TYPE.ORE);
			}
			double[] physicsTransform = { 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, randX, randY, randZ, 1 };
			AsteroidCluster[i].setPhysicsObject(
					(engine.getSceneGraph()).addPhysicsSphere(0, physicsTransform, scalingFactor * 2.2f));
			AsteroidCluster[i].getPhysicsObject().setBounciness(0f);

			if (i < 70) {
				(engine.getSceneGraph()).getPhysicsEngine().getObjects().lastElement().getCollisionShape()
						.setUserPointer(AsteroidOres[i]);
			} else {
				(engine.getSceneGraph()).getPhysicsEngine().getObjects().lastElement().getCollisionShape()
						.setUserPointer(AsteroidCluster[i]);
			}
		}

		FakeAsteroids = new GameObject[150];
		for (int i = 0; i < FakeAsteroids.length / 2; i++) {
			scalingFactor = (float) 500 + (random.nextFloat() * 1000f);

			float randX = (float) -23000 - (-6000 + (random.nextFloat() * 110000));
			float randY = (float) (-7000 + (random.nextFloat() * 14000)) - randX / 3;
			float randZ = (float) (2500 - Math.pow(randX / 300, 2)) + (-4000 + (random.nextFloat() * 8000));
			if (randX < -50000) {
				FakeAsteroids[i] = new GameObject(GameObject.root(), FakeAsteroid);
			} else {
				FakeAsteroids[i] = new GameObject(GameObject.root(), FakeAsteroid, AsteroidT);
			}

			initialTranslation = (new Matrix4f()).translation(randX, randY, randZ);
			initialScale = (new Matrix4f()).scaling(scalingFactor);
			initialRotation = (new Matrix4f()).rotate((float) Math.toRadians((random.nextFloat() * 360)),
					(float) random.nextFloat(), (float) random.nextFloat(), (float) random.nextFloat());
			FakeAsteroids[i].setLocalScale(initialScale);
			FakeAsteroids[i].setLocalRotation(initialRotation);
			FakeAsteroids[i].setLocalTranslation(initialTranslation);
		}

		for (int i = FakeAsteroids.length / 2; i < FakeAsteroids.length; i++) {
			scalingFactor = 500 + (float) (random.nextFloat() * 1000f);

			float randX = (float) 23000 + (-6000 + (random.nextFloat() * 110000));
			float randY = (float) (-7000 + (random.nextFloat() * 14000)) - randX / 3;
			float randZ = (float) (2500 - Math.pow(randX / 300, 2)) + (-4000 + (random.nextFloat() * 8000));
			if (randX > 50000) {
				FakeAsteroids[i] = new GameObject(GameObject.root(), FakeAsteroid);
			} else {
				FakeAsteroids[i] = new GameObject(GameObject.root(), FakeAsteroid, AsteroidT);
			}

			initialTranslation = (new Matrix4f()).translation(randX, randY, randZ);
			initialScale = (new Matrix4f()).scaling(scalingFactor);
			initialRotation = (new Matrix4f()).rotate((float) Math.toRadians((random.nextFloat() * 360)),
					(float) random.nextFloat(), (float) random.nextFloat(), (float) random.nextFloat());
			FakeAsteroids[i].setLocalScale(initialScale);
			FakeAsteroids[i].setLocalRotation(initialRotation);
			FakeAsteroids[i].setLocalTranslation(initialTranslation);
		}

		// -------Building Players Ship----------
		Ship = new GameObject(GameObject.root(), ShipS, ShipT);
		initialTranslation = (new Matrix4f()).translation(0, 0, -9000f);
		initialScale = (new Matrix4f()).scaling(40.0f);
		Ship.setLocalTranslation(initialTranslation);
		Ship.setLocalScale(initialScale);
		double[] ShipPhysicsTransform;
		if (playerOnHomeTeam) {
			ShipPhysicsTransform = new double[] { 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, -14000, 1 };
		} else {
			ShipPhysicsTransform = new double[] { -1, 0, 0, 0, 0, -1, 0, 0, 0, 0, -1, 0, 0, 0, 14000, 1 };
		}
		float[] ShipPhysicsDimensions = { 400, 120, 560 };
		Ship.setPhysicsObject((engine.getSceneGraph()).addPhysicsBox(100, ShipPhysicsTransform, ShipPhysicsDimensions));

		Ship.getPhysicsObject().setBounciness(0f);
		Ship.getPhysicsObject().setSleepThresholds(-1, -1);
		(engine.getSceneGraph()).getPhysicsEngine().getObjects().lastElement().getCollisionShape()
				.setUserPointer(Ship);
		Ship.setType(GameObject.OBJECT_TYPE.PLAYER);

		// --------Building Force Field------------
		forceFieldLayer1 = new GameObject(GameObject.root(), forceFieldS, forceFieldT);
		forceFieldLayer1.setLocalScale((new Matrix4f()).scaling(22400f));
		double[] ffcpPhysicsTransform = { 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, -20000, 1 };
		forceFieldContactPoint = new GameObject(GameObject.root(), forceFieldContactPointS);
		forceFieldContactPoint.setLocalScale((new Matrix4f()).scaling(200f));
		forceFieldContactPoint
				.setPhysicsObject((engine.getSceneGraph()).addPhysicsSphere(0, ffcpPhysicsTransform, 600f));
		forceFieldContactPoint.getRenderStates().disableRendering();
		forceFieldContactPoint.getPhysicsObject().setBounciness(100f);

		// ----------BUILDING EACH TEAM SPACE STATION----------
		SpaceStation1 = new GameObject(GameObject.root(), SpaceStationS, SpaceStationT);
		initialTranslation = (new Matrix4f()).translation(0, 0, -15000f);
		initialScale = (new Matrix4f()).scaling(25f);
		SpaceStation1.setLocalTranslation(initialTranslation);
		SpaceStation1.setLocalScale(initialScale);
		double[] SpaceStation1LeftBeamTransform = { 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, -1780, 0, -13750, 1 };
		float[] SpaceStation1LeftBeamDimensions = { 2500, 350, 250 };
		double[] SpaceStation1RightBeamTransform = { 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 1780, 0, -13750, 1 };
		float[] SpaceStation1RightBeamDimensions = { 2500, 350, 250 };
		double[] SpaceStation1UpBeamTransform = { 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 1500, -13750, 1 };
		float[] SpaceStation1UpBeamDimensions = { 350, 2500, 250 };
		double[] SpaceStation1DownBeamTransform = { 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, -1500, -13750, 1 };
		float[] SpaceStation1DownBeamDimensions = { 350, 2500, 250 };
		(engine.getSceneGraph()).addPhysicsBox(0, SpaceStation1LeftBeamTransform, SpaceStation1LeftBeamDimensions);
		(engine.getSceneGraph()).addPhysicsBox(0, SpaceStation1RightBeamTransform, SpaceStation1RightBeamDimensions);
		(engine.getSceneGraph()).addPhysicsBox(0, SpaceStation1UpBeamTransform, SpaceStation1UpBeamDimensions);
		(engine.getSceneGraph()).addPhysicsBox(0, SpaceStation1DownBeamTransform, SpaceStation1DownBeamDimensions);
		SpaceStation2 = new GameObject(GameObject.root(), SpaceStationS, SpaceStationT);
		initialTranslation = (new Matrix4f()).translation(0, 0, 15000f);
		initialScale = (new Matrix4f()).scaling(25f);
		initialRotation = (new Matrix4f()).rotate((float) Math.toRadians(180), 1, 0, 0);
		SpaceStation2.setLocalTranslation(initialTranslation);
		SpaceStation2.setLocalScale(initialScale);
		SpaceStation2.setLocalRotation(initialRotation);
		double[] SpaceStation2LeftBeamTransform = { 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, -1780, 0, 13750, 1 };
		float[] SpaceStation2LeftBeamDimensions = { 2500, 350, 250 };
		double[] SpaceStation2RightBeamTransform = { 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 1780, 0, 13750, 1 };
		float[] SpaceStation2RightBeamDimensions = { 2500, 350, 250 };
		double[] SpaceStation2UpBeamTransform = { 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 1500, 13750, 1 };
		float[] SpaceStation2UpBeamDimensions = { 350, 2500, 250 };
		double[] SpaceStation2DownBeamTransform = { 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, -1500, 13750, 1 };
		float[] SpaceStation2DownBeamDimensions = { 350, 2500, 250 };
		(engine.getSceneGraph()).addPhysicsBox(0, SpaceStation1LeftBeamTransform, SpaceStation1LeftBeamDimensions);
		(engine.getSceneGraph()).addPhysicsBox(0, SpaceStation1RightBeamTransform, SpaceStation1RightBeamDimensions);
		(engine.getSceneGraph()).addPhysicsBox(0, SpaceStation1UpBeamTransform, SpaceStation1UpBeamDimensions);
		(engine.getSceneGraph()).addPhysicsBox(0, SpaceStation1DownBeamTransform, SpaceStation1DownBeamDimensions);

		terrainObject = new GameObject(SpaceStation1, terrS);
		initialTranslation = (new Matrix4f()).translation(0, -150f, 1200);
		initialScale = (new Matrix4f()).scaling(20f, 1f, 15f);
		terrainObject.setLocalTranslation(initialTranslation);
		terrainObject.setLocalScale(initialScale);
		terrainObject.setHeightMap(terrain);
		terrainObject.getRenderStates().setTiling(1);
		terrainObject.getRenderStates().setTileFactor(10);

		// A tiny sphere I made as a child of the players ship in order to easily lock
		// the camera to a specifed position
		camAnchor = new GameObject(Ship, camAnchorS);
		initialTranslation = (new Matrix4f()).translation(0, 2f, 0);
		initialScale = (new Matrix4f()).scaling(0.1f);
		camAnchor.setLocalTranslation(initialTranslation);
		camAnchor.setLocalScale(initialScale);
		camAnchor.applyParentRotationToPosition(true);
		camAnchor.applyParentScaleToPosition(true);

		// CrossHair
		crossHair = new GameObject(camAnchor, crossHairS);
		initialTranslation = (new Matrix4f()).translation(0, -.1f, 16f);
		initialRotation = (new Matrix4f()).rotateY((float) Math.toRadians(90));
		initialScale = (new Matrix4f()).scaling(0.06f);
		crossHair.setLocalScale(initialScale);
		crossHair.setLocalRotation(initialRotation);
		crossHair.setLocalTranslation(initialTranslation);
		crossHair.applyParentRotationToPosition(true);
		crossHair.applyParentScaleToPosition(paused);

		// CockPit and HUD
		CockPit = new GameObject(camAnchor, CockPitS, CockPitT);
		initialTranslation = (new Matrix4f()).translation(0, -12f, 25f);
		initialScale = (new Matrix4f()).scaling(15f);
		initialRotation = (new Matrix4f()).rotateY((float) Math.toRadians(180));
		CockPit.setLocalScale(initialScale);
		CockPit.setLocalRotation(initialRotation);
		CockPit.setLocalTranslation(initialTranslation);
		CockPit.applyParentRotationToPosition(true);
		CockPit.applyParentScaleToPosition(true);
		CockPit.getRenderStates().setRenderHiddenFaces(true);

		// Robo Arm
		roboArm = new GameObject(camAnchor, roboArmS, OreT);
		initialTranslation = (new Matrix4f()).translation(8f, -15f, 5f);
		initialScale = (new Matrix4f()).scaling(0.5f);
		initialRotation = (new Matrix4f()).rotateY((float) Math.toRadians(95));
		initialRotation.rotateX((float) Math.toRadians(90));
		roboArm.setLocalScale(initialScale);
		roboArm.setLocalRotation(initialRotation);
		roboArm.setLocalTranslation(initialTranslation);
		roboArm.applyParentRotationToPosition(true);
		roboArm.applyParentScaleToPosition(true);

		// ----------PREBUILDING THE DRONE NPCS----------
		droneNPCs = new Drone[200];
		for (int i = 0; i < droneNPCs.length; i++) {
			droneNPCs[i] = new Drone(i, droneS, droneT, droneProjectileS, droneProjectileT, protClient, engine);

			initialTranslation = (new Matrix4f()).translation(400 * i, -20000, 0);
			initialScale = (new Matrix4f()).scaling(450f);
			droneNPCs[i].setLocalScale(initialScale);
			droneNPCs[i].setLocalTranslation(initialTranslation);
			double[] dronePhysicsTransform = { 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 400 * i, -20000,
					0 };
			droneNPCs[i]
					.setPhysicsObject((engine.getSceneGraph()).addPhysicsSphere(10, dronePhysicsTransform, 150f));

			droneNPCs[i].getPhysicsObject().setBounciness(1f);
			droneNPCs[i].getPhysicsObject().setSleepThresholds(-1, -1);
			(engine.getSceneGraph()).getPhysicsEngine().getObjects().lastElement().getCollisionShape()
					.setUserPointer(droneNPCs[i]);
			droneNPCs[i].getRenderStates().disableRendering();
		}

		// ----------INITIALIZING THE PLAYER CONTROLLER----------
		shipControls.buildShip(Ship, camAnchor);

	}

	@Override
	public void initializeLights() {
		Light.setGlobalAmbient(0.4f, 0.4f, 0.4f);
		light1 = new Light();
		light1.setLocation(new Vector3f(15000f, 8000f, -15000f));
		light1.setAmbient(.3f, .4f, .4f);
		light2 = new Light();
		light2.setLocation(new Vector3f(0f, 0f, -15000f));
		light2.setAmbient(0, 0, 0);
		light2.setDiffuse(0, 0, 0);
		light2.setSpecular(0, 0, 0);
		light2.setRange(1f);
		(engine.getSceneGraph()).addLight(light1);
		(engine.getSceneGraph()).addLight(light2);

		headLight = new Light();
		headLight.setLocation(camAnchor.getWorldLocation());
		headLight.setDirection(Ship.getWorldForwardVector());
		headLight.setAmbient(.8f, 0, 0);
		headLight.setDiffuse(0.8f, 0, 0);
		headLight.setCutoffAngle((float) Math.toRadians(15));
		(engine.getSceneGraph()).addLight(headLight);
	}

	public Light getLight1() {
		return this.light1;
	}

	public Light getLight2() {
		return this.light2;
	}

	@Override
	public void createViewports() {
		(engine.getRenderSystem()).addViewport("MAIN", 0, 0, 1f, 1f);
		shipView = (engine.getRenderSystem()).getViewport("MAIN").getCamera();
	}

	@Override
	public void initializeGame() {
		teamScore = 0;
		playerScore = 0;
		im = engine.getInputManager();

		// Ships Camera
		String gpName = im.getFirstGamepadName();
		String mouseName = im.getMouseName();
		cam = (engine.getRenderSystem().getViewport("MAIN").getCamera());
		fpv = new CameraFirstPersonController(cam, Ship, gpName, mouseName, engine, protClient);
		shipControls.setUp(gpName, mouseName, protClient, fpv);

		slowRocket.setLocation(Ship.getWorldLocation());
		boostRocket.setLocation(Ship.getWorldLocation());
		backwardRocket.setLocation(Ship.getWorldLocation());
		lazerClose.setLocation(Ship.getWorldLocation());
		for (int i = 0; i < droneNPCs.length; i++) {
			droneNPCs[i].setUpSound();
		}
		setEarParameters();

		// Rotation Controller allowing for all of the asteroids to spin.
		rc = new RotationController(engine, new Vector3f(1, 1, 1), 0.00005f);
		(engine.getSceneGraph()).addNodeController(rc);
		for (int i = 0; i < AsteroidCluster.length; i++) {
			rc.addTarget(AsteroidCluster[i]);
			if (i < 70) {
				rc.addTarget(AsteroidOres[i]);
			}

		}
		// rc.addTarget(forceFieldLayer1);
		// rc.addTarget(forceFieldLayer2);
		rc.toggle();

		forceFieldSpin = new RotationController(engine, new Vector3f(1, 1, 1), 0.000005f);
		(engine.getSceneGraph()).addNodeController(forceFieldSpin);
		forceFieldSpin.addTarget(forceFieldLayer1);
		forceFieldSpin.toggle();

		// LeftBeamController = new RotationController(engine,
		// new Vector3f(-0.0174f, 0, 0.9998f), .5f);
		// (engine.getSceneGraph()).addNodeController(LeftBeamController);
		// LeftBeamController.addTarget(beamL);
		// LeftBeamController.toggle();

		endFrameTime = System.currentTimeMillis();
		startFrameTime = System.currentTimeMillis();
		(engine.getRenderSystem()).setWindowDimensions(1900, 1000);
		setupNetworking();
	}

	public void setEarParameters() {
		Camera camera = (engine.getRenderSystem()).getViewport("MAIN").getCamera();
		audioMgr.getEar().setLocation(camAnchor.getWorldLocation());
		audioMgr.getEar().setOrientation(camera.getN(), camera.getV());
	}

	public Sound getSlowRocket() {
		return this.slowRocket;
	}

	public Sound getBoostRocket() {
		return this.boostRocket;
	}

	public Sound getBackwardRocket() {
		return this.backwardRocket;
	}

	public Sound getLazerClose() {
		return this.lazerClose;
	}

	public Sound getLazerImpact() {
		return this.lazerImpact;
	}

	public Sound getCrashSound() {
		return this.crashSound;
	}

	public Sound getHullDamageAlarm() {
		return this.hullDamageAlarm;
	}

	public Sound getInitiateBoostSound() {
		return this.initiateBoost;
	}

	public Sound getExplosionSound() {
		return this.explosionSound;
	}

	@Override
	public void update() {
		startFrameTime = endFrameTime;
		im.update((float) elapsTime);
		(engine.getSceneGraph().getPhysicsEngine()).update((float) elapsTime);

		fpv.updateCameraPosition();
		fpv.SlowDown();
		shipControls.updateLocation();
		shipControls.SlowDown();
		shipView.setLocation(camAnchor.getWorldLocation());
		slowRocket.setLocation(Ship.getWorldLocation());
		boostRocket.setLocation(Ship.getWorldLocation());
		backwardRocket.setLocation(Ship.getWorldLocation());
		lazerClose.setLocation(Ship.getWorldLocation());
		if (!backgroundRumble.getIsPlaying()) {
			backgroundRumble.play();
		}
		setEarParameters();
		headLight.setLocation(camAnchor.getWorldLocation());
		headLight.setDirection(Ship.getWorldForwardVector());

		moveForceFieldContactPoint();

		for (int i = 0; i < myDrones.size(); i++) {
			droneNPCs[myDrones.get(i)].move();
		}
		checkForCollisions();
		roboArmS.updateAnimation();

		String dispStr1 = Team2Score + " / 10000";
		String dispStr2 = Team1Score + "  vs  " + Team2Score;
		String dead = new String("----Press X to respawn----");
		Vector3f hud1Color = new Vector3f(1, 0, 0);
		Vector3f hud2Color = new Vector3f(0, 0, 1);
		Vector3f hud3Color = new Vector3f(1f, 1f, 1f);
		(engine.getHUDmanager()).setHUD2(dispStr2, hud2Color, 900, 950);

		if (!(shipControls.isAlive())) {
			(engine.getHUDmanager()).setHUD1(dead, hud3Color, 740, 540);
		} else {
			(engine.getHUDmanager()).setHUD1("", hud3Color, 750, 540);
		}

		endFrameTime = System.currentTimeMillis();
		elapsTime = endFrameTime - startFrameTime;
		processNetworking((float) elapsTime);
		checkScore();
	}

	private void checkForCollisions() {
		com.bulletphysics.dynamics.DynamicsWorld dynamicsWorld;
		com.bulletphysics.collision.broadphase.Dispatcher dispatcher;
		com.bulletphysics.collision.narrowphase.PersistentManifold manifold;
		com.bulletphysics.dynamics.RigidBody object1, object2;
		com.bulletphysics.collision.narrowphase.ManifoldPoint contactPoint;
		dynamicsWorld = ((JBulletPhysicsEngine) (engine.getSceneGraph()).getPhysicsEngine()).getDynamicsWorld();
		dispatcher = dynamicsWorld.getDispatcher();
		int manifoldCount = dispatcher.getNumManifolds();
		for (int i = 0; i < manifoldCount; i++) {
			manifold = dispatcher.getManifoldByIndexInternal(i);

			if (manifold != null) {
				object1 = (com.bulletphysics.dynamics.RigidBody) manifold.getBody0();
				object2 = (com.bulletphysics.dynamics.RigidBody) manifold.getBody1();
				JBulletPhysicsObject obj1 = JBulletPhysicsObject.getJBulletPhysicsObject(object1);
				JBulletPhysicsObject obj2 = JBulletPhysicsObject.getJBulletPhysicsObject(object2);
				for (int j = 0; j < manifold.getNumContacts(); j++) {
					contactPoint = manifold.getContactPoint(j);
					if (contactPoint.getDistance() < 0.0f && obj1.getCollisionShape().getUserPointer() != null
							&& obj2.getCollisionShape().getUserPointer() != null) {
						if (((GameObject) obj1.getCollisionShape().getUserPointer())
								.getType() == GameObject.OBJECT_TYPE.PLAYER &&
								((GameObject) obj2.getCollisionShape().getUserPointer())
										.getType() == GameObject.OBJECT_TYPE.BULLET) {
							shipControls.hitShip(5f);
							(engine.getSceneGraph()).removePhysicsObject(
									((GameObject) obj2.getCollisionShape().getUserPointer()).getPhysicsObject());
							(engine.getSceneGraph())
									.removeGameObject(((GameObject) obj2.getCollisionShape().getUserPointer()));
							return;
						} else if (((GameObject) obj1.getCollisionShape().getUserPointer())
								.getType() == GameObject.OBJECT_TYPE.BULLET &&
								((GameObject) obj2.getCollisionShape().getUserPointer())
										.getType() == GameObject.OBJECT_TYPE.PLAYER) {
							shipControls.hitShip(5f);
							(engine.getSceneGraph()).removePhysicsObject(
									((GameObject) obj1.getCollisionShape().getUserPointer()).getPhysicsObject());
							(engine.getSceneGraph())
									.removeGameObject(((GameObject) obj1.getCollisionShape().getUserPointer()));
							return;
						} else if (((GameObject) obj1.getCollisionShape().getUserPointer())
								.getType() == GameObject.OBJECT_TYPE.MY_MISSLE) {
							GameObject.OBJECT_TYPE type = ((GameObject) obj2.getCollisionShape().getUserPointer())
									.getType();
							if (type != GameObject.OBJECT_TYPE.PLAYER && type != null) {
								shipControls.detonateMissle();
							}
						} else if (((GameObject) obj2.getCollisionShape().getUserPointer())
								.getType() == GameObject.OBJECT_TYPE.MY_MISSLE) {
							GameObject.OBJECT_TYPE type = ((GameObject) obj1.getCollisionShape().getUserPointer())
									.getType();
							if (type != GameObject.OBJECT_TYPE.PLAYER && type != null) {
								shipControls.detonateMissle();
							}
						}

					}
					System.out.println("Objects Collide");
				}

			}
		}
	}

	// Current Keyboard Debugg Section
	@Override
	public void keyPressed(KeyEvent e) {

		Matrix4f rotMat = new Matrix4f();
		switch (e.getKeyCode()) {
			case KeyEvent.VK_C:
				rc.toggle();
				break;
			case KeyEvent.VK_B:
				LeftBeamController.toggle();
				break;
			case KeyEvent.VK_F:
				roboArmS.stopAnimation();
				roboArmS.playAnimation("FINGERGUN", 0.5f, AnimatedShape.EndType.STOP, 0);
				break;
			case KeyEvent.VK_1:
				engine.enablePhysicsWorldRender();
				break;
			case KeyEvent.VK_2:
				engine.disablePhysicsWorldRender();
				break;

		}
		super.keyPressed(e);
	}

	public AnimatedShape getRoboArm() {
		return this.roboArmS;
	}

	public GameObject getSpaceStationTeam1() {
		return this.SpaceStation1;
	}

	public GameObject getSpaceStationTeam2() {
		return this.SpaceStation2;
	}

	public void AddScoreTeam1(int deposit) {
		this.Team1Score += deposit;
		protClient.sendScoreMessage(deposit, true);
		int numDroneWaves = 0;
		while (deposit > 400) {
			double[] droneSpawnPoint = { 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, -3000, 13750 + (numDroneWaves * 300),
					1 };
			droneNPCs[protClient.getDroneId()].spawn(droneSpawnPoint, protClient.getDroneId(), Ship);
			myDrones.add(protClient.getDroneId());
			protClient.sendCreateDroneMessage();
			droneSpawnPoint[13] = 3000;
			droneNPCs[protClient.getDroneId()].spawn(droneSpawnPoint, protClient.getDroneId(), Ship);
			myDrones.add(protClient.getDroneId());
			protClient.sendCreateDroneMessage();
			droneSpawnPoint[13] = 0;
			droneSpawnPoint[12] = -3000;
			droneNPCs[protClient.getDroneId()].spawn(droneSpawnPoint, protClient.getDroneId(), Ship);
			myDrones.add(protClient.getDroneId());
			protClient.sendCreateDroneMessage();
			droneSpawnPoint[12] = 3000;
			droneNPCs[protClient.getDroneId()].spawn(droneSpawnPoint, protClient.getDroneId(), Ship);
			myDrones.add(protClient.getDroneId());
			protClient.sendCreateDroneMessage();
			deposit -= 400;
			numDroneWaves++;
		}
	}

	public void AddScoreTeam2(int deposit) {
		this.Team2Score += deposit;
		protClient.sendScoreMessage(deposit, false);
		int numDroneWaves = 0;
		while (deposit > 400) {
			double[] droneSpawnPoint = { 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, -3000, -13750 - (numDroneWaves * 300),
					1 };
			droneNPCs[protClient.getDroneId()].spawn(droneSpawnPoint, protClient.getDroneId(), Ship);
			myDrones.add(protClient.getDroneId());
			protClient.sendCreateDroneMessage();
			droneSpawnPoint[13] = 3000;
			droneNPCs[protClient.getDroneId()].spawn(droneSpawnPoint, protClient.getDroneId(), Ship);
			myDrones.add(protClient.getDroneId());
			protClient.sendCreateDroneMessage();
			droneSpawnPoint[13] = 0;
			droneSpawnPoint[12] = -3000;
			droneNPCs[protClient.getDroneId()].spawn(droneSpawnPoint, protClient.getDroneId(), Ship);
			myDrones.add(protClient.getDroneId());
			protClient.sendCreateDroneMessage();
			droneSpawnPoint[12] = 3000;
			droneNPCs[protClient.getDroneId()].spawn(droneSpawnPoint, protClient.getDroneId(), Ship);
			myDrones.add(protClient.getDroneId());
			protClient.sendCreateDroneMessage();
			deposit -= 400;
			numDroneWaves++;
		}
	}

	private void moveForceFieldContactPoint() {
		Vector3f shipLoc = Ship.getWorldLocation();
		float magnitude;
		float x = (shipLoc.x);
		float y = (shipLoc.y);
		float z = (shipLoc.z);
		magnitude = (float) (Math.pow(x, 2) + Math.pow(y, 2));
		magnitude = (float) (Math.sqrt(magnitude));
		magnitude = (float) (Math.pow(magnitude, 2) + Math.pow(z, 2));
		magnitude = (float) (Math.sqrt(magnitude));

		float factor = 20000f / magnitude;
		x *= factor;
		y *= factor;
		z *= factor;
		Vector3f ffcpLoc = new Vector3f(x, y, z);
		forceFieldContactPoint.setLocalLocation(ffcpLoc);

		double[] ffcpPhysicsTransform = { 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, x, y, z, 1 };
		forceFieldContactPoint.getPhysicsObject().setTransform(ffcpPhysicsTransform);
		((JBulletPhysicsObject) forceFieldContactPoint.getPhysicsObject()).getRigidBody().activate(true);
	}

	public void setTeam(boolean team) {
		playerOnHomeTeam = team;
	}

	public void checkScore() {
		if (playerOnHomeTeam) {
			if (Team1Score > 10000) {
				Vector3f hudColor = new Vector3f(0, 1f, 0.5f);
				(engine.getHUDmanager()).setHUD1("MISSION SUCCESS", hudColor, 750, 540);
			} else if (Team2Score > 10000) {
				Vector3f hudColor = new Vector3f(1f, 0, 0f);
				(engine.getHUDmanager()).setHUD1("MISSION FAILED", hudColor, 750, 540);
			}
		} else {
			if (Team1Score > 10000) {
				Vector3f hudColor = new Vector3f(1f, 0, 0f);
				(engine.getHUDmanager()).setHUD1("MISSION FAILED", hudColor, 750, 540);
			} else if (Team2Score > 10000) {
				Vector3f hudColor = new Vector3f(0, 1f, 0.5f);
				(engine.getHUDmanager()).setHUD1("MISSION SUCCESS", hudColor, 750, 540);
			}
		}
	}

	// ------NETWORKING SECTION------

	public ObjShape getGhostShape() {
		return ghostS;
	}

	public ObjShape getGhostBeams() {
		return this.beamS;
	}

	public TextureImage getGhostTexture() {
		return ghostT;
	}

	public GhostManager getGhostManager() {
		return gm;
	}

	public Drone[] getDrones() {
		return this.droneNPCs;
	}

	public ObjShape getGhostDroneShape() {
		return this.droneS;
	}

	public TextureImage getGhostDroneTexture() {
		return this.droneT;
	}

	public Engine getEngine() {
		return engine;
	}

	public ShipController getShip() {
		return this.shipControls;
	}

	public AsteroidOre[] getAsteroids() {
		return this.AsteroidOres;
	}

	public boolean getTeam() {
		return this.playerOnHomeTeam;
	}

	public void updateScore(int points, boolean team) {
		if (team) {
			this.Team1Score += points;
		} else {
			this.Team2Score += points;
		}
	}

	public void spawnGhostExplosion(Vector3f location) {
		GameObject explosion = new GameObject(GameObject.root(), ExplosionS, explosionT);
		explosion.getRenderStates().setRenderHiddenFaces(true);
		explosion.setLocalTranslation((new Matrix4f()).translation(location.x, location.y, location.z));
		ExplosionController explosionEffect = new ExplosionController(engine, this, System.currentTimeMillis());
		explosionEffect.addTarget(explosion);
		explosionEffect.toggle();
		(engine.getSceneGraph()).addNodeController(explosionEffect);
		getExplosionSound().setLocation(explosion.getLocalLocation());
		getExplosionSound().play();

	}

	private void setupNetworking() {
		isClientConnected = false;
		try {
			protClient = new ProtocolClient(InetAddress.getByName(serverAddress), serverPort, serverProtocol, this);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (protClient == null) {
			System.out.println("missing protocol host");
		} else {
			shipControls.setProtClient(protClient);
			fpv.setProtClient(protClient);
			for (int i = 0; i < droneNPCs.length; i++) {
				droneNPCs[i].setProtClient(protClient);
			}
			System.out.println("sending join message to protocol host");
			protClient.sendJoinMessage();
		}
	}

	public Vector3f getPlayerPosition() {
		return Ship.getWorldLocation();
	}

	protected void processNetworking(float elapsTime) {
		if (protClient != null) {
			protClient.processPackets();
		}
	}

	public void setIsConnected(boolean value) {
		this.isClientConnected = value;
	}

	private class SendCloseConnectionPacketAction extends AbstractInputAction {
		@Override
		public void performAction(float time, net.java.games.input.Event event) {
			if (protClient != null && isClientConnected == true) {
				protClient.sendByeMessage();
			}
		}
	}

}
