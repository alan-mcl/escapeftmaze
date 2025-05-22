package mclachlan.maze.ui.diygui;

import java.util.*;
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
	private final Map<String, MazeScript> options;

	private final boolean forceSelection;

	public MazeScriptOptions(Map<String, MazeScript> options, boolean forceSelection)
	{
		this.forceSelection = forceSelection;
		this.options = options;
	}

	@Override
	public List<MazeEvent> optionChosen(String option)
	{
		MazeScript script = options.get(option);

		if (script == null)
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

		return script.getEvents();
	}
}
