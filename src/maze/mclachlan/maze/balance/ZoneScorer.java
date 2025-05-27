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

package mclachlan.maze.balance;

import java.util.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.data.Loader;
import mclachlan.maze.data.Saver;
import mclachlan.maze.data.v2.V2Loader;
import mclachlan.maze.data.v2.V2Saver;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.game.MazeScript;
import mclachlan.maze.game.event.ZoneChangeEvent;
import mclachlan.maze.map.*;
import mclachlan.maze.stat.FoeTemplate;
import mclachlan.maze.stat.GroupOfPossibilities;
import mclachlan.maze.stat.PercentageTable;

/**
 *
 */
public class ZoneScorer
{
	private Database db;

	/*-------------------------------------------------------------------------*/
	public ZoneScore scoreZone(Zone zone, Database db) throws Exception
	{
		this.db = db;
		ZoneScore result = new ZoneScore();
		result.name = zone.getName();
		result.order = zone.getOrder();
		
		List<Tile> tiles = getNavigableTiles(zone);

		for (Tile t : tiles)
		{
			result.score += scoreTile(t);
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	private double scoreTile(Tile t) throws Exception
	{
		double encPerc = t.getRandomEncounterChance()/1000D;
		double encScore = scoreEncounters(t.getRandomEncounters());

		// todo: scripts, loot, etc

		return encScore * encPerc;
	}

	/*-------------------------------------------------------------------------*/
	private double scoreEncounters(EncounterTable et) throws Exception
	{
		double result = 0D;
		FoeScorer foeScorer = new FoeScorer(db);

		PercentageTable<FoeEntry> table = et.getEncounterTable();

		for (FoeEntry fe : table.getItems())
		{
			double foeEntryScore = 0D;
			double foeEntryPerc = table.getPercentage(fe)/100D;

			GroupOfPossibilities<FoeEntryRow> contains = fe.getContains();
			for (FoeEntryRow fer : contains.getPossibilities())
			{
				double foeEntryRowPerc = contains.getPercentage(fer)/100D;

				FoeTemplate ft = db.getFoeTemplate(fer.getFoeName());
				double foeScore = foeScorer.scoreFoe(ft);

				foeEntryScore += (foeScore * fer.getQuantity().getAverage() * foeEntryRowPerc);
			}

			result += (foeEntryScore * foeEntryPerc);
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public List<Tile> getNavigableTiles(Zone zone)
	{
		Tile startTile = zone.getTile(zone.getPlayerOrigin());
		List<Tile> result = new ArrayList<Tile>(2500);
		getNavigableTilesRecursive(zone, startTile, result);

		return result;
	}

	/*-------------------------------------------------------------------------*/
	private void getNavigableTilesRecursive(Zone zone, Tile t, List<Tile> result)
	{
		if (t == null)
		{
			return;
		}

		result.add(t);

		Tile west = zone.getTileRelativeTo(t, -1, 0, true);
		if (!result.contains(west))
		{
			getNavigableTilesRecursive(zone, west, result);
			walkThroughPortal(zone.getPortal(t.getCoords(), ZoneChangeEvent.Facing.WEST), zone, result);
		}

		Tile north = zone.getTileRelativeTo(t, 0, -1, true);
		if (!result.contains(north))
		{
			getNavigableTilesRecursive(zone, north, result);
			walkThroughPortal(zone.getPortal(t.getCoords(), ZoneChangeEvent.Facing.NORTH), zone, result);
		}

		Tile south = zone.getTileRelativeTo(t, 0, 1, true);
		if (!result.contains(south))
		{
			getNavigableTilesRecursive(zone, south, result);
			walkThroughPortal(zone.getPortal(t.getCoords(), ZoneChangeEvent.Facing.SOUTH), zone, result);
		}

		Tile east = zone.getTileRelativeTo(t, 1, 0, true);
		if (!result.contains(east))
		{
			getNavigableTilesRecursive(zone, east, result);
			walkThroughPortal(zone.getPortal(t.getCoords(), ZoneChangeEvent.Facing.EAST), zone, result);
		}

		// todo: secret passages, teleporters, etc
	}

	/*-------------------------------------------------------------------------*/
	private void walkThroughPortal(Portal portal, Zone zone, List<Tile> result)
	{
		if (portal != null)
		{
			String ms = portal.getMazeScript();
			if (ms != null)
			{
				MazeScript mazeScript = Database.getInstance().getMazeScripts().get(ms);

				for (MazeEvent e : mazeScript.getEvents())
				{
					if (e instanceof ZoneChangeEvent)
					{
						// todo: are other event types required here?
						return;
					}
				}
			}

			Tile tile = zone.getTile(portal.getTo());
			if (!result.contains(tile))
			{
				getNavigableTilesRecursive(zone, tile, result);
			}
			tile = zone.getTile(portal.getFrom());
			if (!result.contains(tile))
			{
				getNavigableTilesRecursive(zone, tile, result);
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	public static void main(String[] args) throws Exception
	{
		Loader loader = new V2Loader();
		Saver saver = new V2Saver();
		Database db = new Database(loader, saver, Maze.getStubCampaign());

		List<String> zones = db.getZoneNames();

		ZoneScorer s = new ZoneScorer();
		List<ZoneScore> zoneScores = new ArrayList<ZoneScore>();

		for (String z : zones)
		{
			try
			{
				Zone zone = db.getZone(z);
				zoneScores.add(s.scoreZone(zone, db));
			}
			catch (Exception e)
			{
				System.out.println("!!! " + z);
				throw e;
			}
		}

		Collections.sort(zoneScores, new Comparator<ZoneScore>()
		{
			public int compare(ZoneScore o1, ZoneScore o2)
			{
				return o1.order - o2.order;
			}
		});

		for (ZoneScore zs : zoneScores)
		{
			System.out.println(zs);
		}

	}

	/*-------------------------------------------------------------------------*/
	private class ZoneScore
	{
		String name;
		double score;
		int order;

		@Override
		public String toString()
		{
			final StringBuilder sb = new StringBuilder();
			sb.append(name);
			sb.append(",").append(order);
			sb.append(",").append(score);
			return sb.toString();
		}
	}
}
