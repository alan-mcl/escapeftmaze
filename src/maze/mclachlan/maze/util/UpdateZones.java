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
import mclachlan.maze.data.Saver;
import mclachlan.maze.data.v2.V2Loader;
import mclachlan.maze.data.v2.V2Saver;
import mclachlan.maze.game.Maze;
import mclachlan.maze.map.Zone;

/**
 * loads every zone then saves it
 */
public class UpdateZones
{
	public static void main(String[] args) throws Exception
	{
		Database db = new Database(new V2Loader(), new V2Saver(), Maze.getStubCampaign());

		db.initImpls();
		db.initCaches(null);

		if (args.length > 0)
		{
			String zoneName = args[0];
			updateZone(db.getSaver(), db, zoneName);
		}
		else
		{
			List<String> zones = db.getZoneNames();

			for (String zoneName : zones)
			{
				//			Zone z1 = dbv1.getZone(zoneName);
				updateZone(db.getSaver(), db, zoneName);
			}
		}
	}

	private static void updateZone(Saver saver, Database db, String zoneName) throws Exception
	{
		Zone z2 = db.getZone(zoneName);

		System.out.print(z2.getName() + " ");

		saver.saveZone(z2);
		System.out.println("done!");
	}
}