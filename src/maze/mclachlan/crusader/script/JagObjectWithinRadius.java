package mclachlan.crusader.script;

import java.awt.Point;
import mclachlan.crusader.CrusaderEngine;
import mclachlan.crusader.EngineObject;
import mclachlan.crusader.ObjectScript;

/**
 * A script to move an object around in "jagged" bursts between random points
 * within a given radius.
 */
public class JagObjectWithinRadius extends ObjectScript
{
	// starting x and y of the sprite
	private int originX, originY;

	// max radius of movement
	private final int maxRadius;
	// min duration in ms
	private final int minSpeed;
	// max duration in ms
	private final int maxSpeed;
	// min pause between moves, in ms
	private final int minPause;
	// max pause between moves, in ms
	private final int maxPause;


	// current origin
	private int startX, startY;
	// the current destination
	private int destX, destY;
	// coordinate change per ms
	private double incX, incY;
	// time it should take to get to the next destination, in ms
	private int currentSpeed;
	// nano time value that we are pausing until
	private long pauseUntil;

	// nano time started
	private long started = 0;

	/*-------------------------------------------------------------------------*/
	public JagObjectWithinRadius(
		int maxRadius,
		int minSpeed,
		int maxSpeed,
		int minPause,
		int maxPause
	)
	{
		this.maxRadius = maxRadius;
		this.minSpeed = minSpeed;
		this.maxSpeed = maxSpeed;
		this.minPause = minPause;
		this.maxPause = maxPause;
	}

	/*-------------------------------------------------------------------------*/

	@Override
	public ObjectScript spawnNewInstance(EngineObject object,
		CrusaderEngine engine)
	{
		JagObjectWithinRadius result = new JagObjectWithinRadius(
			maxRadius,
			minSpeed,
			maxSpeed,
			minPause,
			maxPause);

		result.init(object, engine);

		return result;
	}

	/*-------------------------------------------------------------------------*/

	@Override
	public void init(EngineObject obj, CrusaderEngine engine)
	{
		this.originX = obj.getXPos();
		this.originY = obj.getYPos();

		nextRandomDestination(this.originX, this.originY);
	}

	/*-------------------------------------------------------------------------*/
	private void nextRandomDestination(int startX, int startY)
	{
		this.startX = startX;
		this.startY = startY;

		// randomise the next destination
		currentSpeed = minSpeed + (int)(Math.random() * (maxSpeed-minSpeed)); // 500-1500 ms
		destX = this.originX + (int)(Math.random() * maxRadius * 2 - maxRadius);
		destY = this.originY + (int)(Math.random() * maxRadius * 2 - maxRadius);
		pauseUntil = -1;

		incX = 1D * (destX - startX) / currentSpeed;
		incY = 1D * (destY - startY) / currentSpeed;

		started = 0;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public void execute(long framecount, EngineObject obj)
	{
		long nanoTime = System.nanoTime();
		if (started == 0)
		{
			started = nanoTime;
		}
		if (pauseUntil == -1)
		{
			long timePassed = nanoTime - started;

			double dX = incX * timePassed / 1000000D;
			double dY = incY * timePassed / 1000000D;

			double posX = startX + dX;
			double posY = startY + dY;

			obj.setXPos((int)posX);
			obj.setYPos((int)posY);

			if ((timePassed / 1000000) >= currentSpeed)
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

				// we got there. pick another dest, but pause first
				nextRandomDestination(destX, destY);
				int pauseDuration = (int)(minPause + Math.random()*(maxPause-minPause));
				pauseUntil = nanoTime + (pauseDuration * 1000000L);
			}
		}
		else if (nanoTime >= pauseUntil)
		{
			// done pausing
			pauseUntil = -1;
			started = 0;
		}
	}

	@Override
	public Point getCurrentRenderTextureData(EngineObject obj,
		int textureX, int textureY,
		int imageWidth, int imageHeight)

	{
		return null;
	}

	/*-------------------------------------------------------------------------*/

	public int getMaxRadius()
	{
		return maxRadius;
	}

	public int getMinSpeed()
	{
		return minSpeed;
	}

	public int getMaxSpeed()
	{
		return maxSpeed;
	}

	public int getMinPause()
	{
		return minPause;
	}

	public int getMaxPause()
	{
		return maxPause;
	}
}
