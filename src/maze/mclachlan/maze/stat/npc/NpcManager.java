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

package mclachlan.maze.stat.npc;

import java.awt.Point;
import java.util.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.data.Loader;
import mclachlan.maze.data.Saver;
import mclachlan.maze.game.Log;
import mclachlan.maze.game.Maze;
import mclachlan.maze.stat.Foe;
import mclachlan.maze.stat.GameCache;
import mclachlan.maze.stat.Item;
import mclachlan.maze.util.MazeException;

/**
 *
 */
public class NpcManager implements GameCache
{
	private Map<String, Npc> npcs;
	private Map<String, NpcFaction> factions;

	private static NpcManager instance = new NpcManager();

	/*-------------------------------------------------------------------------*/
	public static NpcManager getInstance()
	{
		return instance;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Start a game by adding all the NPCs at their initial positions.
	 */
	public void startGame()
	{
		this.npcs = new HashMap<String, Npc>();
		Map<String, NpcTemplate> npcTemplates = Database.getInstance().getNpcTemplates();
		for (NpcTemplate npcTemplate : npcTemplates.values())
		{
			initNpc(npcTemplate);
		}

		this.factions = new HashMap<String, NpcFaction>();
		Map<String, NpcFactionTemplate> npcFactionTemplates = Database.getInstance().getNpcFactionTemplates();

		for (NpcFactionTemplate template : npcFactionTemplates.values())
		{
			initFaction(template);
		}

		for (NpcFaction nf : factions.values())
		{
			nf.setAttitude(nf.getTemplate().getStartingAttitude());
		}
	}

	/*-------------------------------------------------------------------------*/
	private void initFaction(NpcFactionTemplate template)
	{
		factions.put(template.getName(), new NpcFaction(template));
	}

	/*-------------------------------------------------------------------------*/
	private void initNpc(NpcTemplate npcTemplate)
	{
		Npc npc = new Npc(npcTemplate);
		if (npc.getInventoryTemplate() != null)
		{
			List<Item> inv = npc.getInventoryTemplate().update(new ArrayList<Item>());
			npc.setCurrentInventory(inv);
			npc.sortInventory();
		}
		else
		{
			npc.setCurrentInventory(new ArrayList<Item>());
		}
		this.npcs.put(npc.getName(), npc);
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Loads the current NPC status.
	 */
	public void loadGame(String name, Loader loader) throws Exception
	{
		this.npcs = loader.loadNpcs(name);
		this.factions = loader.loadNpcFactions(name);

		//
		// If there are any templates that are not in the loaded map, add them now
		// This case can happen during development and mod making, but should
		// never happen during actual play
		//
		Map<String, NpcTemplate> templates = loader.loadNpcTemplates();
		for (String templateName : templates.keySet())
		{
			if (!this.npcs.containsKey(templateName))
			{
				initNpc(templates.get(templateName));
			}
		}

		Map<String, NpcFactionTemplate> factionTemplates =
			loader.loadNpcFactionTemplates();
		for (String factionName : factionTemplates.keySet())
		{
			if (!this.factions.containsKey(factionName))
			{
				initFaction(factionTemplates.get(factionName));
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	public void saveGame(String saveGameName, Saver saver) throws Exception
	{
		saver.saveNpcs(saveGameName, npcs);
		saver.saveNpcFactions(saveGameName, factions);
	}

	/*-------------------------------------------------------------------------*/
	public void endOfTurn(long turnNr)
	{
		updateNpcs(turnNr);
	}

	/*-------------------------------------------------------------------------*/
	public Npc[] getNpcsOnTile(String zone, Point tile)
	{
		if (this.npcs == null)
		{
			return new Npc[]{};
		}

		ArrayList<Npc> result = new ArrayList<Npc>();

		for (Npc npc : this.npcs.values())
		{
			if (npc.getZone().equals(zone) && npc.getTile().equals(tile) && !npc.isDead())
			{
				result.add(npc);
			}
		}

		return result.toArray(new Npc[result.size()]);
	}

	/*-------------------------------------------------------------------------*/
	private void updateNpcs(long turnNr)
	{
		Maze.log("updating NPCs...");
		for (Npc npc : this.npcs.values())
		{
			Maze.log(Log.DEBUG, "["+npc.getName()+"]");

			Maze.getInstance().appendEvents(npc.getScript().endOfTurn(turnNr));
			if (npc.getTradingInventory()!= null &&
				npc.getInventoryTemplate() != null &&
				(turnNr % 200 == 0))
			{
				Maze.log(Log.DEBUG, "updating inventory (previous size "+npc.getTradingInventory().size()+")");
				npc.getInventoryTemplate().update(npc.getTradingInventory());
				Maze.log(Log.DEBUG, "done (new size "+npc.getTradingInventory().size()+")");

			}
		}
		Maze.log("finished updating NPCs");
	}

	/*-------------------------------------------------------------------------*/
	public NpcFaction getNpcFaction(String name)
	{
		NpcFaction result = this.factions.get(name);
		if (result == null)
		{
			throw new MazeException("Invalid name ["+name+"]");
		}
		return result;
	}

	/*-------------------------------------------------------------------------*/
	public void npcDies(Foe foe)
	{
		for (Npc npc : npcs.values())
		{
			if (npc.getFoeName().equals(foe.getName()))
			{
				npc.setDead(true);
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	public Collection<Npc> getNpcs()
	{
		if (npcs != null)
		{
			return npcs.values();
		}
		else
		{
			return null;
		}
	}

	/*-------------------------------------------------------------------------*/
	public Npc getNpc(String name)
	{
		for (Npc npc : npcs.values())
		{
			if (npc.getName().equals(name))
			{
				return npc;
			}
		}

		throw new MazeException("Unrecognised NPC ["+name+"]");
	}

	/*-------------------------------------------------------------------------*/
	public boolean hasNpc(String name)
	{
		for (Npc npc : npcs.values())
		{
			if (npc.getName().equals(name))
			{
				return true;
			}
		}

		return false;
	}
}
