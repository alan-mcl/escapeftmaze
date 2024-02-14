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

package mclachlan.maze.ui.diygui;

import mclachlan.crusader.Map;
import mclachlan.crusader.MouseClickScript;
import mclachlan.maze.game.Maze;
import mclachlan.maze.stat.Foe;
import mclachlan.maze.stat.GameSys;
import mclachlan.maze.stat.PlayerCharacter;

/**
 *
 */
public class FoeInfoMouseClickScript implements MouseClickScript, ConfirmCallback
{
	private Foe foe;

	/*-------------------------------------------------------------------------*/
	public FoeInfoMouseClickScript(Foe foe)
	{
		this.foe = foe;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public void initialise(Map map)
	{
		// nothing to do
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public void execute(Map map)
	{
		PlayerCharacter mythologist = GameSys.getInstance().getMythologist(
			Maze.getInstance().getParty(), foe);

		int mythologyTotal = GameSys.getInstance().getMythologyToIdentify(mythologist, foe);

		int information = mythologyTotal - foe.getIdentificationDifficulty();

		DiyGuiUserInterface.gui.setDialog(new FoeDetailsDialog(foe, information));
	}

	@Override
	public int getMaxDist()
	{
		return 1;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public void confirm()
	{
		DiyGuiUserInterface.gui.clearDialog();
	}
}
