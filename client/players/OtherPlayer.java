package client.players;

import java.awt.Graphics2D;

import client.Screen;
import client.players.spells.AuraSpell;
import client.players.spells.CastSpell;
import client.players.spells.Spell;

public class OtherPlayer extends Player {

	public OtherPlayer(int playerNumber, int characterType) {
		super(playerNumber, characterType);
	}

	public void update() {
		commonUpdate();
	}

	public void paintCharacter(Graphics2D g2d) {
		// om karakt�ren g�r �t h�ger ska bilden spelgas, detta g�rs genom att bredden g�rs negativ och x v�rdet flyttas lika l�ngt som karakt�rens bredd
		int modWidth = 1;
		int modX = 0;

		// om man g�r �t v�nster
		if (isFacingLeft()) {
			modWidth = -1;
			modX = -getWidth();
		}

		g2d.drawImage(getImage(), Screen.fixX(getX(), 1) - modX, Screen.fixY(getY(), 1), getWidth() * modWidth, getHeight(), null);

	}

	public void castSpellFromServer(String info) {
		String[] spellInfo = info.split("@");

		int x = Integer.parseInt(spellInfo[0]);
		int y = Integer.parseInt(spellInfo[1]);
		double direction = Double.parseDouble(spellInfo[2]);
		int id = Integer.parseInt(spellInfo[3]);
		int type = Integer.parseInt(spellInfo[4]);

		Spell spell = null;

		if (Spell.castSpells.contains(type)) {
			spell = new CastSpell(x, y, type, id, direction, this);
		} else if (Spell.auraSpells.contains(type)) {
			spell = new AuraSpell(x, y, type, id, 0, this);
		}
		
		activeSpells.add(spell);
	}

}
