package mclachlan.maze.game.journal;

import java.util.*;
import mclachlan.maze.data.StringUtil;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.util.MazeException;

/**
 *
 */
public class JournalEntryEvent extends MazeEvent
{
	private JournalManager.JournalType type;
	private String key, journalText;

	public JournalEntryEvent()
	{
	}

	public JournalEntryEvent(JournalManager.JournalType type, String key,
		String text)
	{
		this.type = type;
		this.key = key;
		this.journalText = text;
	}

	/*-------------------------------------------------------------------------*/

	public JournalManager.JournalType getType()
	{
		return type;
	}

	public void setType(JournalManager.JournalType type)
	{
		this.type = type;
	}

	public String getKey()
	{
		return key;
	}

	public void setKey(String key)
	{
		this.key = key;
	}

	public String getJournalText()
	{
		return journalText;
	}

	public void setJournalText(String text)
	{
		this.journalText = text;
	}

	/*-------------------------------------------------------------------------*/

	@Override
	public List<MazeEvent> resolve()
	{
		switch (type)
		{
			case LOGBOOK ->
			{
				Maze.getInstance().getUi().addMessage(StringUtil.getEventText("msg.journal.logbook"), false);
				JournalManager.getInstance().logbook(journalText);
			}
			case ZONE ->
			{
				JournalManager.getInstance().zoneJournal(journalText);
				Maze.getInstance().getUi().addMessage(StringUtil.getEventText("msg.journal.zone"), false);
			}
			case QUEST ->
			{
				JournalManager.getInstance().questJournal(key, journalText);
				Maze.getInstance().getUi().addMessage(StringUtil.getEventText("msg.journal.quest"), false);
			}
			case NPC ->
			{
				JournalManager.getInstance().npcJournal(journalText);
				Maze.getInstance().getUi().addMessage(StringUtil.getEventText("msg.journal.npc"), false);
			}
			default -> throw new MazeException("Invalid "+type);
		}

		return null;
	}
}
