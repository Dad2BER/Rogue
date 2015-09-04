package com.clementsclan.rogue;

public class Door extends Sprite {

	public Door(int x, int y) {
		super(false, x, y, "sprites/images/door.png", 5);
		setImage(1); //Move the index to the Open Door
	}

	public void CloseDoor() {
		if(!isSolid()) {
			setImage(getImage() -1 );
			setSolid(true);
		}
	}
	
	public void OpenDoor() {
		if(isSolid()) {
			setImage(getImage() +1 );
			setSolid(false);
		}
		
	}

	public void defineSuroundings(Sprite top, Sprite bottom, Sprite left, Sprite right) {
		if (left != null && left.isSolid() && right != null && right.isSolid()) {
			setImage(1);
			setSolid(false);
		}
		else {
			setImage(3);
			setSolid(false);
		}
	}
	
	
}
