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
import mclachlan.maze.data.StringUtil;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.game.event.UiMessageEvent;
import mclachlan.maze.stat.Dice;
import mclachlan.maze.stat.SpellTarget;
import mclachlan.maze.stat.UnifiedActor;
import mclachlan.maze.stat.combat.ActorActionResolver;
import mclachlan.maze.stat.combat.SpellAction;
import mclachlan.maze.stat.combat.SpellTargetUtils;
import mclachlan.maze.stat.magic.Spell;
import mclachlan.maze.stat.magic.SpellBook;
import mclachlan.maze.stat.magic.SpellEffect;
import mclachlan.maze.stat.magic.SpellResult;
import mclachlan.maze.ui.diygui.animation.AnimationContext;

/**
 * Troubadour's Spell Stealing ability
 */
public class SpellStealingSpellResult extends SpellResult
{
	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> apply(
		UnifiedActor source,
		UnifiedActor target,
		int castingLevel,
		SpellEffect parent,
		Spell spell)
	{
		ArrayList<MazeEvent> result = new ArrayList<MazeEvent>();

		// Target knows no spells = early exit
		SpellBook spellBook = target.getSpellBook();
		if (spellBook == null || spellBook.getSpells().isEmpty())
		{
			result.add(
				new UiMessageEvent(
					StringUtil.getEventText("msg.spell.stealing.no.spells",
						target.getDisplayName())));
			return result;
		}

		// get random spell to cast
		Spell spellToCast = spellBook.getSpells().get(Dice.nextInt(spellBook.getSpells().size()));

		// cast the spell
		int castAtLevel = castingLevel/3;
		SpellTarget spellTarget = SpellTargetUtils.getRandomSensibleSpellTarget(
			source, spellToCast, Maze.getInstance().getCurrentCombat(), spellToCast.getTargetType());

		SpellAction sa = new SpellAction(
			spellTarget,
			spellToCast,
			castAtLevel,
			source);

		return ActorActionResolver.resolveSpell(
			Maze.getInstance().getCurrentCombat(),
			source,
			sa,
			false,
			false,
			new AnimationContext(source));
	}
}
