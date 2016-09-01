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
import mclachlan.maze.game.MazeEvent;
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
	/** scripts attached to this tile */
	private List<TileScript> scripts;

	/** Modifiers to any actor on this tile */
	private StatModifier statModifier;

	/** Terrain type of this tile, one of the enum */
	private TerrainType terrainType;
	/** Terrain sub type, free text*/
	private String terrainSubType;

	/** Chance of a random encounter per turn, expressed as a value out of 1000 */
	private int randomEncounterChance;
	/** Random encounter table for this tile */
	private EncounterTable randomEncounters;

	/** Zone to which this tile belongs */
	private String zone;
	/** X,Y coords of this tile */
	private Point coords;

	/** Danger of resting on this tile*/
	private RestingDanger restingDanger;
	private RestingEfficiency restingEfficiency;

	private static Set<String> magicModifiers;
	public static final int MAX_TILE_MAGIC = 13;

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
	public static enum TerrainType
	{
		FAKE("fake"),
		URBAN(Stats.Modifiers.STREETWISE),
		DUNGEON(Stats.Modifiers.DUNGEONEER),
		WILDERNESS(Stats.Modifiers.WILDERNESS_LORE),
		WASTELAND(Stats.Modifiers.SURVIVAL);

		private String stealthModifier;

		TerrainType(String stealthModifier)
		{
			this.stealthModifier = stealthModifier;
		}

		public String getStealthModifier()
		{
			return stealthModifier;
		}
	}

	/*-------------------------------------------------------------------------*/
	public static enum RestingDanger
	{
		NONE, LOW, MEDIUM, HIGH, EXTREME
	}

	/*-------------------------------------------------------------------------*/
	public static enum RestingEfficiency
	{
		POOR, AVERAGE, GOOD, EXCELLENT
	}

	/*-------------------------------------------------------------------------*/
	public Tile(
		List<TileScript> scripts,
		EncounterTable randomEncounters,
		StatModifier statModifier,
		TerrainType terrainType,
		String terrainSubType,
		int randomEncounterChance,
		RestingDanger restingDanger,
		RestingEfficiency restingEfficiency)
	{
		this.randomEncounters = randomEncounters;
		this.scripts = scripts;
		this.statModifier = statModifier;
		this.terrainType = terrainType;
		this.terrainSubType = terrainSubType;
		this.randomEncounterChance = randomEncounterChance;
		this.restingDanger = restingDanger;
		this.restingEfficiency = restingEfficiency;
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

	public TerrainType getTerrainType()
	{
		return terrainType;
	}

	public RestingDanger getRestingDanger()
	{
		return restingDanger;
	}

	public RestingEfficiency getRestingEfficiency()
	{
		return restingEfficiency;
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

	public void setTerrainType(TerrainType terrainType)
	{
		this.terrainType = terrainType;
	}

	public void setRestingDanger(RestingDanger restingDanger)
	{
		this.restingDanger = restingDanger;
	}

	public void setRestingEfficiency(RestingEfficiency restingEfficiency)
	{
		this.restingEfficiency = restingEfficiency;
	}

	/**
	 * Returns the stealth modifier required by actors in this tile.
	 */
	public String getStealthModifierRequired()
	{
		TerrainType terrainType = this.getTerrainType();
		return terrainType.stealthModifier;
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

	public List<MazeEvent> addCondition(Condition c)
	{
		ConditionManager.getInstance().addCondition(this, c);
		Maze.getInstance().getUi().setTile(Maze.getInstance().getCurrentZone(), this, Maze.getInstance().getTile());
		return new ArrayList<MazeEvent>();
	}

	public void removeCondition(Condition c)
	{
		ConditionManager.getInstance().removeCondition(this, c);
	}

	public List<Condition> getConditions()
	{
		return ConditionManager.getInstance().getConditions(this);
	}
}
