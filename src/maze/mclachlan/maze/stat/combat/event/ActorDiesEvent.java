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

package mclachlan.maze.stat.combat.event;

import java.util.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.game.MazeScript;
import mclachlan.maze.stat.*;
import mclachlan.maze.stat.npc.NpcManager;

/**
 *
 */
public class ActorDiesEvent extends MazeEvent
{
	UnifiedActor victim;
	UnifiedActor attacker;

	/*-------------------------------------------------------------------------*/
	/**
	 * @param victim
	 * 	the actor who dies
	 * @param attacker
	 * 	the attacked, or null if not applicable
	 */
	public ActorDiesEvent(UnifiedActor victim, UnifiedActor attacker)
	{
		this.victim = victim;
		this.attacker = attacker;
	}

	/*-------------------------------------------------------------------------*/
	public UnifiedActor getVictim()
	{
		return victim;
	}
	
	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> resolve()
	{
		List<MazeEvent> result = new ArrayList<MazeEvent>();

		final Maze maze = Maze.getInstance();
		// this check in case we're doing the CLI combat testing.
		if (maze != null)
		{
			MazeScript script = Database.getInstance().getScript("_DIE_");
			maze.appendEvents(script.getEvents());

			result.add(new MazeEvent()
			{
				@Override
				public List<MazeEvent> resolve()
				{
					maze.actorDies(victim);
					return null;
				}
			});
		}

		if (attacker instanceof PlayerCharacter)
		{
			((PlayerCharacter)attacker).incKills(1);

			result.addAll(SpeechUtil.getInstance().slayFoeSpeech((PlayerCharacter)attacker));
		}

		if (attacker != null && attacker.getModifier(Stats.Modifiers.BERSERKER) > 0)
		{
			if (GameSys.getInstance().actorGoesBeserk(attacker))
			{
				result.add(new BerserkEvent(attacker));
			}
		}

		if (victim instanceof Foe)
		{
			Foe foe = (Foe)victim;
			if (foe.isNpc())
			{
				NpcManager.getInstance().npcDies(foe);
			}
			if (foe.getDeathScript() != null)
			{
				result.addAll(foe.getDeathScript().getEvents());
			}
		}

		if (victim instanceof PlayerCharacter)
		{
			List<MazeEvent> speechEvents = SpeechUtil.getInstance().
				allyDiesSpeech((PlayerCharacter)victim);
			if (speechEvents != null)
			{
				result.addAll(speechEvents);
			}
		}

		victim.getHitPoints().setCurrent(0);

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public int getDelay()
	{
		return Maze.getInstance().getUserConfig().getCombatDelay();
	}

	/*-------------------------------------------------------------------------*/
	public String getText()
	{
		return getVictim().getDisplayName() + " DIES!";
	}
}
