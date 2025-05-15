/*
 * Copyright (c) 2012 Alan McLachlan
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
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import mclachlan.diygui.toolkit.DIYToolkit;
import mclachlan.diygui.toolkit.Renderer;
import mclachlan.diygui.toolkit.Widget;
import mclachlan.maze.data.Database;
import mclachlan.maze.game.Maze;
import mclachlan.maze.stat.FoeGroup;
import mclachlan.maze.stat.condition.CloudSpell;
import mclachlan.maze.ui.diygui.FoeGroupWidget;

public class FoeGroupWidgetRenderer extends Renderer
{
	/*-------------------------------------------------------------------------*/
	@Override
	public void render(Graphics2D g, int x, int y, int width, int height, Widget widget)
	{
		FoeGroupWidget w = (FoeGroupWidget)widget;

		FoeGroup group = w.getFoeGroup();

		if (group == null)
		{
			return;
		}

		if (group.numAlive() > 0)
		{

			String display =
				group.numAlive()+" "+group.getDescription() + " ("+group.numActive()+")";

			drawText(g, display, Color.CYAN, x + 6, y + 13);
			drawText(g, display, Color.DARK_GRAY, x + 5, y + 12);

			for (int i = 0; i < group.getCloudSpells().size(); i++)
			{
				CloudSpell spell = group.getCloudSpells().get(i);
				BufferedImage icon = Database.getInstance().getImage(spell.getIcon());

				g.drawImage(
					icon,
					x+width-16-(14*i),
					y,
					12,
					12,
					Maze.getInstance().getComponent());
			}
		}

		if (DIYToolkit.debug)
		{
			g.setColor(Color.CYAN);
			g.drawRect(x, y, width, height);
		}

	}

	private void drawText(Graphics2D g, String display, Color colour, int xx,
		int yy)
	{
		g.setColor(colour);
		g.drawString(display, xx, yy);
	}
}