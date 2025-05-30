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
import mclachlan.diygui.DIYPanel;
import mclachlan.diygui.toolkit.DIYToolkit;
import mclachlan.diygui.toolkit.Renderer;
import mclachlan.diygui.toolkit.Widget;

/**
 *
 */
public class MazePanelRenderer extends Renderer
{
	public void render(Graphics2D g, int x, int y, int width, int height, Widget widget)
	{
		DIYPanel panel = (DIYPanel)widget;
		Image image = panel.getBackgroundImage();
		
		if (image == null)
		{
			g.setColor(MazeRendererFactory.PANEL_BACKGROUND);
			g.fillRect(x, y, width, height);
		}
		else
		{
			g.drawImage(image, x, y, width, height, DIYToolkit.getInstance().getComponent());
		}
	}
}
