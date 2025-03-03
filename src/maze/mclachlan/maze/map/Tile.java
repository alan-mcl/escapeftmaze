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

import java.awt.Point;
import java.util.*;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.stat.StatModifier;
import mclachlan.maze.stat.Stats;
import mclachlan.maze.stat.condition.Condition;
import mclachlan.maze.stat.condition.ConditionBearer;
import mclachlan.maze.stat.condition.ConditionManager;

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

	/** Sector name of this tile within the zone, may be null.
	 * Used to partition the auto-map.*/
	private String sector;

	/** X,Y coords of this tile */
	private Point coords;

	/** Danger of resting on this tile*/
	private RestingDanger restingDanger;

	/** Efficiency of resting on this tile*/
	private RestingEfficiency restingEfficiency;

	private static final Set<Stats.Modifier> magicModifiers;
	public static final int MAX_TILE_MAGIC = 13;

	/*-------------------------------------------------------------------------*/
	static
	{
		magicModifiers = new HashSet<>();
		magicModifiers.add(Stats.Modifier.BLACK_MAGIC_GEN);
		magicModifiers.add(Stats.Modifier.BLUE_MAGIC_GEN);
		magicModifiers.add(Stats.Modifier.RED_MAGIC_GEN);
		magicModifiers.add(Stats.Modifier.WHITE_MAGIC_GEN);
		magicModifiers.add(Stats.Modifier.GREEN_MAGIC_GEN);
		magicModifiers.add(Stats.Modifier.PURPLE_MAGIC_GEN);
		magicModifiers.add(Stats.Modifier.GOLD_MAGIC_GEN);
	}

	/*-------------------------------------------------------------------------*/
	public enum TerrainType
	{
		FAKE(null),
		URBAN(Stats.Modifier.STREETWISE),
		DUNGEON(Stats.Modifier.DUNGEONEER),
		WILDERNESS(Stats.Modifier.WILDERNESS_LORE),
		WASTELAND(Stats.Modifier.SURVIVAL);

		private final Stats.Modifier stealthModifier;

		TerrainType(Stats.Modifier stealthModifier)
		{
			this.stealthModifier = stealthModifier;
		}
	}

	/*-------------------------------------------------------------------------*/
	public enum RestingDanger
	{
		NONE, LOW, MEDIUM, HIGH, EXTREME
	}

	/*-------------------------------------------------------------------------*/
	public enum RestingEfficiency
	{
		POOR, AVERAGE, GOOD, EXCELLENT
	}

	public Tile()
	{
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
	public int getModifier(Stats.Modifier modifier)
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
		return getModifier(Stats.Modifier.BLACK_MAGIC_GEN);
	}

	public int getAmountBlueMagic()
	{
		return getModifier(Stats.Modifier.BLUE_MAGIC_GEN);
	}

	public int getAmountGoldMagic()
	{
		return getModifier(Stats.Modifier.GOLD_MAGIC_GEN);
	}

	public int getAmountGreenMagic()
	{
		return getModifier(Stats.Modifier.GREEN_MAGIC_GEN);
	}

	public int getAmountPurpleMagic()
	{
		return getModifier(Stats.Modifier.PURPLE_MAGIC_GEN);
	}

	public int getAmountRedMagic()
	{
		return getModifier(Stats.Modifier.RED_MAGIC_GEN);
	}

	public int getAmountWhiteMagic()
	{
		return getModifier(Stats.Modifier.WHITE_MAGIC_GEN);
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
	public Stats.Modifier getStealthModifierRequired()
	{
		TerrainType terrainType = this.getTerrainType();
		return terrainType.stealthModifier;
	}

	public String getSector()
	{
		return sector;
	}

	public void setSector(String sector)
	{
		this.sector = sector;
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
		return new ArrayList<>();
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
}
