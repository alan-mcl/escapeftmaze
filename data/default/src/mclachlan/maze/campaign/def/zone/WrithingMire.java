
package mclachlan.maze.campaign.def.zone;

import java.awt.Point;
import java.util.*;
import mclachlan.crusader.Wall;
import mclachlan.maze.data.Database;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.game.MazeVariables;
import mclachlan.maze.map.Zone;
import mclachlan.maze.map.ZoneScript;

/**
 *
 */
public class WrithingMire extends ZoneScript
{
	static final Point[] horiz_walls =
		{
			new Point(418,450),
			new Point(674,706),
			new Point(586,618),
			new Point(621,653),
			new Point(787,819),
			new Point(698,666),
			new Point(371,403),
			new Point(369,401),
		};

	static final Point[] vert_walls =
		{
			new Point(802,803),
			new Point(841,842),
			new Point(880,882),
			new Point(645,646),
		};

	public static final String MAZE_VAR = "writhing.mire.zone.script.random.seed";

	/*-------------------------------------------------------------------------*/
	public void init(Zone zone, long turnNr)
	{
		long seed;
		if (MazeVariables.get(MAZE_VAR) != null)
		{
			seed = MazeVariables.getLong(MAZE_VAR);
		}
		else
		{
			seed = System.currentTimeMillis();
		}

		setWallStates(zone, seed);
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> endOfTurn(Zone zone, long turnNr)
	{
		if (turnNr % 33 == 0)
		{
			setWallStates(zone, System.currentTimeMillis());
		}
		
		return null;
	}

	/*-------------------------------------------------------------------------*/
	private void setWallStates(Zone zone, long seed)
	{
		Random r = new Random(seed);

		for (int i = 0; i < horiz_walls.length; i++)
		{
			toggleWalls(zone.getMap().getHorizontalWalls(), horiz_walls[i], r.nextBoolean());
		}

		for (int i = 0; i < vert_walls.length; i++)
		{
			toggleWalls(zone.getMap().getVerticalWalls(), vert_walls[i], r.nextBoolean());
		}

		MazeVariables.set(MAZE_VAR, String.valueOf(seed));
	}

	/*-------------------------------------------------------------------------*/
	private void toggleWalls(Wall[] wall_array, Point coords, boolean present)
	{
		toggleWall(present, wall_array, coords.x);
		toggleWall(present, wall_array, coords.y);
	}

	/*-------------------------------------------------------------------------*/
	private void toggleWall(boolean present, Wall[] wall_array, int index)
	{
		if (present)
		{
			wall_array[index] = new Wall(
				Database.getInstance().getMazeTexture("TREE_WALL_1").getTexture(),
				null,
				true,
				true,
				null,
				null);
		}
		else
		{
			wall_array[index] =
				new Wall(mclachlan.crusader.Map.NO_WALL,
					null,
					false,
					false,
					null,
					null);
		}
	}
}
