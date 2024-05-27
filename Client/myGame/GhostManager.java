package myGame;

import java.awt.Color;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Iterator;
import java.util.UUID;
import java.util.Vector;
import org.joml.*;

import tage.*;
import tage.physics.JBullet.JBulletPhysicsObject;
import tage.shapes.ImportedModel;

public class GhostManager {
	private MyGame game;
	private Vector<GhostAvatar> ghostAvatars = new Vector<GhostAvatar>();
	private boolean isHomeTeam;
	private ObjShape allyTeamShellS;
	private ObjShape enemyTeamShellS;

	public GhostManager(VariableFrameRateGame vfrg) {
		game = (MyGame) vfrg;
	}

	public void createGhostAvatar(UUID id, Vector3f position, boolean isHomeTeam) throws IOException {
		System.out.println("adding ghost with ID --> " + id);
		ObjShape s = game.getGhostShape();
		ObjShape beams = game.getGhostBeams();
		TextureImage t = game.getGhostTexture();
		GhostAvatar newAvatar = new GhostAvatar(id, s, beams, t, position, game.getEngine());
		allyTeamShellS.setMatAmb(Utils.blueOreAmbient());
		allyTeamShellS.setMatDif(Utils.blueOreDiffuse());
		allyTeamShellS.setMatSpe(Utils.blueOreSpecular());
		enemyTeamShellS.setMatAmb(Utils.crystalAmbient());
		enemyTeamShellS.setMatDif(Utils.crystalDiffuse());
		enemyTeamShellS.setMatSpe(Utils.crystalSpecular());

		Matrix4f initialScale = (new Matrix4f()).scaling(40f);
		newAvatar.setLocalScale(initialScale);
		initialScale = (new Matrix4f()).scaling(1.1f);

		Vector3f translation = new Vector3f();
		newAvatar.getLocalTranslation().getTranslation(translation);
		double[] ShipPhysicsTransform = { 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, translation.x, translation.y,
				translation.z };
		float[] ShipPhysicsDimensions = { 400, 120, 560 };

		newAvatar.setPhysicsObject(
				(game.getEngine().getSceneGraph()).addPhysicsBox(1, ShipPhysicsTransform, ShipPhysicsDimensions));
		newAvatar.getPhysicsObject().setBounciness(0f);
		(game.getEngine().getSceneGraph()).getPhysicsEngine().getObjects().lastElement().getCollisionShape()
				.setUserPointer(newAvatar);

		if (game.getTeam() == isHomeTeam) {
			GameObject teamShell = new GameObject(newAvatar, allyTeamShellS);
			teamShell.setLocalScale(initialScale);
			teamShell.applyParentRotationToPosition(true);
			teamShell.applyParentScaleToPosition(true);
			newAvatar.setType(GameObject.OBJECT_TYPE.ALLY);
		} else {
			GameObject teamShell = new GameObject(newAvatar, enemyTeamShellS);
			teamShell.setLocalScale(initialScale);
			teamShell.applyParentRotationToPosition(true);
			teamShell.applyParentScaleToPosition(true);
			newAvatar.setType(GameObject.OBJECT_TYPE.ENEMY);
		}

		ghostAvatars.add(newAvatar);

	}

	public void removeGhostAvatar(UUID id) {
		GhostAvatar ghostAvatar = findAvatar(id);
		if (ghostAvatar != null) {
			game.getEngine().getSceneGraph().removeGameObject(ghostAvatar);
			ghostAvatars.remove(ghostAvatar);
		} else {
			System.out.println("tried to remove, but unable to find ghost in list");
		}
	}

	public void setShells(ObjShape allyShell, ObjShape enemyShell) {
		this.allyTeamShellS = allyShell;
		this.enemyTeamShellS = enemyShell;
	}

	private GhostAvatar findAvatar(UUID id) {
		GhostAvatar ghostAvatar;
		Iterator<GhostAvatar> it = ghostAvatars.iterator();
		while (it.hasNext()) {
			ghostAvatar = it.next();
			if (ghostAvatar.getID().compareTo(id) == 0) {
				return ghostAvatar;
			}
		}
		return null;
	}

	public void updateGhostAvatar(UUID id, Vector3f position) {
		GhostAvatar ghostAvatar = findAvatar(id);
		if (ghostAvatar != null) {
			ghostAvatar.setPosition(position);
			((JBulletPhysicsObject) ghostAvatar.getPhysicsObject()).getRigidBody().activate(true);

			Vector3f translation = new Vector3f();
			ghostAvatar.getLocalTranslation().getTranslation(translation);

			double[] ShipPhysicsTransform = {
					ghostAvatar.getLocalRotation().get(0, 0), ghostAvatar.getLocalRotation().get(0, 1),
					ghostAvatar.getLocalRotation().get(0, 2), 0,
					ghostAvatar.getLocalRotation().get(1, 0), ghostAvatar.getLocalRotation().get(1, 1),
					ghostAvatar.getLocalRotation().get(1, 2), 0,
					ghostAvatar.getLocalRotation().get(2, 0), ghostAvatar.getLocalRotation().get(2, 1),
					ghostAvatar.getLocalRotation().get(2, 2), 0,
					translation.x, translation.y, translation.z, 1 };

			ghostAvatar.getPhysicsObject().setTransform(ShipPhysicsTransform);
		} else {
			System.out.println("tried to update ghost avatar position, but unable to find ghost in list");
		}
	}

	public void updateGhostAvatar(UUID id, Matrix4f rotation) {
		GhostAvatar ghostAvatar = findAvatar(id);
		if (ghostAvatar != null) {
			System.out.println("Trying to Rotate");

			Vector3f translation = new Vector3f();
			ghostAvatar.getLocalTranslation().getTranslation(translation);

			double[] ShipPhysicsTransform = {
					ghostAvatar.getLocalRotation().get(0, 0), ghostAvatar.getLocalRotation().get(0, 1),
					ghostAvatar.getLocalRotation().get(0, 2), 0,
					ghostAvatar.getLocalRotation().get(1, 0), ghostAvatar.getLocalRotation().get(1, 1),
					ghostAvatar.getLocalRotation().get(1, 2), 0,
					ghostAvatar.getLocalRotation().get(2, 0), ghostAvatar.getLocalRotation().get(2, 1),
					ghostAvatar.getLocalRotation().get(2, 2), 0,
					translation.x, translation.y, translation.z, 1 };

			ghostAvatar.getPhysicsObject().setTransform(ShipPhysicsTransform);

			ghostAvatar.setRotation(rotation);
		} else {
			System.out.println("tried to update ghost avatar Rotation, but unable to find ghost in list");
		}
	}

	public void updateGhostAvatar(UUID id, boolean firing) {
		GhostAvatar ghostAvatar = findAvatar(id);
		if (ghostAvatar != null) {
			ghostAvatar.setBeamActive(firing);
		}
	}

	public Vector<GhostAvatar> getGhosts() {
		return this.ghostAvatars;
	}
}
