package client.objects.lootableWorldObjects;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.net.URL;

import client.Main;
import client.Screen;
import client.handlers.InputHandler;
import client.handlers.MapHandler;
import client.handlers.Sound;
import client.objects.Platform;
import client.objects.WorldObject;

public abstract class LootableWorldObject extends WorldObject {

	private boolean hovered = false;
	private Image imageHighlight;
	private boolean falling = false;
	private int fallingSpeed = 1;
	private int numItems;

	URL pickupSound = null;

	// G�r s� att inte massa ljud fr�n lootableworldobjects spelar samtidigt n�r man springer p� massa saker p� marken
	static long lastSoundStamp = 0;
	static long soundDelay = 50;

	public LootableWorldObject(int x, int y, int width, int height, double paralax, String imagePath, int objectId, int versionType) {
		super(x, y, width, height, paralax, imagePath, objectId, versionType);
	}

	public abstract void uniqueUpdate();

	@Override
	public void update() {
		uniqueUpdate();
		
		Rectangle lootableRect = getCollisionBox();
		Rectangle mouseRect = new Rectangle(InputHandler.getWorldMouseX(), InputHandler.getWorldMouseY(), 1, 1);

		// om man hovrar p� den
		if (lootableRect.intersects(mouseRect)) {
			hovered = true;
		} else {
			hovered = false;
		}

		Rectangle playerRect = Main.clientPlayer.getCollisionBox();

		if (lootableRect.intersects(playerRect)) {
			if (Main.clientPlayer.isAlive()) {
				pickUpItem();
				MapHandler.sendRemoveObject(this);
				MapHandler.removeObject(this);
			}
		}

		falling = true;

		// kollar om den kollidar men n�got platform
		for (int i = 0; i < MapHandler.worldObjects.size(); i++) {
			WorldObject obj = MapHandler.worldObjects.get(i);

			if (obj instanceof Platform) {
				Rectangle rectObj = obj.getCollisionBox();
				if (rectObj.intersects(lootableRect)) {
					falling = false;
				}
			}

		}

		// n�r objektet faller
		if (falling) {
			int y = getY() + fallingSpeed;
			setY(y);
			setCollisionBox(createCollisionBox()); // uppdaterar colllision box
		}

	}

	public void playPickupSound() {

		long timeNow = System.currentTimeMillis();
		long timeSinceLast = timeNow - lastSoundStamp; // hur l�nge sedan det senaste ljudet spelades fr�n ett lootable world object

		// kollar s� att det g�tt en viss tid innan n�sta ljud spelas upp s� inte massor av ljud spelas samtidigt n�r man lootar m�nga saker p� marken
		if (timeSinceLast > soundDelay) {
			if (pickupSound != null) {
				Sound.play(pickupSound, getX(), getY(), 1f);

				lastSoundStamp = System.currentTimeMillis(); // sparar vad tiden �r nu i en genensam variabel f�r alla lootableworldobjekts
			}
		}
	}

	@Override
	public Image getImage() {
		Image img = super.getImage();

		if (isHovered()) {
			img = imageHighlight;
		}

		return img;
	}

	public boolean isHovered() {
		return hovered;
	}

	public abstract void pickUpItem();

	@Override
	public Rectangle createCollisionBox() {
		return new Rectangle(getX(), getY(), getWidth(), getHeight());
	}

	// initaliserar highlight bilden f�r objecktet. Om det inte finns n�gon highlight bild blir bara hela bilden vitare
	public void setImageHighlight(Image imgHighlight) {

		Image objectImage = super.getImage();
		BufferedImage bimage = new BufferedImage(objectImage.getWidth(null), objectImage.getHeight(null), BufferedImage.TYPE_INT_ARGB);

		Graphics2D bGr = bimage.createGraphics();
		bGr.drawImage(objectImage, 0, 0, null);

		if (imgHighlight != null) {
			bGr.drawImage(imgHighlight, 0, 0, objectImage.getWidth(null), objectImage.getHeight(null), null);
		} else {
			bGr.setColor(Color.white);
			Screen.setScreenAlpha(bGr, 0.3f);
		}

		this.imageHighlight = bimage;

	}

	public void setPickupSound(URL url) {
		this.pickupSound = url;
	}

	public int getNumItems() {
		return numItems;
	}

	public void setNumItems(int numItems) {
		this.numItems = numItems;
	}

}
