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
import mclachlan.maze.data.Database;
import mclachlan.maze.data.v2.V2Loader;
import mclachlan.maze.data.v2.V2Saver;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.stat.npc.NpcSpeech;
import mclachlan.maze.stat.npc.NpcSpeechEvent;
import mclachlan.maze.stat.npc.NpcSpeechRow;
import mclachlan.maze.stat.npc.NpcTemplate;

/**
 *
 */
public class NpcSpeechReport
{
	public static void main(String[] args) throws Exception
	{
		String name = args[0];
		System.out.println("...");

		Database db = new Database(new V2Loader(), new V2Saver(), Maze.getStubCampaign());
		db.initImpls();
		db.initCaches(null);

		NpcTemplate npc = db.getNpcTemplates().get(name);
		NpcSpeech dialogue = npc.getDialogue();

		Report<NpcSpeechRow> report = new Report<>("\t");
		report.addColumn("Key Words", NpcSpeechRow::getKeywords);
		report.addColumn("Speech", npcSpeechRow -> '"'+npcSpeechRow.getSpeech()+'"');

		List<NpcSpeechRow> list = new ArrayList<>(dialogue.getDialogue());
		list.sort(Comparator.comparing(NpcSpeechRow::getSpeech));

		getNpcSpeechRow(npc, "First Greeting", null, npc.getScript().firstGreeting());
		getNpcSpeechRow(npc, "Friendly Greeting", npc.getDialogue().getFriendlyGreeting(), npc.getScript().friendlyGreeting());
		getNpcSpeechRow(npc, "Neutral Greeting", npc.getDialogue().getNeutralGreeting(), npc.getScript().neutralGreeting());
		getNpcSpeechRow(npc, "Party Leaves Neutral", npc.getDialogue().getNeutralFarewell(), npc.getScript().partyLeavesNeutral());
		getNpcSpeechRow(npc, "Friendly Farewell", npc.getDialogue().getFriendlyFarewell(), npc.getScript().partyLeavesFriendly());
		getNpcSpeechRow(npc, "Party Can't Afford Item", npc.getDialogue().getPartyCantAffordItem(), null);
		getNpcSpeechRow(npc, "Character Inventory Full", npc.getDialogue().getCharacterInventoryFull(), null);
		getNpcSpeechRow(npc, "Not Interested In Buying Item", npc.getDialogue().getNotInterestedInBuyingItem(), null);
		getNpcSpeechRow(npc, "Can't Afford To Buy Item", npc.getDialogue().getCantAffordToBuyItem(), null);
		getNpcSpeechRow(npc, "NPC Inventory Full", npc.getDialogue().getNpcInventoryFull(), null);
		getNpcSpeechRow(npc, "Doesn't Want Item", npc.getDialogue().getDoesntWantItem(), null);
		getNpcSpeechRow(npc, "Doesn't Know About", npc.getDialogue().getDoesntKnowAbout(), null);

		report.print(list);
	}

	private static NpcSpeechRow getNpcSpeechRow(NpcTemplate npc, String key, String str,
		List<MazeEvent> mazeEvents)
	{
		StringBuilder speech = new StringBuilder();
		if (str == null || str.isEmpty())
		{
			if (mazeEvents != null && !mazeEvents.isEmpty())
			{
				for (MazeEvent me : mazeEvents)
				{
					if (me instanceof NpcSpeechEvent)
					{
						speech.append(((NpcSpeechEvent)me).getSpeechText());
					}
				}
			}
		}
		else
		{
			speech = new StringBuilder(str);
		}

		return new NpcSpeechRow(0, new HashSet<>(Collections.singletonList(key)), speech.toString());
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