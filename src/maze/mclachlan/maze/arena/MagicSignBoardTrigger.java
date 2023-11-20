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

package mclachlan.maze.arena;

import mclachlan.maze.map.TileScript;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeVariables;
import mclachlan.maze.data.Database;
import java.awt.*;
import mclachlan.crusader.EngineObject;
import mclachlan.crusader.Texture;

/**
 * Test arena script implementation.
 * Creates the appearing signboard when the player steps on tile 45 for the
 * first time.
 */
public class MagicSignBoardTrigger extends TileScript
{
	static final String VAR = "arena.magic_signboard_added";
	
	/*-------------------------------------------------------------------------*/
	public java.util.List<MazeEvent> execute(Maze maze, Point tile, Point previousTile, int facing)
	{
		if (tile == previousTile)
		{
			return null;
		}
		
		if (MazeVariables.get(VAR) != null)
		{
			return null;
		}
		
		Texture front = Database.getInstance().getMazeTexture("objects.woodsign.front").getTexture();
		Texture side = Database.getInstance().getMazeTexture("objects.woodsign.side").getTexture();
		maze.addObject(new EngineObject(null,front,front,side,side,45,true,null,null, EngineObject.Alignment.BOTTOM));
		MazeVariables.set(VAR, "true");

		return null;
	}
}
