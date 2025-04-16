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

import java.awt.*;
import java.awt.image.ColorModel;
import java.awt.image.MemoryImageSource;
import java.awt.image.PixelGrabber;
import java.util.List;
import java.util.*;
import java.util.concurrent.*;
import mclachlan.crusader.Map.SkyConfig;
import mclachlan.crusader.postprocessor.*;
import mclachlan.crusader.script.MoveTo;
import mclachlan.maze.util.MazeException;

/**
 * Implementation with 32-bit colour
 */ 
public class CrusaderEngine32 implements CrusaderEngine
{
	/**
	 * Container for tiles, textures and map information.
	 */
	private final Map map;

	/**
	 * Helper class for colour-management.  
	 */ 
	private final ColorModel colourModel = new CrusaderColourModel();
	
	/**
	 * Textures in use in this map.
	 */
	private Texture[] textures;

	/** The color to shade with */
	private final int shadeRed;
	private final int shadeGreen;
	private final int shadeBlue;
	
	/** The distance (in units) at which shading starts */
	private final int shadingDistance;
	/** The distance (in units) for the shading effect to double */
	private final int shadingThickness;

	/**
	 * Objects in the map.  The engine sorts them in descending 
	 * order of distance from the player
	 */ 
	private EngineObject[] objects;
	/** used to protect the arrays when things are dynamically added and removed */
	private final Object objectMutex = new Object();

	// functionality flags
	private final boolean doShading, doLighting;

	/** pipeline of post processors (eg for anti-aliasing)*/
	private PostProcessor[] filters;
	private final Object filterMutex = new Object();
	
	/** The pixels that the engine returns to the outside world. */
	private final int[] renderBuffer;
	/** A back-buffer for the post-processors to work with */
	private final int[] postProcessingBuffer;
	
	/** The width of the map (ie east-west), in grid cells */
	private final int mapWidth;
	/** The length of the map (ie north-south), in grid cells */
	private final int mapLength;

	/** The length of the edge of a tile, in units */
	public int tileSize;
	
	/** width of the projection plane, in cast columns (ie pixels) */  
	private final int projectionPlaneWidth;
	/** height of the projection plane, in cast columns (ie pixels) */
	private final int projectionPlaneHeight;

	// Note the difference between measurement units (eg tileSize) and
	// pixels (eg PROJECTION_PLANE_HEIGHT)
	private final int playerHeightInUnits;

	/** A constant from {@link CrusaderEngine.FieldOfView} */
	private final int playerFovOption;
	/** various degrees of arc, expressed in cast columns (ie pixels)*/
	private static int
		ANGLE360,
		ANGLE315,
		ANGLE270,
		ANGLE225,
		ANGLE180,
		ANGLE135,
		ANGLE90,
		ANGLE60,
		ANGLE45,
		ANGLE30,
		ANGLE5,
		ANGLE0;

	private int playerFov;
	private int playerFovHalf;

	/** Used to represent a ray hit on a vertical (ie north-south) wall*/ 
	private static final byte HIT_VERTICAL = 1;
	/** Used to represent a ray hit on a vertical (ie east-west) wall*/
	private static final byte HIT_HORIZONTAL = 2;

	// Pre-calc trig tables, indexed on angle expressed in cast columns (ie pixels)
	private float[] sinTable;
	private float[] iSinTable;
	private float[] cosTable;
	private float[] iCosTable;
	private float[] tanTable;
	private float[] iTanTable;
	private float[] fishbowlTable;
	
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
	/** Scales the player distance to the projection plane */
	private double playerDistanceMult = 1.0;
	/** Translates the projection plane (in pixels downwards) */
	private final int projPlaneOffset;
	/** How far the player moves for each forward movement, in units */
	private int playerSpeed;
	/** How far the player rotates, an angle expressed in cast columns (ie pixels)*/
	private int playerRotation;
	/** How high the player is */
	private final int playerHeight;

	/** A record of grid block hits, indexed on cast column and then depth */
	private final BlockHitRecord[][] blockHitRecord;
	private int maxHitDepth = 20;
	
	/** A record of applicable mouse click scripts, by index in the render buffer */
	private final MouseClickScriptRecord[] mouseClickScriptRecords;
	
	/** Incremented each frame, used for animation */
	private long frameCount;

	/** 
	 * The of mode player movement. A constant from 
	 * {@link mclachlan.crusader.CrusaderEngine.MovementMode}.
	 */
	private int movementMode;
	
	// AWT fields
	private final MemoryImageSource pictureArray;
	private final Image displayImage;

	// multi threading support
	private final ExecutorCompletionService<Object> executor;
	private final DrawColumn[] columnRenderers;
	private final FilterColumn[] columnFilterers;

	// admin
	private final static Random r = new Random();
	private long timeNow;

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
	 * @param filters
	 * 	Any post processing filters (eg anti aliasing), null if none.
	 * @param projectionPlaneOffset
	 * 	The vertical offset of the projection plane from player eye level.  A
	 * 	negative number shifts it towards the floor, a positive one shifts it
	 * 	towards the ceiling.  Expressed in pixels.
	 * @param playerFieldOfView
	 * 	A constant from {@link CrusaderEngine.FieldOfView}.
	 * @param scaleDistFromProjPlane
	 * 	Scales the player distance from the projection plane.  For example, set
	 * 	it to 0.5 for half the usual distance, or 2.0 for double.
	 * @param maxHitDepth
	 * 	The max number of wall hits to trace a ray through. 0 or less defaults to 5.
	 * @param nrThreads
	 * 	The number of rendering threads.
	 */
	public CrusaderEngine32(
		Map map,
		int screenWidth,
		int screenHeight,
		int movementMode,
		Color shadeTargetColour,
		boolean doShading,
		boolean doLighting,
		double shadingDistance,
		double shadingMultiplier,
		Filter[] filters,
		int projectionPlaneOffset,
		int playerFieldOfView,
		double scaleDistFromProjPlane,
		int maxHitDepth,
		int nrThreads,
		Component component)
	{
		this.map = map;

		this.textures = new Texture[0];
		this.initImages();

		tileSize = map.baseImageSize;
		
		this.shadingDistance = (int)(tileSize * shadingDistance);
		this.shadingThickness = (int)(tileSize * shadingMultiplier);
		this.shadeRed = shadeTargetColour.getRed();
		this.shadeGreen = shadeTargetColour.getGreen();
		this.shadeBlue = shadeTargetColour.getBlue();
		
		this.projectionPlaneWidth = screenWidth;
		this.projectionPlaneHeight = screenHeight;
		this.playerHeight = (int)(projectionPlaneHeight/2);
		this.playerHeightInUnits = (int)(tileSize /2);
		this.projPlaneOffset = projectionPlaneOffset;
		this.playerFovOption = playerFieldOfView;
		this.playerDistanceMult = scaleDistFromProjPlane;

		this.maxHitDepth = (maxHitDepth <= 0) ? 5 : maxHitDepth;

		this.createTables();

		this.setMovementMode(movementMode);

		this.doLighting = doLighting;
		this.doShading = doShading;

		buildFilters(filters);

		this.renderBuffer = new int[screenWidth * screenHeight];
		this.postProcessingBuffer = new int[screenWidth * screenHeight];
		this.mouseClickScriptRecords = new MouseClickScriptRecord[screenWidth * screenHeight];
		for (int i = 0; i < mouseClickScriptRecords.length; i++)
		{
			mouseClickScriptRecords[i] = new MouseClickScriptRecord();
		}

		this.blockHitRecord = new BlockHitRecord[screenWidth][this.maxHitDepth];
		for (int i = 0; i < blockHitRecord.length; i++)
		{
			for (int j = 0; j < this.maxHitDepth; j++)
			{
				blockHitRecord[i][j] = new BlockHitRecord();
			}
		}
		
		mapWidth = map.width;
		mapLength = map.length;

		this.objects = (EngineObject[])map.renderObjects.clone();
		
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

		executor = new ExecutorCompletionService<>(Executors.newFixedThreadPool(nrThreads));
		columnRenderers = new DrawColumn[projectionPlaneWidth];
		columnFilterers = new FilterColumn[projectionPlaneWidth];

		for (int screenX = 0; screenX < projectionPlaneWidth; screenX++)
		{
			 columnRenderers[screenX] = new DrawColumn(0, screenX);
			 columnFilterers[screenX] = new FilterColumn(screenX, postProcessingBuffer);
		}
	}

	/*-------------------------------------------------------------------------*/
	private void buildFilters(Filter[] filters)
	{
		if (filters != null)
		{
			List<PostProcessor> processors = new ArrayList<>();

			for (int i = 0; i < filters.length; i++)
			{
				Filter filter = filters[i];
				switch (filter)
				{
					case NONE:
						this.filters = null;
						break;
					case SMOOTH:
						processors.add(new BoxFilter(new float[]{1, 1, 1, 1, 2, 1, 1, 1, 1},
							projectionPlaneWidth, projectionPlaneHeight, 0));
						break;
					case SHARPEN:
						processors.add(new BoxFilter(new float[]{-1, -1, -1, -1, 9, -1, -1, -1, -1},
							projectionPlaneWidth, projectionPlaneHeight, 0));
						break;
					case RAISED:
						processors.add(new BoxFilter(new float[]{0, 0, -2, 0, 2, 0, 1, 0, 0},
							projectionPlaneWidth, projectionPlaneHeight, 0));
						break;
					case MOTION_BLUR:
						processors.add(new BoxFilter(new float[]{0, 0, 1, 0, 0, 0, 1, 0, 0},
							projectionPlaneWidth, projectionPlaneHeight, 0));
						break;
					case EDGE_DETECT:
						processors.add(new BoxFilter(new float[]{0, 1, 0, 1, -4, 1, 0, 1, 0},
							projectionPlaneWidth, projectionPlaneHeight, 0));
						break;
					case EMBOSS:
						processors.add(new BoxFilter(new float[]{-4, -2, 0, -2, 1, 2, 0, 2, 4},
							projectionPlaneWidth, projectionPlaneHeight, 60));
						processors.add(new GreyscaleFilter(projectionPlaneWidth, projectionPlaneHeight));
						break;
					case WIREFRAME:
						processors.add(new BoxFilter(new float[]{0, 1, 0, 1, -4, 1, 0, 1, 0},
							projectionPlaneWidth, projectionPlaneHeight, 0));
						processors.add(new WireframeFilter(projectionPlaneWidth, projectionPlaneHeight));
						break;
					case GREYSCALE:
						processors.add(new GreyscaleFilter(projectionPlaneWidth, projectionPlaneHeight));
						break;
					case GHOST:
						processors.add(new GhostlyPostProcessor(projectionPlaneWidth, projectionPlaneHeight));
						break;
					case HEX:
						processors.add(new HexagonalPostProcessor(projectionPlaneWidth, projectionPlaneHeight, 3));
						break;
					case RIPPLE:
						processors.add(new RipplePostProcessor(
							projectionPlaneWidth, projectionPlaneHeight,
							projectionPlaneWidth/2, projectionPlaneHeight/2,
							.2, 5));
					case DEFAULT:
					case FXAA:
						processors.add(new FXAAFilter(projectionPlaneWidth, projectionPlaneHeight));
						break;
					default:
						throw new CrusaderException("Invalid: " + filter);
				}
			}

			this.filters = processors.toArray(new PostProcessor[0]);
		}
		else
		{
			this.filters = null;
		}
	}

	/*-------------------------------------------------------------------------*/
	private void initMouseClickScripts()
	{
		for (Wall horizontalWall : map.horizontalWalls)
		{
			if (horizontalWall.internalScript != null)
			{
				horizontalWall.internalScript.initialise(map);
			}
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
			if (verticalWall.internalScript != null)
			{
				verticalWall.internalScript.initialise(map);
			}
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

		switch (this.movementMode)
		{
			case MovementMode.DISCRETE ->
			{
				this.playerSpeed = tileSize;
				this.playerRotation = ANGLE90;
			}
			case MovementMode.CONTINUOUS ->
			{
				this.playerSpeed = tileSize / 8;
				this.playerRotation = ANGLE5;
			}
			case MovementMode.OCTO ->
			{
				this.playerSpeed = tileSize;
				this.playerRotation = ANGLE90 / 2;
			}
			default ->
				throw new CrusaderException("Unrecognised mode [" + movementMode + "]");
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

			ANGLE45 = ANGLE90 / 2;
			ANGLE135 = ANGLE90 + ANGLE45;
			ANGLE225 = ANGLE180 + ANGLE45;
			ANGLE315 = ANGLE270 + ANGLE45;

			playerFov = ANGLE30;
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

			ANGLE45 = ANGLE90 / 2;
			ANGLE135 = ANGLE90 + ANGLE45;
			ANGLE225 = ANGLE180 + ANGLE45;
			ANGLE315 = ANGLE270 + ANGLE45;

			playerFov = ANGLE60;
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

			ANGLE45 = ANGLE90 / 2;
			ANGLE135 = ANGLE90 + ANGLE45;
			ANGLE225 = ANGLE180 + ANGLE45;
			ANGLE315 = ANGLE270 + ANGLE45;

			playerFov = ANGLE90;
		}
		else if (playerFovOption == FieldOfView.FOV_180_DEGREES)
		{
			// fake it out a bit
			int angle170 = projectionPlaneWidth;

			ANGLE180 = angle170*18/17;
			ANGLE90 = ANGLE180/2;
			ANGLE30 = (ANGLE180 / 6);
			ANGLE60 = ANGLE180/3;
			ANGLE270 = (ANGLE90 * 3);
			ANGLE360 = (ANGLE180 * 2);
			ANGLE0 = 0;
			ANGLE5 = (ANGLE180 / 36);

			ANGLE45 = ANGLE90 / 2;
			ANGLE135 = ANGLE90 + ANGLE45;
			ANGLE225 = ANGLE180 + ANGLE45;
			ANGLE315 = ANGLE270 + ANGLE45;

			playerFov = angle170;
		}
		else
		{
			throw new CrusaderException("Invalid playerFieldOfView: "+playerFovOption);
		}
		playerFovHalf = playerFov /2;

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

			// facing left
			if (i >= ANGLE90 && i < ANGLE270)
			{
				xStepTable[i] = (tileSize / tanTable[i]);
				if (xStepTable[i] > 0)
				{
					xStepTable[i] = -xStepTable[i];
				}
			}
			// facing right
			else
			{
				xStepTable[i] = (tileSize / tanTable[i]);
				if (xStepTable[i] < 0)
				{
					xStepTable[i] = -xStepTable[i];
				}
			}

			// FACING DOWN
			if (i >= ANGLE0 && i < ANGLE180)
			{
				yStepTable[i] = (tileSize * tanTable[i]);
				if (yStepTable[i] < 0)
				{
					yStepTable[i] = -yStepTable[i];
				}
			}
			// FACING UP
			else
			{
				yStepTable[i] = (tileSize * tanTable[i]);
				if (yStepTable[i] > 0)
				{
					yStepTable[i] = -yStepTable[i];
				}
			}
		}

		for (i = -playerFovHalf; i <= projectionPlaneWidth- playerFovHalf; i++)
		{
			radian = arcToRad(i);
			fishbowlTable[i + playerFovHalf] = (float)(1.0F / Math.cos(radian));
		}

		playerDistToProjectionPlane =
			(int)((projectionPlaneWidth/2) / tanTable[playerFovHalf] *playerDistanceMult);
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
		int halfATile = tileSize /2;
		this.playerX = x* tileSize + halfATile;
		this.playerY = y* tileSize + halfATile;

		// set facing
		switch (facing)
		{
			case Facing.NORTH -> this.playerArc = ANGLE270;
			case Facing.SOUTH -> this.playerArc = ANGLE90;
			case Facing.EAST -> this.playerArc = ANGLE0;
			case Facing.WEST -> this.playerArc = ANGLE180;
			case Facing.NORTH_EAST -> this.playerArc = ANGLE315;
			case Facing.NORTH_WEST -> this.playerArc = ANGLE225;
			case Facing.SOUTH_EAST -> this.playerArc = ANGLE45;
			case Facing.SOUTH_WEST -> this.playerArc = ANGLE135;
			default ->
				throw new CrusaderException("Unrecognized facing: " + facing);
		}

		if (this.movementMode == MovementMode.DISCRETE || movementMode == MovementMode.OCTO)
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
					if (this.movementMode == MovementMode.DISCRETE)
					{
						throw new CrusaderException("Unrecognized facing for DISCRETE mode: " + facing);
					}
			}
		}

		if (movementMode == MovementMode.OCTO)
		{
			// offset based on facing: the offset amount works out dx and dy as
			// the two right angles sides of a right triangle with hypotenuse = halfATile-1
			// since |dx| = |dy| it follows that |dx| = (halfATile-1)/sqrt(2)

			int d = (int)((halfATile-1) / Math.sqrt(2D));

			switch (facing)
			{
				case Facing.NORTH_EAST:
					this.playerY += d;
					this.playerX -= d;
					break;
				case Facing.NORTH_WEST:
					this.playerY += d;
					this.playerX += d;
					break;
				case Facing.SOUTH_EAST:
					this.playerY -= d;
					this.playerX -= d;
					break;
				case Facing.SOUTH_WEST:
					this.playerY -= d;
					this.playerX += d;
					break;
				case Facing.WEST:
				case Facing.EAST:
				case Facing.SOUTH:
				case Facing.NORTH:
					// these have been handles above
					break;
				default:
					throw new CrusaderException("Unrecognized facing for OCTO mode: " + facing);
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
		int xGridIndex = x / tileSize;
		int yGridIndex = y / tileSize;

		return new Point(xGridIndex, yGridIndex);
	}

	/*-------------------------------------------------------------------------*/

	/**
	 * @return
	 * 	An approximate bounds (within this raycaster's XY coord system) that
	 * 	will contain the sprite of the given EngineObject
	 */
	public Rectangle getObjectBounds(EngineObject obj)
	{
		int projectedObjectWidth = obj.projectedObjectWidth;
		int projectedObjectHeight = obj.projectedObjectHeight;
		int projectedTextureOffset = obj.projectedTextureOffset;
		int projectedWallHeight = obj.projectedWallHeight;
		int x = obj.startScreenX;

		int y;
		int topY = projectionPlaneHeight/2 -projectedWallHeight/2 +projectedTextureOffset;
		y = switch (obj.verticalAlignment)
			{
				case TOP -> topY + projPlaneOffset;
				case CENTER -> topY + projectedWallHeight / 2 - projectedObjectHeight / 2 + projPlaneOffset;
				case BOTTOM -> topY + projectedWallHeight - projectedObjectHeight + projPlaneOffset;
				default ->
					throw new CrusaderException("invalid alignment " + obj.verticalAlignment);
			};

		return new Rectangle(x, y, projectedObjectWidth, projectedObjectHeight);
	}

	/*-------------------------------------------------------------------------*/
	public int getPlayerFacing()
	{
		return getPlayerFacing(this.playerArc);
	}

	@Override
	public int getTileSize()
	{
		return tileSize;
	}

	public Dimension getMapSize()
	{
		return new Dimension(map.width, map.length);
	}

	public int getProjectionPlaneWidth()
	{
		return projectionPlaneWidth;
	}

	public int getProjectionPlaneHeight()
	{
		return projectionPlaneHeight;
	}

	public int getPlayerHeightInUnits()
	{
		return playerHeightInUnits;
	}

	public int getPlayerDistToProjectionPlane()
	{
		return playerDistToProjectionPlane;
	}

	public int getPlayerHeight()
	{
		return playerHeight;
	}

	public float[] getFishbowlTable()
	{
		return fishbowlTable;
	}

	/*-------------------------------------------------------------------------*/
	private int getPlayerFacing(int arc)
	{
		if (this.movementMode == MovementMode.DISCRETE || movementMode == MovementMode.OCTO)
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
				if (this.movementMode == MovementMode.DISCRETE)
				{
					throw new CrusaderException("Invalid playerArc for DISCRETE mode: " + arc);
				}
			}
		}

		if (movementMode == MovementMode.OCTO)
		{
			if (arc == ANGLE45)
			{
				return Facing.SOUTH_EAST;
			}
			else if (arc == ANGLE135)
			{
				return Facing.SOUTH_WEST;
			}
			else if (arc == ANGLE225)
			{
				return Facing.NORTH_WEST;
			}
			else if (arc == ANGLE315)
			{
				return Facing.NORTH_EAST;
			}
			else
			{
				throw new CrusaderException("Invalid playerArc for OCTO mode: " + arc);
			}
		}

		return arc;
	}

	/*-------------------------------------------------------------------------*/
	private int getGeneralFacing(int arc)
	{
		arc = arc % ANGLE360;

		if (arc > ANGLE315 || arc <= ANGLE45)
		{
			return Facing.EAST;
		}
		else if (arc > ANGLE45 && arc <= ANGLE135)
		{
			return Facing.SOUTH;
		}
		else if (arc > ANGLE135 && arc <= ANGLE225)
		{
			return Facing.WEST;
		}
		else if (arc > ANGLE225 && arc <= ANGLE315)
		{
			return Facing.NORTH;
		}
		throw new CrusaderException("Invalid: " + arc);
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
		MouseClickScriptRecord mcsr = mouseClickScriptRecords[bufferIndex];

		if (mcsr.script != null)
		{
			int distanceInTiles = (int)Math.ceil(mcsr.distance/tileSize);

			if (distanceInTiles <= mcsr.script.getMaxDist())
			{
				mcsr.script.execute(map);
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	public MouseClickScript handleMouseClickAndReturnScript(int x, int y)
	{
		int bufferIndex = x + y * projectionPlaneWidth;

		MouseClickScriptRecord mcsr = mouseClickScriptRecords[bufferIndex];
		if (mcsr.script != null)
		{
			int distanceInTiles = (int)Math.ceil(mcsr.distance/tileSize);

			if (distanceInTiles <= mcsr.script.getMaxDist())
			{
				return mcsr.script;
			}
		}
		return null;
	}

	@Override
	public void addPostProcessor(PostProcessor postProcessor)
	{
		List<PostProcessor> list = new ArrayList<>();
		if (filters != null)
		{
			list.addAll(Arrays.asList(filters));
		}

		list.add(postProcessor);
		synchronized (filterMutex)
		{
			this.filters = list.toArray(new PostProcessor[0]);
		}
	}

	@Override
	public void removePostProcessor(PostProcessor postProcessor)
	{
		if (this.filters != null)
		{
			List<PostProcessor> list = new ArrayList<>(Arrays.asList(filters));
			list.remove(postProcessor);

			synchronized (filterMutex)
			{
				this.filters = list.toArray(new PostProcessor[0]);
			}
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

		int halfATile = tileSize /2-1;
		int diagonalD = (int)(halfATile / Math.sqrt(2D));

		if (this.movementMode == MovementMode.DISCRETE || this.movementMode == MovementMode.OCTO)
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
			else if (newPlayerArc == ANGLE45)
			{
				newPlayerX += diagonalD;
				newPlayerY += diagonalD;
			}
			else if (newPlayerArc == ANGLE135)
			{
				newPlayerX -= diagonalD;
				newPlayerY += diagonalD;
			}
			else if (newPlayerArc == ANGLE225)
			{
				newPlayerX -= diagonalD;
				newPlayerY -= diagonalD;
			}
			else if (newPlayerArc == ANGLE315)
			{
				newPlayerX += diagonalD;
				newPlayerY -= diagonalD;
			}
			else
			{
				throw new CrusaderException(
					"Invalid playerArc: "+newPlayerArc);
			}
		}

		// detect if we will pass through a wall, bit of a brute force approach here.
		if (movementMode == MovementMode.DISCRETE || movementMode == MovementMode.OCTO)
		{
			int tileIndex = getMapIndex(newPlayerX/ tileSize, newPlayerY/ tileSize);

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
						willPassThroughWall = map.horizontalWalls[map.getNorthWall(tileIndex)].solid;
					}
					else if (newPlayerArc == ANGLE180)
					{
						// facing west
						willPassThroughWall = map.horizontalWalls[map.getSouthWall(tileIndex)].solid;
					}
					else if (newPlayerArc == ANGLE90)
					{
						// facing south
						willPassThroughWall = map.verticalWalls[map.getEastWall(tileIndex)].solid;
					}
					else if (newPlayerArc == ANGLE270)
					{
						// facing north
						willPassThroughWall = map.verticalWalls[map.getWestWall(tileIndex)].solid;
					}
					else if (newPlayerArc == ANGLE45)
					{
						// facing south east
						willPassThroughWall =
							map.verticalWalls[map.getEastWall(tileIndex)].solid &&
							map.horizontalWalls[map.getNorthWall(tileIndex)].solid
							||
							map.verticalWalls[map.getEastWall(tileIndex)].solid &&
							map.verticalWalls[map.getEastWall(tileIndex-mapWidth)].solid
							||
							map.horizontalWalls[map.getNorthWall(tileIndex+1)].solid &&
							map.horizontalWalls[map.getNorthWall(tileIndex)].solid
							||
							map.verticalWalls[map.getEastWall(tileIndex-mapWidth)].solid &&
							map.horizontalWalls[map.getNorthWall(tileIndex+1)].solid;
					}
					else if (newPlayerArc == ANGLE135)
					{
						// facing south west
						willPassThroughWall =
							map.verticalWalls[map.getEastWall(tileIndex)].solid &&
							map.horizontalWalls[map.getSouthWall(tileIndex)].solid
							||
							map.horizontalWalls[map.getSouthWall(tileIndex)].solid &&
							map.horizontalWalls[map.getSouthWall(tileIndex + 1)].solid
							||
							map.verticalWalls[map.getEastWall(tileIndex)].solid &&
							map.verticalWalls[map.getEastWall(tileIndex+mapWidth)].solid
							||
							map.horizontalWalls[map.getSouthWall(tileIndex + 1)].solid &&
							map.verticalWalls[map.getEastWall(tileIndex+mapWidth)].solid;
					}
					else if (newPlayerArc == ANGLE225)
					{
						// facing north west
						willPassThroughWall =
							map.verticalWalls[map.getWestWall(tileIndex)].solid &&
							map.horizontalWalls[map.getSouthWall(tileIndex)].solid
							||
							map.verticalWalls[map.getWestWall(tileIndex)].solid &&
							map.verticalWalls[map.getWestWall(tileIndex+mapWidth)].solid
							||
							map.horizontalWalls[map.getSouthWall(tileIndex-1)].solid &&
							map.horizontalWalls[map.getSouthWall(tileIndex)].solid
							||
							map.verticalWalls[map.getWestWall(tileIndex+mapWidth)].solid &&
							map.horizontalWalls[map.getSouthWall(tileIndex-1)].solid;
					}
					else if (newPlayerArc == ANGLE315)
					{
						// facing north east
						willPassThroughWall =
							map.verticalWalls[map.getWestWall(tileIndex)].solid &&
							map.horizontalWalls[map.getNorthWall(tileIndex)].solid
							||
							map.verticalWalls[map.getWestWall(tileIndex)].solid &&
							map.verticalWalls[map.getWestWall(tileIndex - mapWidth)].solid
							||
							map.horizontalWalls[map.getNorthWall(tileIndex - 1)].solid &&
							map.horizontalWalls[map.getNorthWall(tileIndex)].solid
							||
							map.verticalWalls[map.getWestWall(tileIndex-mapWidth)].solid &&
							map.horizontalWalls[map.getNorthWall(tileIndex-1)].solid;
					}
					break;
				case KeyStroke.STRAFE_RIGHT:
					if (newPlayerArc == ANGLE0)
					{
						// facing east
						willPassThroughWall = map.horizontalWalls[map.getSouthWall(tileIndex)].solid;
					}
					else if (newPlayerArc == ANGLE180)
					{
						// facing west
						willPassThroughWall = map.horizontalWalls[map.getNorthWall(tileIndex)].solid;
					}
					else if (newPlayerArc == ANGLE90)
					{
						// facing south
						willPassThroughWall = map.verticalWalls[map.getWestWall(tileIndex)].solid;
					}
					else if (newPlayerArc == ANGLE270)
					{
						// facing north
						willPassThroughWall = map.verticalWalls[map.getEastWall(tileIndex)].solid;
					}
					else if (newPlayerArc == ANGLE45)
					{
						// facing south east
						willPassThroughWall =
							map.verticalWalls[map.getWestWall(tileIndex)].solid &&
							map.horizontalWalls[map.getSouthWall(tileIndex)].solid
							||
							map.verticalWalls[map.getWestWall(tileIndex)].solid &&
							map.verticalWalls[map.getWestWall(tileIndex+mapWidth)].solid
							||
							map.horizontalWalls[map.getSouthWall(tileIndex-1)].solid &&
							map.horizontalWalls[map.getSouthWall(tileIndex)].solid
							||
							map.verticalWalls[map.getWestWall(tileIndex+mapWidth)].solid &&
							map.horizontalWalls[map.getSouthWall(tileIndex-1)].solid;
					}
					else if (newPlayerArc == ANGLE135)
					{
						// facing south west
						willPassThroughWall =
							map.verticalWalls[map.getWestWall(tileIndex)].solid &&
							map.horizontalWalls[map.getNorthWall(tileIndex)].solid
							||
							map.verticalWalls[map.getWestWall(tileIndex)].solid &&
							map.verticalWalls[map.getWestWall(tileIndex - mapWidth)].solid
							||
							map.horizontalWalls[map.getNorthWall(tileIndex - 1)].solid &&
							map.horizontalWalls[map.getNorthWall(tileIndex)].solid
							||
							map.verticalWalls[map.getWestWall(tileIndex-mapWidth)].solid &&
							map.horizontalWalls[map.getNorthWall(tileIndex-1)].solid;
					}
					else if (newPlayerArc == ANGLE225)
					{
						// facing north west
						willPassThroughWall =
							map.verticalWalls[map.getEastWall(tileIndex)].solid &&
							map.horizontalWalls[map.getNorthWall(tileIndex)].solid
							||
							map.verticalWalls[map.getEastWall(tileIndex)].solid &&
							map.verticalWalls[map.getEastWall(tileIndex-mapWidth)].solid
							||
							map.horizontalWalls[map.getNorthWall(tileIndex+1)].solid &&
							map.horizontalWalls[map.getNorthWall(tileIndex)].solid
							||
							map.verticalWalls[map.getEastWall(tileIndex-mapWidth)].solid &&
							map.horizontalWalls[map.getNorthWall(tileIndex+1)].solid;
					}
					else if (newPlayerArc == ANGLE315)
					{
						// facing north east
						willPassThroughWall =
							map.verticalWalls[map.getEastWall(tileIndex)].solid &&
							map.horizontalWalls[map.getSouthWall(tileIndex)].solid
							||
							map.horizontalWalls[map.getSouthWall(tileIndex)].solid &&
							map.horizontalWalls[map.getSouthWall(tileIndex + 1)].solid
							||
							map.verticalWalls[map.getEastWall(tileIndex)].solid &&
							map.verticalWalls[map.getEastWall(tileIndex+mapWidth)].solid
							||
							map.horizontalWalls[map.getSouthWall(tileIndex + 1)].solid &&
							map.verticalWalls[map.getEastWall(tileIndex+mapWidth)].solid;
					}
					break;
				case KeyStroke.FORWARD:
					if (newPlayerArc == ANGLE0)
					{
						// facing east
						willPassThroughWall = map.verticalWalls[map.getEastWall(tileIndex)].solid;
					}
					else if (newPlayerArc == ANGLE180)
					{
						// facing west
						willPassThroughWall = map.verticalWalls[map.getWestWall(tileIndex)].solid;
					}
					else if (newPlayerArc == ANGLE90)
					{
						// facing south
						willPassThroughWall = map.horizontalWalls[map.getSouthWall(tileIndex)].solid;
					}
					else if (newPlayerArc == ANGLE270)
					{
						// facing north
						willPassThroughWall = map.horizontalWalls[map.getNorthWall(tileIndex)].solid;
					}
					else if (newPlayerArc == ANGLE45)
					{
						// facing south east
						willPassThroughWall =
							map.verticalWalls[map.getEastWall(tileIndex)].solid &&
							map.horizontalWalls[map.getSouthWall(tileIndex)].solid
							||
							map.horizontalWalls[map.getSouthWall(tileIndex)].solid &&
							map.horizontalWalls[map.getSouthWall(tileIndex + 1)].solid
							||
							map.verticalWalls[map.getEastWall(tileIndex)].solid &&
							map.verticalWalls[map.getEastWall(tileIndex+mapWidth)].solid
							||
							map.horizontalWalls[map.getSouthWall(tileIndex + 1)].solid &&
							map.verticalWalls[map.getEastWall(tileIndex+mapWidth)].solid;
					}
					else if (newPlayerArc == ANGLE135)
					{
						// facing south west
						willPassThroughWall =
							map.verticalWalls[map.getWestWall(tileIndex)].solid &&
							map.horizontalWalls[map.getSouthWall(tileIndex)].solid
							||
							map.verticalWalls[map.getWestWall(tileIndex)].solid &&
							map.verticalWalls[map.getWestWall(tileIndex+mapWidth)].solid
							||
							map.horizontalWalls[map.getSouthWall(tileIndex-1)].solid &&
							map.horizontalWalls[map.getSouthWall(tileIndex)].solid
							||
							map.verticalWalls[map.getWestWall(tileIndex+mapWidth)].solid &&
							map.horizontalWalls[map.getSouthWall(tileIndex-1)].solid;
					}
					else if (newPlayerArc == ANGLE225)
					{
						// facing north west
						willPassThroughWall =
							map.verticalWalls[map.getWestWall(tileIndex)].solid &&
							map.horizontalWalls[map.getNorthWall(tileIndex)].solid
							||
							map.verticalWalls[map.getWestWall(tileIndex)].solid &&
							map.verticalWalls[map.getWestWall(tileIndex - mapWidth)].solid
							||
							map.horizontalWalls[map.getNorthWall(tileIndex - 1)].solid &&
							map.horizontalWalls[map.getNorthWall(tileIndex)].solid
							||
							map.verticalWalls[map.getWestWall(tileIndex-mapWidth)].solid &&
							map.horizontalWalls[map.getNorthWall(tileIndex-1)].solid;
					}
					else if (newPlayerArc == ANGLE315)
					{
						// facing north east
						willPassThroughWall =
							map.verticalWalls[map.getEastWall(tileIndex)].solid &&
							map.horizontalWalls[map.getNorthWall(tileIndex)].solid
							||
							map.verticalWalls[map.getEastWall(tileIndex)].solid &&
							map.verticalWalls[map.getEastWall(tileIndex-mapWidth)].solid
							||
							map.horizontalWalls[map.getNorthWall(tileIndex+1)].solid &&
							map.horizontalWalls[map.getNorthWall(tileIndex)].solid
							||
							map.verticalWalls[map.getEastWall(tileIndex-mapWidth)].solid &&
							map.horizontalWalls[map.getNorthWall(tileIndex+1)].solid;
					}
					break;
				case KeyStroke.BACKWARD:
					if (newPlayerArc == ANGLE0)
					{
						// facing east
						willPassThroughWall = map.verticalWalls[map.getWestWall(tileIndex)].solid;
					}
					else if (newPlayerArc == ANGLE180)
					{
						// facing west
						willPassThroughWall = map.verticalWalls[map.getEastWall(tileIndex)].solid;
					}
					else if (newPlayerArc == ANGLE90)
					{
						// facing south
						willPassThroughWall = map.horizontalWalls[map.getNorthWall(tileIndex)].solid;
					}
					else if (newPlayerArc == ANGLE270)
					{
						// facing north
						willPassThroughWall = map.horizontalWalls[map.getSouthWall(tileIndex)].solid;
					}
					else if (newPlayerArc == ANGLE45)
					{
						// facing south east
						willPassThroughWall =
							map.verticalWalls[map.getWestWall(tileIndex)].solid &&
							map.horizontalWalls[map.getNorthWall(tileIndex)].solid
							||
							map.verticalWalls[map.getWestWall(tileIndex)].solid &&
							map.verticalWalls[map.getWestWall(tileIndex - mapWidth)].solid
							||
							map.horizontalWalls[map.getNorthWall(tileIndex - 1)].solid &&
							map.horizontalWalls[map.getNorthWall(tileIndex)].solid
							||
							map.verticalWalls[map.getWestWall(tileIndex-mapWidth)].solid &&
							map.horizontalWalls[map.getNorthWall(tileIndex-1)].solid;
					}
					else if (newPlayerArc == ANGLE135)
					{
						// facing south west
						willPassThroughWall =
							map.verticalWalls[map.getEastWall(tileIndex)].solid &&
							map.horizontalWalls[map.getNorthWall(tileIndex)].solid
							||
							map.verticalWalls[map.getEastWall(tileIndex)].solid &&
							map.verticalWalls[map.getEastWall(tileIndex-mapWidth)].solid
							||
							map.horizontalWalls[map.getNorthWall(tileIndex+1)].solid &&
							map.horizontalWalls[map.getNorthWall(tileIndex)].solid
							||
							map.verticalWalls[map.getEastWall(tileIndex-mapWidth)].solid &&
							map.horizontalWalls[map.getNorthWall(tileIndex+1)].solid;
					}
					else if (newPlayerArc == ANGLE225)
					{
						// facing north west
						willPassThroughWall =
							map.verticalWalls[map.getWestWall(tileIndex)].solid &&
							map.horizontalWalls[map.getSouthWall(tileIndex)].solid
							||
							map.verticalWalls[map.getWestWall(tileIndex)].solid &&
							map.verticalWalls[map.getWestWall(tileIndex+mapWidth)].solid
							||
							map.horizontalWalls[map.getSouthWall(tileIndex-1)].solid &&
							map.horizontalWalls[map.getSouthWall(tileIndex)].solid
							||
							map.verticalWalls[map.getWestWall(tileIndex+mapWidth)].solid &&
							map.horizontalWalls[map.getSouthWall(tileIndex-1)].solid;
					}
					else if (newPlayerArc == ANGLE315)
					{
						// facing north east
						willPassThroughWall =
							map.verticalWalls[map.getWestWall(tileIndex)].solid &&
							map.horizontalWalls[map.getSouthWall(tileIndex)].solid
							||
							map.verticalWalls[map.getWestWall(tileIndex)].solid &&
							map.verticalWalls[map.getWestWall(tileIndex+mapWidth)].solid
							||
							map.horizontalWalls[map.getSouthWall(tileIndex-1)].solid &&
							map.horizontalWalls[map.getSouthWall(tileIndex)].solid
							||
							map.verticalWalls[map.getWestWall(tileIndex+mapWidth)].solid &&
							map.horizontalWalls[map.getSouthWall(tileIndex-1)].solid;
					}
					break;
				default:
					throw new CrusaderException("Invalid key stroke: "+key);
			}
		}

		switch (key)
		{
			case KeyStroke.TURN_LEFT:
				// rotate left
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
				if (newPlayerArc == ANGLE45 || newPlayerArc == ANGLE135 ||
					newPlayerArc == ANGLE225 || newPlayerArc == ANGLE315)
				{
					newPlayerX += (int)(Math.signum(strafeLeftXDir) * tileSize);
					newPlayerY += (int)(Math.signum(strafeLeftYDir) * playerSpeed);
				}
				else
				{
					newPlayerX += (int)(strafeLeftXDir * playerSpeed);
					newPlayerY += (int)(strafeLeftYDir * playerSpeed);
				}
				break;
			case KeyStroke.STRAFE_RIGHT:
				// strafe right
				if (newPlayerArc == ANGLE45 || newPlayerArc == ANGLE135 ||
					newPlayerArc == ANGLE225 || newPlayerArc == ANGLE315)
				{
					newPlayerX += (int)(Math.signum(strafeRightXDir) * tileSize);
					newPlayerY += (int)(Math.signum(strafeRightYDir) * tileSize);
				}
				else
				{
					newPlayerX += (int)(strafeRightXDir * playerSpeed);
					newPlayerY += (int)(strafeRightYDir * playerSpeed);
				}
				break;
			case KeyStroke.FORWARD:
				// move forward
				if (newPlayerArc == ANGLE45 || newPlayerArc == ANGLE135 ||
					newPlayerArc == ANGLE225 || newPlayerArc == ANGLE315)
				{
					newPlayerX += (int)(Math.signum(playerXDir) * this.tileSize);
					newPlayerY += (int)(Math.signum(playerYDir) * this.tileSize);
				}
				else
				{
					newPlayerX += (int)(playerXDir * this.playerSpeed);
					newPlayerY += (int)(playerYDir * this.playerSpeed);
				}
				break;
			case KeyStroke.BACKWARD:
				// move backward
				if (newPlayerArc == ANGLE45 || newPlayerArc == ANGLE135 ||
					newPlayerArc == ANGLE225 || newPlayerArc == ANGLE315)
				{
					newPlayerX -= (int)(Math.signum(playerXDir) * this.tileSize);
					newPlayerY -= (int)(Math.signum(playerYDir) * this.tileSize);
				}
				else
				{
					newPlayerX -= (int)(playerXDir * this.playerSpeed);
					newPlayerY -= (int)(playerYDir * this.playerSpeed);
				}
				break;
			default:
				throw new CrusaderException("Invalid key stroke: "+key);
		}

		if (movementMode == MovementMode.DISCRETE || movementMode == MovementMode.OCTO)
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
			else if (newPlayerArc == ANGLE45)
			{
				newPlayerX -= diagonalD;
				newPlayerY -= diagonalD;
			}
			else if (newPlayerArc == ANGLE135)
			{
				newPlayerX += diagonalD;
				newPlayerY -= diagonalD;
			}
			else if (newPlayerArc == ANGLE225)
			{
				newPlayerX += diagonalD;
				newPlayerY += diagonalD;
			}
			else if (newPlayerArc == ANGLE315)
			{
				newPlayerX -= diagonalD;
				newPlayerY += diagonalD;
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
		for (Texture texture : map.textures)
		{
			this.addTexture(texture);
		}
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

		texture.setImageData(
			new int[texture.images.length]
				[texture.imageHeight*texture.imageWidth]);
		
		for (int i = 0; i < texture.images.length; i++)
		{
			texture.getImageData()[i] = this.grabPixels(
				texture.images[i],
				texture.imageWidth,
				texture.imageHeight);
		}

		if (texture.tint != null)
		{
			texture.applyTint(texture.tint);
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
	 * This method sets up the global variables in preparation for calling
	 * {@link #drawColumn} to draw the column.
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

		//---- Initialisation --------------------------------------------------//

		// A special case calculation of the X-intersection initial values.
		if (castArc > ANGLE0 && castArc < ANGLE180)
		{
			// The ray is facing south
			
			horizontalGrid = (playerY / tileSize) * tileSize + tileSize;
			distToNextHorizontalGrid = tileSize;
			
			float xtemp = iTanTable[castArc] * (horizontalGrid - playerY);
			xIntersection = xtemp + playerX;
		}
		else
		{
			// The ray is facing north
			
			horizontalGrid = (playerY / tileSize) * tileSize;
			distToNextHorizontalGrid = -tileSize;
			
			float xtemp = iTanTable[castArc] * (horizontalGrid - playerY);
			xIntersection = xtemp + playerX;
			
			horizontalGrid--;
		}
		
		// A special case calculation of the Y-intersection initial values.
		if (castArc < ANGLE90 || castArc > ANGLE270)
		{
			// ray is facing east
			
			verticalGrid = tileSize + (playerX / tileSize) * tileSize;
			distToNextVerticalGrid = tileSize;
			
			float ytemp = tanTable[castArc] * (verticalGrid - playerX);
			yIntersection = ytemp + playerY;
		}
		else
		{
			verticalGrid = (playerX / tileSize) * tileSize;
			distToNextVerticalGrid = -tileSize;
			
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

		// should be optimised away
		for (int j=0; j<maxHitDepth; j++)
		{
			blockHitRecord[castColumn][j].distance = -1;
			blockHitRecord[castColumn][j].textureXRecord = -1;
			blockHitRecord[castColumn][j].textureYRecord = -1;
			blockHitRecord[castColumn][j].blockHit = -1;
			blockHitRecord[castColumn][j].hitType = -1;
			blockHitRecord[castColumn][j].textures = null;
			blockHitRecord[castColumn][j].wall = null;
			blockHitRecord[castColumn][j].projectedWallHeight = -1;
		}

		//---- Ray Casting -----------------------------------------------------//

		// calculate the first two hits. The nearest determines which order we
		// search for more in

		int depth = 0;

		BlockHitRecord horizBlockHitRecord = new BlockHitRecord();
		BlockHitRecord vertBlockHitRecord = new BlockHitRecord();

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
			while (depth < maxHitDepth &&
				(distToHorizontalGridBeingHit < Float.MAX_VALUE ||
					distToVerticalGridBeingHit < Float.MAX_VALUE))
			{
				if (horizFirst)
				{
					if (distToHorizontalGridBeingHit < Float.MAX_VALUE &&
						horizBlockHitRecord.wall != null &&
						horizBlockHitRecord.wall.visible &&
						depth < maxHitDepth)
					{
						populateBlockHitRecordGlobal(castColumn, horizBlockHitRecord, depth);
						depth++;
					}
				}
				else
				{
					if (distToVerticalGridBeingHit < Float.MAX_VALUE &&
						vertBlockHitRecord.wall != null &&
						vertBlockHitRecord.wall.visible &&
						depth < maxHitDepth)
					{
						populateBlockHitRecordGlobal(castColumn, vertBlockHitRecord, depth);
						depth++;
					}
				}

				if (depth < maxHitDepth)
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

			blockHitRecord[castColumn][depth].projectedWallHeight = tileSize *
				(float)playerDistToProjectionPlane / blockHitRecord[castColumn][depth].distance;
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
		// init the result
		result.distance = -1;
		result.textureXRecord = -1;
		result.textureYRecord = -1;
		result.blockHit = -1;
		result.hitType = -1;
		result.textures = null;
		result.wall = null;
		result.projectedWallHeight = -1;

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

		xGridIndex = playerX / tileSize;
		yGridIndex = playerY / tileSize;

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
				xGridIndex = (int)(xIntersection / tileSize);
				yGridIndex = (horizontalGrid / tileSize);

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
						horizontalWallHit = this.map.horizontalWalls[wallIndex];
						horizontalBlockHitRecord = this.map.getNorthBlock(wallIndex);
					}
					else
					{
						horizontalWallHit = null;
						horizontalBlockHitRecord = -1;
					}
				}
				else
				{
					// ray is tracing north
					int wallIndex;
					if (!outOfBounds)
					{
						wallIndex = map.getSouthWall(mapIndex);
						horizontalWallHit = this.map.horizontalWalls[wallIndex];
						horizontalBlockHitRecord = this.map.getSouthBlock(wallIndex);
					}
					else
					{
						horizontalWallHit = null;
						horizontalBlockHitRecord = -1;
					}
				}

				if (horizontalWallHit != null && horizontalWallHit.visible || !requireVisibleWall)
				{
					distToHorizontalGridBeingHit = (xIntersection - playerX) * iCosTable[castArc];
					horizontalTextureXRecord = (int)(xIntersection) % tileSize;

					result.distance = distToHorizontalGridBeingHit;
					result.textureXRecord = horizontalTextureXRecord;
					result.blockHit = horizontalBlockHitRecord;
					result.hitType = HIT_HORIZONTAL;
					result.textures = horizontalWallHit.textures;
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
		// init the result
		result.distance = -1;
		result.textureXRecord = -1;
		result.textureYRecord = -1;
		result.blockHit = -1;
		result.hitType = -1;
		result.textures = null;
		result.wall = null;
		result.projectedWallHeight = -1;

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

		xGridIndex = playerX / tileSize;
		yGridIndex = playerY / tileSize;

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
				xGridIndex = (verticalGrid / tileSize);
				yGridIndex = (int)(yIntersection / tileSize);

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
						verticalWallHit = this.map.verticalWalls[wallIndex];
						verticalBlockHitRecord = map.getWestBlock(wallIndex);
					}
					else
					{
						verticalWallHit = null;
						verticalBlockHitRecord = -1;
					}

				}
				else
				{
					int wallIndex;
					if (!outOfBounds)
					{
						wallIndex = map.getEastWall(mapIndex);
						verticalWallHit = this.map.verticalWalls[wallIndex];
						verticalBlockHitRecord = map.getEastBlock(wallIndex);
					}
					else
					{
						verticalWallHit = null;
						verticalBlockHitRecord = -1;
					}
				}

				if (verticalWallHit != null && verticalWallHit.visible || !requireVisibleWall)
				{
					distToVerticalGridBeingHit = (yIntersection - playerY) * iSinTable[castArc];
					verticalTextureXRecord = (int)(yIntersection) % tileSize;

					result.distance = distToVerticalGridBeingHit;
					result.textureXRecord = verticalTextureXRecord;
					result.blockHit = verticalBlockHitRecord;
					result.hitType = HIT_VERTICAL;
					result.textures = verticalWallHit.textures;
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

			// field of view is playerFov degree with the point of view
			// (player's direction in the middle)
			// We will trace the rays starting from the leftmost ray
			float castArc = playerArc- playerFovHalf;

			// wrap around if necessary
			if (castArc < 0)
			{
				castArc = ANGLE360 + castArc;
			}
			float castInc = (float)playerFov /(float)projectionPlaneWidth;

			// execute any animations
			this.animation();

			// execute any map scripts
			this.map.executeScripts(frameCount);

			for (EngineObject obj : objects)
			{
				obj.executeScripts(frameCount);
			}

			// init object state for rendering
			initAndSortObjects();

			// fill the render buffer with alpha
			Arrays.fill(renderBuffer, 0x00000000);

			try
			{
				// ray cast and render each column
				for (int screenX = 0; screenX < projectionPlaneWidth; screenX++)
				{
					columnRenderers[screenX].castArc = castArc;
					executor.submit(columnRenderers[screenX]);

					castArc += castInc;
					if (castArc >= ANGLE360)
					{
						castArc -= ANGLE360;
					}
				}

				// wait for all the render threads to finish
				for (int screenX = 0; screenX < projectionPlaneWidth; screenX++)
				{
					executor.take();
				}

				// post processing filter
				if (filters != null)
				{

					synchronized (filterMutex)
					{
						for (int i = 0; i < filters.length; i++)
						{
							for (int screenX = 0; screenX < projectionPlaneWidth; screenX++)
							{
								columnFilterers[screenX].filter = filters[i];
								executor.submit(columnFilterers[screenX]);
							}

							// wait for all the render threads to finish
							for (int screenX = 0; screenX < projectionPlaneWidth; screenX++)
							{
								executor.take();
							}

							System.arraycopy(
								postProcessingBuffer,
								0,
								renderBuffer,
								0,
								renderBuffer.length);
						}
					}
				}
			}
			catch (InterruptedException e)
			{
				throw new CrusaderException(e);
			}


			this.frameCount++;
		}

		return this.renderBuffer;
	}

	/*-------------------------------------------------------------------------*/
	private void drawObjects(int castColumn, int depth)
	{
		int objectCount = objects.length - 1;
		for (; objectCount >= 0; objectCount--)
		{
			if (this.objects[objectCount].distance > 0 &&
				this.objects[objectCount].apparentDistance < blockHitRecord[castColumn][depth].distance)
			{
				if (this.objects[objectCount].projectedObjectWidth > 0 &&
					this.objects[objectCount].endScreenX > 0)
				{
					if (castColumn >= this.objects[objectCount].startScreenX &&
						castColumn < this.objects[objectCount].endScreenX)
					{
						drawObjectColumn(this.objects[objectCount], renderBuffer, castColumn, depth);
					}
				}
			}
			else
			{
				break;
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	private boolean drawColumn(int castArc, int screenX, int depth, int[] outputBuffer)
	{
		boolean hasAlpha = false;

		BlockHitRecord hitRecord = blockHitRecord[screenX][depth];
		float projectedWallHeight = hitRecord.projectedWallHeight;
		int blockHit = hitRecord.blockHit;

		Wall wall = hitRecord.wall;
		Texture[] textures = hitRecord.textures;

		if (blockHit == -1 || wall == null || textures == null)
		{
			// no block hit at this depth, prob out of bounds
			return false;
		}

		int wallStackHeight = wall.height;
		Texture[] maskTextures = wall.maskTextures;

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
			shadeMult = calcShadeMult(hitRecord.distance, shadingDistance, shadingThickness);
		}

		float wallTop = playerHeight - (projectedWallHeight / 2) - (projectedWallHeight * (wallStackHeight - 1)) + projPlaneOffset;
		float wallBottom = playerHeight + (projectedWallHeight / 2) + projPlaneOffset;
		// todo: do we really need this?
		float diff = -(playerHeight -(projectedWallHeight/2) -(projectedWallHeight*(wallStackHeight-1))) -projPlaneOffset;

		int top = Math.round(Math.max(wallTop, 0));
		int bottom = Math.round(Math.min(wallBottom, projectionPlaneHeight));
		int ceilingTop = Math.round(Math.max(playerHeight -(projectedWallHeight/2) +projPlaneOffset, 0)); // todo ceiling height

		int textureX = hitRecord.textureXRecord;

		// todo: fix image mapping so this condition doesn't happen
		if (textureX < 0)
		{
			textureX = 0;
		}

		int textureY = 0;
		int screenY = top;

		if (ceilingTop > 0)
		{
			hasAlpha |= drawCeiling(castArc, screenX, projectedWallHeight, depth, ceilingTop, outputBuffer);
		}

		int roundedProjWallHeight = (int)Math.ceil(projectedWallHeight);
		int roundedWallTop = (int)Math.ceil(wallTop);

		while (screenY <= bottom)
		{
			int bufferIndex = screenX + screenY * projectionPlaneWidth;

			if (bufferIndex < outputBuffer.length && // todo prob hides a bug
				hasAlpha(outputBuffer[bufferIndex]))
			{
				int textureIndex = 0;
				int maskTextureIndex = 0;
				if (hitRecord.wall.height > 1)
				{
					int index = wallStackHeight - ((screenY- roundedWallTop)/ roundedProjWallHeight);

					textureIndex = Math.abs(((index-1) % textures.length));
					if (maskTextures != null)
					{
						maskTextureIndex = Math.abs((index-1) % maskTextures.length);
					}
				}

				textureY = (int)(((screenY+diff) * tileSize) / roundedProjWallHeight);

				int colour;
				if (maskTextures != null)
				{
					// use the mask texture instead of the wall texture
					int maskPixel = maskTextures[maskTextureIndex].getCurrentImageData(textureX, textureY, timeNow);

					colour = alphaBlend(
						textures[textureIndex].getCurrentImageData(textureX, textureY, timeNow),
						maskPixel);

					// if this click is on a visible piece of the mask texture, use that script
					if (wall.maskTextureMouseClickScript != null &&
						getAlpha(maskPixel) != 0)
					{
						// use the mask texture mouse click script instead
						this.mouseClickScriptRecords[bufferIndex].script =
							wall.maskTextureMouseClickScript;
						this.mouseClickScriptRecords[bufferIndex].distance =
							hitRecord.distance;
					}
				}
				else
				{
					colour = textures[textureIndex].getCurrentImageData(textureX, textureY, timeNow);
					this.mouseClickScriptRecords[bufferIndex].script = wall.mouseClickScript;
					this.mouseClickScriptRecords[bufferIndex].distance = hitRecord.distance;
				}

				int pixel = colourPixel(colour, lightLevel, shadeMult);
				pixel = alphaBlend(pixel, outputBuffer[bufferIndex]);
				outputBuffer[bufferIndex] = pixel;

				if (!hasAlpha)
				{
					hasAlpha |= hasAlpha(pixel);
				}
			}

			screenY++;
		}

		if (bottom+1 < projectionPlaneHeight)
		{
			hasAlpha |= drawFloor(castArc, screenX, projectedWallHeight, depth, bottom+1, outputBuffer);
		}

		return hasAlpha;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Can Be Optomised
	 *  @param column
	 * 	the column being drawn
	 * @param wallHeight
	 * @param outputBuffer
	 */
	private boolean drawFloor(
		int castArc,
		int column,
		float wallHeight,
		int depth,
		int top,
		int[] outputBuffer)
	{
		boolean hasAlpha = false;

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
		int pixel;
		int lightLevel;
		Texture texture=null;
		Texture maskTexture=null;

		while (screenY < bottom)
		{
			try
			{
				int bufferIndex = column + screenY * projectionPlaneWidth;
				if (hasAlpha(outputBuffer[bufferIndex]))
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
					xIntersection = Math.min(xIntersection, mapWidth* tileSize -1);
					yIntersection = Math.min(yIntersection, mapLength* tileSize -1);
					//---
					xIntersection = Math.max(xIntersection, 0);
					yIntersection = Math.max(yIntersection, 0);
					//---

					gridX = xIntersection / tileSize;
					gridY = yIntersection / tileSize;
					mapIndex = gridX + gridY*mapWidth;

					textureX = Math.abs(xIntersection % tileSize);
					textureY = Math.abs(yIntersection % tileSize);

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
					if (maskTexture != null)
					{
						colour = alphaBlend(
							texture.getCurrentImageData(textureX, textureY, timeNow),
							maskTexture.getCurrentImageData(textureX, textureY, timeNow));
					}
					else
					{
						colour = texture.getCurrentImageData(textureX, textureY, timeNow);
					}
					pixel = colourPixel(colour, lightLevel, shadeMult);
					pixel = alphaBlend(pixel, outputBuffer[bufferIndex]);

					outputBuffer[bufferIndex] = pixel;

					if (!hasAlpha)
					{
						hasAlpha |= hasAlpha(pixel);
					}

					// mouse click scripts associated with floors and ceilings not yet supported
					this.mouseClickScriptRecords[bufferIndex].script = null;
					this.mouseClickScriptRecords[bufferIndex].distance = -1;
				}

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

		return hasAlpha;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Can Be Optomised
	 *  @param screenX
	 * 	the column being drawn
	 * @param wallHeight
	 * @param outputBuffer
	 */
	private boolean drawCeiling(
		int castArc,
		int screenX,
		float wallHeight,
		int depth,
		int bottom,
		int[] outputBuffer)
	{
		boolean hasAlpha = false;

		int top = 0;//halfProjectionPlaneHeight + (wallHeight/2);
		// this is the bottom if the wall height is 1
//		int height1bottom = playerHeight - (wallHeight/2) +projPlaneOffset;

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
		int colour;
		int pixel;
		int lightLevel;
		int ceilingHeight;
		Texture texture=null;
		Texture maskTexture=null;

		while (screenY < bottom)
		{
			try
			{
				int bufferIndex = screenX + screenY * projectionPlaneWidth;

				heightOnProjectionPlane = (projectionPlaneHeight - this.playerHeight) - screenY + projPlaneOffset;

				straightDistance = playerDistToProjectionPlane
					* playerHeightInUnits / (float)(heightOnProjectionPlane);

				beta = playerArc - castArc;

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

				xDistance = actualDistance * cosTable[castArc];
				yDistance = actualDistance * sinTable[castArc];

				xIntersection = (int)(playerX + xDistance);
				yIntersection = (int)(playerY + yDistance);

				//--- todo: these inaccuracies surely point to a bug in the maths somewhere?
				xIntersection = Math.min(xIntersection, mapWidth * tileSize - 1);
				yIntersection = Math.min(yIntersection, mapLength * tileSize - 1);
				//---
				xIntersection = Math.max(xIntersection, 0);
				yIntersection = Math.max(yIntersection, 0);
				//---

				gridX = xIntersection / tileSize;
				gridY = yIntersection / tileSize;
				mapIndex = gridX + gridY * mapWidth;

				textureX = Math.abs(xIntersection % tileSize);
				textureY = Math.abs(yIntersection % tileSize);

				texture = map.tiles[mapIndex].floorTexture;
				maskTexture = map.tiles[mapIndex].ceilingMaskTexture;
				ceilingHeight = map.tiles[mapIndex].ceilingHeight;

				if (bufferIndex < outputBuffer.length && // todo prod hides a bug
					hasAlpha(outputBuffer[bufferIndex]))
				{
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
					if (maskTexture != null)
					{
						colour = alphaBlend(
							texture.getCurrentImageData(textureX, textureY, timeNow),
							maskTexture.getCurrentImageData(textureX, textureY, timeNow));
					}
					else
					{
						colour = texture.getCurrentImageData(textureX, textureY, timeNow);
					}

					pixel = colourPixel(colour, lightLevel, shadeMult);
					pixel = alphaBlend(pixel, outputBuffer[bufferIndex]);

					outputBuffer[bufferIndex] = pixel;

					if (!hasAlpha)
					{
						hasAlpha |= hasAlpha(pixel);
					}

					// mouse click scripts associated with floors and ceilings not yet supported
					this.mouseClickScriptRecords[bufferIndex].script = null;
					this.mouseClickScriptRecords[bufferIndex].distance = -1;
				}

				screenY++;
			}
			catch (RuntimeException e)
			{
				log("playerArc = [" + playerArc + "]");
				log("castArc = [" + castArc + "]");
				log("wallHeight = [" + wallHeight + "]");
				log("column = [" + screenX + "]");
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
				log("blockHitRecord[column][0] = " + blockHitRecord[screenX][0]);

				throw e;
			}
		}

		return hasAlpha;
	}

	/*-------------------------------------------------------------------------*/

	/**
	 * @return the sky pixel to render for the given cast arc and Y coord
	 */
	private int getSkyPixel(int castArc, int screenY)
	{
		int result = 0;

		for (SkyConfig config : map.skyConfigs)
		{
			int pixel = renderSkyConfig(config, castArc, screenY);
			result = (result == 0) ? pixel : alphaBlend(pixel, result);
			if (!hasAlpha(result))
			{
				return result; // Fully opaque, stop rendering
			}
		}
		return result;
	}

	/*-------------------------------------------------------------------------*/
	private int renderSkyConfig(SkyConfig skyConfig, int castArc, int screenY)
	{
		switch (skyConfig.type)
		{
			case CYLINDER_IMAGE ->
			{
				return renderCylinderImageSky(skyConfig, castArc, screenY);
			}
			case CYLINDER_GRADIENT ->
			{
				return computeCylinderGradient(skyConfig, castArc, screenY);
			}
			case HIGH_CEILING_IMAGE ->
			{
				return renderHighCeilingSky(skyConfig, castArc, screenY);
			}
			case CUBEMAP_IMAGES ->
			{
				return renderCubemapImages(skyConfig, castArc, screenY);
			}
			default -> throw new MazeException("invalid: "+skyConfig.type);
		}
	}

	/*-------------------------------------------------------------------------*/
	private int renderCubemapImages(SkyConfig config, int castArc, int screenY)
	{
		double angle = ((double)castArc / ANGLE360) * 4.0; // Map arc to one of 4 orientations
		double u = angle +0.5 -(int)angle; // Texture coordinate within the face

		int facing = getGeneralFacing(castArc);
		Texture texture = switch (facing)
			{
				case Facing.NORTH -> config.cubeNorth;
				case Facing.EAST -> config.cubeEast;
				case Facing.SOUTH -> config.cubeSouth;
				case Facing.WEST -> config.cubeWest;
				default -> throw new MazeException("invalid: "+facing);
			};

		int skyTextureX = (int)(u * texture.imageWidth);
		int skyTextureY = (int)(screenY * texture.imageHeight / projectionPlaneHeight);
		return texture.getCurrentImageData(skyTextureX, skyTextureY, timeNow);
	}

	/*-------------------------------------------------------------------------*/
	private int computeCylinderGradient(SkyConfig config, int castArc, int screenY)
	{
		float gradientRatio = (float)screenY / projectionPlaneHeight;
		int bottomColor = config.bottomColour;
		int topColor = config.topColour;

		int r1 = (bottomColor >> 16) & 0xFF;
		int g1 = (bottomColor >> 8) & 0xFF;
		int b1 = bottomColor & 0xFF;

		int r2 = (topColor >> 16) & 0xFF;
		int g2 = (topColor >> 8) & 0xFF;
		int b2 = topColor & 0xFF;

		int r = (int)(r1 + gradientRatio * (r2 - r1));
		int g = (int)(g1 + gradientRatio * (g2 - g1));
		int b = (int)(b1 + gradientRatio * (b2 - b1));

		return (255 << 24) | (r << 16) | (g << 8) | b;
	}

	/*-------------------------------------------------------------------------*/
	private int renderCylinderImageSky(SkyConfig skyConfig, int castArc, int screenY)
	{
		Texture image = skyConfig.cylinderSkyImage;

		int skyTextureX = castArc % image.imageWidth;
		int skyTextureY = screenY * image.imageHeight / playerHeight;

		return image.getCurrentImageData(skyTextureX, skyTextureY, timeNow);
	}

	/*-------------------------------------------------------------------------*/
	private int renderHighCeilingSky(SkyConfig skyConfig, int castArc, int screenY)
	{
		int skyTextureX;
		int skyTextureY;

		Texture image = skyConfig.ceilingImage;

		double heightOnProjectionPlane = (projectionPlaneHeight - this.playerHeight) - screenY + projPlaneOffset;

		// this scaling makes the sky appear higher than normal
		heightOnProjectionPlane /= skyConfig.ceilingHeight;
		double straightDistance = playerDistToProjectionPlane
			* playerHeightInUnits / (float)(heightOnProjectionPlane);
		int beta = playerArc - castArc;
		if (beta < 0)
		{
			beta += ANGLE360;
		}
		else if (beta > ANGLE360)
		{
			beta -= ANGLE360;
		}
		double actualDistance = straightDistance / cosTable[beta];

		// now we know that the ray intersects with the floor at an
		// angle of (castArc) and a distance of (actualDistance)

		double xDistance = actualDistance * cosTable[castArc];
		double yDistance = actualDistance * sinTable[castArc];

		// not adding player x/y means the sky stays static as the player moves
		int xIntersection = (int)(/*playerX +*/ xDistance);
		int yIntersection = (int)(/*playerY +*/ yDistance);

		// adding half the texture dimension so that any tiling happens off to the side of the player eyeline
		skyTextureX = (xIntersection / skyConfig.imageScale +image.imageWidth/2) % image.imageWidth;
		skyTextureY = (yIntersection / skyConfig.imageScale +image.imageHeight/2) % image.imageHeight;

		return image.getCurrentImageData(skyTextureX, skyTextureY, timeNow);
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

				center = (int)(objectAngle - (playerArc - playerFovHalf));

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
					tileSize,
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
	private void drawObjectColumn(
		EngineObject obj,
		int[] outputBuffer,
		int castColumn,
		int depth)
	{
		if (blockHitRecord[castColumn][depth].distance > obj.apparentDistance)
		{
			int startScreenY;
			int topY = projectionPlaneHeight/2 -obj.projectedWallHeight/2 +obj.projectedTextureOffset;
			startScreenY = switch (obj.verticalAlignment)
				{
					case TOP		-> topY + projPlaneOffset;
					case CENTER	-> topY + obj.projectedWallHeight / 2 - obj.projectedObjectHeight / 2 + projPlaneOffset;
					case BOTTOM	-> topY + obj.projectedWallHeight - obj.projectedObjectHeight + projPlaneOffset;
					default ->
						throw new CrusaderException("invalid alignment " + obj.verticalAlignment);
				};

			int currentScreenY = startScreenY;
			int endScreenY = startScreenY + obj.projectedObjectHeight;

			int textureWidth = obj.renderTexture.imageWidth;
			int textureHeight = obj.renderTexture.imageHeight;

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
				int bufferIndex = castColumn + projectionPlaneWidth * currentScreenY;
				if (hasAlpha(outputBuffer[bufferIndex]))
				{
					int textureX = textureWidth * (castColumn - obj.startScreenX) / obj.projectedObjectWidth;
					int textureY = textureHeight * (currentScreenY - startScreenY) / obj.projectedObjectHeight;

					int imagePixel = obj.getCurrentRenderTextureData(textureX, textureY, timeNow);

					int pixel = colourPixel(imagePixel, obj.adjustedLightLevel, obj.shadeMult);
					pixel = alphaBlend(pixel, outputBuffer[bufferIndex]);

					outputBuffer[bufferIndex] = pixel;
					this.mouseClickScriptRecords[bufferIndex].script = obj.mouseClickScript;
					this.mouseClickScriptRecords[bufferIndex].distance = obj.apparentDistance;
				}
				currentScreenY++;
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	private boolean hasAlpha(int pixel)
	{
		return ((pixel>>24) & 0xFF) < 0xFF;
	}

	/*-------------------------------------------------------------------------*/
	private byte getAlpha(int pixel)
	{
		return (byte)((pixel>>24) & 0xFF);
	}

	/*-------------------------------------------------------------------------*/
	private int alphaBlend(int background, int mask)
	{
		int alpha = (mask>>24) & 0xFF;

		if (alpha == 0xFF)
		{
			return mask;
		}
		else if (alpha == 0)
		{
			return background;
		}

		int backAlpha = (background>>24) & 0xFF;
		if (backAlpha == 0)
		{
			return mask;
		}

		int redMask =   (mask>>16) & 0xFF;
		int greenMask = (mask>>8) & 0xFF;
		int blueMask =  mask & 0xFF;

		int redBack =   (background>>16) & 0xFF;
		int greenBack = (background>>8) & 0xFF;
		int blueBack =  background & 0xFF;

		int alphaDiff = 0xFF - alpha;

		int red = (((redBack*alphaDiff)/255) + ((redMask*alpha)/255)) &0xFF;
		int green = ((((greenBack*alphaDiff)/255) + ((greenMask*alpha)/255))) &0xFF;
		int blue = ((((blueBack*alphaDiff)/255) + ((blueMask*alpha)/255))) &0xFF;
		int newAlpha = Math.min(alpha + backAlpha, 0xFF);

		return (newAlpha<<24) | ((red<<16) | (green<<8) | blue);
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
			this.map.initObjectFromTileIndex(obj, EngineObject.Placement.CENTER);
		}

		EngineObject[] new_array = new EngineObject[objects.length+1];
		System.arraycopy(objects, 0, new_array, 0, objects.length);
		new_array[objects.length] = obj;
		this.objects = new_array;

		for (Texture texture : obj.getTextures())
		{
			this.addTexture(texture);
		}
	}

	/*-------------------------------------------------------------------------*/

	public void initObjectInFrontOfPlayer(
		EngineObject obj, 
		double distance, 
		double arcOffset,
		boolean randomStartingFrame)
	{
		synchronized(objectMutex)
		{
			int dist = (int)(tileSize *distance);
			int arc = (int)(playerArc - playerFovHalf + arcOffset* playerFov);
			if (arc < 0)
			{
				arc += ANGLE360;
			}

			obj.currentTextureFrame =  r.nextInt(obj.currentTexture.nrFrames);

			int x = (int)(dist * cosTable[arc]);
			int y = (int)(dist * sinTable[arc]);
			
			obj.xPos = playerX + x;
			obj.yPos = playerY + y;
		}
	}

	/*-------------------------------------------------------------------------*/

	public void moveObjectToFrontOfPlayer(
		EngineObject obj,
		double distance,
		double arcOffset)
	{
		synchronized(objectMutex)
		{
			int dist = (int)(tileSize *distance);
			int arc = (int)(playerArc - playerFovHalf + arcOffset* playerFov);
			if (arc < 0)
			{
				arc += ANGLE360;
			}

			int x = (int)(dist * cosTable[arc]);
			int y = (int)(dist * sinTable[arc]);
			int textureOffset = 0;

			ObjectScript[] objectScripts = obj.removeAllScripts();
			obj.addScript(new MoveTo(playerX + x, playerY + y, textureOffset, 750, Arrays.asList(objectScripts)).spawnNewInstance(obj, this));
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
		timeNow = System.currentTimeMillis();

		for (Texture texture : textures)
		{
			if (texture.animationDelay > -1 && timeNow - texture.lastChanged >= texture.animationDelay)
			{
				texture.currentFrame++;
				if (texture.currentFrame >= texture.nrFrames)
				{
					texture.currentFrame = 0;
				}
				texture.lastChanged = timeNow;
			}
		}

		for (EngineObject object : objects)
		{
			//todo: multi-sided objects?
			Texture texture;
			if (object.currentTexture != null)
			{
				texture = object.currentTexture;
			}
			else
			{
				texture = object.northTexture;
			}

			if (texture.animationDelay > -1 && timeNow - object.textureLastChanged >= texture.animationDelay)
			{
				object.currentTextureFrame++;
				if (object.currentTextureFrame >= texture.nrFrames)
				{
					object.currentTextureFrame = 0;
				}
				object.textureLastChanged = timeNow;
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
		Texture[] textures;
		int textureXRecord;
		int textureYRecord;
		float distance;
		float projectedWallHeight;
		Wall wall;

		/**
		 * Either {@link CrusaderEngine32#HIT_VERTICAL} or {@link CrusaderEngine32#HIT_HORIZONTAL}
		 */
		byte hitType;

		public String toString()
		{
			return "blockHit=["+blockHit+"], " +
				"wall=["+wall+"]" +
				"distance=["+distance+"], " +
				"projectedWallHeight=["+projectedWallHeight+"], " +
				"hitType=["+hitType+"]"; 
		}

		public void populateFrom(BlockHitRecord other)
		{
			this.blockHit = other.blockHit;
			this.textures = other.textures;
			this.textureXRecord = other.textureXRecord;
			this.textureYRecord = other.textureYRecord;
			this.distance = other.distance;
			this.wall = other.wall;
			this.hitType = other.hitType;
		}
	}

	/*-------------------------------------------------------------------------*/
	private static class MouseClickScriptRecord
	{
		MouseClickScript script;
		double distance;
	}

	/*-------------------------------------------------------------------------*/
	private class DrawColumn implements Callable<Object>
	{
		private float castArc;
		private final int screenX;

		public DrawColumn(float castArc, int screenX)
		{
			this.castArc = castArc;
			this.screenX = screenX;
		}

		@Override
		public Object call()
		{
			try
			{
				rayCast(Math.round(castArc), screenX);

				for (int depth = 0; depth < maxHitDepth; depth++)
				{
					drawObjects(screenX, depth);
					if (!drawColumn(Math.round(castArc), screenX, depth, renderBuffer))
					{
						break;
					}
				}

				// render the sky
				for (int screenY=0; screenY < projectionPlaneHeight; screenY++)
				{
					int bufferIndex = screenX + screenY * projectionPlaneWidth;

					if (hasAlpha(renderBuffer[bufferIndex]))
					{
						renderBuffer[bufferIndex] = alphaBlend(
							getSkyPixel(Math.round(castArc), screenY),
							renderBuffer[bufferIndex]);
					}
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
				System.exit(-1);
			}

			return null;
		}
	}

	/*-------------------------------------------------------------------------*/
	private class FilterColumn implements Callable<Object>
	{
		private final int screenX;
		private final int[] outputBuffer;
		private PostProcessor filter;

		public FilterColumn(int screenX, int[] outputBuffer)
		{
			this.screenX = screenX;
			this.outputBuffer = outputBuffer;
		}

		@Override
		public Object call() throws Exception
		{
			filter.process(renderBuffer, outputBuffer, screenX);
			return null;
		}
	}
}
