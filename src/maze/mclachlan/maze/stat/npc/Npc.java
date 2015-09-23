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
import mclachlan.maze.stat.*;
import mclachlan.maze.util.MazeException;

/**
 *
 */
public class Npc extends Foe
{
	private static Comparator<Item> cmp = new InventoryComparator<Item>();

	private NpcTemplate template;

	//
	// general parameters
	//

	/** The attitude of this NPC towards the party */
	private NpcFaction.Attitude attitude;

	//
	// trading parameters
	//

	/** what this NPC has in stock at the moment */
	private List<Item> currentInventory;

	//
	// Interaction parameters
	//

	/** a measure of how likely the NPC thinks the party is of stealing from him,
	 * based on their past actions */
	private int theftCounter;

	//
	// map parameters
	//

	/** the current zone that the NPC is in */
	private String zone;

	/** the tile that the NPC is on */
	private Point tile;

	/** whether the PCs have ever met this NPC before */
	private boolean found = false;

	/** whether this NPC is dead */
	private boolean dead = false;
	
	/** whether this NPC is a guild master */
	private boolean guildMaster = false;
	
	/** characters in the guild, recruitable from this NPC */
	private List<String> guild;

	/** manager for quests, if the NPC has them */
	private QuestManager questManager;

	/*-------------------------------------------------------------------------*/
	/**
	 * Recreates an NPC with the given state
	 */
	public Npc(
		NpcTemplate template,
		NpcFaction.Attitude attitude,
		List<Item> currentInventory,
		int theftCounter,
		Point tile,
		String zone,
		boolean found,
		boolean dead,
		boolean guildMaster,
		List<String> guild,
		FoeTemplate foeTemplate)
	{
		super(foeTemplate);

		this.attitude = attitude;
		this.currentInventory = currentInventory;
		this.currentInventory.addAll(currentInventory);
		this.dead = dead;
		this.found = found;
		this.template = template;
		this.theftCounter = theftCounter;
		this.tile = tile;
		this.zone = zone;
		this.guildMaster = guildMaster;
		this.guild = guild;
		this.questManager = new QuestManager(this);
		this.template.script.npc = this;
		this.template.script.initialise();
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Creates a new NPC from the given template
	 */
	public Npc(NpcTemplate npcTemplate)
	{
		this(
			npcTemplate,
			npcTemplate.getAttitude(),
			new ArrayList<Item>(),
			npcTemplate.getTheftCounter(),
			npcTemplate.tile,
			npcTemplate.zone,
			npcTemplate.found,
			npcTemplate.dead,
			npcTemplate.guildMaster,
			new ArrayList<String>(),
			npcTemplate.getFoeTemplate());

		this.template.script.start();
	}

	/*-------------------------------------------------------------------------*/
	public NpcFaction.Attitude getAttitude()
	{
		return attitude;
	}

	public void setAttitude(NpcFaction.Attitude attitude)
	{
		this.attitude = attitude;
	}

	public void changeAttitude(NpcFaction.AttitudeChange change)
	{
		this.attitude = GameSys.getInstance().calcAttitudeChange(this.attitude, change);
	}

	public int getBuysAt()
	{
		return template.buysAt;
	}

	public List<Item> getTradingInventory()
	{
		return currentInventory;
	}

	@Override
	public List<Item> getStealableItems()
	{
		return this.currentInventory;
	}

	public String getFaction()
	{
		return template.faction;
	}

	public String getFoeName()
	{
		return template.foeName;
	}

	public String getDisplayName()
	{
		return template.displayName;
	}

	public NpcInventoryTemplate getInventoryTemplate()
	{
		return template.inventoryTemplate;
	}

	public int getMaxPurchasePrice()
	{
		return template.maxPurchasePrice;
	}

	public NpcScript getScript()
	{
		return template.script;
	}

	public int getSellsAt()
	{
		return template.sellsAt;
	}

	public String getAlliesOnCall()
	{
		return template.alliesOnCall;
	}

	public Point getTile()
	{
		return tile;
	}

	public BitSet getWillBuyItemTypes()
	{
		return template.willBuyItemTypes;
	}

	public String getZone()
	{
		return zone;
	}

	@Override
	public boolean isFound()
	{
		return found;
	}

	@Override
	public void setFound(boolean found)
	{
		this.found = found;
	}

	public int getResistBribes()
	{
		return template.resistBribes;
	}

	public int getResistThreats()
	{
		return template.resistThreats;
	}

	public int getResistSteal()
	{
		return template.resistSteal;
	}

	public int getTheftCounter()
	{
		return theftCounter;
	}

	public void incTheftCounter(int value)
	{
		this.theftCounter += value;
	}

	public boolean isDead()
	{
		return dead;
	}

	public void setDead(boolean dead)
	{
		this.dead = dead;
	}

	public void setZone(String zone)
	{
		this.zone = zone;
	}

	public void setTile(Point tile)
	{
		this.tile = tile;
	}

	public NpcTemplate getTemplate()
	{
		return template;
	}

	public void setTemplate(NpcTemplate template)
	{
		this.template = template;
	}
	
	public boolean isGuildMaster()
	{
		return this.guildMaster;
	}

	public void setGuildMaster(boolean guildMaster)
	{
		this.guildMaster = guildMaster;
	}

	public List<String> getGuild()
	{
		return guild;
	}

	public void setGuild(List<String> guild)
	{
		this.guild = guild;
	}
	
	/*-------------------------------------------------------------------------*/
	public CurMaxSub getHitPoints()
	{
		// overridden so that the Actor counts as alive.
		return new CurMaxSub(1);
	}

	@Override
	public NpcScript getActionScript()
	{
		return getScript();
	}

	public void setCurrentInventory(List<Item> inv)
	{
		this.currentInventory.clear();
		this.currentInventory.addAll(inv);
	}

	@Override
	public boolean isInterestedInBuyingItem(Item item)
	{
		return this.template.willBuyItemTypes != null &&
			this.template.willBuyItemTypes.get(item.getType()) &&
			!item.isQuestItem();
	}

	@Override
	public boolean isAbleToAffordItem(Item item)
	{
		return GameSys.getInstance().getItemCost(
			item, this.template.buysAt) <= this.template.maxPurchasePrice;
	}

	@Override
	public void removeItem(Item item, boolean removeWholeStack)
	{
		if (!currentInventory.remove(item))
		{
			throw new MazeException("Item not in NPC inventory: "+item.getName());
		}
	}

	public void addItem(Item item)
	{
		if (currentInventory != null)
		{
			currentInventory.add(item);
		}
	}

	/*-------------------------------------------------------------------------*/
	public void sortInventory()
	{
		if (currentInventory != null)
		{
			currentInventory.sort(cmp);
		}
	}

	@Override
	public boolean addInventoryItem(Item item)
	{
		addItem(item);
		return true;
	}

	/*-------------------------------------------------------------------------*/
	public int getLevel()
	{
		return super.getLevel();
	}

	public String getName()
	{
		return super.getName();
	}

	public QuestManager getQuestManager()
	{
		return questManager;
	}

	/*-------------------------------------------------------------------------*/
	private static class InventoryComparator<T> implements Comparator
	{
		public int compare(Object o, Object o1)
		{
			Item a = (Item)o;
			Item b = (Item)o1;

			// first sort key: item type
			if (a.getType() != b.getType())
			{
				return a.getType() - b.getType();
			}

			// second sort key: item cost
			if (a.getBaseCost() != b.getBaseCost())
			{
				return a.getBaseCost() - b.getBaseCost();
			}

			// third sort key: item name
			return a.getName().compareTo(b.getName());
		}
	}

	/*-------------------------------------------------------------------------*/
	public static class TheftResult
	{
		public static final int SUCCESS = 1;
		public static final int FAILED_UNDETECTED = 2;
		public static final int FAILED_DETECTED = 3;
	}
}
