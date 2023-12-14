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

package mclachlan.maze.map.script;

import java.util.*;
import mclachlan.crusader.Texture;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.game.MazeVariables;
import mclachlan.maze.util.MazeException;

/**
 *
 */
public class ToggleWallEvent extends MazeEvent
{
	private final boolean horizontalWall;
	private final int wallIndex;

	private final String mazeVariable;

	// state 1 wall attributes
	private final Texture state1Texture;
	private final Texture state1MaskTexture;
	private final boolean state1Visible;
	private final boolean state1Solid;
	private final int state1Height;

	// state 2 wall attributes
	private final Texture state2Texture;
	private final Texture state2MaskTexture;
	private final boolean state2Visible;
	private final boolean state2Solid;
	private final int state2Height;

	/*-------------------------------------------------------------------------*/
	public ToggleWallEvent(
		String mazeVariable,
		int wallIndex,
		boolean horizontalWall,
		Texture state1Texture,
		Texture state1MaskTexture,
		boolean state1Visible,
		boolean state1Solid,
		int state1Height,
		Texture state2Texture,
		Texture state2MaskTexture,
		boolean state2Visible,
		boolean state2Solid,
		int state2Height)
	{
		this.mazeVariable = mazeVariable;
		this.horizontalWall = horizontalWall;
		this.wallIndex = wallIndex;
		this.state1Texture = state1Texture;
		this.state1MaskTexture = state1MaskTexture;
		this.state1Visible = state1Visible;
		this.state1Solid = state1Solid;
		this.state1Height = state1Height;
		this.state2Texture = state2Texture;
		this.state2MaskTexture = state2MaskTexture;
		this.state2Visible = state2Visible;
		this.state2Solid = state2Solid;
		this.state2Height = state2Height;
	}
	
	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> resolve()
	{
		switch (ToggleWall.State.valueOf(MazeVariables.get(this.mazeVariable)))
		{
			case STATE_1:
				MazeVariables.set(this.mazeVariable, ToggleWall.State.STATE_2.name());
				break;
			case STATE_2:
				MazeVariables.set(this.mazeVariable, ToggleWall.State.STATE_1.name());
				break;
			default:
				throw new MazeException("invalid state: "+MazeVariables.get(this.mazeVariable));
		}

		ToggleWall.setWallAttributes(
			Maze.getInstance(),
			mazeVariable,
			wallIndex,
			horizontalWall,
			state1Texture,
			state1MaskTexture,
			state1Visible,
			state1Solid,
			state1Height,
			state2Texture,
			state2MaskTexture,
			state2Visible,
			state2Solid,
			state2Height);


//		Wall wall = new Wall(texture, maskTexture, visible, solid, height, mouseClickScript, maskTextureMouseClickScript, null);
//
//		Maze.getInstance().getCurrentZone().getMap().setWall(wallIndex, horizontalWall, wall);

		return null;
	}
}
