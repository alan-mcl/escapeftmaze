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
import mclachlan.diygui.DIYTextField;
import mclachlan.diygui.toolkit.DIYToolkit;
import mclachlan.diygui.toolkit.Renderer;
import mclachlan.diygui.toolkit.Widget;
import mclachlan.maze.data.Database;
import mclachlan.maze.game.Maze;

/**
 *
 */
public class MFTextFieldRenderer extends Renderer
{
	private static final long blinkPeriod = 750;
	private static final int inset = 12;

	public void render(Graphics2D g, int x, int y, int width, int height, Widget widget)
	{
		DIYTextField field = (DIYTextField)widget;
		String text = field.getText();

		renderMfTextures(g, x, y, width, height, field, Maze.getInstance().getComponent());
		
		FontMetrics fm = g.getFontMetrics();
	
		int textHeight = fm.getHeight();

		// center the text on the Y axis
		int textY = y + height/2 + textHeight/2 -fm.getDescent();
		
		int textX = x + inset;

		if (field.hasFocus() && (System.currentTimeMillis()%(blinkPeriod*2)) < blinkPeriod)
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

	/*-------------------------------------------------------------------------*/
	private void renderMfTextures(Graphics2D g, int x, int y, int width, int height,
		DIYTextField field, Component comp)
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

		if (field.isEnabled())
		{
			if (field.isHover())
			{
				borderTop = Database.getInstance().getImage("ui/mf/textfield/border_top_hover");
				borderBottom = Database.getInstance().getImage("ui/mf/textfield/border_bottom_hover");
				borderLeft = Database.getInstance().getImage("ui/mf/textfield/border_left_hover");
				borderRight = Database.getInstance().getImage("ui/mf/textfield/border_right_hover");
				cornerTopLeft = Database.getInstance().getImage("ui/mf/textfield/corner_top_left_hover");
				cornerTopRight = Database.getInstance().getImage("ui/mf/textfield/corner_top_right_hover");
				cornerBottomLeft = Database.getInstance().getImage("ui/mf/textfield/corner_bottom_left_hover");
				cornerBottomRight = Database.getInstance().getImage("ui/mf/textfield/corner_bottom_right_hover");
				center = Database.getInstance().getImage("ui/mf/textfield/center_hover");
			}
			else
			{
				borderTop = Database.getInstance().getImage("ui/mf/textfield/border_top");
				borderBottom = Database.getInstance().getImage("ui/mf/textfield/border_bottom");
				borderLeft = Database.getInstance().getImage("ui/mf/textfield/border_left");
				borderRight = Database.getInstance().getImage("ui/mf/textfield/border_right");
				cornerTopLeft = Database.getInstance().getImage("ui/mf/textfield/corner_top_left");
				cornerTopRight = Database.getInstance().getImage("ui/mf/textfield/corner_top_right");
				cornerBottomLeft = Database.getInstance().getImage("ui/mf/textfield/corner_bottom_left");
				cornerBottomRight = Database.getInstance().getImage("ui/mf/textfield/corner_bottom_right");
				center = Database.getInstance().getImage("ui/mf/textfield/center");
			}
		}
		else
		{
			// disabled
			borderTop = Database.getInstance().getImage("ui/mf/textfield/border_top_disabled");
			borderBottom = Database.getInstance().getImage("ui/mf/textfield/border_bottom_disabled");
			borderLeft = Database.getInstance().getImage("ui/mf/textfield/border_left_disabled");
			borderRight = Database.getInstance().getImage("ui/mf/textfield/border_right_disabled");
			cornerTopLeft = Database.getInstance().getImage("ui/mf/textfield/corner_top_left_disabled");
			cornerTopRight = Database.getInstance().getImage("ui/mf/textfield/corner_top_right_disabled");
			cornerBottomLeft = Database.getInstance().getImage("ui/mf/textfield/corner_bottom_left_disabled");
			cornerBottomRight = Database.getInstance().getImage("ui/mf/textfield/corner_bottom_right_disabled");
			center = Database.getInstance().getImage("ui/mf/textfield/center_disabled");
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
	}
}
