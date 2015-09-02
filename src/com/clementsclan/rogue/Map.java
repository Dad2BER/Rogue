package com.clementsclan.rogue;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

import com.clementsclan.rogue.Tile.Type;

public class Map {
	private Tile[][] grid;
	private List<Rectangle> roomList;
	private final int windingPercent = 0;
	Random random;
	
	public Map(int w, int h, int maxRooms) {
	    random = new Random();
		
	    initGrid(w,h);	 //Allocate the grid and set all the cells to walls to start		
	    placeRooms(maxRooms); //Randomly place rooms onto the Grid (No two rooms may overlap)
	    fillInMaze();  	 //Fill in anywhere that is not a room with hallways
	    connectRegions();  	 //Start in a random room, remove a connector to join it to a neighboring region.	
	    fillDeadEnds();	 //Clean up dead end hallways
	    CloseAllDoors();	 //Close the doors.  They are inserted open, so that regions can be connected
	    UpdateWallImages();
	}
	
	private int currentRegion = -1;
	private int[][] regions2D;

	private void draw(GC gc, Rectangle gridRect, Rectangle destRect) {
		int tileWidth = destRect.width / gridRect.width;
		int tileHeight = destRect.height / gridRect.height;
		for(int y=0; y<gridRect.height; y++) { 
			for(int x=0; x<gridRect.width; x++){
				grid[gridRect.x + x][gridRect.y + y].draw(gc, destRect.x+x*tileWidth, destRect.y+y*tileHeight,tileWidth,tileHeight);
			}
		}		
	}
	
	public Rectangle draw(GC gc, Point centerPoint, int numberTileWide, int numberTileHigh, Rectangle destRect) {
		Rectangle r = new Rectangle(centerPoint.x - numberTileWide/2, centerPoint.y-numberTileHigh/2, numberTileWide, numberTileHigh);
		if (r.x < 0) {r.x = 0;}
		else if ((r.x+r.width) >= grid.length) { r.x = grid.length - r.width; }
		
		if (r.y < 0) { r.y = 0; }
		else if ((r.y + r.height) >= grid[0].length) { r.y = grid[0].length - r.height; }
		
		draw(gc, r, destRect);
		return r;
	}
	
	public int getNumberRooms() { return roomList.size(); }

	public Rectangle getRandomRoom() { return getRoom(random.nextInt(roomList.size())); }
		
	public Rectangle getRoom(int roomIndex) {
		Rectangle r = roomList.get(roomIndex);
		return new Rectangle(r.x, r.y, r.width, r.height);
	}

	
	private void initGrid(int w, int h) {
		grid = new Tile[w][h];
		regions2D = new int[w][h];
		for(int y=0; y<grid[0].length; y++) {  //Fill in the entire grid with walls
			for(int x=0; x<grid.length; x++){
				grid[x][y] = new Tile(Tile.Type.WALL);
				regions2D[x][y] = currentRegion;
			}	
		}
	}
	private boolean rectanglesOverlap(Rectangle r1, Rectangle r2) {
		boolean bRVal = true;
		if ((r1.x+r1.width)<r2.x || (r2.x+r2.width)<r1.x || (r1.y+r1.height)<r2.y || (r2.y+r2.height)<r1.y) {
		    bRVal = false;
		}
		return bRVal;
	}
		
	//NOTE:  Trick to this, is that rooms always are located at an odd number x,y and are odd widths and heights
//	       This guarantees that there is the correct spacing for hallways
	private void placeRooms(int maxRooms) {
		roomList = new ArrayList<Rectangle>();
		for(int tryCount=0; tryCount<maxRooms; tryCount++) {
			int x = random.nextInt(grid.length/2)*2+1;
			int y = random.nextInt(grid[0].length/2)*2+1;
			int width = 3 + random.nextInt(3)*2; // This will give us 3, 5, 7 as possible room widths
			int height = 3 + random.nextInt(3)*2; // This will give us 3, 5, 7 as possible room heights
			Rectangle newRoom = new Rectangle(x,y,width,height);
			if ((x+width) < grid.length && (y+height) < grid[0].length) {
				boolean bAddRoom = true; //Check every existing room to see if this room overlaps
				for(Rectangle existingRoom : roomList) {
					if (rectanglesOverlap(newRoom, existingRoom)) {
						bAddRoom = false;
						break;
					}
				}
				if (bAddRoom) {//We did not find any overlapping room, so we can add it
					currentRegion++;
					roomList.add(newRoom);
					for(x=0; x<newRoom.width; x++) {
						for(y=0; y<newRoom.height; y++) {
						    setTile(newRoom.x + x, newRoom.y + y, Tile.Type.ROOM);
						    regions2D[newRoom.x + x][newRoom.y + y] = currentRegion;
						}
					}
				}
			}
		}
	}
	private void fillInMaze() {
		for (int y = 1; y < grid[0].length; y += 2) {
			for (int x = 1; x < grid.length; x += 2) {
				if (getTile(x,y).isSolid()) {
					growMaze(x,y);
				}
			}
		}		
	}
		
	private enum Direction {UP, DOWN, LEFT, RIGHT};
	private Point directionOffset(Point p, Direction d, int m) {
		int deltaY = 0;
		int deltaX = 0;
		switch(d) {
			case UP:		deltaY = -1;		break;
			case DOWN:	deltaY = 1;		break;
			case LEFT:	deltaX = -1;		break;
			case RIGHT: 	deltaX = 1;		break;
		}
		return new Point(p.x + deltaX*m, p.y + deltaY*m);
	}
		
	private void growMaze(int x, int y) {
		List<Point> cells = new ArrayList<Point>();
		Direction lastDir = null;

		currentRegion++;
		setTile(x, y, Tile.Type.FLOOR);
		regions2D[x][y] = currentRegion;

		Point start = new Point(x,y);
		cells.add(start);
		Random random = new Random();
		while (cells.size() > 0) {
			Point cell = cells.get(cells.size()-1);

			// See which adjacent cells are open.
			List<Direction> unmadeCells = new ArrayList<Direction>();
			for (Direction dir : Direction.values()) {
				if (canCarve(cell, dir)) unmadeCells.add(dir);
			}
			if(unmadeCells.size() > 0) { //Did we find at least one direction we can go		        
				Direction dir;  // Prefer carving in the same direction
			        if (unmadeCells.contains(lastDir) && random.nextInt(101) > windingPercent) {
			        	        dir = lastDir;
				} 
			        else {
			          	int randomIndex = random.nextInt(unmadeCells.size());
			        	        dir = unmadeCells.get(randomIndex);
				}
			        Point carvePoint = directionOffset(cell, dir, 1);
				setTile(carvePoint.x, carvePoint.y, Tile.Type.FLOOR);
				regions2D[carvePoint.x][carvePoint.y] = currentRegion;
				carvePoint = directionOffset(cell, dir, 2);
				setTile(carvePoint.x, carvePoint.y, Tile.Type.FLOOR);
				regions2D[carvePoint.x][carvePoint.y] = currentRegion;

				cells.add(carvePoint);
				lastDir = dir;
			} 
			else {  // No adjacent uncarved cells.
				cells.remove(cells.size()-1);
			        lastDir = null;  // This path has ended.
			}
		}
	}
	private void connectRegions() {
		//Region 0 is always a randomly placed room.  We will start there
		currentRegion = 0;
		List<Point> possibleConnectors = getBoarderWalls(currentRegion);
		while (possibleConnectors.size() > 0) { //If there are possible connectors, there must be multiple regions
			int randomIndex = random.nextInt(possibleConnectors.size());
			ConnectSections(possibleConnectors.get(randomIndex));
			possibleConnectors = getBoarderWalls(currentRegion);
		}
	}
	
	private boolean canCarve(Point pos, Direction direction) {
		boolean bRVal = false;
		Point gridCheckPoint = directionOffset(pos, direction, 3);
		if (gridCheckPoint.x < 1 || gridCheckPoint.x >= (grid.length-1) ||
			gridCheckPoint.y < 1 || gridCheckPoint.y >= (grid[0].length-1)) {
			bRVal = false;
		}
		else {
			Point dstPoint = directionOffset(pos, direction, 2);
			bRVal = getTile(dstPoint.x, dstPoint.y).isSolid();
		}
		return bRVal;
	}

	//Is this a wall, and does it sit between two different regions
	private boolean isBoarderWall(int x, int y, int regionID) {
		boolean bRVal = false;
		if (regions2D[x][y] != -1) { //The block must be a wall
			bRVal = false;
		}
		else {  
			int regionMatchCount = 0;
			if (regions2D[x-1][y] == regionID) {regionMatchCount++;}
			if (regions2D[x+1][y] == regionID) {regionMatchCount++;}
			if (regions2D[x][y-1] == regionID) {regionMatchCount++;}
			if (regions2D[x][y+1] == regionID) {regionMatchCount++;}
			if (regionMatchCount == 1) { //The region we are looking for exists on only one side of the wall
				int otherRegionCount = 0;
				if (regions2D[x-1][y] != regionID && regions2D[x-1][y] != -1) {otherRegionCount++;}
				else if (regions2D[x+1][y] != regionID && regions2D[x+1][y] != -1) {otherRegionCount++;}
				else if (regions2D[x][y-1] != regionID && regions2D[x][y-1] != -1) {otherRegionCount++;}
				else if (regions2D[x][y+1] != regionID && regions2D[x][y+1] != -1) {otherRegionCount++;}
				if (otherRegionCount > 0) { 	bRVal = true;	}
			}
		}
		return bRVal;
	}

	//Get a list of every wall, that seperates two regions.
	private List<Point> getBoarderWalls(int regionID) {
		List<Point> boarderWalls = new ArrayList<Point>();
		for (int y = 1; y < regions2D[0].length-1; y ++) {
		      for (int x = 1; x < regions2D.length-1; x ++) {
		    	  if(isBoarderWall(x,y,regionID)) {  boarderWalls.add(new Point(x,y));	  }
		      }
		}
		return boarderWalls;
	}

	//After we join two regions we will want to replace the region id of the old with the current
	private void setRegion(int oldRegion) {
		for(int y=0; y<regions2D[0].length; y++) {
			for(int x=0; x<regions2D.length; x++){
				if (regions2D[x][y] == oldRegion) { regions2D[x][y] =currentRegion; }
			}	
		}		
	}

	//This is the wall we are going to remove.  Turn it into a door and join all the adjacent regions
	private void ConnectSections(Point p) {
	    setTile(p.x, p.y, Tile.Type.DOOR);
	    regions2D[p.x][p.y] = currentRegion;
	    if (regions2D[p.x-1][p.y] != -1 && regions2D[p.x-1][p.y] != currentRegion ) {setRegion(regions2D[p.x-1][p.y]); }
	    if (regions2D[p.x+1][p.y] != -1 && regions2D[p.x+1][p.y] != currentRegion ) {setRegion(regions2D[p.x+1][p.y]);	}
	    if (regions2D[p.x][p.y-1] != -1 && regions2D[p.x][p.y-1] != currentRegion ) {	setRegion(regions2D[p.x][p.y-1]);}
	    if (regions2D[p.x][p.y+1] != -1 && regions2D[p.x][p.y+1] != currentRegion ) {	setRegion(regions2D[p.x][p.y+1]);}
	}
	private void fillDeadEnds() {
		for (int y = 1; y < grid[0].length; y++) {
			for (int x = 1; x < grid.length; x++) {
				checkAndFill(x,y);
			}
		}
	}

	
	public Tile getTile(int x, int y) {
		return grid[x][y];
	}
	
	public void setTile(int x, int y, Tile.Type t) {
		grid[x][y] = new Tile(t);
		if (t == Type.DOOR) {
			if (grid[x-1][y].isSolid() && grid[x+1][y].isSolid()) {
				grid[x][y].setImage(1);
				grid[x][y].setSolid(false);
			}
			else {
				grid[x][y].setImage(3);
				grid[x][y].setSolid(false);
			}
		}
	}
	
	//Recursively find floors with walls on three sides, and filling them in with walls
	private void checkAndFill(int x, int y) {
		if (!getTile(x,y).isSolid()) {
			int solidCount = 0;
			if (getTile(x-1,y).isSolid()) { solidCount++; }
			if (getTile(x+1,y).isSolid()) { solidCount++; }
			if (getTile(x,y-1).isSolid()) { solidCount++; }
			if (getTile(x,y+1).isSolid()) { solidCount++; }
			if (solidCount >= 3) {  //If there were blocks on three or four sides
				grid[x][y] = new Tile(Type.WALL);
				checkAndFill(x-1,y); //We may have just made a neighbor a dead end
				checkAndFill(x+1,y); //Now we recursively call checkAndFill for all the neighbor blocks
				checkAndFill(x,y-1); 
				checkAndFill(x,y+1); 				
			}                        
		}
	}
	
	public void CloseDoor(int x, int y) {
		if(grid[x][y].getType() == Type.DOOR && !grid[x][y].isSolid()) {
			grid[x][y].setImage(grid[x][y].getImage() -1 );
			grid[x][y].setSolid(true);
		}
	}
	
	public void OpenDoor(int x, int y) {
		if(grid[x][y].getType() == Type.DOOR && grid[x][y].isSolid()) {
			grid[x][y].setImage(grid[x][y].getImage() +1 );
			grid[x][y].setSolid(false);
		}
		
	}
	
	public void CloseAllDoors() {
		for (int y=1; y< grid[0].length; y++) {
			for (int x=1; x<grid.length; x++) {
				CloseDoor(x,y);
			}
		}
	}
	
	public void hideMap() {
		for(int y=0; y<grid[0].length; y++) {
			for(int x=0; x< grid.length; x++) {
				grid[x][y].hide();
			}
		}
	}
	
	public void SetVisible(Point center, int radius) {
		Rectangle maxRect = new Rectangle(center.x - radius, center.y - radius, 2*radius, 2*radius);
		for(int x=maxRect.x; x<=maxRect.x+maxRect.width; x++) {
			if (x >=0 && x < grid.length) {
				for(int y=maxRect.y; y<=maxRect.y+maxRect.width; y++) {
					if (y >= 0 && y < grid[0].length) {
						double distance = Math.sqrt((center.x-x)*(center.x-x) + (center.y - y)*(center.y - y));
						if (distance <= radius) {
							grid[x][y].show();
						}
					}
				}
			}
		}
	}
	
	private void UpdateWallImages() {
		for(int y=0; y<grid[0].length; y++) {
			for(int x=0; x< grid.length; x++) {
				if(grid[x][y].getType() == Type.WALL) {
					boolean bTop = y>0 && grid[x][y-1].getType()==Type.WALL;
					boolean bBottom = y<grid[0].length-1 && grid[x][y+1].getType() == Type.WALL;
					boolean bLeft = x>0 && grid[x-1][y].getType() == Type.WALL;
					boolean bRight = x<grid.length-1 && grid[x+1][y].getType() == Type.WALL;
					grid[x][y].UpdateWallImage(bTop, bBottom, bLeft, bRight);
				}
			}
		}	
	}

}
