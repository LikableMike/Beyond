package tage;

import java.awt.Color;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Iterator;
import java.util.UUID;
import java.util.Vector;
import org.joml.*;

import tage.*;
import tage.networking.client.GameConnectionClient;
import myGame.*;

public class ProtocolClient extends GameConnectionClient {
	private MyGame game;
	private GhostManager ghostManager;
	private UUID id;
	private Drone[] droneArr;
	private int droneID;

	public ProtocolClient(InetAddress remoteAddr, int remotePort, ProtocolType protocolType, MyGame game)
			throws IOException {
		super(remoteAddr, remotePort, protocolType);
		this.game = game;
		this.id = UUID.randomUUID();
		ghostManager = game.getGhostManager();
		droneArr = new Drone[200];
		droneID = 0;
	}

	public UUID getID() {
		return id;
	}

	public int getDroneId() {
		return this.droneID;
	}

	public void createGhostDrone(int id) throws IOException {
		droneArr[id] = game.getDrones()[id];
		droneArr[id].awaken();
		droneArr[id].getRenderStates().enableRendering();
		droneID = id;
	}

	public void updateGhostDrone(int id, Vector3f location, Matrix4f rotation) {
		if (droneArr[id] != null) {
			double[] dronePosition = {
					rotation.get(0, 0), rotation.get(0, 1),
					rotation.get(0, 2), 0,
					rotation.get(1, 0), rotation.get(1, 1),
					rotation.get(1, 2), 0,
					rotation.get(2, 0), rotation.get(2, 1),
					rotation.get(2, 2), 0,
					location.x, location.y, location.z, 1 };
			droneArr[id].getPhysicsObject().setTransform(dronePosition);

			droneArr[id].setLocalRotation(rotation);
			droneArr[id].updateLocation();
		}

	}

	public void killGhostDrone(int killId) {
		if (droneArr[killId] != null) {
			droneArr[killId].hitDrone(50f);
		}
	}

	@Override
	protected void processPacket(Object message) {
		String strMessage = (String) message;
		System.out.println("message received -->" + strMessage);
		if (strMessage != null) {
			String[] messageTokens = strMessage.split(",");

			// Game specific protocol to handle the message
			if (messageTokens.length > 0) {
				// Handle JOIN message
				// Format: (join,success) or (join,failure)
				if (messageTokens[0].compareTo("join") == 0) {
					if (messageTokens[1].compareTo("success") == 0) {
						System.out.println("join success confirmed");
						game.setIsConnected(true);
						sendCreateMessage(game.getPlayerPosition(), game.getTeam());
					}
					if (messageTokens[1].compareTo("failure") == 0) {
						System.out.println("join failure confirmed");
						game.setIsConnected(false);
					}
				}

				// Handle BYE message
				// Format: (bye,remoteId)
				if (messageTokens[0].compareTo("bye") == 0) { // remove ghost avatar with id = remoteId
																// Parse out the id into a UUID
					UUID ghostID = UUID.fromString(messageTokens[1]);
					ghostManager.removeGhostAvatar(ghostID);
				}

				// Handle CREATE message
				// Format: (create,remoteId,x,y,z)
				// AND
				// Handle DETAILS_FOR message
				// Format: (dsfr,remoteId,x,y,z)
				if (messageTokens[0].compareTo("create") == 0 || (messageTokens[0].compareTo("dsfr") == 0)) { // create
																												// a
																												// new
																												// ghost
																												// avatar
																												// Parse
																												// out
																												// the
																												// id
																												// into
																												// a
																												// UUID
					UUID ghostID = UUID.fromString(messageTokens[1]);

					// Parse out the position into a Vector3f
					Vector3f ghostPosition = new Vector3f(
							Float.parseFloat(messageTokens[2]),
							Float.parseFloat(messageTokens[3]),
							Float.parseFloat(messageTokens[4]));
					boolean team = Boolean.parseBoolean(messageTokens[5]);

					try {
						ghostManager.createGhostAvatar(ghostID, ghostPosition, team);
					} catch (IOException e) {
						System.out.println("error creating ghost avatar");
					}

				}

				if (messageTokens[0].compareTo("spawnDrone") == 0) {
					int ghostID = Integer.parseInt(messageTokens[1]);

					try {
						createGhostDrone(ghostID);
					} catch (IOException e) {
						System.out.println("error creating ghost avatar");
					}
				}

				// Handle WANTS_DETAILS message
				// Format: (wsds,remoteId)
				if (messageTokens[0].compareTo("wsds") == 0) {
					// Send the local client's avatar's information
					// Parse out the id into a UUID
					UUID ghostID = UUID.fromString(messageTokens[1]);
					sendDetailsForMessage(ghostID, game.getPlayerPosition(), game.getTeam());
				}

				// Handle MOVE message
				// Format: (move,remoteId,x,y,z)
				if (messageTokens[0].compareTo("move") == 0) {
					// System.out.println("Ghost Moving");
					// move a ghost avatar
					// Parse out the id into a UUID
					UUID ghostID = UUID.fromString(messageTokens[1]);

					// Parse out the position into a Vector3f
					Vector3f ghostPosition = new Vector3f(
							Float.parseFloat(messageTokens[2]),
							Float.parseFloat(messageTokens[3]),
							Float.parseFloat(messageTokens[4]));

					ghostManager.updateGhostAvatar(ghostID, ghostPosition);
				}
				if (messageTokens[0].compareTo("rotate") == 0) {

					System.out.println("Ghost Rotating");
					UUID ghostID = UUID.fromString(messageTokens[1]);
					Matrix4f ghostRotation = new Matrix4f(
							Float.parseFloat(messageTokens[2]),
							Float.parseFloat(messageTokens[3]),
							Float.parseFloat(messageTokens[4]),
							Float.parseFloat(messageTokens[5]),
							Float.parseFloat(messageTokens[6]),
							Float.parseFloat(messageTokens[7]),
							Float.parseFloat(messageTokens[8]),
							Float.parseFloat(messageTokens[9]),
							Float.parseFloat(messageTokens[10]),
							Float.parseFloat(messageTokens[11]),
							Float.parseFloat(messageTokens[12]),
							Float.parseFloat(messageTokens[13]),
							Float.parseFloat(messageTokens[14]),
							Float.parseFloat(messageTokens[15]),
							Float.parseFloat(messageTokens[16]),
							Float.parseFloat(messageTokens[17]));

					ghostManager.updateGhostAvatar(ghostID, ghostRotation);
				}

				if (messageTokens[0].compareTo("updateDrone") == 0) {
					int ghostID = Integer.parseInt(messageTokens[1]);

					Vector3f dronePosition = new Vector3f(
							Float.parseFloat(messageTokens[2]),
							Float.parseFloat(messageTokens[3]),
							Float.parseFloat(messageTokens[4]));

					Matrix4f droneRotation = new Matrix4f(
							Float.parseFloat(messageTokens[5]),
							Float.parseFloat(messageTokens[6]),
							Float.parseFloat(messageTokens[7]),
							Float.parseFloat(messageTokens[8]),
							Float.parseFloat(messageTokens[9]),
							Float.parseFloat(messageTokens[10]),
							Float.parseFloat(messageTokens[11]),
							Float.parseFloat(messageTokens[12]),
							Float.parseFloat(messageTokens[13]),
							Float.parseFloat(messageTokens[14]),
							Float.parseFloat(messageTokens[15]),
							Float.parseFloat(messageTokens[16]),
							Float.parseFloat(messageTokens[17]),
							Float.parseFloat(messageTokens[18]),
							Float.parseFloat(messageTokens[19]),
							Float.parseFloat(messageTokens[20]));

					updateGhostDrone(ghostID, dronePosition, droneRotation);
				}

				if (messageTokens[0].compareTo("killDrone") == 0) {
					int ghostID = Integer.parseInt(messageTokens[1]);
					killGhostDrone(ghostID);

				}

				if (messageTokens[0].compareTo("shoot") == 0 && messageTokens[1].compareTo(id.toString()) == 0) {
					System.out.println("Being Hit --- " + messageTokens[1]);
					float damage = Float.parseFloat(messageTokens[2]);
					game.getShip().hitShip(damage);

				}

				if (messageTokens[0].compareTo("explosion") == 0) {
					Vector3f location = new Vector3f(Float.parseFloat(messageTokens[1]),
							Float.parseFloat(messageTokens[2]), Float.parseFloat(messageTokens[3]));
					game.spawnGhostExplosion(location);
				}

				if (messageTokens[0].compareTo("firing") == 0) {
					UUID ghostID = UUID.fromString(messageTokens[1]);
					ghostManager.updateGhostAvatar(ghostID, true);
					System.out.println("EnemyFiring");
				}

				if (messageTokens[0].compareTo("notFiring") == 0) {
					UUID ghostID = UUID.fromString(messageTokens[1]);
					ghostManager.updateGhostAvatar(ghostID, false);
					System.out.println("EnemyFiring");
				}

				if (messageTokens[0].compareTo("shrinkOre") == 0) {
					int index = Integer.parseInt(messageTokens[2]);
					Matrix4f currScale = game.getAsteroids()[index].getLocalScale();
					game.getAsteroids()[index].setLocalScale(currScale.scale(new Vector3f(.999f, .999f, .999f)));
				}

				if (messageTokens[0].compareTo("updateScore") == 0) {
					int points = Integer.parseInt(messageTokens[2]);
					boolean team = Boolean.parseBoolean(messageTokens[3]);
					game.updateScore(points, team);

				}
			}
		}
	}

	// The initial message from the game client requesting to join the
	// server. localId is a unique identifier for the client. Recommend
	// a random UUID.
	// Message Format: (join,localId)

	public void sendJoinMessage() {
		try {
			sendPacket(new String("join," + id.toString()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Informs the server that the client is leaving the server.
	// Message Format: (bye,localId)

	public void sendByeMessage() {
		try {
			sendPacket(new String("bye," + id.toString()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Informs the server of the clients Avatars position. The server
	// takes this message and forwards it to all other clients registered
	// with the server.
	// Message Format: (create,localId,x,y,z) where x, y, and z represent the
	// position

	public void sendCreateMessage(Vector3f position, boolean homeTeam) {
		try {
			String message = new String("create," + id.toString());
			message += "," + position.x();
			message += "," + position.y();
			message += "," + position.z();
			message += "," + homeTeam;

			sendPacket(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sendCreateDroneMessage() {
		try {
			String message = new String("spawnDrone," + this.id.toString() + "," + (droneID + 1));
			sendPacket(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
		droneID++;
		if (droneID > 95) {
			droneID = 0;
		}
	}

	// Informs the server of the local avatar's position. The server then
	// forwards this message to the client with the ID value matching remoteId.
	// This message is generated in response to receiving a WANTS_DETAILS message
	// from the server.
	// Message Format: (dsfr,remoteId,localId,x,y,z) where x, y, and z represent the
	// position.

	public void sendDetailsForMessage(UUID remoteId, Vector3f position, boolean homeTeam) {
		try {
			String message = new String("dsfr," + remoteId.toString() + "," + id.toString());
			message += "," + position.x();
			message += "," + position.y();
			message += "," + position.z();
			message += "," + homeTeam;

			sendPacket(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Informs the server that the local avatar has changed position.
	// Message Format: (move,localId,x,y,z) where x, y, and z represent the
	// position.

	public void sendMoveMessage(Vector3f position) {
		try {
			String message = new String("move," + id.toString());
			message += "," + position.x();
			message += "," + position.y();
			message += "," + position.z();

			sendPacket(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sendMoveMessage(Vector3f position, UUID droneID) {
		try {
			String message = new String("move," + droneID.toString());
			message += "," + position.x();
			message += "," + position.y();
			message += "," + position.z();

			sendPacket(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sendRotationMessage(Matrix4f rotation) {
		try {
			String message = new String("rotate," + id.toString());
			message += "," + rotation.get(0, 0);
			message += "," + rotation.get(0, 1);
			message += "," + rotation.get(0, 2);
			message += "," + rotation.get(0, 3);
			message += "," + rotation.get(1, 0);
			message += "," + rotation.get(1, 1);
			message += "," + rotation.get(1, 2);
			message += "," + rotation.get(1, 3);
			message += "," + rotation.get(2, 0);
			message += "," + rotation.get(2, 1);
			message += "," + rotation.get(2, 2);
			message += "," + rotation.get(2, 3);
			message += "," + rotation.get(3, 0);
			message += "," + rotation.get(3, 1);
			message += "," + rotation.get(3, 2);
			message += "," + rotation.get(3, 3);

			sendPacket(message);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void sendRotationMessage(Matrix4f rotation, UUID droneID) {
		try {
			String message = new String("rotate," + droneID.toString());
			message += "," + rotation.get(0, 0);
			message += "," + rotation.get(0, 1);
			message += "," + rotation.get(0, 2);
			message += "," + rotation.get(0, 3);
			message += "," + rotation.get(1, 0);
			message += "," + rotation.get(1, 1);
			message += "," + rotation.get(1, 2);
			message += "," + rotation.get(1, 3);
			message += "," + rotation.get(2, 0);
			message += "," + rotation.get(2, 1);
			message += "," + rotation.get(2, 2);
			message += "," + rotation.get(2, 3);
			message += "," + rotation.get(3, 0);
			message += "," + rotation.get(3, 1);
			message += "," + rotation.get(3, 2);
			message += "," + rotation.get(3, 3);

			sendPacket(message);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void sendShootMessage(UUID enemyId, float damage) {
		try {
			String message = new String("shoot," + id.toString() + "," + enemyId.toString() + "," + damage);

			sendPacket(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sendExplosionMessage(Vector3f location) {
		try {
			String message = new String(
					"explosion," + id.toString() + "," + location.x + "," + location.y + "," + location.z);
			sendPacket(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sendFiringMessage(boolean firing) {
		try {
			String message;
			if (firing) {
				message = new String("firing," + id.toString());
			} else {
				message = new String("notFiring," + id.toString());
			}
			sendPacket(message);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void sendAsteroidShrinkMessage(int index) {
		try {
			String message;
			message = new String("shrinkOre," + id.toString() + "," + index);
			sendPacket(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sendScoreMessage(int points, boolean team) {
		try {
			String message;
			message = new String("updateScore," + id.toString() + "," + points + "," + team);
			sendPacket(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sendDroneUpdateMessage(int id, Vector3f location, Matrix4f rotation) {

		try {
			String message = new String("updateDrone," + this.id.toString() + "," + id);
			message += "," + location.x();
			message += "," + location.y();
			message += "," + location.z();

			message += "," + rotation.get(0, 0);
			message += "," + rotation.get(0, 1);
			message += "," + rotation.get(0, 2);
			message += "," + rotation.get(0, 3);
			message += "," + rotation.get(1, 0);
			message += "," + rotation.get(1, 1);
			message += "," + rotation.get(1, 2);
			message += "," + rotation.get(1, 3);
			message += "," + rotation.get(2, 0);
			message += "," + rotation.get(2, 1);
			message += "," + rotation.get(2, 2);
			message += "," + rotation.get(2, 3);
			message += "," + rotation.get(3, 0);
			message += "," + rotation.get(3, 1);
			message += "," + rotation.get(3, 2);
			message += "," + rotation.get(3, 3);
			sendPacket(message);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void sendKillDroneMessage(int id) {
		try {
			String message = new String("killDrone," + this.id.toString() + "," + id);
			sendPacket(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
