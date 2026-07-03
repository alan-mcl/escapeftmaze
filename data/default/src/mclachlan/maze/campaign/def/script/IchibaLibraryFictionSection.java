package mclachlan.maze.campaign.def.script;

import java.awt.Point;
import java.util.*;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.map.TileScript;
import mclachlan.maze.map.script.FlavourTextEvent;

/**
 *
 */
public class IchibaLibraryFictionSection extends TileScript
{
	public IchibaLibraryFictionSection()
	{
	}

	@Override
	public List<MazeEvent> execute(Maze maze, Point tile, Point previousTile,
		int facing)
	{
		FlavourTextEvent intro = new FlavourTextEvent("You examine the bookshelves...");
		FlavourTextEvent sample = new FlavourTextEvent();
		sample.setColdStringKey("ichiba.library.fiction.sample");
		return getList(intro, sample);
	}

	/*-------------------------------------------------------------------------*/
	protected IchibaLibraryFictionSection(IchibaLibraryFictionSection copy)
	{
		super(copy);
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public TileScript copyScript()
	{
		return new IchibaLibraryFictionSection(this);
	}

}
