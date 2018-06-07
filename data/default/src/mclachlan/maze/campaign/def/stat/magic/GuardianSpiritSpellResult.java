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

package mclachlan.maze.campaign.def.stat.magic;

import java.util.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.game.GameTime;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.map.Tile;
import mclachlan.maze.stat.StatModifier;
import mclachlan.maze.stat.Stats;
import mclachlan.maze.stat.UnifiedActor;
import mclachlan.maze.stat.combat.event.ConditionEvent;
import mclachlan.maze.stat.condition.Condition;
import mclachlan.maze.stat.magic.MagicSys;
import mclachlan.maze.stat.magic.Spell;
import mclachlan.maze.stat.magic.SpellEffect;
import mclachlan.maze.stat.magic.SpellResult;
import mclachlan.maze.util.MazeException;

/**
 * A spell result that opens lock or disarms traps
 */
public class GuardianSpiritSpellResult extends SpellResult
{
	@Override
	public List<MazeEvent> apply(
		UnifiedActor source,
		UnifiedActor target,
		int castingLevel,
		SpellEffect parent,
		Spell spell)
	{
		List<MazeEvent> result = new ArrayList<MazeEvent>();

		Tile tile = Maze.getInstance().getCurrentTile();
		Tile.TerrainType terrainType = tile.getTerrainType();

		final String adjective;
		final StatModifier statModifier = new StatModifier();

		switch (terrainType)
		{
			case FAKE:
			case URBAN:
			case DUNGEON:
				adjective = "warded by a spirit of the earth";
				statModifier.setModifier(Stats.Modifier.RESIST_BLUDGEONING, 5);
				statModifier.setModifier(Stats.Modifier.RESIST_PIERCING, 10);
				statModifier.setModifier(Stats.Modifier.RESIST_SLASHING, 15);
				statModifier.setModifier(Stats.Modifier.RESIST_EARTH, 20);
				statModifier.setModifier(Stats.Modifier.RESIST_FIRE, 15);
				statModifier.setModifier(Stats.Modifier.PARRY, 10);
				statModifier.setModifier(Stats.Modifier.IMMUNE_TO_KO, 1);
				break;
			case WASTELAND:
				adjective = "warded by a spirit of the air";
				statModifier.setModifier(Stats.Modifier.RESIST_BLUDGEONING, 20);
				statModifier.setModifier(Stats.Modifier.RESIST_PIERCING, 15);
				statModifier.setModifier(Stats.Modifier.RESIST_SLASHING, 20);
				statModifier.setModifier(Stats.Modifier.RESIST_AIR, 30);
				statModifier.setModifier(Stats.Modifier.RESIST_WATER, 20);
				statModifier.setModifier(Stats.Modifier.RESIST_FIRE, 20);
				statModifier.setModifier(Stats.Modifier.RIPOSTE, 10);
				statModifier.setModifier(Stats.Modifier.IMMUNE_TO_LIGHTNING, 1);
				break;
			case WILDERNESS:
				adjective = "warded by a spirit of the wilderness";
				statModifier.setModifier(Stats.Modifier.RESIST_BLUDGEONING, 20);
				statModifier.setModifier(Stats.Modifier.RESIST_PIERCING, 20);
				statModifier.setModifier(Stats.Modifier.RESIST_SLASHING, 20);
				statModifier.setModifier(Stats.Modifier.RESIST_EARTH, 20);
				statModifier.setModifier(Stats.Modifier.RESIST_FIRE, 20);
				statModifier.setModifier(Stats.Modifier.RESIST_WATER, 20);
				statModifier.setModifier(Stats.Modifier.RESIST_AIR, 20);
				statModifier.setModifier(Stats.Modifier.DODGE, 10);
				statModifier.setModifier(Stats.Modifier.IMMUNE_TO_WEB, 1);
				statModifier.setModifier(Stats.Modifier.IMMUNE_TO_SWALLOW, 1);
				statModifier.setModifier(Stats.Modifier.INITIATIVE, 2);
				break;

			default:
				throw new MazeException(""+terrainType);
		}

		Condition condition = new Condition(
			Database.getInstance().getConditionTemplate("guardian spirit"),
			5 + castingLevel,
			castingLevel,
			castingLevel,
			null,
			null,
			null,
			null,
			MagicSys.SpellEffectType.EARTH,
			MagicSys.SpellEffectSubType.NONE,
			source,
			true,
			true,
			GameTime.getTurnNr(),
			false)
		{
			@Override
			public String getAdjective()
			{
				return adjective;
			}

			@Override
			public Map<Stats.Modifier, Integer> getModifiers()
			{
				return statModifier.getModifiers();
			}
		};

		result.add(new ConditionEvent(target, condition));

		return result;
	}
}
