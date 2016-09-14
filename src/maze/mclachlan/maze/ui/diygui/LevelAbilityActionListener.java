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

package mclachlan.maze.ui.diygui;

import java.awt.Rectangle;
import java.util.*;
import mclachlan.diygui.toolkit.ActionEvent;
import mclachlan.diygui.toolkit.ActionListener;
import mclachlan.maze.data.StringUtil;
import mclachlan.maze.game.Maze;
import mclachlan.maze.stat.*;

/**
 * Pops up a modifier display dialog.
 */
public class LevelAbilityActionListener implements ActionListener
{
	/*-------------------------------------------------------------------------*/
	public void actionPerformed(ActionEvent event)
	{
		popupLevelAbilityDetailsDialog(
			(LevelAbility)event.getPayload());
	}

	/*-------------------------------------------------------------------------*/
	private void popupLevelAbilityDetailsDialog(
		LevelAbility ability)
	{
		if (ability == null)
		{
			return;
		}

		String title = StringUtil.getGamesysString(
			ability.getDisplayName(),
			false,
			ability.getDisplayArgs());

		StringBuilder text = new StringBuilder(
			StringUtil.getGamesysString(ability.getDescription()));

		if (ability instanceof StatModifierLevelAbility)
		{
			descModifiers("laal.modifiers", ((StatModifierLevelAbility)ability).getModifier(), text);
		}
		else if (ability instanceof BannerModifierLevelAbility)
		{
			descModifiers("laal.banner.modifiers", ((BannerModifierLevelAbility)ability).getModifier(), text);
		}
		else if (ability instanceof AddSpellPicks)
		{
			text.append("\n\n").append(StringUtil.getUiLabel("laal.spellpicks")).append(" +");
			text.append(((AddSpellPicks)ability).getSpellPicks());
		}

		//center it
		int x = DiyGuiUserInterface.SCREEN_WIDTH/4;
		int y = DiyGuiUserInterface.SCREEN_HEIGHT/4;
		Rectangle rectangle = new Rectangle(x, y,
			DiyGuiUserInterface.SCREEN_WIDTH/2, DiyGuiUserInterface.SCREEN_HEIGHT/2);

		OkDialogWidget d = new OkDialogWidget(rectangle, title, text.toString());
		Maze.getInstance().getUi().showDialog(d);
	}

	/*-------------------------------------------------------------------------*/
	private void descModifiers(String modLabel, StatModifier modifiers,
		StringBuilder text)
	{
		text.append("\n\n").append(StringUtil.getUiLabel(modLabel)).append(" ");
		List<Stats.Modifier> sortedModifiers = new ArrayList<Stats.Modifier>(modifiers.getModifiers().keySet());
		Collections.sort(sortedModifiers);

		for (Stats.Modifier modifier : sortedModifiers)
		{
			text.append(StringUtil.getModifierName(modifier));
			text.append(" ");
			text.append(StringUtil.descModifier(modifier, modifiers.getModifier(modifier)));

			if (sortedModifiers.indexOf(modifier) < sortedModifiers.size()-1)
			{
				text.append(", ");
			}
		}
	}
}
