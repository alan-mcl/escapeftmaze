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

package mclachlan.diygui.render.dflt;

import java.awt.*;
import mclachlan.diygui.DIYLabel;
import mclachlan.diygui.toolkit.DIYToolkit;
import mclachlan.diygui.toolkit.Renderer;
import mclachlan.diygui.toolkit.Widget;
import mclachlan.maze.game.Maze;

/**
 *
 */
public class DefaultLabelRenderer extends Renderer
{
	public void render(Graphics2D g, int x, int y, int width, int height, Widget widget)
	{
		DIYLabel label = (DIYLabel)widget;
		String text = label.getText();

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
		if (icon != null)
		{
			int iconWidth = icon.getWidth(Maze.getInstance().getComponent());
			int iconHeight = icon.getHeight(Maze.getInstance().getComponent());

			// center the icon on the Y axis
			int iconY = y + height/2 + iconHeight/2;

			int iconX = x;
			if (label.getAlignment() == DIYToolkit.Align.CENTER)
			{
				// center the text on the X axis
				iconX = x + width/2 - iconWidth/2;
			}
			else if (label.getAlignment() == DIYToolkit.Align.RIGHT)
			{
				// align right
				iconX = x + width - iconWidth;
			}

			g.drawImage(icon, iconX, iconY, Maze.getInstance().getComponent());
		}
		else
		{

			FontMetrics fm = g.getFontMetrics();

			int textHeight = fm.getAscent();
			int textWidth = fm.stringWidth(text);

			// center the text on the Y axis
			int textY = y + height/2 + textHeight/2;

			int textX = x;
			if (label.getAlignment() == DIYToolkit.Align.CENTER)
			{
				// center the text on the X axis
				textX = x + width/2 - textWidth/2;
			}
			else if (label.getAlignment() == DIYToolkit.Align.RIGHT)
			{
				// align right
				textX = x + width - textWidth;
			}

			Color foreground = label.getForegroundColour();
			if (foreground == null)
			{
				foreground = DefaultRendererFactory.LABEL_FOREGROUND;

				if (!label.isEnabled())
				{
					foreground = foreground.darker();
				}
			}

			Color background = label.getBackgroundColour();
			if (background != null)
			{
				g.setColor(background);
				g.fillRect(x, y, width, height);
			}

			g.setColor(foreground);
			g.drawString(text, textX, textY);

			// reset the font
			g.setFont(base);
		}
	}
}
