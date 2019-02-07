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
import mclachlan.diygui.toolkit.*;
import mclachlan.maze.data.Database;
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
public class PropertiesDisplayWidget extends ContainerWidget implements ActionListener
{
	private PlayerCharacter character;

	private static final int ROWS = 25;
	private static final int STARTING_ROW = 4;

	DIYLabel[] propertiesLabels = new DIYLabel[ROWS];
	DIYLabel[] conditionLabels = new DIYLabel[ROWS];
	private DIYLabel nameLabel = new DIYLabel("", DIYToolkit.Align.LEFT);
	private DIYLabel propertiesHeader = getLabel("Properties", Constants.Colour.ATTRIBUTES_CYAN);
	private DIYLabel conditionsHeader = getLabel("Conditions", Constants.Colour.ATTRIBUTES_CYAN);
	private ActionListener modifiersListener;

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

		this.modifiersListener = new ModifiersDisplayActionListener();

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
		switch (col)
		{
			case 0:
				this.propertiesLabels[row] = label;
				label.addActionListener(this.modifiersListener);
				label.setActionPayload(this.character);
				break;
			case 1:
				this.conditionLabels[row] = label;
				label.addActionListener(this);
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
			conditionLabels[i].setIcon(null);
		}

		int rowCount = STARTING_ROW;
		for (Stats.Modifier modifier : Stats.propertiesModifiers)
		{
			// todo: display excess properties somehow
			if (rowCount >= ROWS)
			{
				break;
			}

			int value = this.character.getModifier(modifier);
			if (value > 0)
			{
				propertiesLabels[rowCount].setText(descModifier(modifier, value));
				propertiesLabels[rowCount].setActionMessage(modifier.toString());
				propertiesLabels[rowCount].setActionPayload(character);
				rowCount++;
			}
		}

		rowCount = STARTING_ROW;
		for (Condition c : character.getConditions())
		{
			conditionLabels[rowCount].setText(c.getDisplayName() + " (level " + c.getCastingLevel() +")");
			conditionLabels[rowCount].setIcon(Database.getInstance().getImage(c.getDisplayIcon()));
			conditionLabels[rowCount].setActionPayload(c);
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
	private String descModifier(Stats.Modifier modifier, int value)
	{
		String modifierName = StringUtil.getModifierName(modifier);

		Stats.ModifierMetric metric = modifier.getMetric();
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

	/*-------------------------------------------------------------------------*/
	private void popupConditionDetailsDialog(Condition condition)
	{
		DiyGuiUserInterface.instance.popupConditionDetailsDialog(condition);
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public void actionPerformed(ActionEvent event)
	{
		if (event.getPayload() instanceof Condition)
		{
			popupConditionDetailsDialog((Condition)event.getPayload());
		}
	}

	/*-------------------------------------------------------------------------*/

}
