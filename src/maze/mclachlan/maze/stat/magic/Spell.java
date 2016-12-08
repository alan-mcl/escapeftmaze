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

package mclachlan.maze.stat.magic;

import java.util.*;
import mclachlan.maze.game.Log;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeScript;
import mclachlan.maze.stat.GroupOfPossibilities;
import mclachlan.maze.stat.StatModifier;
import mclachlan.maze.stat.Stats;
import mclachlan.maze.stat.UnifiedActor;

/**
 *
 */
public class Spell
{
	/** spell name, eg Force Bolt */ 
	private String name;
	
	/** spell display name */
	private String displayName;
	
	/** costs to cast this spell */
	private Value hitPointCost;
	private Value actionPointCost;
	private Value magicPointCost;

	/** spell target type, eg FOE.  A constant from {@link MagicSys.SpellTargetType} */
	private int targetType;
	
	/** spell school, eg Evocation.  A constant from {@link MagicSys.SpellSchool} */ 
	private String school;
	
	/** spell book, eg Red Magic.  A constant from {@link MagicSys.SpellBook}  */
	private MagicSys.SpellBook book;
	
	/** spell effects */ 
	private GroupOfPossibilities<SpellEffect> effects;

	/** spell description */
	private String description;

	/** spell level */
	private int level;

	/** spell requirements to learn */
	private StatModifier requirementsToLearn;

	/** spell requirements to cast */
	private List<ManaRequirement> requirementsToCast;

	/** the script that happens when the player casts this spell, typically animations */
	private MazeScript castByPlayerScript;

	/** the script that happens when a foe casts this spell, typically animations */
	private MazeScript castByFoeScript;

	/** when this spell can be cast,  a constant from {@link MagicSys.SpellUsabilityType} */
	private int usabilityType;

	/** the primary modifier involved in casting this spell */
	private Stats.Modifier primaryModifier;

	/** the secondary modifier involved in casting this spell */
	private Stats.Modifier secondaryModifier;

	/** if non null, this is a wild magic spell */
	private Value wildMagicValue;

	/** a table of spell names keyed on the wild magic value */
	private String[] wildMagicTable;

	/*-------------------------------------------------------------------------*/
	public Spell(
		String name,
		String displayName,
		Value hitPointCost,
		Value actionPointCost,
		Value magicPointCost,
		String description,
		int level,
		int targetType,
		int usabilityType,
		String school,
		MagicSys.SpellBook book,
		GroupOfPossibilities<SpellEffect> effects,
		List<ManaRequirement> requirementsToCast,
		StatModifier requirementsToLearn,
		MazeScript castingScript,
		MazeScript castByFoeScript,
		Stats.Modifier primaryModifier,
		Stats.Modifier secondaryModifier,
		Value wildMagicValue,
		String[] wildMagicTable
	)
	{
		this.displayName = displayName;
		this.hitPointCost = hitPointCost;
		this.actionPointCost = actionPointCost;
		this.magicPointCost = magicPointCost;
		this.description = description;
		this.level = level;
		this.name = name;
		this.targetType = targetType;
		this.school = school;
		this.book = book;
		this.effects = effects;
		this.requirementsToCast = requirementsToCast;
		this.requirementsToLearn = requirementsToLearn;
		this.castByPlayerScript = castingScript;
		this.castByFoeScript = castByFoeScript;
		this.usabilityType = usabilityType;
		this.primaryModifier = primaryModifier;
		this.secondaryModifier = secondaryModifier;
		this.wildMagicValue = wildMagicValue;
		this.wildMagicTable = wildMagicTable;
	}

	/*-------------------------------------------------------------------------*/
	public int getLevel()
	{
		return level;
	}

	public String getName()
	{
		return name;
	}

	public Value getHitPointCost()
	{
		return hitPointCost;
	}

	public void setHitPointCost(Value hitPointCost)
	{
		this.hitPointCost = hitPointCost;
	}

	public Value getActionPointCost()
	{
		return actionPointCost;
	}

	public void setActionPointCost(Value actionPointCost)
	{
		this.actionPointCost = actionPointCost;
	}

	public Value getMagicPointCost()
	{
		return magicPointCost;
	}

	public void setMagicPointCost(Value magicPointCost)
	{
		this.magicPointCost = magicPointCost;
	}

	public MagicSys.SpellBook getBook()
	{
		return book;
	}

	public GroupOfPossibilities<SpellEffect> getEffects()
	{
		return effects;
	}

	public String getSchool()
	{
		return school;
	}

	public String getDisplayName()
	{
		return displayName;
	}

	/** A constant from {@link MagicSys.SpellTargetType} */
	public int getTargetType()
	{
		return targetType;
	}

	public String getDescription()
	{
		return description;
	}

	public List<ManaRequirement> getRequirementsToCast()
	{
		return requirementsToCast;
	}

	public MazeScript getCastByPlayerScript()
	{
		return castByPlayerScript;
	}

	public MazeScript getCastByFoeScript()
	{
		return castByFoeScript;
	}

	/**
	 * @return
	 * when this spell can be cast,  a constant from {@link MagicSys.SpellUsabilityType}
	 */
	public int getUsabilityType()
	{
		return usabilityType;
	}

	public Stats.Modifier getPrimaryModifier()
	{
		return primaryModifier;
	}

	public StatModifier getRequirementsToLearn()
	{
		return requirementsToLearn;
	}

	public Stats.Modifier getSecondaryModifier()
	{
		return secondaryModifier;
	}

	public Value getWildMagicValue()
	{
		return wildMagicValue;
	}

	public String[] getWildMagicTable()
	{
		return wildMagicTable;
	}

	public void setBook(MagicSys.SpellBook book)
	{
		this.book = book;
	}

	public void setCastByFoeScript(MazeScript castByFoeScript)
	{
		this.castByFoeScript = castByFoeScript;
	}

	public void setCastByPlayerScript(MazeScript castByPlayerScript)
	{
		this.castByPlayerScript = castByPlayerScript;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public void setEffects(GroupOfPossibilities<SpellEffect> effects)
	{
		this.effects = effects;
	}

	public void setLevel(int level)
	{
		this.level = level;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public void setPrimaryModifier(Stats.Modifier primaryModifier)
	{
		this.primaryModifier = primaryModifier;
	}

	public void setRequirementsToCast(List<ManaRequirement> requirementsToCast)
	{
		this.requirementsToCast = requirementsToCast;
	}

	public void setRequirementsToLearn(StatModifier requirementsToLearn)
	{
		this.requirementsToLearn = requirementsToLearn;
	}

	public void setSchool(String school)
	{
		this.school = school;
	}

	public void setSecondaryModifier(Stats.Modifier secondaryModifier)
	{
		this.secondaryModifier = secondaryModifier;
	}

	public void setDisplayName(String displayName)
	{
		this.displayName = displayName;
	}

	public void setTargetType(int targetType)
	{
		this.targetType = targetType;
	}

	public void setUsabilityType(int usabilityType)
	{
		this.usabilityType = usabilityType;
	}

	public void setWildMagicValue(Value wildMagicValue)
	{
		this.wildMagicValue = wildMagicValue;
	}

	public void setWildMagicTable(String[] wildMagicTable)
	{
		this.wildMagicTable = wildMagicTable;
	}

	/*-------------------------------------------------------------------------*/
	public String toString()
	{
		return name+" ("+level+")";
	}

	/*-------------------------------------------------------------------------*/
	public boolean meetsRequirements(UnifiedActor actor)
	{
		// must meet all mana requirements
		if (requirementsToCast != null)
		{
			for (ManaRequirement m : requirementsToCast)
			{
				if (!meetsRequirement(m, actor))
				{
					Maze.log(Log.DEBUG, this.getName() + ": failed mana present requirement: " + m);
					return false;
				}
			}
		}

		// must meet all spell effect requirements
		for (SpellEffect se : this.getEffects().getPossibilities())
		{
			if (!se.meetsRequirements(actor))
			{
				Maze.log(Log.DEBUG, this.getName() + ": failed spell effect requirement: " + se.getName());
				return false;
			}
		}

		return true;
	}

	/*-------------------------------------------------------------------------*/
	private boolean meetsRequirement(ManaRequirement m, UnifiedActor actor)
	{
		if (actor == null)
		{
			return true;
		}
		
		int present = actor.getAmountMagicPresent(m.colour);
		return present >= m.amount;
	}
}
