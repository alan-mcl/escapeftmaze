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

import java.awt.*;
import mclachlan.diygui.DIYLabel;
import mclachlan.diygui.toolkit.DIYToolkit;
import mclachlan.diygui.toolkit.Renderer;
import mclachlan.diygui.toolkit.Widget;
import mclachlan.maze.util.MazeException;

/**
 *
 */
public class MazeLabelRenderer extends Renderer
{
	protected Color labelForegroundEnabled = MazeRendererFactory.LABEL_FOREGROUND;
	protected Color labelForegroundEnabledHover = MazeRendererFactory.LABEL_FOREGROUND.brighter();
	protected Color labelForegroundDisabled = MazeRendererFactory.DISABLED_LABEL_FOREGROUND;

	public void render(Graphics2D g, int x, int y, int width, int height, Widget widget)
	{
		DIYLabel label = (DIYLabel)widget;

		if (DIYToolkit.debug)
		{
			g.setColor(Color.BLUE);
			g.drawRect(x, y, width, height);
		}

		Font base = g.getFont();
		if (label.getFont() != null)
		{
			g.setFont(label.getFont());
		}

		Image icon = label.getIcon();
		String text = label.getText();

		Rectangle iconBounds = null;
		Rectangle textBounds = null;

		if (icon != null && text == null)
		{
			// only an icon to draw
			iconBounds = new Rectangle(x, y, width, height);
		}
		else if (icon == null && text != null)
		{
			// only text to draw
			textBounds = new Rectangle(x, y, width, height);
		}
		else if (icon != null && text != null)
		{
			// both to draw
			int iconWidth = (int)DIYToolkit.getDimension(icon).getWidth();
			int textWidth = g.getFontMetrics().stringWidth(text);
			int combinedWidth = iconWidth + textWidth;

			int startX = switch (label.getAlignment())
				{
					case LEFT -> x;
					case CENTER, TOP, BOTTOM -> x + width / 2 - combinedWidth / 2;
					case RIGHT -> x + width - combinedWidth;
					default ->
						throw new MazeException(label.getAlignment().toString());
				};

			iconBounds = new Rectangle(startX, y, iconWidth, height);
			int textX = switch (label.getIconAlign())
				{
					case LEFT -> startX + iconWidth;
					case CENTER, TOP, BOTTOM -> startX;
					case RIGHT -> startX;
					default ->
						throw new MazeException(label.getAlignment().toString());
				};
			textBounds = new Rectangle(textX, y, textWidth, height);
		}

		// draw the icon
		if (icon != null)
		{
			if (label.isHover())
			{
				if (label.getHoverIcon() != null)
				{
					DIYToolkit.drawImageAligned(g, label.getHoverIcon(), iconBounds, label.getIconAlign());
				}
				else
				{
					DIYToolkit.drawImageAligned(g, icon, iconBounds, label.getIconAlign());
				}
			}
			else
			{
				DIYToolkit.drawImageAligned(g, icon, iconBounds, label.getIconAlign());
			}
		}

		// draw the text
		if (text != null)
		{
			Color foreground = label.getForegroundColour();
			if (foreground == null)
			{
				if (label.isEnabled())
				{
					if (label.isHover() && label.getListeners().size()>0)
					{
						foreground = labelForegroundEnabledHover;
					}
					else
					{
						foreground = labelForegroundEnabled;
					}
				}
				else
				{
					foreground = labelForegroundDisabled;
				}
			}

			DIYToolkit.drawStringCentered(
				g,
				text,
				textBounds,
				label.getAlignment(),
				foreground,
				label.getBackgroundColour());
		}

		// reset the font
		g.setFont(base);
	}
}
