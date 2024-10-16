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

import mclachlan.maze.game.Maze;
import mclachlan.maze.stat.PlayerCharacter;

/**
 *
 */
public class GetAmount implements TextDialogCallback
{
	private final GetAmountCallback callback;
	private final PlayerCharacter user;
	private final int userIndex;

	/*-------------------------------------------------------------------------*/
	public GetAmount(GetAmountCallback callback, PlayerCharacter pc, int max)
	{
		this.callback = callback;
		this.user = pc;
		this.userIndex = Maze.getInstance().getParty().getActors().indexOf(user);

		GetAmountDialog dialog = new GetAmountDialog(pc, max, this);
		Maze.getInstance().getUi().showDialog(dialog);
	}

	/*-------------------------------------------------------------------------*/
	public void textEntered(String text)
	{
		// we can take no sensible default action
		callback.amountChosen(Integer.parseInt(text), user, userIndex);
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public void textEntryCancelled()
	{
		// no op
	}
}
