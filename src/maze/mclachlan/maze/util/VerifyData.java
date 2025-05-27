/*
 * Copyright (c) 2013 Alan McLachlan
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
import mclachlan.maze.data.Loader;
import mclachlan.maze.data.Saver;
import mclachlan.maze.data.v2.V2Loader;
import mclachlan.maze.data.v2.V2Saver;
import mclachlan.maze.game.Maze;
import mclachlan.maze.map.Portal;
import mclachlan.maze.map.Tile;
import mclachlan.maze.map.TileScript;
import mclachlan.maze.map.Zone;
import mclachlan.maze.map.script.Chest;

/**
 *
 */
public class VerifyData
{
	public static void main(String[] args) throws Exception
	{
		Loader loader = new V2Loader();
		Saver saver = new V2Saver();
		Database db = new Database(loader, saver, Maze.getStubCampaign());

		System.out.println("Zones...");
		List<String> zoneNames = db.getZoneNames();
		for (String zn : zoneNames)
		{
			Zone zone = db.getZone(zn);

			System.out.println(zone.getName());

			// check Portal states
			for (Portal p : zone.getPortals())
			{
				// no maze variable means that the state must start unlocked
				if (p.getMazeVariable() == null || "".equals(p.getMazeVariable()))
				{
					if (!Portal.State.UNLOCKED.equals(p.getState()))
					{
						problem("Portal [" + p.getFrom() + " -> " + p.getTo() + "] " +
							"has no maze var but does not start unlocked");
					}
				}
			}


			// check Chest states
			for (Tile[] x : zone.getTiles())
				for (Tile tile : x)
				{
					for (TileScript ts : tile.getScripts())
					{
						if (ts instanceof Chest)
						{
							Chest chest = (Chest)ts;
							if (chest.getMazeVariable() == null || "".equals(chest.getMazeVariable()))
							{
								problem("Chest [" + tile.getCoords() + "] has no maze variable!");
							}
						}
					}
				}
		}
	}

	/*-------------------------------------------------------------------------*/
	private static void problem(String s)
	{
		System.out.println(" *** "+s);
	}
}
