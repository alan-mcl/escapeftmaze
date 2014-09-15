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

import java.awt.Font;
import mclachlan.diygui.DIYLabel;
import mclachlan.diygui.DIYPane;
import mclachlan.diygui.toolkit.ActionListener;
import mclachlan.diygui.toolkit.DIYGridLayout;
import mclachlan.diygui.toolkit.DIYToolkit;
import mclachlan.maze.data.StringUtil;
import mclachlan.maze.stat.Stats;

import static mclachlan.maze.ui.diygui.Constants.Colour.GOLD;

/**
 *
 */
public class ResourcesDisplayWidget extends DIYPane
{
	private DIYLabel[] labels;
	private DIYLabel[] values;
	private int hitPoints;
	private int actionPoints;
	private int magicPoints;
	private final boolean percent;

	/*-------------------------------------------------------------------------*/
	public ResourcesDisplayWidget(
		String title, int hitPoints, int actionPoints, int magicPoints,
		boolean percent)
	{
		this.hitPoints = hitPoints;
		this.actionPoints = actionPoints;
		this.magicPoints = magicPoints;
		this.percent = percent;

		int inset = 2;
		int rows = 3;
		int columns = 3;

		this.setLayoutManager(new DIYGridLayout(columns, rows+1, inset, inset));
		this.labels = new DIYLabel[rows];
		this.values = new DIYLabel[rows];

		ActionListener listener = new ModifiersDisplayActionListener();

		DIYLabel top = getTitle(title);
		top.setForegroundColour(Constants.Colour.GOLD);
		add(new DIYLabel(""));
		add(top);
		add(new DIYLabel(""));

		for (int i=0; i<rows; i++)
		{
			labels[i] = new DIYLabel("", DIYToolkit.Align.LEFT);
			labels[i].addActionListener(listener);
			values[i] = new DIYLabel();
			values[i].addActionListener(listener);

			this.add(labels[i]);
			this.add(new DIYLabel());
			this.add(values[i]);
		}

		this.display(hitPoints, actionPoints, magicPoints);
	}

	/*-------------------------------------------------------------------------*/
	public void display(int hitPoints, int actionPoints, int magicPoints)
	{
		for (int i = 0; i < labels.length; i++)
		{
			labels[i].setText("");
			labels[i].setActionMessage(null);
			values[i].setText("");
			values[i].setActionMessage(null);
		}

		labels[0].setText(StringUtil.getModifierName(Stats.Modifiers.HIT_POINTS));
		labels[0].setActionMessage(Stats.Modifiers.HIT_POINTS);
		values[0].setText(descValue(hitPoints));
		values[0].setActionMessage(Stats.Modifiers.HIT_POINTS);

		labels[1].setText(StringUtil.getModifierName(Stats.Modifiers.ACTION_POINTS));
		labels[1].setActionMessage(Stats.Modifiers.ACTION_POINTS);
		values[1].setText(descValue(actionPoints));
		values[1].setActionMessage(Stats.Modifiers.ACTION_POINTS);

		labels[2].setText(StringUtil.getModifierName(Stats.Modifiers.MAGIC_POINTS));
		labels[2].setActionMessage(Stats.Modifiers.MAGIC_POINTS);
		values[2].setText(descValue(magicPoints));
		values[2].setActionMessage(Stats.Modifiers.MAGIC_POINTS);
	}

	/*-------------------------------------------------------------------------*/
	private String descValue(int value)
	{
		if (percent)
		{
			return value+"%";
		}
		else
		{
			return ""+value;
		}
	}

	/*-------------------------------------------------------------------------*/
	private DIYLabel getTitle(String titleText)
	{
		DIYLabel title = new DIYLabel(titleText, DIYToolkit.Align.CENTER);
		title.setForegroundColour(GOLD);
		Font defaultFont = DiyGuiUserInterface.instance.getDefaultFont();
		Font f = defaultFont.deriveFont(Font.PLAIN, defaultFont.getSize()+3);
		title.setFont(f);
		return title;
	}
}
