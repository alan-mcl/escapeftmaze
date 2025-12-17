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

package mclachlan.maze.campaign.def.npc;

import java.util.*;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.map.script.FlavourTextEvent;
import mclachlan.maze.stat.PlayerCharacter;
import mclachlan.maze.stat.UnifiedActor;
import mclachlan.maze.stat.npc.ActorsLeaveEvent;
import mclachlan.maze.stat.npc.NpcScript;
import mclachlan.maze.stat.npc.NpcSpeechEvent;

/**
 * Vendor in Aenen city, tobacconist
 */
public class Pandarus extends NpcScript
{
	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> preAppearance()
	{
		return getList(
			new FlavourTextEvent("A small silver bell tinkles as you enter " +
				"the finely attired shop. Polished wooden shelves stoked with neatly " +
				"packaged and labelled bundles line the walls, and your feet " +
				"sink into the plushly carpeted floor.\n\n" +
				"A well-fed gnome in expensive red robes looks up from " +
				"behind a glass-topped counter and smiles.",
				MazeEvent.Delay.WAIT_ON_CLICK, true));
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> firstGreeting()
	{
		boolean hasMale = false;
		boolean hasFemale = false;
		for (UnifiedActor a : getParty().getActors())
		{
			PlayerCharacter pc = (PlayerCharacter)a;
			if (pc.getGender().getName().equalsIgnoreCase("male"))
			{
				hasMale = true;
			}
			else if (pc.getGender().getName().equalsIgnoreCase("female"))
			{
				hasFemale = true;
			}
		}

		String greeting;
		if (hasMale && !hasFemale)
		{
			greeting = "Gentlemen, gentlemen";
		}
		else if (!hasMale & hasFemale)
		{
			greeting = "Fair ladies";
		}
		else
		{
			greeting = "Ladies and gentlemen";
		}

		return getList(
			new NpcSpeechEvent(greeting+"... welcome! What fine " +
				"pipe weed or other fine wares are you seeking?", npc),
			new NpcSpeechEvent("I am Pandarus Lang Mac Vulane. You have no doubt heard of " +
				"the quality of my products throughout the Second Realm.", npc));
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> friendlyGreeting()
	{
		return firstGreeting();
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> neutralGreeting()
	{
		return getList(
			new NpcSpeechEvent("I am at your service.", npc));
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> partyLeavesNeutral()
	{
		return getList(
			new NpcSpeechEvent("Farewell.", npc),
			new ActorsLeaveEvent());
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> partyLeavesFriendly()
	{
		return getList(
			new NpcSpeechEvent("Fare ye well. Return again soon!", npc),
			new ActorsLeaveEvent());
	}
}