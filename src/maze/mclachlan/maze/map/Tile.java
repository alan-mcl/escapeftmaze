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

package mclachlan.maze.map;

import java.util.*;
import java.util.List;
import java.awt.*;
import mclachlan.maze.stat.*;
import mclachlan.maze.stat.condition.Condition;
import mclachlan.maze.stat.condition.ConditionBearer;
import mclachlan.maze.stat.condition.ConditionManager;
import mclachlan.maze.stat.magic.MagicSys;
import mclachlan.maze.util.MazeException;
import mclachlan.maze.game.Maze;

/**
 * Represents one tile on the map.
 */
public class Tile implements ConditionBearer
{
	List<TileScript> scripts;
	/** Modifiers to any actor on this tile */
	StatModifier statModifier;
	String terrainType;
	String terrainSubType;
	/** chance of a random encounter per turn, expressed as a value out of 1000 */
	int randomEncounterChance;
	EncounterTable randomEncounters;

	String zone;
	Point coords;
	
	public static final int MAX_TILE_MAGIC = 13;
	
	private static Set<String> magicModifiers;
	
	/*-------------------------------------------------------------------------*/
	static
	{
		magicModifiers = new HashSet<String>();
		magicModifiers.add(Stats.Modifiers.BLACK_MAGIC_GEN);
		magicModifiers.add(Stats.Modifiers.BLUE_MAGIC_GEN);
		magicModifiers.add(Stats.Modifiers.RED_MAGIC_GEN);
		magicModifiers.add(Stats.Modifiers.WHITE_MAGIC_GEN);
		magicModifiers.add(Stats.Modifiers.GREEN_MAGIC_GEN);
		magicModifiers.add(Stats.Modifiers.PURPLE_MAGIC_GEN);
		magicModifiers.add(Stats.Modifiers.GOLD_MAGIC_GEN);
	}

	/*-------------------------------------------------------------------------*/
	public Tile(
		List<TileScript> scripts,
		EncounterTable randomEncounters,
		StatModifier statModifier,
		String terrainType,
		String terrainSubType,
		int randomEncounterChance)
	{
		this.randomEncounters = randomEncounters;
		this.scripts = scripts;
		this.statModifier = statModifier;
		this.terrainType = terrainType;
		this.terrainSubType = terrainSubType;
		this.randomEncounterChance = randomEncounterChance;
	}

	/*-------------------------------------------------------------------------*/
	private int getModifier(String modifier)
	{
		int result = statModifier.getModifier(modifier);

		for (Condition c : ConditionManager.getInstance().getConditions(this))
		{
			result += c.getModifier(modifier, this);
		}
		
		if (magicModifiers.contains(modifier) && result > MAX_TILE_MAGIC)
		{
			result = MAX_TILE_MAGIC;
		}
		
		return result;
	}

	public int getAmountBlackMagic()
	{
		String modifier = Stats.Modifiers.BLACK_MAGIC_GEN;
		return getModifier(modifier);
	}

	public int getAmountBlueMagic()
	{
		return getModifier(Stats.Modifiers.BLUE_MAGIC_GEN);
	}

	public int getAmountGoldMagic()
	{
		return getModifier(Stats.Modifiers.GOLD_MAGIC_GEN);
	}

	public int getAmountGreenMagic()
	{
		return getModifier(Stats.Modifiers.GREEN_MAGIC_GEN);
	}

	public int getAmountPurpleMagic()
	{
		return getModifier(Stats.Modifiers.PURPLE_MAGIC_GEN);
	}

	public int getAmountRedMagic()
	{
		return getModifier(Stats.Modifiers.RED_MAGIC_GEN);
	}

	public int getAmountWhiteMagic()
	{
		return getModifier(Stats.Modifiers.WHITE_MAGIC_GEN);
	}

	public int getAmountMagicPresent(int type)
	{
		switch (type)
		{
			case MagicSys.ManaType.RED:  return getAmountRedMagic();
			case MagicSys.ManaType.BLACK: return getAmountBlackMagic();
			case MagicSys.ManaType.PURPLE: return getAmountPurpleMagic();
			case MagicSys.ManaType.GOLD: return getAmountGoldMagic();
			case MagicSys.ManaType.WHITE: return getAmountWhiteMagic();
			case MagicSys.ManaType.GREEN: return getAmountGreenMagic();
			case MagicSys.ManaType.BLUE: return getAmountBlueMagic();
			default: throw new MazeException("Invalid mana colour "+type);
		}
	}

	public StatModifier getStatModifier()
	{
		return statModifier;
	}

	public String getTerrainSubType()
	{
		return terrainSubType;
	}

	public String getTerrainType()
	{
		return terrainType;
	}

	public int getRandomEncounterChance()
	{
		return randomEncounterChance;
	}

	public EncounterTable getRandomEncounters()
	{
		return randomEncounters;
	}

	public List<TileScript> getScripts()
	{
		return scripts;
	}

	public String getZone()
	{
		return zone;
	}

	public void setZone(String zone)
	{
		this.zone = zone;
	}

	public Point getCoords()
	{
		return coords;
	}

	public void setCoords(Point coords)
	{
		this.coords = coords;
	}

	public void setRandomEncounterChance(int randomEncounterChance)
	{
		this.randomEncounterChance = randomEncounterChance;
	}

	public void setRandomEncounters(EncounterTable randomEncounters)
	{
		this.randomEncounters = randomEncounters;
	}

	public void setScripts(List<TileScript> scripts)
	{
		this.scripts = scripts;
	}

	public void setStatModifier(StatModifier statModifier)
	{
		this.statModifier = statModifier;
	}

	public void setTerrainSubType(String terrainSubType)
	{
		this.terrainSubType = terrainSubType;
	}

	public void setTerrainType(String terrainType)
	{
		this.terrainType = terrainType;
	}
	
	/*-------------------------------------------------------------------------*/
	/**
	 * Returns the stealth modifier required by actors in this tile.
	 */
	public String getStealthModifierRequired()
	{
		String terrainType = this.getTerrainType();
		String result = TerrainType.stealthModifiers.get(terrainType);
		if (result == null)
		{
			throw new MazeException("Invalid terrain type ["+terrainType+"]");
		}
		return result;
	}

	/*-------------------------------------------------------------------------*/
	public String getName()
	{
		return "tile";
	}

	/*-------------------------------------------------------------------------*/
	public String getDisplayName()
	{
		return getName();
	}

	public void addCondition(Condition c)
	{
		ConditionManager.getInstance().addCondition(this, c);
		Maze.getInstance().getUi().setTile(Maze.getInstance().getZone(), this, Maze.getInstance().getTile());
	}

	public void removeCondition(Condition c)
	{
		ConditionManager.getInstance().removeCondition(this, c);
	}

	public List<Condition> getConditions()
	{
		return ConditionManager.getInstance().getConditions(this);
	}

	/*-------------------------------------------------------------------------*/
	public static class TerrainType
	{
		public static final String FAKE = "fake";
		public static final String URBAN = "Urban";
		public static final String DUNGEON = "Dungeon";
		public static final String WILDERNESS = "Wilderness";
		public static final String WASTELAND = "Wasteland";

		static Map<String, String> stealthModifiers = new HashMap<String, String>();

		static
		{
			stealthModifiers.put(FAKE, "fake");
			stealthModifiers.put(URBAN, Stats.Modifiers.STREETWISE);
			stealthModifiers.put(DUNGEON, Stats.Modifiers.DUNGEONEER);
			stealthModifiers.put(WILDERNESS, Stats.Modifiers.WILDERNESS_LORE);
			stealthModifiers.put(WASTELAND, Stats.Modifiers.SURVIVAL);
		}
	}
}
