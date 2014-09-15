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
import mclachlan.diygui.DIYButton;
import java.awt.*;

/**
 *
 */
public class DefaultButtonRenderer extends Renderer
{
	public void render(Graphics2D g, int x, int y, int width, int height, Widget widget)
	{
		DIYButton button = (DIYButton)widget;
		String text = button.getText();

		if (button.isEnabled())
		{
			switch (button.getState())
			{
				case DEFAULT:
					g.setColor(Color.YELLOW);
					break;
				case HOVER:
					g.setColor(Color.ORANGE);
					break;
				case DEPRESSED:
					g.setColor(Color.ORANGE.darker());
					break;
			}
		}
		else
		{
			g.setColor(Color.LIGHT_GRAY);
		}

		g.fillRect(x, y, width, height);
		
		FontMetrics fm = g.getFontMetrics();
	
		int textHeight = fm.getAscent();
		int textWidth = fm.stringWidth(text);

		// center the text on the Y axis
		int textY = y + height/2 + textHeight/2;
		
		int textX = x;
		if (button.getAlignment() == DIYToolkit.Align.CENTER)
		{
			// center the text on the X axis
			textX = x + width/2 - textWidth/2;
		}
		else if (button.getAlignment() == DIYToolkit.Align.RIGHT)
		{
			// align right
			textX = x + width - textWidth;
		}

		g.setColor(Color.BLACK);
		g.drawString(text, textX, textY);
		
		if (DIYToolkit.debug)
		{
			g.setColor(Color.BLUE);
			g.drawRect(x, y, width, height);
		}
	}
}
