package com.clementsclan.rogue;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

public class Tile extends Sprite {
	public enum Type {FLOOR, WALL, ROOM, DOOR};

	private Type type;
	private static Image floorImage = null;
	private static Image wallImage = null;
	private static Image roomImage = null;
	private static Image doorImage = null;
	
	public Tile(Type t) {
		super(t==Type.WALL,0,0);
		type = t;
		if (floorImage == null) { //floorImage and wallImage need to be loaded once
			Display display = Display.getCurrent();
			floorImage = new Image(display,"images/floor.png");
			wallImage = new Image(display,"images/wall.png");
			roomImage = new Image(display,"images/room.png");
			doorImage = new Image(display, "images/door.png");
		}
		switch(type) {
			case FLOOR:	setImage(floorImage,2);		break;
			case ROOM:	setImage(roomImage,2);		break;
			case WALL:  	setImage(wallImage,2); 		break;
			case DOOR:
				setImage(doorImage,5);
				setImage(1); //Move the index to the Open Door
				break;
			default:    break; //Should never happen
		}		 
	}
	
	public void UpdateWallImage(boolean top, boolean bottom, boolean left, boolean right) {
		if (type == Type.WALL && getNumberFrames() >= 16 ){
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
	
	public Type getType() {	return type;  }
	
	public char getSymbol() {
		char returnVal;
		switch(type) {
			case FLOOR: returnVal = ' '; break;
			case WALL:  returnVal = '#'; break;
			case DOOR:  returnVal = 'D'; break;
			default:    returnVal = '?'; break;  //Should never happen
		}
		return returnVal;
	}
	
}

