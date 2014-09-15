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

import java.util.*;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.game.MazeVariables;

/**
 *
 */
public class QuestManager
{
	Npc npc;
	List<Quest> quests;

	private String counterVar, stateVar;

	/*-------------------------------------------------------------------------*/
	public QuestManager(Npc npc)
	{
		this.npc = npc;
		this.quests = new ArrayList<Quest>();

		String npcVarName = npc.getName().toLowerCase().replaceAll(" ", ".");

		counterVar = npcVarName +".quest.manager.counter";
		stateVar = npcVarName +".quest.manager.state";
	}

	/*-------------------------------------------------------------------------*/
	public void addQuest(Quest q)
	{
		this.quests.add(q);
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> getNextQuestRelatedStuff()
	{
		List<MazeEvent> result = new ArrayList<MazeEvent>();

		if (getQuestState().equals(State.PRIMED))
		{
			if (getQuestCounter() == -1)
			{
				acceptNextQuest();
			}
			Quest currentQuest = getCurrentQuest();

			if (currentQuest != null)
			{
				result.addAll(currentQuest.getIntroduction());
				setQuestState(State.GRANTED);
			}
		}
		else if (getQuestState().equals(State.GRANTED))
		{
			Quest currentQuest = getCurrentQuest();

			if (currentQuest.isComplete())
			{
				setQuestState(State.COMPLETED);
				result.addAll(currentQuest.getReward());
				acceptNextQuest();
				setQuestState(State.PRIMED);
			}
			else
			{
				result.addAll(currentQuest.getEncouragement());
			}
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public boolean hasNewQuestAvailable()
	{
		return getQuestState().equals(State.PRIMED);
	}

	/*-------------------------------------------------------------------------*/
	public boolean isCurrentQuestCompleted()
	{
		return getCurrentQuest() != null && getCurrentQuest().isComplete();
	}

	/*-------------------------------------------------------------------------*/
	public boolean isCurrentQuestRewarded()
	{
		return getCurrentQuest() != null && getCurrentQuest().isRewarded();
	}

	/*-------------------------------------------------------------------------*/
	private Quest getCurrentQuest()
	{
		int index = getQuestCounter();
		if (index > quests.size()-1 || index < 0)
		{
			return null;
		}
		else
		{
			return quests.get(index);
		}
	}

	/*-------------------------------------------------------------------------*/
	private void acceptNextQuest()
	{
		setQuestCounter(getQuestCounter() + 1);
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Start game.
	 */
	public void start()
	{
		setQuestCounter(0);
		setQuestState(State.PRIMED);
	}

	/*-------------------------------------------------------------------------*/
	private void setQuestState(String state)
	{
		MazeVariables.set(stateVar, state);
	}

	/*-------------------------------------------------------------------------*/
	private String getQuestState()
	{
		String result = MazeVariables.get(stateVar);

		if (result == null)
		{
			result = State.PRIMED;
			setQuestState(result);
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	private void setQuestCounter(int index)
	{
		MazeVariables.set(counterVar, Integer.toString(index));
	}

	/*-------------------------------------------------------------------------*/
	private int getQuestCounter()
	{
		if (MazeVariables.get(counterVar) == null)
		{
			MazeVariables.set(counterVar, "-1");
		}
		return MazeVariables.getInt(counterVar);
	}

	/*-------------------------------------------------------------------------*/
	private static class State
	{
		public static final String PRIMED = "primed";
		public static final String GRANTED = "granted";
		public static final String COMPLETED = "completed";
		public static final String REWARDED = "rewarded";
	}
}
