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
import java.util.*;
import mclachlan.diygui.DIYPane;
import mclachlan.diygui.DIYRadioButton;
import mclachlan.diygui.toolkit.ContainerWidget;
import mclachlan.diygui.toolkit.DIYButtonGroup;
import mclachlan.diygui.toolkit.DIYToolkit;
import mclachlan.diygui.toolkit.DIYGridLayout;
import mclachlan.maze.data.Database;
import mclachlan.maze.game.DifficultyLevel;

/**
 *
 */
public class StartGameOptionsWidget extends ContainerWidget
{
	private DIYButtonGroup buttonGroup;

	/*-------------------------------------------------------------------------*/
	public StartGameOptionsWidget(int x, int y, int width, int height)
	{
		super(x, y, width, height);
		this.buildGui();
	}

	/*-------------------------------------------------------------------------*/
	public StartGameOptionsWidget(Rectangle bounds)
	{
		this(bounds.x, bounds.y, bounds.width, bounds.height);
	}

	/*-------------------------------------------------------------------------*/
	private void buildGui()
	{
		DIYPane levelsPane = getDifficultyLevelsPane(
			new Rectangle(x+width/3, y+height/3, width/3, height/3));
		this.add(levelsPane);
	}

	/*-------------------------------------------------------------------------*/
	private DIYPane getDifficultyLevelsPane(Rectangle bounds)
	{
		Map<String, DifficultyLevel> difficultyLevels = Database.getInstance().getDifficultyLevels();
		DIYPane levelsPane = new DIYPane(bounds);
		levelsPane.setLayoutManager(new DIYGridLayout(1, difficultyLevels.size(), 5, 5));

		List<DifficultyLevel> sortedLevels = new ArrayList<DifficultyLevel>(
			difficultyLevels.values());
		Collections.sort(sortedLevels, new DifficultyLevelComparator());

		buttonGroup = new DIYButtonGroup();

		for (DifficultyLevel dl : sortedLevels)
		{
			DIYRadioButton w = new DIYRadioButton(dl.getName());
			levelsPane.add(w);
			buttonGroup.addButton(w);

			if (dl.getName().equals(sortedLevels.get(0).getName()))
			{
				w.setSelected(true);
			}
		}

		buttonGroup.setSelected(0);

		return levelsPane;
	}

	/*-------------------------------------------------------------------------*/
	public String getWidgetName()
	{
		return DIYToolkit.NONE;
	}

	/*-------------------------------------------------------------------------*/
	public String getDifficultyLevel()
	{
		return buttonGroup.getSelected().get(0).getCaption();
	}

	/*-------------------------------------------------------------------------*/
	private class DifficultyLevelComparator implements Comparator
	{
		public int compare(Object o1, Object o2)
		{
			DifficultyLevel d1 = (DifficultyLevel)o1;
			DifficultyLevel d2 = (DifficultyLevel)o2;

			return d1.getSortOrder() - d2.getSortOrder();
		}
	}
}