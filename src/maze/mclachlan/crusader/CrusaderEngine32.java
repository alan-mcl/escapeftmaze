/*
 * Copyright (c) 2011 Alan McLachlan
 *
 * This file is part of Escape From The Maze.
 *
 * Escape From The Maze is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package mclachlan.crusader;

import java.awt.Color;
import java.awt.Component;
import java.awt.Image;
import java.awt.Point;
import java.awt.image.ColorModel;
import java.awt.image.MemoryImageSource;
import java.awt.image.PixelGrabber;
import java.util.*;

/**
 * Implementation with 32-bit colour
 */ 
public class CrusaderEngine32 implements CrusaderEngine
{
	/**
	 * Container for tiles, textures and map information.
	 */
	private Map map;
	
	/**
	 * Helper class for colour-management.  
	 */ 
	private ColorModel colourModel = new CrusaderColourModel();
	
	/**
	 * First index: image nr <br>
	 * Second index: indexed pixels of the image
	 */ 
	private int[][] skyImage;
	private int skyTextureWidth, skyTextureHeight;
	
	/**
	 * 
	 */ 
	private Texture[] textures;

	/**
	 * The colour in the palette that is transparent.
	 */ 
	private Color transparency;

	/** The color to shade with */  
	private int shadeRed;
	private int shadeGreen;
	private int shadeBlue;
	
	/** The distance (in units) at which shading starts */
	private int shadingDistance;
	/** The distance (in units) for the shading effect to double */
	private int shadingThickness;

	/**
	 * Objects in the map.  The engine sorts them in descending 
	 * order of distance from the player
	 */ 
	private EngineObject[] objects;
	/** used to protect the arrays when things are dynamically added and removed */
	private final Object objectMutex = new Object();

	private boolean doShading, doLighting;
	
	/**
	 * The pixels that the engine returns to the outside world.
	 */ 
	private int[] renderBuffer;
	
	/** The width of the map (ie east-west), in grid cells */
	private int mapWidth;
	/** The length of the map (ie north-south), in grid cells */
	private int mapLength;

	/** The length of the edge of a tile, in units */
	private static int TILE_SIZE;
	
	/** width of the projection plane, in cast columns (ie pixels) */  
	private int projectionPlaneWidth;
	/** height of the projection plane, in cast columns (ie pixels) */
	private int projectionPlaneHeight;

	// Note the difference between measurement units (eg TILE_SIZE) and
	// pixels (eg PROJECTION_PLANE_HEIGHT)
	private int playerHeightInUnits;

	/** A constant from {@link CrusaderEngine.FieldOfView} */
	private int playerFovOption;
	/** 60 degrees, expressed in cast columns (ie pixels)*/
	private static int ANGLE60;
	/** 30 degrees, expressed in cast columns (ie pixels)*/
	private static int ANGLE30;
	/** 90 degrees, expressed in cast columns (ie pixels)*/
	private static int ANGLE90;
	/** 180 degrees, expressed in cast columns (ie pixels)*/
	private static int ANGLE180;
	/** 270 degrees, expressed in cast columns (ie pixels)*/
	private static int ANGLE270;
	/** 360 degrees, expressed in cast columns (ie pixels)*/
	private static int ANGLE360;
	/** 0 degrees, expressed in cast columns (ie pixels)*/
	private static int ANGLE0;
	/** 5 degrees, expressed in cast columns (ie pixels)*/
	private static int ANGLE5;
	
	private static int PLAYER_FOV;
	private static int PLAYER_FOV_HALF;

	/** Used to represent a ray hit on a vertical (ie north-south) wall*/ 
	private static final byte HIT_VERTICAL = 1;
	/** Used to represent a ray hit on a vertical (ie east-west) wall*/
	private static final byte HIT_HORIZONTAL = 2;

	/** Pre-calc trig table, indexed on angle expressed in cast columns (ie pixels)*/
	private float sinTable[];
	/** Pre-calc trig table, indexed on angle expressed in cast columns (ie pixels)*/
	private float iSinTable[];
	/** Pre-calc trig table, indexed on angle expressed in cast columns (ie pixels)*/
	private float cosTable[];
	/** Pre-calc trig table, indexed on angle expressed in cast columns (ie pixels)*/
	private float iCosTable[];
	/** Pre-calc trig table, indexed on angle expressed in cast columns (ie pixels)*/
	private float tanTable[];
	/** Pre-calc trig table, indexed on angle expressed in cast columns (ie pixels)*/
	private float iTanTable[];
	
	/** 
	 * Pre-calc correction for the fishbowl effect, indexed on angle 
	 * expressed in cast columns (ie pixels) 
	 */
	static float[] fishbowlTable;
	
	/** Pre-calc block step table, indexed on angle expressed in cast columns (ie pixels)*/
	private float[] xStepTable;
	/** Pre-calc block step table, indexed on angle expressed in cast columns (ie pixels)*/
	private float[] yStepTable;

	/** The players X-coord, in units. */ 
	private int playerX;
	/** The players Y-coord, in units. */ 
	private int playerY;
	/** The players direction, an angle expressed in cast columns (ie pixels)*/	
	private int playerArc;

	/** Defined as (ProjectionPlaneWidth/2)/tan(field of view angle/2) */
	private int playerDistToProjectionPlane;
	/** Scales the player distance to the proj plane */
	private double playerDistanceMult = 1.0;
	/** Translates the projection plane (in pixels downwards) */
	private int projPlaneOffset;
	/** How far the player moves for each forward movement, in units */
	private int playerSpeed;
	/** How far the player rotates, an angle expressed in cast columns (ie pixels)*/
	private int playerRotation;
	/** How high the player is */
	private int playerHeight;
	/** Scales the player height */
	private double playerHeightMult = 1.0;

	/** A record of grid block hits, indexed on cast column and then depth */
	private BlockHitRecord[][] blockHitRecord;
	private static int MAX_HIT_DEPTH = 5;
	
	/** A record of applicable mouse click scripts, by index in the render buffer */
	private MouseClickScript[] mouseClickScriptRecords;
	
	/** Incremented each frame, used for animation */
	private long frameCount;

	/** 
	 * The of mode player movement. A constant from 
	 * {@link mclachlan.crusader.CrusaderEngine.MovementMode}.
	 */
	private int movementMode;
	
	// AWT fields
	private MemoryImageSource pictureArray;
	private Image displayImage;

	private static Random r = new Random();

	/*-------------------------------------------------------------------------*/
	/**
	 * @param screenWidth
	 * 	The width of the projection plane, in pixels
	 * @param screenHeight
	 * 	The height of the projection plane, in pixels
	 * @param movementMode
	 * 	One of the constants 
	 * 	from {@link MovementMode}
	 * @param shadeTargetColour
	 * 	The color towards which to the engine shades.  Set it to black for a
	 * 	normal effect, white for a fog effect, and so on.
	 * @param transparentColour
	 * 	The color in associated images that the engine treats as transparent.
	 * @param doShading
	 * 	True if shading of distant objects should be done.
	 * @param doLighting
	 * 	True if light levels of tiles should be taken into account.
	 * @param shadingDistance
	 * 	The number of tiles (or fraction thereof) at which objects start to 
	 * 	be shaded.  Can be used to define an unshaded space around the player.
	 * @param shadingMultiplier
	 * 	The number of tiles (of fraction thereof) at which the shading effect
	 * 	doubles.  A higher number will result in "thicker" shading.
	 * @param projectionPlaneOffset
	 * 	The vertical offset of the projection plane from player eye level.  A
	 * 	negative number shifts it towards the floor, a positive one shifts it
	 * 	towards the ceiling.  Expressed in pixels.
	 * @param playerFieldOfView
	 * 	A constant from {@link CrusaderEngine.FieldOfView}.
	 * @param scaleDistFromProjPlane
	 * 	Scales the player distance from the projection plane.  For example, set
	 * 	it to 0.5 for half the usual distance, or 2.0 for double.
	 */
	public CrusaderEngine32(
		Map map,
		int screenWidth,
		int screenHeight,
		int movementMode,
		Color shadeTargetColour,
		Color transparentColour,
		boolean doShading,
		boolean doLighting,
		double shadingDistance,
		double shadingMultiplier,
		int projectionPlaneOffset,
		int playerFieldOfView,
		double scaleDistFromProjPlane,
		Component component)
	{
		this.map = map;
		this.transparency = transparentColour;

		this.textures = new Texture[0];
		this.initImages();

		TILE_SIZE = map.baseImageSize;
		
		this.shadingDistance = (int)(TILE_SIZE * shadingDistance);
		this.shadingThickness = (int)(TILE_SIZE * shadingMultiplier);
		this.shadeRed = shadeTargetColour.getRed();
		this.shadeGreen = shadeTargetColour.getGreen();
		this.shadeBlue = shadeTargetColour.getBlue();
		
		this.projectionPlaneWidth = screenWidth;
		this.projectionPlaneHeight = screenHeight;
		this.playerHeight = (int)(projectionPlaneHeight/2 *playerHeightMult);
		this.playerHeightInUnits = (int)(TILE_SIZE/2 *playerHeightMult);
		this.projPlaneOffset = projectionPlaneOffset;
		this.playerFovOption = playerFieldOfView;
		this.playerDistanceMult = scaleDistFromProjPlane;

		this.createTables();

		this.setMovementMode(movementMode);

		this.doLighting = doLighting;
		this.doShading = doShading;

		this.renderBuffer = new int[screenWidth * screenHeight];
		this.mouseClickScriptRecords = new MouseClickScript[screenWidth * screenHeight];
		this.blockHitRecord = new BlockHitRecord[screenWidth][MAX_HIT_DEPTH];
		for (int i = 0; i < blockHitRecord.length; i++)
		{
			for (int j = 0; j < MAX_HIT_DEPTH; j++)
			{
				blockHitRecord[i][j] = new BlockHitRecord();
			}
		}
		
		mapWidth = map.width;
		mapLength = map.length;

		this.objects = (EngineObject[])map.objects.clone();
		
		pictureArray = new MemoryImageSource(
			screenWidth, 
			screenHeight, 
			getColourModel(),
			renderBuffer,
			0,
			screenWidth);
		
		pictureArray.setAnimated(true);
		pictureArray.setFullBufferUpdates(true);
		displayImage = component.createImage(pictureArray);
		
		initMouseClickScripts();
	}
	
	/*-------------------------------------------------------------------------*/
	private void initMouseClickScripts()
	{
		for (Wall horizontalWall : map.horizontalWalls)
		{
			if (horizontalWall.mouseClickScript != null)
			{
				horizontalWall.mouseClickScript.initialise(map);
			}
			if (horizontalWall.maskTextureMouseClickScript != null)
			{
				horizontalWall.maskTextureMouseClickScript.initialise(map);
			}
		}

		for (Wall verticalWall : map.verticalWalls)
		{
			if (verticalWall.mouseClickScript != null)
			{
				verticalWall.mouseClickScript.initialise(map);
			}
			if (verticalWall.maskTextureMouseClickScript != null)
			{
				verticalWall.maskTextureMouseClickScript.initialise(map);
			}
		}

		for (EngineObject object : objects)
		{
			if (object.mouseClickScript != null)
			{
				object.mouseClickScript.initialise(map);
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	private void setMovementMode(int movementMode)
	{
		this.movementMode = movementMode;

		if (this.movementMode == MovementMode.DISCRETE)
		{
			this.playerSpeed = TILE_SIZE;
			this.playerRotation = ANGLE90;
		}
		else if (this.movementMode == MovementMode.CONTINUOUS)
		{
			this.playerSpeed = TILE_SIZE/8;
			this.playerRotation = ANGLE5;
		}
		else
		{
			throw new CrusaderException("Unrecognised mode ["+movementMode+"]");
		}
	}

	/*-------------------------------------------------------------------------*/
	private void createTables()
	{
		if (playerFovOption == FieldOfView.FOV_30_DEGREES)
		{
			ANGLE30 = projectionPlaneWidth;
			ANGLE60 = ANGLE30*2;
			ANGLE90 = (ANGLE30 * 3);
			ANGLE180 = (ANGLE90 * 2);
			ANGLE270 = (ANGLE90 * 3);
			ANGLE360 = (ANGLE60 * 6);
			ANGLE0 = 0;
			ANGLE5 = (ANGLE30 / 6);

			PLAYER_FOV = ANGLE30;
		}
		else if (playerFovOption == FieldOfView.FOV_60_DEGREES)
		{
			ANGLE60 = projectionPlaneWidth;
			ANGLE30 = (ANGLE60 / 2);
			ANGLE90 = (ANGLE30 * 3);
			ANGLE180 = (ANGLE90 * 2);
			ANGLE270 = (ANGLE90 * 3);
			ANGLE360 = (ANGLE60 * 6);
			ANGLE0 = 0;
			ANGLE5 = (ANGLE30 / 6);

			PLAYER_FOV = ANGLE60;
		}
		else if (playerFovOption == FieldOfView.FOV_90_DEGREES)
		{
			ANGLE90 = projectionPlaneWidth;
			ANGLE30 = (ANGLE90 / 3);
			ANGLE60 = ANGLE30*2;
			ANGLE180 = (ANGLE90 * 2);
			ANGLE270 = (ANGLE90 * 3);
			ANGLE360 = (ANGLE90 * 4);
			ANGLE0 = 0;
			ANGLE5 = (ANGLE90 / 18);

			PLAYER_FOV = ANGLE90;
		}
		else if (playerFovOption == FieldOfView.FOV_180_DEGREES)
		{
			int angle170 = projectionPlaneWidth;

			ANGLE180 = angle170*18/17;
			ANGLE90 = ANGLE180/2;
			ANGLE30 = (ANGLE180 / 6);
			ANGLE60 = ANGLE180/3;
			ANGLE270 = (ANGLE90 * 3);
			ANGLE360 = (ANGLE180 * 2);
			ANGLE0 = 0;
			ANGLE5 = (ANGLE180 / 36);
			PLAYER_FOV = angle170;

//			ANGLE180 = projectionPlaneWidth;
//			ANGLE90 = ANGLE180/2;
//			ANGLE30 = (ANGLE180 / 6);
//			ANGLE60 = ANGLE180/3;
//			ANGLE270 = (ANGLE90 * 3);
//			ANGLE360 = (ANGLE180 * 2);
//			ANGLE0 = 0;
//			ANGLE5 = (ANGLE180 / 36);

//			PLAYER_FOV = ANGLE180;
		}
		else
		{
			throw new CrusaderException("Invalid playerFieldOfView: "+playerFovOption);
		}
		PLAYER_FOV_HALF = PLAYER_FOV/2;

		int i;
		float radian;
		sinTable = new float[ANGLE360 + 1];
		iSinTable = new float[ANGLE360 + 1];
		cosTable = new float[ANGLE360 + 1];
		iCosTable = new float[ANGLE360 + 1];
		tanTable = new float[ANGLE360 + 1];
		iTanTable = new float[ANGLE360 + 1];
		fishbowlTable = new float[projectionPlaneWidth + 1];
		xStepTable = new float[ANGLE360 + 1];
		yStepTable = new float[ANGLE360 + 1];

		for (i = 0; i <= ANGLE360; i++)
		{
			// get the radian value (the last addition is to avoid division by 0, try removing
			// that and you'll see a hole in the wall when a ray is at 0, 90, 180, or 270 degree)
			radian = arcToRad(i) + (float)(0.0001);
			sinTable[i] = (float)Math.sin(radian);
			iSinTable[i] = (1.0F / (sinTable[i]));
			cosTable[i] = (float)Math.cos(radian);
			iCosTable[i] = (1.0F / (cosTable[i]));
			tanTable[i] = (float)Math.tan(radian);
			iTanTable[i] = (1.0F / tanTable[i]);

			//  you can see that the distance between xi is the same
			//  if we know the angle
			//  _____|_/next xi______________
			//       |
			//  ____/|next xi_________   slope = tan = height / dist between xi's
			//     / |
			//  __/__|_________  dist between xi = height/tan where height=tile size
			// old xi|
			//                  distance between xi = x_step[view_angle];
			//
			//
			// facing left
			// facing left
			if (i >= ANGLE90 && i < ANGLE270)
			{
				xStepTable[i] = (TILE_SIZE / tanTable[i]);
				if (xStepTable[i] > 0)
				{
					xStepTable[i] = -xStepTable[i];
				}
			}
			// facing right
			else
			{
				xStepTable[i] = (TILE_SIZE / tanTable[i]);
				if (xStepTable[i] < 0)
				{
					xStepTable[i] = -xStepTable[i];
				}
			}

			// FACING DOWN
			if (i >= ANGLE0 && i < ANGLE180)
			{
				yStepTable[i] = (TILE_SIZE * tanTable[i]);
				if (yStepTable[i] < 0)
				{
					yStepTable[i] = -yStepTable[i];
				}
			}
			// FACING UP
			else
			{
				yStepTable[i] = (TILE_SIZE * tanTable[i]);
				if (yStepTable[i] > 0)
				{
					yStepTable[i] = -yStepTable[i];
				}
			}
		}

		for (i = -PLAYER_FOV_HALF; i <= projectionPlaneWidth-PLAYER_FOV_HALF; i++)
		{
			radian = arcToRad(i);
			fishbowlTable[i + PLAYER_FOV_HALF] = (float)(1.0F / Math.cos(radian));
		}

		playerDistToProjectionPlane =
			(int)((projectionPlaneWidth/2) / tanTable[PLAYER_FOV_HALF] *playerDistanceMult);
			// decrease this to show more floor and ceiling
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Sets the player position in the map, assuming that the map is square and
	 * that the following axes are used:
	 * <pre>
	 * 0----x----
	 * |
	 * y
	 * |
	 * </pre>
	 *
	 * @param facing
	 * 	A constant from {@link Facing}
	 * 	determining which way the player is initially facing.
	 */
	public void setPlayerPos(int x, int y, int facing)
	{
		// place the player in the middle of the block
		int halfATile = TILE_SIZE/2;
		this.playerX = x*TILE_SIZE + halfATile;
		this.playerY = y*TILE_SIZE + halfATile;

		// set facing
		switch (facing)
		{
			case Facing.NORTH:
				this.playerArc = ANGLE270;
				break;
			case Facing.SOUTH:
				this.playerArc = ANGLE90;
				break;
			case Facing.EAST:
				this.playerArc = ANGLE0;
				break;
			case Facing.WEST:
				this.playerArc = ANGLE180;
				break;
			default:
				throw new CrusaderException("Unrecognized facing: "+facing);
		}

		if (this.movementMode == MovementMode.DISCRETE)
		{
			// offset based on facing
			switch (facing)
			{
				case Facing.NORTH:
					this.playerY += (halfATile-1);
					break;
				case Facing.SOUTH:
					this.playerY -= (halfATile-1);
					break;
				case Facing.EAST:
					this.playerX -= (halfATile-1);
					break;
				case Facing.WEST:
					this.playerX += (halfATile-1);
					break;
				default:
					throw new CrusaderException("Unrecognized facing: "+facing);
			}
		}
	}
	
	/*-------------------------------------------------------------------------*/
	public Point getPlayerPos()
	{
		return getPlayerPos(playerX,  playerY);
	}

	/*-------------------------------------------------------------------------*/
	private Point getPlayerPos(int x, int y)
	{
		int xGridIndex = x / TILE_SIZE;
		int yGridIndex = y / TILE_SIZE;

		return new Point(xGridIndex, yGridIndex);
	}

	/*-------------------------------------------------------------------------*/
	public int getPlayerFacing()
	{
		return getPlayerFacing(this.playerArc);
	}

	/*-------------------------------------------------------------------------*/
	private int getPlayerFacing(int arc)
	{
		if (this.movementMode == MovementMode.DISCRETE)
		{
			if (arc == ANGLE270)
			{
				return Facing.NORTH;
			}
			else if (arc == ANGLE90)
			{
				return Facing.SOUTH;
			}
			else if (arc == ANGLE0)
			{
				return Facing.EAST;
			}
			else if (arc == ANGLE180)
			{
				return Facing.WEST;
			}
			else
			{
				throw new CrusaderException("Invalid playerArc for discrete mode: "+arc);
			}
		}
		else
		{
			return arc;
		}
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @param key
	 * 	the key code, a constant 
	 * 	from {@link KeyStroke}
	 */
	public void handleKey(int key)
	{
		handleKey(predictKey(key));
	}

	/*-------------------------------------------------------------------------*/
	public void handleKey(PlayerStatus prediction)
	{
		synchronized(objectMutex)
		{
			this.playerArc = prediction.playerArc;
			this.playerX = prediction.playerX;
			this.playerY = prediction.playerY;
		}
	}
	
	/*-------------------------------------------------------------------------*/
	public void handleMouseClick(int x, int y)
	{
		int bufferIndex = x + y * projectionPlaneWidth;
		
		if (mouseClickScriptRecords[bufferIndex] != null)
		{
			// todo: figure out if it's a wall, wall mask or object
			mouseClickScriptRecords[bufferIndex].execute(map);
		}
	}

	/*-------------------------------------------------------------------------*/
	public MouseClickScript handleMouseClickReturnScript(int x, int y)
	{
		int bufferIndex = x + y * projectionPlaneWidth;

		if (mouseClickScriptRecords[bufferIndex] != null)
		{
			// todo: figure out if it's a wall, wall mask or object
			return mouseClickScriptRecords[bufferIndex];
		}
		else
		{
			return null;
		}
	}

	/*-------------------------------------------------------------------------*/
	public PlayerStatus predictKey(int key)
	{
		int newPlayerX = this.playerX;
		int newPlayerY = this.playerY;
		int newPlayerArc = this.playerArc;
		// currently not supported for Continuous Mode
		boolean willPassThroughWall = false;

		//  _____     _
		// |\ arc     |
		// |  \       y
		// |    \     |
		//            -
		// |--x--|
		//
		//  sin(arc)=y/diagonal
		//  cos(arc)=x/diagonal   where diagonal=speed
		float playerXDir = cosTable[newPlayerArc];
		float playerYDir = sinTable[newPlayerArc];

		int strafeLeft = newPlayerArc-ANGLE90;
		if (strafeLeft < 0)
		{
			strafeLeft += ANGLE360;
		}
		float strafeLeftXDir = cosTable[strafeLeft];
		float strafeLeftYDir = sinTable[strafeLeft];

		int strafeRight = newPlayerArc+ANGLE90;
		if (strafeRight > ANGLE360)
		{
			strafeRight -= ANGLE360;
		}
		float strafeRightXDir = cosTable[strafeRight];
		float strafeRightYDir = sinTable[strafeRight];

		int halfATile = TILE_SIZE/2-1;
		if (this.movementMode == MovementMode.DISCRETE)
		{
			// plop the player in the middle of the block, we'll move him to the
			// edge after rotation.

			if (newPlayerArc == ANGLE0)
			{
				// facing east
				newPlayerX += halfATile;
			}
			else if (newPlayerArc == ANGLE180)
			{
				// facing west
				newPlayerX -= halfATile;
			}
			else if (newPlayerArc == ANGLE90)
			{
				// facing south
				newPlayerY += halfATile;
			}
			else if (newPlayerArc == ANGLE270)
			{
				// facing north
				newPlayerY -= halfATile;
			}
			else
			{
				throw new CrusaderException(
					"Invalid playerArc for discrete mode: "+newPlayerArc);
			}
		}

		// detect if we will pass through a wall, bit of a brute force approach here.
		if (movementMode == MovementMode.DISCRETE)
		{
			int tileIndex = getMapIndex(newPlayerX/TILE_SIZE, newPlayerY/TILE_SIZE);

			switch (key)
			{
				case KeyStroke.TURN_LEFT:
				case KeyStroke.TURN_RIGHT:
					willPassThroughWall = false;
					break;
				case KeyStroke.STRAFE_LEFT:
					if (newPlayerArc == ANGLE0)
					{
						// facing east
						willPassThroughWall = map.horizontalWalls[map.getNorthWall(tileIndex)].visible;
					}
					else if (newPlayerArc == ANGLE180)
					{
						// facing west
						willPassThroughWall = map.horizontalWalls[map.getSouthWall(tileIndex)].visible;
					}
					else if (newPlayerArc == ANGLE90)
					{
						// facing south
						willPassThroughWall = map.verticalWalls[map.getEastWall(tileIndex)].visible;
					}
					else if (newPlayerArc == ANGLE270)
					{
						// facing north
						willPassThroughWall = map.verticalWalls[map.getWestWall(tileIndex)].visible;
					}
					break;
				case KeyStroke.STRAFE_RIGHT:
					if (newPlayerArc == ANGLE0)
					{
						// facing east
						willPassThroughWall = map.horizontalWalls[map.getSouthWall(tileIndex)].visible;
					}
					else if (newPlayerArc == ANGLE180)
					{
						// facing west
						willPassThroughWall = map.horizontalWalls[map.getNorthWall(tileIndex)].visible;
					}
					else if (newPlayerArc == ANGLE90)
					{
						// facing south
						willPassThroughWall = map.verticalWalls[map.getWestWall(tileIndex)].visible;
					}
					else if (newPlayerArc == ANGLE270)
					{
						// facing north
						willPassThroughWall = map.verticalWalls[map.getEastWall(tileIndex)].visible;
					}
					break;
				case KeyStroke.FORWARD:
					if (newPlayerArc == ANGLE0)
					{
						// facing east
						willPassThroughWall = map.verticalWalls[map.getEastWall(tileIndex)].visible;
					}
					else if (newPlayerArc == ANGLE180)
					{
						// facing west
						willPassThroughWall = map.verticalWalls[map.getWestWall(tileIndex)].visible;
					}
					else if (newPlayerArc == ANGLE90)
					{
						// facing south
						willPassThroughWall = map.horizontalWalls[map.getSouthWall(tileIndex)].visible;
					}
					else if (newPlayerArc == ANGLE270)
					{
						// facing north
						willPassThroughWall = map.horizontalWalls[map.getNorthWall(tileIndex)].visible;
					}
					break;
				case KeyStroke.BACKWARD:
					if (newPlayerArc == ANGLE0)
					{
						// facing east
						willPassThroughWall = map.verticalWalls[map.getWestWall(tileIndex)].visible;
					}
					else if (newPlayerArc == ANGLE180)
					{
						// facing west
						willPassThroughWall = map.verticalWalls[map.getEastWall(tileIndex)].visible;
					}
					else if (newPlayerArc == ANGLE90)
					{
						// facing south
						willPassThroughWall = map.horizontalWalls[map.getNorthWall(tileIndex)].visible;
					}
					else if (newPlayerArc == ANGLE270)
					{
						// facing north
						willPassThroughWall = map.horizontalWalls[map.getSouthWall(tileIndex)].visible;
					}
					break;
				default:
					throw new CrusaderException("Invalid key stroke: "+key);
			}
		}

		// rotate left
		switch (key)
		{
			case KeyStroke.TURN_LEFT:
				if ((newPlayerArc -= playerRotation) < ANGLE0)
				{
					newPlayerArc += ANGLE360;
				}
				break;
			case KeyStroke.TURN_RIGHT:
				// rotate right
				if ((newPlayerArc += playerRotation) >= ANGLE360)
				{
					newPlayerArc -= ANGLE360;
				}
				break;
			case KeyStroke.STRAFE_LEFT:
				// strafe left
				newPlayerX += (int)(strafeLeftXDir * playerSpeed);
				newPlayerY += (int)(strafeLeftYDir * playerSpeed);
				break;
			case KeyStroke.STRAFE_RIGHT:
				// strafe right
				newPlayerX += (int)(strafeRightXDir * playerSpeed);
				newPlayerY += (int)(strafeRightYDir * playerSpeed);
				break;
			case KeyStroke.FORWARD:
				// move forward
				newPlayerX += (int)(playerXDir * playerSpeed);
				newPlayerY += (int)(playerYDir * playerSpeed);
				break;
			case KeyStroke.BACKWARD:
				// move backward
				newPlayerX -= (int)(playerXDir * playerSpeed);
				newPlayerY -= (int)(playerYDir * playerSpeed);
				break;
			default:
				throw new CrusaderException("Invalid key stroke: "+key);
		}

		if (this.movementMode == MovementMode.DISCRETE)
		{
			// scoot the player over to the proper edge of the block

			if (newPlayerArc == ANGLE0)
			{
				// facing east
				newPlayerX -= halfATile;
			}
			else if (newPlayerArc == ANGLE180)
			{
				// facing west
				newPlayerX += halfATile;
			}
			else if (newPlayerArc == ANGLE90)
			{
				// facing south
				newPlayerY -= halfATile;
			}
			else if (newPlayerArc == ANGLE270)
			{
				// facing north
				newPlayerY += halfATile;
			}
			else
			{
				throw new CrusaderException(
					"Invalid playerArc for discrete mode: "+newPlayerArc);
			}
		}

		return new PlayerStatus(
			newPlayerX,
			newPlayerY,
			newPlayerArc,
			getPlayerPos(newPlayerX, newPlayerY),
			getPlayerFacing(newPlayerArc),
			willPassThroughWall);
	}

	/*-------------------------------------------------------------------------*/
	private void initImages()
	{
		if (map.paletteImage != null)
		{
			this.initImageGroup(map.paletteImage);
		}

		for (Texture texture : map.textures)
		{
			this.addTexture(texture);
		}

		this.skyImage = this.initImageGroup(map.skyImage);
		
		this.skyTextureWidth = map.skyImage.imageWidth;
		this.skyTextureHeight = map.skyImage.imageHeight;
	}

	/*-------------------------------------------------------------------------*/
	public void addTexture(Texture texture)
	{
		// check to see if the texture is already here:
		for (Texture texture1 : textures)
		{
			if (texture1 == texture)
			{
				return;
			}
		}

		texture.imageData = 
			new int[texture.images.length]
				[texture.imageHeight*texture.imageWidth];
		
		for (int i = 0; i < texture.images.length; i++)
		{
			int[] rawPixels = this.grabPixels(
				texture.images[i],
				texture.imageWidth,
				texture.imageHeight);
			
			this.convertPixels(
				texture.imageWidth,
				texture.imageHeight,
				rawPixels,
				texture.imageData[i]);
		}
		
		synchronized(objectMutex)
		{
			Texture[] temp = new Texture[this.textures.length+1];
			System.arraycopy(textures, 0, temp, 0, textures.length);
			temp[textures.length] = texture;
			this.textures = temp;
		}
	}

	/*-------------------------------------------------------------------------*/
	private int[] grabPixels(Image image, int width, int height)
	{
		int[] result = new int[width*height];
		PixelGrabber grabber = new PixelGrabber(
			image, 0, 0, width, height , result, 0, width);
	
		try
		{
			grabber.grabPixels();
		}
		catch (InterruptedException e)
		{
			throw new CrusaderException(e);
		}
		
		return result;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Initializes the given image group.
	 * 
	 * @return 
	 * 	A 2d array, first index image nr, second index image data
	 */ 
	private int[][] initImageGroup(ImageGroup imageGroup)
	{
		int[][] result = 
			new int[imageGroup.images.length]
				[imageGroup.imageHeight*imageGroup.imageWidth];
		
		for (int i = 0; i < imageGroup.images.length; i++)
		{
			int[] rawPixels = this.grabPixels(
				imageGroup.images[i],
				imageGroup.imageWidth,
				imageGroup.imageHeight);
			
			this.convertPixels(
				imageGroup.imageWidth,
				imageGroup.imageHeight,
				rawPixels,
				result[i]);
		}
		
		return result;
	}

	/*-------------------------------------------------------------------------*/
	private void convertPixels(int width, int height, int[] sourcePixels, int[] destPixels)
	{
		int currentPixel;
		int sourceRed, sourceGreen, sourceBlue;
		
		ColorModel cm = ColorModel.getRGBdefault();
		
		for (currentPixel = 0; currentPixel < (width * height); currentPixel++)
		{
			sourceRed = cm.getRed(sourcePixels[currentPixel]);
			sourceGreen = cm.getGreen(sourcePixels[currentPixel]);
			sourceBlue = cm.getBlue(sourcePixels[currentPixel]);
			
			// default 0, 64, 0
			if ((sourceRed == transparency.getRed()) 
				&& (sourceGreen == transparency.getGreen())
				&& (sourceBlue == transparency.getBlue()))
			{
				destPixels[currentPixel] = 0; // transparent pixel
			}
			else
			{
				destPixels[currentPixel] = sourcePixels[currentPixel]; 
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * This will render the scene and return it as an Image.
	 */
	public Image render()
	{
		// populate the renderBuffer
		this.renderInternal();
		
		// Note the importance of using framenotify=false.  Without this flag
		// the screen flickers on this method call.
		pictureArray.newPixels(0, 0, projectionPlaneWidth, projectionPlaneHeight, false);
		
		return displayImage;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * This will render the scene and return it as a byte array.
	 */
	public int[] renderInternal()
	{
		synchronized(objectMutex)
		{
			//
			// Render in vertical strips 1 pixel wide.
			//
			float castArc, castInc;

			// field of view is PLAYER_FOV degree with the point of view
			// (player's direction in the middle)
			// We will trace the rays starting from the leftmost ray
			castArc = playerArc-PLAYER_FOV_HALF;

			// wrap around if necessary
			if (castArc < 0)
			{
				castArc = ANGLE360 + castArc;
			}

			castInc = (float)PLAYER_FOV/(float)projectionPlaneWidth;

			// execute any animations
			this.animation();

			// execute any map scripts
			this.map.executeScripts(frameCount);

			// init object state for rendering
			initAndSortObjects();

			// ray cast and render each column
			for (int castColumn = 0; castColumn < projectionPlaneWidth; castColumn++)
			{
				this.rayCast(Math.round(castArc), castColumn);

				for (int depth=MAX_HIT_DEPTH-1; depth >= 0; depth--)
				{
					this.drawWall(Math.round(castArc), castColumn, depth, renderBuffer);
					for (int i=0; i<objects.length; i++)
					{
						if (this.objects[i].distance > 0)
						{
							if (this.objects[i].projectedObjectHeight > 0 && this.objects[i].endScreenX > 0)
							{
								if (castColumn >= this.objects[i].startScreenX && castColumn < this.objects[i].endScreenX)
								{
									drawObjectColumn(this.objects[i], renderBuffer, castColumn, depth);
								}
							}
						}
					}
				}

				castArc += castInc;
				if (castArc >= ANGLE360)
				{
					castArc -= ANGLE360;
				}
			}

			this.frameCount++;
		}
		
		return this.renderBuffer;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * This method sets up the global variables in preparation for calling
	 * {@link #drawWall} to draw the column.
	 *
	 * @param castArc the angle of the cast ray
	 * @param castColumn the column that is being drawn
	 */
	private void rayCast(int castArc, int castColumn)
	{
		// horizontal or vertical coordinate of intersection
		int verticalGrid;
		int horizontalGrid;

		// how far to the next bound (this is multiple of tile size)
		int distToNextVerticalGrid;
		int distToNextHorizontalGrid;
		
		float distToHorizontalGridBeingHit = Float.MAX_VALUE;
		float distToVerticalGridBeingHit = Float.MAX_VALUE;
		
		// x and y intersections
		float xIntersection;
		float yIntersection;

		BlockHitRecord horizBlockHitRecord = new BlockHitRecord();
		BlockHitRecord vertBlockHitRecord = new BlockHitRecord();

		//---- Initialisation --------------------------------------------------//

		// A special case calculation of the X-intersection initial values.
		if (castArc > ANGLE0 && castArc < ANGLE180)
		{
			// The ray is facing south
			
			horizontalGrid = (playerY / TILE_SIZE) * TILE_SIZE + TILE_SIZE;
			distToNextHorizontalGrid = TILE_SIZE;
			
			float xtemp = iTanTable[castArc] * (horizontalGrid - playerY);
			xIntersection = xtemp + playerX;
		}
		else
		{
			// The ray is facing north
			
			horizontalGrid = (playerY / TILE_SIZE) * TILE_SIZE;
			distToNextHorizontalGrid = -TILE_SIZE;
			
			float xtemp = iTanTable[castArc] * (horizontalGrid - playerY);
			xIntersection = xtemp + playerX;
			
			horizontalGrid--;
		}
		
		// A special case calculation of the Y-intersection initial values.
		if (castArc < ANGLE90 || castArc > ANGLE270)
		{
			// ray is facing east
			
			verticalGrid = TILE_SIZE + (playerX / TILE_SIZE) * TILE_SIZE;
			distToNextVerticalGrid = TILE_SIZE;
			
			float ytemp = tanTable[castArc] * (verticalGrid - playerX);
			yIntersection = ytemp + playerY;
		}
		else
		{
			verticalGrid = (playerX / TILE_SIZE) * TILE_SIZE;
			distToNextVerticalGrid = -TILE_SIZE;
			
			float ytemp = tanTable[castArc] * (verticalGrid - playerX);
			yIntersection = ytemp + playerY;
			
			verticalGrid--;
		}

		RayState horizRayState = new RayState();
		horizRayState.grid = horizontalGrid;
		horizRayState.distToNextGrid = distToNextHorizontalGrid;
		horizRayState.intersection= xIntersection;

		RayState vertRayState = new RayState();
		vertRayState.grid = verticalGrid;
		vertRayState.distToNextGrid = distToNextVerticalGrid;
		vertRayState.intersection= yIntersection;

		//---- Ray Casting -----------------------------------------------------//

		// calculate the first two hits. The nearest determines which order we
		// search for more in

		int depth = 0;

		distToHorizontalGridBeingHit = computeNextHorizBlockHit(
			castArc,
			horizRayState,
			horizBlockHitRecord,
			true);

		distToVerticalGridBeingHit = computeNextVertBlockHit(
			castArc,
			vertRayState,
			vertBlockHitRecord,
			true);

		boolean horizFirst = distToHorizontalGridBeingHit < distToVerticalGridBeingHit;

		try
		{
			while (depth < MAX_HIT_DEPTH &&
				(distToHorizontalGridBeingHit < Float.MAX_VALUE ||
					distToVerticalGridBeingHit < Float.MAX_VALUE))
			{
				if (horizFirst)
				{
					if (distToHorizontalGridBeingHit < Float.MAX_VALUE &&
						horizBlockHitRecord.wall.visible &&
						depth < MAX_HIT_DEPTH)
					{
						populateBlockHitRecordGlobal(castColumn, horizBlockHitRecord, depth);
						depth++;
					}
				}
				else
				{
					if (distToVerticalGridBeingHit < Float.MAX_VALUE &&
						vertBlockHitRecord.wall.visible &&
						depth < MAX_HIT_DEPTH)
					{
						populateBlockHitRecordGlobal(castColumn, vertBlockHitRecord, depth);
						depth++;
					}
				}

				if (depth < MAX_HIT_DEPTH)
				{
					if (horizFirst)
					{
						distToHorizontalGridBeingHit = computeNextHorizBlockHit(
							castArc,
							horizRayState,
							horizBlockHitRecord,
							true);
					}
					else
					{
						distToVerticalGridBeingHit = computeNextVertBlockHit(
							castArc,
							vertRayState,
							vertBlockHitRecord,
							true);
					}

					horizFirst = distToHorizontalGridBeingHit < distToVerticalGridBeingHit;
				}
			}
		}
		catch (Exception e)
		{
			System.out.println("horizBlockHitRecord = [" + horizBlockHitRecord + "]");
			System.out.println("vertBlockHitRecord = [" + vertBlockHitRecord + "]");
			e.printStackTrace();
			System.exit(0);
		}
	}

	/*-------------------------------------------------------------------------*/
	private void populateBlockHitRecordGlobal(
		int castColumn,
		BlockHitRecord hit,
		int depth)
	{
		try
		{
			blockHitRecord[castColumn][depth].populateFrom(hit);

			// correct distance (compensate for the fishbowl effect)
			blockHitRecord[castColumn][depth].distance /= fishbowlTable[castColumn];

			blockHitRecord[castColumn][depth].projectedWallHeight = (int)(TILE_SIZE *
				(float)playerDistToProjectionPlane / blockHitRecord[castColumn][depth].distance);
		}
		catch (NullPointerException x)
		{
			x.printStackTrace();
			System.exit(1);
		}
	}

	/*-------------------------------------------------------------------------*/
	private float computeNextHorizBlockHit(
		int castArc,
		RayState rayState,
		BlockHitRecord result,
		boolean requireVisibleWall)
	{
		// horizontal or vertical coordinate of intersection
		int horizontalGrid = rayState.grid;

		// how far to the next bound (this is multiple of tile size)
		int distToNextHorizontalGrid = rayState.distToNextGrid;

		float distToHorizontalGridBeingHit = Float.MAX_VALUE;

		// x and y intersections
		float xIntersection = rayState.intersection;
		float distToNextXIntersection;

		// the current cell that the ray is in
		int xGridIndex=0;
		int yGridIndex=0;

		int horizontalTextureXRecord=0;

		int horizontalBlockHitRecord=0;

		int mapIndex=0;

		xGridIndex = playerX / TILE_SIZE;
		yGridIndex = playerY / TILE_SIZE;

		Wall horizontalWallHit=null;

		// Step through the grid looking for an intersection
		if (castArc == ANGLE0 || castArc == ANGLE180)
		{
			distToHorizontalGridBeingHit = Float.MAX_VALUE;
		}
		else
		{
			// Move the ray until it hits a horizontal wall

			distToNextXIntersection = xStepTable[castArc];
			boolean outOfBounds = false;
			while (true)
			{
				xGridIndex = (int)(xIntersection / TILE_SIZE);
				yGridIndex = (horizontalGrid / TILE_SIZE);

				if ((xGridIndex >= mapWidth) ||
					(yGridIndex >= mapLength) ||
					xGridIndex < 0 || yGridIndex < 0)
				{
					outOfBounds = true;
				}

				if (!outOfBounds)
				{
					mapIndex = getMapIndex(xGridIndex, yGridIndex);
				}

				if (castArc > ANGLE0 && castArc < ANGLE180)
				{
					// ray is tracing south, so only take north wall intersections
					int wallIndex;
					if (!outOfBounds)
					{
						wallIndex = map.getNorthWall(mapIndex);
					}
					else
					{
						wallIndex = map.getSouthWall(mapIndex);
					}
					horizontalWallHit = this.map.horizontalWalls[wallIndex];
					horizontalBlockHitRecord = this.map.getNorthBlock(wallIndex);
				}
				else
				{
					// ray is tracing north
					int wallIndex;
					if (!outOfBounds)
					{
						wallIndex = map.getSouthWall(mapIndex);
					}
					else
					{
						wallIndex = map.getNorthWall(mapIndex);
					}
					horizontalWallHit = this.map.horizontalWalls[wallIndex];
					horizontalBlockHitRecord = this.map.getSouthBlock(wallIndex);
				}

				if (horizontalWallHit.visible || !requireVisibleWall)
				{
					distToHorizontalGridBeingHit = (xIntersection - playerX) * iCosTable[castArc];
					horizontalTextureXRecord = (int)(xIntersection) % TILE_SIZE;

					result.distance = distToHorizontalGridBeingHit;
					result.textureXRecord = horizontalTextureXRecord;
					result.blockHit = horizontalBlockHitRecord;
					result.hitType = HIT_HORIZONTAL;
					result.texture = horizontalWallHit.texture;
					result.wall = horizontalWallHit;

					// extend to the next block in case the caller chooses to continue
					xIntersection += distToNextXIntersection;
					horizontalGrid += distToNextHorizontalGrid;
					rayState.grid = horizontalGrid;
					rayState.distToNextGrid = distToNextHorizontalGrid;
					rayState.intersection = xIntersection;

					break;
				}
				else if (outOfBounds)
				{
					break;
				}
				else
				{
					// The ray is not blocked, extend to the next block

					xIntersection += distToNextXIntersection;
					horizontalGrid += distToNextHorizontalGrid;
				}
			}
		}

		return distToHorizontalGridBeingHit;
	}

	/*-------------------------------------------------------------------------*/
	private float computeNextVertBlockHit(
		int castArc,
		RayState rayState,
		BlockHitRecord result,
		boolean requireVisibleWall)
	{
		// horizontal or vertical coordinate of intersection
		int verticalGrid = rayState.grid;

		// how far to the next bound (this is multiple of tile size)
		int distToNextVerticalGrid = rayState.distToNextGrid;

		float distToVerticalGridBeingHit = Float.MAX_VALUE;

		// x and y intersections
		float yIntersection = rayState.intersection;
		float distToNextYIntersection;

		// the current cell that the ray is in
		int xGridIndex=0;
		int yGridIndex=0;

		int verticalTextureXRecord=0;

		int verticalBlockHitRecord=0;

		int mapIndex=0;

		xGridIndex = playerX / TILE_SIZE;
		yGridIndex = playerY / TILE_SIZE;

		Wall verticalWallHit=null;

		// Step through the grid looking for an intersection
		if (castArc == ANGLE90 || castArc == ANGLE270)
		{
			distToVerticalGridBeingHit = Float.MAX_VALUE;
		}
		else
		{
			distToNextYIntersection = yStepTable[castArc];
			boolean outOfBounds = false;
			while (true)
			{
				xGridIndex = (verticalGrid / TILE_SIZE);
				yGridIndex = (int)(yIntersection / TILE_SIZE);

				if ((xGridIndex >= mapWidth) ||
					(yGridIndex >= mapLength) ||
					xGridIndex < 0 || yGridIndex < 0)
				{
					outOfBounds = true;
				}

				if (!outOfBounds)
				{
					mapIndex = getMapIndex(xGridIndex, yGridIndex);
				}

				if (castArc < ANGLE90 || castArc > ANGLE270)
				{
					// ray tracing east, only take west wall hits
					int wallIndex;
					if (!outOfBounds)
					{
						wallIndex = map.getWestWall(mapIndex);
					}
					else
					{
						wallIndex = map.getEastWall(mapIndex);
					}
					verticalWallHit = this.map.verticalWalls[wallIndex];
					verticalBlockHitRecord = map.getWestBlock(wallIndex);
				}
				else
				{
					int wallIndex;
					if (!outOfBounds)
					{
						wallIndex = map.getEastWall(mapIndex);
					}
					else
					{
						wallIndex = map.getWestWall(mapIndex);
					}
					verticalWallHit = this.map.verticalWalls[wallIndex];
					verticalBlockHitRecord = map.getEastBlock(wallIndex);
				}

				if (verticalWallHit.visible || !requireVisibleWall)
				{
					distToVerticalGridBeingHit = (yIntersection - playerY) * iSinTable[castArc];
					verticalTextureXRecord = (int)(yIntersection) % TILE_SIZE;

					result.distance = distToVerticalGridBeingHit;
					result.textureXRecord = verticalTextureXRecord;
					result.blockHit = verticalBlockHitRecord;
					result.hitType = HIT_VERTICAL;
					result.texture = verticalWallHit.texture;
					result.wall = verticalWallHit;

					// extend to the next block in case the caller chooses to continue
					yIntersection += distToNextYIntersection;
					verticalGrid += distToNextVerticalGrid;
					rayState.grid = verticalGrid;
					rayState.distToNextGrid = distToNextVerticalGrid;
					rayState.intersection = yIntersection;

					break;
				}
				else if (outOfBounds)
				{
					break;
				}
				else
				{
					yIntersection += distToNextYIntersection;
					verticalGrid += distToNextVerticalGrid;
				}
			}
		}

		return distToVerticalGridBeingHit;
	}

	/*-------------------------------------------------------------------------*/
	private int getMapIndex(int xGridIndex, int yGridIndex)
	{
		int mapIndex;
		mapIndex = yGridIndex * mapWidth + xGridIndex;
		return mapIndex;
	}
	
	/*-------------------------------------------------------------------------*/
	/**
	 * @param distance
	 * 	The distance at which to shade
	 * @return
	 * 	The shading multiplier: a number between 0 and 1, where 0 means no 
	 * 	shading and 1 means complete shading.
	 */ 
	static double calcShadeMult(
		double distance,
		int shadingDistance,
		int shadingThickness)
	{
		if (distance < shadingDistance)
		{
			return 0.0;
		}
		
		// the multiplier increases with distance
		double result = 1-shadingThickness/distance;
		if (result < 0)
		{
			result = 0;
		}
		else if (result > 1)
		{
			result = 1;
		}
		return result;
	}

	/*-------------------------------------------------------------------------*/
	private void drawWall(int castArc, int column, int depth, int[] outputBuffer)
	{
		int height;
		height = blockHitRecord[column][depth].projectedWallHeight;
		int blockHit = blockHitRecord[column][depth].blockHit;

		Texture texture = blockHitRecord[column][depth].texture;
		Texture maskTexture = blockHitRecord[column][depth].wall.maskTexture;
		int[] image = texture.imageData[texture.currentFrame];

		int lightLevel;
		if (this.doLighting)
		{
			lightLevel = this.map.tiles[blockHit].currentLightLevel;
		}
		else
		{
			lightLevel = NORMAL_LIGHT_LEVEL;
		}

		double shadeMult = 0.0;
		if (this.doShading)
		{
			// Shading: work out the effective light level for this wall slice
			shadeMult = calcShadeMult(blockHitRecord[column][depth].distance, shadingDistance, shadingThickness);
		}

		int top;
		int bottom;
		top = Math.max(playerHeight -(height/2) +projPlaneOffset, 0);
		bottom = Math.min(playerHeight +(height/2) +projPlaneOffset, projectionPlaneHeight);

		int textureX = blockHitRecord[column][depth].textureXRecord;

		// todo: fix image mapping so this condition doesn't happen
		if (textureX < 0)
		{
			textureX = 0;
		}

		int textureY = 0;

		int screenX = column;
		int screenY = top;

		// todo: can probably be optomised
		int diff = -(playerHeight -(height/2)) -projPlaneOffset;

		while (screenY < bottom)
		{
			if (diff <= 0)
			{
				textureY = ((screenY-top) * TILE_SIZE) / height;
			}
			else
			{
				textureY = ((screenY+diff) * TILE_SIZE) / height;
			}

			int textureIndex = textureX + textureY * TILE_SIZE;
			int bufferIndex = screenX + screenY * projectionPlaneWidth;
			int colour;
			if (maskTexture != null &&
				maskTexture.imageData[maskTexture.currentFrame][textureIndex] != 0)
			{
				// use the mask texture instead of the wall texture
				colour = alphaBlend(
					texture.imageData[texture.currentFrame][textureIndex],
					maskTexture.imageData[maskTexture.currentFrame][textureIndex]);

				if (blockHitRecord[column][depth].wall.maskTextureMouseClickScript != null)
				{
					// use the mask texture mouse click script instead
					this.mouseClickScriptRecords[bufferIndex] =
						blockHitRecord[column][depth].wall.maskTextureMouseClickScript;
				}
			}
			else
			{
				colour = image[textureIndex];
				this.mouseClickScriptRecords[bufferIndex] = blockHitRecord[column][depth].wall.mouseClickScript;
			}

			if (colour != 0)
			{
				int pixel = colourPixel(colour, lightLevel, shadeMult);
				if (depth > 0 && depth < MAX_HIT_DEPTH)
				{
					pixel = alphaBlend(outputBuffer[bufferIndex], pixel);
				}
				outputBuffer[bufferIndex] = pixel;
			}

			screenY++;
		}

		if (bottom < projectionPlaneHeight)
		{
			drawFloor(castArc, column, height, depth, outputBuffer);
		}
		if (top > 0)
		{
			drawCeiling(castArc, column, height, depth, outputBuffer);
		}
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Can Be Optomised
	 *  @param column
	 * 	the column being drawn
	 * @param wallHeight
	 * @param outputBuffer
	 */
	private void drawFloor(int castArc, int column, int wallHeight, int depth,
		int[] outputBuffer)
	{
		int top = playerHeight + (wallHeight/2) +projPlaneOffset;
		int bottom = projectionPlaneHeight;

		int screenY = top;
		int screenYInUnits=0;
		int heightOnProjectionPlane=0;
			// badly named for "row of playerHeight - row of screenY"
		double straightDistance=0;
		int beta=0;
		double actualDistance=0;
		double xDistance=0;
		double yDistance=0;
		int xIntersection=0;
		int yIntersection=0;
		int gridX=0;
		int gridY=0;
		int mapIndex=0;
		int textureX=0;
		int textureY=0;
		int colour;
		int shade;
		int lightLevel;
		Texture texture=null;
		Texture maskTexture=null;

		while (screenY < bottom)
		{
			try
			{
				heightOnProjectionPlane = screenY -playerHeight -projPlaneOffset;

				straightDistance = playerDistToProjectionPlane
					*playerHeightInUnits/(float)(heightOnProjectionPlane);

				beta = playerArc-castArc;

				if (beta < 0)
				{
					beta += ANGLE360;
				}
				else if (beta > ANGLE360)
				{
					beta -= ANGLE360;
				}

				actualDistance = straightDistance / cosTable[beta];

				// now we know that the ray intersects with the floor at an
				// angle of (castArc) and a distance of (actualDistance)

				xDistance = actualDistance*cosTable[castArc];
				yDistance = actualDistance*sinTable[castArc];

				xIntersection = (int)(playerX + xDistance);
				yIntersection = (int)(playerY + yDistance);

				//--- todo: these inaccuracies surely point to a bug in the maths somewhere?
				xIntersection = Math.min(xIntersection, mapWidth*TILE_SIZE-1);
				yIntersection = Math.min(yIntersection, mapLength*TILE_SIZE-1);
				//---
				xIntersection = Math.max(xIntersection, 0);
				yIntersection = Math.max(yIntersection, 0);
				//---

				gridX = xIntersection / TILE_SIZE;
				gridY = yIntersection / TILE_SIZE;
				mapIndex = gridX + gridY*mapWidth;

				textureX = Math.abs(xIntersection % TILE_SIZE);
				textureY = Math.abs(yIntersection % TILE_SIZE);

				texture = map.tiles[mapIndex].floorTexture;
				maskTexture = map.tiles[mapIndex].floorMaskTexture;
				if (this.doLighting)
				{
					lightLevel = map.tiles[mapIndex].currentLightLevel;
				}
				else
				{
					lightLevel = NORMAL_LIGHT_LEVEL;
				}

				double shadeMult = 0.0;
				if (this.doShading)
				{
					// Shading: work out the effective light level for this wall slice
					shadeMult = calcShadeMult(actualDistance, shadingDistance, shadingThickness);
				}

				// draw the floor:
				int textureIndex = textureX + textureY * TILE_SIZE;
				if (maskTexture != null &&
					maskTexture.imageData[maskTexture.currentFrame][textureIndex] != 0)
				{
					colour = alphaBlend(
						texture.imageData[texture.currentFrame][textureIndex],
						maskTexture.imageData[texture.currentFrame][textureIndex]);
				}
				else
				{
					colour = texture.imageData[texture.currentFrame][textureIndex];
				}
				shade = colourPixel(colour, lightLevel, shadeMult);
				int bufferIndex = column + screenY * projectionPlaneWidth;
				outputBuffer[bufferIndex] = shade;
				// mouse click scripts associated with floors and ceilings not yet supported
				this.mouseClickScriptRecords[bufferIndex] = null;

				screenY++;
			}
			catch (RuntimeException e)
			{
				log("playerArc = [" + playerArc + "]");
				log("castArc = [" + castArc + "]");
				log("wallHeight = [" + wallHeight + "]");
				log("column = [" + column + "]");
				log("playerHeight = [" + this.playerHeight + "]");
				log("playerHeightInUnits = [" + playerHeightInUnits + "]");
				log("playerDistanceToTheProjectionPlane = [" + playerDistToProjectionPlane + "]");
				log("PROJECTION_PLANE_HEIGHT = [" + projectionPlaneHeight + "]");
				log("screenY = [" + screenY + "]");
				log("screenYInUnits = [" + screenYInUnits + "]");
				log("heightOnProjectionPlane = [" + heightOnProjectionPlane + "]");
				log("straightDistance = [" + straightDistance + "]");
				log("beta = [" + beta + "]");
				log("actualDistance = [" + actualDistance + "]");
				log("[xDistance, yDistance] = [" + xDistance + ","+ yDistance +"]");
				log("[playerX, playerY] = [" + playerX + ","+ playerY +"]");
				log("[gridX, gridY] = [" + gridX + ","+ gridY +"]");
				log("[textureX, textureY] = [" + textureX + ","+ textureY +"]");
				log("mapIndex = [" + mapIndex + "]");
				log("blockHitRecord[column][0] = " + blockHitRecord[column][0]);
				log("projectionPlaneHeight = [" + projectionPlaneHeight + "]");
				log("projectionPlaneWidth = [" + projectionPlaneWidth + "]");

				throw e;
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Can Be Optomised
	 *  @param column
	 * 	the column being drawn
	 * @param wallHeight
	 * @param outputBuffer
	 */
	private void drawCeiling(int castArc, int column, int wallHeight, int depth,
		int[] outputBuffer)
	{
		int top = 0;//halfProjectionPlaneHeight + (wallHeight/2);
		int bottom = playerHeight - (wallHeight/2) +projPlaneOffset;

		int screenY = top;
		int screenYInUnits=0;
		int heightOnProjectionPlane=0;
			// badly named for "row of center - row of PJ"
		double straightDistance=0;
		int beta=0;
		double actualDistance=0;
		double xDistance=0;
		double yDistance=0;
		int xIntersection=0;
		int yIntersection=0;
		int gridX=0;
		int gridY=0;
		int mapIndex=0;
		int textureX=0;
		int textureY=0;
		int[] image;
		int colour;
		int shade;
		int lightLevel;
		Texture texture=null;
		Texture maskTexture=null;

		while (screenY < bottom)
		{
			try
			{
				heightOnProjectionPlane = (projectionPlaneHeight-this.playerHeight) -screenY +projPlaneOffset;

				straightDistance = playerDistToProjectionPlane
					*playerHeightInUnits/(float)(heightOnProjectionPlane);

				beta = playerArc-castArc;

				if (beta < 0)
				{
					beta += ANGLE360;
				}
				else if (beta > ANGLE360)
				{
					beta -= ANGLE360;
				}

				actualDistance = straightDistance / cosTable[beta];

				// now we know that the ray intersects with the floor at an
				// angle of (castArc) and a distance of (actualDistance)

				xDistance = actualDistance*cosTable[castArc];
				yDistance = actualDistance*sinTable[castArc];

				xIntersection = (int)(playerX + xDistance);
				yIntersection = (int)(playerY + yDistance);

				//--- todo: these inaccuracies surely point to a bug in the maths somewhere?
				xIntersection = Math.min(xIntersection, mapWidth*TILE_SIZE-1);
				yIntersection = Math.min(yIntersection, mapLength*TILE_SIZE-1);
				//---
				xIntersection = Math.max(xIntersection, 0);
				yIntersection = Math.max(yIntersection, 0);
				//---

				gridX = xIntersection / TILE_SIZE;
				gridY = yIntersection / TILE_SIZE;
				mapIndex = gridX + gridY*mapWidth;

				textureX = Math.abs(xIntersection % TILE_SIZE);
				textureY = Math.abs(yIntersection % TILE_SIZE);

				texture = map.tiles[mapIndex].floorTexture;
				maskTexture = map.tiles[mapIndex].ceilingMaskTexture;
				if (this.doLighting)
				{
					lightLevel = map.tiles[mapIndex].currentLightLevel;
				}
				else
				{
					lightLevel = NORMAL_LIGHT_LEVEL;
				}

				double shadeMult = 0.0;
				if (this.doShading)
				{
					shadeMult = calcShadeMult(actualDistance, shadingDistance, shadingThickness);
				}

				// draw the ceiling:
				texture = map.tiles[mapIndex].ceilingTexture;
				int textureIndex = textureX + textureY * TILE_SIZE;
				if (maskTexture != null &&
					maskTexture.imageData[maskTexture.currentFrame][textureIndex] != 0)
				{
					colour = alphaBlend(
						texture.imageData[texture.currentFrame][textureIndex],
						maskTexture.imageData[maskTexture.currentFrame][textureIndex]);
				}
				else
				{
					colour = texture.imageData[texture.currentFrame][textureIndex];
				}

				int bufferIndex = column + screenY * projectionPlaneWidth;
				if (colour != 0)
				{
					shade = colourPixel(colour, lightLevel, shadeMult);
					outputBuffer[bufferIndex] = shade;
				}
				else
				{
					// transparent pixel.
					image = this.skyImage[map.currentSkyImage];

					// tile the sky texture horizontally
					textureX = castArc % skyTextureWidth;
					textureY = screenY*skyTextureHeight/playerHeight;
					outputBuffer[bufferIndex] =
						image[textureX + textureY * skyTextureWidth];
				}
				// mouse click scripts associated with floors and ceilings not yet supported
				this.mouseClickScriptRecords[bufferIndex] = null;

				screenY++;
			}
			catch (RuntimeException e)
			{
				log("playerArc = [" + playerArc + "]");
				log("castArc = [" + castArc + "]");
				log("wallHeight = [" + wallHeight + "]");
				log("column = [" + column + "]");
				log("playerHeight = [" + this.playerHeight + "]");
				log("playerHeightInUnits = [" + playerHeightInUnits + "]");
				log("playerDistanceToTheProjectionPlane = [" + playerDistToProjectionPlane + "]");
				log("PROJECTION_PLANE_HEIGHT = [" + projectionPlaneHeight + "]");
				log("screenY = [" + screenY + "]");
				log("screenYInUnits = [" + screenYInUnits + "]");
				log("heightOnProjectionPlane = [" + heightOnProjectionPlane + "]");
				log("straightDistance = [" + straightDistance + "]");
				log("beta = [" + beta + "]");
				log("actualDistance = [" + actualDistance + "]");
				log("[xDistance, yDistance] = [" + xDistance + ","+ yDistance +"]");
				log("[playerX, playerY] = [" + playerX + ","+ playerY +"]");
				log("[gridX, gridY] = [" + gridX + ","+ gridY +"]");
				log("[textureX, textureY] = [" + textureX + ","+ textureY +"]");
				log("mapIndex = [" + mapIndex + "]");
				log("blockHitRecord[column][0] = " + blockHitRecord[column][0]);

				throw e;
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * First calculates the distance to each object from the player.
	 * <p>
	 * Then performs an insertion sort on the array of objects, to arrange them in
	 * descending order of distance.  Insertion sort is used because it's best
	 * case is O(n) for data that is already sorted.  The object distances won't
	 * often change between frames. The array is sorted in-place.
	 */
	private void initAndSortObjects()
	{
		synchronized(objectMutex)
		{
			// calculate each objects distance from the player
			double objectAngle=0;
			int center=0;
			EngineObject obj=null;

			for (int i=0; i<objects.length; i++)
			{
				obj = this.objects[i];

				double xDiff = playerX - obj.xPos;
				double yDiff = playerY - obj.yPos;

				obj.distance = Math.sqrt(xDiff*xDiff + yDiff*yDiff);

				if (playerX == obj.xPos)
				{
					// in this case tan^-1 is infinity, so:
					if (obj.yPos > playerY)
					{
						// object is south of player
						objectAngle = ANGLE90;
					}
					else
					{
						// object is north of player
						objectAngle = ANGLE270;
					}
				}
				else if (playerY == obj.yPos)
				{
					// in this case tan^-1 is 0, so:
					if (obj.xPos > playerX)
					{
						// object is west of player
						objectAngle = ANGLE0;
					}
					else
					{
						// object is east of player
						objectAngle = ANGLE180;
					}
				}
				else
				{
					objectAngle = radToArc(Math.atan(Math.abs(yDiff/xDiff)));

					// transform the objectAngle to be in the first quadrant

					if (obj.xPos < playerX)
					{
						if (obj.yPos > playerY)
						{
							objectAngle = ANGLE180-objectAngle;
						}
						else
						{
							objectAngle = ANGLE180+objectAngle;
						}
					}
					else
					{
						if (obj.yPos < playerY)
						{
							objectAngle = ANGLE360-objectAngle;
						}
						// else no need to transform
					}

				}

				center = (int)(objectAngle - (playerArc - PLAYER_FOV_HALF));

				if (center < 0)
				{
					center += ANGLE360;
				}
				else if (center > ANGLE360)
				{
					center -= ANGLE360;
				}

				obj.center = center;

				obj.prepareForRender(
					projectionPlaneWidth,
					fishbowlTable,
					TILE_SIZE,
					playerDistToProjectionPlane,
					map,
					doLighting,
					doShading,
					shadingDistance,
					shadingThickness,
					movementMode,
					getPlayerFacing());
			}

			int numSorted = 1;
			int index;

			while(numSorted < objects.length)
			{
				EngineObject temp = this.objects[numSorted];

				// insert amongst the sorted values
				for (index = numSorted; index>0; index--)
				{
					if (temp.distance > objects[index-1].distance)
					{
						objects[index] = objects[index-1];
					}
					else
					{
						// this is where we must insert
						break;
					}
				}

				objects[index] = temp;
				numSorted++;
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	private void drawObject(
		EngineObject obj,
		int depth,
		int[] outputBuffer)
	{
		int castColumn=0;

		try
		{
			if (obj.projectedObjectHeight > 0 && obj.endScreenX > 0)
			{
				castColumn = obj.startScreenX;

				if (castColumn < 0)
				{
					castColumn = 0;
				}

				while(castColumn < obj.endScreenX)
				{
					drawObjectColumn(obj, outputBuffer, castColumn, depth);

					castColumn++;
				}
			}
		}
		catch (RuntimeException e)
		{
			log("obj.center = [" + obj.center + "]");
			log("projectedObjectHeight = [" + obj.projectedObjectHeight + "]");
			log("currentScreenX = [" + castColumn + "]");

			throw e;
		}
	}

	/*-------------------------------------------------------------------------*/
	private void drawObjectColumn(
		EngineObject obj,
		int[] outputBuffer,
		int castColumn,
		int depth)
	{
		if (blockHitRecord[castColumn][depth].distance > obj.apparentDistance)
		{
			int startScreenY = playerHeight - obj.projectedObjectHeight/2 +projPlaneOffset;
			int currentScreenY = startScreenY;
			int endScreenY = startScreenY + obj.projectedObjectHeight;

			if (startScreenY < 0)
			{
				currentScreenY = 0;
			}

			if (endScreenY < 0)
			{
				return;
			}

			if (endScreenY > projectionPlaneHeight)
			{
				endScreenY = projectionPlaneHeight;
			}

			while(currentScreenY < endScreenY)
			{
				int[] image = obj.renderTexture.imageData[obj.currentTextureFrame];
				int textureX = TILE_SIZE*(castColumn-obj.startScreenX)/obj.projectedObjectHeight;
				int textureY = TILE_SIZE*(currentScreenY-startScreenY)/obj.projectedObjectHeight;

				int colour = image[textureX + TILE_SIZE*textureY];

				if (colour != 0)
				{
					// 0 is a transparent pixel

					int bufferIndex = castColumn + projectionPlaneWidth * currentScreenY;
					colour = alphaBlend(outputBuffer[bufferIndex], colour);

					int shade = colourPixel(colour, obj.adjustedLightLevel, obj.shadeMult);
					outputBuffer[bufferIndex] = shade;
					this.mouseClickScriptRecords[bufferIndex] = obj.mouseClickScript;
				}

				currentScreenY++;
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	private int alphaBlend(int background, int mask)
	{
		int alpha = (mask>>24) &0xFF;

		if (alpha == 0xFF)
		{
			return mask;
		}
		else if (alpha == 0)
		{
			return background;
		}

		int redMask =   (mask>>16) & 0xFF;
		int greenMask = (mask>>8) & 0xFF;
		int blueMask =  mask & 0xFF;

		int backAlpha = (background>>24) & 0xFF;
		int redBack =   (background>>16) & 0xFF;
		int greenBack = (background>>8) & 0xFF;
		int blueBack =  background & 0xFF;

		int alphaDiff = 0xFF - alpha;
		// the >>8 represents "/256",
		// used because it's faster than "/255" (although a little less accurate)
		int red = (((redBack*alphaDiff)/255) + ((redMask*alpha)/255)) &0xFF;
		int green = ((((greenBack*alphaDiff)/255) + ((greenMask*alpha)/255))) &0xFF;
		int blue = ((((blueBack*alphaDiff)/255) + ((blueMask*alpha)/255))) &0xFF;

		return (backAlpha<<24) | ((red<<16) | (green<<8) | blue);
	}

	/*-------------------------------------------------------------------------*/
	private int colourPixel(int colour, int lightLevel, double shadingMult)
	{
		if (lightLevel > MAX_LIGHT_LEVEL)
		{
			throw new CrusaderException("Invalid light level "+lightLevel);
		}

		int result;

		int alpha = (colour>>24) & 0xFF;
		int red =   (colour>>16) & 0xFF;
		int green = (colour>>8) & 0xFF;
		int blue =  colour & 0xFF;

		// The >> 5 represents "/ NORMAL_LIGHT_LEVEL"
		red = (red * lightLevel >> 5);
		green = (green * lightLevel >> 5);
		blue = (blue * lightLevel >> 5);

		if (red > 0xFF) red = 0xFF;
		if (green > 0xFF) green = 0xFF;
		if (blue > 0xFF) blue = 0xFF;

		red = shade(red, shadeRed, shadingMult);
		green = shade(green, shadeGreen, shadingMult);
		blue = shade(blue, shadeBlue, shadingMult);

		result = (alpha<<24) | (red<<16) | (green<<8) | blue;

		return result;
	}

	/*-------------------------------------------------------------------------*/
	private int shade(int col, int shadeTo, double shadingMult)
	{
		if (col > shadeTo)
		{
			col = col - (int)((col-shadeTo)*shadingMult);
		}
		else
		{
			col = col + (int)((shadeTo-col)*shadingMult);
		}
		if (col > 0xFF)
		{
			col = 0xFF;
		}
		else if (col < 0)
		{
			col = 0;
		}
		return col;
	}

	/*-------------------------------------------------------------------------*/
	public EngineObject removeObject(EngineObject obj)
	{
		synchronized(objectMutex)
		{
			int index = -1;
			for (int i = 0; i < objects.length; i++)
			{
				if (objects[i] == obj)
				{
					index = i;
					break;
				}
			}
			
			if (index == -1)
			{
				// object not in array
				return null;
			}

			removeObjectAt(index);
			return obj;
		}
	}

	/*-------------------------------------------------------------------------*/
	public List<EngineObject> removeObject(String objectName)
	{
		if (objectName == null)
		{
			return null;
		}

		synchronized(objectMutex)
		{
			List<EngineObject> result = new ArrayList<>();

			for (int i = 0; i < objects.length; i++)
			{
				if (objectName.equals(objects[i].getName()))
				{
					result.add(objects[i]);
					removeObjectAt(i);
					i--;
				}
			}

			return result;
		}
	}

	/*-------------------------------------------------------------------------*/
	private void removeObjectAt(int index)
	{
		EngineObject[] new_array = new EngineObject[objects.length-1];
		System.arraycopy(objects, 0, new_array, 0, index);
		System.arraycopy(objects, index+1, new_array, index, objects.length-index-1);
		this.objects = new_array;
	}

	/*-------------------------------------------------------------------------*/
	public void addObject(EngineObject obj)
	{
		synchronized(objectMutex)
		{
			addObject(obj, true);
		}
	}

	/*-------------------------------------------------------------------------*/
	public void addObject(EngineObject obj, boolean init)
	{
		if (init)
		{
			this.map.initObject(obj, EngineObject.Placement.CENTER);
		}

		EngineObject[] new_array = new EngineObject[objects.length+1];
		System.arraycopy(objects, 0, new_array, 0, objects.length);
		new_array[objects.length] = obj;
		this.objects = new_array;

		for (Texture texture : obj.textures)
		{
			this.addTexture(texture);
		}
	}

	/*-------------------------------------------------------------------------*/
	public void addObjectInFrontOfPlayer(
		EngineObject obj, 
		double distance, 
		double arcOffset,
		boolean randomStartingFrame)
	{
		synchronized(objectMutex)
		{
			int dist = (int)(TILE_SIZE*distance);
			int arc = (int)(playerArc - PLAYER_FOV_HALF + arcOffset*PLAYER_FOV);
			if (arc < 0)
			{
				arc += ANGLE360;
			}

			obj.currentTextureFrame =  r.nextInt(obj.textures[obj.northTexture].nrFrames);

			int x = (int)(dist * cosTable[arc]);
			int y = (int)(dist * sinTable[arc]);
			
			obj.xPos = playerX + x;
			obj.yPos = playerY + y;
			
			addObject(obj, false);
		}
	}

	/*-------------------------------------------------------------------------*/
	public void addScript(MapScript script)
	{
		this.map.addScript(script);
	}
	
	/*-------------------------------------------------------------------------*/
	public MapScript removeScript(MapScript script)
	{
		return this.map.removeScript(script);
	}

	/*-------------------------------------------------------------------------*/
	private void animation()
	{
		long now = System.currentTimeMillis();

		for (Texture texture1 : textures)
		{
			if (now - texture1.lastChanged >= texture1.animationDelay)
			{
				texture1.currentFrame++;
				if (texture1.currentFrame >= texture1.nrFrames)
				{
					texture1.currentFrame = 0;
				}
				texture1.lastChanged = now;
			}
		}

		for (EngineObject object : objects)
		{
			//todo: multi-sided objects?
			Texture texture;
			if (object.currentTexture != -1)
			{
				texture = object.textures[object.currentTexture];
			}
			else
			{
				texture = object.textures[object.northTexture];
			}

			if (now - object.textureLastChanged >= texture.animationDelay)
			{
				object.currentTextureFrame++;
				if (object.currentTextureFrame >= texture.nrFrames)
				{
					object.currentTextureFrame = 0;
				}
				object.textureLastChanged = now;
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	public ColorModel getColourModel()
	{
		return this.colourModel;
	}

	/*-------------------------------------------------------------------------*/
	private void log(String s)
	{
		System.out.println(s);
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Converts from degrees (expressed in cast columns) to radians.
	 * @param arcAngle
	 * 	an angle expressed in cast columns
	 * @return
	 * 	that angle in radians
	 */ 
	private float arcToRad(float arcAngle)
	{
		return (float)(arcAngle * Math.PI) / (float)ANGLE180;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Convert from radians to degrees (expressed in cast columns)
	 * @param radianAngle
	 * 	an angle expressed in radians
	 * @return
	 * 	that angle in cast columns
	 */ 
	private double radToArc(double radianAngle)
	{
		return radianAngle / Math.PI * ANGLE180;
	}

	/*-------------------------------------------------------------------------*/
	private static class RayState
	{
		int grid;
		float intersection;
		int distToNextGrid;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Used to record the details of where a ray strikes a block.
	 */
	private static class BlockHitRecord
	{
		int blockHit;
		Texture texture;
		int textureXRecord, textureYRecord;
		float distance;
		int projectedWallHeight;
		Wall wall;

		/**
		 * Either {@link CrusaderEngine32#HIT_VERTICAL} or {@link CrusaderEngine32#HIT_HORIZONTAL}
		 */
		byte hitType;

		/**
		 * Clones the given BHR
		 */
		public BlockHitRecord(BlockHitRecord other)
		{
			this.blockHit = other.blockHit;
			this.texture = other.texture;
			this.textureXRecord = other.textureXRecord;
			this.textureYRecord = other.textureYRecord;
			this.distance = other.distance;
			this.wall = other.wall;
			this.hitType = other.hitType;
		}

		public BlockHitRecord()
		{
		}

		public String toString()
		{
			return "blockHit=["+blockHit+"], " +
				"wall=["+wall+"]" +
				"texture=["+texture+"], " +
				"distance=["+distance+"], " +
				"projectedWallHeight=["+projectedWallHeight+"], " +
				"hitType=["+hitType+"]"; 
		}

		public void populateFrom(BlockHitRecord other)
		{
			this.blockHit = other.blockHit;
			this.texture = other.texture;
			this.textureXRecord = other.textureXRecord;
			this.textureYRecord = other.textureYRecord;
			this.distance = other.distance;
			this.wall = other.wall;
			this.hitType = other.hitType;
		}
	}
}
