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

package mclachlan.maze.stat;

import java.awt.Point;
import java.util.*;
import mclachlan.crusader.EngineObject;
import mclachlan.crusader.Texture;
import mclachlan.maze.data.Database;
import mclachlan.maze.data.Loader;
import mclachlan.maze.data.Saver;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.game.PartyCamp;
import mclachlan.maze.game.PartyCampMouseClickScript;
import mclachlan.maze.map.Zone;

/**
 * Manages the single temporary party camp allowed per save game.
 */
public class PartyCampManager implements GameCache
{
	/** Placeholder until a dedicated camp texture is supplied. */
	public static final String PARTY_CAMP_TEXTURE = "PARTY_CAMP";

	public static final String ENGINE_OBJECT_NAME = "party-camp";

	private static final PartyCampManager instance = new PartyCampManager();

	private PartyCamp camp;

	/*-------------------------------------------------------------------------*/
	public static PartyCampManager getInstance()
	{
		return instance;
	}

	/*-------------------------------------------------------------------------*/
	public static void resetForTesting()
	{
		instance.camp = null;
	}

	/*-------------------------------------------------------------------------*/
	public void clear()
	{
		camp = null;
	}

	/*-------------------------------------------------------------------------*/
	public boolean hasCamp()
	{
		return camp != null && !camp.getCharacterNames().isEmpty();
	}

	/*-------------------------------------------------------------------------*/
	public PartyCamp getCamp()
	{
		return camp;
	}

	/*-------------------------------------------------------------------------*/
	public boolean isCampAt(Zone zone, Point tile)
	{
		return camp != null
			&& camp.getZone().equals(zone.getName())
			&& camp.getTile().equals(tile);
	}

	/*-------------------------------------------------------------------------*/
	public boolean isCampAt(String zoneName, Point tile)
	{
		return camp != null
			&& camp.getZone().equals(zoneName)
			&& camp.getTile().equals(tile);
	}

	/*-------------------------------------------------------------------------*/
	public void createCamp(String zoneName, Point tile)
	{
		camp = new PartyCamp(zoneName, tile, new ArrayList<>());
	}

	/*-------------------------------------------------------------------------*/
	public void addCharacter(String name)
	{
		if (camp == null)
		{
			throw new IllegalStateException("no camp");
		}
		camp.getCharacterNames().add(name);
	}

	/*-------------------------------------------------------------------------*/
	public void removeCharacter(String name)
	{
		if (camp != null)
		{
			camp.getCharacterNames().remove(name);
		}
	}

	/*-------------------------------------------------------------------------*/
	public void clearIfEmpty()
	{
		if (camp != null && camp.getCharacterNames().isEmpty())
		{
			camp = null;
		}
	}

	/*-------------------------------------------------------------------------*/
	public List<PlayerCharacter> getCampPlayerCharacters(Maze maze)
	{
		List<PlayerCharacter> result = new ArrayList<>();
		if (camp == null)
		{
			return result;
		}

		for (String name : camp.getCharacterNames())
		{
			PlayerCharacter pc = maze.getPlayerCharacters().get(name);
			if (pc != null)
			{
				result.add(pc);
			}
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public void syncVisual(Maze maze)
	{
		maze.removeObject(ENGINE_OBJECT_NAME);

		if (camp == null || camp.getCharacterNames().isEmpty())
		{
			return;
		}

		Zone currentZone = maze.getCurrentZone();
		if (currentZone == null || !camp.getZone().equals(currentZone.getName()))
		{
			return;
		}

		EngineObject obj = createEngineObject();
		obj.setTileIndex(currentZone.getTileIndex(camp.getTile()));
		maze.addObject(obj);
	}

	/*-------------------------------------------------------------------------*/
	private EngineObject createEngineObject()
	{
		Texture texture = Database.getInstance()
			.getMazeTexture(PARTY_CAMP_TEXTURE)
			.getTexture();

		return new EngineObject(
			ENGINE_OBJECT_NAME,
			0, 0,
			texture,
			texture,
			texture,
			texture,
			0,
			false,
			new PartyCampMouseClickScript(),
			EngineObject.Alignment.BOTTOM);
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public void loadGame(String name, Loader loader,
		Map<String, PlayerCharacter> playerCharacterCache) throws Exception
	{
		this.camp = loader.loadPartyCamp(name);
		clearIfEmpty();
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public void saveGame(String saveGameName, Saver saver) throws Exception
	{
		saver.savePartyCamp(saveGameName, camp);
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public List<MazeEvent> endOfTurn(long turnNr)
	{
		return null;
	}
}
