package mclachlan.crusader.script;

import mclachlan.crusader.CrusaderEngine;
import mclachlan.crusader.CrusaderEngine32;
import mclachlan.crusader.EngineObject;
import mclachlan.crusader.ObjectScript;
import mclachlan.maze.util.MazeException;

/**
 * A script to make an object appear from the side of the players field of view
 */
public class AppearanceFromSide extends ObjectScript
{
	private boolean fromLeft;
	private final CrusaderEngine32 engine;

	int startX, startY, destX, destY;

	// coordinate change per ms
	double incX, incY;

	// intended duration of the animation, in ms
	int duration;

	/** nano time started and last updated */
	private long started, lastUpdated;


	public AppearanceFromSide(EngineObject object, boolean fromLeft, int duration, CrusaderEngine32 engine)
	{
		this.fromLeft = fromLeft;
		this.engine = engine;

		this.destX = object.getXPos();
		this.destY = object.getYPos();

		this.duration = duration;

		int playerX = engine.getPlayerPos().x;
		int playerY = engine.getPlayerPos().y;

		double distFromPlayer = Math.sqrt(Math.pow(destX - playerX, 2) + Math.pow(destY - playerY, 2));

		// todo: work this out better
		int dist = (int)(engine.tileSize/3 + distFromPlayer/12);

		incX = 0;
		incY = 0;

		// todo: only working for DISCRETE mode now
		this.startX = destX;
		this.startY = destY;
		switch (engine.getPlayerFacing())
		{
			case CrusaderEngine.Facing.NORTH:
				startY = destY;
				if (fromLeft)
				{
					startX = destX - dist;
					incX = 1D*dist/duration;
				}
				else
				{
					startX = destX + dist;
					incX = -1D*dist/duration;
				}
				break;
			case CrusaderEngine.Facing.SOUTH:
				startY = destY;
				if (fromLeft)
				{
					startX = destX + dist;
					incX = -1D*dist/duration;
				}
				else
				{
					startX = destX - dist;
					incX = 1D*dist/duration;
				}
				break;
			case CrusaderEngine.Facing.EAST:
				startX = destX;
				if (fromLeft)
				{
					startY = destY - dist;
					incY = 1D*dist/duration;
				}
				else
				{
					startY = destY + dist;
					incY = -1D*dist/duration;
				}
				break;
			case CrusaderEngine.Facing.WEST:
				startX = destX;
				if (fromLeft)
				{
					startY = destY + dist;
					incY = -1D*dist/duration;
				}
				else
				{
					startY = destY - dist;
					incY = 1D*dist/duration;
				}
				break;
			default:
				throw new MazeException("invalid facing: "+engine.getPlayerFacing());
		}

		object.setXPos(startX);
		object.setYPos(startY);

		lastUpdated = System.nanoTime();
	}

	@Override
	public void execute(long framecount, EngineObject obj)
	{
		long nanoTime = System.nanoTime();
		if (started == 0)
		{
			started = nanoTime;
		}
		long timePassed = nanoTime - started;

		double dX = incX * timePassed / 1000000D;
		double dY = incY * timePassed / 1000000D;

		double posX = startX + dX;
		double posY = startY + dY;

		obj.setXPos((int)posX);
		obj.setYPos((int)posY);

		if ((timePassed/1000000) >= duration)
		{
			// we're done

			// correct if we have overshot
			if (incX > 0 && posX > destX || incX < 0 && posX < destX)
			{
				obj.setXPos(destX);
			}
			if (incY > 0 && posY > destY || incY < 0 && posY < destY)
			{
				obj.setYPos(destY);
			}

			obj.removeScript(this);
		}

		lastUpdated = System.nanoTime();
	}
}
