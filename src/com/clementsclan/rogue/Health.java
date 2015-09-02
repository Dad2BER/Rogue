package com.clementsclan.rogue;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

public class Health extends Sprite {
	private static Image healthImage = null;
	private int hpBonus;
	
	public Health(int hitPoints) {
		super(false, 0, 0);
		setBonus(hitPoints);
		if (healthImage == null) {
			Display display = Display.getCurrent();
			Image fileImage = new Image(display,"images/health.png");
			healthImage = getTransparentVersion(display, fileImage);			
		}
		setImage(healthImage, 1);

	}
	
	//Manipulate the Players Hit Points
	public int getBonus() {	return hpBonus;	}
	public int setBonus(int newBonus) { hpBonus = newBonus; return hpBonus;}

}
