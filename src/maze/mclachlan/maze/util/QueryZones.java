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
import mclachlan.maze.data.v2.V2Saver;
import mclachlan.maze.data.v2.V2Loader;
import mclachlan.maze.data.v2.V2Saver;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeScript;
import mclachlan.maze.map.HiddenStuff;
import mclachlan.maze.map.TileScript;
import mclachlan.maze.map.Zone;
import mclachlan.maze.map.script.*;

/**
 *
 */
public class QueryZones
{

	private static V2Saver saver;

	public static void main(String[] args) throws Exception
	{
		System.out.println("...");

		Database db = new Database(new V2Loader(), new V2Saver(), Maze.getStubCampaign());

		db.initImpls();
		db.initCaches(null);

		List<String> zones = db.getZoneNames();

		for (String s : zones)
		{
			Zone z = db.getZone(s);

			List<TileScript> allTileScripts = z.getAllTileScripts();

			for (TileScript ts : allTileScripts)
			{
				if (ts instanceof Encounter)
				{
					if (((Encounter)ts).getPreScriptEvents() != null)
					{
						System.out.println(((Encounter)ts).getPreScriptEvents().getName());
					}
					if (((Encounter)ts).getPostAppearanceScriptEvents() != null)
					{
						System.out.println(((Encounter)ts).getPostAppearanceScriptEvents().getName());
					}
				}
				else if (ts instanceof Chest)
				{
					if (((Chest)ts).getPreScript() != null)
					{
						System.out.println(((Chest)ts).getPreScript().getName());
					}
				}
				else if (ts instanceof HiddenStuff)
				{
					if (((HiddenStuff)ts).getPreScript() != null)
					{
						System.out.println(((HiddenStuff)ts).getPreScript().getName());
					}
					if (((HiddenStuff)ts).getContent() != null)
					{
						System.out.println(((HiddenStuff)ts).getContent().getName());
					}
				}
				else if (ts instanceof ExecuteMazeScript)
				{
					if (((ExecuteMazeScript)ts).getScript() != null)
					{
						System.out.println(((ExecuteMazeScript)ts).getScript().getName());
					}
				}
				else if (ts instanceof Lever)
				{
					if (((Lever)ts).getPreTransitionScript() != null)
					{
						System.out.println(((Lever)ts).getPreTransitionScript().getName());
					}
					if (((Lever)ts).getPostTransitionScript() != null)
					{
						System.out.println(((Lever)ts).getPostTransitionScript().getName());
					}
				}
				else if (ts instanceof DisplayOptions)
				{
					for (MazeScript ms : ((DisplayOptions)ts).getMazeScripts())
					{
						System.out.println(ms.getName());
					}
				}
			}
		}
	}
}