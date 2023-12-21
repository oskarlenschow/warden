/*

 Denna klassen sk�ter all kommunikation mellan servern och klienten, 
 h�r tas b�de meddelanden emot och skickas. 

 N�r klienten ska skicka meddelande till servern anv�nds huvudsakligen
 metoden sendData() f�r att ha ett konsekvent 'format' p� informationen
 men det finns n�gra undantag.

 N�r ett meddelande skickas till server har det alltid ett 'prefix' som st�r
 i b�rjan av str�ngen, till exempel 'PX' (Players X). Utifr�n vilket 'prefix'
 som skickas med vet server hur den ska tolka och hantera informationen som kommer
 efter. 

 M�nga meddelanden som klienten skickar ska servern direkt skicka vidare till alla
 andra ansluta klienter s� att de kan ta del av informationen, till exempel n�r 
 man skickar sin koordinater. S�dana meddelanden som direkt ska skickas vidare till
 alla klienter b�rjar med '#' .


    

 */
package client;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

import client.handlers.MapHandler;
import client.players.OtherPlayer;
import client.players.Player;
import client.players.Team;
import client.players.YourPlayer;
import client.ui.mainMenu.MainMenu;


public class Client implements Runnable {

	Socket SOCK;
	Scanner INPUT;
	static PrintWriter OUT;

	public static int playerNumber; // denna klientens globala spelarnummer

	static long sentPing = 0;
	static long delayToServer = 0; // efterdr�jningen till servern

	// delay till andra klienter
	static int recievedMessagesFromClients = 0;
	static long totalDelayToClients = 0;
	static long averageDelayToClients = 0;

	static boolean DSIRecieved = false;

	public Client(Socket X) {
		this.SOCK = X;
	}

	// startar klienten
	@Override
	public void run() {
		try {
			try {
				INPUT = new Scanner(SOCK.getInputStream());
				OUT = new PrintWriter(SOCK.getOutputStream());
				OUT.flush();
				checkStream();

				INPUT.close(); // k�?
				OUT.close();
			} finally {
				SOCK.close();
				INPUT.close();
				OUT.close();
			}
		} catch (Exception E) {
			// System.out.println(E);
			E.printStackTrace();
		}

	}

	public void checkStream() throws IOException {
		// kollar hela tiden om det kommit nytt meddelande fr�n servern
		while (true) {
			recieve();
		}
	}

	// ansluter till servern
	public static void connect(final String host) {

		Main.state = Main.States.inLoadingScreen;

		// detta m�ste vara i en egen thread f�r annars fryser programmet under de
		// sekundrarna som man f�rs�ker ansluta
		Thread thread = new Thread("Connect") {
			public void run() {
				try {

					final int PORT = 25565;
					// String host = "217.211.254.62";
					//String host = Main.mainMenu.getComponentValueByName("IP"); // h�mtar IP fr�n textf�ltet
					// String host = "78.68.24.24";

					// String host = "10.0.0.8";'

					// String host = "localhost";
					System.out.println(host);
					Socket SOCK = new Socket(host, PORT);
					System.out.println("You connected to: " + host);

					Client chatClient = new Client(SOCK);

					Thread X = new Thread(chatClient);
					X.start();

				} catch (Exception X) {
					X.printStackTrace();
				}
			}
		};
		thread.start();
	}

	// i denna metoden kollar klienten om man har f�tt n�got meddelande f�r server, sedan tolkar klienten meddelandet beroende p� vilket "prefix" meddalndet har
	public void recieve() {
		try {
			if (INPUT.hasNext()) {
				Screen.noMessageReceivedFor = 0; // �terst�ller r�knaren som r�knar hur l�nge det har dr�jt sedan man fick det senast meddelandet fr�n servern

				String fullMessage = INPUT.nextLine(); // sparar meddelandet fr�n servern

				String message = fetchMessage(fullMessage);

				if (fullMessage.startsWith("SPL")) { // send player list
					handlePlayersListInfo(message);
					// sending players coordinates
				} else if (fullMessage.startsWith("SPC")) {
					String[] split = message.split("&");

					int playerNumber = Integer.parseInt(split[0]);
					int x = Integer.parseInt(split[1]);
					int y = Integer.parseInt(split[2]);

					Player player = Main.getPlayerByNumber(playerNumber);
					player.setX(x);
					player.setY(y);

				}
				// Assign player number
				else if (fullMessage.startsWith("APN")) {
					playerNumber = Integer.parseInt(message);

					try {
						Main.clientPlayer = new YourPlayer(playerNumber, 0);
					} catch (NoSuchMethodException e) {
						e.printStackTrace();
					} catch (SecurityException e) {
						e.printStackTrace();
					}

				} else if (fullMessage.startsWith("DSI")) { // done sending information. N�r detta skickas fr�n servern har all information klienten beh�ver f�r att starta spelet skickats
					MapHandler.finalInit();
					Main.clientPlayer.setStartPosition();
					DSIRecieved = true;
					Main.state = Main.States.inGame;

				} else if (fullMessage.startsWith("SNDWORLDOBJECTS")) { // sending map info
					MapHandler.initObjectsFromServer(message);

				} else if (fullMessage.startsWith("SENDCASTSPELL")) { // sending cast spell
					String[] info = message.split("&");
					int playerNumber = Integer.parseInt(info[0]);

					OtherPlayer OP = (OtherPlayer) (Main.getPlayerByNumber(playerNumber));
					OP.castSpellFromServer(info[1]);

				} else if (fullMessage.startsWith("ISFALLING")) { // sending is falling
					String[] info = message.split("&");
					int playerNumber = Integer.parseInt(info[0]);

					boolean falling = (info[1].equals("1")); // s�tter true eller false
					Main.getPlayerByNumber(playerNumber).setFalling(falling);

				} else if (fullMessage.startsWith("ISMOVING")) { // sending is moving
					String[] info = message.split("&");
					int playerNumber = Integer.parseInt(info[0]);
					boolean moving = (info[1].equals("1")); // s�tter true eller false

					Main.getPlayerByNumber(playerNumber).setMoving(moving);

				} else if (fullMessage.startsWith("SNDHEALTH")) { // sending health
					String[] info = message.split("&");
					int playerNumber = Integer.parseInt(info[0]);

					int health = Integer.parseInt(info[1]);

					Main.getPlayerByNumber(playerNumber).setHealth(health);

				} else if (fullMessage.startsWith("SENDENERGY")) { // sending energy
					String[] info = message.split("&");
					int playerNumber = Integer.parseInt(info[0]);

					int energy = Integer.parseInt(info[1]);

					Main.getPlayerByNumber(playerNumber).setEnergy(energy);

				} else if (fullMessage.startsWith("ISFACINGLEFT")) { // sending is facing left
					String[] info = message.split("&");
					int playerNumber = Integer.parseInt(info[0]);

					boolean facingLeft = (info[1].equals("1")); // s�tter true eller false

					Main.getPlayerByNumber(playerNumber).setFacingLeft(facingLeft);

				} else if (fullMessage.startsWith("SNDMAPINFO")) { // sending map info
					MapHandler.initMapInfo(message);
				} else if (fullMessage.startsWith("SNDADDOBJECT")) { // sending add object
					MapHandler.addObjectFromInfo(message);
				} else if (fullMessage.startsWith("SENDREMOVESPELL")) { // sending remove spell
					String[] info = message.split("&");
					// splittar informationen om spellen
					String[] secondSplit = info[1].split("@");

					int id = Integer.parseInt(secondSplit[0]);
					int casterNumber = Integer.parseInt(secondSplit[1]); // Numret p� spelaren som kastade spellen

					Player player = Main.getPlayerByNumber(casterNumber);
					player.removeSpellById(id);

				} else if (fullMessage.startsWith("SNDTREESPAWNS")) { // sending tree spawn info
					MapHandler.initTreeSpawns(message);
				} else if (fullMessage.startsWith("SNDSTATEOFTREESPAWN")) { // sending state of treespawn
					MapHandler.updateStateOfTreeSpawn(message);
				} else if (fullMessage.startsWith("SNDIMAGECHANGEDELAY")) { // sending image change delay
					String[] info = message.split("&");
					int playerNumber = Integer.parseInt(info[0]);
					int changeDelay = Integer.parseInt(info[1]);

					Main.getPlayerByNumber(playerNumber).setImageChangeDelay(changeDelay);
				} else if (fullMessage.startsWith("SENDDOUBLEJUMP")) { // sending double jump effect

					String[] info = message.split("&");
					int playerNumber = Integer.parseInt(info[0]);

					Player player = Main.getPlayerByNumber(playerNumber);
					player.createDoubleJumpEffect();
					player.playJumpSound();
				} else if (fullMessage.startsWith("SNDTREECUTDOWN")) { // sending tree cut down
					MapHandler.cutTreeFromServer(message);

				} else if (fullMessage.startsWith("SNDISDASHING")) { // sending dashing
					String[] info = message.split("&");
					if (info != null) {
						int playerNumber = Integer.parseInt(info[0]);

						boolean dashing = (info[1].equals("1")); // s�tter true eller false
						Main.getPlayerByNumber(playerNumber).setDashing(dashing);
					}

				} else if (fullMessage.startsWith("SNDCLIENTREMOVEWORLDOBJECT")) { // sending remove world object from another client
					MapHandler.removeObjectFromServer(message);
				} else if (fullMessage.startsWith("SENDINGTEAMINFO")) { // sending team info
					String[] info = message.split("@");

					int teamNumber = Integer.parseInt(info[0]);

					Team team = Main.getTeamByNumber(teamNumber);

					// om detta �r null s� har inte lagets skapats �nnu
					if (team == null) {
						team = new Team(teamNumber);
						Main.teams.add(team);
					}

					if (info.length > 1) {
						team.readInfoFromServer(info[1]); // l�ser infon om detta team
					}

				} else if (fullMessage.startsWith("SNDOBJECTIVETOWERSPAWNS")) { // sending objective tower spawns
					MapHandler.initObjectiveTowerSpawns(message);
				} else if (fullMessage.startsWith("SENDCHANGEOBJECTIVETOWEROWNER")) { // sending objective tower spawns
					MapHandler.updateOwnerOfObjectiveTower(message);

				} else if (fullMessage.startsWith("SENDTEAMSCORE")) { // sending teams score
					Main.handleTeamScoreFromServer(message);
				} else if (fullMessage.startsWith("SENDWEAPONTYPE")) { // sending weapon type
					Main.handleWeaponTypeFromClient(message);
				} else if (fullMessage.startsWith("SENDBUBBLESEFFECT")) { // sending bubble effect
					MapHandler.handleBubblesEffect(message);
				} else if (fullMessage.startsWith("SENDINTERACTIONSOUND")) { // sending interactionsound
					MapHandler.handleInteractionSound(message);
				} else if (fullMessage.startsWith("SENDSHIELDINFO")) { // sending shield info
					// tar emot information om hur m�nga charges en spelare har kvar p� sin sk�ld
					int playerNumber = fetchPlayerNumber(message);
					int charges = Integer.parseInt(fetchInfo(message));
					Player player = Main.getPlayerByNumber(playerNumber);
					player.getFireShield().setCharges(charges);
				}

			}
		} catch (NullPointerException e) {
			e.printStackTrace();
		} catch (IndexOutOfBoundsException e) {
			e.printStackTrace();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}

	}

	// tar bort prefixet fr�n ett meddelande
	static String fetchMessage(String message) {
		if (message.contains("�")) {
			String[] split = message.split("�");
			message = split[0];
			calculateDelay(split[1]);
		}

		String[] messageSplit = message.split("!");
		if (messageSplit.length > 1) {
			message = messageSplit[1];
		}

		return message;
	}

	static void handlePlayersListInfo(String message) {
		message = message.replace("[", "");
		message = message.replace("]", "");

		String[] playerNumbers = message.split(",");

		Main.players.clear(); // t�mmar listan f�r att fylla om den
		Main.activePlayersNumber.clear(); // t�mmar listan f�r att fylla om den

		for (int i = 0; i < playerNumbers.length; i++) {

			int playerNumber = Integer.parseInt(playerNumbers[i].trim());

			Player player;
			// kollar om man ska l�gga till sig sj�lv, �teranv�nder i s� fall samma gamla objekt
			if (playerNumber == Client.playerNumber) {
				player = Main.clientPlayer;
			} else {
				player = new OtherPlayer(playerNumber, 0);
			}
			Main.addPlayer(player);
			Main.activePlayersNumber.add(playerNumber);
		}

		Main.clientPlayer.sendAllInfo(); // n�r listan g�rs om betyder det att det troligtvis har anslutit en ny spelare s� d�rf�r skickas all info till server f�r att den nya klienten ska f� informationen
	}

	// sendData() �r den metod som huvudsakligen ska anv�ndas f�r att skicka information till server
	public static void sendData(String prefix, String message) {
		String temp = message;
		OUT.println(prefix + "!" + playerNumber + "&" + temp + "�" + System.currentTimeMillis());
		OUT.flush();
	}

	public static void send(String message) {
		OUT.println(message);
		OUT.flush();
	}

	// h�mtar s�ndarens spelarnummer fr�n ett meddelande
	public static int fetchPlayerNumber(String message) {
		String[] split = message.split("&");

		int playerNumber = Integer.parseInt(split[0]);

		return playerNumber;
	}

	// h�mtar informationsdelen fr�n ett meddelande
	public static String fetchInfo(String message) {
		String[] split = message.split("&");

		String info = split[1];

		return info;
	}

	// skickar ett 'ping' till server, server kommer svara med att skicka tillbaka ett 'ping' s� fort som m�jligt s� att efterdr�jningen till
	// servern kan r�knas ut
	static void ping() {

		sentPing = System.currentTimeMillis();

		// System.out.println("Sending ping with: " + sentPing);
		OUT.println("PING");
		OUT.flush();
	}

	static void calculateDelay(String message) {

		recievedMessagesFromClients++;

		long sentTime = Long.parseLong(message);

		long recievedTime = System.currentTimeMillis();

		long delay = recievedTime - sentTime;

		totalDelayToClients += delay;

		averageDelayToClients = totalDelayToClients / recievedMessagesFromClients;

	}

	public static void main(String[] args) {

		Screen.init();
		Main.mainMenu = new MainMenu();
		Screen.createFrame();
	}

}
