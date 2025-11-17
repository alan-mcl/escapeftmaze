package mclachlan.maze.stat.npc;

import mclachlan.maze.data.v1.DataObject;

/**
 *
 */
public class FoeSpeech extends DataObject
{
	private String name;

	private String friendlyGreeting;
	private String neutralGreeting;
	private String friendlyFarewell;
	private String neutralFarewell;
	private NpcSpeech dialog;

	/*-------------------------------------------------------------------------*/

	public FoeSpeech()
	{
	}

	public FoeSpeech(String name, String friendlyGreeting,
		String neutralGreeting,
		String friendlyFarewell, String neutralFarewell, NpcSpeech dialog)
	{
		this.name = name;
		this.friendlyGreeting = friendlyGreeting;
		this.neutralGreeting = neutralGreeting;
		this.friendlyFarewell = friendlyFarewell;
		this.neutralFarewell = neutralFarewell;
		this.dialog = dialog;
	}

	/*-------------------------------------------------------------------------*/

	public String getFriendlyGreeting()
	{
		return friendlyGreeting;
	}

	public void setFriendlyGreeting(String friendlyGreeting)
	{
		this.friendlyGreeting = friendlyGreeting;
	}

	public String getNeutralGreeting()
	{
		return neutralGreeting;
	}

	public void setNeutralGreeting(String neutralGreeting)
	{
		this.neutralGreeting = neutralGreeting;
	}

	public String getFriendlyFarewell()
	{
		return friendlyFarewell;
	}

	public void setFriendlyFarewell(String friendlyFarewell)
	{
		this.friendlyFarewell = friendlyFarewell;
	}

	public String getNeutralFarewell()
	{
		return neutralFarewell;
	}

	public void setNeutralFarewell(String neutralFarewell)
	{
		this.neutralFarewell = neutralFarewell;
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
