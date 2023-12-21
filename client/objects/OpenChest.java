package client.objects;

import java.awt.Image;
import java.util.ArrayList;

import client.Screen;
import client.handlers.Images;
import client.handlers.MapHandler;

public class OpenChest extends WorldObject {

	private int timeExisted = 0;
	private int maxLifeTime = 5000;

	// variabler f�r animationen p� kistan
	private int animationCounter = 0;
	private int changeImageDelay = 75;
	private boolean animationComplete = false;
	private int currentImage = 0;

	// gemensamma f�r alla �ppna kistor
	public static ArrayList<Image> images = new ArrayList<Image>(); // static f�r att spara p� ramminnet
	public static int numImages = 3;

	public OpenChest(int x, int y, int width, int height, double paralax, String imagePath, int objectId, int versionType) {
		super(x, y, width, height, paralax, imagePath, objectId, versionType);
	}

	// l�ser in alla bilder f�r animationen. K�rs fr�n Images.java n�r spelet startas
	public static void initImages() {
		for (int i = 0; i < numImages; i++) {
			Image image = Images.readImageFromPath("chests/openChest" + (i + 1) + ".png");
			images.add(image);
		}
	}

	@Override
	public void update() {
		animationCounter += Screen.sleep;
		timeExisted += Screen.sleep;

		// n�r det g�tt en viss tid byter kistan bild till en helt �ppen kista f�r att animera den
		if (animationCounter >= changeImageDelay && !animationComplete) {
			// byter bild
			setImage(images.get(currentImage));
			currentImage++;
			animationCounter = 0;

			// om animationen �r p� sista bilden
			if (currentImage == images.size()) {
				animationComplete = true;
			}

		}

		// tar bort den tomma kistan efter en stund
		if (timeExisted >= maxLifeTime) {
			MapHandler.removeObject(this);
		}

	}

	@Override
	public void onRemove() {
		// TODO Auto-generated method stub

	}

}
