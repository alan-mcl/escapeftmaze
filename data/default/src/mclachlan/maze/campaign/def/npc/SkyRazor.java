
package mclachlan.maze.campaign.def.npc;

import java.util.*;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.stat.Item;
import mclachlan.maze.stat.PlayerCharacter;
import mclachlan.maze.stat.npc.ActorsLeaveEvent;
import mclachlan.maze.stat.npc.NpcScript;
import mclachlan.maze.stat.npc.NpcSpeechEvent;
import mclachlan.maze.stat.npc.NpcTakesItemEvent;

public class SkyRazor extends NpcScript
{
	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> givenItemByParty(PlayerCharacter owner, Item item)
	{
		if (item.getName().equals("C.O.C Paper Slip"))
		{
			return getList(
				new NpcSpeechEvent("Hmmm, you're here about the mercenary position.", npc),
				new NpcTakesItemEvent(owner, item, npc),
				new ActorsLeaveEvent());
		}
		else
		{
			return super.givenItemByParty(owner, item);
		}
	}
}