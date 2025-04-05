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
//		Database dbv1 = new Database(new V1Loader(), new V1Saver(), Maze.getStubCampaign());
		V2Saver v2Saver = new V2Saver();
		Database dbv2 = new Database(new V2Loader(), v2Saver, Maze.getStubCampaign());

//		dbv1.initImpls();
//		dbv1.initCaches(null);

		dbv2.initImpls();
		dbv2.initCaches(null);

		List<String> zones = dbv2.getZoneNames();

		for (String zoneName : zones)
		{
//			Zone z1 = dbv1.getZone(zoneName);

			Zone z2 = dbv2.getZone(zoneName);

			System.out.print(z2.getName()+" ");

			v2Saver.saveZone(z2);
			System.out.println("done!");
		}
	}
}