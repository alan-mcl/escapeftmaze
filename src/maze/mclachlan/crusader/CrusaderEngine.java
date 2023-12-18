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

import java.util.List;
import java.awt.*;
import java.awt.image.ColorModel;

/**
 */
public interface CrusaderEngine
{
	/**
	 *  The maximum light level
	 */
	byte MAX_LIGHT_LEVEL = 64;

	/**
	 * The normal light level. Pixels rendered at this light level will have
	 * the same colour as in their source texture.
	 */
	byte NORMAL_LIGHT_LEVEL = 32;

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
	 * 	A constant from {@link Facing} determining which way the player is
	 * 	initially facing.
	 */
	void setPlayerPos(int x, int y, int facing);

	/*-------------------------------------------------------------------------*/
	/**
	 * @return
	 * 	The X and Y coordinates of the player in the map grid (not in the
	 * 	ray caster's internal coordiantes)
	 */
	Point getPlayerPos();

	/*-------------------------------------------------------------------------*/
	/**
	 * @return
	 * 	In discrete mode, returns a constant from {@link Facing}.  In
	 * 	continuous mode, returns the player's facing
	 */
	int getPlayerFacing();

	/*-------------------------------------------------------------------------*/

	/**
	 * @return
	 * 	The tile size in pixels
	 */
	int getTileSize();

	/*-------------------------------------------------------------------------*/
	/**
	 * Apply the given keystroke to the engine state.
	 *
	 * @param key
	 * 	the key code, a constant from {@link KeyStroke}
	 */
	void handleKey(int key);

	/*-------------------------------------------------------------------------*/
	/**
	 * Apply the given keystroke to the engine state.
	 *
	 * @param prediction
	 * 	the predicted player status
	 */
	void handleKey(PlayerStatus prediction);

	/*-------------------------------------------------------------------------*/
	/**
	 * Return what would happen if the given key is pressed.
	 *
	 * @param key
	 * 	the key code, a constant from {@link KeyStroke}
	 * @return
	 * 	the player position and facing that would result
	 */
	PlayerStatus predictKey(int key);

	/*-------------------------------------------------------------------------*/
	/**
	 * Render the scene and return it as an Image.
	 */
	Image render();

	/*-------------------------------------------------------------------------*/
	/**
	 * Removes the given object from the map and returns it.
	 * If the object does not exist in the map this method returns null.
	 */
	EngineObject removeObject(EngineObject obj);

	/*-------------------------------------------------------------------------*/
	/**
	 * Removes all objects with the given name from the map and returns a list
	 * of them.
	 * If no objects exist in the map with this name this method returns an
	 * empty list.
	 */
	List<EngineObject> removeObject(String objectName);

	/*-------------------------------------------------------------------------*/
	/**
	 * Adds the given object to the scene.  The objects coordinates are required
	 * to be set up.
	 */
	void addObject(EngineObject obj);

	/*-------------------------------------------------------------------------*/
	void addObject(EngineObject obj, boolean setup);

	/*-------------------------------------------------------------------------*/
	/**
	 * Initialises the object in front of the player, but does not add it yet.
	 *
	 * @param obj
	 * 	The object to add
	 * @param distance
	 * 	The distance in front of the player, expressed as a multiple of tileSize.
	 * @param arcOffset
	 * 	The angle of view to add, expressed as a fraction of the PLAYER_FOV.
	 * 	0.0 will add the object on the left edge of the screen, 1.0 will add
	 * 	it on the right edge.
	 * @param randomStartingFrame
	 * 	Set to true if the object begins with a random starting frame of
	 * 	animation
	 */
	void initObjectInFrontOfPlayer(
		EngineObject obj,
		double distance,
		double arcOffset,
		boolean randomStartingFrame);

	void moveObjectToFrontOfPlayer(
		EngineObject obj,
		double distance,
		double arcOffset);

	/*-------------------------------------------------------------------------*/
	void addScript(MapScript script);

	/*-------------------------------------------------------------------------*/
	MapScript removeScript(MapScript script);

	/*-------------------------------------------------------------------------*/
	ColorModel getColourModel();

	/*-------------------------------------------------------------------------*/
	void addTexture(Texture texture);

	/*-------------------------------------------------------------------------*/
	/**
	 * X and Y coords are in screen coords: ie, (0,0) is the top left of the
	 * maze windows, position [0] in the render buffer
	 */
	void handleMouseClick(int x, int y);

	/*-------------------------------------------------------------------------*/

	/**
	 * X and Y coords are in screen coords: ie, (0,0) is the top left of the
	 * maze windows, position [0] in the render buffer.
	 *
	 * @return the script under the click, or null if there is none. It's up
	 * to the caller to decide whether to execute it.
	 */
	MouseClickScript handleMouseClickReturnScript(int x, int y);

	/*-------------------------------------------------------------------------*/
	class PlayerStatus
	{
		public Point position;
		public int facing;
		public boolean willPassThroughWall;

		int playerX;
		int playerY;
		int playerArc;

		public PlayerStatus(
			int playerX,
			int playerY,
			int playerArc,
			Point position,
			int facing,
			boolean willPassThroughWall)
		{
			this.playerX = playerX;
			this.playerY = playerY;
			this.playerArc = playerArc;
			this.facing = facing;
			this.position = position;
			this.willPassThroughWall = willPassThroughWall;
		}
	}

	/*-------------------------------------------------------------------------*/
	class FieldOfView
	{
		public static final int FOV_30_DEGREES = 1;
		public static final int FOV_60_DEGREES = 2;
		public static final int FOV_90_DEGREES = 3;
		public static final int FOV_180_DEGREES = 4;
	}

	/*-------------------------------------------------------------------------*/
	class MovementMode
	{
		public static final int CONTINUOUS = 1;
		public static final int DISCRETE = 2;
		public static final int OCTO = 3;
	}

	/*-------------------------------------------------------------------------*/
	class Facing
	{
		public static final int NORTH = 1;
		public static final int SOUTH = 2;
		public static final int EAST = 3;
		public static final int WEST = 4;

		public static final int NORTH_EAST = 5;
		public static final int NORTH_WEST = 6;
		public static final int SOUTH_EAST = 7;
		public static final int SOUTH_WEST = 8;
	}

	/*-------------------------------------------------------------------------*/
	class KeyStroke
	{
		public static final int FORWARD = 2;
		public static final int BACKWARD = 3;
		public static final int TURN_LEFT = 4;
		public static final int TURN_RIGHT = 5;
		public static final int STRAFE_LEFT = 6;
		public static final int STRAFE_RIGHT = 7;
	}

	/*-------------------------------------------------------------------------*/
	enum Filter
	{
		NONE,
		DEFAULT,
		SMOOTH,
		SHARPEN,
		RAISED,
		MOTION_BLUR,
		EDGE_DETECT,
		EMBOSS,
		FXAA,
		WIREFRAME,
		GREYSCALE,
	}
}
