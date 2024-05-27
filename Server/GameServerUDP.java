import java.io.IOException;
import java.net.InetAddress;
import java.util.UUID;
import org.joml.*;

import tage.networking.server.GameConnectionServer;
import tage.networking.server.IClientInfo;

public class GameServerUDP extends GameConnectionServer<UUID> {
	public GameServerUDP(int localPort) throws IOException {
		super(localPort, ProtocolType.UDP);
	}

	@Override
	public void processPacket(Object o, InetAddress senderIP, int senderPort) {
		String message = (String) o;
		String[] messageTokens = message.split(",");

		if (messageTokens.length > 0) { // JOIN -- Case where client just joined the server
										// Received Message Format: (join,localId)
			if (messageTokens[0].compareTo("join") == 0) {
				try {
					IClientInfo ci;
					ci = getServerSocket().createClientInfo(senderIP, senderPort);
					UUID clientID = UUID.fromString(messageTokens[1]);
					addClient(ci, clientID);
					System.out.println("Join request received from - " + clientID.toString());
					sendJoinedMessage(clientID, true);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			// BYE -- Case where clients leaves the server
			// Received Message Format: (bye,localId)
			if (messageTokens[0].compareTo("bye") == 0) {
				UUID clientID = UUID.fromString(messageTokens[1]);
				System.out.println("Exit request received from - " + clientID.toString());
				sendByeMessages(clientID);
				removeClient(clientID);
			}

			// CREATE -- Case where server receives a create message (to specify avatar
			// location)
			// Received Message Format: (create,localId,x,y,z)
			if (messageTokens[0].compareTo("create") == 0) {
				UUID clientID = UUID.fromString(messageTokens[1]);
				String[] pos = { messageTokens[2], messageTokens[3], messageTokens[4] };
				boolean homeTeam = Boolean.parseBoolean(messageTokens[5]);
				sendCreateMessages(clientID, pos, homeTeam);
				System.out.println("joining team " + homeTeam);
				sendWantsDetailsMessages(clientID);
			}

			if (messageTokens[0].compareTo("spawnDrone") == 0) {
				UUID clientID = UUID.fromString(messageTokens[1]);
				int droneID = Integer.parseInt(messageTokens[2]);
				sendCreateDroneMessages(clientID, droneID);
				// sendWantsDetailsMessages(clientID);
			}

			if (messageTokens[0].compareTo("updateDrone") == 0) {
				UUID clientID = UUID.fromString(messageTokens[1]);
				int droneID = Integer.parseInt(messageTokens[2]);
				String[] posAndRot = new String[19];
				for (int i = 0; i < 19; i++) {
					posAndRot[i] = messageTokens[i + 3];
				}
				sendUpdateDroneMessage(clientID, droneID, posAndRot);
				// sendWantsDetailsMessages(clientID);
			}

			if (messageTokens[0].compareTo("killDrone") == 0) {
				UUID clientID = UUID.fromString(messageTokens[1]);
				int droneID = Integer.parseInt(messageTokens[2]);
				sendKillDroneMessage(clientID, droneID);
			}

			// DETAILS-FOR --- Case where server receives a details for message
			// Received Message Format: (dsfr,remoteId,localId,x,y,z)
			if (messageTokens[0].compareTo("dsfr") == 0) {
				UUID clientID = UUID.fromString(messageTokens[1]);
				UUID remoteID = UUID.fromString(messageTokens[2]);
				String[] pos = { messageTokens[3], messageTokens[4], messageTokens[5] };
				boolean homeTeam = Boolean.parseBoolean(messageTokens[6]);
				sendDetailsForMessage(clientID, remoteID, pos, homeTeam);
			}

			// MOVE --- Case where server receives a move message
			// Received Message Format: (move,localId,x,y,z)
			if (messageTokens[0].compareTo("move") == 0) {
				UUID clientID = UUID.fromString(messageTokens[1]);
				String[] pos = { messageTokens[2], messageTokens[3], messageTokens[4] };
				sendMoveMessages(clientID, pos);
			}

			if (messageTokens[0].compareTo("rotate") == 0) {
				UUID clientID = UUID.fromString(messageTokens[1]);
				String[] rot = { messageTokens[2], messageTokens[3], messageTokens[4], messageTokens[5],
						messageTokens[6], messageTokens[7], messageTokens[8], messageTokens[9],
						messageTokens[10], messageTokens[11], messageTokens[12], messageTokens[13],
						messageTokens[14], messageTokens[15], messageTokens[16], messageTokens[17] };
				sendRotationMessages(clientID, rot);
			}

			if (messageTokens[0].compareTo("shoot") == 0) {
				UUID clientID = UUID.fromString(messageTokens[1]);
				UUID enemyID = UUID.fromString(messageTokens[2]);
				float damage = Float.parseFloat(messageTokens[3]);
				sendShootMessage(clientID, enemyID, damage);
			}

			if (messageTokens[0].compareTo("firing") == 0) {
				UUID clientID = UUID.fromString(messageTokens[1]);
				sendFiringMessage(clientID, true);
			}

			if (messageTokens[0].compareTo("notFiring") == 0) {
				UUID clientID = UUID.fromString(messageTokens[1]);
				sendFiringMessage(clientID, false);
			}

			if (messageTokens[0].compareTo("shrinkOre") == 0) {
				UUID clientID = UUID.fromString(messageTokens[1]);
				int index = Integer.parseInt(messageTokens[2]);
				sendShrinkOreMessage(clientID, index);
			}

			if (messageTokens[0].compareTo("updateScore") == 0) {
				UUID clientID = UUID.fromString(messageTokens[1]);
				int points = Integer.parseInt(messageTokens[2]);
				boolean team = Boolean.parseBoolean(messageTokens[3]);
				sendUpdateScoreMessage(clientID, points, team);
			}

			if (messageTokens[0].compareTo("explosion") == 0) {
				UUID clientID = UUID.fromString(messageTokens[1]);
				Vector3f location = new Vector3f(Float.parseFloat(messageTokens[2]),
						Float.parseFloat(messageTokens[3]), Float.parseFloat(messageTokens[4]));
				sendExplosionMessage(clientID, location);
			}

		}
	}

	// Informs the client who just requested to join the server if their if their
	// request was able to be granted.
	// Message Format: (join,success) or (join,failure)

	public void sendJoinedMessage(UUID clientID, boolean success) {
		try {
			System.out.println("trying to confirm join");
			String message = new String("join,");
			if (success)
				message += "success";
			else
				message += "failure";
			sendPacket(message, clientID);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Informs a client that the avatar with the identifier remoteId has left the
	// server.
	// This message is meant to be sent to all client currently connected to the
	// server
	// when a client leaves the server.
	// Message Format: (bye,remoteId)

	public void sendByeMessages(UUID clientID) {
		try {
			String message = new String("bye," + clientID.toString());
			forwardPacketToAll(message, clientID);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Informs a client that a new avatar has joined the server with the unique
	// identifier
	// remoteId. This message is intended to be send to all clients currently
	// connected to
	// the server when a new client has joined the server and sent a create message
	// to the
	// server. This message also triggers WANTS_DETAILS messages to be sent to all
	// client
	// connected to the server.
	// Message Format: (create,remoteId,x,y,z) where x, y, and z represent the
	// position

	public void sendCreateMessages(UUID clientID, String[] position, boolean homeTeam) {
		try {
			String message = new String("create," + clientID.toString());
			message += "," + position[0];
			message += "," + position[1];
			message += "," + position[2];
			message += "," + homeTeam;
			forwardPacketToAll(message, clientID);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sendCreateDroneMessages(UUID clientID, int droneID) {
		try {
			String message = new String("spawnDrone," + droneID);

			forwardPacketToAll(message, clientID);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sendUpdateDroneMessage(UUID clientID, int droneID, String[] posAndRot) {
		try {
			String message = new String("updateDrone," + droneID);
			for (int i = 0; i < 19; i++) {
				message += "," + posAndRot[i];
			}
			forwardPacketToAll(message, clientID);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sendKillDroneMessage(UUID clientID, int droneID) {
		try {
			String message = new String("killDrone," + droneID);
			forwardPacketToAll(message, clientID);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Informs a client of the details for a remote client�s avatar. This message is
	// in response
	// to the server receiving a DETAILS_FOR message from a remote client. That
	// remote client�s
	// message�s localId becomes the remoteId for this message, and the remote
	// client�s message�s
	// remoteId is used to send this message to the proper client.
	// Message Format: (dsfr,remoteId,x,y,z) where x, y, and z represent the
	// position.

	public void sendDetailsForMessage(UUID clientID, UUID remoteId, String[] position, boolean homeTeam) {
		try {
			String message = new String("dsfr," + remoteId.toString());
			message += "," + position[0];
			message += "," + position[1];
			message += "," + position[2];
			message += "," + homeTeam;
			sendPacket(message, clientID);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Informs a local client that a remote client wants the local client�s avatar�s
	// information.
	// This message is meant to be sent to all clients connected to the server when
	// a new client
	// joins the server.
	// Message Format: (wsds,remoteId)

	public void sendWantsDetailsMessages(UUID clientID) {
		try {
			String message = new String("wsds," + clientID.toString());
			forwardPacketToAll(message, clientID);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Informs a client that a remote client�s avatar has changed position. x, y,
	// and z represent
	// the new position of the remote avatar. This message is meant to be forwarded
	// to all clients
	// connected to the server when it receives a MOVE message from the remote
	// client.
	// Message Format: (move,remoteId,x,y,z) where x, y, and z represent the
	// position.

	public void sendMoveMessages(UUID clientID, String[] position) {
		try {
			String message = new String("move," + clientID.toString());
			message += "," + position[0];
			message += "," + position[1];
			message += "," + position[2];
			forwardPacketToAll(message, clientID);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sendRotationMessages(UUID clientID, String[] rotation) {
		try {
			String message = new String("rotate," + clientID.toString());
			message += "," + rotation[0];
			message += "," + rotation[1];
			message += "," + rotation[2];
			message += "," + rotation[3];
			message += "," + rotation[4];
			message += "," + rotation[5];
			message += "," + rotation[6];
			message += "," + rotation[7];
			message += "," + rotation[8];
			message += "," + rotation[9];
			message += "," + rotation[10];
			message += "," + rotation[11];
			message += "," + rotation[12];
			message += "," + rotation[13];
			message += "," + rotation[14];
			message += "," + rotation[15];
			forwardPacketToAll(message, clientID);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sendShootMessage(UUID clientID, UUID enemyId, float damage) {
		try {
			String message = new String("shoot," + enemyId.toString() + "," + damage);

			forwardPacketToAll(message, clientID);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sendExplosionMessage(UUID clientID, Vector3f location) {
		try {
			String message = new String(
					"explosion," + location.x + "," + location.y + "," + location.z);
			forwardPacketToAll(message, clientID);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sendFiringMessage(UUID clientID, boolean firing) {
		try {
			if (firing) {
				String message = new String("firing," + clientID.toString());
				forwardPacketToAll(message, clientID);
			} else {
				String message = new String("notFiring," + clientID.toString());
				forwardPacketToAll(message, clientID);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sendShrinkOreMessage(UUID clientID, int index) {
		try {
			String message = new String("shrinkOre," + clientID + "," + index);
			forwardPacketToAll(message, clientID);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sendUpdateScoreMessage(UUID clientID, int index, boolean team) {
		try {
			String message = new String("updateScore," + clientID + "," + index + "," + team);
			forwardPacketToAll(message, clientID);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
