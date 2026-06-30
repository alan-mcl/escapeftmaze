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

package mclachlan.maze.test.support;

import java.util.*;
import mclachlan.maze.stat.*;
import mclachlan.maze.stat.magic.Spell;
import mclachlan.maze.stat.npc.NpcScript;

/**
 * A deliberately lightweight {@link UnifiedActor} for pure-logic tests.
 * <p>
 * It overrides only the handful of accessors that {@code Value}/{@code ValueList}
 * computations depend on ({@link #getLevel()}, {@link #getLevel(String)},
 * {@link #getModifier(Stats.Modifier)} and {@link #getActorGroup()}) so that
 * those tests are isolated from the full actor machinery (equipment, conditions,
 * carrying capacity, banners, etc.). All other abstract members return inert
 * defaults.
 */
public class StubActor extends UnifiedActor
{
	private final String name;
	private int level;
	private final Map<String, Integer> classLevels = new HashMap<>();
	private final StatModifier modifiers = new StatModifier();
	private ActorGroup actorGroup;

	/*-------------------------------------------------------------------------*/
	public StubActor(String name)
	{
		this.name = name;
	}

	/*-------------------------------------------------------------------------*/
	public StubActor withLevel(int level)
	{
		this.level = level;
		return this;
	}

	public StubActor withClassLevel(String className, int level)
	{
		this.classLevels.put(className, level);
		return this;
	}

	public StubActor withModifier(Stats.Modifier modifier, int value)
	{
		this.modifiers.setModifier(modifier, value);
		return this;
	}

	public StubActor withActorGroup(ActorGroup group)
	{
		this.actorGroup = group;
		return this;
	}

	/*-------------------------------------------------------------------------*/
	// the bits Value/ValueList actually read

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public int getLevel()
	{
		return level;
	}

	@Override
	public int getLevel(String className)
	{
		return classLevels.getOrDefault(className, 0);
	}

	@Override
	public int getModifier(Stats.Modifier modifier)
	{
		return modifiers.getModifier(modifier);
	}

	@Override
	public ActorGroup getActorGroup()
	{
		return actorGroup;
	}

	/*-------------------------------------------------------------------------*/
	// inert implementations of the remaining abstract surface

	@Override
	public void inventoryItemAdded(Item item)
	{
	}

	@Override
	public NpcScript getActionScript()
	{
		return null;
	}

	@Override
	public void removeLevelAbility(Spell spell)
	{
	}

	@Override
	public List<Item> getStealableItems()
	{
		return new ArrayList<>();
	}

	@Override
	public List<AttackWith> getAttackWithOptions()
	{
		return new ArrayList<>();
	}

	@Override
	public List<TypeDescriptor> getTypes()
	{
		return new ArrayList<>();
	}

	@Override
	public int getBaseModifier(Stats.Modifier modifier)
	{
		return modifiers.getModifier(modifier);
	}

	@Override
	public String getDisplayName()
	{
		return name;
	}

	@Override
	public String getDisplayNamePlural()
	{
		return name;
	}

	@Override
	public void removeCurse(int strength)
	{
	}

	@Override
	public void addAllies(List<FoeGroup> foeGroups)
	{
	}

	@Override
	public boolean isActiveModifier(Stats.Modifier modifier)
	{
		return false;
	}

	@Override
	public CharacterClass.Focus getFocus()
	{
		return null;
	}

	@Override
	public String getFaction()
	{
		return null;
	}
}
