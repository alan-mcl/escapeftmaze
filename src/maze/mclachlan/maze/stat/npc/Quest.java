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
public class Quest
{
	private String name, description;
	private String completionVar, rewardedVar;
	private List<MazeEvent> introduction, reward;
	private List<MazeEvent> encouragement;

	/*-------------------------------------------------------------------------*/
	public Quest(
		String name,
		String description,
		String completionVar,
		String rewardedVar,
		List<MazeEvent> introduction,
		List<MazeEvent> reward,
		List<MazeEvent> encouragement)
	{
		this.name = name;
		this.description = description;
		this.completionVar = completionVar;
		this.rewardedVar = rewardedVar;
		this.introduction = introduction;
		this.reward = reward;
		this.encouragement = encouragement;
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> getIntroduction()
	{
		return this.introduction;
	}

	/*-------------------------------------------------------------------------*/
	public boolean isComplete()
	{
		return MazeVariables.getBoolean(this.completionVar);
	}

	/*-------------------------------------------------------------------------*/
	public boolean isRewarded()
	{
		return MazeVariables.getBoolean(this.rewardedVar);
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> getReward()
	{
		return this.reward;
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> getEncouragement()
	{
		return this.encouragement;
	}

	public String getName()
	{
		return name;
	}

	public String getDescription()
	{
		return description;
	}

	public String getCompletionVar()
	{
		return completionVar;
	}

	public String getRewardedVar()
	{
		return rewardedVar;
	}
}
