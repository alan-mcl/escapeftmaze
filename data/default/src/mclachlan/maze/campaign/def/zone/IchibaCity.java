package mclachlan.maze.campaign.def.zone;

import java.util.*;
import java.awt.Point;
import mclachlan.maze.campaign.def.map.IchibaGnollEncounters;
import mclachlan.maze.campaign.def.npc.Imogen;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.game.MazeVariables;
import mclachlan.maze.map.DefaultZoneScript;
import mclachlan.maze.map.Zone;
import mclachlan.maze.stat.Dice;

/**
 * Ichiba City zone script: day/night via {@link DefaultZoneScript} fields plus
 * post-quest gnoll ambushes inside the walls.
 */
public class IchibaCity extends DefaultZoneScript
{
	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> endOfTurn(Zone zone, long turnNr)
	{
		List<MazeEvent> result = super.endOfTurn(zone, turnNr);

		if (MazeVariables.getBoolean(Imogen.QUEST_3_COMPLETE))
		{
			if (Maze.getInstance().getCurrentCombat() == null)
			{
				Point pos = Maze.getInstance().getPlayerPos();

				// inside the walls
				if (pos.x >= 5 && pos.x <= 35 && pos.y >= 5 && pos.y <= 34)
				{
					// not imogens tower
					if (!(pos.x >= 12 && pos.x <= 15 && pos.y >= 29 && pos.y <= 33))
					{
						if (Dice.d100.roll("Ichiba city gnoll encounters") <= 5)
						{
							return new IchibaGnollEncounters().getEncounter();
						}
					}
				}
			}
		}

		return result;
	}
}
