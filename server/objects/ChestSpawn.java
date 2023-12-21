package server.objects;

import java.util.Random;

import server.ServerMap;

// Ett ChestSpawn �r en plats d�r kistor kan spawna. Servern ansvarar f�r att spawna in kistor p� olika best�ma platser som best�ms d�r man laddar in kartan i ServerMap
// N�r en klient �ppnar en kista skickar hen denna information till servern s� detta spawn vet att dens kista har blivit �ppnad och kan spawna en ny efter en stund. 
public class ChestSpawn {

	private int x;
	private int y;
	private int width;
	private int height;

	private int spawnChance;
	private int interval;

	private boolean hasChest = false; // om det finns en kista vid detta spawnet nu

	private int intervalCounter = 0;

	private int currentChestId = 0; // vilket ID den kistan som finns vid spawnet just nu har. Beh�vs f�r n�r en klient skickar info om att en kista har �ppnats f�r att veta vilket spawn kistan tillh�r

	Random ra = new Random();

	public ChestSpawn(int x, int y, int width, int height, int spawnChance, int interval, boolean spawnWithChest) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.spawnChance = spawnChance;
		this.interval = interval;
		
		//om det ska finnas en kista fr�n b�rjan
		if (spawnWithChest) {
			spawnChest();
		}

	}

	public void update() {

		// om det inte finns n�gon kista vid dett spawn
		if (!hasChest) {
			intervalCounter += ServerMap.sleep;
			if (intervalCounter >= interval) {
				intervalCounter = 0;
				int randInt = ra.nextInt(100);

				if (randInt <= spawnChance) {
					spawnChest();
				}

			}
		}

	}

	public int getCurrentChestId() {
		return currentChestId;
	}

	public void setHasChest(boolean hasChest) {
		this.hasChest = hasChest;
	}

	public void spawnChest() {
		setHasChest(true);
		WorldObject obj = ServerMap.addObjectLive(x, y, width, height, 1, "chest", 0);
		currentChestId = obj.getObjectId();
	}
}
