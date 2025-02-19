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
	private int minOffset, maxOffset;

	/** min and max speed of movement, in units of rows per second */
	private int minSpeed;
	private int maxSpeed;

	// min and max pause between moves, in ms
	private int minPause;
	private int maxPause;

	// should pause at the top/bottom
	private boolean pauseTop;
	private boolean pauseBottom;

	// should always return to top/bottom
	private boolean homeTop;
	private boolean homeBottom;


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

	public JagObjectVertically()
	{
	}

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

	public void setMinOffset(int minOffset)
	{
		this.minOffset = minOffset;
	}

	public int getMaxOffset()
	{
		return maxOffset;
	}

	public void setMaxOffset(int maxOffset)
	{
		this.maxOffset = maxOffset;
	}

	public int getMinSpeed()
	{
		return minSpeed;
	}

	public void setMinSpeed(int minSpeed)
	{
		this.minSpeed = minSpeed;
	}

	public int getMaxSpeed()
	{
		return maxSpeed;
	}

	public void setMaxSpeed(int maxSpeed)
	{
		this.maxSpeed = maxSpeed;
	}

	public int getMinPause()
	{
		return minPause;
	}

	public void setMinPause(int minPause)
	{
		this.minPause = minPause;
	}

	public int getMaxPause()
	{
		return maxPause;
	}

	public void setMaxPause(int maxPause)
	{
		this.maxPause = maxPause;
	}

	public boolean isPauseTop()
	{
		return pauseTop;
	}

	public void setPauseTop(boolean pauseTop)
	{
		this.pauseTop = pauseTop;
	}

	public boolean isPauseBottom()
	{
		return pauseBottom;
	}

	public void setPauseBottom(boolean pauseBottom)
	{
		this.pauseBottom = pauseBottom;
	}

	public boolean isHomeTop()
	{
		return homeTop;
	}

	public void setHomeTop(boolean homeTop)
	{
		this.homeTop = homeTop;
	}

	public boolean isHomeBottom()
	{
		return homeBottom;
	}

	public void setHomeBottom(boolean homeBottom)
	{
		this.homeBottom = homeBottom;
	}

	public int getNextDest()
	{
		return nextDest;
	}

	public void setNextDest(int nextDest)
	{
		this.nextDest = nextDest;
	}

	public double getCurrentSpeed()
	{
		return currentSpeed;
	}

	public void setCurrentSpeed(double currentSpeed)
	{
		this.currentSpeed = currentSpeed;
	}

	public long getPauseUntil()
	{
		return pauseUntil;
	}

	public void setPauseUntil(long pauseUntil)
	{
		this.pauseUntil = pauseUntil;
	}

	public long getLastUpdated()
	{
		return lastUpdated;
	}

	public void setLastUpdated(long lastUpdated)
	{
		this.lastUpdated = lastUpdated;
	}

	public int getDirection()
	{
		return direction;
	}

	public void setDirection(int direction)
	{
		this.direction = direction;
	}

	public int getStartingTextureOffset()
	{
		return startingTextureOffset;
	}

	public void setStartingTextureOffset(int startingTextureOffset)
	{
		this.startingTextureOffset = startingTextureOffset;
	}

	/*-------------------------------------------------------------------------*/

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
		{
			return true;
		}
		if (o == null || getClass() != o.getClass())
		{
			return false;
		}

		JagObjectVertically that = (JagObjectVertically)o;

		if (getMinOffset() != that.getMinOffset())
		{
			return false;
		}
		if (getMaxOffset() != that.getMaxOffset())
		{
			return false;
		}
		if (getMinSpeed() != that.getMinSpeed())
		{
			return false;
		}
		if (getMaxSpeed() != that.getMaxSpeed())
		{
			return false;
		}
		if (getMinPause() != that.getMinPause())
		{
			return false;
		}
		if (getMaxPause() != that.getMaxPause())
		{
			return false;
		}
		if (isPauseTop() != that.isPauseTop())
		{
			return false;
		}
		if (isPauseBottom() != that.isPauseBottom())
		{
			return false;
		}
		if (isHomeTop() != that.isHomeTop())
		{
			return false;
		}
		return isHomeBottom() == that.isHomeBottom();
	}

	@Override
	public int hashCode()
	{
		int result = getMinOffset();
		result = 31 * result + getMaxOffset();
		result = 31 * result + getMinSpeed();
		result = 31 * result + getMaxSpeed();
		result = 31 * result + getMinPause();
		result = 31 * result + getMaxPause();
		result = 31 * result + (isPauseTop() ? 1 : 0);
		result = 31 * result + (isPauseBottom() ? 1 : 0);
		result = 31 * result + (isHomeTop() ? 1 : 0);
		result = 31 * result + (isHomeBottom() ? 1 : 0);
		return result;
	}
}
