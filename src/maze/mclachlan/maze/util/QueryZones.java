/*
 * Copyright (c) 2012 Alan McLachlan
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

package mclachlan.maze.util;

import java.util.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.data.v1.V1Loader;
import mclachlan.maze.data.v1.V1Saver;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.game.MazeScript;
import mclachlan.maze.game.event.CharacterClassKnowledgeEvent;
import mclachlan.maze.map.Tile;
import mclachlan.maze.map.TileScript;
import mclachlan.maze.map.Zone;
import mclachlan.maze.map.script.ExecuteMazeScript;

/**
 *
 */
public class QueryZones
{

	private static V1Saver saver;

	public static void main(String[] args) throws Exception
	{
		System.out.println("...");

		V1Loader loader = new V1Loader();
		saver = new V1Saver();
		Database db = new Database(loader, saver, Maze.getStubCampaign());

		List<String> zones = db.getZoneNames();

		for (String s : zones)
		{
			Zone z = db.getZone(s);

			Tile[][] tiles = z.getTiles();
			for (int i = 0, tilesLength = tiles.length; i < tilesLength; i++)
			{
				Tile[] x = tiles[i];
				for (int j = 0, xLength = x.length; j < xLength; j++)
				{
					Tile y = x[j];
					if (y.getScripts() != null && !y.getScripts().isEmpty())
					{
						for (TileScript ms : y.getScripts())
						{
							if (ms instanceof ExecuteMazeScript)
							{
								MazeScript script = Database.getInstance().getMazeScripts().get(
									((ExecuteMazeScript)ms).getMazeScript());

								for (MazeEvent me : script.getEvents())
								{
									if (me instanceof CharacterClassKnowledgeEvent)
									{
										System.out.println(s + ": "+i+","+j);
									}
								}
							}
						}
					}
				}
			}
		}
	}
}