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
import mclachlan.maze.map.EncounterTable;
import mclachlan.maze.map.FoeEntry;
import mclachlan.maze.map.script.GrantExperienceEvent;
import mclachlan.maze.stat.*;
import mclachlan.maze.stat.combat.Combat;
import mclachlan.maze.stat.npc.Npc;
import mclachlan.maze.stat.npc.NpcManager;
import mclachlan.maze.util.MazeException;

/**
 *
 */
public class ActorDiesEvent extends MazeEvent
{
	private UnifiedActor victim;
	private UnifiedActor attacker;

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

		if (attacker != null && attacker.getModifier(Stats.Modifier.BERSERKER) > 0)
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

			// check for NOTORIETY kills on PCs
			if (victim instanceof PlayerCharacter &&
				attacker instanceof PlayerCharacter &&
				attacker.getModifier(Stats.Modifier.NOTORIETY) > 0)
			{
				result.add(
					new GrantExperienceEvent(
						getNotorietyReward(victim),
						(PlayerCharacter)attacker));
			}

			// check for SLIP_AWAY
			PlayerParty party = Maze.getInstance().getParty();
			Combat currentCombat = Maze.getInstance().getCurrentCombat();
			if (currentCombat != null && party.numAlive() == 1)
			{
				PlayerCharacter pc = party.getLivePlayerCharacters().get(0);
				if (pc.getModifier(Stats.Modifier.SLIP_AWAY) > 0)
				{
					result.add(new RunAwayAttemptEvent(pc, currentCombat));
				}
			}
		}

		// check for NOTORIETY kills on NPCs
		if (victim instanceof Npc &&
			attacker instanceof PlayerCharacter &&
			attacker.getModifier(Stats.Modifier.NOTORIETY) > 0)
		{
			result.add(
				new GrantExperienceEvent(
					getNotorietyReward(victim),
					(PlayerCharacter)attacker));
		}

		// check for REINCARNATE effects
		if (victim.getModifier(Stats.Modifier.REINCARNATE_BEAST) > 0)
		{
			EncounterTable table =
				Database.getInstance().getEncounterTable("reincarnate.beast");
			FoeEntry fe = table.getEncounterTable().getRandomItem();
			List<FoeGroup> foeGroups = fe.generate();

			result.add(new SummoningSucceedsEvent(foeGroups, victim));
		}

		victim.getHitPoints().setCurrent(0);

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public int getNotorietyReward(UnifiedActor victim)
	{
		int amount;

		if (victim instanceof Npc)
		{
			amount = ((Npc)victim).getExperience() / 4;
		}
		else if (victim instanceof PlayerCharacter)
		{
			amount = 100;
		}
		else
		{
			throw new MazeException("Invalid notoriety target: "+victim);
		}

		amount *= attacker.getModifier(Stats.Modifier.NOTORIETY);
		return amount;
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
