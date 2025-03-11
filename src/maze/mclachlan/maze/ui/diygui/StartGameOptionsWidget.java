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
import java.awt.Rectangle;
import java.util.*;
import mclachlan.diygui.*;
import mclachlan.diygui.toolkit.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.game.DifficultyLevel;
import mclachlan.maze.ui.diygui.render.maze.MazeRendererFactory;

/**
 *
 */
public class StartGameOptionsWidget extends ContainerWidget implements ActionListener
{
	private DIYButtonGroup buttonGroup;
	private DIYLabel image;
	private DIYTextArea description;
	private Map<String, DifficultyLevel> buttonMap = new HashMap<>();

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
		int inset = 10;

		int imageWidth = 250, imageHeight = 200;

		int lightBorder = DIYToolkit.getInstance().getRendererProperties().getProperty(RendererProperties.Property.PANEL_LIGHT_BORDER);

		DIYPanel imagePanel = new DIYPanel(
			x + width -imageWidth -inset -lightBorder*2,
			y +inset,
			imageWidth +lightBorder*2,
			imageHeight +lightBorder*2);
		imagePanel.setStyle(DIYPanel.Style.PANEL_LIGHT);

		image = new DIYLabel();
		image.setBounds(
			imagePanel.x +lightBorder,
			imagePanel.y +lightBorder,
			imageWidth,
			imageHeight);

		imagePanel.add(image);

		DIYPane levelsPane = getDifficultyLevelsPane(
			new Rectangle(
				x +inset/2,
				y +inset,
				width -image.width -inset*3,
				imageHeight));

		description = new DIYTextArea("");
		description.setBounds(x,
			imagePanel.y + imagePanel.height +inset,
			width,
			30);

		Font defaultFont = DiyGuiUserInterface.instance.getDefaultFont();
		Font f = defaultFont.deriveFont(Font.ITALIC, defaultFont.getSize());
		description.setFont(f);
		description.setTransparent(true);
		description.setAlignment(DIYToolkit.Align.CENTER);
		description.setForegroundColour(MazeRendererFactory.LABEL_FOREGROUND);

		this.add(levelsPane);
		this.add(imagePanel);
		this.add(description);

		refresh(buttonGroup.getSelected().get(0));
	}

	/*-------------------------------------------------------------------------*/
	private DIYPane getDifficultyLevelsPane(Rectangle bounds)
	{
		Map<String, DifficultyLevel> difficultyLevels = Database.getInstance().getDifficultyLevels();
		DIYPane levelsPane = new DIYPane(bounds);
		levelsPane.setLayoutManager(new DIYGridLayout(1, difficultyLevels.size(), 5, 5));

		List<DifficultyLevel> sortedLevels = new ArrayList<>(
			difficultyLevels.values());
		sortedLevels.sort(new DifficultyLevelComparator());

		buttonGroup = new DIYButtonGroup();
		DIYRadioButton selected = null;

		for (DifficultyLevel dl : sortedLevels)
		{
			String caption = dl.getDisplayName();

			buttonMap.put(caption, dl);

			DIYRadioButton w = new DIYRadioButton(caption);
			w.addActionListener(this);
			levelsPane.add(w);
			buttonGroup.addButton(w);

			if (dl.isDefaultSelection())
			{
				w.setSelected(true);
				selected = w;
			}
		}

		if (selected != null)
		{
			buttonGroup.setSelected(selected);
		}
		else
		{
			buttonGroup.setSelected(0);
		}

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
		return buttonMap.get(buttonGroup.getSelected().get(0).getCaption()).getName();
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public boolean actionPerformed(ActionEvent event)
	{
		DIYRadioButton button = (DIYRadioButton)event.getSource();
		refresh(button);

		return true;
	}

	private void refresh(DIYRadioButton button)
	{
		DifficultyLevel difficultyLevel = buttonMap.get(button.getCaption());

		image.setIcon(Database.getInstance().getImage(difficultyLevel.getImage()));
		description.setText(difficultyLevel.getDescription());
	}

	/*-------------------------------------------------------------------------*/
	private static class DifficultyLevelComparator implements Comparator<DifficultyLevel>
	{
		public int compare(DifficultyLevel o1, DifficultyLevel o2)
		{
			return o1.getSortOrder() - o2.getSortOrder();
		}
	}
}