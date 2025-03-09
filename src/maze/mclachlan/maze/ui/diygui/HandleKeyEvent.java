package mclachlan.maze.ui.diygui;

import java.awt.Point;
import java.util.*;
import mclachlan.crusader.CrusaderEngine;
import mclachlan.maze.data.Database;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.game.MazeScript;
import mclachlan.maze.game.event.ZoneChangeEvent;
import mclachlan.maze.map.Portal;
import mclachlan.maze.map.Zone;

/**
 *
 */
class HandleKeyEvent extends MazeEvent
{
	private final int crusaderKey;

	/*-------------------------------------------------------------------------*/
	public HandleKeyEvent(int crusaderKey)
	{
		this.crusaderKey = crusaderKey;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public List<MazeEvent> resolve()
	{
		List<MazeEvent> result = new ArrayList<>();

		Point oldTile = DiyGuiUserInterface.instance.raycaster.getPlayerPos();
		int oldFacing = DiyGuiUserInterface.instance.raycaster.getPlayerFacing();

		CrusaderEngine.PlayerStatus playerStatus =
			DiyGuiUserInterface.instance.raycaster.predictKey(crusaderKey);

		if (playerStatus.willPassThroughWall)
		{
			Zone.Vector portalDest = Maze.getInstance().playerAttemptsMoveThroughWall(oldTile, oldFacing);
			if (portalDest != null)
			{
				result.addAll(playerActivatesPortal(
					oldTile,
					portalDest.location,
					portalDest.facing,
					portalDest.portal));
			}
			else
			{
				result.addAll(Maze.getInstance().incTurn(true));
			}
		}
		else
		{
			if (Maze.getInstance().getCurrentCombat() == null)
			{
				result.addAll(movePlayer(playerStatus, oldTile));
			}
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	private List<MazeEvent> playerActivatesPortal(Point oldTile, Point newTile,
		int facing,
		Portal portal)
	{
		List<MazeEvent> result = new ArrayList<>();

		Maze maze = Maze.getInstance();
		Zone oldZone = maze.getCurrentZone();

		if (portal.getMazeScript() != null)
		{
			MazeScript script = Database.getInstance().getMazeScript(portal.getMazeScript());
			result.addAll(script.getEvents());
		}

		for (MazeEvent me : result)
		{
			if (me instanceof ZoneChangeEvent && !(oldZone.getName().equals(((ZoneChangeEvent)me).getZone())))
			{
				// player is going to change zones, we do not need to handle
				// the rest of the key implications
				return result;
			}
		}

		CrusaderEngine rc = DiyGuiUserInterface.instance.raycaster;
		rc.setPlayerPos(newTile.x, newTile.y, facing);

		result.addAll(maze.incTurn(true));
		result.addAll(maze.encounterTile(newTile, oldTile, facing));

		return result;
	}

	/*-------------------------------------------------------------------------*/
	private List<MazeEvent> movePlayer(CrusaderEngine.PlayerStatus playerStatus,
		Point oldTile)
	{
		List<MazeEvent> result = new ArrayList<>();

		Maze.getPerfLog().enter("MazeActionListener::movePlayer");

		DiyGuiUserInterface.instance.raycaster.handleKey(playerStatus);

		Maze maze = Maze.getInstance();
		result.addAll(maze.incTurn(true));
		Point newTile = DiyGuiUserInterface.instance.raycaster.getPlayerPos();
		int facing = DiyGuiUserInterface.instance.raycaster.getPlayerFacing();

		result.addAll(maze.encounterTile(newTile, oldTile, facing));

		Maze.getPerfLog().exit("MazeActionListener::movePlayer");

		return result;
	}
}
