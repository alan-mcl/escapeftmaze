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

import mclachlan.diygui.toolkit.Renderer;
import mclachlan.diygui.toolkit.Widget;
import mclachlan.diygui.toolkit.DIYToolkit;
import mclachlan.diygui.DIYCheckbox;
import java.awt.*;

/**
 *
 */
public class DefaultCheckboxRenderer extends Renderer
{
	public void render(Graphics2D g, int x, int y, int width, int height, Widget widget)
	{
		DIYCheckbox checkbox = (DIYCheckbox)widget;
		String caption = checkbox.getCaption();
		
		int boxSize = 12;
		int boxX = x+2;
		int boxY = y + height/2 - boxSize/2;

		if (checkbox.isEnabled())
		{
			g.setColor(Color.WHITE);
		}
		else
		{
			g.setColor(Color.LIGHT_GRAY);
		}
		g.drawRect(boxX, boxY, boxSize, boxSize);
		
		if (checkbox.isSelected())
		{
			if (checkbox.isEnabled())
			{
				g.setColor(Color.ORANGE);
			}
			else
			{
				g.setColor(Color.ORANGE.darker());
			}
			g.fillRect(boxX+1, boxY+1, boxSize-1, boxSize-1);
		}
		
		FontMetrics fm = g.getFontMetrics();
	
		int textHeight = fm.getAscent();

		// center the text on the Y axis
		int textY = y + height/2 + textHeight/2;
		
		int textX = boxX + boxSize + 4;

		if (checkbox.isEnabled())
		{
			g.setColor(Color.WHITE);
		}
		else
		{
			g.setColor(Color.LIGHT_GRAY);
		}
		g.drawString(caption, textX, textY);
		
		if (DIYToolkit.debug)
		{
			g.setColor(Color.BLUE);
			g.drawRect(x, y, width, height);
		}
	}
}
