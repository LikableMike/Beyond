package myGame;

import java.util.UUID;

import tage.*;
import tage.audio.Sound;
import tage.shapes.*;
import org.joml.*;
import java.lang.Math;
import tage.audio.*;

// A ghost MUST be connected as a child of the root,
// so that it will be rendered, and for future removal.
// The ObjShape and TextureImage associated with the ghost
// must have already been created during loadShapes() and
// loadTextures(), before the game loop is started.

public class GhostAvatar extends GameObject {
	UUID uuid;
	private GameObject beamL, beamR;
	private Sound booster;
	private Engine engine;

	public GhostAvatar(UUID id, ObjShape s, ObjShape beams, TextureImage t, Vector3f p, Engine e) {
		super(GameObject.root(), s, t);
		this.getShape().setMatAmb(Utils.ShipAmbient());
		this.getShape().setMatDif(Utils.ShipDiffuse());
		this.getShape().setMatSpe(Utils.ShipSpecular());
		this.engine = e;

		beamL = new GameObject(this, beams);
		beamL.setLocalTranslation((new Matrix4f()).translation(-2f, 1.1f, 200f));
		beamL.setLocalScale((new Matrix4f()).scaling(0, 0, 0f));
		beamL.setLocalRotation((new Matrix4f()).rotateY((float) Math.toRadians(.5f)));
		beamL.applyParentRotationToPosition(true);
		beamL.applyParentScaleToPosition(true);

		beamR = new GameObject(this, beams);
		beamR.setLocalTranslation((new Matrix4f()).translation(2f, 1.1f, 200f));
		beamR.setLocalScale((new Matrix4f()).scaling(0, 0, 0f));
		beamR.setLocalRotation((new Matrix4f()).rotateY((float) Math.toRadians(-0.5f)));
		beamR.applyParentRotationToPosition(true);
		beamR.applyParentScaleToPosition(true);
		uuid = id;
		setUpSound();
		setPosition(p);

	}

	public GhostAvatar(UUID id, ObjShape s, TextureImage t, Vector3f p) {
		super(GameObject.root(), s, t);
		this.getShape().setMatAmb(Utils.ShipAmbient());
		this.getShape().setMatDif(Utils.ShipDiffuse());
		this.getShape().setMatSpe(Utils.ShipSpecular());
		uuid = id;
		setPosition(p);
	}

	public void setUpSound() {
		AudioResource boosterR;
		boosterR = engine.getAudioManager().createAudioResource("assets/sounds/BackWardRocket.wav",
				AudioResourceType.AUDIO_STREAM);
		booster = new Sound(boosterR, SoundType.SOUND_EFFECT, 100, true);
		booster.initialize(engine.getAudioManager());
		booster.setMaxDistance(8000f);
		booster.setMinDistance(0.5f);
		booster.setRollOff(0.005f);
		booster.play();
	}

	public UUID getID() {
		return uuid;
	}

	public void setPosition(Vector3f m) {
		booster.setLocation(m);
		setLocalLocation(m);
	}

	public void setRotation(Matrix4f m) {
		setLocalRotation(m);
	}

	public Vector3f getPosition() {
		return getWorldLocation();
	}

	public void setBeamActive(boolean b) {
		Matrix4f Active, Inactive;
		Inactive = (new Matrix4f()).scaling(0, 0, 0);
		Active = (new Matrix4f()).scaling(0.1f, 0.1f, 200f);
		if (b) {
			beamL.setLocalScale(Active);
			beamR.setLocalScale(Active);
			System.out.println("ENEMY FIRING");
		} else {
			beamL.setLocalScale(Inactive);
			beamR.setLocalScale(Inactive);
		}
	}
}
