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
import mclachlan.maze.data.StringUtil;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.game.MazeScript;
import mclachlan.maze.game.MazeVariables;
import mclachlan.maze.game.event.UiMessageEvent;
import mclachlan.maze.stat.GameSys;
import mclachlan.maze.stat.PlayerCharacter;
import mclachlan.maze.stat.SpeechUtil;

/**
 *
 */
public class HiddenStuff extends TileScript
{
	private int spotDifficulty, findDifficulty;
	private String mazeVariable;
	private MazeScript preScript, content;
	
	/*-------------------------------------------------------------------------*/
	public HiddenStuff(
		MazeScript content,
		MazeScript preScript,
		String mazeVariable,
		int spotDifficulty, int findDifficulty)
	{
		this.mazeVariable = mazeVariable;
		this.content = content;
		this.preScript = preScript;
		this.spotDifficulty = spotDifficulty;
		this.findDifficulty = findDifficulty;
	}
	
	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> execute(Maze maze, Point tile, Point previousTile, int facing)
	{
		List<MazeEvent> result = new ArrayList<MazeEvent>();

		if (preScript != null)
		{
			result.addAll(preScript.getEvents());
		}

		PlayerCharacter pc = GameSys.getInstance().scoutingSpotsStash(maze, spotDifficulty);
		if (pc != null)
		{
			result.addAll(SpeechUtil.getInstance().spotStashSpeech(pc));
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> handlePlayerAction(Maze maze, Point tile, int facing, int playerAction)
	{
		List<MazeEvent> result = new ArrayList<MazeEvent>();

		if (playerAction == PlayerAction.SEARCH)
		{
			PlayerCharacter pc = GameSys.getInstance().scoutingFindsStash(maze, findDifficulty);
			if (pc != null)
			{
				if (mazeVariable == null || MazeVariables.get(mazeVariable) == null)
				{
					if (mazeVariable != null && !mazeVariable.equals(""))
					{
						MazeVariables.set(mazeVariable, "1");
					}

					result.add(new UiMessageEvent(
						StringUtil.getGamesysString("scouting.find.stash", false, pc.getDisplayName())));

					result.addAll(content.getEvents());
				}
			}
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public MazeScript getContent()
	{
		return content;
	}

	public String getMazeVariable()
	{
		return mazeVariable;
	}

	public MazeScript getPreScript()
	{
		return preScript;
	}

	public int getSpotDifficulty()
	{
		return spotDifficulty;
	}

	public int getFindDifficulty()
	{
		return findDifficulty;
	}
}
