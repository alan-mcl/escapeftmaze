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

import java.awt.Rectangle;
import java.util.*;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.map.script.LockOrTrap;
import mclachlan.maze.stat.PlayerCharacter;
import mclachlan.maze.ui.diygui.DiyGuiUserInterface;
import mclachlan.maze.ui.diygui.PickLockWidget;

public class PickLockEvent extends MazeEvent
{
	private final PlayerCharacter pc;
	private final LockOrTrap lockOrTrap;

	/*-------------------------------------------------------------------------*/
	public PickLockEvent(
		PlayerCharacter pc,
		LockOrTrap lockOrTrap)
	{
		this.pc = pc;
		this.lockOrTrap = lockOrTrap;
	}

	/*-------------------------------------------------------------------------*/

	@Override
	public int getDelay()
	{
		return Delay.NONE;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public List<MazeEvent> resolve()
	{
		int width = DiyGuiUserInterface.SCREEN_WIDTH / 2;
		int height = DiyGuiUserInterface.SCREEN_HEIGHT / 2;

		int x = DiyGuiUserInterface.SCREEN_WIDTH/2 -width/2;
		int y = DiyGuiUserInterface.SCREEN_HEIGHT/2 -height/2;

		Rectangle rectangle = new Rectangle(x, y, width, height);

		PickLockWidget dialog = new PickLockWidget(lockOrTrap, rectangle, pc);
		Maze.getInstance().getUi().showDialog(dialog);

		return null;
	}
}
