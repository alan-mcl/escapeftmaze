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

import java.awt.Color;
import java.util.*;
import mclachlan.diygui.DIYLabel;
import mclachlan.diygui.DIYPane;
import mclachlan.diygui.toolkit.DIYBorderLayout;
import mclachlan.diygui.toolkit.DIYFlowLayout;
import mclachlan.diygui.toolkit.DIYGridLayout;
import mclachlan.diygui.toolkit.DIYToolkit;
import mclachlan.maze.data.StringUtil;
import mclachlan.maze.stat.*;

/**
 *
 */
public class LevelAbilityProgressionWidget extends DIYPane
{
	private CharacterClass cc;
	private  LevelAbilityActionListener listener = new LevelAbilityActionListener();

	/*-------------------------------------------------------------------------*/
	public LevelAbilityProgressionWidget(CharacterClass cc)
	{
		this.cc = cc;
		this.buildGUI();
	}

	/*-------------------------------------------------------------------------*/
	private void buildGUI()
	{
		int inset = 3;

		this.setLayoutManager(new DIYBorderLayout(inset, inset));

		DIYPane left = new DIYPane(new DIYGridLayout(1, LevelAbilityProgression.MAX_LEVELS, inset, inset));
		DIYPane right = new DIYPane(new DIYGridLayout(1, LevelAbilityProgression.MAX_LEVELS, inset, inset));

		LevelAbilityProgression progression = cc.getProgression();

		this.add(left, DIYBorderLayout.Constraint.WEST);
		this.add(right, DIYBorderLayout.Constraint.CENTER);
		this.doLayout();

		for (int i=1; i<=LevelAbilityProgression.MAX_LEVELS; i++)
		{
			List<LevelAbility> forLevel = progression.getForLevel(i);

			addRow(
				left,
				right,
				StringUtil.getUiLabel("lapw.level", i),
				forLevel);
		}

	}

	/*-------------------------------------------------------------------------*/
	private void addRow(DIYPane left, DIYPane right, String label,
		List<LevelAbility> abilities)
	{
		DIYPane labelHack = new DIYPane(new DIYFlowLayout(0, 0, DIYToolkit.Align.LEFT));
		labelHack.add(getLabel(label));
		left.add(labelHack);

		DIYPane row = new DIYPane(new DIYFlowLayout(0, 0, DIYToolkit.Align.LEFT));
		right.add(row);

		for (int i = 0; i < abilities.size(); i++)
		{
			LevelAbility la = abilities.get(i);

			String text = StringUtil.getGamesysString(
				la.getDisplayName(), false, la.getDisplayArgs());

			if (i < abilities.size()-1)
			{
				text += ",";
			}


			DIYLabel diyLabel = new DIYLabel(
				text);

			diyLabel.addActionListener(listener);
			diyLabel.setActionPayload(la);

			row.add(diyLabel);
		}
	}

	/*-------------------------------------------------------------------------*/
	private DIYLabel getLabel(String text)
	{
		return new DIYLabel(text, DIYToolkit.Align.LEFT);
	}

	/*-------------------------------------------------------------------------*/
	private DIYLabel getLabel(String text, Color colour)
	{
		DIYLabel result = new DIYLabel(text, DIYToolkit.Align.LEFT);
		result.setForegroundColour(colour);
		return result;
	}
}
