package com.clementsclan.rogue;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

public class Player extends Sprite{
	
	private static Image playerImage = null;
	private int hitPoints=100;
	private int sightRadius = 3;

	public Player(int x, int y) {
		super(true, x, y);
		if (playerImage == null) {
			Display display = Display.getCurrent();
			Image fileImage = new Image(display,"images/player.png");
			playerImage = getTransparentVersion(display, fileImage);			
		}
		setImage(playerImage, 1);
	}
	//Manipulate the Players Hit Points
	public int getHitPoints() {	return hitPoints;	}
	public int setHP(int newHitPoints) { hitPoints = newHitPoints; return hitPoints;}
	public int addHP(int hpOffset) { return setHP(hitPoints + hpOffset); }

	public int getVisibilityRadius() { return sightRadius; }
	public void setVisiblilityRadius(int radius) { sightRadius = radius; }
		
	

}
