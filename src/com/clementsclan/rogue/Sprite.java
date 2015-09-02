package com.clementsclan.rogue;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;

public class Sprite {
	private Image imageStrip; //One image holding multiple frames of what to draw [0][1][2]...
	private int numberFrames; //Need to hold on to the number of frames in the image, so we can index correctly
	private Rectangle imageFrame; //This will hold on to the rectangle that goes around the sub-image as the source
	private int visibleFrameIndex;
	private boolean solid; //Not used internally, but helpful for a parent or superclass
	private Point location;  //Not used internally, but helpful for a parent or superclass
	private boolean hidden;
	private List<Sprite> itemList;
	
	
	public Sprite(boolean isSolid, int x, int y) {
		itemList = null;
		location = new Point(x,y);
		solid = isSolid;
		hidden = false;
	}
	
	public int getNumberFrames() {
		return numberFrames;
	}
	
	//Get and set the location information.  
	public Point getLocation()            	{ return location;			}
	public void setLocation(Point p)      	{ location.x = p.x;  location.y = p.y; 	}
	public void setLocation(int x, int y) 	{ location.x = x;	location.y = y;	}
		
	//Get and set being solid
	public boolean isSolid()		  	{ return solid;				}
	public void setSolid(boolean bSolid) 	{ solid = bSolid;			}
	//Sets the image to use for this sprite, and tells us how many frames are in the image
	public void setImage(Image image, int nFrames) {
		imageStrip = image;
		numberFrames = nFrames;
		setImage(0);
	}
		
	//Adjust our source rectangle to frame the selection we want to use based on the frameIndex
	public void setImage(int frameIndex) {
		visibleFrameIndex = frameIndex;
		if (!hidden) {
			setFrame(visibleFrameIndex);
		}
	}
	
	public int getImage() {
		return visibleFrameIndex;
	}
	
	private void setFrame(int frameIndex) {
		int frameWidth = imageStrip.getBounds().width / numberFrames;
		imageFrame = new Rectangle(frameWidth*frameIndex,0,frameWidth,imageStrip.getBounds().height);
	}
	
	public void hide() {
		hidden = true;
		setFrame(numberFrames-1);
	}
	
	public void show() {
		hidden = false;
		setFrame(visibleFrameIndex);
	}
		
	//draw the sub image most recently set, to the destination GC and location
	public void draw(GC gc, int destX, int destY, int destWidth, int destHeight) {
		gc.drawImage(imageStrip, imageFrame.x, imageFrame.y, imageFrame.width, imageFrame.height, 
					destX, destY, destWidth, destHeight);
		if(!hidden && itemList != null) {
			for(Sprite s : itemList) {
				s.draw(gc, destX, destY, destWidth, destHeight);
			}
		}
	}
	//Utility method for letting us convert loaded images into transparent images
	public static Image getTransparentVersion(Display display, Image image) {
	    ImageData imageData = image.getImageData();
	    int transparentColor = imageData.palette.getPixel(new RGB(255,0,255));
	    imageData.transparentPixel = transparentColor;
	    image = new Image(display,imageData); 
	    return image;
	}
	
	public Sprite[] GetItems() {
		Sprite[] rList = null;
		if (itemList != null) {rList = (Sprite[])itemList.toArray();}
		return rList;
	}
	
	public void addItem(Sprite s) {
		if (itemList == null) {	itemList = new ArrayList<Sprite>(); }
		itemList.add(s);
	}
	
	public void removeItem(Sprite s) {
		itemList.remove(s);
	}
	

}
