package client.objects.interactableWorldObjects;

import java.awt.Image;
import java.util.Random;

import client.Client;
import client.Main;
import client.Screen;
import client.handlers.MapHandler;
import client.handlers.Sound;

public class PotionShrine extends InteractableWorldObject {

	private int cost = 3;

	// variabler f�r att spela upp effect ibland
	private int effectInterval = 1000;
	private int timeCounter = 0;
	private int chanceForEffect = 10; // hur h�g procent chans det �r att effekten spelas vid ett intervall

	public PotionShrine(int x, int y, int width, int height, double paralax, String imagePath, int objectId, int versionType) {
		super(x, y, width, height, paralax, imagePath, objectId, versionType);

		interactionTime = 500;

		setInfoText("Hold 'E' to swap " + cost + " minerals for a potion");

		
		setCompleteSound(Sound.readSoundFile("sounds/objects/PotionHandle.wav"));
	}

	@Override
	public void onRemove() {
		// TODO Auto-generated method stub

	}

	@Override
	public void clickObject() {
		// TODO Auto-generated method stub

	}

	@Override
	public Image getHighlightImage() {
		// TODO Auto-generated method stub
		return null;
	}

	// skapar en bubbel effekt �ver objektet. SendToOther best�mmer om denna klienten ansvarar f�r att skicka informationen om bubblorna till alla andra klienter
	public void causeEffect(int num, boolean sendToOther) {

		int delay = 700;
		int bubbleWidth = 10;
		int bubbleHeight = 10;
		int startX = getX() + getWidth() / 2 - bubbleWidth / 2;
		int startY = getY() - bubbleHeight;

		MapHandler.causeBubbleEffect(startX, startY, bubbleWidth, bubbleHeight, num, getVersionType(), delay);

		if (sendToOther) {
			Client.sendData("#SENDBUBBLESEFFECT", startX + "@" + startY + "@" + bubbleWidth + "@" + bubbleHeight + "@" + num + "@" + getVersionType() + "@" + delay);
		}

	}

	@Override
	public void uniqueUpdate() {
		timeCounter += Screen.sleep;

		// g�r s� att det kommer en bubbel-effekt ibland
		if (timeCounter >= effectInterval) {
			// random mellan 1 och 100
			Random ra = new Random();
			int randInt = ra.nextInt(100);

			// g�r s� att effekten inte spelas upp varje intervall genom slump. F�r att �ka variationen
			if (randInt <= chanceForEffect) {
				causeEffect(1, false);
			}

			timeCounter = 0; // �terst�ller timern
		}
	}

	@Override
	public void uniqueCompleteInteraction() {
		// kollar vilket typ av torn detta �r och ger den potionen som man ska f� fr�n detta torn
		if (getVersionType() == 0) {
			Main.clientPlayer.pickUpHealthPotion(1);
		} else if (getVersionType() == 1) {
			Main.clientPlayer.pickUpEnergyPotion(1);
		}

		Main.clientPlayer.pickUpMinerals(-cost); // tar bort s� mycket det kostade att g�ra detta

		causeEffect(3, true); // orskar en bubbel-effekt och skickar till alla andra klienter

	}

	@Override
	public void uniqueOnStartInteraction() {
		// det kostar wood att interacta med detta objekt
		if (Main.clientPlayer.getNumMinerals() < cost) {
			Main.clientPlayer.stopInteraction();
		}
	}

}
