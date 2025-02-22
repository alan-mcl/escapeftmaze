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
import java.util.function.*;
import java.util.stream.*;
import mclachlan.maze.balance.FoeScorer;
import mclachlan.maze.data.Database;
import mclachlan.maze.data.v1.V1Loader;
import mclachlan.maze.data.v1.V1Saver;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.game.MazeScript;
import mclachlan.maze.map.*;
import mclachlan.maze.map.script.Encounter;
import mclachlan.maze.map.script.EncounterActorsEvent;
import mclachlan.maze.map.script.ExecuteMazeScript;
import mclachlan.maze.stat.FoeTemplate;

/**
 *
 */
public class ZoneReport
{

	private static V1Saver saver;

	public static void main(String[] args) throws Exception
	{
		String zoneName = args[0];
		System.out.println("...");

		V1Loader loader = new V1Loader();
		saver = new V1Saver();
		Database db = new Database(loader, saver, Maze.getStubCampaign());

		Zone z = db.getZone(zoneName);

		// get unique foes in this zone
		Set<FoeTemplate> foes = new HashSet<>();

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
								if (me instanceof EncounterActorsEvent)
								{
									getUniqueFoes(db, foes, ((EncounterActorsEvent)me).getEncounterTable());
								}
							}
						}
						else if (ms instanceof Encounter)
						{
							getUniqueFoes(db, foes, ((Encounter)ms).getEncounterTable().getName());
						}
					}
				}
			}
		}

		// enumerate foes
		FoeScorer foeScorer = new FoeScorer(db);

		Report<FoeTemplate> report = new Report<>("\t");
		report.addColumn("Name", FoeTemplate::getName);
		report.addColumn("Level", FoeTemplate::getLevelRange);
		report.addColumn("HP", FoeTemplate::getHitPointsRange);
		report.addColumn("AP", FoeTemplate::getActionPointsRange);
		report.addColumn("MP", FoeTemplate::getMagicPointsRange);
		report.addColumn("Avg dam/round", foeTemplate -> {
			try
			{
				return foeScorer.scoreFoe(foeTemplate);
			}
			catch (Exception e)
			{
				throw new RuntimeException(e);
			}
		});

		List<FoeTemplate> foesList = new ArrayList<>(foes);
		foesList.sort(Comparator.comparing(FoeTemplate::getName));

		report.print(foesList);
	}

	/*-------------------------------------------------------------------------*/
	private static void getUniqueFoes(Database db, Set<FoeTemplate> foes, String et)
	{
		EncounterTable encounterTable = db.getEncounterTable(et);

		for (FoeEntry fe : encounterTable.getEncounterTable().getItems())
		{
			for (FoeEntryRow fer : fe.getContains().getPossibilities())
			{
				foes.add(db.getFoeTemplates().get(fer.getFoeName()));
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	static class Report<T>
	{
		private final List<Column<T>> columns = new ArrayList<>();
		private final String delimiter;

		public Report(String delimiter)
		{
			this.delimiter = delimiter;
		}

		public void addColumn(String name, Function<T, ?> function)
		{
			columns.add(new Column<T>(name, function));
		}

		public void print(List<T> list)
		{
			System.out.println(columns.stream().map(
				Column::getName).collect(Collectors.joining(delimiter)));

			for (T t : list)
			{
				System.out.println(columns.stream().map(col -> col.print(t).toString()).
					collect(Collectors.joining(delimiter)));
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	static class Column<T>
	{
		private final String name;
		private final Function<T, ?> function;

		public Column(String name, Function<T, ?> function)
		{
			this.name = name;
			this.function = function;
		}

		public String getName()
		{
			return name;
		}

		public Object print(T t)
		{
			return function.apply(t);
		}
	}
}