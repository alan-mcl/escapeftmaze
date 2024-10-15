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

package mclachlan.maze.ui.diygui;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.geom.RoundRectangle2D;
import mclachlan.diygui.DIYLabel;
import mclachlan.diygui.DIYPanel;
import mclachlan.diygui.toolkit.ActionEvent;
import mclachlan.diygui.toolkit.ActionListener;
import mclachlan.diygui.toolkit.DIYToolkit;
import mclachlan.diygui.toolkit.RendererProperties;
import mclachlan.maze.data.Database;
import mclachlan.maze.data.StringUtil;
import mclachlan.maze.game.Maze;
import mclachlan.maze.stat.CurMax;
import mclachlan.maze.stat.CurMaxSub;
import mclachlan.maze.stat.PlayerCharacter;

public class MugshotWidget extends DIYPanel implements ActionListener
{
	private PlayerCharacter pc;

	private final DIYLabel portrait;
	private final FilledBarWidget hpBar, apBar, mpBar;

	private boolean selected;

	/*-------------------------------------------------------------------------*/
	public MugshotWidget()
	{
		this.setStyle(Style.PANEL_LIGHT);
		this.addActionListener(this);

		portrait = new DIYLabel();
		portrait.setIconAlign(DIYToolkit.Align.BOTTOM);
		portrait.addActionListener(this);

		hpBar = new FilledBarWidget(0,0);
		hpBar.setOrientation(FilledBarWidget.Orientation.VERTICAL);
		hpBar.setBarColour(Constants.Colour.COMBAT_RED);
		hpBar.setSubBarColour(Constants.Colour.FATIGUE_PINK);
		hpBar.addActionListener(this);

		apBar = new FilledBarWidget(0,0);
		apBar.setOrientation(FilledBarWidget.Orientation.VERTICAL);
		apBar.setBarColour(Constants.Colour.STEALTH_GREEN);
		apBar.addActionListener(this);

		mpBar = new FilledBarWidget(0,0);
		mpBar.setOrientation(FilledBarWidget.Orientation.VERTICAL);
		mpBar.setBarColour(Constants.Colour.MAGIC_BLUE);
		mpBar.addActionListener(this);

		this.add(portrait);
		this.add(hpBar);
		this.add(apBar);
		this.add(mpBar);
	}

	/*-------------------------------------------------------------------------*/

	@Override
	public void draw(Graphics2D g)
	{
		super.draw(g);

		if (isSelected())
		{
			Color col1 = Color.WHITE;
			Color col2 = Constants.Colour.GOLD;

			g.setPaint(new GradientPaint(x, y, col1, x+width, y+width, col2));
			RoundRectangle2D border = new RoundRectangle2D.Double(x+1, y+1, width-2, height-2, 5, 5);
			g.draw(border);
		}
	}

	/*-------------------------------------------------------------------------*/

	@Override
	public void doLayout()
	{
		RendererProperties rp = DIYToolkit.getInstance().getRendererProperties();

		int portraitWidth = rp.getProperty(RendererProperties.Property.PCW_PORTRAIT_WIDTH);
		int portraitHeight = rp.getProperty(RendererProperties.Property.PCW_PORTRAIT_HEIGHT);
		int panelBorderInset = rp.getProperty(RendererProperties.Property.PANEL_LIGHT_BORDER);

		int inset = 2;
		int barWidth = 12;

		// we're going to scale to this widget's height ... but only for laying out the other widgets!
		int scaledPortraitHeight = height -panelBorderInset*2 -inset*2;
		int scaledPortraitWidth = scaledPortraitHeight*portraitWidth/portraitHeight;

		// portrait
		portrait.setBounds(
			x+panelBorderInset+inset,
			y+panelBorderInset+inset,
			scaledPortraitWidth,
			scaledPortraitHeight);

		// bars
		int startX = x +panelBorderInset +inset +scaledPortraitWidth +inset*2;
		int startY = y +panelBorderInset +inset;
		int barHeight = height -panelBorderInset*2 -inset*2;
		int barSep = (width -panelBorderInset*2 -inset*3 -scaledPortraitWidth -barWidth*3) /2;

		hpBar.setBounds(startX, startY, barWidth, barHeight);
		startX += (barWidth+barSep);
		apBar.setBounds(startX, startY, barWidth, barHeight);
		startX += (barWidth+barSep);
		mpBar.setBounds(startX, startY, barWidth, barHeight);
	}

	/*-------------------------------------------------------------------------*/
	public PlayerCharacter getCharacter()
	{
		return pc;
	}

	/*-------------------------------------------------------------------------*/
	public void setCharacter(PlayerCharacter pc)
	{
		this.pc = pc;
		refresh();
	}

	/*-------------------------------------------------------------------------*/
	public void refresh()
	{
		if (this.pc != null)
		{
			portrait.setIcon(Database.getInstance().getImage(pc.getPortrait()));

			CurMaxSub hp = pc.getHitPoints();
			CurMax ap = pc.getActionPoints();
			CurMax mp = pc.getMagicPoints();

			hpBar.setFromCurMaxSub(hp);
			apBar.setFromCurMax(pc.getActionPoints());
			mpBar.setFromCurMax(pc.getMagicPoints());

			if (hp.getSub() > 0)
			{
				hpBar.setTooltip(StringUtil.getUiLabel("pcw.hp.fatigue.tooltip", hp.getCurrent(), hp.getMaximum(), hp.getSub()));
			}
			else
			{
				hpBar.setTooltip(StringUtil.getUiLabel("pcw.hp.tooltip", hp.getCurrent(), hp.getMaximum()));
			}

			apBar.setTooltip(StringUtil.getUiLabel("pcw.ap.tooltip", ap.getCurrent(), ap.getMaximum()));
			mpBar.setTooltip(StringUtil.getUiLabel("pcw.mp.tooltip", mp.getCurrent(), mp.getMaximum()));

		}
		else
		{
			portrait.setIcon(null);

			hpBar.setFromCurMaxSub(new CurMaxSub(0,0,0));
			hpBar.setTooltip(null);

			apBar.setFromCurMax(new CurMax(0,0));
			apBar.setTooltip(null);

			mpBar.setFromCurMax(new CurMax(0,0));
			mpBar.setTooltip(null);
		}
	}

	/*-------------------------------------------------------------------------*/
	public boolean isSelected()
	{
		return selected;
	}

	/*-------------------------------------------------------------------------*/
	public void setSelected(boolean selected)
	{
		this.selected = selected;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public boolean actionPerformed(ActionEvent event)
	{
		if (Maze.getInstance().isInCombat())
		{
			// during an "Equip" action in combat, only the current character can be edited
			return false;
		}

		Maze.getInstance().getUi().characterSelected(this.getCharacter());
		return true;
	}
}
