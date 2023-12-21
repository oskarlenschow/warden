package server.objects;

import server.ServerMap;
import server.Team;

public class ObjectiveTowerSpawn {

	private int x;
	private int y;
	private int width;
	private int height;
	Team belongingTeam;

	private int gainPointDelay = 1000;
	private int gainPointAmount = 1;
	private int gainCounter = 0;

	public ObjectiveTowerSpawn(int x, int y, int width, int height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.belongingTeam = null;
	}

	public String getInfo() {
		String info = "";

		int teamOwnerNumber;

		// om ingen �ger det h�r tornet �r det neutralt och d� �r teamnumret 2
		if (belongingTeam != null) {
			teamOwnerNumber = belongingTeam.getTeamNumber();
		} else {
			teamOwnerNumber = 2;
		}

		info += x + "@";
		info += y + "@";
		info += width + "@";
		info += height + "@";
		info += teamOwnerNumber;

		return info;
	}

	public void setOwner(Team team) {
		this.belongingTeam = team;
	}

	public void update() {
		// om den tillh�r n�got lag
		if (belongingTeam != null) {
			gainCounter += ServerMap.sleep;

			// l�gger till score p� ett intervall
			if (gainCounter >= gainPointDelay) {

				belongingTeam.addScore(gainPointAmount);
				gainCounter = 0;
			}
		}

	}
}
