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


	// current origin
	private int startX, startY;
	// the current destination
	private int destX, destY;
	// coordinate change per ms
	private double incX, incY;
	// time it should take to get to the next destination, in ms
	private int currentDuration;

	/**
	 * nano time started
	 */
	private long started = 0;

	/*-------------------------------------------------------------------------*/
	public JagObjectWithinRadius(int maxRadius)
	{
		this.maxRadius = maxRadius;
	}

	/*-------------------------------------------------------------------------*/

	@Override
	public ObjectScript spawnNewInstance(EngineObject object,
		CrusaderEngine engine)
	{
		JagObjectWithinRadius result = new JagObjectWithinRadius(maxRadius);

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
		currentDuration = (int)(Math.random() * 1000 + 500); // 500-1500 ms
		destX = this.originX + (int)(Math.random() * maxRadius * 2 - maxRadius);
		destY = this.originY + (int)(Math.random() * maxRadius * 2 - maxRadius);

		incX = 1D * (destX - startX) / currentDuration;
		incY = 1D * (destY - startY) / currentDuration;

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
		long timePassed = nanoTime - started;

		double dX = incX * timePassed / 1000000D;
		double dY = incY * timePassed / 1000000D;

		double posX = startX + dX;
		double posY = startY + dY;

		obj.setXPos((int)posX);
		obj.setYPos((int)posY);

		if ((timePassed / 1000000) >= currentDuration)
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

			nextRandomDestination(destX, destY);
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
}
