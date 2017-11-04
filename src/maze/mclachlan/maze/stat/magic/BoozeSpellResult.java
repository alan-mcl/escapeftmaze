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

package mclachlan.maze.stat.magic;

import java.util.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.stat.Dice;
import mclachlan.maze.stat.Stats;
import mclachlan.maze.stat.UnifiedActor;
import mclachlan.maze.stat.combat.event.ConditionEvent;
import mclachlan.maze.stat.combat.event.HealingEvent;
import mclachlan.maze.stat.combat.event.StaminaEvent;
import mclachlan.maze.stat.condition.Condition;
import mclachlan.maze.stat.condition.ConditionTemplate;

/**
 * applies some random boozy stuff
 */
public class BoozeSpellResult extends SpellResult
{
	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> apply(
		UnifiedActor source,
		UnifiedActor target,
		int castingLevel,
		SpellEffect parent, Spell spell)
	{
		List<MazeEvent> results = new ArrayList<MazeEvent>();

		// if not drinking fit, something bad might happen
		if (source.getModifier(Stats.Modifier.DRINKING_FIT) < 0)
		{
			// use the targets brawn, unless power is > 10 and > brawn
			int attrib;
			int brawn = source.getModifier(Stats.Modifier.BRAWN);
			int power = source.getModifier(Stats.Modifier.POWER);

			if (power > brawn && power > 10)
			{
				attrib = power;
			}
			else
			{
				attrib = brawn;
			}

			attrib -= castingLevel;

			// roll 1d10, compare to attrib, 1 always good, 10 always bad
			int roll = Dice.d10.roll();

			if (roll == 10 || (roll >= attrib && roll != 1))
			{
				somethingBad(results, target, castingLevel);
			}
			else
			{
				somethingGood(results, target, castingLevel);
			}
		}
		else
		{
			somethingGood(results, target, castingLevel);
		}

		// regardless, reduce some fatigue
		results.add(getStaminaEvent(target, castingLevel));

		return results;
	}

	/*-------------------------------------------------------------------------*/
	private void somethingGood(List<MazeEvent> results, UnifiedActor target, int castingLevel)
	{
		// roll 1d6, add casting level

		int roll = Dice.d6.roll()+castingLevel;

		switch (roll)
		{
			case 1:
			case 2:
			case 3: results.add(getStaminaEvent(target, castingLevel)); break;
			case 4:
			case 5:
			case 6:
			case 7: results.add(getHealingEvent(target, castingLevel)); break;
			case 8:
			case 9:
			case 10:
			case 11: results.add(getConditionEvent(target, "BOOZE_SUPERMAN", castingLevel)); break; 
			case 12:
			case 13: results.add(getConditionEvent(target, "BOOZE_HEROISM", castingLevel)); break;
			case 14:
			default: results.add(getConditionEvent(target, "BOOZE_FERVOUR", castingLevel)); break;
		}
	}

	/*-------------------------------------------------------------------------*/
	private void somethingBad(List<MazeEvent> results, UnifiedActor target, int castingLevel)
	{
		// roll 1d6, add casting level

		int roll = Dice.d6.roll()+castingLevel;

		switch (roll)
		{
			case 1:
			case 2:
			case 3: results.add(getConditionEvent(target, "SCALING_SLEEP", castingLevel*3)); break;
			case 4:
			case 5:
			case 6: results.add(getConditionEvent(target, "GENERIC_KO", castingLevel)); break;
			case 7:
			case 8:
			case 9: results.add(getConditionEvent(target, "SCALING_NAUSEA", castingLevel*3)); break;
			case 10:
			case 11:
			case 12: results.add(getConditionEvent(target, "SCALING_PARALYSE", castingLevel*3)); break;
			case 13:
			case 14:
			default: results.add(getConditionEvent(target, "SCALING_HEX", castingLevel*3)); break;

		}
	}

	/*-------------------------------------------------------------------------*/
	private HealingEvent getHealingEvent(UnifiedActor target, int castingLevel)
	{
		return new HealingEvent(target, new Dice(castingLevel, 2, 0).roll());
	}

	/*-------------------------------------------------------------------------*/
	private StaminaEvent getStaminaEvent(UnifiedActor target, int castingLevel)
	{
		return new StaminaEvent(target, new Dice(castingLevel, 2, 0).roll());
	}

	/*-------------------------------------------------------------------------*/
	private ConditionEvent getConditionEvent(
		UnifiedActor target,
		String conditionTemplate,
		int castingLevel)
	{
		ConditionTemplate ct = Database.getInstance().getConditionTemplate(conditionTemplate);

		// will setting source=target break anything?
		Condition condition = ct.create(
			target, target, castingLevel, MagicSys.SpellEffectType.WATER, MagicSys.SpellEffectSubType.NONE);

		return new ConditionEvent(target, condition);
	}
}
