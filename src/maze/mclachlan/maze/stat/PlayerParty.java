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
import mclachlan.maze.stat.condition.CloudSpell;
import mclachlan.maze.util.MazeException;

/**
 *
 */
public class PlayerParty implements ActorGroup
{
	/** player characters in this party */
	private List<UnifiedActor> actors = new ArrayList<>();

	/**
	 * The index of the first character in the back row.
	 */
	private int formation;

	/**
	 * Party gold.
	 */
	private int gold;

	/**
	 * Units of supplies
	 */
	private int supplies;

	/**
	 * Cloud spells afflicting the party
	 */
	private final List<CloudSpell> cloudSpells = new ArrayList<>();

	/*-------------------------------------------------------------------------*/
	public PlayerParty(List<UnifiedActor> actors)
	{
		this(actors, 0, 0, 3);
	}

	/*-------------------------------------------------------------------------*/
	public PlayerParty(List<UnifiedActor> actors, int gold, int supplies,
		int formation)
	{
		this.actors = actors;
		this.gold = gold;
		this.supplies = supplies;
		this.formation = formation;
	}
	
	/*-------------------------------------------------------------------------*/
	public int numAlive()
	{
		int result = 0;

		for (UnifiedActor actor1 : actors)
		{
			PlayerCharacter actor = (PlayerCharacter)actor1;
			if (actor.getStats().getHitPoints().getCurrent() > 0)
			{
				result++;
			}
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public String getDescription()
	{
		return "Player Party";
	}

	/*-------------------------------------------------------------------------*/
	public List<UnifiedActor> getActors()
	{
		return actors;
	}

	/*-------------------------------------------------------------------------*/
	public List<String> getPartyNames()
	{
		List<String> result = new ArrayList<>();
		for (UnifiedActor a : actors)
		{
			result.add(a.getName());
		}
		return result;
	}

	/*-------------------------------------------------------------------------*/
	public void setActors(List<UnifiedActor> actors)
	{
		this.actors = actors;
	}

	/*-------------------------------------------------------------------------*/
	public List<PlayerCharacter> getPlayerCharacters()
	{
		ArrayList<PlayerCharacter> result = new ArrayList<>();

		for (UnifiedActor a : actors)
		{
			result.add((PlayerCharacter)a);
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public List<PlayerCharacter> getLivePlayerCharacters()
	{
		ArrayList<PlayerCharacter> result = new ArrayList<>();

		for (UnifiedActor a : actors)
		{
			if (a.isAlive())
			{
				result.add((PlayerCharacter)a);
			}
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public int getGold()
	{
		return gold;
	}

	/*-------------------------------------------------------------------------*/
	public void incGold(int value)
	{
		gold += value;
	}

	/*-------------------------------------------------------------------------*/
	public List<UnifiedActor> getActors(int engagementRange, int minRange, int maxRange)
	{
		//
		// MELEE engage:		front=MELEE, back=EXTENDED
		// EXTENDED engage:	front=EXTENDED, back=THROWN
		// THROWN engage:		front=back=THROWN
		// LONG engage:		front=back=LONG
		//

		List<UnifiedActor> frontRow = new ArrayList<>();
		List<UnifiedActor> backRow = new ArrayList<>();
		int max = actors.size();
		for (int i=0; i<max; i++)
		{
			if (i<formation)
			{
				frontRow.add(actors.get(i));
			}
			else
			{
				backRow.add(actors.get(i));
			}
		}

		if (minRange > engagementRange)
		{
			// engagement is closer than minimum range: no legal targets
			return null;
		}

		if (maxRange < engagementRange)
		{
			// engagement is outside maximum range: no legal targets
			return null;
		}

		List<UnifiedActor> result = new ArrayList<>(frontRow);

		switch (engagementRange)
		{
			case ItemTemplate.WeaponRange.MELEE ->
			{
				if (maxRange >= ItemTemplate.WeaponRange.EXTENDED)
				{
					result.addAll(backRow);
				}
			}
			case ItemTemplate.WeaponRange.EXTENDED ->
			{
				if (maxRange >= ItemTemplate.WeaponRange.THROWN)
				{
					result.addAll(backRow);
				}
			}
			case ItemTemplate.WeaponRange.THROWN, ItemTemplate.WeaponRange.LONG ->
				result.addAll(backRow);
			default ->
				throw new MazeException("Invalid range: " + engagementRange);
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public void addCloudSpell(CloudSpell cloudSpell)
	{
		this.cloudSpells.add(cloudSpell);
	}

	/*-------------------------------------------------------------------------*/
	public void removeCloudSpell(CloudSpell cloudSpell)
	{
		this.cloudSpells.remove(cloudSpell);
	}

	/*-------------------------------------------------------------------------*/
	public List<CloudSpell> getCloudSpells()
	{
		return this.cloudSpells;
	}

	@Override
	public int numActive()
	{
		int result = 0;

		for (UnifiedActor a : getActors())
		{
			if (GameSys.getInstance().isActorAlive(a) &&
				!GameSys.getInstance().isActorImmobile(a) &&
				!GameSys.getInstance().isActorBlinkedOut(a))
			{
				result ++;
			}
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public int getAverageLevel()
	{
		int result = 0;
		for (UnifiedActor a : actors)
		{
			result += a.getLevel();
		}

		return result/actors.size();
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public UnifiedActor getActorWithBestModifier(Stats.Modifier modifier)
	{
		return getActorWithBestModifier(modifier, null);
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public UnifiedActor getActorWithBestModifier(Stats.Modifier modifier,
		UnifiedActor excluded)
	{
		UnifiedActor result = null;
		int cur = Integer.MIN_VALUE;

		List<UnifiedActor> actors = new ArrayList<>(getActors());
		// shuffle to randomise ties
		Collections.shuffle(actors);

		for (UnifiedActor a : actors)
		{
			if (cur < a.getModifier(modifier) && a != excluded)
			{
				cur = a.getModifier(modifier);
				result = a;
			}
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public List<UnifiedActor> getActorsWithModifier(Stats.Modifier modifier)
	{
		List<UnifiedActor> result = new ArrayList<>();
		List<UnifiedActor> actors = new ArrayList<>(getActors());

		for (UnifiedActor a : actors)
		{
			if (a.getModifier(modifier) > 0)
			{
				result.add(a);
			}
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public int getBestModifier(Stats.Modifier modifier)
	{
		return getBestModifier(modifier, null);
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public int getBestModifier(Stats.Modifier modifier, UnifiedActor excluded)
	{
		UnifiedActor actor = getActorWithBestModifier(modifier, excluded);

		if (actor != null)
		{
			return actor.getModifier(modifier);
		}
		else
		{
			return 0;
		}
	}

	/*-------------------------------------------------------------------------*/
	public void clearCloudSpells()
	{
		this.cloudSpells.clear();
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @return
	 * 	true if the party contains a character of the given class (only
	 * 	current classes are considered)
	 */
	public boolean containsCharacterClass(String className)
	{
		for (UnifiedActor pc : actors)
		{
			if (((PlayerCharacter)pc).getCharacterClass().getName().equals(className))
			{
				return true;
			}
		}
		
		return false;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @return
	 * 	true if the given item is currently equipped by any of the characters.
	 */
	public boolean isItemEquipped(String itemName)
	{
		for (UnifiedActor actor : actors)
		{
			PlayerCharacter pc = (PlayerCharacter)actor;

			if (pc.hasItemEquipped(itemName))
			{
				return true;
			}
		}

		return false;
	}

	/*-------------------------------------------------------------------------*/
	public void removeItem(Item item)
	{
		for (UnifiedActor a : actors)
		{
			a.removeItem(item, true);
		}
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Grants the given amount of experience to each player character.
	 */
	public void grantExperience(int amount)
	{
		for (UnifiedActor a : actors)
		{
			PlayerCharacter pc = (PlayerCharacter)a;
			if (pc.getHitPoints().getCurrent() > 0)
			{
				pc.incExperience(amount);
			}
		}
	}

	/*-------------------------------------------------------------------------*/

	/**
	 * @return
	 * 	The index (started at 0) of the first character in the back row
	 */
	public int getFormation()
	{
		return this.formation;
	}

	/*-------------------------------------------------------------------------*/
	public void setGold(int gold)
	{
		this.gold = gold;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @param formation
	 * 	The index of the first character in the back row (party is 0 indexed)
	 */
	public void setFormation(int formation)
	{
		this.formation = formation;
	}

	/*-------------------------------------------------------------------------*/
	public int getSupplies()
	{
		return supplies;
	}

	/*-------------------------------------------------------------------------*/
	public void setSupplies(int supplies)
	{
		this.supplies = supplies;
	}

	/*-------------------------------------------------------------------------*/
	public void incSupplies(int amount)
	{
		this.supplies += amount;
	}

	/*-------------------------------------------------------------------------*/
	public PlayerCharacter getPlayerCharacter(int i)
	{
		return (PlayerCharacter)this.actors.get(i);
	}

	/*-------------------------------------------------------------------------*/
	public PlayerCharacter getRandomPlayerCharacter()
	{
		Dice d = new Dice(1, actors.size(), -1);
		return getPlayerCharacter(d.roll("PlayerParty.getRandomPlayerCharacter"));
	}

	/*-------------------------------------------------------------------------*/
	public PlayerCharacter getRandomPlayerCharacterForSpeech(String speechKey)
	{
		for (int i=0; i<6; i++)
		{
			PlayerCharacter pc = getRandomPlayerCharacter();

			String words = pc.getPersonality().getWords(speechKey);
			if (GameSys.getInstance().isActorAware(pc) &&
				words != null && words.length() > 0)
			{
				return pc;
			}
		}

		return null;
	}

	/*-------------------------------------------------------------------------*/
	public int size()
	{
		return this.actors.size();
	}

	/*-------------------------------------------------------------------------*/
	public boolean isFrontRow(UnifiedActor pc)
	{
		return actors.indexOf(pc) < formation;
	}

	/*-------------------------------------------------------------------------*/
	public int getPlayerCharacterIndex(PlayerCharacter pc)
	{
		return this.getActors().indexOf(pc);
	}

	/*-------------------------------------------------------------------------*/
	public void reorderPartyToCompensateForDeadCharacters()
	{
		for (int i=0; i<actors.size(); i++)
		{
			outer: if (actors.get(i).getHitPoints().getCurrent() <= 0)
			{
				for (int j=i+1; j<actors.size(); j++)
				{
					// only move the dead guy if there is a pc further back who
					// is still alive.
					if (actors.get(j).getHitPoints().getCurrent() > 0)
					{
						// remove character, place at the back, and move all the others up
						UnifiedActor temp = actors.remove(i);
						actors.add(temp);
						break outer;
					}
				}
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @return
	 * 	The effective party level: the highest level member determines this
	 */
	public int getPartyLevel()
	{
		int result = 0;

		for (UnifiedActor a : actors)
		{
			if (a.getLevel() > result)
			{
				result = a.getLevel();
			}
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @return
	 * 	total modifier of all the party
	 */
	public int getTotalModifier(Stats.Modifier modifier)
	{
		int result = 0;

		for (UnifiedActor a : getActors())
		{
			result += a.getModifier(modifier);
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/

	/**
	 * @return
	 * 	true if any party member has the given modifier > 0
	 */
	public boolean hasModifier(Stats.Modifier mod)
	{
		for (UnifiedActor actor : getActors())
		{
			if (actor.getModifier(mod) >0)
			{
				return true;
			}
		}

		return false;
	}

	@Override
	public String getDisplayName()
	{
		return "Player Party";
	}

	@Override
	public int getModifier(Stats.Modifier modifier)
	{
		return 0;
	}
}
