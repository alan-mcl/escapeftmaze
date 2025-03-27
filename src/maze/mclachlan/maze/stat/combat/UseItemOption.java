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

import java.util.*;
import mclachlan.maze.data.StringUtil;
import mclachlan.maze.game.ActorEncounter;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.event.ModifySuppliesEvent;
import mclachlan.maze.map.TileScript;
import mclachlan.maze.map.script.GrantGoldEvent;
import mclachlan.maze.stat.*;
import mclachlan.maze.stat.magic.Spell;
import mclachlan.maze.ui.diygui.UseItem;
import mclachlan.maze.ui.diygui.UseItemCallback;
import mclachlan.maze.util.MazeException;

import static mclachlan.maze.stat.magic.MagicSys.SpellTargetType.*;

/**
 * Presents the user with the option of using an item.
 */
public class UseItemOption extends ActorActionOption implements UseItemCallback
{
	private ActorActionIntention intention;
	private ActionOptionCallback callback;
	private Combat combat;

	/*-------------------------------------------------------------------------*/
	public UseItemOption()
	{
		super("Use Item", "aao.use.item");
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public ActorActionIntention getIntention()
	{
		return intention;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public void select(UnifiedActor actor, Combat combat,
		ActionOptionCallback callback)
	{
		this.combat = combat;
		this.callback = callback;
		new UseItem(
			getActor().getName(),
			StringUtil.getUiLabel("isd.use"),
			this, (PlayerCharacter)getActor());
	}

	/*-------------------------------------------------------------------------*/
	public boolean useItem(
		Item item, PlayerCharacter user, int userIndex, SpellTarget target)
	{
		ActorGroup selectedFoeGroup = null;
		if (combat != null)
		{
			selectedFoeGroup = Maze.getInstance().getUi().getSelectedFoeGroup();
		}

		if (item.getType() == ItemTemplate.Type.SUPPLIES && item.isIdentified())
		{
			int supplies = TileScript.extractSupplies(new ArrayList<>(List.of(item)));
			Maze.getInstance().appendEvents(
				new ModifySuppliesEvent(supplies));
			user.removeItem(item, true);

			return true;
		}
		else if (item.getType() == ItemTemplate.Type.MONEY && item.isIdentified())
		{
			int gold = TileScript.extractGold(new ArrayList<>(List.of(item)));
			Maze.getInstance().appendEvents(new GrantGoldEvent(gold));
			user.removeItem(item, true);

			return true;
		}

		SpellTarget spellTarget = target;

		Spell invokedSpell = item.getTemplate().getInvokedSpell();

		if (invokedSpell != null)
		{
			switch (invokedSpell.getTargetType())
			{
				// do not require target selection
				case CASTER:
					spellTarget = user;
					break;

				case PARTY:
					if (combat != null)
					{
						spellTarget = combat.getActorGroup(user);
					}
					else
					{
						spellTarget = Maze.getInstance().getParty();
					}
					break;

				case PARTY_BUT_NOT_CASTER:
					spellTarget = SpellTargetUtils.getActorGroupWithoutCaster(user);
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
						spellTarget = selectedFoeGroup.getActors().get(d.roll("SpecialAbilityOption foe targeting"));
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
					// no op needed, character has already been chosen by UseItem
					break;

				case NPC:
					ActorEncounter currentActorEncounter = Maze.getInstance().getCurrentActorEncounter();
					if (currentActorEncounter != null)
					{
						spellTarget = currentActorEncounter.getLeader();
					}
					else
					{
						spellTarget = null;
					}
					break;

				// makes no sense, never cast in combat
				case LOCK_OR_TRAP:
					spellTarget = null;
					break;

				default:
					throw new MazeException("Unrecognized spell target type: "
						+ invokedSpell.getTargetType());
			}
		}

		intention = new UseItemIntention(item, spellTarget);
		callback.selected(intention);
		this.combat = null;
		return true;
	}
}
