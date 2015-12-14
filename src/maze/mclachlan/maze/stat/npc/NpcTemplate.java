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

import java.awt.*;
import java.util.BitSet;
import mclachlan.maze.data.Database;
import mclachlan.maze.stat.FoeTemplate;

/**
 *
 */
public class NpcTemplate
{
	//
	// general parameters
	//

	/** The display name for this NPC */
	private String displayName;

	/** The name of the foe representing this NPC */
	private String foeName;

	/** The name of the faction that this NPC belongs to */
	private String faction;

	/** The attitude of this NPC towards the party */
	private NpcFaction.Attitude attitude;

	/** The all important NPC script */
	private NpcScript script;

	/** The name of an encounter table of aid that this NPC summons */
	private String alliesOnCall;

	//
	// trading parameters
	//

	/** base % of item price that this NPC buys items from the party at */
	private int buysAt;

	/** base % of item price that this NPC sells items to the party at */
	private int sellsAt;

	/** the max amount that this NPC will spend to buy an item from the party */
	private int maxPurchasePrice;

	/** which item types this NPC will buy, a bitset over the values in {@link
	 * mclachlan.maze.stat.ItemTemplate.Type} */
	private BitSet willBuyItemTypes;

	/** how this NPCs inventory is populated */
	private NpcInventoryTemplate inventoryTemplate;

	//
	// Interaction parameters
	//

	/** a positive value indicates a high resistance to threats */
	private int resistThreats;

	/** a positive value indicates a high resistance to bribes */
	private int resistBribes;

	/** a positive value indicates a high resistance to theft */
	private int resistSteal;

	/** a measure of how likely the NPC thinks the party is of stealing from him,
	 * based on their past actions */
	private int theftCounter;

	/** basic dialogue of this NPC */
	private NpcSpeech dialogue;

	//
	// map parameters
	//

	/** the current zone that the NPC is in */
	private String zone;

	/** the tile that the NPC is on */
	private Point tile;

	/** whether the PCs have ever met this NPC before */
	private boolean found;

	/** whether this NPC is dead */
	private boolean dead;
	
	/** whether this NPC is a guild master */
	private boolean guildMaster;

	/*-------------------------------------------------------------------------*/
	public NpcTemplate(
		String displayName,
		String foeName,
		String faction,
		NpcFaction.Attitude attitude,
		NpcScript script,
		String alliesOnCall,
		int buysAt,
		int sellsAt,
		int maxPurchasePrice,
		BitSet willBuyItemTypes,
		NpcInventoryTemplate inventoryTemplate,
		int resistThreats,
		int resistBribes,
		int resistSteal,
		int theftCounter,
		NpcSpeech dialogue,
		String zone,
		Point tile,
		boolean found,
		boolean dead,
		boolean guildMaster)
	{
		this.displayName = displayName;
		this.attitude = attitude;
		this.alliesOnCall = alliesOnCall;
		this.buysAt = buysAt;
		this.faction = faction;
		this.foeName = foeName;
		this.inventoryTemplate = inventoryTemplate;
		this.maxPurchasePrice = maxPurchasePrice;
		this.script = script;
		this.sellsAt = sellsAt;
		this.resistThreats = resistThreats;
		this.resistBribes = resistBribes;
		this.resistSteal = resistSteal;
		this.theftCounter = theftCounter;
		this.dialogue = dialogue;
		this.tile = tile;
		this.willBuyItemTypes = willBuyItemTypes;
		this.zone = zone;
		this.found = found;
		this.dead = dead;
		this.guildMaster = guildMaster;
	}

	/*-------------------------------------------------------------------------*/
	public String getAlliesOnCall()
	{
		return alliesOnCall;
	}

	public NpcFaction.Attitude getAttitude()
	{
		return attitude;
	}

	public int getBuysAt()
	{
		return buysAt;
	}

	public boolean isDead()
	{
		return dead;
	}

	public String getDisplayName()
	{
		return displayName;
	}

	public String getFaction()
	{
		return faction;
	}

	public String getFoeName()
	{
		return foeName;
	}

	public boolean isFound()
	{
		return found;
	}

	public NpcInventoryTemplate getInventoryTemplate()
	{
		return inventoryTemplate;
	}

	public int getMaxPurchasePrice()
	{
		return maxPurchasePrice;
	}

	public int getResistBribes()
	{
		return resistBribes;
	}

	public int getResistSteal()
	{
		return resistSteal;
	}

	public int getResistThreats()
	{
		return resistThreats;
	}

	public NpcScript getScript()
	{
		return script;
	}

	public int getSellsAt()
	{
		return sellsAt;
	}

	public int getTheftCounter()
	{
		return theftCounter;
	}

	public Point getTile()
	{
		return tile;
	}

	public BitSet getWillBuyItemTypes()
	{
		return willBuyItemTypes;
	}

	public String getZone()
	{
		return zone;
	}

	public String getName()
	{
		return displayName;
	}

	public boolean isGuildMaster()
	{
		return guildMaster;
	}

	public NpcSpeech getDialogue()
	{
		return dialogue;
	}

	/*-------------------------------------------------------------------------*/

	public void setDialogue(NpcSpeech dialogue)
	{
		this.dialogue = dialogue;
	}

	public void setAlliesOnCall(String alliesOnCall)
	{
		this.alliesOnCall = alliesOnCall;
	}

	public void setAttitude(NpcFaction.Attitude attitude)
	{
		this.attitude = attitude;
	}

	public void setBuysAt(int buysAt)
	{
		this.buysAt = buysAt;
	}

	public void setDead(boolean dead)
	{
		this.dead = dead;
	}

	public void setDisplayName(String displayName)
	{
		this.displayName = displayName;
	}

	public void setFaction(String faction)
	{
		this.faction = faction;
	}

	public void setFoeName(String foeName)
	{
		this.foeName = foeName;
	}

	public void setFound(boolean found)
	{
		this.found = found;
	}

	public void setInventoryTemplate(NpcInventoryTemplate inventoryTemplate)
	{
		this.inventoryTemplate = inventoryTemplate;
	}

	public void setMaxPurchasePrice(int maxPurchasePrice)
	{
		this.maxPurchasePrice = maxPurchasePrice;
	}

	public void setResistBribes(int resistBribes)
	{
		this.resistBribes = resistBribes;
	}

	public void setResistSteal(int resistSteal)
	{
		this.resistSteal = resistSteal;
	}

	public void setResistThreats(int resistThreats)
	{
		this.resistThreats = resistThreats;
	}

	public void setScript(NpcScript script)
	{
		this.script = script;
	}

	public void setSellsAt(int sellsAt)
	{
		this.sellsAt = sellsAt;
	}

	public void setTheftCounter(int theftCounter)
	{
		this.theftCounter = theftCounter;
	}

	public void setTile(Point tile)
	{
		this.tile = tile;
	}

	public void setWillBuyItemTypes(BitSet willBuyItemTypes)
	{
		this.willBuyItemTypes = willBuyItemTypes;
	}

	public void setZone(String zone)
	{
		this.zone = zone;
	}

	public void setName(String name)
	{
		this.displayName = name;
	}

	public void setGuildMaster(boolean guildMaster)
	{
		this.guildMaster = guildMaster;
	}

	public FoeTemplate getFoeTemplate()
	{
		return Database.getInstance().getFoeTemplate(this.getFoeName());
	}
}
