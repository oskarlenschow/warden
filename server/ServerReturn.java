/*
 F�r varje spelare som ansluter skapas en ny instans utav denna klassen.
 I run(), som k�rs hela tiden kollar servern om denna klienten som den h�r tr�den h�r ihop med har skickat
 ett meddelande till server och hanterar det. Alla meddelanden som b�rjar med '#' ska direkt skickas vidare
 till alla andra klienter d� de ocks� ska ta del utav denna informationen. 


 */
package server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class ServerReturn implements Runnable {

	public Socket SOCK;
	private Scanner INPUT;
	private PrintWriter OUT;

	String MESSAGE = "";
	int playerNumber = 0;
	int delay = 0;

	Team team;

	public ServerReturn(Socket X, int playersOnline) {

		this.playerNumber = playersOnline; // globlat spelarnummer
		this.SOCK = X;

		try {
			INPUT = new Scanner(SOCK.getInputStream());
			OUT = new PrintWriter(SOCK.getOutputStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	// N�r klienten f�rlorar anslutning k�rs detta
	public void clientDisconnect() {
		Server.disconnectedPlayers.add(playerNumber); // l�gger till denna spelaren i listan �ver spelare som f�rlorat sin anslutning

		// hittar vilket index denna spelaren har i listorna och tar bort spelaren
		int index = Server.SRS.indexOf(this);

		Server.SRS.remove(index);
		
		Server.updateActivePlayers(); // servern uppdaterar aktiva spelare
		Server.sendPlayerList(); // skickar den uppdaterade listan �ver spelare till alla klienter
		Server.updateTextAreas(); // uppdaterar serverFrames informationsf�lt

		team.removePlayer(this);
	}

	@Override
	public void run() {
		try {
			try {

				while (true) {

					if (!INPUT.hasNext()) {
						return;
					}
					MESSAGE = INPUT.nextLine();
					// alla meddelanden som b�rjar med '#' ska direkt skickas ut till alla klienter
					if (MESSAGE.startsWith("#")) {
						// loopar igenom alla anslutningar till alla andra klienter f�r att skicka meddelande till dem
						for (int i = 0; i < Server.SRS.size(); i++) {
							// kollar s� listan har hunnits skapats
							if (Server.SRS.get(i).getSocket() != null) {
								// kollar s� att den nuvarande anslutningen inte �r till klienten som h�r ihop med denna instansen
								// utav ServerReturn f�r det �r on�digt att klienten som skickade meddelandet ska f� tillbaka det igen
								if (Server.SRS.get(i).getSocket() != SOCK) {
									Socket TEMP_SOCK = Server.SRS.get(i).getSocket();
									PrintWriter TEMP_OUT = new PrintWriter(TEMP_SOCK.getOutputStream());

									String temp = MESSAGE.substring(1); // tar bort #
									TEMP_OUT.println(temp);
									TEMP_OUT.flush();

								}
							}
						}

						if (MESSAGE.startsWith("#SNDTREECUTDOWN")) {
							ServerMap.cutTreeFromClient(MESSAGE);
						}

						if (MESSAGE.startsWith("#SENDCHANGEOBJECTIVETOWEROWNER")) {
							ServerMap.changeObjectiveOwnerFromClient(MESSAGE);
						}

						if (MESSAGE.startsWith("#SNDCLIENTREMOVEWORLDOBJECT")) {
							ServerMap.removeObjectFromClient(MESSAGE);
						}

					}

					if (MESSAGE.startsWith("SENDDROPALLITEMS")) {
						ServerMap.dropAllItemsFromPlayer(MESSAGE);
					}
					
					if (MESSAGE.startsWith("SENDSPAWNLADDER")) {
						ServerMap.spawnLadderFromClient(MESSAGE);
					}
					
					if (MESSAGE.startsWith("SENDSPAWNOPENCHEST")) {
						ServerMap.spawnOpenChestFromClient(MESSAGE);
					}

					if (OUT.checkError()) {
						// clientDisconnect();
					}
				}
			} finally {
				clientDisconnect();
				// Server.clientDisconnectedInfo(playerNumber);
				SOCK.close();

			}
		} catch (Exception X) {
			// clientDisconnect();
			System.out.println("ERROR 1");
			System.out.print(X);
			X.printStackTrace();
		}
		// TODO Auto-generated method stub

	}

	public int getPlayerNumber() {
		return this.playerNumber;
	}

	public void setTeam(Team team) {
		this.team = team;
	}

	public Team getTeam() {
		return team;
	}

	public Socket getSocket() {
		return SOCK;
	}

	public int getDelay() {
		return delay;
	}

}
