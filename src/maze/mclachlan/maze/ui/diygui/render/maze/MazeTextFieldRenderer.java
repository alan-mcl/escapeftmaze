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
import mclachlan.diygui.DIYTextField;
import mclachlan.diygui.toolkit.Renderer;
import mclachlan.diygui.toolkit.Widget;

/**
 *
 */
public class MazeTextFieldRenderer extends Renderer
{
	private static final long blinkPeriod = 1000;
	private static final int inset = 4;

	public void render(Graphics2D g, int x, int y, int width, int height, Widget widget)
	{
		DIYTextField field = (DIYTextField)widget;
		String text = field.getText();

		if (field.isEnabled())
		{
			g.setColor(Color.BLUE.brighter());
		}
		else
		{
			g.setColor(Color.LIGHT_GRAY);
		}
		g.drawRect(x, y, width, height);
		
		FontMetrics fm = g.getFontMetrics();
	
		int textHeight = fm.getAscent();

		// center the text on the Y axis
		int textY = y + height/2 + textHeight/2;
		
		int textX = x + inset;

		if (field.hasFocus() && (System.currentTimeMillis()/(blinkPeriod*2)) < blinkPeriod)
		{
			// draw the cursor
			text += '|';
		}

		if (field.isEnabled())
		{
			g.setColor(Color.WHITE);
		}
		else
		{
			g.setColor(Color.LIGHT_GRAY);
		}
		g.drawString(text, textX, textY);
	}
}
