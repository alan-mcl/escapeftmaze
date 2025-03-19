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

import java.awt.Cursor;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import mclachlan.diygui.DIYPane;
import mclachlan.diygui.toolkit.DIYToolkit;
import mclachlan.maze.data.Database;
import mclachlan.maze.game.Maze;
import mclachlan.maze.stat.PlayerCharacter;

/* Not one of my greatest moments. */
class WhoDialog extends DIYPane
{
	private final ChooseCharacterCallback callback;

	/*----------------------------------------------------------------------*/
	public WhoDialog(ChooseCharacterCallback callback)
	{
		this.callback = callback;
		Cursor cursor = Toolkit.getDefaultToolkit().createCustomCursor(
			Database.getInstance().getImage("cursor/who"),
			new Point(0,0),
			"cursor/who");
		DIYToolkit.getInstance().setCursor(cursor, null);
	}

	/*----------------------------------------------------------------------*/
	private void chooseCharacter(PlayerCharacter pc)
	{
		int index = Maze.getInstance().getParty().getActors().indexOf(pc);

		if (callback.characterChosen(pc, index))
		{
			destroy();
		}

		callback.afterCharacterChosen();
	}

	/*----------------------------------------------------------------------*/
	public void processMouseClicked(MouseEvent e)
	{
		// because of the invisible pane behind the dialog we need to base this
		// on the point of the click
		for (PlayerCharacter pc : Maze.getInstance().getParty().getPlayerCharacters())
		{
			if (Maze.getInstance().getUi().getPlayerCharacterWidgetBounds(pc).contains(e.getPoint()))
			{
				chooseCharacter(pc);
				break;
			}
		}

		// a mouse click elsewhere deselects "WHO?" mode.
		destroy();
	}

	/*----------------------------------------------------------------------*/
	public void processKeyPressed(KeyEvent e)
	{
		// on a press on 1-6, choose that character
		PlayerCharacter playerCharacter = null;
		switch (e.getKeyCode())
		{
			case KeyEvent.VK_1, KeyEvent.VK_NUMPAD1 ->
				playerCharacter = Maze.getInstance().getPlayerCharacter(0);
			case KeyEvent.VK_2, KeyEvent.VK_NUMPAD2 ->
				playerCharacter = Maze.getInstance().getPlayerCharacter(1);
			case KeyEvent.VK_3, KeyEvent.VK_NUMPAD3 ->
				playerCharacter = Maze.getInstance().getPlayerCharacter(2);
			case KeyEvent.VK_4, KeyEvent.VK_NUMPAD4 ->
				playerCharacter = Maze.getInstance().getPlayerCharacter(3);
			case KeyEvent.VK_5, KeyEvent.VK_NUMPAD5 ->
				playerCharacter = Maze.getInstance().getPlayerCharacter(4);
			case KeyEvent.VK_6, KeyEvent.VK_NUMPAD6 ->
				playerCharacter = Maze.getInstance().getPlayerCharacter(5);
			case KeyEvent.VK_ESCAPE -> destroy();
		}

		if (playerCharacter != null)
		{
			e.consume();
			chooseCharacter(playerCharacter);
		}
	}

	private void destroy()
	{
		DIYToolkit.getInstance().clearCursor();
		DIYToolkit.getInstance().clearDialog(this);
	}
}
