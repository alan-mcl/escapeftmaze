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
import java.awt.event.KeyEvent;
import java.util.*;
import java.util.List;
import mclachlan.diygui.DIYScrollPane;
import mclachlan.maze.stat.PlayerCharacter;
import mclachlan.diygui.DIYListBox;
import mclachlan.diygui.toolkit.ContainerWidget;
import mclachlan.diygui.toolkit.DIYToolkit;

/**
 *
 */
public class GuildDisplayWidget extends ContainerWidget
{
	private final List<PlayerCharacter> characters;
	private final DIYListBox list;
	private final DIYScrollPane scroller;

	/*-------------------------------------------------------------------------*/
	public GuildDisplayWidget(Rectangle bounds, List<PlayerCharacter> characters)
	{
		super(bounds);
		this.characters = new ArrayList<>(characters);

		List<GuildCharacter> guildCharacters = getGuildCharactersList(characters);
		Collections.sort(guildCharacters);
		list = new DIYListBox(guildCharacters, new Rectangle(x, y, width, height));

		scroller = new DIYScrollPane(x, y, width, height, list);
		
		add(scroller);
	}

	/*-------------------------------------------------------------------------*/
	private List<GuildCharacter> getGuildCharactersList(List<PlayerCharacter> characters)
	{
		List<GuildCharacter> guildCharacters = new ArrayList<>();
		for (PlayerCharacter character : characters)
		{
			guildCharacters.add(new GuildCharacter(character));
		}
		return guildCharacters;
	}

	/*-------------------------------------------------------------------------*/
	public PlayerCharacter getSelected()
	{
		if (list.getSelected() == null)
		{
			return null;
		}
		else
		{
			return ((GuildCharacter)list.getSelected()).pc;
		}
	}

	/*-------------------------------------------------------------------------*/
	public String getWidgetName()
	{
		return DIYToolkit.PANE;
	}
	
	/*-------------------------------------------------------------------------*/
	public void remove(PlayerCharacter pc)
	{
		characters.remove(pc);
		list.setItems(getGuildCharactersList(characters));
		list.setSelected(null);
	}
	
	/*-------------------------------------------------------------------------*/
	public void add(PlayerCharacter pc)
	{
		characters.add(pc);
		list.setItems(getGuildCharactersList(characters));
	}
	
	/*-------------------------------------------------------------------------*/
	public void processKeyPressed(KeyEvent e)
	{
		switch (e.getKeyCode())
		{
			case KeyEvent.VK_UP, KeyEvent.VK_DOWN -> list.processKeyPressed(e);
			default ->
			{
			} // no op
		}
	}

	/*-------------------------------------------------------------------------*/
	public void refresh()
	{
		if (list.getItems().size() > 0)
		{
			list.setSelected(list.getItems().get(0));
		}
	}

	/*-------------------------------------------------------------------------*/

	@Override
	public void setBounds(int x, int y, int width, int height)
	{
		super.setBounds(x,y,width,height);

		if (list != null && scroller != null)
		{
			scroller.remove(list);
			list.setBounds(x, y, width, height);
			scroller.setBounds(x, y, width, height);
			scroller.add(list);
		}
	}

	/*-------------------------------------------------------------------------*/
	private static class GuildCharacter implements Comparable<GuildCharacter>
	{
		PlayerCharacter pc;

		/*----------------------------------------------------------------------*/
		public GuildCharacter(PlayerCharacter pc)
		{
			this.pc = pc;
		}

		/*----------------------------------------------------------------------*/
		public String toString()
		{
			if (pc != null)
			{
				return pc.getName()
					+" ("
					+pc.getRace().getName()
					+" "
					+pc.getCharacterClass().getName()
					+", lvl "
					+pc.getLevel()
					+")";
			}
			else
			{
				return "";
			}
		}

		/*----------------------------------------------------------------------*/
		public int compareTo(GuildCharacter other)
		{
			// sort by focus (combat/magic/stealth) then by name

			// bit of a hack here, comparing enum ordinals
			if (other.pc.getCharacterClass().getFocus() !=
				this.pc.getCharacterClass().getFocus())
			{
				return this.pc.getCharacterClass().getFocus().ordinal() -
					other.pc.getCharacterClass().getFocus().ordinal();
			}

			return this.pc.getName().compareTo(other.pc.getName());
		}
	}
}
