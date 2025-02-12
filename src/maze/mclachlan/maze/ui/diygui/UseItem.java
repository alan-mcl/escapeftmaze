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

import java.util.*;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.event.ModifySuppliesEvent;
import mclachlan.maze.map.TileScript;
import mclachlan.maze.map.script.GrantGoldEvent;
import mclachlan.maze.stat.*;
import mclachlan.maze.stat.magic.MagicSys;
import mclachlan.maze.util.MazeException;


/**
 *
 */
public class UseItem implements ChooseCharacterCallback,
	ItemSelectionCallback
{
	private final UseItemCallback callback;
	private Item item;
	private final PlayerCharacter user;
	private final int userIndex;

	/*-------------------------------------------------------------------------*/
	public UseItem(String title, String action, UseItemCallback callback, PlayerCharacter pc)
	{
		this.callback = callback;
		this.user = pc;
		this.userIndex = Maze.getInstance().getParty().getActors().indexOf(user);

		ItemSelectionDialog itemDialog = new ItemSelectionDialog(
			title, action, pc, this, true, true);
		Maze.getInstance().getUi().showDialog(itemDialog);
	}

	/*-------------------------------------------------------------------------*/
	public boolean characterChosen(PlayerCharacter pc, int pcIndex)
	{
		useItem(pc);
		return true;
	}

	@Override
	public void afterCharacterChosen()
	{

	}

	/*-------------------------------------------------------------------------*/
	private void useItem(SpellTarget target)
	{
		if (!this.callback.useItem(item, user, userIndex, target))
		{
			if (item.getType() == ItemTemplate.Type.SUPPLIES && item.isIdentified())
			{
				int supplies = TileScript.extractSupplies(new ArrayList<>(List.of(item)));
				Maze.getInstance().appendEvents(
					new ModifySuppliesEvent(supplies));
				user.removeItem(item, true);
				Maze.getInstance().getUi().characterSelected(user); // to refresh the inv widgets

				return;
			}
			else if (item.getType() == ItemTemplate.Type.MONEY && item.isIdentified())
			{
				int gold = TileScript.extractGold(new ArrayList<>(List.of(item)));
				Maze.getInstance().appendEvents(new GrantGoldEvent(gold));
				user.removeItem(item, true);
				Maze.getInstance().getUi().characterSelected(user); // to refresh the inv widgets

				return;
			}
			else if (item.getInvokedSpell() == null)
			{
				return;
			}

			switch (item.getInvokedSpell().getTargetType())
			{
				// do not require target selection
				case MagicSys.SpellTargetType.CASTER,
					MagicSys.SpellTargetType.PARTY,
					MagicSys.SpellTargetType.PARTY_BUT_NOT_CASTER,
					MagicSys.SpellTargetType.TILE,
					MagicSys.SpellTargetType.ITEM ->
					GameSys.getInstance().useItemOutsideCombat(item, user, null);


				// requires the selection of a player character
				case MagicSys.SpellTargetType.ALLY ->
				{
					GameSys.getInstance().useItemOutsideCombat(
						item, user, target);
					return;
				}

				// makes no sense
				case MagicSys.SpellTargetType.ALL_FOES,
					MagicSys.SpellTargetType.FOE,
					MagicSys.SpellTargetType.FOE_GROUP,
					MagicSys.SpellTargetType.LOCK_OR_TRAP,
					MagicSys.SpellTargetType.NPC,
					MagicSys.SpellTargetType.CLOUD_ONE_GROUP,
					MagicSys.SpellTargetType.CLOUD_ALL_GROUPS ->
					// cast it anyway
					GameSys.getInstance().useItemOutsideCombat(item, user, null);

				default ->
					throw new MazeException("Unrecognized spell effect target type: "
						+ item.getInvokedSpell().getTargetType());
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	public void itemSelected(Item item)
	{
		this.item = item;

		if (item == null)
		{
			return;
		}
		
		if (item.getInvokedSpell() != null)
		{
			if (item.getInvokedSpell().getTargetType() == MagicSys.SpellTargetType.ALLY &&
				item.getType() != ItemTemplate.Type.SPELLBOOK)
			{
				// requires further work
				// (spell books mean that the PC will attempt to learn the spell
				DiyGuiUserInterface.instance.chooseACharacter(this);
			}
			else
			{
				useItem(null);
			}
		}
		else
		{
			useItem(null);
		}
	}
}
