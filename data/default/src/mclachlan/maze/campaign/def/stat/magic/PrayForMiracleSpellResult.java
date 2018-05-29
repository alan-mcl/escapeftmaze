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
import mclachlan.maze.map.script.GrantGoldEvent;
import mclachlan.maze.stat.*;
import mclachlan.maze.stat.combat.ActorActionResolver;
import mclachlan.maze.stat.combat.SpellAction;
import mclachlan.maze.stat.combat.SpellTargetUtils;
import mclachlan.maze.stat.combat.event.NoEffectEvent;
import mclachlan.maze.stat.magic.Spell;
import mclachlan.maze.stat.magic.SpellEffect;
import mclachlan.maze.stat.magic.SpellResult;
import mclachlan.maze.ui.diygui.animation.AnimationContext;
import mclachlan.maze.util.MazeException;

/**
 * Create ammo for the currently equipped ranged weapon
 */
public class PrayForMiracleSpellResult extends SpellResult
{
	private static List<PrayerResult> reallyBad, bad, neutral, good, great, reallyGreat;

	static
	{
		reallyBad = new ArrayList<PrayerResult>();
		reallyBad.add(new SpellPrayerResult("Fear"));
		reallyBad.add(new SpellPrayerResult("Bane"));
		reallyBad.add(new SpellPrayerResult("Weakness"));
		reallyBad.add(new SpellPrayerResult("Silence"));
		reallyBad.add(new SpellPrayerResult("Jinx"));
		reallyBad.add(new SpellPrayerResult("Slow"));
		reallyBad.add(new SpellPrayerResult("Melt Armour"));
		reallyBad.add(new FlavourTextPrayerResult("msg.pray.lesson"));
		reallyBad.add(new FlavourTextPrayerResult("msg.pray.doom"));
		reallyBad.add(new FlavourTextPrayerResult("msg.pray.nothing"));

		bad = new ArrayList<PrayerResult>();
		bad.add(new SpellPrayerResult("Fear"));
		bad.add(new SpellPrayerResult("Bane"));
		bad.add(new SpellPrayerResult("Weakness"));
		bad.add(new SpellPrayerResult("Silence"));
		bad.add(new GrantGoldPrayerResult(Dice.d100));
		bad.add(new FlavourTextPrayerResult("msg.pray.lesson"));
		bad.add(new FlavourTextPrayerResult("msg.pray.doom"));
		bad.add(new FlavourTextPrayerResult("msg.pray.nothing"));

		neutral = new ArrayList<PrayerResult>();
		neutral.add(new SpellPrayerResult("Heal Light Wounds"));
		neutral.add(new SpellPrayerResult("Bless Party"));
		neutral.add(new SpellPrayerResult("Stamina"));
		neutral.add(new SpellPrayerResult("Striking"));
		neutral.add(new GrantGoldPrayerResult(Dice.d100));
		neutral.add(new FlavourTextPrayerResult("msg.pray.lesson"));
		neutral.add(new FlavourTextPrayerResult("msg.pray.nothing"));

		good = new ArrayList<PrayerResult>();
		good.add(new SpellPrayerResult("Heal Moderate Wounds"));
		good.add(new SpellPrayerResult("Guardian Angel"));
		good.add(new SpellPrayerResult("Bless Party"));
		good.add(new SpellPrayerResult("Striking"));
		good.add(new SpellPrayerResult("Rest All"));
		good.add(new GrantGoldPrayerResult(Dice.d100));

		great = new ArrayList<PrayerResult>();
		great.add(new SpellPrayerResult("Heal All"));
		great.add(new SpellPrayerResult("Heal Serious Wounds"));
		great.add(new SpellPrayerResult("Magic Screen"));
		great.add(new SpellPrayerResult("Revitalise"));
		great.add(new SpellPrayerResult("Soul Shield"));
		great.add(new SpellPrayerResult("Cure Disease"));
		great.add(new GrantGoldPrayerResult(Dice.d100));

		reallyGreat = new ArrayList<PrayerResult>();
		reallyGreat.add(new SpellPrayerResult("Heal All"));
		reallyGreat.add(new SpellPrayerResult("Rest All"));
		reallyGreat.add(new SpellPrayerResult("Revitalise"));
		reallyGreat.add(new SpellPrayerResult("Magic Screen"));
		reallyGreat.add(new SpellPrayerResult("Soul Shield"));
		reallyGreat.add(new SpellPrayerResult("Cure Disease"));
		reallyGreat.add(new SpellPrayerResult("Resurrection"));
		reallyGreat.add(new GrantGoldPrayerResult(Dice.d1000));
	}

	private static enum KarmaLevel
	{
		REALLY_BAD, BAD, NEUTRAL, GOOD, GREAT, REALLY_GREAT
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> apply(
		UnifiedActor source,
		UnifiedActor target,
		int castingLevel,
		SpellEffect parent, Spell spell)
	{
		ArrayList<MazeEvent> result = new ArrayList<MazeEvent>();
		if (!(source instanceof PlayerCharacter))
		{
			result.add(new NoEffectEvent());
			return result;
		}

		PlayerCharacter caster = (PlayerCharacter)source;
		int karma = caster.getKarma();

		List<PrayerResult> table;
		KarmaLevel level;

		if (karma < -25) level = KarmaLevel.REALLY_BAD;
		else if (karma < -5) level = KarmaLevel.BAD;
		else if (karma < 5) level = KarmaLevel.NEUTRAL;
		else if (karma < 25) level = KarmaLevel.GOOD;
		else if (karma < 50) level = KarmaLevel.GREAT;
		else level = KarmaLevel.REALLY_GREAT;

		switch (level)
		{
			case REALLY_BAD: table = reallyBad;
				break;
			case BAD: table = bad;
				break;
			case NEUTRAL: table = neutral;
				break;
			case GOOD: table = good;
				break;
			case GREAT: table = great;
				break;
			case REALLY_GREAT: table = reallyGreat;
				break;
			default: throw new MazeException(String.valueOf(level));
		}

		PrayerResult prayerResult = table.get(Dice.nextInt(table.size()));

		result.addAll(
			prayerResult.execute(
				source,
				level,
				Maze.getInstance()));

		return result;
	}

	/*-------------------------------------------------------------------------*/
	private static interface PrayerResult
	{
		List<MazeEvent> execute(
			UnifiedActor source,
			KarmaLevel karma,
			Maze maze);
	}

	/*-------------------------------------------------------------------------*/
	private static class FlavourTextPrayerResult implements PrayerResult
	{
		private String msg;

		private FlavourTextPrayerResult(String msg)
		{
			this.msg = msg;
		}

		@Override
		public List<MazeEvent> execute(
			UnifiedActor source,
			KarmaLevel karma,
			Maze maze)
		{
			List<MazeEvent> result = new ArrayList<MazeEvent>();

			result.add(new UiMessageEvent(StringUtil.getEventText(msg)));

			return result;
		}
	}

	/*-------------------------------------------------------------------------*/
	private static class GrantGoldPrayerResult implements PrayerResult
	{
		private Dice amount;

		private GrantGoldPrayerResult(Dice amount)
		{
			this.amount = amount;
		}

		@Override
		public List<MazeEvent> execute(
			UnifiedActor source,
			KarmaLevel karma,
			Maze maze)
		{
			List<MazeEvent> result = new ArrayList<MazeEvent>();

			result.add(new UiMessageEvent(StringUtil.getEventText("msg.pray.gold")));
			result.add(new GrantGoldEvent(amount.roll("GrantGoldPrayerResult")));

			return result;
		}
	}

	/*-------------------------------------------------------------------------*/
	private static class SpellPrayerResult implements PrayerResult
	{
		private String spellName;

		private SpellPrayerResult(String spellName)
		{
			this.spellName = spellName;
		}

		@Override
		public List<MazeEvent> execute(
			UnifiedActor source,
			KarmaLevel karma,
			final Maze maze)
		{
			Spell spell = Database.getInstance().getSpell(spellName);

			int castingLevel;
			int targetType = spell.getTargetType();

			switch (karma)
			{
				case REALLY_BAD: castingLevel = 7;
					targetType = SpellTargetUtils.getRandomSpellBackfireTargetType(spell);
					break;
				case BAD: castingLevel = 3;
					targetType = SpellTargetUtils.getRandomSpellBackfireTargetType(spell);
					break;
				case NEUTRAL: castingLevel = 1;
					break;
				case GOOD: castingLevel = 3;
					break;
				case GREAT: castingLevel = 7;
					break;
				case REALLY_GREAT: castingLevel = 10;
					break;
				default: throw new MazeException(String.valueOf(karma));
			}

			SpellTarget spellTarget = SpellTargetUtils.getRandomSensibleSpellTarget(
				source, spell, maze.getCurrentCombat());

			SpellAction sa = new SpellAction(
				spellTarget,
				spell,
				castingLevel,
				source);

			return ActorActionResolver.resolveSpell(
				maze.getCurrentCombat(),
				source,
				sa,
				false, // todo
				false,
				new AnimationContext(source));
		}
	}

	/*-------------------------------------------------------------------------*/

}
