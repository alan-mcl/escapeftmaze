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

package mclachlan.maze.ui.diygui.render.mf;

import java.awt.Color;
import java.awt.Component;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import mclachlan.diygui.DIYCheckbox;
import mclachlan.diygui.toolkit.DIYToolkit;
import mclachlan.diygui.toolkit.Renderer;
import mclachlan.diygui.toolkit.Widget;
import mclachlan.maze.data.Database;
import mclachlan.maze.game.Maze;

/**
 *
 */
public class MFCheckboxRenderer extends Renderer
{
	public void render(Graphics2D g, int x, int y, int width, int height, Widget widget)
	{
		DIYCheckbox checkbox = (DIYCheckbox)widget;
		String caption = checkbox.getCaption();
		Component comp = Maze.getInstance().getComponent();
		
		BufferedImage image;

		if (checkbox.isSelected())
		{
			if (checkbox.isEnabled())
			{
				if (checkbox.isHover())
				{
					image = Database.getInstance().getImage("ui/mf/checkbox/checkbox_selected_hover");
				}
				else
				{
					image = Database.getInstance().getImage("ui/mf/checkbox/checkbox_selected");
				}
			}
			else
			{
				image = Database.getInstance().getImage("ui/mf/checkbox/checkbox_selected_disabled");
			}
		}
		else
		{
			if (checkbox.isEnabled())
			{
				if (checkbox.isHover())
				{
					image = Database.getInstance().getImage("ui/mf/checkbox/checkbox_hover");
				}
				else
				{
					image = Database.getInstance().getImage("ui/mf/checkbox/checkbox");
				}
			}
			else
			{
				image = Database.getInstance().getImage("ui/mf/checkbox/checkbox_disabled");
			}
		}

		int boxSize = 24;
		int boxX = x+2;
		int boxY = y + height/2 - boxSize/2;

		g.drawImage(image, boxX, boxY, comp);

		// draw the text
		FontMetrics fm = g.getFontMetrics();
	
		int textHeight = fm.getAscent();

		// center the text on the Y axis
		int textY = y + height/2 + textHeight/2 - fm.getDescent();
		
		int textX = boxX + boxSize + 4;

		if (checkbox.isEnabled())
		{
			if (checkbox.isHover())
			{
				g.setColor(Colours.LABEL_TEXT_HIGHLIGHTED);
			}
			else
			{
				g.setColor(Colours.LABEL_TEXT);
			}
		}
		else
		{
			g.setColor(Colours.LABEL_TEXT_DISABLED);
		}
		g.drawString(caption, textX, textY);
		
		if (DIYToolkit.debug)
		{
			g.setColor(Color.BLUE);
			g.drawRect(x, y, width, height);
		}
	}
}
