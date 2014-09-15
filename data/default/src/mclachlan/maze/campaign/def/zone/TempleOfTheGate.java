package mclachlan.maze.campaign.def.zone;

import java.util.*;
import mclachlan.maze.campaign.def.map.TempleOfTheGateDragonRod;
import mclachlan.maze.campaign.def.map.TempleOfTheGateGholaCoin;
import mclachlan.maze.campaign.def.map.TempleOfTheGateRagDoll;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.game.MazeVariables;
import mclachlan.maze.map.Portal;
import mclachlan.maze.map.Zone;
import mclachlan.maze.map.ZoneScript;

/**
 *
 */
public class TempleOfTheGate extends ZoneScript
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
		// if all the stuff has been done, unlock the wall
		if (MazeVariables.getBoolean(TempleOfTheGateGholaCoin.GHOLA_COIN_USED) &&
			MazeVariables.getBoolean(TempleOfTheGateRagDoll.RAG_DOLL_USED) &&
			MazeVariables.getBoolean(TempleOfTheGateDragonRod.DRAGON_ROD_USED))
		{
			MazeVariables.set("temple.of.the.gate.portal.quest.portal",
				Portal.State.UNLOCKED);
		}
	}
}