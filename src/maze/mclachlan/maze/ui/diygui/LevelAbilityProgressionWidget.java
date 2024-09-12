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
import java.awt.Rectangle;
import java.util.*;
import mclachlan.diygui.DIYLabel;
import mclachlan.diygui.DIYPane;
import mclachlan.diygui.toolkit.DIYBorderLayout;
import mclachlan.diygui.toolkit.DIYFlowLayout;
import mclachlan.diygui.toolkit.DIYGridLayout;
import mclachlan.diygui.toolkit.DIYToolkit;
import mclachlan.maze.data.StringUtil;
import mclachlan.maze.stat.CharacterClass;
import mclachlan.maze.stat.LevelAbility;
import mclachlan.maze.stat.LevelAbilityProgression;

/**
 *
 */
public class LevelAbilityProgressionWidget extends DIYPane
{
	private final boolean clip;
	private final int levels;
	private final LevelAbilityActionListener listener = new LevelAbilityActionListener();

	private ArrayList<DIYPane> rows;

	/*-------------------------------------------------------------------------*/
	public LevelAbilityProgressionWidget(CharacterClass cc)
	{
		this(cc, LevelAbilityProgression.MAX_LEVELS, null, false);
	}

	/*-------------------------------------------------------------------------*/

	/**
	 * @param levels
	 * 	The number of levels to display, starting at 1
	 */
	public LevelAbilityProgressionWidget(CharacterClass cc, int levels, Rectangle bounds, boolean clip)
	{
		this.clip = clip;
		if (bounds != null)
		{
			setBounds(bounds);
		}
		this.levels = levels;
		this.buildGUI(levels);
		if (cc != null)
		{
			refresh(cc);
		}
	}

	/*-------------------------------------------------------------------------*/
	public void refresh(CharacterClass cc)
	{
		LevelAbilityProgression progression = cc.getProgression();
		for (int i=1; i<=levels; i++)
		{
			List<LevelAbility> forLevel = progression.getForLevel(i);

			DIYPane row = rows.get(i-1);
			refresh(forLevel, row);
		}
	}

	/*-------------------------------------------------------------------------*/
	private void buildGUI(int levels)
	{
		int gap = 2;

		this.setLayoutManager(new DIYBorderLayout(0, 0));

		DIYPane left = new DIYPane(new DIYGridLayout(1, levels, gap, gap));
		DIYPane right = new DIYPane(new DIYGridLayout(1, levels, gap, gap));

		this.add(left, DIYBorderLayout.Constraint.WEST);
		this.add(right, DIYBorderLayout.Constraint.CENTER);
		this.doLayout();

		rows = new ArrayList<>();

		for (int i=1; i<=levels; i++)
		{
			addRow(
				left,
				right,
				StringUtil.getUiLabel("lapw.level", i));
		}
	}

	/*-------------------------------------------------------------------------*/
	private void addRow(DIYPane left, DIYPane right, String label)
	{
		DIYPane labelHack = new DIYPane(new DIYFlowLayout(0, 0, DIYToolkit.Align.LEFT));
		labelHack.add(getLabel(label));
		left.add(labelHack);

		DIYPane row = new DIYPane(new DIYFlowLayout(0, 0, DIYToolkit.Align.LEFT));
		right.add(row);

		rows.add(row);
	}

	/*-------------------------------------------------------------------------*/
	private void refresh(List<LevelAbility> abilities, DIYPane row)
	{
		row.removeAllChildren();

		int textSum = 0, textMax = 40;

		for (int i = 0; i < abilities.size(); i++)
		{
			LevelAbility la = abilities.get(i);

			String text = StringUtil.getGamesysString(
				la.getDisplayName(), false, la.getDisplayArgs());

			if (i < abilities.size()-1)
			{
				text += ",";
			}

			DIYLabel diyLabel = new DIYLabel(text);

			diyLabel.addActionListener(listener);
			diyLabel.setActionPayload(la);

			row.add(diyLabel);

			textSum += text.length();

			if (clip && textSum > textMax)
			{
				diyLabel.setText(text.substring(0,5)+"...");

				// stop adding labels
				break;
			}
		}
		doLayout();
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
