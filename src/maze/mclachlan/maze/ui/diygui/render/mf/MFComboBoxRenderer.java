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

package mclachlan.maze.ui.diygui.render.mf;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import mclachlan.diygui.DIYComboBox;
import mclachlan.diygui.toolkit.DIYToolkit;
import mclachlan.diygui.toolkit.Renderer;
import mclachlan.diygui.toolkit.Widget;
import mclachlan.maze.data.Database;
import mclachlan.maze.game.Maze;

/**
 *
 */
public class MFComboBoxRenderer extends Renderer
{
	public void render(Graphics2D g, int x, int y, int width, int height, Widget widget)
	{
		DIYComboBox<?> combo = (DIYComboBox<?>)widget;
		Object selected = combo.getSelected();
		Component comp = Maze.getInstance().getComponent();
		String text = selected==null ? "" : selected.toString();

		if (combo.getEditorText() != null)
		{
			text = combo.getEditorText();
		}

		// combo
		renderMfTextures(g, x, y, width, height, combo, comp);

		// text
		DIYToolkit.drawStringCentered(g, text,
			new Rectangle(x, y, width, height),
			combo.getAlignment(),
			Color.DARK_GRAY,
			null);

		if (DIYToolkit.debug)
		{
			g.setColor(Color.BLUE);
			g.drawRect(x, y, width, height);
		}
	}

	/*-------------------------------------------------------------------------*/
	private void renderMfTextures(Graphics2D g, int x, int y, int width, int height,
		DIYComboBox<?> field, Component comp)
	{
		BufferedImage borderTop;
		BufferedImage borderBottom;
		BufferedImage borderLeft;
		BufferedImage borderRight;
		BufferedImage cornerTopLeft;
		BufferedImage cornerTopRight;
		BufferedImage cornerBottomLeft;
		BufferedImage cornerBottomRight;
		BufferedImage center;
		BufferedImage arrowRight, arrowLeft;

		if (field.isEnabled())
		{
			if (field.getEditorState() == DIYComboBox.EditorState.HOVER)
			{
				borderTop = Database.getInstance().getImage("ui/mf/combobox/border_top_hover");
				borderBottom = Database.getInstance().getImage("ui/mf/combobox/border_bottom_hover");
				borderLeft = Database.getInstance().getImage("ui/mf/combobox/border_left_hover");
				borderRight = Database.getInstance().getImage("ui/mf/combobox/border_right_hover");
				cornerTopLeft = Database.getInstance().getImage("ui/mf/combobox/corner_top_left_hover");
				cornerTopRight = Database.getInstance().getImage("ui/mf/combobox/corner_top_right_hover");
				cornerBottomLeft = Database.getInstance().getImage("ui/mf/combobox/corner_bottom_left_hover");
				cornerBottomRight = Database.getInstance().getImage("ui/mf/combobox/corner_bottom_right_hover");
				center = Database.getInstance().getImage("ui/mf/combobox/center_hover");

				arrowRight = Database.getInstance().getImage("ui/mf/combobox/arrow_right_hover");
				arrowLeft = Database.getInstance().getImage("ui/mf/combobox/arrow_left_hover");

			}
			else if (field.getEditorState() == DIYComboBox.EditorState.DEPRESSED)
			{
				borderTop = Database.getInstance().getImage("ui/mf/combobox/border_top_depressed");
				borderBottom = Database.getInstance().getImage("ui/mf/combobox/border_bottom_depressed");
				borderLeft = Database.getInstance().getImage("ui/mf/combobox/border_left_depressed");
				borderRight = Database.getInstance().getImage("ui/mf/combobox/border_right_depressed");
				cornerTopLeft = Database.getInstance().getImage("ui/mf/combobox/corner_top_left_depressed");
				cornerTopRight = Database.getInstance().getImage("ui/mf/combobox/corner_top_right_depressed");
				cornerBottomLeft = Database.getInstance().getImage("ui/mf/combobox/corner_bottom_left_depressed");
				cornerBottomRight = Database.getInstance().getImage("ui/mf/combobox/corner_bottom_right_depressed");
				center = Database.getInstance().getImage("ui/mf/combobox/center_depressed");

				arrowRight = Database.getInstance().getImage("ui/mf/combobox/arrow_right_depressed");
				arrowLeft = Database.getInstance().getImage("ui/mf/combobox/arrow_left_depressed");
			}
			else
			{
				borderTop = Database.getInstance().getImage("ui/mf/combobox/border_top");
				borderBottom = Database.getInstance().getImage("ui/mf/combobox/border_bottom");
				borderLeft = Database.getInstance().getImage("ui/mf/combobox/border_left");
				borderRight = Database.getInstance().getImage("ui/mf/combobox/border_right");
				cornerTopLeft = Database.getInstance().getImage("ui/mf/combobox/corner_top_left");
				cornerTopRight = Database.getInstance().getImage("ui/mf/combobox/corner_top_right");
				cornerBottomLeft = Database.getInstance().getImage("ui/mf/combobox/corner_bottom_left");
				cornerBottomRight = Database.getInstance().getImage("ui/mf/combobox/corner_bottom_right");
				center = Database.getInstance().getImage("ui/mf/combobox/center");

				arrowRight = Database.getInstance().getImage("ui/mf/combobox/arrow_right");
				arrowLeft = Database.getInstance().getImage("ui/mf/combobox/arrow_left");
			}
		}
		else
		{
			// disabled
			borderTop = Database.getInstance().getImage("ui/mf/combobox/border_top_disabled");
			borderBottom = Database.getInstance().getImage("ui/mf/combobox/border_bottom_disabled");
			borderLeft = Database.getInstance().getImage("ui/mf/combobox/border_left_disabled");
			borderRight = Database.getInstance().getImage("ui/mf/combobox/border_right_disabled");
			cornerTopLeft = Database.getInstance().getImage("ui/mf/combobox/corner_top_left_disabled");
			cornerTopRight = Database.getInstance().getImage("ui/mf/combobox/corner_top_right_disabled");
			cornerBottomLeft = Database.getInstance().getImage("ui/mf/combobox/corner_bottom_left_disabled");
			cornerBottomRight = Database.getInstance().getImage("ui/mf/combobox/corner_bottom_right_disabled");
			center = Database.getInstance().getImage("ui/mf/combobox/center_disabled");

			arrowRight = Database.getInstance().getImage("ui/mf/combobox/arrow_right_disabled");
			arrowLeft = Database.getInstance().getImage("ui/mf/combobox/arrow_left_disabled");
		}

		// corners
		g.drawImage(cornerTopLeft, x, y, comp);
		g.drawImage(cornerTopRight, x + width -cornerTopRight.getWidth(), y, comp);
		g.drawImage(cornerBottomLeft, x, y + height -cornerBottomLeft.getHeight(), comp);
		g.drawImage(cornerBottomRight, x + width -cornerBottomRight.getWidth(), y + height -cornerBottomRight.getHeight(), comp);

		// horiz borders
		DIYToolkit.drawImageTiled(g, borderTop,
			x +cornerTopLeft.getWidth(), y,
			width -cornerTopLeft.getWidth() -cornerTopRight.getWidth(), borderTop.getHeight());
		DIYToolkit.drawImageTiled(g, borderBottom,
			x +cornerBottomLeft.getWidth(), y + height -borderBottom.getHeight(),
			width -cornerBottomLeft.getWidth() -cornerBottomRight.getWidth(), borderBottom.getHeight());

		// vert borders
		DIYToolkit.drawImageTiled(g, borderLeft,
			x, y +cornerTopLeft.getHeight(),
			borderLeft.getWidth(), height -cornerTopLeft.getHeight() -cornerBottomLeft.getHeight());
		DIYToolkit.drawImageTiled(g, borderRight,
			x + width -borderRight.getWidth(), y +cornerTopRight.getHeight(),
			borderRight.getWidth(), height -cornerTopRight.getHeight() -cornerBottomRight.getHeight());

		// center
		DIYToolkit.drawImageTiled(g, center,
			x +borderLeft.getWidth(), y +borderTop.getHeight(),
			width -borderLeft.getWidth() -borderRight.getWidth(),
			height -borderTop.getHeight() -borderBottom.getHeight());

		// arrow
		// popup indicator
		switch (field.getPopupDirection())
		{
			case UP ->
			{
				// todo
			}
			case DOWN ->
			{
				// todo
			}
			case RIGHT ->
			{
				DIYToolkit.drawImageCentered(g,
					arrowRight,
					new Rectangle(
						x +width -borderRight.getWidth(),
						y +height/2 -arrowRight.getHeight()/2,
						arrowRight.getWidth(),
						arrowRight.getHeight()),
					DIYToolkit.Align.CENTER);
			}
			case LEFT ->
			{
				DIYToolkit.drawImageCentered(g,
					arrowLeft,
					new Rectangle(
						x +borderRight.getWidth() -arrowLeft.getWidth() -1,
						y +height/2 -arrowLeft.getHeight()/2,
						arrowLeft.getWidth(),
						arrowLeft.getHeight()),
					DIYToolkit.Align.CENTER);
			}
		}

	}
}
