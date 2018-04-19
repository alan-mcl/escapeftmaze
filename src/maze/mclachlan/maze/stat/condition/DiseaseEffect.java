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

package mclachlan.maze.stat.condition;

import java.util.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.game.GameTime;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.stat.Dice;
import mclachlan.maze.stat.Personality;
import mclachlan.maze.stat.Stats;
import mclachlan.maze.stat.combat.event.ConditionEvent;
import mclachlan.maze.stat.magic.MagicSys;
import mclachlan.maze.stat.magic.Value;
import mclachlan.maze.stat.magic.ValueList;
import mclachlan.maze.util.MazeException;


/**
 *
 */
public class DiseaseEffect extends ConditionEffect
{
	/*-------------------------------------------------------------------------*/
	public DiseaseEffect()
	{
	}

	/*-------------------------------------------------------------------------*/
	public DiseaseEffect(String name)
	{
		super(name);
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public boolean isMultiplesAllowed()
	{
		return true;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public List<MazeEvent> endOfTurn(Condition condition, long turnNr)
	{
		// todo: give the pc a chance for a saving throw to remove the disease

		// let disease never expire naturally
		condition.decDuration(-1);

		// check for spawning new conditions
		if (Dice.d100.roll("disease: new condition check") <= 1)
		{
			Maze.log("disease spawns new condition");
			Condition spawn;
			int roll = Dice.d10.roll("disease: random condition");
			switch (roll)
			{
				case 1:
				case 2:
				case 3:
					// poison!
					spawn = new Condition(
						Database.getInstance().getConditionTemplate("GENERIC_POISON"),
						new Dice(5, 4, 1).roll("poison duration"),
						condition.getStrength(),
						condition.getCastingLevel(),
						new ValueList(new Value(1, Value.SCALE.NONE)),
						null,
						null,
						null,
						MagicSys.SpellEffectType.EARTH,
						MagicSys.SpellEffectSubType.POISON,
						condition.getSource(),
						false,
						false,
						GameTime.getTurnNr(),
						true);
					break;

				case 4:
				case 5:
				case 6:
					// nausea!
					spawn = new Condition(
						Database.getInstance().getConditionTemplate("GENERIC_NAUSEA"),
						new Dice(10, 4, 10).roll("nausea duration"),
						condition.getStrength(),
						condition.getCastingLevel(),
						null,
						null,
						null,
						null,
						condition.getType(),
						MagicSys.SpellEffectSubType.NONE,
						condition.getSource(),
						true,
						false,
						GameTime.getTurnNr(),
						true);
					break;

				case 7:
					// blind!
					spawn = new Condition(
						Database.getInstance().getConditionTemplate("GENERIC_BLIND"),
						new Dice(5, 4, 1).roll("blind duration"),
						condition.getStrength(),
						condition.getCastingLevel(),
						null,
						null,
						null,
						null,
						condition.getType(),
						MagicSys.SpellEffectSubType.NONE,
						condition.getSource(),
						true,
						false,
						GameTime.getTurnNr(),
						true);
					break;

				case 8:
				case 9:
					// fear!
					spawn = new Condition(
						Database.getInstance().getConditionTemplate("GENERIC_FEAR"),
						new Dice(2, 20, 2).roll("fear duration"),
						condition.getStrength(),
						condition.getCastingLevel(),
						null,
						null,
						null,
						null,
						condition.getType(),
						MagicSys.SpellEffectSubType.PSYCHIC,
						condition.getSource(),
						true,
						false,
						GameTime.getTurnNr(),
						true);
					break;

				case 10:
					// insane!
					spawn = new Condition(
						Database.getInstance().getConditionTemplate("GENERIC_INSANE"),
						new Dice(2, 20, 2).roll("insane duration"),
						condition.getStrength(),
						condition.getCastingLevel(),
						null,
						null,
						null,
						null,
						condition.getType(),
						MagicSys.SpellEffectSubType.PSYCHIC,
						condition.getSource(),
						true,
						false,
						GameTime.getTurnNr(),
						true);
					break;
				default: throw new MazeException("oops: "+roll);
			}

			spawn.setTarget(condition.getTarget());

			List<MazeEvent> result = new ArrayList<MazeEvent>();
			result.add(new ConditionEvent(condition.getTarget(), spawn));
			return result;
		}
		else if (Dice.d1000.roll("disease: recovery check") <= 2)
		{
			// expire disease
			condition.setDuration(-1);
			return null;
		}

		return null;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public Stats.Modifier getImmunityModifier()
	{
		return Stats.Modifier.IMMUNE_TO_DISEASE;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public String getSpeechKey()
	{
		return Personality.BasicSpeech.CONDITION_DISEASE.getKey();
	}
}
