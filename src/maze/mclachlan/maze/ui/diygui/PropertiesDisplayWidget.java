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
import java.awt.Insets;
import java.awt.Rectangle;
import mclachlan.diygui.DIYLabel;
import mclachlan.diygui.DIYPanel;
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

	private static final int ROWS = 20;
	private static final int COLS = 2;

	private final DIYLabel[] propertiesLabels = new DIYLabel[(ROWS-1)*COLS];
	private final DIYLabel[] conditionLabels = new DIYLabel[(ROWS-1)*COLS];
	private DIYLabel nameLabel;
	private final ActionListener modifiersListener;

	/*-------------------------------------------------------------------------*/
	public PropertiesDisplayWidget(Rectangle bounds)
	{
		super(bounds);

		this.modifiersListener = new ModifiersDisplayActionListener();

		this.buildGUI(bounds);
	}

	/*-------------------------------------------------------------------------*/
	private void buildGUI(Rectangle bounds)
	{
		RendererProperties rp = DIYToolkit.getInstance().getRendererProperties();

		int inset = rp.getProperty(RendererProperties.Property.INSET);
//		int titleHeight = rp.getProperty(RendererProperties.Property.TITLE_PANE_HEIGHT);
		int titleHeight = 20;
		int buttonPaneHeight = rp.getProperty(RendererProperties.Property.BUTTON_PANE_HEIGHT);
		int headerOffset = titleHeight + DiyGuiUserInterface.SCREEN_EDGE_INSET;
		int contentTop = headerOffset + inset;
		int contentHeight = height - contentTop - buttonPaneHeight - inset - DiyGuiUserInterface.SCREEN_EDGE_INSET;
		int panelBorderInset = rp.getProperty(RendererProperties.Property.PANEL_MED_BORDER);
		int frameBorderInset = rp.getProperty(RendererProperties.Property.PANEL_LIGHT_BORDER);

		int column1x = bounds.x + inset;
		int columnWidth = (width - 3 * inset) / 2;

		int column2x = column1x + columnWidth + inset;

		// screen title
		DIYLabel title = getSubTitle(StringUtil.getUiLabel("pdw.title"));
		title.setBounds(
			200, DiyGuiUserInterface.SCREEN_EDGE_INSET,
			DiyGuiUserInterface.SCREEN_WIDTH - 400, titleHeight);

		nameLabel = new DIYLabel("", DIYToolkit.Align.LEFT);
		nameLabel.addActionListener(this);

		// personal info
		DIYPanel personalPanel = new DIYPanel();
		personalPanel.setStyle(DIYPanel.Style.PANEL_LIGHT);
		personalPanel.setLayoutManager(null);
		personalPanel.setBounds(
			column1x,
			contentTop,
			(width - 5 * inset) / 3 * 2,
			frameBorderInset * 2 + 30);

		nameLabel.setBounds(
			personalPanel.x + frameBorderInset + inset / 2,
			personalPanel.y + frameBorderInset,
			personalPanel.width / 2,
			20);

		personalPanel.add(nameLabel);

		// properties
		DIYPanel propertiesPanel = new DIYPanel();
		propertiesPanel.setStyle(DIYPanel.Style.PANEL_MED);
		propertiesPanel.setLayoutManager(new DIYGridLayout(COLS, ROWS, inset/2, inset/2));
		propertiesPanel.setInsets(new Insets(panelBorderInset, panelBorderInset +inset/2, panelBorderInset, panelBorderInset +inset/2));
		propertiesPanel.setBounds(
			column1x,
			personalPanel.y + personalPanel.height + inset,
			columnWidth,
			contentHeight - personalPanel.height - inset*4 -buttonPaneHeight);

		DIYLabel propertiesHeader = getLabel(StringUtil.getUiLabel("pdw.properties"), Constants.Colour.ATTRIBUTES_CYAN);

		propertiesPanel.add(propertiesHeader);
		propertiesPanel.add(new DIYLabel());
		for (int i=0; i<propertiesLabels.length; i++)
		{
			propertiesLabels[i] = new DIYLabel("", DIYToolkit.Align.LEFT);
			propertiesLabels[i].addActionListener(modifiersListener);
			propertiesLabels[i].setActionPayload(this.character);
		}

		for (int i=0; i<propertiesLabels.length/2; i++)
		{
			propertiesPanel.add(propertiesLabels[i]);
			propertiesPanel.add(propertiesLabels[i+(ROWS-1)]);
		}

		// conditions
		DIYPanel conditionsPanel = new DIYPanel();
		conditionsPanel.setStyle(DIYPanel.Style.PANEL_MED);
		conditionsPanel.setLayoutManager(new DIYGridLayout(COLS, ROWS, inset/2, inset/2));
		conditionsPanel.setInsets(new Insets(panelBorderInset, panelBorderInset +inset/2, panelBorderInset, panelBorderInset +inset/2));
		conditionsPanel.setBounds(
			column2x,
			personalPanel.y + personalPanel.height + inset,
			columnWidth,
			contentHeight - personalPanel.height - inset*4 -buttonPaneHeight);

		DIYLabel conditionsHeader = getLabel(StringUtil.getUiLabel("pdw.conditions"), Constants.Colour.ATTRIBUTES_CYAN);

		conditionsPanel.add(conditionsHeader);
		conditionsPanel.add(new DIYLabel());
		for (int i=0; i<((ROWS-1)*COLS); i++)
		{
			conditionLabels[i] = new DIYLabel("", DIYToolkit.Align.LEFT);
			conditionLabels[i].addActionListener(this);
			conditionLabels[i].setActionPayload(this.character);
		}

		for (int i=0; i<propertiesLabels.length/2; i++)
		{
			conditionsPanel.add(conditionLabels[i]);
			conditionsPanel.add(conditionLabels[i+(ROWS-1)]);
		}

		this.add(title);
		this.add(personalPanel);
		this.add(propertiesPanel);
		this.add(conditionsPanel);

		doLayout();
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

		for (int i=0; i<propertiesLabels.length; i++)
		{
			propertiesLabels[i].setText("");
		}

		for (int i=0; i<conditionLabels.length; i++)
		{
			conditionLabels[i].setText("");
			conditionLabels[i].setIcon(null);
		}

		int rowCount = 0;
		for (Stats.Modifier modifier : Stats.propertiesModifiers)
		{
			// todo: display excess properties somehow
			if (rowCount >= propertiesLabels.length)
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

		rowCount = 0;
		for (Condition c : character.getConditions())
		{
			// todo: display excess conditions somehow
			if (rowCount >= conditionLabels.length)
			{
				break;
			}

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
	public boolean actionPerformed(ActionEvent event)
	{
		if (event.getPayload() instanceof Condition)
		{
			popupConditionDetailsDialog((Condition)event.getPayload());
			return true;
		}

		return false;
	}

	/*-------------------------------------------------------------------------*/
	private DIYLabel getSubTitle(String titleText)
	{
		DIYLabel title = new DIYLabel(titleText);
		title.setForegroundColour(GOLD);
		Font defaultFont = DiyGuiUserInterface.instance.getDefaultFont();
		Font f = defaultFont.deriveFont(Font.PLAIN, defaultFont.getSize() + 3);
		title.setFont(f);
		return title;
	}

}
