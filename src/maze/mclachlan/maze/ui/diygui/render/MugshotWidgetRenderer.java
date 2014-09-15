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

package mclachlan.maze.ui.diygui.render;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.RoundRectangle2D;
import mclachlan.diygui.toolkit.DIYToolkit;
import mclachlan.diygui.toolkit.Renderer;
import mclachlan.diygui.toolkit.Widget;
import mclachlan.maze.stat.CurMax;
import mclachlan.maze.stat.CurMaxSub;
import mclachlan.maze.stat.PlayerCharacter;
import mclachlan.maze.ui.diygui.Constants;
import mclachlan.maze.ui.diygui.MugshotWidget;

public class MugshotWidgetRenderer extends Renderer
{
	/*-------------------------------------------------------------------------*/
	@Override
	public void render(Graphics2D g, int x, int y, int width, int height, Widget w)
	{
		MugshotWidget widget = (MugshotWidget)w;

		Image portrait = widget.getPortrait();
		if (portrait == null)
		{
			return;
		}

		// todo: don't calc all this every time

		int inset = 5;
		int barWidth = width/20;

		int portraitWidth = portrait.getWidth(DIYToolkit.getInstance().getComponent());
		int portraitHeight = portrait.getHeight(DIYToolkit.getInstance().getComponent());

		// we're going to scale to this widgets height
		int scaledHeight = widget.height - inset*2;
		int scaledWidth = scaledHeight*portraitWidth/portraitHeight;

		g.drawImage(portrait, x+ inset, y+ inset, scaledWidth, scaledHeight,
			DIYToolkit.getInstance().getComponent());

		// draw bars
		int startX = x + width -barWidth*6 + inset;
		int startY = y+inset;
		int barHeight = height- inset*2;
		int barSep = barWidth/2;

		PlayerCharacter character = widget.getCharacter();
		drawBar(g, Constants.Colour.COMBAT_RED, Constants.Colour.FATIGUE_PINK,
			character.getHitPoints(), startX, startY, barWidth, barHeight);
		startX += (barWidth+barSep);
		drawBar(g, Constants.Colour.STEALTH_GREEN, null,
			character.getActionPoints(), startX, startY, barWidth, barHeight);
		startX += (barWidth+barSep);
		drawBar(g, Constants.Colour.MAGIC_BLUE, null,
			character.getMagicPoints(), startX, startY, barWidth, barHeight);

		if (widget.isSelected())
		{
			Color col1 = Color.WHITE;
			Color col2 = Constants.Colour.GOLD;
			
			g.setPaint(new GradientPaint(x, y, col1, x+width, y+width, col2));
			RoundRectangle2D border = new RoundRectangle2D.Double(x+1, y+1, width-2, height-2, 5, 5);
			g.draw(border);
		}
	}

	/*-------------------------------------------------------------------------*/
	private void drawBar(Graphics2D g, Color colour, Color subColour, CurMax stat,
		int x, int y, int barWidth, int barHeight)
	{
		Color borderCol1 = Color.LIGHT_GRAY;
		Color borderCol2 = Color.LIGHT_GRAY.darker();

		Color col1 = colour;
		Color col2 = colour.darker();

		int solidHeight = (int)((barHeight)*stat.getRatio());

		if (solidHeight < 0)
		{
			solidHeight = 0;
		}

		RoundRectangle2D border = new RoundRectangle2D.Double(
			x, y, barWidth, barHeight, 5, 5);
		RoundRectangle2D filler = new RoundRectangle2D.Double(
			x, y+barHeight-solidHeight, barWidth, solidHeight, 5, 5);

		g.setPaint(new GradientPaint(x, y, col1, x+barWidth, y+barHeight, col2));
		g.fill(filler);

		g.setPaint(new GradientPaint(x, y, borderCol1, x+barWidth, y+barHeight, borderCol2));
		g.draw(border);

		if (stat instanceof CurMaxSub)
		{
			solidHeight = (int)((barHeight)*((CurMaxSub)stat).getSubRatio());
			RoundRectangle2D sub = new RoundRectangle2D.Double(
				x, y+barHeight-solidHeight, barWidth, solidHeight, 5, 5);

			col1 = subColour;
			col2 = subColour.darker();

			g.setPaint(new GradientPaint(x, y, col1, x+barWidth, y+barHeight, col2));
			g.fill(sub);
		}
	}
}