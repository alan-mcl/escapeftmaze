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

import java.awt.Font;
import java.util.*;
import mclachlan.diygui.DIYLabel;
import mclachlan.diygui.DIYPane;
import mclachlan.diygui.toolkit.ActionListener;
import mclachlan.diygui.toolkit.DIYGridLayout;
import mclachlan.diygui.toolkit.DIYToolkit;
import mclachlan.maze.data.StringUtil;
import mclachlan.maze.stat.StatModifier;
import mclachlan.maze.stat.Stats;
import mclachlan.maze.util.MazeException;

import static mclachlan.maze.ui.diygui.Constants.Colour.GOLD;

/**
 *
 */
public class StatModifierDisplayWidget extends DIYPane
{
	private int rows;
	private DIYLabel[] labels;
	private DIYLabel[] values;
	private List<Stats.Modifier> toInclude;
	private boolean displayZeroes;
	private StatModifier modifier;

	/*-------------------------------------------------------------------------*/
	public StatModifierDisplayWidget(
		String title,
		StatModifier current,
		int nrRows,
		List<Stats.Modifier> toInclude,
		boolean displayZeroes,
		boolean unknown)
	{
		this.toInclude = toInclude;
		this.displayZeroes = displayZeroes;
		int inset = 2;
		this.rows = nrRows;
		int columns = 3;

		this.setLayoutManager(new DIYGridLayout(columns, this.rows+1, inset, inset));
		this.labels = new DIYLabel[nrRows];
		this.values = new DIYLabel[nrRows];

		ActionListener listener = new ModifiersDisplayActionListener();

		DIYLabel top = getTitle(title);
		top.setForegroundColour(Constants.Colour.GOLD);
		add(new DIYLabel(""));
		add(top);
		add(new DIYLabel(""));

		for (int i=0; i<nrRows; i++)
		{
			labels[i] = new DIYLabel("", DIYToolkit.Align.LEFT);
			labels[i].addActionListener(listener);
			values[i] = new DIYLabel();
			values[i].addActionListener(listener);

			this.add(labels[i]);
			this.add(new DIYLabel());
			this.add(values[i]);
		}

		this.setStatModifier(current, unknown);
	}

	/*-------------------------------------------------------------------------*/
	public void setStatModifier(StatModifier current, boolean unknown)
	{
		this.modifier = current;
		
		for (int i = 0; i < labels.length; i++)
		{
			labels[i].setText("");
			labels[i].setActionMessage(null);
			values[i].setText("");
			values[i].setActionMessage(null);
		}

		if (current != null)
		{
			int count = 0;

			for (Stats.Modifier mod : toInclude)
			{
				int modifierValue = current.getModifier(mod);
				if ((displayZeroes || modifierValue != 0) && count < rows)
				{
					String modifierName = StringUtil.getModifierName(mod);
					labels[count].setText(modifierName);
					labels[count].setActionMessage(mod.toString());
					if (unknown)
					{
						values[count].setText("?");
					}
					else
					{
						values[count].setText(descModifier(mod, modifierValue));
					}
					values[count].setActionMessage(mod.toString());

					count++;
				}
			}
		}
	}

	/*-------------------------------------------------------------------------*/

	public StatModifier getStatModifier()
	{
		return modifier;
	}

	/*-------------------------------------------------------------------------*/
	private String descModifier(Stats.Modifier modifier, int value)
	{
		Stats.ModifierMetric metric = modifier.getMetric();
		switch (metric)
		{
			case PLAIN:
				return descPlainModifier(value);
			case BOOLEAN:
				if (value >= 0)
				{
					return "";
				}
				else
				{
					return "cancelled";
				}
			case PERCENTAGE:
				return value+"%";
			default:
				throw new MazeException(metric.name());
		}
	}

	/*-------------------------------------------------------------------------*/
	private String descPlainModifier(int value)
	{
		if (value >= 0)
		{
			return "+"+value;
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
