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

import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import mclachlan.diygui.DIYPanel;
import mclachlan.diygui.toolkit.DIYToolkit;
import mclachlan.diygui.toolkit.Renderer;
import mclachlan.diygui.toolkit.Widget;
import mclachlan.maze.data.Database;
import mclachlan.maze.game.Maze;
import mclachlan.maze.util.MazeException;

/**
 *
 */
public class MFPanelRenderer extends Renderer
{
	public void render(Graphics2D g, int x, int y, int width, int height,
		Widget widget)
	{
		DIYPanel panel = (DIYPanel)widget;
		Image image = panel.getBackgroundImage();
		Component comp = Maze.getInstance().getComponent();

		switch (panel.getStyle())
		{
			case DIALOG ->
			{
				drawWithTextures(g, x, y, width, height, comp,
					Database.getInstance().getImage("ui/mf/dialog/border_top"),
					Database.getInstance().getImage("ui/mf/dialog/border_bottom"),
					Database.getInstance().getImage("ui/mf/dialog/border_left"),
					Database.getInstance().getImage("ui/mf/dialog/border_right"),
					Database.getInstance().getImage("ui/mf/dialog/corner_top_left"),
					Database.getInstance().getImage("ui/mf/dialog/corner_top_right"),
					Database.getInstance().getImage("ui/mf/dialog/corner_bottom_left"),
					Database.getInstance().getImage("ui/mf/dialog/corner_bottom_right"),
					Database.getInstance().getImage("ui/mf/dialog/center"),
					Database.getInstance().getImage("ui/mf/dialog/title_bar"));
			}
			case FIXED_PANEL ->
			{
				drawWithTextures(g, x, y, width, height, comp,
					Database.getInstance().getImage("ui/mf/panel/border_top"),
					Database.getInstance().getImage("ui/mf/panel/border_bottom"),
					Database.getInstance().getImage("ui/mf/panel/border_left"),
					Database.getInstance().getImage("ui/mf/panel/border_right"),
					Database.getInstance().getImage("ui/mf/panel/corner_top_left"),
					Database.getInstance().getImage("ui/mf/panel/corner_top_right"),
					Database.getInstance().getImage("ui/mf/panel/corner_bottom_left"),
					Database.getInstance().getImage("ui/mf/panel/corner_bottom_right"),
					Database.getInstance().getImage("ui/mf/panel/center"),
					null);
			}
			case IMAGE_BACK ->
			{
				g.drawImage(image, x, y, width, height, DIYToolkit.getInstance().getComponent());
			}
			case TRANSPARENT ->
			{
			}
			default ->
			{
				throw new MazeException("invalid: "+panel.getStyle());
			}
		}
	}

	/*-------------------------------------------------------------------------*/

	private void drawWithTextures(Graphics2D g, int x, int y, int width, int height,
		Component comp, BufferedImage borderTop, BufferedImage borderBottom,
		BufferedImage borderLeft, BufferedImage borderRight,
		BufferedImage cornerTopLeft, BufferedImage cornerTopRight,
		BufferedImage cornerBottomLeft, BufferedImage cornerBottomRight,
		BufferedImage center, BufferedImage titleBar)
	{
		// corners
		g.drawImage(cornerTopLeft, x, y, comp);
		g.drawImage(cornerTopRight, x + width - cornerTopRight.getWidth(), y, comp);
		g.drawImage(cornerBottomLeft, x, y + height - cornerBottomLeft.getHeight(), comp);
		g.drawImage(cornerBottomRight, x + width - cornerBottomRight.getWidth(), y + height - cornerBottomRight.getHeight(), comp);

		// horiz borders
		DIYToolkit.drawImageTiled(g, borderTop,
			x + cornerTopLeft.getWidth(), y,
			width - cornerTopLeft.getWidth() - cornerTopRight.getWidth(), borderTop.getHeight());
		DIYToolkit.drawImageTiled(g, borderBottom,
			x + cornerBottomLeft.getWidth(), y + height - borderBottom.getHeight(),
			width - cornerBottomLeft.getWidth() - cornerBottomRight.getWidth(), borderBottom.getHeight());

		// vert borders
		DIYToolkit.drawImageTiled(g, borderLeft,
			x, y + cornerTopLeft.getHeight(),
			borderLeft.getWidth(), height - cornerTopLeft.getHeight() - cornerBottomLeft.getHeight());
		DIYToolkit.drawImageTiled(g, borderRight,
			x + width - borderRight.getWidth(), y + cornerTopRight.getHeight(),
			borderRight.getWidth(), height - cornerTopRight.getHeight() - cornerBottomRight.getHeight());

		// center
		DIYToolkit.drawImageTiled(g, center,
			x + borderLeft.getWidth(), y + borderTop.getHeight(),
			width - borderLeft.getWidth() - borderRight.getWidth(),
			height - borderTop.getHeight() - borderBottom.getHeight());

		// title bar
		if (titleBar != null)
		{
			DIYToolkit.drawImageTiled(g, titleBar,
				x + borderLeft.getWidth(), y + borderTop.getHeight(),
				width - borderLeft.getWidth() - borderRight.getWidth(),
				titleBar.getHeight());
		}
	}
}
