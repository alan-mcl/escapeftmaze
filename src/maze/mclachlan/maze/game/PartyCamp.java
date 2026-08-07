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

package mclachlan.maze.game;

import java.awt.Point;
import java.util.*;

/**
 * A temporary camp where party members can be left behind while exploring.
 */
public class PartyCamp
{
	private String zone;
	private Point tile;
	private List<String> characterNames = new ArrayList<>();

	/*-------------------------------------------------------------------------*/
	public PartyCamp()
	{
	}

	/*-------------------------------------------------------------------------*/
	public PartyCamp(String zone, Point tile, List<String> characterNames)
	{
		this.zone = zone;
		this.tile = tile;
		this.characterNames = characterNames;
	}

	/*-------------------------------------------------------------------------*/
	public String getZone()
	{
		return zone;
	}

	/*-------------------------------------------------------------------------*/
	public void setZone(String zone)
	{
		this.zone = zone;
	}

	/*-------------------------------------------------------------------------*/
	public Point getTile()
	{
		return tile;
	}

	/*-------------------------------------------------------------------------*/
	public void setTile(Point tile)
	{
		this.tile = tile;
	}

	/*-------------------------------------------------------------------------*/
	public List<String> getCharacterNames()
	{
		return characterNames;
	}

	/*-------------------------------------------------------------------------*/
	public void setCharacterNames(List<String> characterNames)
	{
		this.characterNames = characterNames;
	}
}
