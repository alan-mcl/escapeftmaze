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
import mclachlan.maze.data.StringUtil;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.game.MazeScript;
import mclachlan.maze.game.event.UiMessageEvent;
import mclachlan.maze.map.EncounterTable;
import mclachlan.maze.map.FoeEntry;
import mclachlan.maze.map.script.GrantExperienceEvent;
import mclachlan.maze.stat.*;
import mclachlan.maze.stat.combat.ActorActionResolver;
import mclachlan.maze.stat.combat.Combat;
import mclachlan.maze.stat.combat.SpellAction;
import mclachlan.maze.stat.magic.Spell;
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
		final Maze maze = Maze.getInstance();
		List<MazeEvent> result = new ArrayList<MazeEvent>();

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

		// Trigger any PC speech
		if (attacker instanceof PlayerCharacter)
		{
			((PlayerCharacter)attacker).incKills(1);

			result.addAll(SpeechUtil.getInstance().slayFoeSpeech((PlayerCharacter)attacker));
		}

		// a Berserker slaying an enemy has a chance of going berserk
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

			// check whether an NPC has been killed
			if (foe.isNpc())
			{
				NpcManager.getInstance().npcDies(foe);
			}

			// trigger any foe death script
			if (foe.getDeathScript() != null)
			{
				result.addAll(foe.getDeathScript().getEvents());
			}
		}

		if (victim instanceof PlayerCharacter)
		{
			PlayerParty party = Maze.getInstance().getParty();
			Combat currentCombat = Maze.getInstance().getCurrentCombat();

			// Trigger any PC speech
			List<MazeEvent> speechEvents = SpeechUtil.getInstance().
				allyDiesSpeech((PlayerCharacter)victim);
			if (speechEvents != null)
			{
				result.addAll(speechEvents);
			}

			// check for LAST_MAN_STANDING
			if (currentCombat != null && party.numAlive() == 1)
			{
				PlayerCharacter pc = party.getLivePlayerCharacters().get(0);
				if (pc.getModifier(Stats.Modifier.LAST_STAND) > 0)
				{
					result.add(new UiMessageEvent(StringUtil.getEventText("msg.last.stand", pc.getDisplayName())));
					result.add(new HealingEvent(pc, pc.getHitPoints().getMaximum()/2));
					result.add(new StaminaEvent(pc, pc.getHitPoints().getMaximum()/4));
					result.add(new RestoreActionPointsEvent(pc, pc.getActionPoints().getMaximum()/2));
				}
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

		// check for INSPIRING_BLOW effects
		int inspiringBlow = attacker.getModifier(Stats.Modifier.INSPIRING_BLOW);
		if (inspiringBlow > 0 && maze.getCurrentCombat() != null)
		{
			Spell spell = GameSys.getInstance().getInspiringBlowSpell(attacker);
			SpellAction sa = new SpellAction(attacker.getActorGroup(), spell, inspiringBlow);
			sa.setActor(attacker);

			result.addAll(ActorActionResolver.resolveAction(sa, maze.getCurrentCombat()));
		}

		// set victim hits to 0, so that we don't have issues with negative hits
		// on a later resurrection
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
		return StringUtil.getEventText("msg.dies", getVictim().getDisplayName());
	}
}
