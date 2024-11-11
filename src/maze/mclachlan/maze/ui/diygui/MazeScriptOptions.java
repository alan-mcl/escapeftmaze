package mclachlan.maze.ui.diygui;

import java.util.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.game.MazeScript;
import mclachlan.maze.util.MazeException;

/**
 *
 */
public class MazeScriptOptions implements GeneralOptionsCallback
{
	/**
	 * Map of option to maze script name.
	 */
	private final Map<String, String> options;

	private final boolean forceSelection;

	public MazeScriptOptions(Map<String, String> options, boolean forceSelection)
	{
		this.forceSelection = forceSelection;
		this.options = options;
	}

	@Override
	public List<MazeEvent> optionChosen(String option)
	{
		String scriptName = options.get(option);

		if (scriptName == null)
		{
			if (forceSelection)
			{
				throw new MazeException("invalid option [" + option + "]");
			}
			else
			{
				return null;
			}
		}

		MazeScript mazeScript = Database.getInstance().getMazeScript(scriptName);

		return mazeScript.getEvents();
	}
}
