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

import mclachlan.maze.data.StringUtil;
import mclachlan.maze.game.Maze;
import mclachlan.maze.stat.*;
import mclachlan.maze.ui.diygui.ChooseCharacterCallback;
import mclachlan.maze.util.MazeException;

import static mclachlan.maze.stat.magic.MagicSys.SpellTargetType.*;

/**
 *
 */
public class SpecialAbilityOption extends ActorActionOption
	implements ChooseCharacterCallback
{
	private SpellLikeAbility spellLikeAbility;
	private ActorActionIntention intention;
	private ActionOptionCallback callback;

	/*-------------------------------------------------------------------------*/

	public SpecialAbilityOption(SpellLikeAbility spellLikeAbility)
	{
		super("Spell Like Ability", "aao.spell.like.ability");
		this.spellLikeAbility = spellLikeAbility;
	}

	/*-------------------------------------------------------------------------*/

	@Override
	public void select(UnifiedActor actor, Combat combat,
		ActionOptionCallback callback)
	{
		this.callback = callback;
		ActorGroup selectedFoeGroup = null;
		if (combat != null)
		{
			selectedFoeGroup = Maze.getInstance().getUi().getSelectedFoeGroup();
		}

		SpellTarget spellTarget;

		switch (spellLikeAbility.getSpell().getTargetType())
		{
			// do not require target selection
			case CASTER:
				spellTarget = actor;
				break;

			case PARTY:
				if (combat != null)
				{
					spellTarget = combat.getActorGroup(actor);
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
				// EARLY EXIT
				Maze.getInstance().getUi().chooseACharacter(this);
				return;

			// makes no sense, never cast in combat
			case LOCK_OR_TRAP:
			case NPC:
				spellTarget = null;
				break;

			default:
				throw new MazeException("Unrecognized spell target type: "
					+ spellLikeAbility.getSpell().getTargetType());
		}

		this.intention = new SpecialAbilityIntention(
			spellTarget,
			spellLikeAbility.getSpell(),
			spellLikeAbility.getCastingLevel().compute(actor));

		callback.selected(intention);
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public ActorActionIntention getIntention()
	{
		return this.intention;
	}

	/*-------------------------------------------------------------------------*/
	public boolean characterChosen(PlayerCharacter pc, int pcIndex)
	{
		this.intention = new SpecialAbilityIntention(
			pc,
			spellLikeAbility.getSpell(),
			spellLikeAbility.getCastingLevel().compute(pc));
		callback.selected(intention);
		return true;
	}

	/*-------------------------------------------------------------------------*/

	@Override
	public String toString()
	{
		return StringUtil.getUiLabel(
			getDisplayName(),
			this.spellLikeAbility.getName());
	}

	/*	*//*-------------------------------------------------------------------------*//*
	private void castSpellOutsideCombat(int target)
	{
		switch (spellLikeAbility.getSpell().getTargetType())
		{
			// do not require target selection
			case MagicSys.SpellTargetType.CASTER:
			case MagicSys.SpellTargetType.PARTY:
			case MagicSys.SpellTargetType.TILE:
			case MagicSys.SpellTargetType.ITEM:
				GameSys.getInstance().castPartySpellOutsideCombat(
					spellLikeAbility.getSpell(),
					(PlayerCharacter)getActor(),
					spellLikeAbility.getCastingLevel().roll(),
					null);
				break;

			// requires the selection of a player character
			case MagicSys.SpellTargetType.ALLY:
				GameSys.getInstance().castPartySpellOutsideCombat(
					spellLikeAbility.getSpell(),
					(PlayerCharacter)getActor(),
					spellLikeAbility.getCastingLevel().roll(),
					null);
				break;

			// makes no sense
			case MagicSys.SpellTargetType.ALL_FOES:
			case MagicSys.SpellTargetType.FOE:
			case MagicSys.SpellTargetType.FOE_GROUP:
			case MagicSys.SpellTargetType.LOCK_OR_TRAP:
			case MagicSys.SpellTargetType.NPC:
			case MagicSys.SpellTargetType.CLOUD_ONE_GROUP:
			case MagicSys.SpellTargetType.CLOUD_ALL_GROUPS:
				// cast it anyway
				GameSys.getInstance().castPartySpellOutsideCombat(
					spellLikeAbility.getSpell(),
					(PlayerCharacter)getActor(),
					spellLikeAbility.getCastingLevel().roll(),
					null);
				break;

			default:
				throw new MazeException("Unrecognized spell target type: "
					+ spellLikeAbility.getSpell().getTargetType());
		}
	}*/
}
