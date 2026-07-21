package mclachlan.maze.campaign.def.script;

import java.awt.Point;
import java.util.*;
import mclachlan.maze.data.ColdString;
import mclachlan.maze.data.Database;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.map.TileScript;
import mclachlan.maze.map.script.FlavourTextEvent;
import mclachlan.maze.stat.Dice;
import mclachlan.maze.util.MazeException;

/**
 *
 */
public class IchibaLibraryFictionSection extends TileScript
{
	private static final String FICTION_SHARD = "ichiba-library-fiction";
	private static final String RED_GREAVE_PREFIX = "ichiba.library.fiction.red.greave.";

	public IchibaLibraryFictionSection()
	{
	}

	@Override
	public List<MazeEvent> execute(Maze maze, Point tile, Point previousTile,
		int facing)
	{
		FlavourTextEvent intro = new FlavourTextEvent("You inspect the titles on the bookshelf.\n\n" +
			"This appears to be a complete paperback run of unauthorized fantasy (or fan fiction?) " +
			"about a hero called Red Greave, by one 'Dame Eleanor Cross'.");
		FlavourTextEvent fiction = new FlavourTextEvent();
		fiction.setColdStringKey(pickRandomRedGreaveKey(maze));
		return getList(intro, fiction);
	}

	private static String pickRandomRedGreaveKey(Maze maze)
	{
		Map<String, ColdString> shard = Database.getInstance()
			.getTextRepository(maze.getCampaign())
			.getColdShard(FICTION_SHARD);
		if (shard == null)
		{
			throw new MazeException("ColdStrings shard not found [" + FICTION_SHARD + "]");
		}

		List<String> keys = new ArrayList<>();
		for (String key : shard.keySet())
		{
			if (key.startsWith(RED_GREAVE_PREFIX))
			{
				keys.add(key);
			}
		}
		if (keys.isEmpty())
		{
			throw new MazeException("No Red Greave cold strings in shard [" + FICTION_SHARD + "]");
		}

		return keys.get(Dice.nextInt(keys.size()));
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
