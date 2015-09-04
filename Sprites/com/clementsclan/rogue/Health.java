package com.clementsclan.rogue;

public class Health extends Sprite {
	private int hpBonus;
	
	public Health(int x, int y, int hitPoints) {
		super(false, x, y, "sprites/images/health.png", 1);
		setBonus(hitPoints);
	}
	
	//Manipulate the Players Hit Points
	public int getBonus() {	return hpBonus;	}
	public int setBonus(int newBonus) { hpBonus = newBonus; return hpBonus;}

}
