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

import mclachlan.maze.map.Tile;
import mclachlan.maze.map.TileScript;
import mclachlan.maze.map.EncounterTable;
import mclachlan.maze.stat.StatModifier;
import mclachlan.maze.data.Database;
import java.util.*;

/**
 *
 */
public class V1Tile
{
	public static final String SEP = ":";

	/*-------------------------------------------------------------------------*/
	public static String toString(Tile t)
	{
		if (t == null)
		{
			return "";
		}

		StringBuilder s = new StringBuilder();

		List<TileScript> scripts = t.getScripts();

		s.append(scripts==null?0:scripts.size());
		s.append(SEP);
		if (scripts != null)
		{
			for (TileScript script : scripts)
			{
				s.append(V1TileScript.toString(script));
				s.append(SEP);
			}
		}
		s.append(V1StatModifier.toString(t.getStatModifier()));
		s.append(SEP);
		s.append(t.getTerrainType());
		s.append(SEP);
		s.append(t.getTerrainSubType());
		s.append(SEP);
		s.append(t.getRandomEncounterChance());
		s.append(SEP);
		s.append(t.getRandomEncounters().getName());
		s.append(SEP);
		s.append(t.getRestingDanger().name());
		s.append(SEP);
		s.append(t.getRestingEfficiency().name());

		return s.toString();
	}

	/*-------------------------------------------------------------------------*/
	public static Tile fromString(String s)
	{
		if (s.equals(""))
		{
			return null;
		}

		List<TileScript> scripts = new ArrayList<TileScript>();

		String[] strs = s.split(SEP);

		int i=0;
		int nrScripts = Integer.parseInt(strs[i++]);

		for (int j=0; j<nrScripts; j++)
		{
			TileScript tileScript = V1TileScript.fromString(strs[i++]);
			scripts.add(tileScript);
		}

		StatModifier modifier = V1StatModifier.fromString(strs[i++]);
		Tile.TerrainType terrainType = Tile.TerrainType.valueOf(strs[i++]);
		String terrainSubType = strs[i++];
		int randomEncounterChance = Integer.parseInt(strs[i++]);
		EncounterTable encounters = Database.getInstance().getEncounterTable(strs[i++]);
		Tile.RestingDanger restingDanger = Tile.RestingDanger.valueOf(strs[i++]);
		Tile.RestingEfficiency restingEfficiency = Tile.RestingEfficiency.valueOf(strs[i++]);

		return new Tile(
			scripts,
			encounters,
			modifier,
			terrainType,
			terrainSubType,
			randomEncounterChance,
			restingDanger,
			restingEfficiency);
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * for testing only
	 */
	public static void main(String[] args)
	{
	}
}
