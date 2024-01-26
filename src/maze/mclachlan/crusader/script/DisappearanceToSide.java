package mclachlan.crusader.script;

import java.awt.Point;
import mclachlan.crusader.CrusaderEngine;
import mclachlan.crusader.EngineObject;
import mclachlan.crusader.ObjectScript;
import mclachlan.maze.util.MazeException;

/**
 * A script to make an object disappear from the players field of view towards the side
 */
public class DisappearanceToSide extends ObjectScript
{
	private int startX, startY, destX, destY;

	/** coordinate change per ms */
	private double incX, incY;

	/** the direction to appear from */
	private final boolean toLeft;

	/** intended duration of the animation, in ms */
	private final int duration;

	/** nano time started and last updated */
	private long started;
	private CrusaderEngine engine;

	/*-------------------------------------------------------------------------*/
	public DisappearanceToSide(boolean toLeft, int duration)
	{
		this.toLeft = toLeft;
		this.duration = duration;
	}

	@Override
	public ObjectScript spawnNewInstance(EngineObject object, CrusaderEngine engine)
	{
		DisappearanceToSide result = new DisappearanceToSide(toLeft, duration);

		result.init(object, engine);

		return result;
	}

	@Override
	public void init(EngineObject object, CrusaderEngine engine)
	{
		this.startX = object.getXPos();
		this.startY = object.getYPos();

		// todo:
		// work out the distance from the player of the destination x and y
		// int playerX = engine.getPlayerPos().x;
		//	int playerY = engine.getPlayerPos().y;
		// double dfp = Math.sqrt(Math.pow(destX - playerX, 2) + Math.pow(destY - playerY, 2));
		// assuming a 60deg FOV, use the law of sines to work out the distance from
		// a point dfp distance directly in front of the player
//		int dist = (int)(dfp * Math.sin(Math.toRadians(60)) / Math.sin(Math.toRadians(30)));

		this.engine = engine;
		int dist = (int)(this.engine.getTileSize()/3);

		incX = 0;
		incY = 0;

		// todo: only working for DISCRETE mode now
		this.destX = 0;
		this.destY = 0;
		switch (engine.getPlayerFacing())
		{
			case CrusaderEngine.Facing.NORTH:
				destY = startY;
				if (toLeft)
				{
					destX = startX - dist;
					incX = 1D*dist/duration;
				}
				else
				{
					destX = startX + dist;
					incX = -1D*dist/duration;
				}
				break;
			case CrusaderEngine.Facing.SOUTH:
				destY = startY;
				if (toLeft)
				{
					destX = startX + dist;
					incX = -1D*dist/duration;
				}
				else
				{
					destX = startX - dist;
					incX = 1D*dist/duration;
				}
				break;
			case CrusaderEngine.Facing.EAST:
				destX = startX;
				if (toLeft)
				{
					destY = startY - dist;
					incY = 1D*dist/duration;
				}
				else
				{
					destY = startY + dist;
					incY = -1D*dist/duration;
				}
				break;
			case CrusaderEngine.Facing.WEST:
				destX = startX;
				if (toLeft)
				{
					destY = startY + dist;
					incY = -1D*dist/duration;
				}
				else
				{
					destY = startY - dist;
					incY = 1D*dist/duration;
				}
				break;
			default:
				throw new MazeException("invalid facing: "+ engine.getPlayerFacing());
		}

//		object.setXPos(startX);
//		object.setYPos(startY);
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

			engine.removeObject(obj);
		}
	}

	@Override
	public Point getCurrentRenderTextureData(EngineObject obj,
		int textureX, int textureY,
		int imageWidth, int imageHeight)

	{
		return null;
	}
}
