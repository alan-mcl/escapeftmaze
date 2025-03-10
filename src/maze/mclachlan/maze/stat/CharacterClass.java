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

import java.util.*;
import mclachlan.maze.data.v1.DataObject;


/**
 *
 */
public class CharacterClass extends DataObject implements TypeDescriptor
{
	/** name of this character class */
	private String name;
	/** wordy description of this character class */
	private String description;

	/** The focus of this class */
	private Focus focus;

	/** The starting hit points of this class.  This will be modified by race */
	private int startingHitPoints;
	/** The starting action points of this class.  This will be modified by race */
	private int startingActionPoints;
	/** The starting magic points of this class.  This will be modified by race */
	private int startingMagicPoints;

	/** once off bonus applied to a new character of this class */
	private StatModifier startingModifiers;
	/** when modifiers become unlockable */
	private StatModifier unlockModifiers;

	/** The set of modifiers that start as active for a character of this class */
	private StatModifier startingActiveModifiers;

	/** The allowed genders who can take this class.  NULL means all */
	private Set<String> allowedGenders;

	/** The allowed races who can take this class NULL means all */
	private Set<String> allowedRaces;

	/** The experience table for this class */
	private ExperienceTable experienceTable;

	/** hit points granted at level up */
	private Dice levelUpHitPoints;
	/** action points granted at level up */
	private Dice levelUpActionPoints;
	/** magic points granted at level up */
	private Dice levelUpMagicPoints;
	/** modifiers points the player can assign at level up */
	private int levelUpAssignableModifiers;
	/** modifiers applied to the character every level up */
	private StatModifier levelUpModifiers;

	/** ability progression for this class */
	private LevelAbilityProgression progression;

	public CharacterClass()
	{
	}

	/*-------------------------------------------------------------------------*/
	public enum Focus
	{
		COMBAT(0),
		STEALTH(1),
		MAGIC(2);

		int sortOrder;

		Focus(int sortOrder)
		{
			this.sortOrder = sortOrder;
		}

		public int getSortOrder()
		{
			return sortOrder;
		}
	}

	/*-------------------------------------------------------------------------*/
	public CharacterClass(
		String name,
		Focus focus,
		String desc,
		int startingHitPoints,
		int startingActionPoints,
		int startingMagicPoints,
		StatModifier startingActiveModifiers,
		StatModifier startingModifiers,
		StatModifier unlockModifiers,
		Set<String> allowedGenders,
		Set<String> allowedRaces,
		ExperienceTable experienceTable,
		Dice levelUpHitPoints,
		Dice levelUpActionPoints,
		Dice levelUpMagicPoints,
		int levelUpAssignableModifiers,
		StatModifier levelUpModifiers,
		LevelAbilityProgression progression)
	{
		this.name = name;
		this.focus = focus;
		this.description = desc;
		this.startingHitPoints = startingHitPoints;
		this.startingActionPoints = startingActionPoints;
		this.startingActiveModifiers = startingActiveModifiers;
		this.startingMagicPoints = startingMagicPoints;
		this.startingModifiers = startingModifiers;
		this.unlockModifiers = unlockModifiers;
		this.allowedGenders = allowedGenders;
		this.allowedRaces = allowedRaces;
		this.experienceTable = experienceTable;
		this.levelUpHitPoints = levelUpHitPoints;
		this.levelUpActionPoints = levelUpActionPoints;
		this.levelUpMagicPoints = levelUpMagicPoints;
		this.levelUpAssignableModifiers = levelUpAssignableModifiers;
		this.levelUpModifiers = levelUpModifiers;
		this.progression = progression;
//		progression.setCharacterClass(this);
	}

	/*-------------------------------------------------------------------------*/
	public String getName()
	{
		return name;
	}

	@Override
	public Stats.Modifier getFavouredEnemyModifier()
	{
		return null;
	}

	/*-------------------------------------------------------------------------*/
	public Focus getFocus()
	{
		return focus;
	}

	/*-------------------------------------------------------------------------*/
	public String getDescription()
	{
		return description;
	}

	/*-------------------------------------------------------------------------*/
	public boolean isAllowedGender(Gender gender)
	{
		return allowedGenders == null || allowedGenders.contains(gender.getName());
	}

	/*-------------------------------------------------------------------------*/
	public boolean isAllowedRace(Race race)
	{
		return allowedRaces == null || allowedRaces.contains(race.getName());
	}

	/*-------------------------------------------------------------------------*/
	public StatModifier getStartingModifiers()
	{
		return startingModifiers;
	}

	public ExperienceTable getExperienceTable()
	{
		return experienceTable;
	}

	public Dice getLevelUpHitPoints()
	{
		return levelUpHitPoints;
	}

	public Dice getLevelUpActionPoints()
	{
		return levelUpActionPoints;
	}

	public Dice getLevelUpMagicPoints()
	{
		return levelUpMagicPoints;
	}

	public int getLevelUpAssignableModifiers()
	{
		return levelUpAssignableModifiers;
	}

	public StatModifier getLevelUpModifiers()
	{
		return levelUpModifiers;
	}

	public StatModifier getStartingActiveModifiers()
	{
		return startingActiveModifiers;
	}

	public Set<String> getAllowedGenders()
	{
		return allowedGenders;
	}

	public Set<String> getAllowedRaces()
	{
		return allowedRaces;
	}

	public int getStartingHitPoints()
	{
		return startingHitPoints;
	}

	public int getStartingMagicPoints()
	{
		return startingMagicPoints;
	}

	public int getStartingActionPoints()
	{
		return startingActionPoints;
	}

	public StatModifier getUnlockModifiers()
	{
		return unlockModifiers;
	}

	public LevelAbilityProgression getProgression() { return progression; }

	/*-------------------------------------------------------------------------*/
	public void setAllowedGenders(Set<String> allowedGenders)
	{
		this.allowedGenders = allowedGenders;
	}

	public void setAllowedRaces(Set<String> allowedRaces)
	{
		this.allowedRaces = allowedRaces;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public void setExperienceTable(ExperienceTable experienceTable)
	{
		this.experienceTable = experienceTable;
	}

	public void setFocus(Focus focus)
	{
		this.focus = focus;
	}

	public void setLevelUpAssignableModifiers(int levelUpAssignableModifiers)
	{
		this.levelUpAssignableModifiers = levelUpAssignableModifiers;
	}

	public void setLevelUpHitPoints(Dice levelUpHitPoints)
	{
		this.levelUpHitPoints = levelUpHitPoints;
	}

	public void setLevelUpMagicPoints(Dice levelUpMagicPoints)
	{
		this.levelUpMagicPoints = levelUpMagicPoints;
	}

	public void setLevelUpModifiers(StatModifier levelUpModifiers)
	{
		this.levelUpModifiers = levelUpModifiers;
	}

	public void setLevelUpActionPoints(Dice levelUpActionPoints)
	{
		this.levelUpActionPoints = levelUpActionPoints;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public void setStartingActiveModifiers(StatModifier startingActiveModifiers)
	{
		this.startingActiveModifiers = startingActiveModifiers;
	}

	public void setStartingHitPoints(int startingHitPoints)
	{
		this.startingHitPoints = startingHitPoints;
	}

	public void setStartingMagicPoints(int startingMagicPoints)
	{
		this.startingMagicPoints = startingMagicPoints;
	}

	public void setStartingModifiers(StatModifier startingModifiers)
	{
		this.startingModifiers = startingModifiers;
	}

	public void setStartingActionPoints(int startingActionPoints)
	{
		this.startingActionPoints = startingActionPoints;
	}

	public void setUnlockModifiers(StatModifier unlockModifiers)
	{
		this.unlockModifiers = unlockModifiers;
	}

	public void setProgression(LevelAbilityProgression progression)
	{
		this.progression = progression;
	}

	/*-------------------------------------------------------------------------*/

	@Override
	public String toString()
	{
		return "CharacterClass{" +
			"name='" + name + '\'' +
			", description='" + description + '\'' +
			", focus=" + focus +
			", startingHitPoints=" + startingHitPoints +
			", startingActionPoints=" + startingActionPoints +
			", startingMagicPoints=" + startingMagicPoints +
			", startingModifiers=" + startingModifiers +
			", unlockModifiers=" + unlockModifiers +
			", startingActiveModifiers=" + startingActiveModifiers +
			", allowedGenders=" + allowedGenders +
			", allowedRaces=" + allowedRaces +
			", experienceTable=" + experienceTable +
			", levelUpHitPoints=" + levelUpHitPoints +
			", levelUpActionPoints=" + levelUpActionPoints +
			", levelUpMagicPoints=" + levelUpMagicPoints +
			", levelUpAssignableModifiers=" + levelUpAssignableModifiers +
			", levelUpModifiers=" + levelUpModifiers +
			", progression=" + progression +
			"} " + super.toString();
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
		{
			return true;
		}
		if (o == null || getClass() != o.getClass())
		{
			return false;
		}

		CharacterClass that = (CharacterClass)o;

		if (getStartingHitPoints() != that.getStartingHitPoints())
		{
			return false;
		}
		if (getStartingActionPoints() != that.getStartingActionPoints())
		{
			return false;
		}
		if (getStartingMagicPoints() != that.getStartingMagicPoints())
		{
			return false;
		}
		if (getLevelUpAssignableModifiers() != that.getLevelUpAssignableModifiers())
		{
			return false;
		}
		if (!getName().equals(that.getName()))
		{
			return false;
		}
		if (getDescription() != null ? !getDescription().equals(that.getDescription()) : that.getDescription() != null)
		{
			return false;
		}
		if (getFocus() != that.getFocus())
		{
			return false;
		}
		if (getStartingModifiers() != null ? !getStartingModifiers().equals(that.getStartingModifiers()) : that.getStartingModifiers() != null)
		{
			return false;
		}
		if (getUnlockModifiers() != null ? !getUnlockModifiers().equals(that.getUnlockModifiers()) : that.getUnlockModifiers() != null)
		{
			return false;
		}
		if (getStartingActiveModifiers() != null ? !getStartingActiveModifiers().equals(that.getStartingActiveModifiers()) : that.getStartingActiveModifiers() != null)
		{
			return false;
		}
		if (getAllowedGenders() != null ? !getAllowedGenders().equals(that.getAllowedGenders()) : that.getAllowedGenders() != null)
		{
			return false;
		}
		if (getAllowedRaces() != null ? !getAllowedRaces().equals(that.getAllowedRaces()) : that.getAllowedRaces() != null)
		{
			return false;
		}
		if (getExperienceTable() != null ? !getExperienceTable().equals(that.getExperienceTable()) : that.getExperienceTable() != null)
		{
			return false;
		}
		if (getLevelUpHitPoints() != null ? !getLevelUpHitPoints().equals(that.getLevelUpHitPoints()) : that.getLevelUpHitPoints() != null)
		{
			return false;
		}
		if (getLevelUpActionPoints() != null ? !getLevelUpActionPoints().equals(that.getLevelUpActionPoints()) : that.getLevelUpActionPoints() != null)
		{
			return false;
		}
		if (getLevelUpMagicPoints() != null ? !getLevelUpMagicPoints().equals(that.getLevelUpMagicPoints()) : that.getLevelUpMagicPoints() != null)
		{
			return false;
		}
		if (getLevelUpModifiers() != null ? !getLevelUpModifiers().equals(that.getLevelUpModifiers()) : that.getLevelUpModifiers() != null)
		{
			return false;
		}
		return getProgression() != null ? getProgression().equals(that.getProgression()) : that.getProgression() == null;
	}

	@Override
	public int hashCode()
	{
		int result = getName().hashCode();
		result = 31 * result + (getDescription() != null ? getDescription().hashCode() : 0);
		result = 31 * result + (getFocus() != null ? getFocus().hashCode() : 0);
		result = 31 * result + getStartingHitPoints();
		result = 31 * result + getStartingActionPoints();
		result = 31 * result + getStartingMagicPoints();
		result = 31 * result + (getStartingModifiers() != null ? getStartingModifiers().hashCode() : 0);
		result = 31 * result + (getUnlockModifiers() != null ? getUnlockModifiers().hashCode() : 0);
		result = 31 * result + (getStartingActiveModifiers() != null ? getStartingActiveModifiers().hashCode() : 0);
		result = 31 * result + (getAllowedGenders() != null ? getAllowedGenders().hashCode() : 0);
		result = 31 * result + (getAllowedRaces() != null ? getAllowedRaces().hashCode() : 0);
		result = 31 * result + (getExperienceTable() != null ? getExperienceTable().hashCode() : 0);
		result = 31 * result + (getLevelUpHitPoints() != null ? getLevelUpHitPoints().hashCode() : 0);
		result = 31 * result + (getLevelUpActionPoints() != null ? getLevelUpActionPoints().hashCode() : 0);
		result = 31 * result + (getLevelUpMagicPoints() != null ? getLevelUpMagicPoints().hashCode() : 0);
		result = 31 * result + getLevelUpAssignableModifiers();
		result = 31 * result + (getLevelUpModifiers() != null ? getLevelUpModifiers().hashCode() : 0);
		result = 31 * result + (getProgression() != null ? getProgression().hashCode() : 0);
		return result;
	}
}
