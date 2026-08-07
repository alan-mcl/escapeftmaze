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

package mclachlan.maze.game.event;

import java.util.*;
import mclachlan.maze.data.StringUtil;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.stat.PartyCampManager;
import mclachlan.maze.stat.PlayerCharacter;
import mclachlan.maze.ui.diygui.GuildCallback;
import mclachlan.maze.ui.diygui.GuildDisplayDialog;

/**
 * Opens the party camp dialog for transferring characters between the field
 * party and the temporary camp.
 */
public class InitiatePartyCampEvent extends MazeEvent
{
	@Override
	public int getDelay()
	{
		return Delay.NONE;
	}

	@Override
	public List<MazeEvent> resolve()
	{
		Maze maze = Maze.getInstance();
		PartyCampManager mgr = PartyCampManager.getInstance();

		List<PlayerCharacter> campPcs = mgr.getCampPlayerCharacters(maze);

		GuildDisplayDialog dialog = new GuildDisplayDialog(
			GuildDisplayDialog.Mode.CAMP,
			StringUtil.getUiLabel("gdd.title.camp"),
			campPcs,
			100,
			new GuildCallback()
			{
				@Override
				public void createCharacter(int createPrice)
				{
				}

				@Override
				public boolean transferPlayerCharacterToParty(PlayerCharacter pc, int recruitPrice)
				{
					return maze.transferPlayerCharacterFromCamp(pc);
				}

				@Override
				public void removeFromParty(PlayerCharacter pc, int recruitPrice)
				{
					maze.transferPlayerCharacterToCamp(pc);
				}
			});
		maze.getUi().showDialog(dialog);

		return null;
	}
}
