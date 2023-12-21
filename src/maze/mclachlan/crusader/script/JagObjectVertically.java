package mclachlan.crusader.script;

import java.awt.Point;
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
	private final int minSpeed;
	private final int maxSpeed;

	// min and max pause between moves, in ms
	private final int minPause;
	private final int maxPause;

	// should pause at the top/bottom
	private final boolean pauseTop;
	private final boolean pauseBottom;

	// should always return to top/bottom
	private final boolean homeTop;
	private final boolean homeBottom;


	// volatile

	/** the next offset value at which to reset destination and speed */
	private int nextDest;

	/** current speed */
	private double currentSpeed;

	/** nano time value that we are pausing until */
	private long pauseUntil;

	/** nano time last updated */
	private long lastUpdated;

	/** current direction, up or down*/
	private int direction;

	/** where we started */
	private int startingTextureOffset;

	/*-------------------------------------------------------------------------*/
	public JagObjectVertically(
		int minOffset,
		int maxOffset,
		int minSpeed,
		int maxSpeed,
		int minPause,
		int maxPause,
		boolean pauseTop,
		boolean pauseBottom,
		boolean homeTop,
		boolean homeBottom)
	{
		this.minOffset = minOffset;
		this.maxOffset = maxOffset;
		this.minSpeed = minSpeed;
		this.maxSpeed = maxSpeed;
		this.minPause = minPause;
		this.maxPause = maxPause;
		this.pauseTop = pauseTop;
		this.pauseBottom = pauseBottom;
		this.homeTop = homeTop;
		this.homeBottom = homeBottom;
	}

	@Override
	public ObjectScript spawnNewInstance(EngineObject object,
		CrusaderEngine engine)
	{
		JagObjectVertically result = new JagObjectVertically(
			minOffset, maxOffset, minSpeed, maxSpeed, minPause, maxPause,
			pauseTop, pauseBottom, homeTop, homeBottom);

		result.init(object, engine);

		return result;
	}

	@Override
	public void init(EngineObject obj, CrusaderEngine engine)
	{
//		int startingTextureOffset = (int)(Math.random() * (maxOffset - minOffset));
//		obj.setTextureOffset(startingTextureOffset);
		startingTextureOffset = obj.getTextureOffset();

		resetDestAndSpeed(obj);

		lastUpdated = System.nanoTime();
	}

	/*-------------------------------------------------------------------------*/
	private void resetDestAndSpeed(EngineObject obj)
	{
//		this.nextDest = nextDest == maxOffset ? minOffset : maxOffset;
		if (homeBottom && direction==-1)
		{
			// time to return to the bottom
			this.nextDest = maxOffset;
		}
		else if (homeTop && direction==1)
		{
			// time to return to the top
			this.nextDest = minOffset;
		}
		else
		{
			this.nextDest = (int)(minOffset + Math.random() * (maxOffset - minOffset));
		}

		this.currentSpeed = minSpeed + Math.random() * (maxSpeed - minSpeed);

		if (obj.getTextureOffset() > nextDest)
		{
			direction = -1;
		}
		else
		{
			direction = 1;
		}

		pauseUntil = -1;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public void execute(long framecount, EngineObject obj)
	{
		long nanoTime = System.nanoTime();
		long ms = TimeUnit.MILLISECONDS.convert(nanoTime - lastUpdated, TimeUnit.NANOSECONDS);
		int d = (int)(ms * currentSpeed / 1000) * direction;

		if (pauseUntil == -1)
		{
			if (d != 0)
			{
				obj.setTextureOffset(obj.getTextureOffset() + d);

				if (direction > 0 && obj.getTextureOffset() > nextDest ||
					direction < 0 && obj.getTextureOffset() < nextDest)
				{
					int oldDirection = direction;
					resetDestAndSpeed(obj);

					if (oldDirection < 0 && pauseTop || oldDirection > 0 && pauseBottom)
					{
						int pauseDuration = (int)(minPause + Math.random() * (maxPause - minPause));
						pauseUntil = nanoTime + (pauseDuration * 1000000L);
					}
				}

				lastUpdated = nanoTime;
			}
		}
		else if (nanoTime >= pauseUntil)
		{
			// done pausing
			pauseUntil = -1;

			lastUpdated = nanoTime;
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

	public int getMinPause()
	{
		return minPause;
	}

	public int getMaxPause()
	{
		return maxPause;
	}

	public boolean isPauseTop()
	{
		return pauseTop;
	}

	public boolean isPauseBottom()
	{
		return pauseBottom;
	}

	public boolean isHomeTop()
	{
		return homeTop;
	}

	public boolean isHomeBottom()
	{
		return homeBottom;
	}
}
