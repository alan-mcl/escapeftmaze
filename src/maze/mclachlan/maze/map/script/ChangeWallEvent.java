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
import mclachlan.crusader.MouseClickScript;
import mclachlan.crusader.Texture;
import mclachlan.crusader.Wall;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;

/**
 *
 */
public class ChangeWallEvent extends MazeEvent
{
	private final boolean horizontalWall;
	private final int wallIndex;

	private final Texture texture;
	private final Texture maskTexture;
	private final boolean visible;
	private final boolean solid;
	private final int height;
	private final MouseClickScript mouseClickScript;
	private final MouseClickScript maskTextureMouseClickScript;

	/*-------------------------------------------------------------------------*/
	public ChangeWallEvent(
		String mazeVariable,
		boolean horizontalWall,
		int wallIndex,
		Texture texture,
		Texture maskTexture,
		boolean visible,
		boolean solid,
		int height,
		MouseClickScript mouseClickScript,
		MouseClickScript maskTextureMouseClickScript)
	{
		this.texture = texture;
		this.maskTexture = maskTexture;
		this.visible = visible;
		this.solid = solid;
		this.height = height;
		this.mouseClickScript = mouseClickScript;
		this.maskTextureMouseClickScript = maskTextureMouseClickScript;
		this.horizontalWall = horizontalWall;
		this.wallIndex = wallIndex;
	}
	
	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> resolve()
	{
		Wall wall = new Wall(texture, maskTexture, visible, solid, height, mouseClickScript, maskTextureMouseClickScript, null);

		Maze.getInstance().getCurrentZone().getMap().setWall(wallIndex, horizontalWall, wall);

		return null;
	}
	
	/*-------------------------------------------------------------------------*/
	public boolean isHorizontalWall()
	{
		return horizontalWall;
	}

	public int getWallIndex()
	{
		return wallIndex;
	}

	public Texture getTexture()
	{
		return texture;
	}

	public Texture getMaskTexture()
	{
		return maskTexture;
	}

	public boolean isVisible()
	{
		return visible;
	}

	public boolean isSolid()
	{
		return solid;
	}

	public int getHeight()
	{
		return height;
	}

	public MouseClickScript getMouseClickScript()
	{
		return mouseClickScript;
	}

	public MouseClickScript getMaskTextureMouseClickScript()
	{
		return maskTextureMouseClickScript;
	}
}
