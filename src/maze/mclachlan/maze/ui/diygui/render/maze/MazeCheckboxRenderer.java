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

import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import mclachlan.diygui.toolkit.Renderer;
import mclachlan.diygui.toolkit.Widget;
import mclachlan.diygui.toolkit.DIYToolkit;
import mclachlan.diygui.DIYCheckbox;
import java.awt.*;
import mclachlan.maze.ui.diygui.Constants;

/**
 *
 */
public class MazeCheckboxRenderer extends Renderer
{
	public void render(Graphics2D g, int x, int y, int width, int height, Widget widget)
	{
		DIYCheckbox checkbox = (DIYCheckbox)widget;
		String caption = checkbox.getCaption();
		
		Color col1, col2;

		int boxSize = 12;
		int boxX = x+2;
		int boxY = y + height/2 - boxSize/2;

		// draw the filler
		if (checkbox.isSelected())
		{
			if (checkbox.isEnabled())
			{
				col1 = Color.WHITE;
				col2 = Constants.Colour.GOLD;
			}
			else
			{
				col1 = Color.LIGHT_GRAY;
				col2 = Color.LIGHT_GRAY.darker();
			}

			int inset = 0;
			Rectangle2D filler = new Rectangle2D.Double(boxX+inset, boxY+inset, boxSize-inset*2, boxSize-inset*2);
			g.setPaint(new RadialGradientPaint(
				new Rectangle2D.Double(boxX, boxY, boxSize-2, boxSize-2),
				new float[]{0.1f, 1.0f},
				new Color[]{col1, col2},
				MultipleGradientPaint.CycleMethod.NO_CYCLE));
			g.fill(filler);
		}

		// draw the border
		if (checkbox.isEnabled())
		{
			col1 = Color.WHITE;
			col2 = Color.LIGHT_GRAY.darker();
		}
		else
		{
			col1 = Color.LIGHT_GRAY.darker();
			col2 = Color.LIGHT_GRAY.darker().darker();
		}

		Rectangle2D e2d = new Rectangle2D.Double(boxX, boxY, boxSize, boxSize);
		g.setPaint(new GradientPaint(boxX, boxY, col1, boxX+height/2, boxY+height/2, col2, true));
		g.draw(e2d);

		// draw the text
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
