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
public class IchibaLibraryReferenceSection extends TileScript
{
	public IchibaLibraryReferenceSection()
	{
	}

	@Override
	public List<MazeEvent> execute(Maze maze, Point tile, Point previousTile,
		int facing)
	{
		FlavourTextEvent txt1 = new FlavourTextEvent("Shelves in the Reference " +
			"Section are stacked with a bewildering profusion of volumes covering " +
			"a comprehensive range of subjects.");
		FlavourTextEvent txt2 = new FlavourTextEvent("They look well organised, " +
			"but you're going to need look for something specific to make progress here.");

		FlavourTextEvent sample = new FlavourTextEvent();
		sample.setColdStringKey("ichiba.library.ref.sample");
		return getList(txt1, txt2, sample);
	}

	/*-------------------------------------------------------------------------*/
	protected IchibaLibraryReferenceSection(IchibaLibraryReferenceSection copy)
	{
		super(copy);
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public TileScript copyScript()
	{
		return new IchibaLibraryReferenceSection(this);
	}

}
