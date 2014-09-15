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
import java.util.ArrayList;
import java.util.Scanner;
import mclachlan.diygui.DIYTextArea;
import mclachlan.diygui.toolkit.Renderer;
import mclachlan.diygui.toolkit.Widget;
import mclachlan.diygui.toolkit.DIYToolkit;

/**
 *
 */
public class DefaultTextAreaRenderer extends Renderer
{
	private final int inset = 2;

	public void render(Graphics2D g, int x, int y, int width, int height, Widget widget)
	{
		DIYTextArea area = (DIYTextArea)widget;

		if (!area.isTransparent())
		{
			g.setColor(DefaultRendererFactory.PANEL_BACKGROUND);
			g.fillRect(x, y, width, height);
		}
		
		if (DIYToolkit.debug)
		{
			g.setColor(Color.PINK);
			g.drawRect(x, y, width, height);
		}
		
		g.setColor(Color.WHITE);
		
		Scanner scanner = new Scanner(area.getText());
		scanner.useDelimiter("\n");
		
		FontMetrics fm = g.getFontMetrics();
		int lineHeight = 10; //hacK! fm.getHeight();
		
		int pos = y+lineHeight;
		while (scanner.hasNext())
		{
			String s = scanner.next();
			ArrayList<String> lines = wrapText(s, g, width);
			for (String line : lines)
			{
				g.drawString(line, x+inset, pos);
				pos += lineHeight;
			}
		}
	}
	
	/*-------------------------------------------------------------------------*/
	private ArrayList<String> wrapText(String s, Graphics g, int width)
	{
		FontMetrics fm = g.getFontMetrics();

		Scanner scanner = new Scanner(s);
		
		ArrayList<String> result = new ArrayList<String>();
		String temp = "";
		
		while (scanner.hasNext())
		{
			String cur = scanner.next();
			if (fm.stringWidth(temp+cur) > width)
			{
				result.add(temp.trim());
				temp = cur;
			}
			else
			{
				temp += " "+cur;
			}
		}
		result.add(temp.trim());
		
		return result;
	}
}
