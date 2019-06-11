package mclachlan.maze.campaign.def.zone;

import java.util.*;
import mclachlan.maze.campaign.def.map.EkirthsTombGetselsBoneComb;
import mclachlan.maze.campaign.def.map.EkirthsTombMedallionOfBel;
import mclachlan.maze.campaign.def.map.EkirthsTombWallButton1;
import mclachlan.maze.campaign.def.map.EkirthsTombWallButton2;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.game.MazeVariables;
import mclachlan.maze.map.Zone;
import mclachlan.maze.map.ZoneScript;
import mclachlan.crusader.Wall;

/**
 *
 */
public class EkirthsTomb extends ZoneScript
{
	/*-------------------------------------------------------------------------*/
	public void init(Zone zone, long turnNr)
	{
		checkMazeVars(zone);
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> endOfTurn(Zone zone, long turnNr)
	{
		checkMazeVars(zone);
		return null;
	}

	/*-------------------------------------------------------------------------*/
	private void checkMazeVars(Zone zone)
	{
		// if all the stuff has been done, clear the wall
		if (MazeVariables.getBoolean(EkirthsTombWallButton1.MAZE_VAR) &&
			MazeVariables.getBoolean(EkirthsTombWallButton2.MAZE_VAR) &&
			MazeVariables.getBoolean(EkirthsTombMedallionOfBel.MEDALLION_OF_BEL_USED) &&
			MazeVariables.getBoolean(EkirthsTombGetselsBoneComb.BONE_COMB_USED))
		{
			zone.getMap().getVerticalWalls()[522] =
				new Wall(mclachlan.crusader.Map.NO_WALL, null, false, false, 1, null, null);
		}
	}
}
