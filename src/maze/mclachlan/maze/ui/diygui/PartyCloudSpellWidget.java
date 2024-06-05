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
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import mclachlan.diygui.toolkit.ContainerWidget;
import mclachlan.diygui.toolkit.DIYToolkit;
import mclachlan.maze.data.Database;
import mclachlan.maze.game.Maze;
import mclachlan.maze.stat.PlayerParty;
import mclachlan.maze.stat.condition.CloudSpell;

/**
 * Displays icons for cloud spells affecting the party
 */
public class PartyCloudSpellWidget extends ContainerWidget
{
	private PlayerParty group;
	private Rectangle bounds;

	/*-------------------------------------------------------------------------*/
	public PartyCloudSpellWidget(PlayerParty group, Rectangle bounds)
	{
		super(bounds);
		this.group = group;
		this.bounds = bounds;
	}

	/*-------------------------------------------------------------------------*/
	public void draw(Graphics2D g)
	{
		if (group == null)
		{
			return;
		}

		for (int i = 0; i < group.getCloudSpells().size(); i++)
		{
			CloudSpell spell = group.getCloudSpells().get(i);
			BufferedImage icon = Database.getInstance().getImage(spell.getIcon());

			g.drawImage(
				icon,
				bounds.x+2+(24*i),
				bounds.y,
				Maze.getInstance().getComponent());
		}

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
	public PlayerParty getFoeGroup()
	{
		return group;
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
}