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

import java.awt.Rectangle;
import mclachlan.diygui.toolkit.ActionEvent;
import mclachlan.diygui.toolkit.ActionListener;
import mclachlan.maze.data.StringUtil;
import mclachlan.maze.game.Maze;
import mclachlan.maze.stat.GameSys;
import mclachlan.maze.stat.ModifierValue;
import mclachlan.maze.stat.PlayerCharacter;
import mclachlan.maze.stat.Stats;

/**
 * Pops up a modifier display dialog.
 */
public class ModifiersDisplayActionListener implements ActionListener
{
	/*-------------------------------------------------------------------------*/
	public void actionPerformed(ActionEvent event)
	{
		String modifier = event.getMessage();

		if (modifier != null)
		{
			popupModifierDetailsDialog(
				Stats.Modifier.valueOf(modifier),
				(PlayerCharacter)event.getPayload());
		}
	}

	/*-------------------------------------------------------------------------*/
	private void popupModifierDetailsDialog(Stats.Modifier modifier, PlayerCharacter pc)
	{
		if (modifier == null)
		{
			return;
		}

		String title = StringUtil.getModifierName(modifier);

		StringBuilder text = new StringBuilder(StringUtil.getModifierDescription(modifier));

		if (pc != null)
		{
			ModifierValue modifierValue = pc.getModifierValue(modifier, true);
			if (!modifierValue.getInfluences().isEmpty())
			{
				text.append("\n\n").
					append(StringUtil.getUiLabel("mdal.influences")).append("\n");
			}
			for (ModifierValue.ModifierInfluence i : modifierValue.getInfluences())
			{
				text.append(i.getName())
					.append(": ")
					.append(Stats.descModifier(modifier, i.getValue()))
					.append("\n");
			}

			if (pc != null && Stats.regularModifiers.contains(modifier))
			{
				text.append("\n\n").
					append(StringUtil.getUiLabel("mdal.practice")).append(" ")
					.append(pc.getPractice().getPracticePoints(modifier))
					.append("/")
					.append(GameSys.getInstance().getPracticePointsRequired(pc, modifier));
			}
		}

		//center it
		int x = DiyGuiUserInterface.SCREEN_WIDTH/4;
		int y = DiyGuiUserInterface.SCREEN_HEIGHT/4;
		Rectangle rectangle = new Rectangle(x, y,
			DiyGuiUserInterface.SCREEN_WIDTH/2, DiyGuiUserInterface.SCREEN_HEIGHT/2);

		OkDialogWidget d = new OkDialogWidget(rectangle, title, text.toString());
		Maze.getInstance().getUi().showDialog(d);
	}
}
