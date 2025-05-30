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

import java.awt.Color;
import java.awt.Point;
import java.util.*;
import mclachlan.maze.stat.*;
import mclachlan.maze.util.MazeException;

/**
 *
 */
public class Npc extends Foe
{
	private static final Comparator<Item> cmp = new InventoryComparator<Item>();

	private NpcTemplate template;

	//
	// general parameters
	//

	/** The attitude of this NPC towards the party */
	private NpcFaction.Attitude attitude;

	//
	// trading parameters
	//

	/**
	 * What this NPC has in stock at the moment, for trade.
	 * Note that this is distinct to Foe.inventory, which is on their person
	 * and can be used in combat, but is not available for trade.
	 */
	private List<Item> tradingInventory;

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

	public Npc()
	{
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Recreates an NPC with the given state
	 */
	public Npc(
		NpcTemplate template,
		NpcFaction.Attitude attitude,
		List<Item> tradingInventory,
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
		this.tradingInventory = new ArrayList<>(tradingInventory);
		this.dead = dead;
		this.found = found;
		this.template = template;
		this.theftCounter = theftCounter;
		this.tile = tile;
		this.zone = zone;
		this.guildMaster = guildMaster;
		this.guild = guild;

		init();
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
			new ArrayList<>(),
			npcTemplate.getTheftCounter(),
			npcTemplate.getTile(),
			npcTemplate.getZone(),
			npcTemplate.isFound(),
			npcTemplate.isDead(),
			npcTemplate.isGuildMaster(),
			new ArrayList<>(),
			npcTemplate.getFoeTemplate());

		this.template.getScript().start();
	}

	/*-------------------------------------------------------------------------*/
	public void init()
	{
		this.questManager = new QuestManager(this.getName());
		this.template.getScript().npc = this;
		this.template.getScript().initialise();

		if (super.template == null)
		{
			super.setTemplate(template.getFoeTemplate());
		}
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
		return template.getBuysAt();
	}

	public List<Item> getTradingInventory()
	{
		return tradingInventory;
	}

	@Override
	public List<Item> getStealableItems()
	{
		return this.tradingInventory;
	}

	public String getFaction()
	{
		return template.getFaction();
	}

	public String getFoeName()
	{
		return template.getFoeName();
	}

	public String getDisplayName()
	{
		return template.getDisplayName();
	}

	public NpcInventoryTemplate getInventoryTemplate()
	{
		return template.getInventoryTemplate();
	}

	public int getMaxPurchasePrice()
	{
		return template.getMaxPurchasePrice();
	}

	@Override
	public int getMaxStealableGold()
	{
		return getMaxPurchasePrice();
	}

	public NpcScript getScript()
	{
		return template.getScript();
	}

	public int getSellsAt()
	{
		return template.getSellsAt();
	}

	public String getAlliesOnCall()
	{
		return template.getAlliesOnCall();
	}

	public Point getTile()
	{
		return tile;
	}

	public BitSet getWillBuyItemTypes()
	{
		return template.getWillBuyItemTypes();
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
		return template.getResistBribes();
	}

	public int getResistThreats()
	{
		return template.getResistThreats();
	}

	public int getResistSteal()
	{
		return template.getResistSteal();
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

	@Override
	public NpcScript getActionScript()
	{
		return getScript();
	}

	public void setTradingInventory(List<Item> inv)
	{
		this.tradingInventory = new ArrayList<>();

		if (inv != null)
		{
			this.tradingInventory.addAll(inv);
		}
	}

	@Override
	public boolean isInterestedInBuyingItem(Item item, PlayerCharacter pc)
	{
		return this.template.getWillBuyItemTypes() != null &&
			this.template.getWillBuyItemTypes().get(item.getType()) &&
			!item.isQuestItem();
	}

	@Override
	public boolean isAbleToAffordItem(Item item, PlayerCharacter pc)
	{
		int itemCost = GameSys.getInstance().getItemCost(item, this.template.getBuysAt(), pc);
		return itemCost <= this.template.getMaxPurchasePrice();
	}

	public void removeTradingItem(Item item, boolean removeWholeStack)
	{
		if (!tradingInventory.remove(item))
		{
			throw new MazeException("Item not in NPC inventory: " + item.getName());
		}
	}

	public void addTradingItem(Item item)
	{
		if (tradingInventory != null)
		{
			tradingInventory.add(item);
		}
	}

	/*-------------------------------------------------------------------------*/
	public void sortTradingInventory()
	{
		if (tradingInventory != null)
		{
			tradingInventory.sort(cmp);
		}
	}

	/*-------------------------------------------------------------------------*/
	public int getLevel()
	{
		return super.getLevel();
	}

	public String getName()
	{
		return template.getName();
	}

	public QuestManager getQuestManager()
	{
		return questManager;
	}

	public List<Item> getCurrentInventory()
	{
		return tradingInventory;
	}

	public void setTheftCounter(int theftCounter)
	{
		this.theftCounter = theftCounter;
	}

	public Color getSpeechColour()
	{
		return template.getSpeechColour();
	}

	/*-------------------------------------------------------------------------*/
	private static class InventoryComparator<T> implements Comparator<Item>
	{
		public int compare(Item a, Item b)
		{
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
