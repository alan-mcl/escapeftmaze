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

import mclachlan.diygui.DIYPane;
import mclachlan.diygui.toolkit.DIYToolkit;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.KeyEvent;
import mclachlan.maze.data.Database;
import mclachlan.maze.stat.PlayerCharacter;
import mclachlan.maze.game.Maze;

/* Not one of my greatest moments. */
class WhoDialog extends DIYPane
{
	private ChooseCharacterCallback callback;

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
	}

	/*----------------------------------------------------------------------*/
	public boolean processMouseClicked(MouseEvent e)
	{
		if (e.getSource() instanceof PlayerCharacterWidget)
		{
			chooseCharacter(((PlayerCharacterWidget)e.getSource()).getPlayerCharacter());
		}
		else
		{
			// a mouse click elsewhere deselects "WHO?" mode.
			destroy();
		}

		return true;
	}

	/*----------------------------------------------------------------------*/
	public void processKeyPressed(KeyEvent e)
	{
		// on a press on 1-6, choose that character
		PlayerCharacter playerCharacter = null;
		switch (e.getKeyCode())
		{
			case KeyEvent.VK_1:
			case KeyEvent.VK_NUMPAD1:
				playerCharacter = Maze.getInstance().getPlayerCharacter(0);
				break;
			case KeyEvent.VK_2:
			case KeyEvent.VK_NUMPAD2:
				playerCharacter = Maze.getInstance().getPlayerCharacter(1);
				break;
			case KeyEvent.VK_3:
			case KeyEvent.VK_NUMPAD3:
				playerCharacter = Maze.getInstance().getPlayerCharacter(2);
				break;
			case KeyEvent.VK_4:
			case KeyEvent.VK_NUMPAD4:
				playerCharacter = Maze.getInstance().getPlayerCharacter(3);
				break;
			case KeyEvent.VK_5:
			case KeyEvent.VK_NUMPAD5:
				playerCharacter = Maze.getInstance().getPlayerCharacter(4);
				break;
			case KeyEvent.VK_6:
			case KeyEvent.VK_NUMPAD6:
				playerCharacter = Maze.getInstance().getPlayerCharacter(5);
				break;
			case KeyEvent.VK_ESCAPE:
				destroy();
				break;
		}

		if (playerCharacter != null)
		{
			chooseCharacter(playerCharacter);
		}
	}

	private void destroy()
	{
		DIYToolkit.getInstance().clearCursor();
		DIYToolkit.getInstance().clearDialog(this);
	}
}
