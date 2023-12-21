package client.objects;

import java.awt.Rectangle;

public class Platform extends WorldObject {

	public Platform(int x, int y, int width, int height, double paralax, String imagePath, int objectId, int versionType) {
		super(x, y, width, height, paralax, imagePath, objectId, versionType);
	}

	@Override
	public Rectangle createCollisionBox() {
		/*Image img = getImage();
		// Create a buffered image with transparency
		BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);

		// Draw the image on to the buffered image
		Graphics2D bGr = bimage.createGraphics();
		bGr.drawImage(img, 0, 0, null);

		int imgWidth = bimage.getWidth(null);
		int imgHeight = bimage.getHeight(null);

		// hur m�nga procent den hoppar varje steg, ju l�gre desto mer nogrann
		int yPercent = 1;
		int xPercent = 20;

		double yStep = (imgHeight * (yPercent * 0.01));
		int xStep = (int) (imgWidth * (xPercent * 0.01));

		int collisionHeight = 0;

		int yRunCount = 0;
		// g�r igenom alla y delar och f�r varje y del kollar den om majoriteten av alla x-delar �r vita, i s� fall ska inte collision rectangeln str�cka sig l�ngre
		for (double i = 0; i < 100; i = i + yPercent) {
			int xRunCount = 0;

			ArrayList<Integer> clrs = new ArrayList<Integer>(); // lista �ver alla f�rger p� den h�r raden
			for (int n = 0; n < 100; n = n + xPercent) {

				int clr = bimage.getRGB(xStep * xRunCount, (int) (yStep * yRunCount));

				clrs.add(clr);
				xRunCount++;

			}

			int numWhitePixels = 0;
			// loopar igenom alla pixlar som har sparats och r�knar hur m�nga som �r vita
			for (int n = 0; n < clrs.size(); n++) {
				if (clrs.get(n) == 0) { // kollar om denna pixel �r vit
					numWhitePixels++;
				}
			}

			// kollar om majoriteten av pixlarna f�r denna raden �r vita
			if (numWhitePixels > clrs.size() / 2.0) {

				break;
			}
			collisionHeight = (int) (yStep * yRunCount);
			yRunCount++;
		}
		bGr.dispose();

		double collisionPercentY = collisionHeight / (imgHeight * 1.0);

		Rectangle rectangle = new Rectangle(getX(), getY(), getWidth(), (int) (getHeight() * collisionPercentY));
		return rectangle;*/
		
		Rectangle rectangle = new Rectangle(getX(), getY(), getWidth(), 10);
		return rectangle;
	}

	@Override
	public void update() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onRemove() {
		// TODO Auto-generated method stub
		
	}

}