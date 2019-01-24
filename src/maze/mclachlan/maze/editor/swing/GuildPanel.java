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

package mclachlan.maze.editor.swing;

import java.util.Vector;
import java.util.Collections;
import mclachlan.maze.data.Database;
import mclachlan.maze.stat.PlayerCharacter;

/**
 *
 */
public class GuildPanel extends PlayerCharactersPanel
{
	/*-------------------------------------------------------------------------*/
	public GuildPanel()
	{
		super(SwingEditor.Tab.GUILD);
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public Vector getCharacterNames()
	{
		Vector<String> vec = new Vector<String>(Database.getInstance().getCharacterGuild().keySet());
		Collections.sort(vec);
		return vec;
	}

	/*-------------------------------------------------------------------------*/
	public PlayerCharacter getPlayerCharacter(String name)
	{
		return Database.getInstance().getCharacterGuild().get(name);
	}
	
	/*-------------------------------------------------------------------------*/
	public void commitPlayerCharacter(PlayerCharacter pc)
	{
		Database.getInstance().getCharacterGuild().put(pc.getName(), pc);
	}
}
