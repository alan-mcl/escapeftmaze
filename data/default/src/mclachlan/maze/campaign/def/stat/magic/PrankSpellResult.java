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
import mclachlan.maze.data.StringUtil;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.game.event.UiMessageEvent;
import mclachlan.maze.stat.Dice;
import mclachlan.maze.stat.PlayerCharacter;
import mclachlan.maze.stat.UnifiedActor;
import mclachlan.maze.stat.combat.event.NoEffectEvent;
import mclachlan.maze.stat.magic.ConditionSpellResult;
import mclachlan.maze.stat.magic.Spell;
import mclachlan.maze.stat.magic.SpellEffect;
import mclachlan.maze.stat.magic.SpellResult;
import mclachlan.maze.util.MazeException;

/**
 * Troubadour's Prank ability
 */
public class PrankSpellResult extends SpellResult
{
	private static List<PrankResult> results1, results2, results3;

	static
	{
		results1 = new ArrayList<PrankResult>();
		results1.add(new ApplyConditionPrankResult("acid splash", "msg.prank.acid.1"));
		results1.add(new ApplyConditionPrankResult("_SCALING_IRRITATE_", "msg.prank.irritate.1"));
		results1.add(new ApplyConditionPrankResult("_SCALING_NAUSEA_", "msg.prank.nauseate.1"));
		results1.add(new ApplyConditionPrankResult("_SCALING_SLOW_", "msg.prank.slow.1"));

		results2 = new ArrayList<PrankResult>();
		results2.add(new ApplyConditionPrankResult("acid splash", "msg.prank.acid.1"));
		results2.add(new ApplyConditionPrankResult("_SCALING_IRRITATE_", "msg.prank.irritate.1"));
		results2.add(new ApplyConditionPrankResult("_SCALING_NAUSEA_", "msg.prank.nauseate.1"));
		results2.add(new ApplyConditionPrankResult("_SCALING_SLOW_", "msg.prank.slow.1"));

		results2.add(new ApplyConditionPrankResult("_SCALING_WEB_", "msg.prank.entangle.1"));
		results2.add(new ApplyConditionPrankResult("_SCALING_BLIND_", "msg.prank.blind.1"));
		results2.add(new ApplyConditionPrankResult("_SCALING_FEAR_", "msg.prank.fear.1"));
		results2.add(new ApplyConditionPrankResult("mace ko", "msg.prank.ko.1"));

		results3 = new ArrayList<PrankResult>();
		results3.add(new ApplyConditionPrankResult("acid splash", "msg.prank.acid.1"));
		results3.add(new ApplyConditionPrankResult("_SCALING_IRRITATE_", "msg.prank.irritate.1"));
		results3.add(new ApplyConditionPrankResult("_SCALING_NAUSEA_", "msg.prank.nauseate.1"));
		results3.add(new ApplyConditionPrankResult("_SCALING_SLOW_", "msg.prank.slow.1"));

		results3.add(new ApplyConditionPrankResult("_SCALING_BLIND_", "msg.prank.blind.1"));
		results3.add(new ApplyConditionPrankResult("_SCALING_WEB_", "msg.prank.entangle.1"));
		results3.add(new ApplyConditionPrankResult("_SCALING_FEAR_", "msg.prank.fear.1"));
		results3.add(new ApplyConditionPrankResult("mace ko", "msg.prank.ko.1"));

		results3.add(new ApplyConditionPrankResult("_SCALING_SILENCE_", "msg.prank.silence.1"));
		results3.add(new ApplyConditionPrankResult("_SCALING_PARALYZE_", "msg.prank.plyze.1"));
		results3.add(new ApplyConditionPrankResult("poison type E", "msg.prank.poison.1"));
		results3.add(new ApplyConditionPrankResult("_SCALING_SWALLOW_", "msg.prank.swallow.1"));
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> apply(
		UnifiedActor source,
		UnifiedActor target,
		int castingLevel,
		SpellEffect parent,
		Spell spell)
	{
		ArrayList<MazeEvent> result = new ArrayList<MazeEvent>();
		if (!(source instanceof PlayerCharacter))
		{
			result.add(new NoEffectEvent());
			return result;
		}

		List<PrankResult> table;

		int modCastingLevel = source.getLevel("Troubadour");

		switch (castingLevel)
		{
			case 1: table = results1;
				break;
			case 2: table = results2;
				break;
			case 3: table = results3;
				break;
			default: throw new MazeException(""+castingLevel);
		}

		PrankResult prankResult = table.get(Dice.nextInt(table.size()));

		result.addAll(
			prankResult.execute(
				source,
				target,
				modCastingLevel,
				Maze.getInstance(),
				spell,
				parent));

		return result;
	}

	/*-------------------------------------------------------------------------*/
	private static interface PrankResult
	{
		List<MazeEvent> execute(
			UnifiedActor source,
			UnifiedActor target, int castingLevel,
			Maze maze,
			Spell spell,
			SpellEffect spellEffect);
	}

	/*-------------------------------------------------------------------------*/
	private static class ApplyConditionPrankResult implements PrankResult
	{
		private String conditionTemplateName;
		private String flavourTextKey;

		private ApplyConditionPrankResult(String conditionTemplateName,
			String flavourTextKey)
		{
			this.conditionTemplateName = conditionTemplateName;
			this.flavourTextKey = flavourTextKey;
		}

		@Override
		public List<MazeEvent> execute(
			UnifiedActor source,
			UnifiedActor target,
			int castingLevel,
			final Maze maze,
			Spell spell,
			SpellEffect spellEffect)
		{
			List<MazeEvent> result = new ArrayList<MazeEvent>();

			result.add(
				new UiMessageEvent(
					StringUtil.getEventText(
						flavourTextKey,
						source.getDisplayName(),
						target.getDisplayName())));

			ConditionSpellResult sr = new ConditionSpellResult(
				Database.getInstance().getConditionTemplate(conditionTemplateName));
			result.addAll(sr.apply(source, target, castingLevel, spellEffect, spell));
			return result;
		}
	}

	/*-------------------------------------------------------------------------*/

}
