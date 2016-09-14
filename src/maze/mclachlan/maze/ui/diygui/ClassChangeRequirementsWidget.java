/*
 * Copyright (c) 2013 Alan McLachlan
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
import mclachlan.diygui.toolkit.DIYGridLayout;
import mclachlan.diygui.toolkit.DIYToolkit;
import mclachlan.maze.data.Database;
import mclachlan.maze.data.StringUtil;
import mclachlan.maze.stat.CharacterClass;
import mclachlan.maze.stat.PlayerCharacter;
import mclachlan.maze.stat.StatModifier;
import mclachlan.maze.stat.Stats;
import mclachlan.maze.util.MazeException;

/**
 *
 */
public class ClassChangeRequirementsWidget extends DIYPane
{
	private PlayerCharacter pc;

	private DIYLabel nameLabel = new DIYLabel("", DIYToolkit.Align.LEFT);

	/*-------------------------------------------------------------------------*/
	public ClassChangeRequirementsWidget(PlayerCharacter pc)
	{
		this.pc = pc;
		this.buildGUI();
	}

	/*-------------------------------------------------------------------------*/
	private void buildGUI()
	{
		int inset = 3;
		int rows = 40;
		int columns = 7;
		nameLabel.setForegroundColour(Color.WHITE);
		nameLabel.setText(
			StringUtil.getUiLabel("ccrw.namelabel",
				this.pc.getName(),
				this.pc.getLevel(),
				pc.getGender().getName(),
				pc.getRace().getName(),
				pc.getCharacterClass().getName()));

		this.add(nameLabel);
		this.add(new DIYLabel());
		this.add(new DIYLabel());
		this.add(new DIYLabel());
		this.add(new DIYLabel());
		this.add(new DIYLabel());
		this.add(new DIYLabel());

		this.setLayoutManager(new DIYGridLayout(columns, rows, inset, inset));

		List<String> eligibleClasses = pc.getEligibleClasses();
		List<String> ineligibleClasses = pc.getIneligibleClasses();

		Collections.sort(eligibleClasses);
		Collections.sort(ineligibleClasses);

		addRow(StringUtil.getUiLabel("ccrw.current"), pc.getBaseModifiers(), this);

		if (!eligibleClasses.isEmpty())
		{
			// blank row
			for (int i = 0; i < columns; i++)
			{
				this.add(new DIYLabel());
			}
			this.add(getLabel(StringUtil.getUiLabel("ccrw.eligible"), Color.WHITE));
			this.add(new DIYLabel());
			this.add(new DIYLabel());
			this.add(new DIYLabel());
			this.add(new DIYLabel());
			this.add(new DIYLabel());
			this.add(new DIYLabel());

			for (String s : eligibleClasses)
			{
				CharacterClass cc = Database.getInstance().getCharacterClass(s);
				addRow(cc.getName() + ":", cc.getStartingModifiers(), this);
			}
		}

		if (!ineligibleClasses.isEmpty())
		{
			// blank row
			for (int i = 0; i < columns; i++)
			{
				this.add(new DIYLabel());
			}

			this.add(getLabel(StringUtil.getUiLabel("ccrw.ineligible"), Color.WHITE));
			this.add(new DIYLabel());
			this.add(new DIYLabel());
			this.add(new DIYLabel());
			this.add(new DIYLabel());
			this.add(new DIYLabel());
			this.add(new DIYLabel());

			for (String s : ineligibleClasses)
			{
				CharacterClass cc = Database.getInstance().getCharacterClass(s);
				addRow(cc.getName() + ":", cc.getStartingModifiers(), this);
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	private void addRow(String label, StatModifier sm, DIYPane pane)
	{
		pane.add(getLabel(label));
		pane.add(getModifierLabel(Stats.Modifier.BRAWN, sm));
		pane.add(getModifierLabel(Stats.Modifier.SKILL, sm));
		pane.add(getModifierLabel(Stats.Modifier.THIEVING, sm));
		pane.add(getModifierLabel(Stats.Modifier.SNEAKING, sm));
		pane.add(getModifierLabel(Stats.Modifier.BRAINS, sm));
		pane.add(getModifierLabel(Stats.Modifier.POWER, sm));
	}

	/*-------------------------------------------------------------------------*/
	private DIYLabel getModifierLabel(Stats.Modifier modifier, StatModifier req)
	{
		int value = req.getModifier(modifier);

		Color c;
		if (pc.getBaseModifier(modifier) < value)
		{
			c = Color.WHITE;
		}
		else
		{
			c = Color.LIGHT_GRAY;
		}

		return getLabel(descModifier(modifier, value), c);
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
					return "Cancel " + modifierName;
				}
			case PERCENTAGE:
				return modifierName + " " + descPlainModifier(value) + "%";
			default:
				throw new MazeException(metric.name());
		}
	}

	/*-------------------------------------------------------------------------*/
	private String descPlainModifier(int value)
	{
		if (value >= 0)
		{
			return "+" + value;
		}
		else
		{
			return "" + value;
		}
	}

	/*-------------------------------------------------------------------------*/
	private DIYLabel getLabel(Stats.Modifier text)
	{
		return new DIYLabel(text.getResourceBundleKey(), DIYToolkit.Align.LEFT);
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
