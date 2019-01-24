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

import java.util.*;
import mclachlan.maze.stat.PlayerCharacter;

/**
 *
 */
public class SaveGamePlayerCharactersPanel extends PlayerCharactersPanel
{
	private String saveGameName;
	private Map<String,PlayerCharacter> map;

	/*-------------------------------------------------------------------------*/
	public SaveGamePlayerCharactersPanel(String saveGameName)
	{
		super(SwingEditor.Tab.SAVE_GAMES);
		this.saveGameName = saveGameName;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public Vector getCharacterNames()
	{
		if (map == null)
		{
			return new Vector();
		}
		Vector vector = new Vector(map.keySet());
		Collections.sort(vector);
		return vector;
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
	public void refresh(Map<String, PlayerCharacter> playerCharacterCache)
	{
		map = playerCharacterCache;

		refreshNames(null);
	}
}
