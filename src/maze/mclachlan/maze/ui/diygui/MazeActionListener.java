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

import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.*;
import mclachlan.crusader.CrusaderEngine;
import mclachlan.diygui.toolkit.ActionEvent;
import mclachlan.diygui.toolkit.ActionListener;
import mclachlan.diygui.toolkit.DIYToolkit;
import mclachlan.maze.data.Database;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeScript;
import mclachlan.maze.map.Portal;
import mclachlan.maze.map.Zone;
import mclachlan.maze.util.MazeException;

/**
 *
 */
class MazeActionListener implements ActionListener
{
	private static final int KEY_HISTORY = 10;
	List<Integer> keyCodeHistory = new LinkedList<Integer>();

	/*-------------------------------------------------------------------------*/
	public void actionPerformed(ActionEvent event)
	{
		if (event.getEvent() instanceof MouseEvent)
		{
			this.processMouse(event);
		}
		else if (event.getEvent() instanceof KeyEvent)
		{
			this.processKey(event);
		}
		else
		{
			throw new MazeException("Unrecognised InputEvent: "+event);
		}
	}

	/*----------------------------------------------------------------------*/
	private void processMouse(ActionEvent event)
	{
		String message = event.getMessage();

		if (message == null)
		{
			return;
		}

		if (message.equals(Constants.Messages.BACK_TO_GAME))
		{
			Maze.getInstance().setState(Maze.State.MOVEMENT);
		}
		else if (message.equals(Constants.Messages.DISPOSE_DIALOG))
		{
			DiyGuiUserInterface.gui.clearDialog();
		}
		else
		{
//				System.out.println("message = [" + message + "]");
		}
	}

	/*----------------------------------------------------------------------*/
	private void processKey(ActionEvent event)
	{
		if (Maze.getInstance().getState() == Maze.State.MOVEMENT)
		{
			KeyEvent e = (KeyEvent)event.getEvent();
			if(e.getID() != KeyEvent.KEY_PRESSED)
			{
				return;
			}

			int code = e.getKeyCode();

			if (DiyGuiUserInterface.crusaderKeys.containsKey(code) &&
				Maze.getInstance().getState() == Maze.State.MOVEMENT &&
				DIYToolkit.getInstance().getDialog() == null)
			{
				int crusaderKey = DiyGuiUserInterface.crusaderKeys.get(code);
				handleKeyCode(crusaderKey, true);
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	void handleKeyCode(int crusaderKey, boolean checkRandomEncounters)
	{
		keyCodeHistory.add(0, crusaderKey);
		if (keyCodeHistory.size() > KEY_HISTORY)
		{
			keyCodeHistory.remove(keyCodeHistory.size()-1);
		}

		Point oldTile = DiyGuiUserInterface.instance.raycaster.getPlayerPos();
		int oldFacing = DiyGuiUserInterface.instance.raycaster.getPlayerFacing();

		CrusaderEngine.PlayerStatus playerStatus =
			DiyGuiUserInterface.instance.raycaster.predictKey(crusaderKey);

		if (playerStatus.willPassThroughWall)
		{
			Zone.Vector portalDest = Maze.getInstance().playerAttemptsMoveThroughWall(oldTile, oldFacing);
			if (portalDest != null)
			{
				playerActivatesPortal(
					oldTile, 
					portalDest.location, 
					portalDest.facing,
					portalDest.portal);
			}
			else
			{
				Maze.getInstance().incTurn(true);
			}
		}
		else
		{
			if (Maze.getInstance().getCurrentCombat() == null)
			{
				movePlayer(playerStatus, oldTile);
			}
		}
	}
	
	/*-------------------------------------------------------------------------*/
	private void playerActivatesPortal(Point oldTile, Point newTile, int facing, Portal portal)
	{
		Maze maze = Maze.getInstance();
		Zone oldZone = maze.getCurrentZone();

		if (portal.getMazeScript() != null)
		{
			MazeScript script = Database.getInstance().getMazeScript(portal.getMazeScript());
			maze.appendEvents(script.getEvents());
		}
	
		if (maze.getParty() == null)
		{
			// something in the script has ended the party
			return;
		}

		if (oldZone != maze.getCurrentZone())
		{
			// something in the script has changed the zone
			maze.getUi().showMovementScreen();
			maze.incTurn(true);
			newTile = DiyGuiUserInterface.instance.raycaster.getPlayerPos();
			facing = DiyGuiUserInterface.instance.raycaster.getPlayerFacing();
			maze.encounterTile(newTile, oldTile, facing);
			return;
		}

		CrusaderEngine rc = DiyGuiUserInterface.instance.raycaster;
		rc.setPlayerPos(newTile.x, newTile.y, facing);
		maze.incTurn(true);
		maze.encounterTile(newTile, oldTile, facing);
	}

	/*-------------------------------------------------------------------------*/
	private void movePlayer(CrusaderEngine.PlayerStatus playerStatus, Point oldTile)
	{
		Maze.getPerfLog().enter("MazeActionListener::movePlayer");

		DiyGuiUserInterface.instance.raycaster.handleKey(playerStatus);
		Maze.getInstance().incTurn(true);
		Point newTile = DiyGuiUserInterface.instance.raycaster.getPlayerPos();
		int facing = DiyGuiUserInterface.instance.raycaster.getPlayerFacing();
		Maze.getInstance().encounterTile(newTile, oldTile, facing);

		Maze.getPerfLog().exit("MazeActionListener::movePlayer");
	}
}
