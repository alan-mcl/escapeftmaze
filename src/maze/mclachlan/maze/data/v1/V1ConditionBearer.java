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

package mclachlan.maze.data.v1;

import java.awt.Point;
import java.util.*;
import mclachlan.maze.game.Maze;
import mclachlan.maze.map.Tile;
import mclachlan.maze.stat.PlayerCharacter;
import mclachlan.maze.stat.StatModifier;
import mclachlan.maze.stat.condition.ConditionBearer;
import mclachlan.maze.util.MazeException;

/**
 *
 */
public class V1ConditionBearer
{
	public static final String SEP = "~";

	public static final int PLAYER_CHARACTER = 1;
	public static final int TILE = 2;

	/*-------------------------------------------------------------------------*/
	public static String toString(ConditionBearer cb)
	{
		StringBuilder s = new StringBuilder();
		if (cb instanceof PlayerCharacter)
		{
			s.append(PLAYER_CHARACTER);
			s.append(SEP);
			s.append(cb.getName());
		}
		else if (cb instanceof Tile)
		{
			Tile tile = (Tile)cb;

			s.append(TILE);
			s.append(SEP);
			s.append(tile.getZone());
			s.append(SEP);
			s.append(V1Point.toString(tile.getCoords()));
		}
		else
		{
			// ignore, this condition won't be saved
//			throw new MazeException("invalid condition bearer: "+cb);
			return null;
		}

		return s.toString();
	}

	/*-------------------------------------------------------------------------*/
	public static ConditionBearer fromString(String s, String saveGameName,
		Map<String, PlayerCharacter> playerCharacterCache)
	{
		try
		{
			String[] strs = s.split(SEP, -1);

			int type = Integer.parseInt(strs[0]);
			String name = strs[1];
			Maze instance = Maze.getInstance();
			if (type == PLAYER_CHARACTER)
			{
				return playerCharacterCache.get(name);
			}
			else if (type == TILE)
			{
				Point coords = V1Point.fromString(strs[2]);
				if (instance != null && instance.getCurrentZone().getName().equals(name))
				{
					// a condition on a tile in the current zone
					return instance.getCurrentZone().getTile(coords);
				}
				else
				{
					// a condition on a tile in a zone that is not currently loaded
					// fake it
					Tile tile = new Tile(null, null, new StatModifier(),
						Tile.TerrainType.FAKE, "fake", 0,
						Tile.RestingDanger.NONE, Tile.RestingEfficiency.POOR);
					tile.setZone(name);
					tile.setCoords(coords);
					return tile;
				}
			}
			else
			{
				throw new MazeException("invalid type: "+type);
			}
		}
		catch (Exception e)
		{
			throw new MazeException(e);
		}
	}
}
