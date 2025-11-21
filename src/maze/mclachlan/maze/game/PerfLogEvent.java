package mclachlan.maze.game;

import java.util.*;
import mclachlan.maze.util.MazeException;

/**
 *
 */
class PerfLogEvent extends MazeEvent
{
	String tag;
	PerfEvent event;

	enum PerfEvent
	{ENTER, EXIT}

	;

	public PerfLogEvent(PerfEvent event, String tag)
	{
		this.tag = tag;
		this.event = event;
	}

	@Override
	public List<MazeEvent> resolve()
	{
		switch (event)
		{
			case ENTER -> Maze.getPerfLog().enter(tag);
			case EXIT -> Maze.getPerfLog().exit(tag);
			default -> throw new MazeException("invalid " + event);
		}
		return null;
	}
}
