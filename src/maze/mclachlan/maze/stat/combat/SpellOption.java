/*
 * Copyright (c) 2014 Alan McLachlan
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

package mclachlan.maze.stat.combat;

import mclachlan.maze.game.Maze;
import mclachlan.maze.stat.*;
import mclachlan.maze.stat.magic.Spell;
import mclachlan.maze.ui.diygui.CastSpell;
import mclachlan.maze.ui.diygui.CastSpellCallback;
import mclachlan.maze.util.MazeException;

import static mclachlan.maze.stat.magic.MagicSys.SpellTargetType.*;

/**
 *
 */
public class SpellOption extends ActorActionOption
	implements CastSpellCallback
{
	private Combat combat;
	private ActionOptionCallback callback;
	private ActorActionIntention intention;

	/*-------------------------------------------------------------------------*/
	public SpellOption()
	{
		super("Cast Spell", "aao.cast.spell");
	}
	
	/*-------------------------------------------------------------------------*/
	@Override
	public void select(UnifiedActor actor, Combat combat,
		ActionOptionCallback callback)
	{
		this.combat = combat;
		this.callback = callback;
		new CastSpell(this, (PlayerCharacter)getActor());
	}

	/*-------------------------------------------------------------------------*/

	@Override
	public ActorActionIntention getIntention()
	{
		if (this.intention == null)
		{
			return ActorActionIntention.INTEND_NOTHING;
		}
		else
		{
			return this.intention;
		}
	}

	/*-------------------------------------------------------------------------*/
	public boolean castSpell(Spell spell, PlayerCharacter caster,
		int casterIndex, int castingLevel, int target)
	{
		ActorGroup selectedFoeGroup = null;
		if (combat != null)
		{
			selectedFoeGroup = Maze.getInstance().getUi().getSelectedFoeGroup();
		}

		SpellTarget spellTarget;

		switch (spell.getTargetType())
		{
			// do not require target selection
			case CASTER:
				spellTarget = caster;
				break;

			case PARTY:
				if (combat != null)
				{
					spellTarget = combat.getActorGroup(caster);
				}
				else
				{
					spellTarget = Maze.getInstance().getParty();
				}
				break;

			case ALL_FOES:
			case CLOUD_ALL_GROUPS:
			case TILE:
			case ITEM:
				spellTarget = null;
				break;

			// take their target from the selected foe group
			case FOE:
				if (combat != null)
				{
					Dice d = new Dice(1, selectedFoeGroup.numAlive(), -1);
					spellTarget = selectedFoeGroup.getActors().get(d.roll());
				}
				else
				{
					spellTarget = null;
				}
				break;

			case FOE_GROUP:
			case CLOUD_ONE_GROUP:
				if (combat != null)
				{
					spellTarget = selectedFoeGroup;
				}
				else
				{
					spellTarget = null;
				}
				break;

			case ALLY:
				ActorGroup actorGroup;
				if (combat != null)
				{
					actorGroup = combat.getActorGroup(caster);
				}
				else
				{
					actorGroup = Maze.getInstance().getParty();
				}
				spellTarget = actorGroup.getActors().get(target);
				break;

			// makes no sense, never cast in combat
			case LOCK_OR_TRAP:
			case NPC:
				spellTarget = null;
				break;

			default:
				throw new MazeException("Unrecognized spell target type: "
					+ spell.getTargetType());
		}

		this.intention = new SpellIntention(spellTarget, spell, castingLevel);
		callback.selected(intention);
		return true;
	}

}
