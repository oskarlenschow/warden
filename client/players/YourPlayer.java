package client.players;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.net.URL;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import client.Client;
import client.Screen;
import client.handlers.InputHandler;
import client.handlers.MapHandler;
import client.handlers.Sound;
import client.objects.Platform;
import client.objects.WorldObject;
import client.objects.interactableWorldObjects.InteractableWorldObject;
import client.players.actionbar.ActionBar;
import client.players.actionbar.DisplayAbility;
import client.players.spells.AuraSpell;
import client.players.spells.CastSpell;
import client.players.spells.Spell;

public class YourPlayer extends Player {

	private double flyingSpeed = 2;
	private double currentMovingSpeed = 0;
	private double movingAcceleration = 0.09;
	private double movingDeacceleration = 0.025;
	private double maxMovingSpeed = 3;

	private double startJumpingSpeed = 6;
	private double currentJumpingSpeed = startJumpingSpeed;
	private double minJumpingSpeed = 0;

	private double fallingAcceleration = 0.09;
	private double startFallingSpeed = 0;
	private double currentFallingSpeed = startFallingSpeed;
	private double maxFallingSpeed = 10;

	private double lengthFallen = 0;
	private double damagePerPixel = 0.02;
	private int safeFallingDistance = 1200;

	private AtomicBoolean doubleJumpReady; // denna �r av typen atomic boolean f�r att kunna skapa en referens till variablen
	private AtomicInteger doubleJumpCooldown = new AtomicInteger(5000);
	private AtomicInteger doubleJumpCooldownCounter = new AtomicInteger(0);
	private double startDoubleJumpingSpeed = 7;

	private AtomicBoolean doubleJumpUnlocked = new AtomicBoolean(false);
	private AtomicBoolean dashUnlocked = new AtomicBoolean(false);

	public static int onScreenX = 0;
	public static int onScreenY = 0;

	private double dashAcceleration = 0.2;
	private int startDashSpeed = 20;
	private double currentDashSpeed = 0;

	private int regHealthDelay = 800;
	private int regHealthAmount = 2;

	private int regEnergyDelay = 200;
	private int regEnergyAmount = 3;

	private AtomicInteger dashCooldown = new AtomicInteger(20000);
	private AtomicInteger dashCooldownCounter = new AtomicInteger(0);
	private AtomicBoolean dashReady; // denna �r av typen atomic boolean f�r att kunna skapa en referens till variablen

	private boolean godMode = false;

	private boolean holdingInteract = false; // om man f�rs�ker interacta med n�got
	private InteractableWorldObject interactionObject; // det object man interactar med
	private boolean interacting = false; // n�r man h�ller p� att interacta med n�got
	private int timeInteracted = 0;

	private double cameraDelayX = 0;
	private double cameraDelayY = 0;

	private int cameraDelayMax = 20;
	private double cameraDelaySpeed = 0.2;

	private boolean canFly = false;

	private ActionBar actionBar;

	private Color interactionColor;

	private int delayCounter = 0;

	private AtomicInteger numWood = new AtomicInteger(0); // hur mycket wood man har p� sig

	private AtomicInteger numMinerals = new AtomicInteger(0); // hur mycket minerals man har p� sig

	private int spellCounter = 0;

	private int respawnDelay = 5000;
	private int respawnDelayCounter = 0;

	public YourPlayer(int playerNumber, int characterType) throws NoSuchMethodException, SecurityException {
		super(playerNumber, characterType);

		doubleJumpReady = new AtomicBoolean(true);
		dashReady = new AtomicBoolean(true);

		actionBar = new ActionBar();

		pickUpMinerals(100);
		pickUpWood(100);

		onScreenX = Screen.screenWidth / 2 - getWidth() / 2;
		onScreenY = 550;

	}

	public void move(double xMove, double yMove) {
		int speedModifier = 1;

		if (godMode) {
			speedModifier = 5;
		}

		int oldX = getX();
		int oldY = getY();

		oldX += xMove * speedModifier;
		oldY += yMove * speedModifier;

		setX(oldX);
		setY(oldY);

		// g�r s� man inte kan g� f�r l�ngt ut p� sidorna
		if (getX() < 0) {
			setX(0);
		} else if (getX() + getWidth() > MapHandler.worldWidth) {
			setX(MapHandler.worldWidth - getWidth());
		}

		sendCoords();

	}

	public void sendAllInfo() {
		sendCoords();
		sendIsFalling();
		sendIsFacingLeft();
		sendIsMoving();
		sendIsDashing();
		sendImageChangeSpeed();
		sendHealth();
		sendEnergy();
		sendWeaponType();
		sendShieldInfo();
	}

	public void sendCoords() {
		String message = getX() + "&" + getY();
		// sending players coordinates
		Client.sendData("#SPC", message);
	}

	// r�knar ut hur ofta ens spring-bild ska bytas baserat p� hur snabbt man r�r sig
	public void calculateCurrentImageChange() {
		int imageChangeDelay;
		// om man r�r sig
		if (currentMovingSpeed != 0) {
			double pixelsPerTick = Math.abs(currentMovingSpeed); // hur m�nga pixlar man r�r sig p� en tick
			double numPicsPerStep = getMovingImages().size() / 2.0; // hur m�nga bilder det tar f�r karakt�ren att g�ra ett steg
			double pixelsPerPic = (getWidth() * 2) / (numPicsPerStep); // Hur m�nga pixlar man uppskattas g�ra per bild

			imageChangeDelay = (int) Math.ceil(pixelsPerPic / pixelsPerTick); // Hur m�nga g�nger man ska vara p� samma bild f�r att man ska flytta sig s� m�nga pixlar man ska g�ra per bild
			if (imageChangeDelay > 20) {
				imageChangeDelay = 20;
			}
		} else {
			imageChangeDelay = 1;
		}

		setImageChangeDelay(imageChangeDelay);
	}

	// uppdaterar sin dash
	public void updateDash() {
		if (currentDashSpeed > 0) {
			currentDashSpeed -= dashAcceleration;
		}

		if (currentDashSpeed < 0) {
			currentDashSpeed += dashAcceleration;
		}

		if (Math.abs(currentDashSpeed) < dashAcceleration) {
			currentDashSpeed = 0;
		}
	}

	public void updateCameraDelay() {

		if (isFalling()) {

			int mod = 1;

			if (isJumping()) {
				mod = -1;
			}

			if (Math.abs(cameraDelayY) < cameraDelayMax) {
				cameraDelayY += cameraDelaySpeed * mod;
			}

		}

		else {
			int mod = 1;

			if (cameraDelayY < 0) {
				mod = -1;
			}

			if (Math.abs(cameraDelayY) > 0) {
				cameraDelayY -= cameraDelaySpeed * mod;
			}

			if (Math.abs(cameraDelayY) < cameraDelaySpeed) {
				cameraDelayY = 0;
			}
		}

		// f�r horizontellt
		if (isMovingRight() || isMovingLeft() || isDashing()) {

			int speedMod = 1;

			if (isDashing()) {
				speedMod = 1;
			}

			int mod = 1;
			if (isMovingLeft()) {
				mod = -1;
			}

			if (Math.abs(cameraDelayX) < cameraDelayMax * speedMod) {
				cameraDelayX += cameraDelaySpeed * mod * speedMod;
			}

		}

		else {

			int mod = 1;

			if (cameraDelayX < 0) {
				mod = -1;
			}

			if (Math.abs(cameraDelayX) > 0) {
				cameraDelayX -= cameraDelaySpeed * mod;
			}

			if (Math.abs(cameraDelayX) < cameraDelaySpeed) {
				cameraDelayX = 0;
			}

			/*
			 * double percent = currentMovingSpeed / maxMovingSpeed;
			 * 
			 * cameraDelayX = cameraDelayMax * percent;
			 */

		}
	}

	public void update() {

		delayCounter += Screen.sleep;
		checkCollision();
		calculateCurrentImageChange();
		commonUpdate();
		updateInteraction();
		updateCooldowns();
		updateDash();
		updateCameraDelay();
		updateRespawn();

		setMoving(false);

		if (isAlive()) {
			// s� l�nge man r�r sig �t n�got h�ll
			if (isMovingLeft() || isMovingRight()) {

				int direction = 1; // 1 = right, -1 = left

				if (isMovingLeft()) {
					direction = -1;
				}

				currentMovingSpeed += movingAcceleration * direction;

				// om man har g�tt �ver max hastigheten
				if (Math.abs(currentMovingSpeed) > maxMovingSpeed) {
					currentMovingSpeed = maxMovingSpeed * direction;
				}

			} else { // om man ska deaccellerara
				if (currentMovingSpeed > 0) {
					currentMovingSpeed -= movingDeacceleration;
				}
				if (currentMovingSpeed < 0) {
					currentMovingSpeed += movingDeacceleration;
				}
				if (Math.abs(currentMovingSpeed) < movingDeacceleration) {
					currentMovingSpeed = 0;
				}

			}

			if (isMovingLeft()) {
				setMoving(true);
				setFacingLeft(true);
			} else if (isMovingRight()) {
				setMoving(true);
				setFacingLeft(false);
			}

			double totalMovingSpeed = currentMovingSpeed + currentDashSpeed;

			move(totalMovingSpeed, 0);

			// om man har n�got som g�r att man kan flyga, exempelvis n�r man st�r vid en stege
			if (canFly) {
				if (isMovingUp()) {
					move(0, -flyingSpeed);
				}
				if (isFalling()) { // bara om man �r i luften kan man r�ra sig ner�t
					if (isMovingDown()) {
						move(0, flyingSpeed);
					}
				}
			}

			if (godMode) {
				if (isMovingUp()) {
					move(0, -3);
				}
				if (isMovingDown()) {
					move(0, 3);
				}
			}

			// om man faller och inte hoppar och inte �r i godmode ska man falla ner�t
			if (isFalling() && !isJumping() && !godMode && !canFly) {
				lengthFallen += currentFallingSpeed;
				if (currentFallingSpeed < maxFallingSpeed) {
					currentFallingSpeed += fallingAcceleration;

				}
				move(0, currentFallingSpeed);
			}

			if (isJumping()) {
				if (currentJumpingSpeed > minJumpingSpeed) { // om man ska forts�tta hoppa
					currentJumpingSpeed -= fallingAcceleration;
					move(0, -currentJumpingSpeed);
				} else {
					stopJumping();
				}
			}

			// g�r s� man f�r nytt liv �ver tid
			if (delayCounter % regHealthDelay == 0) {
				heal(regHealthAmount);
			}

			// g�r s� man f�r ny energy �ver tid
			if (delayCounter % regEnergyDelay == 0) {
				addEnergy(regEnergyAmount);
			}
		}

		sendIsFalling();
		sendIsMoving();
		sendImageChangeSpeed();
		sendIsDashing();

		actionBar.update();
	}

	// plockar upp vapen eller s�tter samma om man redan har
	public void pickUpWeapon() {
		int level = 0;

		if (getWeapon() != null) {
			level = getWeapon().getLevel();
		}

		pickUpWeapon(level);
	}

	public void pickUpWeapon(int level) {
		int versionType = getTeam().getTeamNumber();
		// int versionType = 0;

		Weapon weapon = new Weapon(versionType, level, this);
		setWeapon(weapon);
		sendWeaponType();
	}

	public void checkCollision() {
		setFalling(true);
		// loopar igenom alla world objects och kollar om man kolliderar med dem
		for (int i = 0; i < MapHandler.worldObjects.size(); i++) {
			WorldObject object = MapHandler.worldObjects.get(i);
			if (object instanceof Platform) { // kollar om det �r en platform
				if (getFeetCollisionBox().intersects(object.getCollisionBox())) {
					stopFalling();
				}
			}
		}

	}

	public void toggleGodMode() {
		godMode = !godMode;
	}

	public ActionBar getActionBar() {
		return actionBar;
	}

	public void setActionBar(ActionBar actionBar) {
		this.actionBar = actionBar;
	}

	public void stopFalling() {
		if (isFalling()) {
			setFalling(false);
			currentFallingSpeed = startFallingSpeed;

			if (lengthFallen > safeFallingDistance) {
				int damage = (int) (lengthFallen * damagePerPixel);
				takeDamage(damage);
			}
			lengthFallen = 0;

		}

	}

	public void startJump() {
		// om man inte faller eller hoppar eller om doubleJump �r redo och uppl�st
		if ((!isJumping() && !isFalling()) || (doubleJumpReady.get() && doubleJumpUnlocked.get())) {

			if (!getCanFly()) { // om man flyger kan man inte hoppa
				// om man redan hoppar eller faller anv�nds doubleJump
				if (isJumping() || isFalling()) { // n�r man anv�nder double jump

					// om man har l�st upp doubleJump
					if (doubleJumpUnlocked.get()) {
						doubleJumpReady.set(false);
						currentJumpingSpeed = startDoubleJumpingSpeed;
						sendDoubleJump();
						playJumpSound();
					}
				} else { // n�r man hoppar vanligt
					currentJumpingSpeed = startJumpingSpeed;
				}

				setJumping(true);
				stopFalling();
			}
		}

	}

	public void sendDoubleJump() {
		Client.sendData("#SENDDOUBLEJUMPEFFECT", "");
		createDoubleJumpEffect();
	}

	public void stopJumping() {
		setJumping(false);
	}

	public void setHealth(int health) {
		super.setHealth(health);
		sendHealth();
	}

	public void heal(int amount) {
		int health = getHealth() + amount;
		setHealth(health);
	}

	public void takeDamage(int damage) {

		// om man har n�got skydd p� sin sk�ld tar man ingen skada men en charge tas bort
		if (getFireShield().getNumCharges() > 0) {
			getFireShield().consumeCharge();
		} else {

			int health = getHealth() - damage;
			setHealth(health);

			stopInteraction();

			// n�r man d�r
			if (health <= 0) {
				sendDropAllItems();

				// halverar allt man har p� sig
				setNumWood((int) (Math.round(getNumWood() / 2.0)));
				setNumMinerals((int) (Math.round(getNumMinerals() / 2.0)));
				setNumHealthPotions((int) (Math.round(getNumHealthPotions() / 2.0)));
				setNumEnergyPotions((int) (Math.round(getNumEnergyPotions() / 2.0)));
			}
		}
	}

	public void updateCooldowns() {
		if (!doubleJumpReady.get()) {
			doubleJumpCooldownCounter.addAndGet(Screen.sleep);

			if (doubleJumpCooldownCounter.get() >= doubleJumpCooldown.get()) {
				doubleJumpReady.set(true);
				doubleJumpCooldownCounter.set(0);
			}

		}

		if (!dashReady.get()) {
			dashCooldownCounter.addAndGet(Screen.sleep);

			if (dashCooldownCounter.get() >= dashCooldown.get()) {
				dashReady.set(true);
				dashCooldownCounter.set(0);
			}
		}

	}

	public void updateInteraction() {
		// om man har sl�ppt interaction knappen slutar man interacta med objectet
		if (!holdingInteract || isMoving() || isFalling()) {
			stopInteraction();
		}

		if (interacting) {
			timeInteracted += Screen.sleep;

			interactionObject.updateInteraction();

			// om man har interactat s� l�nge som kr�vs f�r detta objekt
			if (timeInteracted >= interactionObject.getInteractionTime()) {
				interactionObject.completeInteraction();
				stopInteraction();
			}

		}

	}

	// uppdatar timern f�r att spawna om
	public void updateRespawn() {
		if (!isAlive()) {
			respawnDelayCounter += Screen.sleep;

			if (respawnDelayCounter >= respawnDelay) {
				respawn();
				respawnDelayCounter = 0;
			}
		} else {
			respawnDelayCounter = 0;
		}
	}

	public void respawn() {
		setStartPosition();
		setHealth(100);
	}

	public boolean getCanFly() {
		return canFly;
	}

	public int getOnScreenX() {
		return (int) (onScreenX /* + cameraDelayX */);
	}

	public void setOnScreenX(int onScreenX) {
		this.onScreenX = onScreenX;
	}

	public int getOnScreenY() {
		return (int) (onScreenY /* + cameraDelayY */);
	}

	public void setOnScreenY(int onScreenY) {
		this.onScreenY = onScreenY;
	}

	public void sendShieldInfo() {
		Client.sendData("#SENDSHIELDINFO", getFireShield().getNumCharges() + "");
	}

	// skickar information om vilken vapen man har
	public void sendWeaponType() {

		Weapon weapon = getWeapon();
		String message = "";

		if (weapon == null) {
			message = "none";
		} else {
			message = weapon.getVersionType() + "@" + weapon.getLevel();
		}

		Client.sendData("#SENDWEAPONTYPE", message);

	}

	// n�r man d�r skickas information om hur mycket saker som ska droppas p� marken
	public void sendDropAllItems() {
		Client.sendData("SENDDROPALLITEMS", getX() + "@" + getY() + "@" + getNumWood() + "@" + getNumMinerals() + "@" + getNumHealthPotions() + "@" + getNumEnergyPotions());
	}

	// skickar till alla andra att man skjuter en spell
	public void sendCastSpell(Spell spell) {
		Client.sendData("#SENDCASTSPELL", spell.getStartX() + "@" + spell.getStartY() + "@" + spell.getMovingDirection() + "@" + spell.getId() + "@" + spell.getType());
	}

	public void sendIsDashing() {
		int dashing = (isDashing()) ? 1 : 0;
		Client.sendData("#SNDISDASHING", dashing + "");
	}

	public void sendIsFalling() {
		int falling = (isFalling()) ? 1 : 0;
		Client.sendData("#ISFALLING", falling + "");
	}

	public void sendIsMoving() {
		int moving = (isMoving()) ? 1 : 0;
		Client.sendData("#ISMOVING", moving + "");
	}

	public void sendIsFacingLeft() {
		int facingLeft = (isFacingLeft()) ? 1 : 0;
		Client.sendData("#ISFACINGLEFT", facingLeft + "");
	}

	public void sendImageChangeSpeed() {
		Client.sendData("#SNDIMAGECHANGEDELAY", getImageChangeDelay() + "");
	}

	public void sendHealth() {
		Client.sendData("#SNDHEALTH", getHealth() + "");
	}

	public void sendEnergy() {
		Client.sendData("#SENDENERGY", getEnergy() + "");
	}

	public double getFallingSpeed() {
		return currentFallingSpeed;
	}

	public boolean isMoving() {
		return (Math.abs(currentMovingSpeed) > 0);
	}

	public boolean isDashing() {
		return (Math.abs(currentDashSpeed) > 0);
	}

	public boolean isHoldingInteract() {
		return holdingInteract;
	}

	public void setFacingLeft(boolean facingLeft) {

		boolean changed = false;
		// om den byter
		if (isFacingLeft() != facingLeft) {
			changed = true;
		}

		super.setFacingLeft(facingLeft);

		if (changed) {
			sendIsFacingLeft();
		}
	}

	public boolean isInteracting() {
		return interacting;
	}

	public void setHoldingInteract(boolean holdingInteract) {
		this.holdingInteract = holdingInteract;
	}

	public void startInteraction(InteractableWorldObject object) {
		this.interactionObject = object;
		interacting = true;
		timeInteracted = 0;
		interactionColor = new Color(255, 0, 0);
	}

	public void stopInteraction() {
		this.interactionObject = null;
		interacting = false;
	}

	@Override
	public void paintCharacter(Graphics2D g2d) {
		g2d = (Graphics2D) g2d.create();

		// om karakt�ren g�r �t h�ger ska bilden spelgas, detta g�rs genom att bredden g�rs negativ och x v�rdet flyttas lika l�ngt som karakt�rens bredd
		int modWidth = 1;
		int modX = 0;

		// om man g�r �t v�nster
		if (isFacingLeft()) {
			modWidth = -1;
			modX = -getWidth();
		}

		// om man �r utzoomad ska din karakt�r m�las p� ett annorlunda s�tt
		if (Screen.zoomOutDone) {
			g2d.drawImage(getImage(), Screen.fixX(getX(), 1) - modX, Screen.fixY(getY(), 1), getWidth() * modWidth, getHeight(), null);
		} else {
			g2d.drawImage(getImage(), getOnScreenX() - modX, getOnScreenY(), getWidth() * modWidth, getHeight(), null);

			if (Screen.devMode) {
				g2d.drawRect(Screen.fixX(getCollisionBox().x, 1), Screen.fixY(getCollisionBox().y, 1), getCollisionBox().width, getCollisionBox().height);
			}
			// m�lar progress cirklen f�r en interaction
			if (interacting) {

				double completePercent = ((timeInteracted * 1.0) / interactionObject.getInteractionTime());

				// interactionColor.getR
				int red = interactionColor.getRed();
				int green = interactionColor.getGreen();
				int blue = interactionColor.getBlue();

				// om cirkeln �r i den f�rsta halvan. I s� fall ska det gr�na �ka och g� mot 255 s� att cirkeln blir gul
				if (completePercent < 0.5) {
					green = (int) (255 * completePercent * 2);
				}

				// om man �r p� den andra halvan av cirkeln. I s� fall ska det r�da minska s� den g�r mot gr�n
				if (completePercent > 0.5 && completePercent < 1) {
					double tmpCompletePercent = (completePercent - 0.5) * 2; // r�knar ut hur l�ngt man har kommit p� andra halvan
					red = (int) (255 * (1 - tmpCompletePercent));
				}

				interactionColor = new Color(red, green, blue);

				g2d.setColor(interactionColor);

				g2d.setStroke(new BasicStroke(6)); // g�r str�ck tjockare

				int deg = (int) (completePercent * 360);

				g2d.drawArc(getOnScreenX() + 80, getOnScreenY(), 45, 45, 90, -deg);

			}
		}
	}

	public void castSpell(int type) {

		if (isAlive()) {
			// int direction = (isFacingLeft()) ? -1 : 1;

			double direction = InputHandler.getMouseAngle();

			Spell spell = null;

			// om denna spellens ID finns i listan f�r cast spells. Allts� en spell som ska skjutas iv�g
			if (Spell.castSpells.contains(type)) {
				spell = new CastSpell(getX(), getY(), type, spellCounter, direction, this);
			}
			// om denna spellens ID finns i listan f�r aura spells. Allts� en spell som l�ggs p� marken
			else if (Spell.auraSpells.contains(type)) {
				spell = new AuraSpell(getX(), getY(), type, spellCounter, 0, this);
			} else if (type == Spell.fireShieldId) {
				getFireShield().renewShield();
			} else if (type == Spell.fanOfKnivesId) { // fan of knives ska skjuta ut massor med knivar �t alla h�ll

				double centerAngleOffset = Math.PI / 16;

				int num = 10;
				for (int i = 0; i < num; i++) {

					double splitAngle = (Math.PI * 0.125) / (num);

					double extraAngle = 0;
					if (isFacingLeft()) {
						extraAngle = Math.PI;
					}

					Spell multiSpell = new CastSpell(getX(), getY(), 1, spellCounter, splitAngle * i + extraAngle - centerAngleOffset, this);
					addSpell(multiSpell);
				}

			}

			// om en spell har lagts till
			if (spell != null) {
				addSpell(spell);
			}
		}
	}

	public void addSpell(Spell spell) {
		sendCastSpell(spell);
		activeSpells.add(spell);
		spellCounter++;
	}

	public void setStartPosition() {
		setX(getTeam().getStartX());
		setY(getTeam().getStartY());
	}

	public void setNumEnergyPotions(int numPotions) {
		actionBar.getEnergyPotionAbility().setAmount(numPotions);
	}

	public void setNumHealthPotions(int numPotions) {
		actionBar.getHealthPotionAbility().setAmount(numPotions);
	}

	public void pickUpHealthPotion(int numPotions) {
		DisplayAbility ability = actionBar.getHealthPotionAbility();
		int amount = ability.getAmount() + numPotions;
		ability.setAmount(amount);
	}

	public void pickUpEnergyPotion(int numPotions) {
		DisplayAbility ability = actionBar.getEnergyPotionAbility();
		int amount = ability.getAmount() + numPotions;
		ability.setAmount(amount);
	}

	public int getNumEnergyPotions() {
		return actionBar.getEnergyPotionAbility().getAmount();
	}

	public int getNumHealthPotions() {
		return actionBar.getHealthPotionAbility().getAmount();
	}

	public void removeWood(int numWood) {
		this.numWood.addAndGet(-numWood);
	}

	public void pickUpWood(int numWood) {
		this.numWood.addAndGet(numWood);
	}

	public void removeMinerals(int numMinerals) {
		this.numMinerals.addAndGet(-numMinerals);
	}

	public void pickUpMinerals(int numMinerals) {
		this.numMinerals.addAndGet(numMinerals);
	}

	// startar en dash
	public void startDash() {
		// om man har l�st upp dash
		if (dashUnlocked.get()) {
			// om den �r redo
			if (dashReady.get()) {
				dashReady.set(false);

				int mod = 1;
				if (isFacingLeft()) {
					mod = -1;
				}

				currentDashSpeed = startDashSpeed * mod;
			}
		}
	}

	public void setNumMinerals(int minerals) {
		this.numMinerals.set(minerals);
	}

	public void setNumWood(int wood) {
		this.numWood.set(wood);
	}

	public void setCanFly(boolean canFly) {
		this.canFly = canFly;
	}

	public AtomicBoolean getDoubleJumpReady() {
		return doubleJumpReady;
	}

	public AtomicBoolean getDashReady() {
		return dashReady;
	}

	public AtomicInteger getDoubleJumpCooldown() {
		return doubleJumpCooldown;
	}

	public AtomicInteger getDoubleJumpCooldownCounter() {
		return doubleJumpCooldownCounter;
	}

	public AtomicInteger getDashCooldown() {
		return dashCooldown;
	}

	public AtomicInteger getDashCooldownCounter() {
		return dashCooldownCounter;
	}

	public double getLengthFallen() {
		return lengthFallen;
	}

	public void useEnergyPotion() {
		int energy = getEnergy() + 80;
		setEnergy(energy);

	}

	public void addEnergy(int num) {
		int energy = getEnergy() + num;
		setEnergy(energy);
	}

	public void setEnergy(int energy) {
		super.setEnergy(energy);
		sendEnergy();
	}

	public void useHealthPotion() {
		int health = getHealth() + 20;
		setHealth(health);
	}

	public int getNumWood() {
		return numWood.get();
	}

	public int getNumMinerals() {
		return numMinerals.get();
	}

	public AtomicInteger getWoodCounter() {
		return numWood;
	}

	public AtomicInteger getMineralCounter() {
		return numMinerals;
	}

	public double getMovingSpeed() {
		return currentMovingSpeed;
	}

	public AtomicBoolean getDashUnlocked() {
		return dashUnlocked;
	}

	public AtomicBoolean getDoubleJumpUnlocked() {
		return doubleJumpUnlocked;
	}

}
