package client.players.actionbar;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import client.Main;
import client.Screen;
import client.handlers.Images;

public class DisplayAbility {
	private int id;
	private int x;
	private int y;
	private int size;
	private boolean round;
	private boolean showCounter;
	private int amount;
	private int keyBind;

	private Image image;
	private Image highlightImage;
	private Image shadowImage;
	private boolean hovered = false;

	private Method method;
	private Object methodHolder;
	private Object[] parameters;

	private int cooldown;
	private int cooldownLeft = 0;

	private int energyCost;

	private int levelReq = 0; // vilket level p� vapnet man beh�ver f�r att anv�nda denna ability

	public DisplayAbility(int id, int x, int y, int size, boolean round, boolean showCounter, int keyBind, Method method, Object methodHolder, Object parameter, int cooldown, int energyCost, int levelReq) {
		this.id = id;
		this.x = x;
		this.y = y;
		this.size = size;
		this.image = Images.readImageFromPath("ui/abilities/ability" + id + ".png");

		this.levelReq = levelReq;
		this.round = round;
		this.showCounter = showCounter;
		this.keyBind = keyBind;
		this.cooldown = cooldown;
		this.energyCost = energyCost;

		this.method = method; // vilken metod denna ability ska k�ra
		this.methodHolder = methodHolder; // vilket object som h�ller metoden denna ability ska k�ra

		if (round) {
			this.highlightImage = ActionBar.powerHighlight;
			this.shadowImage = ActionBar.powerShadow;
		} else {
			this.highlightImage = ActionBar.abilityHighlight;
			this.shadowImage = ActionBar.abilityShadow;
		}

		// sparar parameterns som ska skickas med methoden som denna ability ska k�ra
		if (parameter != null) { // Om ingen parameter ska skickas med methoden �r 'parameter' null. Om den inte �r null sparas parametern i en lista �ver parametrar, annars blir listan null
			parameters = new Object[1];
			parameters[0] = parameter;
		} else {
			parameters = null;
		}

		// -1 betyder att man har o�ndligt med charges
		if (!showCounter) {
			amount = -1;
		} else {
			amount = 5;
		}

	}

	public void paint(Graphics2D g2d) {

		Graphics2D g2 = Screen.reverseZoom(g2d); // reversar zoomen

		g2.drawImage(getImage(), getX(), getY(), getSize(), getSize(), null); // m�lar ikonen
		g2.drawImage(getOverlay(), getX(), getY(), getSize(), getSize(), null); // m�lar skugga eller ljus �ver ikonen beroende p� om man hovrar �ver ikonen eller inte

		paintCooldownShadow(g2); // m�lar cooldownskuggan

		// om man inte har r�d med abilityn m�las en skugga �ver den eller om man inte har tillr�ckligt h�g niv� p� vapnet f�r att g�ra denna ability
		if (Main.clientPlayer.getEnergy() < energyCost || Main.clientPlayer.getWeapon().getLevel() < levelReq) {
			paintFullShadow(g2d);
		}

		// p� vissa abilities ska en counter visas f�r att visa hur m�nga charges man har kvar
		if (getShowCounter()) {
			String text = getAmount() + "";
			Font font = new Font("Calibri", Font.BOLD, 40);

			int textWidth = Screen.getTextWidth(font, text);

			int fontX = getX() + getSize() - textWidth - 5;
			int fontY = getY() + getSize() - 5;

			g2.setFont(font);
			g2.setColor(Color.white);
			g2.drawString(getAmount() + "", fontX, fontY);
		}
	}

	public void paintFullShadow(Graphics2D g2d) {
		g2d.setColor(new Color(0, 0, 0, 180));
		if (!round) {
			g2d.fillRect(getX(), getY(), getSize(), getSize());
		} else {
			g2d.fillOval(getX(), getY(), getSize(), getSize());
		}
	}

	// M�lar cooldownskuggan. Skuggan m�las som en cirkel �ver ikonen som fylls p� allt eftersom cooldownen tickar ner. F�rst m�las cirkeln p� en bufferedimage som har samma
	// storlek som ikonen och sedan m�las bufferedimage p� panelen. Detta �r f�r att cirkeln ska kunnas m�las st�rre �n vad ikonen egentligen �r utan att synas utanf�r f�r att det
	// ska se ut som att det �r en kvadrat som skuggar ikonen.
	public void paintCooldownShadow(Graphics2D g2) {

		BufferedImage bimage = new BufferedImage(getSize(), getSize(), BufferedImage.TYPE_INT_ARGB);

		Graphics2D bGr = bimage.createGraphics();

		bGr.setColor(new Color(0, 0, 0, 180));
		// int deg = (int) (completePercent * 360);

		int width = getSize();
		int height = getSize();

		// om ikonen inte �r rund m�ste storleken vara st�rre
		if (!round) {
			width *= 2;
			height *= 2;
		}

		int x = getSize() / 2 - width / 2;
		int y = getSize() / 2 - height / 2;

		double percentComplete = (cooldownLeft * 1D) / cooldown; // hur m�nga procent av cooldownen som har g�tt

		int deg = (int) (360 * percentComplete);

		bGr.fillArc(x, y, width, height, 90, deg);

		g2.drawImage(bimage, getX(), getY(), null);

	}

	public void update() {
		// tickar ner p� cooldownen
		if (cooldownLeft > 0) {
			cooldownLeft -= Screen.sleep;
		}

		if (cooldownLeft < 0) {
			cooldownLeft = 0;
		}
	}

	public void use() {

		// Om spelaren har det vapnet som kr�vs f�r att g�ra denna ability
		if (Main.clientPlayer.getWeapon().getLevel() >= levelReq) {
			// om den inte �r p� coolden
			if (cooldownLeft == 0) {
				// om man har o�ndligt eller har mer �n 0
				if (amount == -1 || amount > 0) {
					// kollar s� man har tillr�ckligt med energy
					if (Main.clientPlayer.getEnergy() >= energyCost) {
						try {
							method.invoke(methodHolder, parameters);
						} catch (IllegalAccessException e) {
							e.printStackTrace();
						} catch (IllegalArgumentException e) {
							e.printStackTrace();
						} catch (InvocationTargetException e) {
							e.printStackTrace();
						}

						Main.clientPlayer.addEnergy(-energyCost);

						if (amount != -1) {
							amount--;
						}
						cooldownLeft = cooldown;
					}
				}
			}
		}

	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getAmount() {
		return amount;
	}

	public void setAmount(int amount) {
		this.amount = amount;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public void setHovered(boolean hovered) {
		this.hovered = hovered;
	}
	
	public boolean getHovered(){
		return hovered;
	}

	public Image getImage() {
		return image;
	}

	public Image getOverlay() {
		Image img = null;

		if (hovered) {
			img = highlightImage;
		} else {
			img = shadowImage;
		}

		return img;
	}

	public void setImage(Image image) {
		this.image = image;
	}

	public boolean getShowCounter() {
		return showCounter;
	}

	public int getKeyBind() {
		return keyBind;
	}

}
