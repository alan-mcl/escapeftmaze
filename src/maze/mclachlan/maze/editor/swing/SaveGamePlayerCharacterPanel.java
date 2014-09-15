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

import mclachlan.maze.stat.PlayerCharacter;
import mclachlan.maze.data.Database;
import mclachlan.maze.util.MazeException;
import java.util.Vector;
import java.util.Collections;
import java.util.Map;

/**
 *
 */
public class SaveGamePlayerCharacterPanel extends PlayerCharacterPanel
{
	private String saveGameName;
	private Map<String,PlayerCharacter> map;

	/*-------------------------------------------------------------------------*/
	public SaveGamePlayerCharacterPanel(String saveGameName)
	{
		super(SwingEditor.Tab.SAVE_GAMES);
		this.saveGameName = saveGameName;
	}

	/*-------------------------------------------------------------------------*/
	public PlayerCharacter getPlayerCharacter(String name)
	{
		return map.get(name);
	}
	
	/*-------------------------------------------------------------------------*/
	public void commitPlayerCharacter(PlayerCharacter pc)
	{
		map.put(pc.getName(), pc);
	}
	
	/*-------------------------------------------------------------------------*/
	public Map<String, PlayerCharacter> getPlayerCharacters()
	{
		return map;
	}

	/*-------------------------------------------------------------------------*/
	public Vector loadData()
	{
		try
		{
			map = Database.getInstance().getLoader().loadPlayerCharacters(saveGameName);
			Vector<String> vec = new Vector<String>(
				map.keySet());
			Collections.sort(vec);
			return vec;
		}
		catch (Exception e)
		{
			throw new MazeException(e);
		}
	}
}
