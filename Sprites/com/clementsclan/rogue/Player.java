package com.clementsclan.rogue;

public class Player extends Sprite{
	
	private int hitPoints=100;
	private int sightRadius = 3;

	public Player(int x, int y) {
		super(true, x, y, "sprites/images/player.png", 1);
	}
	//Manipulate the Players Hit Points
	public int getHitPoints() {	return hitPoints;	}
	public int setHP(int newHitPoints) { hitPoints = newHitPoints; return hitPoints;}
	public int addHP(int hpOffset) { return setHP(hitPoints + hpOffset); }

	public int getVisibilityRadius() { return sightRadius; }
	public void setVisiblilityRadius(int radius) { sightRadius = radius; }
		
	

}
