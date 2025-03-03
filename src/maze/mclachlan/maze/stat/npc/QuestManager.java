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
import mclachlan.maze.game.journal.JournalManager;
import mclachlan.maze.util.MazeException;

/**
 *
 */
public class QuestManager
{
	private final List<Quest> quests;

	private final String counterVar, stateVar;

	/*-------------------------------------------------------------------------*/
	public QuestManager(String name)
	{
		this.quests = new ArrayList<>();

		String varName = name.toLowerCase().replaceAll(" ", ".");

		counterVar = varName +".quest.manager.counter";
		stateVar = varName +".quest.manager.state";
	}

	/*-------------------------------------------------------------------------*/
	public void addQuest(Quest q)
	{
		this.quests.add(q);
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> getNextQuestRelatedStuff()
	{
		List<MazeEvent> result = new ArrayList<>();

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
				setQuestState(currentQuest, State.GRANTED);
			}
		}
		else if (getQuestState().equals(State.GRANTED))
		{
			Quest currentQuest = getCurrentQuest();

			if (currentQuest.isComplete())
			{
				setQuestState(currentQuest, State.COMPLETED);
				result.addAll(currentQuest.getReward());
				acceptNextQuest();
				setQuestState(null, State.PRIMED);
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
		setQuestState(null, State.PRIMED);
	}

	/*-------------------------------------------------------------------------*/
	private void setQuestState(Quest q, State state)
	{
		MazeVariables.set(stateVar, state.name());

		switch (state)
		{
			case PRIMED, REWARDED ->
			{
			}
			case GRANTED ->
			{
				if (q != null)
				{
					String name = q.getName();
					JournalManager.getInstance().questJournal(name, q.getDescription());
					JournalManager.getInstance().questJournal(name, "Quest added");
				}
			}
			case COMPLETED ->
			{
				if (q != null)
				{
					String name = q.getName();
					JournalManager.getInstance().questJournal(name, "Quest completed");
				}
			}
			default -> throw new MazeException("invalid "+state);
		}
	}

	/*-------------------------------------------------------------------------*/
	private State getQuestState()
	{
		if (MazeVariables.get(stateVar) == null)
		{
			setQuestState(null, State.PRIMED);
		}

		return State.valueOf(MazeVariables.get(stateVar));
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
	private static enum State
	{
		PRIMED,
		GRANTED,
		COMPLETED,
		REWARDED;
	}
}
