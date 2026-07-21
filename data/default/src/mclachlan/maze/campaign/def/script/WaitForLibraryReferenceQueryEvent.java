package mclachlan.maze.campaign.def.script;

import java.util.*;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.ui.UserInterface;
import mclachlan.maze.ui.diygui.GetPlayerSpeechDialog;
import mclachlan.maze.ui.diygui.TextDialogCallback;

/**
 * Prompts the player for a library reference lookup query.
 */
public class WaitForLibraryReferenceQueryEvent extends MazeEvent
{
	@Override
	public List<MazeEvent> resolve()
	{
		UserInterface ui = Maze.getInstance().getUi();
		ui.showDialog(new GetPlayerSpeechDialog(
			new TextDialogCallback()
			{
				@Override
				public void textEntered(String text)
				{
					Maze.getInstance().appendEvents(new LibraryReferenceResponseEvent(text));
				}

				@Override
				public void textEntryCancelled()
				{
				}
			},
			"Search reference section for:"));

		return null;
	}
}
