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

import java.awt.*;
import mclachlan.diygui.DIYButton;
import mclachlan.diygui.toolkit.*;
import mclachlan.maze.game.Maze;

/**
 *
 */
public class ButtonToolbar extends ContainerWidget implements ActionListener
{
	private static final int inset = 12;
	private final DIYButton modifiers, stats, properties, inventory, magic, exit;

	/*-------------------------------------------------------------------------*/
	public ButtonToolbar(Rectangle bounds)
	{
		super(bounds.x+inset, bounds.y+inset, bounds.width-inset*2, bounds.height-inset*2);

		int maxButtons = 6;

		this.setLayoutManager(new DIYGridLayout(maxButtons, 1, inset, inset));

		inventory = new DIYButton("(I)nventory");
		inventory.addActionListener(this);

		modifiers = new DIYButton("(M)odifiers");
		modifiers.addActionListener(this);
		
		stats = new DIYButton("(S)tats");
		stats.addActionListener(this);

		properties = new DIYButton("(P)roperties");
		properties.addActionListener(this);

		magic = new DIYButton("M(a)gic");
		magic.addActionListener(this);
		
		exit = new DIYButton("(E)xit");
		exit.addActionListener(this);

		super.add(inventory);
		super.add(modifiers);
		super.add(stats);
		super.add(properties);
		super.add(magic);
		super.add(exit);
	}

	/*-------------------------------------------------------------------------*/
	public String getWidgetName()
	{
		return DIYToolkit.PANE;
	}

	/*-------------------------------------------------------------------------*/
	public boolean actionPerformed(ActionEvent event)
	{
		if (event.getSource() == modifiers)
		{
			modifiers();
			return true;
		}
		else if (event.getSource() == stats)
		{
			stats();
			return true;
		}
		else if (event.getSource() == properties)
		{
			properties();
			return true;
		}
		else if (event.getSource() == inventory)
		{
			inventory();
			return true;
		}
		else if (event.getSource() == magic)
		{
			magic();
			return true;
		}
		else if (event.getSource() == exit)
		{
			exit();
			return true;
		}
		return false;
	}

	public void exit()
	{
		if (Maze.getInstance().isInCombat())
		{
			Maze.getInstance().setState(Maze.State.COMBAT);
		}
		else
		{
			Maze.getInstance().setState(Maze.State.MOVEMENT);
		}
	}

	public void magic()
	{
		Maze.getInstance().setState(Maze.State.MAGIC);
	}

	public void inventory()
	{
		Maze.getInstance().setState(Maze.State.INVENTORY);
	}

	public void properties()
	{
		Maze.getInstance().setState(Maze.State.PROPERTIESDISPLAY);
	}

	public void stats()
	{
		Maze.getInstance().setState(Maze.State.STATSDISPLAY);
	}

	public void modifiers()
	{
		Maze.getInstance().setState(Maze.State.MODIFIERSDISPLAY);
	}
}
