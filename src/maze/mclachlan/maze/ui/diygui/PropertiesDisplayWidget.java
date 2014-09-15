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

import java.awt.Color;
import java.awt.Font;
import java.awt.Rectangle;
import mclachlan.diygui.DIYLabel;
import mclachlan.diygui.DIYPane;
import mclachlan.diygui.toolkit.ActionListener;
import mclachlan.diygui.toolkit.ContainerWidget;
import mclachlan.diygui.toolkit.DIYGridLayout;
import mclachlan.diygui.toolkit.DIYToolkit;
import mclachlan.maze.data.StringUtil;
import mclachlan.maze.stat.PlayerCharacter;
import mclachlan.maze.stat.Stats;
import mclachlan.maze.stat.condition.Condition;
import mclachlan.maze.util.MazeException;

import static java.awt.Color.WHITE;
import static mclachlan.maze.ui.diygui.Constants.Colour.GOLD;

/**
 *
 */
public class PropertiesDisplayWidget extends ContainerWidget
{
	private PlayerCharacter character;

	private static final int ROWS = 25;
	private static final int STARTING_ROW = 4;

	DIYLabel[] propertiesLabels = new DIYLabel[ROWS];
	DIYLabel[] conditionLabels = new DIYLabel[ROWS];
	private DIYLabel nameLabel = new DIYLabel("", DIYToolkit.Align.LEFT);
	private DIYLabel propertiesHeader = getLabel("Properties", Constants.Colour.ATTRIBUTES_CYAN);
	private DIYLabel conditionsHeader = getLabel("Conditions", Constants.Colour.ATTRIBUTES_CYAN);
	private ActionListener listener;

	Object[][] layout = new Object[][]
	{
		{ null, null, null},
		{ null, null, null},
		{ null, null, null},
		{ propertiesHeader, conditionsHeader, null},
		{ null,	null,	null},
		{ null,	null,	null},
		{ null,	null,	null},
		{ null,	null,	null},
		{ null,	null,	null},
		{ null,	null,	null},
		{ null,	null,	null},
		{ null,	null,	null},
		{ null,	null,	null},
		{ null,	null,	null},
		{ null,	null,	null},
		{ null,	null,	null},
		{ null,	null,	null},
		{ null,	null,	null},
		{ null,	null,	null},
		{ null,	null,	null},
		{ null,	null,	null},
		{ null,	null,	null},
		{ null,	null,	null},
		{ null,	null,	null},
		{ null,	null,	null},
	};

	/*-------------------------------------------------------------------------*/
	public PropertiesDisplayWidget(Rectangle bounds)
	{
		super(bounds);

		this.listener = new ModifiersDisplayActionListener();

		this.buildGUI();
	}

	/*-------------------------------------------------------------------------*/
	private void buildGUI()
	{
		DIYLabel topLabel = new DIYLabel("Properties", DIYToolkit.Align.CENTER);
		topLabel.setBounds(162, 0, DiyGuiUserInterface.SCREEN_WIDTH - 162, 30);
		topLabel.setForegroundColour(GOLD);
		Font defaultFont = DiyGuiUserInterface.instance.getDefaultFont();
		Font f = defaultFont.deriveFont(Font.BOLD, defaultFont.getSize()+5);
		topLabel.setFont(f);
		this.add(topLabel);

		int columns = 3;
		int inset = 2;
		int rows = ROWS;
		int rowHeight = height / rows;

		nameLabel.setForegroundColour(WHITE);
		nameLabel.setBounds(x, y+rowHeight-inset, width, rowHeight);
		this.add(nameLabel);

		DIYPane pane = new DIYPane(x,y,width,height);
		pane.setLayoutManager(new DIYGridLayout(columns, rows, inset, inset));
		this.add(pane);

		for (int i = 0; i < layout.length; i++)
		{
			Object[] row = layout[i];
			for (int j = 0; j < row.length; j++)
			{
				if (row[j] == null)
				{
					this.addLabel(pane, getBlank(), j, i);
				}
				else if (row[j] instanceof DIYLabel)
				{
					this.addLabel(pane, (DIYLabel)row[j], j, i);
				}
				else
				{
					throw new MazeException("Invalid cell: "+row[j]);
				}
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	private void addLabel(DIYPane pane, DIYLabel label, int col, int row)
	{
		pane.add(label);
		label.addActionListener(this.listener);
		label.setActionPayload(this.character);
		switch (col)
		{
			case 0: this.propertiesLabels[row] = label;
				break;
			case 1: this.conditionLabels[row] = label;
				break;
			case 2:
			default:
				// no op
		}
	}

	/*-------------------------------------------------------------------------*/
	public void setCharacter(PlayerCharacter character)
	{
		this.character = character;

		if (character != null)
		{
			refreshData();
		}
	}

	/*-------------------------------------------------------------------------*/
	public void refreshData()
	{
		if (this.character == null)
		{
			return;
		}

		nameLabel.setForegroundColour(WHITE);
		nameLabel.setText(this.character.getName()+", "+
			"level " + this.character.getLevel() + " " +
			character.getGender().getName() + " " +
			character.getRace().getName() + " " +
			character.getCharacterClass().getName());

		for (int i=STARTING_ROW; i<ROWS; i++)
		{
			propertiesLabels[i].setText("");
			conditionLabels[i].setText("");
		}

		int rowCount = STARTING_ROW;
		for (String modifier : Stats.propertiesModifiers)
		{
			int value = this.character.getModifier(modifier);
			if (value > 0)
			{
				propertiesLabels[rowCount].setText(descModifier(modifier, value));
				propertiesLabels[rowCount].setActionMessage(modifier);
				propertiesLabels[rowCount].setActionPayload(character);
				rowCount++;
			}
		}

		rowCount = STARTING_ROW;
		for (Condition c : character.getConditions())
		{
			conditionLabels[rowCount].setText(c.getDisplayName() + " (level " + c.getCastingLevel() +")");
			conditionLabels[rowCount].setActionPayload(character);
			rowCount++;
		}
	}

	/*-------------------------------------------------------------------------*/
	private DIYLabel getLabel(String text, Color colour)
	{
		DIYLabel result = new DIYLabel(text, DIYToolkit.Align.LEFT);
		result.setForegroundColour(colour);
		return result;
	}

	/*-------------------------------------------------------------------------*/
	public String getWidgetName()
	{
		return DIYToolkit.PANE;
	}

	/*-------------------------------------------------------------------------*/
	public static DIYLabel getBlank()
	{
		return new DIYLabel("", DIYToolkit.Align.LEFT);
	}

	/*-------------------------------------------------------------------------*/
	private String descModifier(String modifier, int value)
	{
		String modifierName = StringUtil.getModifierName(modifier);

		Stats.ModifierMetric metric = Stats.ModifierMetric.getMetric(modifier);
		switch (metric)
		{
			case PLAIN:
				return modifierName + " " + descPlainModifier(value);
			case BOOLEAN:
				if (value >= 0)
				{
					return modifierName;
				}
				else
				{
					return "Cancel "+modifierName;
				}
			case PERCENTAGE:
				return modifierName + " " + descPlainModifier(value)+"%";
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
}
