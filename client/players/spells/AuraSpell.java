package client.players.spells;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.ArrayList;

import client.Main;
import client.Screen;
import client.handlers.MapHandler;
import client.objects.Platform;
import client.players.Player;

public class AuraSpell extends Spell {

	private int lifeTime;

	// positiva effekter av att st� i sin egen aura
	private int energyReg;
	private int healthReg;

	private int effectInterval; // hur ofta man f�r effecten n�r man st�r auran
	private int effectCounter = 0;

	public AuraSpell(int startX, int startY, int type, int id, int direction, Player caster) {
		super(startX, startY, type, id, direction, caster);
	}

	@Override
	public void initSpellFromType(int type) {
		if (type == 2) {
			setSpellName("circle");

			double scale = 0.8;
			setWidth((int) (300 * scale));
			setHeight((int) (175 * scale));

			setDamage(10);

			lifeTime = 10000;

			healthReg = 1;
			effectInterval = 100;

		} else if (type == 5) {
			setSpellName("AzureBarrier");

			double scale = 0.8;
			setWidth((int) (542 * scale));
			setHeight((int) (455 * scale));

			setDamage(10);

			lifeTime = 10000;

			energyReg = 5;
			effectInterval = 100;
		}

		setY(getCaster().getY() - getHeight() / 2); // s�tter s� att nedre delen av auran �r vid castarens huvud

		placeToClosestGround(0);
	}

	// s�tter auran till n�rmsta marken under d�r den placerades ut
	public void placeToClosestGround(int tries) {
		int currentY = getY();
		Rectangle bottomBox = getBottomBox();

		boolean collides = false;

		ArrayList<Platform> platforms = MapHandler.getAllPlatforms();

		for (int i = 0; i < platforms.size(); i++) {
			Platform platform = platforms.get(i);
			// System.out.println("k�r");
			if (bottomBox.intersects(platform.getCollisionBox())) {
				collides = true;
			}
		}

		// om den fortfarande inte kollidar med en platform
		if (!collides) {
			setY(currentY += 5); // s�tter ner den lite
			// g�r s� att den inte f�rs�ker f�r alltid om det inte g�r
			if (tries < 100) {
				placeToClosestGround(tries + 1);
			}

		}

	}

	// h�mtar "f�tterna" p� auran. Delen l�ngst ner som ska st� mot marken
	public Rectangle getBottomBox() {
		int boxHeight = 10;

		int x = getX() - getWidth() / 2;
		int y = getY() + getHeight() / 2 - boxHeight;
		int width = getWidth();

		return new Rectangle(x, y, width, boxHeight);
	}

	@Override
	public void paint(Graphics2D g2d) {
		super.paint(g2d);

		g2d.fill(getBottomBox());

	}

	@Override
	public void uniqueUpdate() {

		if (getTimeAlive() >= lifeTime) {
			removeSpell();
		}

	}

	@Override
	public void causeEffect() {
		effectCounter += Screen.sleep;

		if (effectCounter >= effectInterval) {
			effectCounter = 0;
			// om det �r din egen aura
			if (getCaster() == Main.clientPlayer) {
				causePositiveEffect();
			} else {
				causeNegativeEffect();
			}
		}

	}

	public void causeNegativeEffect() {
		Main.clientPlayer.takeDamage(getDamage());
	}

	// positiva effekterna av att st� i spelln. Ges om det �r du som har lagt ut den
	public void causePositiveEffect() {
		Main.clientPlayer.addEnergy(energyReg);
		Main.clientPlayer.heal(healthReg);
	}

}
