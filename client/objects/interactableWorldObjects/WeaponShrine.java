package client.objects.interactableWorldObjects;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;

import client.Main;
import client.handlers.Sound;
import client.players.Weapon;

public class WeaponShrine extends InteractableWorldObject {

	private int costIncreaseMultiplier = 2;
	private int originalMineralCost = 1;

	private int requiredIncreaseMultiplier = 1;
	private int originalScoreRequired = 1;

	public static int maxLevel = 2;

	boolean weaponSet = false;

	Weapon showWeapon = null; // n�sta niv� p� vapnet som spelaren f�r n�r hen uppgraderar

	public WeaponShrine(int x, int y, int width, int height, double paralax, String imagePath, int objectId, int versionType) {
		super(x, y, width, height, paralax, imagePath, objectId, versionType);

		interactionTime = 1000;

		setCompleteSound(Sound.readSoundFile("sounds/objects/upgradeWeapon.wav"));
		
		
		updateCostInfo(); // uppdaterar kostnaden


	}

	public void updateShowWeapon() {
		int level = getCurrentUpgradeLevel() + 1;

		// om vapnet som ska visas inte �r �ver max niv�n
		if (level <= maxLevel) {

			int teamNumber = Main.clientPlayer.getTeam().getTeamNumber();

			showWeapon = new Weapon(teamNumber, level, this);
		} else {
			showWeapon = null;
		}
	}

	@Override
	public void paint(Graphics2D g2d) {
		// om det finns ett vapen att visa
		if (showWeapon != null) {
			showWeapon.paint(g2d);
		}

		super.paint(g2d);

	}

	@Override
	public Rectangle createCollisionBox() {
		return new Rectangle(getX(), getY(), getWidth(), getHeight());
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

	// h�mtar den nuvarande uppgraderingsninv�n
	public int getCurrentUpgradeLevel() {
		int upgradeLevel = 0;

		if (Main.clientPlayer.getWeapon() != null) {
			upgradeLevel = Main.clientPlayer.getWeapon().getLevel();
		}

		return upgradeLevel;
	}

	// r�knar ut hur mycket mineraler det kostar f�r spelaren att uppgradera nu
	public int getScoreRequired() {
		int upgradeLevel = getCurrentUpgradeLevel();

		int scoreReq = originalScoreRequired + originalScoreRequired * requiredIncreaseMultiplier * upgradeLevel;

		return scoreReq;
	}

	// r�knar ut hur mycket mineraler det kostar f�r spelaren att uppgradera nu
	public int getMineralCost() {
		int upgradeLevel = getCurrentUpgradeLevel();

		// r�knar ut hur mycket det ska kosta p� denna vapenniv�n
		int mineralCost = originalMineralCost + originalMineralCost * costIncreaseMultiplier * upgradeLevel;
		return mineralCost;
	}

	@Override
	public void uniqueUpdate() {

		// n�r weaponshrinet skapas f�rst kommer spelarens lag att vara null, d�rf�r m�ste shrinet uppdateras efter att spelaren har f�tt sitt lag tilldelat
		if (!weaponSet) {
			if (Main.clientPlayer.getTeam() != null) {
				weaponSet = true;

				updateShowWeapon();
			}
		}


	}

	@Override
	public void uniqueCompleteInteraction() {

		Main.clientPlayer.pickUpMinerals(-getMineralCost());

		// om man inte har ett vapen f�r man ett p� niv� 0
		if (Main.clientPlayer.getWeapon() == null) {
			Main.clientPlayer.pickUpWeapon(0);
		} else {

			int currentLevel = Main.clientPlayer.getWeapon().getLevel();
			// max level
			if (currentLevel < maxLevel) {
				Main.clientPlayer.pickUpWeapon(currentLevel + 1);
			}
		}
		
		updateCostInfo(); // uppdaterar kostnaden
		updateShowWeapon(); // uppdaterar vilket vapen som ska visas


	}

	public void updateCostInfo() {
		int cost = getMineralCost();
		int scoreReq = getScoreRequired();

		setInfoText("Hold 'E' to upgrade your weapon##Cost: " + cost + " minerals##Requires: " + scoreReq + " score");
	}

	@Override
	public void uniqueOnStartInteraction() {
		int currentScore = Main.clientPlayer.getTeam().getScore();
		int currentMinerals = Main.clientPlayer.getNumMinerals();

		// h�mtar hur mycket det kostar f�r spelaren att uppgradera nu
		int mineralCost = getMineralCost();
		int scoreReq = getScoreRequired();

		// om man inte har r�d
		if (currentMinerals < mineralCost || currentScore < scoreReq) {
			Main.clientPlayer.stopInteraction();
		}

		// om man redan har det h�gsta vapnet
		Weapon weapon = Main.clientPlayer.getWeapon();

		if (weapon != null && weapon.getLevel() >= maxLevel) {
			Main.clientPlayer.stopInteraction();
		}

	}

}
