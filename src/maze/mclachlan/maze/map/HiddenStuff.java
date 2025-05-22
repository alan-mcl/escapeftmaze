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
import mclachlan.maze.game.MazeScript;

/**
 * no longer used
 */
public class HiddenStuff extends TileScript
{
	private int findDifficulty;
	private String mazeVariable;
	private MazeScript preScript;
	private MazeScript content;

	public HiddenStuff()
	{
	}

	/*-------------------------------------------------------------------------*/
	public HiddenStuff(
		MazeScript content,
		MazeScript preScript,
		String mazeVariable,
		int findDifficulty)
	{
		this.mazeVariable = mazeVariable;
		this.content = content;
		this.preScript = preScript;
		this.findDifficulty = findDifficulty;
	}
	
	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> execute(Maze maze, Point tile, Point previousTile, int facing)
	{
		List<MazeEvent> result = new ArrayList<>();

		if (preScript != null)
		{
			result.addAll(preScript.getEvents());
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> handlePlayerAction(Maze maze, Point tile, int facing, int playerAction)
	{
		List<MazeEvent> result = new ArrayList<>();

/*		if (playerAction == PlayerAction.SEARCH) todo
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
		}*/

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

	public int getFindDifficulty()
	{
		return findDifficulty;
	}

	public void setFindDifficulty(int findDifficulty)
	{
		this.findDifficulty = findDifficulty;
	}

	public void setMazeVariable(String mazeVariable)
	{
		this.mazeVariable = mazeVariable;
	}

	public void setPreScript(MazeScript preScript)
	{
		this.preScript = preScript;
	}

	public void setContent(MazeScript content)
	{
		this.content = content;
	}

	/*-------------------------------------------------------------------------*/

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
		if (!super.equals(o))
		{
			return false;
		}

		HiddenStuff that = (HiddenStuff)o;

		if (getFindDifficulty() != that.getFindDifficulty())
		{
			return false;
		}
		if (getMazeVariable() != null ? !getMazeVariable().equals(that.getMazeVariable()) : that.getMazeVariable() != null)
		{
			return false;
		}
		if (getPreScript() != null ? !getPreScript().equals(that.getPreScript()) : that.getPreScript() != null)
		{
			return false;
		}
		return getContent() != null ? getContent().equals(that.getContent()) : that.getContent() == null;
	}

	@Override
	public int hashCode()
	{
		int result = super.hashCode();
		result = 31 * result + getFindDifficulty();
		result = 31 * result + (getMazeVariable() != null ? getMazeVariable().hashCode() : 0);
		result = 31 * result + (getPreScript() != null ? getPreScript().hashCode() : 0);
		result = 31 * result + (getContent() != null ? getContent().hashCode() : 0);
		return result;
	}
}
