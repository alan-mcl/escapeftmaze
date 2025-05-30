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

package mclachlan.maze.map.script;

import java.util.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.stat.GameSys;
import mclachlan.maze.stat.UnifiedActor;
import mclachlan.maze.stat.magic.MagicSys;
import mclachlan.maze.stat.magic.Spell;
import mclachlan.maze.util.MazeException;

/**
 *
 */
public class CastSpellAtPartyEvent extends MazeEvent
{
	private String spellName;
	private int casterLevel;
	private int castingLevel;

	public CastSpellAtPartyEvent()
	{
	}

	/*-------------------------------------------------------------------------*/
	public CastSpellAtPartyEvent(String spellName, int casterLevel, int castingLevel)
	{
		this.spellName = spellName;
		this.casterLevel = casterLevel;
		this.castingLevel = castingLevel;
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> resolve()
	{
		Spell spell = Database.getInstance().getSpell(spellName);

		UnifiedActor caster =
			switch (spell.getTargetType())
				{
					// good stuff cast on the party
					case MagicSys.SpellTargetType.CASTER,
						MagicSys.SpellTargetType.PARTY,
						MagicSys.SpellTargetType.PARTY_BUT_NOT_CASTER,
						MagicSys.SpellTargetType.TILE,
						MagicSys.SpellTargetType.ITEM,
						MagicSys.SpellTargetType.ALLY ->
						caster = new GameSys.FriendlyCaster(spell, casterLevel, castingLevel);


					// bad stuff cast at the part
					case MagicSys.SpellTargetType.ALL_FOES,
						MagicSys.SpellTargetType.FOE,
						MagicSys.SpellTargetType.FOE_GROUP,
						MagicSys.SpellTargetType.LOCK_OR_TRAP,
						MagicSys.SpellTargetType.NPC,
						MagicSys.SpellTargetType.CLOUD_ONE_GROUP,
						MagicSys.SpellTargetType.CLOUD_ALL_GROUPS ->
						caster = new GameSys.TrapCaster(spell, casterLevel, castingLevel);

					default ->
						throw new MazeException("Unrecognized spell target type: "
							+ spell.getTargetType());
				};

		GameSys.getInstance().castSpellOnPartyOutsideCombat(
			spell, casterLevel, castingLevel, caster);

		return null;
	}

	/*-------------------------------------------------------------------------*/
	public int getCasterLevel()
	{
		return casterLevel;
	}

	public int getCastingLevel()
	{
		return castingLevel;
	}

	public String getSpellName()
	{
		return spellName;
	}

	public void setSpellName(String spellName)
	{
		this.spellName = spellName;
	}

	public void setCasterLevel(int casterLevel)
	{
		this.casterLevel = casterLevel;
	}

	public void setCastingLevel(int castingLevel)
	{
		this.castingLevel = castingLevel;
	}
}
