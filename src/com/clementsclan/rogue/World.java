package com.clementsclan.rogue;

import java.util.Random;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class World {

	private Player player;
	private Map map; 
	private Canvas mapCanvas;
	private Canvas statsCanvas;
	private Canvas radarCanvas;
	
	private int score;

	private static final int MAP_WIDTH = 51;
	private static final int MAP_HEIGHT = 51;
	private static final int MAP_ZOOM_WIDTH = 20;
	private static final int MAP_ZOOM_HEIGHT = 20;
	
	public World(Shell shell) {
		mapCanvas = new Canvas(shell,SWT.DOUBLE_BUFFERED);
		FormData worldForm = new FormData();
		worldForm.top = new FormAttachment(0,0);
		worldForm.left = new FormAttachment(0,0);
		worldForm.right = new FormAttachment(80,0);
		worldForm.bottom = new FormAttachment(100,0);
		mapCanvas.setLayoutData(worldForm);	
		mapCanvas.addPaintListener(mapPaintListener);
		mapCanvas.addKeyListener(keyListener);
		
		statsCanvas = new Canvas(shell, SWT.DOUBLE_BUFFERED);
		FormData playerForm = new FormData();
		playerForm.top = new FormAttachment(0,0);
		playerForm.left = new FormAttachment(mapCanvas,5);
		playerForm.right = new FormAttachment(100,0);
		playerForm.bottom = new FormAttachment(60,0);
		statsCanvas.setLayoutData(playerForm);	
		statsCanvas.addPaintListener(playerPaintListener);
		statsCanvas.addKeyListener(keyListener);
		
		radarCanvas = new Canvas(shell, SWT.DOUBLE_BUFFERED);
		FormData radarForm = new FormData();
		radarForm.top = new FormAttachment(statsCanvas, 5);
		radarForm.left = new FormAttachment(mapCanvas, 5);
		radarForm.right = new FormAttachment(100,0);
		radarForm.bottom = new FormAttachment(95,0);
		radarCanvas.setLayoutData(radarForm);
		radarCanvas.addPaintListener(radarPaintListener);
		radarCanvas.addKeyListener(keyListener);

		Button resetButton = new Button(shell, SWT.PUSH);
		resetButton.setText("Rest Game");
		FormData ButtonForm = new FormData();
		ButtonForm.top = new FormAttachment(radarCanvas,5);
		ButtonForm.left = new FormAttachment(mapCanvas,5);
		ButtonForm.bottom = new FormAttachment(100,0);
		resetButton.setLayoutData(ButtonForm);
		resetButton.setToolTipText("Press Button to restart game");
		resetButton.addSelectionListener(resetButtonListener);	
		resetButton.addKeyListener(keyListener);
		
		reset();
		
	}
	
	public void reset() {
		score = 0;

		map = new Map(MAP_WIDTH, MAP_HEIGHT, 100);
		map.hideMap();

		Rectangle room = map.getRandomRoom();
		
		player = new Player(room.x+1,room.y+1);
    	map.SetVisible(player.getLocation(), player.getVisibilityRadius());
			
		Random random = new Random();
			
		int numberRooms = map.getNumberRooms();
		for(int i=0; i<numberRooms; i++) {
			if(random.nextInt(100) >= 50) { // 50% chance the room has health in it
				Rectangle r = map.getRoom(i);
				int x = r.x+random.nextInt(r.width);
				int y = r.y+random.nextInt(r.height);
				map.getTile(x,y).addItem(new Health(x,y,random.nextInt(10)+10));
			}
		}
	}
	
	private boolean movePlayer(int deltaX, int deltaY) {
	    boolean bRVal = false; //Assume that the move will fail
	    int testX = player.getLocation().x + deltaX;
	    int testY = player.getLocation().y + deltaY;
	    Sprite tile = map.getTile(testX, testY);
	    if (!tile.isSolid()) {
	    	player.setLocation(testX, testY);
	    	if (player.getHitPoints() > 0) {
	    		Sprite[] Items = map.getTile(testX, testY).GetItems();
	    		if (Items != null) { 
	    			for(Sprite s : Items) {
	    				if (s instanceof Health) {
	    					player.addHP(((Health)s).getBonus());
	    					map.getTile(testX,  testY).removeItem(s);
	    				}
	    			}
		    	}
		    	player.addHP(-1); //moving always costs you a health point
		    	score += 1;
	    	}
	    	map.SetVisible(player.getLocation(), player.getVisibilityRadius());
	    	bRVal = true; //The move worked
	    }
	    else if (tile instanceof Door) {
	    	((Door)tile).OpenDoor();
	    	bRVal = true;  //The move failed, We still want to repaint because the door changed
	    }
	    return bRVal;
	}
	
	private PaintListener playerPaintListener = new PaintListener() {
		public void paintControl(PaintEvent e) { 
			Display display = Display.getCurrent();
			Rectangle clientArea = mapCanvas.getClientArea();
			e.gc.setBackground(display.getSystemColor(SWT.COLOR_BLACK));
			e.gc.fillRectangle(clientArea.x, clientArea.y, clientArea.width, clientArea.height);
			if (player.getHitPoints() > 50) {
				e.gc.setForeground(display.getSystemColor(SWT.COLOR_GREEN));
			}
			else if (player.getHitPoints() > 20) {
				e.gc.setForeground(display.getSystemColor(SWT.COLOR_DARK_YELLOW));
			}
			else {
				e.gc.setForeground(display.getSystemColor(SWT.COLOR_RED));				
			}
			e.gc.drawText("Player Health:  " + player.getHitPoints(), clientArea.x, clientArea.x);
			e.gc.setForeground(display.getSystemColor(SWT.COLOR_CYAN));
			e.gc.drawText("Score: " + score, clientArea.x, clientArea.y+e.gc.stringExtent("Player").y);
			if (player.getHitPoints() == 0) {
				e.gc.drawText("GAME OVER", clientArea.x, clientArea.y + e.gc.stringExtent("Player").y*3);
			}
		}
	};
	
	private void PaintHelper(GC gc, Rectangle area, int numberTilesWide, int numberTilesHigh) {
		Rectangle mapRect = new Rectangle(0,0,numberTilesWide*32, numberTilesHigh*32);
		Image mapImage = new Image(gc.getDevice(), mapRect.width, mapRect.height);
		GC mapGC = new GC(mapImage);
		
		//Draw Map
		Rectangle visibleRect = map.draw(mapGC, player.getLocation(), numberTilesWide, numberTilesHigh, mapRect);
	    //Draw Health Potions
		
		int tileWidth = mapRect.width / visibleRect.width;
		int tileHeight = mapRect.height / visibleRect.height;
	    //Draw Player
	    Point playerLocation = player.getLocation();
	    if (visibleRect.contains(playerLocation)) {
	    	player.draw(mapGC, mapRect.x+(playerLocation.x-visibleRect.x)*tileWidth, mapRect.y+(playerLocation.y-visibleRect.y)*tileHeight,	tileWidth,tileHeight);
	    }
				
	    //Now resize teh off screen Image to fill teh whole rectangle passed in.  This prevents white rounding error boarders
	    gc.drawImage(mapImage, 0, 0, mapRect.width, mapRect.height, area.x, area.y, area.width, area.height);
		
	}
	
	private PaintListener radarPaintListener = new PaintListener() {
		public void paintControl(PaintEvent e) {
			PaintHelper(e.gc, radarCanvas.getClientArea(), MAP_WIDTH, MAP_HEIGHT);
		}
	};

	private PaintListener mapPaintListener = new PaintListener() {
		public void paintControl(PaintEvent e) {
			PaintHelper(e.gc, mapCanvas.getClientArea(), MAP_ZOOM_WIDTH, MAP_ZOOM_HEIGHT);
		}
	};

	private KeyListener keyListener = new KeyListener() {

		@Override
		public void keyPressed(KeyEvent e) {
			boolean bMoved = false;
			switch(e.keyCode) {
				case SWT.ARROW_UP:    bMoved = movePlayer(0,-1); break;
				case SWT.ARROW_DOWN:  bMoved = movePlayer(0,1);  break;
				case SWT.ARROW_LEFT:  bMoved = movePlayer(-1,0); break;
				case SWT.ARROW_RIGHT: bMoved = movePlayer(1,0);  break;
				default: //Do nothing for all the other keys
					break;
			}
			if (bMoved) { 
				mapCanvas.redraw();
				radarCanvas.redraw();
				statsCanvas.redraw();
			} //If we moved, Redraw the screen
		}

		@Override
		public void keyReleased(KeyEvent arg0) {
			//We don't care about the key release right now
		}    	
	};
	
	public SelectionListener resetButtonListener = new SelectionListener() {
		public void widgetDefaultSelected(SelectionEvent arg0) {
			DoSelected();
		}
		public void widgetSelected(SelectionEvent arg0) {
			DoSelected();
		}
		
		private void DoSelected() {
			reset();
			mapCanvas.redraw();
			radarCanvas.redraw();
			statsCanvas.redraw();
		}		
	};
	
}
