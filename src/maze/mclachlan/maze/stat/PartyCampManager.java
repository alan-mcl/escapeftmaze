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
 * Manages temporary party camps where members can be left behind while exploring.
 * Multiple camps may exist after forced splits; voluntary split still allows at most
 * one player-created camp location (enforced in {@link Maze}).
 */
public class PartyCampManager implements GameCache
{
	/** Placeholder until a dedicated camp texture is supplied. */
	public static final String PARTY_CAMP_TEXTURE = "PARTY_CAMP";

	public static final String ENGINE_OBJECT_NAME_PREFIX = "party-camp-";

	private static final PartyCampManager instance = new PartyCampManager();

	private List<PartyCamp> camps = new ArrayList<>();

	/*-------------------------------------------------------------------------*/
	public static PartyCampManager getInstance()
	{
		return instance;
	}

	/*-------------------------------------------------------------------------*/
	public static void resetForTesting()
	{
		instance.camps = new ArrayList<>();
	}

	/*-------------------------------------------------------------------------*/
	public static String engineObjectName(PartyCamp camp)
	{
		String zoneKey = camp.getZone().replace(' ', '_');
		return ENGINE_OBJECT_NAME_PREFIX + zoneKey + "-" + camp.getTile().x + "-" + camp.getTile().y;
	}

	/*-------------------------------------------------------------------------*/
	public void clear()
	{
		camps = new ArrayList<>();
	}

	/*-------------------------------------------------------------------------*/
	public boolean hasCamp()
	{
		return hasAnyCamp();
	}

	/*-------------------------------------------------------------------------*/
	public boolean hasAnyCamp()
	{
		for (PartyCamp camp : camps)
		{
			if (!camp.getCharacterNames().isEmpty())
			{
				return true;
			}
		}
		return false;
	}

	/*-------------------------------------------------------------------------*/
	public PartyCamp getCamp()
	{
		for (PartyCamp camp : camps)
		{
			if (!camp.getCharacterNames().isEmpty())
			{
				return camp;
			}
		}
		return null;
	}

	/*-------------------------------------------------------------------------*/
	public List<PartyCamp> getCamps()
	{
		return Collections.unmodifiableList(camps);
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * The last non-empty camp in creation order ({@link #ensureCampAt} append order).
	 */
	public PartyCamp getMostRecentCamp()
	{
		for (int i = camps.size() - 1; i >= 0; i--)
		{
			PartyCamp camp = camps.get(i);
			if (!camp.getCharacterNames().isEmpty())
			{
				return camp;
			}
		}
		return null;
	}

	/*-------------------------------------------------------------------------*/
	public PartyCamp findCampAt(Zone zone, Point tile)
	{
		return findCampAt(zone.getName(), tile);
	}

	/*-------------------------------------------------------------------------*/
	public PartyCamp findCampAt(String zoneName, Point tile)
	{
		for (PartyCamp camp : camps)
		{
			if (camp.getZone().equals(zoneName) && camp.getTile().equals(tile))
			{
				return camp;
			}
		}
		return null;
	}

	/*-------------------------------------------------------------------------*/
	public boolean isCampAt(Zone zone, Point tile)
	{
		return findCampAt(zone, tile) != null
			&& !findCampAt(zone, tile).getCharacterNames().isEmpty();
	}

	/*-------------------------------------------------------------------------*/
	public boolean isCampAt(String zoneName, Point tile)
	{
		PartyCamp camp = findCampAt(zoneName, tile);
		return camp != null && !camp.getCharacterNames().isEmpty();
	}

	/*-------------------------------------------------------------------------*/
	public PartyCamp createCamp(String zoneName, Point tile)
	{
		PartyCamp existing = findCampAt(zoneName, tile);
		if (existing != null)
		{
			return existing;
		}

		PartyCamp camp = new PartyCamp(zoneName, tile, new ArrayList<>());
		camps.add(camp);
		return camp;
	}

	/*-------------------------------------------------------------------------*/
	public PartyCamp ensureCampAt(String zoneName, Point tile)
	{
		return createCamp(zoneName, tile);
	}

	/*-------------------------------------------------------------------------*/
	public void addCharacter(PartyCamp camp, String name)
	{
		if (camp == null)
		{
			throw new IllegalStateException("no camp");
		}
		camp.getCharacterNames().add(name);
	}

	/*-------------------------------------------------------------------------*/
	public void addCharacter(String name)
	{
		PartyCamp camp = getCamp();
		if (camp == null)
		{
			throw new IllegalStateException("no camp");
		}
		addCharacter(camp, name);
	}

	/*-------------------------------------------------------------------------*/
	public void removeCharacter(PartyCamp camp, String name)
	{
		if (camp != null)
		{
			camp.getCharacterNames().remove(name);
		}
	}

	/*-------------------------------------------------------------------------*/
	public void removeCharacter(String name)
	{
		for (PartyCamp camp : camps)
		{
			camp.getCharacterNames().remove(name);
		}
	}

	/*-------------------------------------------------------------------------*/
	public void removeCampIfEmpty(PartyCamp camp)
	{
		if (camp != null && camp.getCharacterNames().isEmpty())
		{
			camps.remove(camp);
		}
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Removes a camp record and its current-zone visual object (if any).
	 */
	public void removeCamp(PartyCamp camp, Maze maze)
	{
		if (camp == null)
		{
			return;
		}

		if (maze != null)
		{
			maze.removeObject(engineObjectName(camp));
		}
		camps.remove(camp);
	}

	/*-------------------------------------------------------------------------*/
	public void clearIfEmpty()
	{
		camps.removeIf(c -> c.getCharacterNames().isEmpty());
	}

	/*-------------------------------------------------------------------------*/
	public List<PlayerCharacter> getCampPlayerCharacters(Maze maze, PartyCamp camp)
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
	public List<PlayerCharacter> getCampPlayerCharacters(Maze maze)
	{
		PartyCamp camp = getCamp();
		return getCampPlayerCharacters(maze, camp);
	}

	/*-------------------------------------------------------------------------*/
	public void syncVisual(Maze maze)
	{
		Zone currentZone = maze.getCurrentZone();
		if (currentZone == null)
		{
			return;
		}

		String zoneName = currentZone.getName();
		for (PartyCamp camp : camps)
		{
			if (camp.getZone().equals(zoneName))
			{
				maze.removeObject(engineObjectName(camp));
			}
		}

		for (PartyCamp camp : camps)
		{
			if (camp.getCharacterNames().isEmpty())
			{
				continue;
			}
			if (!camp.getZone().equals(zoneName))
			{
				continue;
			}

			EngineObject obj = createEngineObject(camp);
			obj.setTileIndex(currentZone.getTileIndex(camp.getTile()));
			maze.addObject(obj);
		}
	}

	/*-------------------------------------------------------------------------*/
	private EngineObject createEngineObject(PartyCamp camp)
	{
		Texture texture = Database.getInstance()
			.getMazeTexture(PARTY_CAMP_TEXTURE)
			.getTexture();

		return new EngineObject(
			engineObjectName(camp),
			0, 0,
			texture,
			texture,
			texture,
			texture,
			0,
			false,
			new PartyCampMouseClickScript(camp.getZone(), camp.getTile()),
			EngineObject.Alignment.BOTTOM);
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public void loadGame(String name, Loader loader,
		Map<String, PlayerCharacter> playerCharacterCache) throws Exception
	{
		List<PartyCamp> loaded = loader.loadPartyCamps(name);
		this.camps = loaded != null ? new ArrayList<>(loaded) : new ArrayList<>();
		clearIfEmpty();
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public void saveGame(String saveGameName, Saver saver) throws Exception
	{
		List<PartyCamp> toSave = new ArrayList<>();
		for (PartyCamp camp : camps)
		{
			if (!camp.getCharacterNames().isEmpty())
			{
				toSave.add(camp);
			}
		}
		saver.savePartyCamps(saveGameName, toSave);
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public List<MazeEvent> endOfTurn(long turnNr)
	{
		return null;
	}
}
