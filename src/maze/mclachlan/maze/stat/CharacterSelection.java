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
import java.util.function.Consumer;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.game.event.RunCharacterSelectionEvent;

/**
 * Ordered, sequential party character selection. Exclusion methods run first
 * over the full party; inclusion methods accumulate picks in list order.
 */
public class CharacterSelection
{
	private List<CharacterSelectionMethod> methods = new ArrayList<>();
	private List<CharacterSelectionMethod> exclusions = new ArrayList<>();

	public CharacterSelection()
	{
	}

	public CharacterSelection(
		List<CharacterSelectionMethod> methods,
		List<CharacterSelectionMethod> exclusions)
	{
		if (methods != null)
		{
			this.methods = new ArrayList<>(methods);
		}
		if (exclusions != null)
		{
			this.exclusions = new ArrayList<>(exclusions);
		}
	}

	public List<CharacterSelectionMethod> getMethods()
	{
		return methods;
	}

	public void setMethods(List<CharacterSelectionMethod> methods)
	{
		this.methods = methods == null ? new ArrayList<>() : new ArrayList<>(methods);
	}

	public List<CharacterSelectionMethod> getExclusions()
	{
		return exclusions;
	}

	public void setExclusions(List<CharacterSelectionMethod> exclusions)
	{
		this.exclusions = exclusions == null ? new ArrayList<>() : new ArrayList<>(exclusions);
	}

	/*-------------------------------------------------------------------------*/
	public List<PlayerCharacter> getExcluded(List<PlayerCharacter> party)
	{
		if (party == null || party.isEmpty() || exclusions == null || exclusions.isEmpty())
		{
			return List.of();
		}

		Set<PlayerCharacter> excluded = new LinkedHashSet<>();
		List<PlayerCharacter> fullParty = new ArrayList<>(party);
		for (CharacterSelectionMethod method : exclusions)
		{
			if (method == null || method.isPlayerSelection())
			{
				continue;
			}
			excluded.addAll(method.select(fullParty));
		}
		return new ArrayList<>(excluded);
	}

	/*-------------------------------------------------------------------------*/
	public boolean requiresPlayerSelection()
	{
		if (methods == null)
		{
			return false;
		}
		for (CharacterSelectionMethod method : methods)
		{
			if (method != null && method.isPlayerSelection())
			{
				return true;
			}
		}
		return false;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Starts resolving this selection against {@code party}. Returns runtime
	 * events (Who steps) as needed; invokes {@code onSelected} when complete.
	 */
	public List<MazeEvent> select(
		List<PlayerCharacter> party,
		Consumer<List<PlayerCharacter>> onSelected)
	{
		return List.of(new RunCharacterSelectionEvent(this, 0, new ArrayList<>(), onSelected));
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Resolves automatic inclusion methods synchronously (no Who steps).
	 */
	public List<PlayerCharacter> selectAutomatic(List<PlayerCharacter> party)
	{
		if (party == null || party.isEmpty() || methods == null || methods.isEmpty())
		{
			return List.of();
		}

		List<PlayerCharacter> excluded = getExcluded(party);
		Set<PlayerCharacter> selected = new LinkedHashSet<>();
		List<PlayerCharacter> inclusionPool = poolMinus(party, excluded);

		for (CharacterSelectionMethod method : methods)
		{
			if (method == null || method.isPlayerSelection())
			{
				continue;
			}

			List<PlayerCharacter> candidates = candidatesFor(method, party, excluded, selected);
			selected.addAll(method.select(candidates));
		}

		return new ArrayList<>(selected);
	}

	/*-------------------------------------------------------------------------*/
	public static List<PlayerCharacter> candidatesFor(
		CharacterSelectionMethod method,
		List<PlayerCharacter> party,
		List<PlayerCharacter> excluded,
		Set<PlayerCharacter> alreadySelected)
	{
		if (method instanceof ModifierComparisonSelection)
		{
			return poolMinus(party, excluded);
		}
		return poolMinus(poolMinus(party, excluded), alreadySelected);
	}

	/*-------------------------------------------------------------------------*/
	static List<PlayerCharacter> poolMinus(
		List<PlayerCharacter> party,
		Collection<PlayerCharacter> remove)
	{
		if (party == null || party.isEmpty())
		{
			return List.of();
		}
		if (remove == null || remove.isEmpty())
		{
			return new ArrayList<>(party);
		}

		Set<PlayerCharacter> removeSet = new HashSet<>(remove);
		List<PlayerCharacter> result = new ArrayList<>();
		for (PlayerCharacter pc : party)
		{
			if (!removeSet.contains(pc))
			{
				result.add(pc);
			}
		}
		return result;
	}

	/*-------------------------------------------------------------------------*/
	public static List<PlayerCharacter> resolveSelectedNames(
		List<String> selectedNames,
		List<PlayerCharacter> party)
	{
		if (selectedNames == null || selectedNames.isEmpty())
		{
			return new ArrayList<>();
		}

		Map<String, PlayerCharacter> byName = new HashMap<>();
		for (PlayerCharacter pc : party)
		{
			byName.put(pc.getName(), pc);
		}

		List<PlayerCharacter> result = new ArrayList<>();
		for (String name : selectedNames)
		{
			PlayerCharacter pc = byName.get(name);
			if (pc != null)
			{
				result.add(pc);
			}
		}
		return result;
	}

	/*-------------------------------------------------------------------------*/
	public String describe()
	{
		int methodCount = methods == null ? 0 : methods.size();
		int exclusionCount = exclusions == null ? 0 : exclusions.size();
		return methodCount + " method(s), " + exclusionCount + " exclusion(s)";
	}
}
