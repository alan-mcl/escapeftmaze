package mclachlan.crusader.script;

import java.util.concurrent.*;
import mclachlan.crusader.CrusaderEngine;
import mclachlan.crusader.EngineObject;
import mclachlan.crusader.ObjectScript;

/**
 *
 */
public class JagObjectVertically extends ObjectScript
{
	/** min and max offsets, in units of tile_size */
	private final int minOffset, maxOffset;

	/** min and max speed of movement, in units of rows per second */
	private final int minSpeed, maxSpeed;

	// volatile

	/** the next offset value at which to reset destination and speed */
	private int nextDest;

	/** current speed */
	private double currentSpeed;

	/** nano time last updated */
	private long lastUpdated;

	private int direction;

	/*-------------------------------------------------------------------------*/
	public JagObjectVertically(int minOffset, int maxOffset, int minSpeed, int maxSpeed)
	{
		this.minOffset = minOffset;
		this.maxOffset = maxOffset;
		this.minSpeed = minSpeed;
		this.maxSpeed = maxSpeed;
	}

	@Override
	public ObjectScript spawnNewInstance(EngineObject object, CrusaderEngine engine)
	{
		JagObjectVertically result = new JagObjectVertically(minOffset, maxOffset, minSpeed, maxSpeed);

		result.init(object, engine);

		return result;
	}

	@Override
	public void init(EngineObject obj, CrusaderEngine engine)
	{
//		int startingTextureOffset = (int)(Math.random() * (maxOffset - minOffset));
		int startingTextureOffset = obj.getTextureOffset();
		obj.setTextureOffset(startingTextureOffset);

		resetDestAndSpeed(obj);

		lastUpdated = System.nanoTime();
	}

	/*-------------------------------------------------------------------------*/
	private void resetDestAndSpeed(EngineObject obj)
	{
		this.nextDest = (int)(minOffset + Math.random() * (maxOffset - minOffset));
//		this.nextDest = nextDest == maxOffset ? minOffset : maxOffset;
		this.currentSpeed = minSpeed + Math.random() * (maxSpeed - minSpeed);

		if (obj.getTextureOffset() > nextDest)
		{
			direction = -1;
		}
		else
		{
			direction = 1;
		}
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public void execute(long framecount, EngineObject obj)
	{
		long ms = TimeUnit.MILLISECONDS.convert(System.nanoTime() - lastUpdated, TimeUnit.NANOSECONDS);
		int d = (int)(ms * currentSpeed / 1000) * direction;

		if (d != 0)
		{
			obj.setTextureOffset(obj.getTextureOffset() + d);

			if (direction > 0 && obj.getTextureOffset() > nextDest ||
				direction < 0 && obj.getTextureOffset() < nextDest)
			{
				resetDestAndSpeed(obj);
			}

			lastUpdated = System.nanoTime();
		}
	}

	/*-------------------------------------------------------------------------*/

	public int getMinOffset()
	{
		return minOffset;
	}

	public int getMaxOffset()
	{
		return maxOffset;
	}

	public int getMinSpeed()
	{
		return minSpeed;
	}

	public int getMaxSpeed()
	{
		return maxSpeed;
	}
}
