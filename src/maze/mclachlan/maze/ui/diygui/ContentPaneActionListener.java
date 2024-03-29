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

import mclachlan.diygui.toolkit.ActionListener;
import mclachlan.diygui.toolkit.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import mclachlan.maze.game.Maze;

/**
 * Since the content pane is the ultimate parent of all the widgets, events
 * are often passed up here.  This is where all the neat shortcut keys are
 * implemented. 
 */
class ContentPaneActionListener implements ActionListener
{
	private final DiyGuiUserInterface ui;

	// todo: refactor these together
	private final MazeActionListener mal = new MazeActionListener();


	/*-------------------------------------------------------------------------*/
	public ContentPaneActionListener(DiyGuiUserInterface ui)
	{
		this.ui = ui;
	}

	/*-------------------------------------------------------------------------*/
	public boolean actionPerformed(ActionEvent event)
	{
		if (Maze.getInstance().getUi() == null)
		{
			return false;
		}

		boolean consumed = false;

		if (event.getEvent() instanceof KeyEvent)
		{
			consumed |= this.processKeyEvent((KeyEvent)event.getEvent());
		}
		else if (event.getEvent() instanceof MouseEvent)
		{
			consumed |= this.processMouseEvent((MouseEvent)event.getEvent());
		}

		if (!consumed)
		{
			consumed |= mal.actionPerformed(event);
		}

		return false;
	}

	/*-------------------------------------------------------------------------*/
	private boolean processMouseEvent(MouseEvent event)
	{
		boolean consumed = DiyGuiUserInterface.instance.mouseEventToAnimations(event);

		if (!consumed)
		{
			if (Maze.getInstance().getState() == Maze.State.MOVEMENT)
			{
				synchronized (Maze.getInstance().getEventMutex())
				{
					Maze.getInstance().getEventMutex().notifyAll();
				}
			}
			else if (Maze.getInstance().getState() == Maze.State.ENCOUNTER_ACTORS)
			{
				synchronized (Maze.getInstance().getEventMutex())
				{
					Maze.getInstance().getEventMutex().notifyAll();
				}
			}
			else if (Maze.getInstance().getState() == Maze.State.ENCOUNTER_CHEST)
			{
				//			if (ui.combatDisplayIsVisible())
				//			{
				//				ui.combatDisplay.processMouseClicked(event);
				//			}
			}
			else if (Maze.getInstance().getState() == Maze.State.ENCOUNTER_PORTAL)
			{
				//			if (ui.combatDisplayIsVisible())
				//			{
				//				ui.combatDisplay.processMouseClicked(event);
				//			}
			}
			else if (Maze.getInstance().getState() == Maze.State.COMBAT)
			{
				//			if (ui.combatDisplayIsVisible())
				//			{
				//				ui.combatDisplay.processMouseClicked(event);
				//			}
			}
			else if (Maze.getInstance().getState() == Maze.State.RESTING)
			{
				if (ui.restingWidget.done == event.getSource())
				{
					ui.restingWidget.done();
				}
			}
		}

		return consumed;
	}

	/*-------------------------------------------------------------------------*/
	private boolean processKeyEvent(KeyEvent event)
	{
		if (event.getID() != KeyEvent.KEY_PRESSED)
		{
			return false;
		}

		boolean eventConsumed = DiyGuiUserInterface.instance.keyEventToAnimations(event);

		if (!eventConsumed)
		{
			if (Maze.getInstance().getState() == Maze.State.MOVEMENT)
			{
				ui.partyOptionsAndTextWidget.handleKey(event.getKeyCode());
			}
			else if (Maze.getInstance().getState() == Maze.State.MODIFIERSDISPLAY)
			{
				switch (event.getKeyCode())
				{
					case KeyEvent.VK_1:
						ui.characterSelected(0);
						break;
					case KeyEvent.VK_2:
						ui.characterSelected(1);
						break;
					case KeyEvent.VK_3:
						ui.characterSelected(2);
						break;
					case KeyEvent.VK_4:
						ui.characterSelected(3);
						break;
					case KeyEvent.VK_5:
						ui.characterSelected(4);
						break;
					case KeyEvent.VK_6:
						ui.characterSelected(5);
						break;
					case KeyEvent.VK_A:
						ui.buttonToolbar.magic();
						break;
					case KeyEvent.VK_M:
						ui.buttonToolbar.modifiers();
						break;
					case KeyEvent.VK_S:
						ui.buttonToolbar.stats();
						break;
					case KeyEvent.VK_P:
						ui.buttonToolbar.properties();
						break;
					case KeyEvent.VK_I:
						ui.buttonToolbar.inventory();
						break;
					case KeyEvent.VK_E:
						ui.buttonToolbar.exit();
						break;
					case KeyEvent.VK_ENTER:
					case KeyEvent.VK_ESCAPE:
						if (Maze.getInstance().isInCombat())
						{
							Maze.getInstance().setState(Maze.State.COMBAT);
						}
						else
						{
							Maze.getInstance().setState(Maze.State.MOVEMENT);
						}
						break;
				}
			}
			else if (Maze.getInstance().getState() == Maze.State.STATSDISPLAY)
			{
				switch (event.getKeyCode())
				{
					case KeyEvent.VK_1:
						ui.characterSelected(0);
						break;
					case KeyEvent.VK_2:
						ui.characterSelected(1);
						break;
					case KeyEvent.VK_3:
						ui.characterSelected(2);
						break;
					case KeyEvent.VK_4:
						ui.characterSelected(3);
						break;
					case KeyEvent.VK_5:
						ui.characterSelected(4);
						break;
					case KeyEvent.VK_6:
						ui.characterSelected(5);
						break;
					case KeyEvent.VK_A:
						ui.buttonToolbar.magic();
						break;
					case KeyEvent.VK_M:
						ui.buttonToolbar.modifiers();
						break;
					case KeyEvent.VK_S:
						ui.buttonToolbar.stats();
						break;
					case KeyEvent.VK_P:
						ui.buttonToolbar.properties();
						break;
					case KeyEvent.VK_I:
						ui.buttonToolbar.inventory();
						break;
					case KeyEvent.VK_E:
						ui.buttonToolbar.exit();
						break;
					case KeyEvent.VK_ENTER:
					case KeyEvent.VK_ESCAPE:
						if (Maze.getInstance().isInCombat())
						{
							Maze.getInstance().setState(Maze.State.COMBAT);
						}
						else
						{
							Maze.getInstance().setState(Maze.State.MOVEMENT);
						}
						break;
				}
			}
			else if (Maze.getInstance().getState() == Maze.State.INVENTORY)
			{
				switch (event.getKeyCode())
				{
					case KeyEvent.VK_1:
						ui.characterSelected(0);
						break;
					case KeyEvent.VK_2:
						ui.characterSelected(1);
						break;
					case KeyEvent.VK_3:
						ui.characterSelected(2);
						break;
					case KeyEvent.VK_4:
						ui.characterSelected(3);
						break;
					case KeyEvent.VK_5:
						ui.characterSelected(4);
						break;
					case KeyEvent.VK_6:
						ui.characterSelected(5);
						break;
					case KeyEvent.VK_A:
						ui.buttonToolbar.magic();
						break;
					case KeyEvent.VK_M:
						ui.buttonToolbar.modifiers();
						break;
					case KeyEvent.VK_S:
						ui.buttonToolbar.stats();
						break;
					case KeyEvent.VK_P:
						ui.buttonToolbar.properties();
						break;
					case KeyEvent.VK_I:
						ui.buttonToolbar.inventory();
						break;
					case KeyEvent.VK_E:
						ui.buttonToolbar.exit();
						break;
					case KeyEvent.VK_C:
						ui.inventoryDisplay.spell();
						break;
					case KeyEvent.VK_U:
						ui.inventoryDisplay.use();
						break;
					case KeyEvent.VK_R:
						ui.inventoryDisplay.craft();
						break;
					case KeyEvent.VK_D:
						ui.inventoryDisplay.drop();
						break;
					case KeyEvent.VK_L:
						ui.inventoryDisplay.split();
						break;
					case KeyEvent.VK_B:
						ui.inventoryDisplay.disassemble();
						break;
					case KeyEvent.VK_ENTER:
					case KeyEvent.VK_ESCAPE:
						if (Maze.getInstance().isInCombat())
						{
							Maze.getInstance().setState(Maze.State.COMBAT);
						}
						else
						{
							Maze.getInstance().setState(Maze.State.MOVEMENT);
						}
						break;
				}
			}
			else if (Maze.getInstance().getState() == Maze.State.PROPERTIESDISPLAY)
			{
				switch (event.getKeyCode())
				{
					case KeyEvent.VK_1:
						ui.characterSelected(0);
						break;
					case KeyEvent.VK_2:
						ui.characterSelected(1);
						break;
					case KeyEvent.VK_3:
						ui.characterSelected(2);
						break;
					case KeyEvent.VK_4:
						ui.characterSelected(3);
						break;
					case KeyEvent.VK_5:
						ui.characterSelected(4);
						break;
					case KeyEvent.VK_6:
						ui.characterSelected(5);
						break;
					case KeyEvent.VK_A:
						ui.buttonToolbar.magic();
						break;
					case KeyEvent.VK_M:
						ui.buttonToolbar.modifiers();
						break;
					case KeyEvent.VK_S:
						ui.buttonToolbar.stats();
						break;
					case KeyEvent.VK_P:
						ui.buttonToolbar.properties();
						break;
					case KeyEvent.VK_I:
						ui.buttonToolbar.inventory();
						break;
					case KeyEvent.VK_E:
						ui.buttonToolbar.exit();
						break;
					case KeyEvent.VK_ENTER:
					case KeyEvent.VK_ESCAPE:
						if (Maze.getInstance().isInCombat())
						{
							Maze.getInstance().setState(Maze.State.COMBAT);
						}
						else
						{
							Maze.getInstance().setState(Maze.State.MOVEMENT);
						}
						break;
				}
			}
			else if (Maze.getInstance().getState() == Maze.State.MAGIC)
			{
				switch (event.getKeyCode())
				{
					case KeyEvent.VK_1:
						ui.characterSelected(0);
						break;
					case KeyEvent.VK_2:
						ui.characterSelected(1);
						break;
					case KeyEvent.VK_3:
						ui.characterSelected(2);
						break;
					case KeyEvent.VK_4:
						ui.characterSelected(3);
						break;
					case KeyEvent.VK_5:
						ui.characterSelected(4);
						break;
					case KeyEvent.VK_6:
						ui.characterSelected(5);
						break;
					case KeyEvent.VK_A:
						ui.buttonToolbar.magic();
						break;
					case KeyEvent.VK_M:
						ui.buttonToolbar.modifiers();
						break;
					case KeyEvent.VK_S:
						ui.buttonToolbar.stats();
						break;
					case KeyEvent.VK_P:
						ui.buttonToolbar.properties();
						break;
					case KeyEvent.VK_I:
						ui.buttonToolbar.inventory();
						break;
					case KeyEvent.VK_E:
						ui.buttonToolbar.exit();
						break;
					case KeyEvent.VK_ENTER:
					case KeyEvent.VK_ESCAPE:
						if (Maze.getInstance().isInCombat())
						{
							Maze.getInstance().setState(Maze.State.COMBAT);
						}
						else
						{
							Maze.getInstance().setState(Maze.State.MOVEMENT);
						}
						break;
				}
			}
			else if (Maze.getInstance().getState() == Maze.State.MAINMENU)
			{
				switch (event.getKeyCode())
				{
					case KeyEvent.VK_S -> ui.mainMenu.startGame();
					case KeyEvent.VK_C -> ui.mainMenu.createCharacter();
					case KeyEvent.VK_A -> ui.mainMenu.addCharacter();
					case KeyEvent.VK_R -> ui.mainMenu.removeCharacter();
					case KeyEvent.VK_D -> ui.mainMenu.saveOrLoad();
					case KeyEvent.VK_G -> ui.mainMenu.showSettingsDialog();
					case KeyEvent.VK_Q -> ui.mainMenu.quit();
					case KeyEvent.VK_U -> ui.mainMenu.quickStart();
				}

				ui.mainMenu.updateState();
			}
			else if (Maze.getInstance().getState() == Maze.State.COMBAT)
			{
				ui.partyOptionsAndTextWidget.handleKey(event.getKeyCode());
			}
			else if (Maze.getInstance().getState() == Maze.State.ENCOUNTER_PORTAL)
			{
				ui.partyOptionsAndTextWidget.handleKey(event.getKeyCode());
			}
			else if (Maze.getInstance().getState() == Maze.State.ENCOUNTER_ACTORS)
			{
				ui.partyOptionsAndTextWidget.handleKey(event.getKeyCode());
			}
			else if (Maze.getInstance().getState() == Maze.State.ENCOUNTER_CHEST)
			{
				ui.partyOptionsAndTextWidget.handleKey(event.getKeyCode());
			}
			else if (Maze.getInstance().getState() == Maze.State.RESTING)
			{
				switch (event.getKeyCode())
				{
					case KeyEvent.VK_ESCAPE, KeyEvent.VK_ENTER, KeyEvent.VK_SPACE, KeyEvent.VK_D ->
						ui.restingWidget.done();
				}
			}
			else if (Maze.getInstance().getState() == Maze.State.SAVE_LOAD)
			{
				switch (event.getKeyCode())
				{
					case KeyEvent.VK_D -> ui.saveLoad.loadGame();
					case KeyEvent.VK_S -> ui.saveLoad.saveGame();
					default -> ui.saveLoad.keyPressed(event);
				}
			}
		}

		return eventConsumed;
	}
}
