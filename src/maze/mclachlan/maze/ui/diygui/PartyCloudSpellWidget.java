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

package mclachlan.maze.ui.diygui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import mclachlan.diygui.DIYLabel;
import mclachlan.diygui.toolkit.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.data.StringUtil;
import mclachlan.maze.game.Maze;
import mclachlan.maze.map.Tile;
import mclachlan.maze.stat.GameSys;
import mclachlan.maze.stat.PlayerParty;
import mclachlan.maze.stat.condition.CloudSpell;

/**
 * Displays icons for cloud spells affecting the party
 */
public class PartyCloudSpellWidget extends ContainerWidget implements ActionListener
{
	private PlayerParty group;
	private final Rectangle bounds;
	private final DIYLabel[] labels;
	private int partyStealth;
	private String stealthTooltip;

	/*-------------------------------------------------------------------------*/
	public PartyCloudSpellWidget(PlayerParty group, Rectangle bounds)
	{
		super(bounds);
		this.group = group;
		this.bounds = bounds;

		RendererProperties rp = DIYToolkit.getInstance().getRendererProperties();
		int iconSize = rp.getProperty(RendererProperties.Property.CONDITION_ICON_SIZE);
		int hgap = 3;

		int nrLabels = bounds.width / (iconSize + hgap);
		labels = new DIYLabel[nrLabels];

		this.setLayoutManager(new DIYGridLayout(nrLabels, 1, hgap, 0));
		for (int i = 0; i < labels.length; i++)
		{
			labels[i] = new DIYLabel();
			labels[i].setIconAlign(DIYToolkit.Align.CENTER);
			labels[i].addActionListener(this);
			this.add(labels[i]);
		}
	}

	/*-------------------------------------------------------------------------*/
	public void draw(Graphics2D g)
	{
		if (group == null)
		{
			return;
		}

		for (int i = 0; i < labels.length; i++)
		{
			if (group.getCloudSpells().size() > i)
			{
				CloudSpell spell = group.getCloudSpells().get(i);
				BufferedImage icon = Database.getInstance().getImage(spell.getIcon());

				labels[i].setIcon(icon);
				labels[i].setTooltip(spell.getSpell().getDescription());
			}
			else
			{
				labels[i].setIcon(null);
				labels[i].setTooltip(null);
			}
		}

		if (partyStealth > 0)
		{
			labels[labels.length - 1].setIcon(Database.getInstance().getImage("condition/stealth"));
			labels[labels.length - 1].setTooltip(stealthTooltip);
		}

		super.draw(g);

		if (DIYToolkit.debug)
		{
			g.setColor(Color.CYAN);
			g.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);
		}
	}

	/*-------------------------------------------------------------------------*/
	public void setParty(PlayerParty group)
	{
		this.group = group;
	}

	/*-------------------------------------------------------------------------*/
	public void setTile(Point p)
	{
		Tile tile = Maze.getInstance().getCurrentZone().getTile(p);
		partyStealth = GameSys.getInstance().getStealthValue(tile, Maze.getInstance().getParty());
		stealthTooltip = StringUtil.getUiLabel("poatw.stealth", partyStealth);
	}

	/*-------------------------------------------------------------------------*/
	public void processMouseClicked(MouseEvent e)
	{
		parent.processMouseClicked(e);
	}

	/*-------------------------------------------------------------------------*/
	public String getWidgetName()
	{
		return DIYToolkit.PANE;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public boolean actionPerformed(ActionEvent event)
	{
		// todo
		return true;
	}
}