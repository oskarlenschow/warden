package client.objects.interactableWorldObjects;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.net.URL;

import client.Client;
import client.Main;
import client.Screen;
import client.handlers.InputHandler;
import client.handlers.Sound;
import client.objects.WorldObject;

public abstract class InteractableWorldObject extends WorldObject {

	private boolean hovered = false;
	private Image imageHighlight;
	private boolean clickActivated = false; // k�r s� att click metoden bara k�rs en g�ng n�r man klickat p� ett object

	protected int interactionTime = 0;

	private URL completeSound;

	// interactionsljud som spelas upp flera g�nger med ett mellanrum
	private URL interactionSound;
	private int interactionSoundInterval = 0;;
	private int interactionTimeCounter = 0;

	public boolean playerInReach = false;

	int infoBoxWidth = 250;
	int infoBoxHeight = 200;

	int lineWrapWidth = (int) (infoBoxWidth * 0.6);



	int centerTextYOffset = 0;

	String infoText = "";

	int infoTextSize = 20;

	public InteractableWorldObject(int x, int y, int width, int height, double paralax, String imagePath, int objectId, int versionType) {
		super(x, y, width, height, paralax, imagePath, objectId, versionType);

		setImageHighlight(getHighlightImage());

		infoBoxHeight = infoTextSize * 2;

	}

	public abstract Image getHighlightImage();

	public abstract void uniqueOnStartInteraction();

	public void onStartInteraction() {
		uniqueOnStartInteraction();
		interactionTimeCounter = 0;
		playInteractionSound(); // spelar upp interactionsljudet i b�rjan
		sendInteractionSound();
	}

	@Override
	public void paint(Graphics2D g2d) {
		super.paint(g2d); // m�lar ut objektet som vanligt

		if (hovered) {
			paintInfoBox(g2d);
		}
	}

	public void paintInfoBox(Graphics2D g2d) {

		Screen.frontImageGraphics = Screen.setAlpha(Screen.frontImageGraphics, 0.6f);
		Screen.frontImageGraphics.setColor(new Color(255, 255, 255));
		int infoBoxX = InputHandler.onScreenMouseX;
		int infoBoxY = InputHandler.onScreenMouseY;

		Screen.frontImageGraphics.fillRect(infoBoxX, infoBoxY, infoBoxWidth, infoBoxHeight); // m�lar ut boxen

		Screen.frontImageGraphics = Screen.setAlpha(Screen.frontImageGraphics, 1f); // �ters�tller opaciteten

		// m�lar ut info texten p� fronImageGraphics f�r att den ska komma �ver allt annat
		Screen.paintText(infoBoxX + infoBoxWidth / 2, infoBoxY + centerTextYOffset, Screen.secondFont, Font.PLAIN, infoTextSize, getInfoText(), Color.black, Screen.frontImageGraphics, lineWrapWidth, true);

	}

	public abstract void uniqueUpdate();

	public void updateInteraction() {
		interactionTimeCounter += Screen.sleep;

		// betyder att det inte ska spelas n�got interaktions-ljud p� detta objekt
		if (interactionSoundInterval != 0) {
			// g�r s� att det blir ett intervall i vilket interaktions-ljudet spelas upp
			if (interactionTimeCounter >= interactionSoundInterval) {
				playInteractionSound();
				sendInteractionSound();
				interactionTimeCounter = 0;
			}
		}

	}

	@Override
	public void update() {
		uniqueUpdate();

		Rectangle objectRect = getCollisionBox();
		Rectangle mouseRect = new Rectangle(InputHandler.getWorldMouseX(), InputHandler.getWorldMouseY(), 1, 1);

		// om man h�ller p� objektet
		if (objectRect.intersects(mouseRect)) {
			hovered = true;
			// om man klickar p� objektet
			if (InputHandler.clicking && !clickActivated) {
				clickObject();
				clickActivated = true;
			}
		} else {
			hovered = false;
		}

		// om man �r tillr�ckligt n�ra f�r att interacta med detta object
		if (objectRect.intersects(Main.clientPlayer.getCollisionBox())) {
			playerInReach = true;

			// om man f�rs�ker interacta med n�got
			if (Main.clientPlayer.isHoldingInteract()) {

				// kollar s� man inte redan interactar med n�got
				if (!Main.clientPlayer.isInteracting()) {

					// kollar s� man inte faller eller r�r sig nu
					if (!Main.clientPlayer.isMoving() && !Main.clientPlayer.isFalling()) {
						startInteraction();
						onStartInteraction();

					}
				}
			}

			if (!InputHandler.clicking) {
				clickActivated = false;
			}

		} else { // n�r spelaren inte n�r objektet
			playerInReach = false;
		}

	}

	// skickar till alla andra klienter att interaktionsljudet ska spelas upp
	public void sendInteractionSound() {
		Client.sendData("#SENDINTERACTIONSOUND", getX() + "@" + getY() + "@" + getWidth() + "@" + getHeight());
	}

	public void playInteractionSound() {
		if (interactionSound != null) {
			Sound.play(interactionSound, getX(), getY(), 1f);
		}
	}

	public abstract void clickObject();

	public void startInteraction() {
		Main.clientPlayer.startInteraction(this);
	}

	public abstract void uniqueCompleteInteraction();

	public void completeInteraction() {
		uniqueCompleteInteraction();
		playCompleteSound();
	}

	public void playCompleteSound() {
		if (completeSound != null) {
			Sound.play(completeSound, getX(), getY(), 1f);
		}
	}

	@Override
	public Image getImage() {
		Image img = super.getImage();

		if (isHovered()) {
			img = imageHighlight;
		}

		if (img == null) {
			img = super.getImage();
		}

		return img;
	}

	public boolean isHovered() {
		return hovered;
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
			// bGr.setColor(Color.white);
			// Screen.setScreenAlpha(bGr, 0.3f);
		}

		this.imageHighlight = bimage;

	}

	public void setInteractionSoundInterval(int interval) {
		this.interactionSoundInterval = interval;
	}

	public void setInteractionSound(URL url) {
		this.interactionSound = url;
	}

	public void setCompleteSound(URL url) {
		this.completeSound = url;
	}

	public int getInteractionTime() {
		return interactionTime;
	}

	public void setInfoText(String info) {
		infoText = info;
		calculateBoxHeight();
	}

	public void calculateBoxHeight() {

		// kollar hur m�nga rader texten kommer bli
		String[] lines = Screen.wrapLines(getInfoText(), Screen.secondFont, lineWrapWidth);
		int numLines = lines.length;
		int textHeight = numLines * infoTextSize;

		infoBoxHeight = textHeight + infoTextSize * 2; // hur h�g boxen ska vara

		centerTextYOffset = infoBoxHeight / 2 - textHeight / 2;

	}

	public String getInfoText() {
		return infoText;
	}

	

}
