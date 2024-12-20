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

package mclachlan.maze.ui.diygui.render.maze;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.util.*;
import mclachlan.diygui.DIYTextArea;
import mclachlan.diygui.toolkit.DIYToolkit;
import mclachlan.diygui.toolkit.Renderer;
import mclachlan.diygui.toolkit.Widget;

/**
 *
 */
public class MazeTextAreaRenderer extends Renderer
{
	protected Color defaultTextColour = Color.WHITE;

	public void render(Graphics2D g, int x, int y, int width, int height,
		Widget widget)
	{
		DIYTextArea area = (DIYTextArea)widget;

		if (!area.isTransparent())
		{
			if (widget.getBackgroundColour() != null)
			{
				g.setColor(widget.getBackgroundColour());
			}
			else
			{
				g.setColor(MazeRendererFactory.PANEL_BACKGROUND);
			}
			g.fillRect(x, y, width, height);
		}

		if (DIYToolkit.debug)
		{
			g.setColor(Color.PINK);
			g.drawRect(x, y, width, height);
		}

		if (area.getForegroundColour() != null)
		{
			g.setColor(area.getForegroundColour());
		}
		else
		{
			g.setColor(defaultTextColour);
		}

		Font gFont = g.getFont();

		if (area.getFont() != null)
		{
			g.setFont(area.getFont());
		}

		FontMetrics fm = g.getFontMetrics();
		int lineHeight = fm.getHeight() - 2;

		int inset = 2;
		int pos = y + lineHeight;
		List<String> lines = DIYToolkit.wrapText(area.getText(), g, width - inset * 2);
		for (String line : lines)
		{
			int textWidth = fm.stringWidth(line);
			int textX = x + inset;
			if (area.getAlignment() == DIYToolkit.Align.CENTER)
			{
				// center the text on the X axis
				textX = x + inset + width / 2 - textWidth / 2;
			}
			else if (area.getAlignment() == DIYToolkit.Align.RIGHT)
			{
				// align right
				textX = x + width - textWidth - inset;
			}
			g.drawString(line, textX, pos);
			pos += lineHeight;
		}

		if (area.getFont() != null)
		{
			// restore the original
			g.setFont(gFont);
		}
	}
}
