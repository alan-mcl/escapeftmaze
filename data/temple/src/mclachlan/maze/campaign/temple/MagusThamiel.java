package mclachlan.maze.campaign.temple;

import java.util.*;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.map.script.FlavourTextEvent;
import mclachlan.maze.stat.npc.ActorsLeaveEvent;
import mclachlan.maze.stat.npc.NpcScript;
import mclachlan.maze.stat.npc.NpcSpeechEvent;

public class MagusThamiel extends NpcScript
{
	@Override
	public List<MazeEvent> preAppearance()
	{
		return super.preAppearance();
	}

	@Override
	public List<MazeEvent> firstGreeting()
	{
		return getList(
			new FlavourTextEvent("A tall magus appears from the shadows near the " +
				"temple door. \n\nHe looks tired and worried."),
			new NpcSpeechEvent("Ah, I'm glad to see you. I expected some heroes to turn up.", this.npc),
			new NpcSpeechEvent("Greetings. I am the Thamiel of the Planes. Dispatched by my " +
				"order of Magi to attend to this boondoggle.", this.npc),
			new NpcSpeechEvent("Alas, my magic does not work inside the temple. I think yours will though.", this.npc),
			new NpcSpeechEvent("You need to bring the divine body parts back to the shrine " +
				"here to revive him. We don't have much time.", this.npc),
			new NpcSpeechEvent("The Giants are following their omens and waiting for nightfall at Wasud's temple before invading" +
				" the halls of heaven.", this.npc),
			new NpcSpeechEvent("Beiweh has slowed the sunset, but she can't postpone it forever.", this.npc),
			new NpcSpeechEvent("I will stay here and maintain the wards that I have set, keeping further " +
				"Giants away from this place.", this.npc),
			new NpcSpeechEvent("I can't do that forever either, but I can hold them long enough.", this.npc),
			new NpcSpeechEvent("Go now, the fate of the civilised planes depends on you!", this.npc),
			new ActorsLeaveEvent());
	}

	@Override
	public List<MazeEvent> friendlyGreeting()
	{
		return getList(
			new NpcSpeechEvent("You don't appear to have all of Wasud's body yet.", npc),
			new NpcSpeechEvent("You must hurry! Night is falling swiftly!", npc),
			new ActorsLeaveEvent()
		);
	}

	@Override
	public List<MazeEvent> neutralGreeting()
	{
		return friendlyGreeting();
	}

	@Override
	public List<MazeEvent> partyLeavesFriendly()
	{
		return super.partyLeavesFriendly();
	}

	@Override
	public List<MazeEvent> partyLeavesNeutral()
	{
		return super.partyLeavesNeutral();
	}
}
