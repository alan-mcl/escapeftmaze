package mclachlan.maze.stat.npc;

import java.util.*;
import mclachlan.maze.data.DataObject;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.game.MazeScript;

/**
 * Configurable interaction hooks for anonymous foes (speech, scripts, simple item gifts).
 */
public class FoeInteraction extends DataObject
{
	private String name;

	private MazeScript friendlyGreeting;
	private MazeScript neutralGreeting;
	private MazeScript friendlyFarewell;
	private MazeScript neutralFarewell;
	private MazeScript attacksParty;
	private String givenItemName;
	private MazeScript givenItemScript;
	private NpcSpeech dialog;

	/*-------------------------------------------------------------------------*/

	public FoeInteraction()
	{
	}

	public FoeInteraction(String name,
		MazeScript friendlyGreeting,
		MazeScript neutralGreeting,
		MazeScript friendlyFarewell,
		MazeScript neutralFarewell,
		MazeScript attacksParty,
		String givenItemName,
		MazeScript givenItemScript,
		NpcSpeech dialog)
	{
		this.name = name;
		this.friendlyGreeting = friendlyGreeting;
		this.neutralGreeting = neutralGreeting;
		this.friendlyFarewell = friendlyFarewell;
		this.neutralFarewell = neutralFarewell;
		this.attacksParty = attacksParty;
		this.givenItemName = givenItemName;
		this.givenItemScript = givenItemScript;
		this.dialog = dialog;
	}

	/*-------------------------------------------------------------------------*/

	public static List<MazeEvent> eventsOrNull(MazeScript script)
	{
		if (script == null || script.getEvents() == null || script.getEvents().isEmpty())
		{
			return null;
		}
		return new ArrayList<>(script.getEvents());
	}

	/*-------------------------------------------------------------------------*/

	public MazeScript getFriendlyGreeting()
	{
		return friendlyGreeting;
	}

	public void setFriendlyGreeting(MazeScript friendlyGreeting)
	{
		this.friendlyGreeting = friendlyGreeting;
	}

	public MazeScript getNeutralGreeting()
	{
		return neutralGreeting;
	}

	public void setNeutralGreeting(MazeScript neutralGreeting)
	{
		this.neutralGreeting = neutralGreeting;
	}

	public MazeScript getFriendlyFarewell()
	{
		return friendlyFarewell;
	}

	public void setFriendlyFarewell(MazeScript friendlyFarewell)
	{
		this.friendlyFarewell = friendlyFarewell;
	}

	public MazeScript getNeutralFarewell()
	{
		return neutralFarewell;
	}

	public void setNeutralFarewell(MazeScript neutralFarewell)
	{
		this.neutralFarewell = neutralFarewell;
	}

	public MazeScript getAttacksParty()
	{
		return attacksParty;
	}

	public void setAttacksParty(MazeScript attacksParty)
	{
		this.attacksParty = attacksParty;
	}

	public String getGivenItemName()
	{
		return givenItemName;
	}

	public void setGivenItemName(String givenItemName)
	{
		this.givenItemName = givenItemName;
	}

	public MazeScript getGivenItemScript()
	{
		return givenItemScript;
	}

	public void setGivenItemScript(MazeScript givenItemScript)
	{
		this.givenItemScript = givenItemScript;
	}

	public NpcSpeech getDialog()
	{
		return dialog;
	}

	public void setDialog(NpcSpeech dialog)
	{
		this.dialog = dialog;
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public void setName(String newName)
	{
		this.name = newName;
	}
}
