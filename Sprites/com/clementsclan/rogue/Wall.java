package com.clementsclan.rogue;

public class Wall extends Sprite {
	
	public Wall(int x, int y) {
		super(true, x,y,"sprites/images/wall.png",2);
	}

	public void UpdateWallImage(boolean top, boolean bottom, boolean left, boolean right) {
		if (getNumberFrames() >= 16 ){
			int imageIndex = 0;
			if (top && bottom && left && right) { imageIndex = 15; }
			if (top && left && right) { imageIndex = 14; }
			if (top && bottom && right) {imageIndex = 13; }
			if (top && bottom && left) { imageIndex = 12; }
			if (bottom && left && right) { imageIndex = 11; }
			if (top && right) { imageIndex = 10; }
			if (top && left) { imageIndex = 9; }
			if (bottom && right) { imageIndex = 8; }
			if (bottom && left) { imageIndex = 7; }
			if (top && bottom) { imageIndex = 6; }
			if (left && right) { imageIndex = 5; }
			if (top) { imageIndex = 4; }
			if (bottom) { imageIndex = 3; }
			if (left) { imageIndex = 2; }
			if (right) { imageIndex = 1; }
			else { imageIndex = 0; }
			setImage(imageIndex);
		}
	}
	

}
