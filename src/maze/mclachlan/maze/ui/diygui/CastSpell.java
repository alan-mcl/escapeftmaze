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

package mclachlan.maze.ui.diygui;

import mclachlan.maze.game.Maze;
import mclachlan.maze.stat.GameSys;
import mclachlan.maze.stat.PlayerCharacter;
import mclachlan.maze.stat.magic.MagicSys;
import mclachlan.maze.stat.magic.Spell;
import mclachlan.maze.util.MazeException;

/**
 *
 */
public class CastSpell implements SpellSelectionCallback, ChooseCharacterCallback
{
	private CastSpellCallback callback;
	private SpellSelectionDialog spellDialog;
	private Spell spell;
	private PlayerCharacter caster;
	private int casterIndex;

	/*-------------------------------------------------------------------------*/
	public CastSpell(CastSpellCallback callback, PlayerCharacter caster)
	{
		this.callback = callback;
		this.caster = caster;
		this.casterIndex = Maze.getInstance().getParty().getActors().indexOf(caster);

		spellDialog = new SpellSelectionDialog(caster, this);
		Maze.getInstance().getUi().showDialog(spellDialog);
	}

	/*-------------------------------------------------------------------------*/
	public void spellSelected(Spell spell)
	{
		if (spell == null)
		{
			// no spell selected
			return;
		}
		
		this.spell = spell;

		if (this.spell.getTargetType() == MagicSys.SpellTargetType.ALLY)
		{
			// requires further work
			DiyGuiUserInterface.instance.chooseACharacter(this);
		}
		else
		{
			castSpell(-1);
		}
	}

	/*-------------------------------------------------------------------------*/
	public boolean characterChosen(PlayerCharacter pc, int pcIndex)
	{
		castSpell(pcIndex);
		return true;
	}

	/*-------------------------------------------------------------------------*/
	private void castSpell(int target)
	{
		int castingLevel = spellDialog.getCastingLevel();
		if (!this.callback.castSpell(spell, caster, casterIndex, castingLevel, target))
		{
			switch (spell.getTargetType())
			{
				// do not require target selection
				case MagicSys.SpellTargetType.CASTER:
				case MagicSys.SpellTargetType.PARTY:
				case MagicSys.SpellTargetType.TILE:
				case MagicSys.SpellTargetType.ITEM:
					GameSys.getInstance().castPartySpellOutsideCombat(
						spell, caster, castingLevel, null);
					break;

					// requires the selection of a player character
				case MagicSys.SpellTargetType.ALLY:
					GameSys.getInstance().castPartySpellOutsideCombat(
						spell, caster, castingLevel, Maze.getInstance().getPlayerCharacter(target));
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
						spell, caster, castingLevel, null);
					break;

				default:
					throw new MazeException("Unrecognized spell target type: "
						+ spell.getTargetType());
			}
		}
	}
}