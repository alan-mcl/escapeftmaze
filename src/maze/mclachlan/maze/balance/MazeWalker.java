package mclachlan.maze.balance;

import java.util.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.data.Loader;
import mclachlan.maze.data.Saver;
import mclachlan.maze.data.v1.V1Loader;
import mclachlan.maze.data.v1.V1Saver;
import mclachlan.maze.game.*;
import mclachlan.maze.game.event.ZoneChangeEvent;
import mclachlan.maze.map.Tile;
import mclachlan.maze.map.Zone;

/**
 *
 */
public class MazeWalker
{
	public void walk(Database db)
	{
		List<Listener> listeners = getListeners();
		ZoneScorer zs = new ZoneScorer();
		Zone zone = db.getZone("Gatehouse");
		List<Tile> tiles = zs.getNavigableTiles(zone);

		GameState gs = new GameState(
			zone.getName(),
			new DifficultyLevel(),
			zone.getPlayerOrigin(),
			ZoneChangeEvent.Facing.NORTH,
			0,
			0,
			new ArrayList<String>(),
			0,
			0);

		for (Tile t : tiles)
		{
			gs.setPlayerPos(zone.getPoint(t));

			for (Listener listener : listeners)
			{
				listener.walk(gs);
			}
		}

		// summary
		System.out.println(zone.getName());
		for (Listener listener : listeners)
		{
			System.out.println(listener.describe());
		}
	}

	private List<Listener> getListeners()
	{
		List<Listener> result = new ArrayList<Listener>();

		result.add(new TileCountListener());
		result.add(new EncounterListener());

		return result;
	}


	/*-------------------------------------------------------------------------*/
	public static void main(String[] args) throws Exception
	{
		Loader loader = new V1Loader();
		Saver saver = new V1Saver();
		Database db = new Database(loader, saver, Maze.getStubCampaign());

		new MazeWalker().walk(db);
	}

	/*-------------------------------------------------------------------------*/


	public static abstract class Listener
	{
		public abstract void walk(GameState gs);

		public abstract String describe();
	}
}
