package mclachlan.maze.campaign.def.script;

import java.util.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.map.script.FlavourTextEvent;
import mclachlan.maze.stat.npc.FoeSpeech;
import mclachlan.maze.stat.npc.NpcSpeech;
import mclachlan.maze.util.MazeException;

/**
 * Resolves a library reference lookup against FoeSpeech keyword dialogue.
 */
public class LibraryReferenceResponseEvent extends MazeEvent
{
	static final String FOE_SPEECH_KEY = "ichiba library reference section";
	private static final String COLD_STRING_PREFIX = "ichiba.library.ref.";

	private final String query;

	public LibraryReferenceResponseEvent(String query)
	{
		this.query = query;
	}

	@Override
	public List<MazeEvent> resolve()
	{
		List<MazeEvent> result = new ArrayList<>();

		FoeSpeech foeSpeech = Database.getInstance().getFoeSpeech(FOE_SPEECH_KEY);
		if (foeSpeech == null || foeSpeech.getDialog() == null)
		{
			throw new MazeException("invalid: "+FOE_SPEECH_KEY);
		}

		NpcSpeech dialog = foeSpeech.getDialog();
		String response = dialog.lookupPlayerSentence(query);

		if (response == null || response.isEmpty())
		{
			result.add(new FlavourTextEvent("You search the shelves but don't " +
				"find any useful information."));
		}
		else
		{
			for (String line : response.split("\n"))
			{
				String trimmed = line.trim();
				if (!trimmed.isEmpty())
				{
					result.add(flavourTextForSpeech(trimmed));
				}
			}
		}

		if (!NpcSpeech.sentenceContainsKeywords(query, "bye", "goodbye", "farewell"))
		{
			result.add(new WaitForLibraryReferenceQueryEvent());
		}

		return result;
	}

	private static FlavourTextEvent flavourTextForSpeech(String speech)
	{
		if (speech.startsWith(COLD_STRING_PREFIX))
		{
			FlavourTextEvent event = new FlavourTextEvent();
			event.setColdStringKey(speech);
			return event;
		}
		return new FlavourTextEvent(speech);
	}
}
