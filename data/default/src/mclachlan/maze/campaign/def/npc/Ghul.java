
package mclachlan.maze.campaign.def.npc;

import java.util.*;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.game.event.CharacterClassKnowledgeEvent;
import mclachlan.maze.map.script.FlavourTextEvent;
import mclachlan.maze.stat.npc.NpcAttacksEvent;
import mclachlan.maze.stat.npc.NpcScript;
import mclachlan.maze.stat.npc.NpcSpeechEvent;

/**
 * Deadly spirit of the Plain Of Pillars
 */
public class Ghul extends NpcScript
{
	public List<MazeEvent> preAppearance()
	{
		return getList(
			new FlavourTextEvent("The stench in the cavern is sickening. Dry " +
				"bones and pertrified corpses litter the floor... though you " +
				"have little time to take it in. A hunched figure shambles " +
				"towards you from one of the dark corners with deceptive speed, " +
				"giggling and wheezing gleefully..."));
	}

	public List<MazeEvent> firstGreeting()
	{
		return getList(
			new NpcSpeechEvent("muuuulgghhhhh...aahhhllgg... hee hee hee hee!!", npc),
			new NpcSpeechEvent("ahhhg... fressssh meeeeat come for Ghuuuul..." +
				" hee hee hee!!", npc),
			new NpcSpeechEvent("mmmhhhghuull... fresshhh meeat and juuuiicy " +
				"bones.... mmmmmghaaarg... come toooo Ghuulll!!!!", npc),
			new CharacterClassKnowledgeEvent("At once the stench of undeath " +
				"fill your nostrils, stronger than you have ever sensed it " +
				"before. This is no ordinary zombie...", "Priest"),
			new NpcAttacksEvent(npc));
	}

	public List<MazeEvent> subsequentGreeting()
	{
		return firstGreeting();
	}

	public List<MazeEvent> neutralGreeting()
	{
		return firstGreeting();
	}
}