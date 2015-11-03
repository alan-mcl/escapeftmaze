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
	private DiyGuiUserInterface ui;

	/*-------------------------------------------------------------------------*/
	public ContentPaneActionListener(DiyGuiUserInterface ui)
	{
		this.ui = ui;
	}

	/*-------------------------------------------------------------------------*/
	public void actionPerformed(ActionEvent event)
	{
		if (Maze.getInstance().getUi() == null)
		{
			return;
		}
		
		if (event.getEvent() instanceof KeyEvent)
		{
			this.processKeyEvent((KeyEvent)event.getEvent());
		}
		else if (event.getEvent() instanceof MouseEvent)
		{
			this.processMouseEvent((MouseEvent)event.getEvent());
		}
	}

	/*-------------------------------------------------------------------------*/
	private void processMouseEvent(MouseEvent event)
	{
		DiyGuiUserInterface.instance.mouseEventToAnimations(event);

		if (Maze.getInstance().getState() == Maze.State.MOVEMENT)
		{
			synchronized (Maze.getInstance().getEventMutex())
			{
				Maze.getInstance().getEventMutex().notifyAll();
			}
		}
		else if (Maze.getInstance().getState() == Maze.State.SIGNBOARD)
		{
			ui.signBoardWidget.clearSignboard();
		}
		else if (Maze.getInstance().getState() == Maze.State.ENCOUNTER_TILE)
		{
			if (ui.combatDisplayIsVisible())
			{
				ui.combatDisplay.processMouseClicked(event);
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
			if (ui.combatDisplayIsVisible())
			{
				ui.combatDisplay.processMouseClicked(event);
			}
		}
		else if (Maze.getInstance().getState() == Maze.State.ENCOUNTER_PORTAL)
		{
			if (ui.combatDisplayIsVisible())
			{
				ui.combatDisplay.processMouseClicked(event);
			}
		}
		else if (Maze.getInstance().getState() == Maze.State.COMBAT)
		{
			if (ui.combatDisplayIsVisible())
			{
				ui.combatDisplay.processMouseClicked(event);
			}
		}
		else if (Maze.getInstance().getState() == Maze.State.RESTING)
		{
			if (ui.restingWidget.done == event.getSource())
			{
				ui.restingWidget.done();
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	private void processKeyEvent(KeyEvent event)
	{
		if (event.getID() != KeyEvent.KEY_PRESSED)
		{
			return;
		}

		DiyGuiUserInterface.instance.keyEventToAnimations(event);

		if (Maze.getInstance().getState() == Maze.State.MOVEMENT)
		{
			ui.partyOptionsAndTextWidget.handleKey(event.getKeyCode());
		}
		else if (Maze.getInstance().getState() == Maze.State.MODIFIERSDISPLAY)
		{
			switch (event.getKeyCode())
			{
				case KeyEvent.VK_1: ui.characterSelected(0); break;
				case KeyEvent.VK_2: ui.characterSelected(1); break;
				case KeyEvent.VK_3: ui.characterSelected(2); break;
				case KeyEvent.VK_4: ui.characterSelected(3); break;
				case KeyEvent.VK_5: ui.characterSelected(4); break;
				case KeyEvent.VK_6: ui.characterSelected(5); break;
				case KeyEvent.VK_A: ui.buttonToolbar.magic(); break;
				case KeyEvent.VK_M: ui.buttonToolbar.modifiers(); break;
				case KeyEvent.VK_S: ui.buttonToolbar.stats(); break;
				case KeyEvent.VK_P: ui.buttonToolbar.properties(); break;
				case KeyEvent.VK_I: ui.buttonToolbar.inventory(); break;
				case KeyEvent.VK_E: ui.buttonToolbar.exit(); break;
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
				case KeyEvent.VK_1: ui.characterSelected(0); break;
				case KeyEvent.VK_2: ui.characterSelected(1); break;
				case KeyEvent.VK_3: ui.characterSelected(2); break;
				case KeyEvent.VK_4: ui.characterSelected(3); break;
				case KeyEvent.VK_5: ui.characterSelected(4); break;
				case KeyEvent.VK_6: ui.characterSelected(5); break;
				case KeyEvent.VK_A: ui.buttonToolbar.magic(); break;
				case KeyEvent.VK_M: ui.buttonToolbar.modifiers(); break;
				case KeyEvent.VK_S: ui.buttonToolbar.stats(); break;
				case KeyEvent.VK_P: ui.buttonToolbar.properties(); break;
				case KeyEvent.VK_I: ui.buttonToolbar.inventory(); break;
				case KeyEvent.VK_E: ui.buttonToolbar.exit(); break;
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
				case KeyEvent.VK_1: ui.characterSelected(0); break;
				case KeyEvent.VK_2: ui.characterSelected(1); break;
				case KeyEvent.VK_3: ui.characterSelected(2); break;
				case KeyEvent.VK_4: ui.characterSelected(3); break;
				case KeyEvent.VK_5: ui.characterSelected(4); break;
				case KeyEvent.VK_6: ui.characterSelected(5); break;
				case KeyEvent.VK_A: ui.buttonToolbar.magic(); break;
				case KeyEvent.VK_M: ui.buttonToolbar.modifiers(); break;
				case KeyEvent.VK_S: ui.buttonToolbar.stats(); break;
				case KeyEvent.VK_P: ui.buttonToolbar.properties(); break;
				case KeyEvent.VK_I: ui.buttonToolbar.inventory(); break;
				case KeyEvent.VK_E: ui.buttonToolbar.exit(); break;
				case KeyEvent.VK_C: ui.inventoryDisplay.spell(); break;
				case KeyEvent.VK_U: ui.inventoryDisplay.use(); break;
				case KeyEvent.VK_R: ui.inventoryDisplay.craft(); break;
				case KeyEvent.VK_D: ui.inventoryDisplay.drop(); break;
				case KeyEvent.VK_L: ui.inventoryDisplay.split(); break;
				case KeyEvent.VK_B: ui.inventoryDisplay.disassemble(); break;
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
				case KeyEvent.VK_1: ui.characterSelected(0); break;
				case KeyEvent.VK_2: ui.characterSelected(1); break;
				case KeyEvent.VK_3: ui.characterSelected(2); break;
				case KeyEvent.VK_4: ui.characterSelected(3); break;
				case KeyEvent.VK_5: ui.characterSelected(4); break;
				case KeyEvent.VK_6: ui.characterSelected(5); break;
				case KeyEvent.VK_A: ui.buttonToolbar.magic(); break;
				case KeyEvent.VK_M: ui.buttonToolbar.modifiers(); break;
				case KeyEvent.VK_S: ui.buttonToolbar.stats(); break;
				case KeyEvent.VK_P: ui.buttonToolbar.properties(); break;
				case KeyEvent.VK_I: ui.buttonToolbar.inventory(); break;
				case KeyEvent.VK_E: ui.buttonToolbar.exit(); break;
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
				case KeyEvent.VK_1: ui.characterSelected(0); break;
				case KeyEvent.VK_2: ui.characterSelected(1); break;
				case KeyEvent.VK_3: ui.characterSelected(2); break;
				case KeyEvent.VK_4: ui.characterSelected(3); break;
				case KeyEvent.VK_5: ui.characterSelected(4); break;
				case KeyEvent.VK_6: ui.characterSelected(5); break;
				case KeyEvent.VK_A: ui.buttonToolbar.magic(); break;
				case KeyEvent.VK_M: ui.buttonToolbar.modifiers(); break;
				case KeyEvent.VK_S: ui.buttonToolbar.stats(); break;
				case KeyEvent.VK_P: ui.buttonToolbar.properties(); break;
				case KeyEvent.VK_I: ui.buttonToolbar.inventory(); break;
				case KeyEvent.VK_E: ui.buttonToolbar.exit(); break;
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
		else if (Maze.getInstance().getState() == Maze.State.SIGNBOARD)
		{
			switch (event.getKeyCode())
			{
				case KeyEvent.VK_ENTER: 
				case KeyEvent.VK_SPACE: 
				case KeyEvent.VK_ESCAPE: ui.signBoardWidget.clearSignboard(); break;
			}
		}
		else if (Maze.getInstance().getState() == Maze.State.MAINMENU)
		{
			switch (event.getKeyCode())
			{
				case KeyEvent.VK_S: ui.mainMenu.startGame(); break;
				case KeyEvent.VK_C: ui.mainMenu.createCharacter(); break;
				case KeyEvent.VK_A: ui.mainMenu.addCharacter(); break;
				case KeyEvent.VK_R: ui.mainMenu.removeCharacter(); break;
				case KeyEvent.VK_D: ui.mainMenu.saveOrLoad(); break;
				case KeyEvent.VK_G: ui.mainMenu.showSettingsDialog(); break;
				case KeyEvent.VK_Q: ui.mainMenu.quit(); break;
				case KeyEvent.VK_U: ui.mainMenu.quickStart(); break;
			}
			
			ui.mainMenu.updateState();
		}
		else if (Maze.getInstance().getState() == Maze.State.COMBAT)
		{
			ui.partyOptionsAndTextWidget.handleKey(event.getKeyCode());
		}
		else if (Maze.getInstance().getState() == Maze.State.ENCOUNTER_PORTAL)
		{
			switch (event.getKeyCode())
			{
				case KeyEvent.VK_P: ui.portalOptionsWidget.pickLock(); break;
				case KeyEvent.VK_C: ui.portalOptionsWidget.castSpell(); break;
				case KeyEvent.VK_U: ui.portalOptionsWidget.useItem(); break;
				case KeyEvent.VK_F: ui.portalOptionsWidget.force(); break;
				case KeyEvent.VK_ESCAPE:
				case KeyEvent.VK_L: ui.portalOptionsWidget.leave(); break;
			}
		}
		else if (Maze.getInstance().getState() == Maze.State.ENCOUNTER_ACTORS)
		{
			ui.partyOptionsAndTextWidget.handleKey(event.getKeyCode());
		}
		else if (Maze.getInstance().getState() == Maze.State.ENCOUNTER_TILE)
		{
			if (ui.combatDisplayIsVisible())
			{
				ui.combatDisplay.processKeyPressed(event);
			}
		}
		else if (Maze.getInstance().getState() == Maze.State.ENCOUNTER_PORTAL)
		{
			if (ui.combatDisplayIsVisible())
			{
				ui.combatDisplay.processKeyPressed(event);
			}
		}
		else if (Maze.getInstance().getState() == Maze.State.ENCOUNTER_CHEST)
		{
			ui.partyOptionsAndTextWidget.handleKey(event.getKeyCode());
		}
		else if (Maze.getInstance().getState() == Maze.State.RESTING)
		{
			switch (event.getKeyCode())
			{
				case KeyEvent.VK_ESCAPE:
				case KeyEvent.VK_ENTER:
				case KeyEvent.VK_D: ui.restingWidget.done(); break;
			}
		}
		else if (Maze.getInstance().getState() == Maze.State.SAVE_LOAD)
		{
			switch (event.getKeyCode())
			{
				case KeyEvent.VK_D: ui.saveLoad.loadGame(); break;
				case KeyEvent.VK_S: ui.saveLoad.saveGame(); break;
				default: ui.saveLoad.keyPressed(event);
			}
		}
	}
}
