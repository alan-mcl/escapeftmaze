package mclachlan.crusader.script;

import java.awt.Point;
import mclachlan.crusader.CrusaderEngine;
import mclachlan.crusader.EngineObject;
import mclachlan.crusader.ObjectScript;

/**
 *
 */
public class SinusoidalStretch extends ObjectScript
{
	/** speed */
	private double speed;
	/** max stretch as a faction of the original image size i.e. 1.5 = 50% beyond base image width */
	private double maxStretch;
	/** min stretch as a faction of the original image size i.e. 0.5 = 50% of base image width */
	private double minStretch;
	private boolean vertical;
	private boolean horizontal;

	/** current stretch fraction*/
	private double currentStretch;
	/** nano time started */
	private long started = 0;
	/** random starting offset, in radians */
	private double startingOffset;
	/** midpoint of the stretch */
	private double halfStretch;

	public SinusoidalStretch()
	{
	}

	/*-------------------------------------------------------------------------*/
	public SinusoidalStretch(
		double speed,
		double minStretch,
		double maxStretch,
		boolean vertical,
		boolean horizontal)
	{
		this.speed = speed;
		this.minStretch = minStretch;
		this.maxStretch = maxStretch;
		this.vertical = vertical;
		this.horizontal = horizontal;
	}

	@Override
	public ObjectScript spawnNewInstance(EngineObject object,
		CrusaderEngine engine)
	{
		SinusoidalStretch result = new SinusoidalStretch(speed, minStretch, maxStretch, vertical, horizontal);
		result.init(object, engine);

		return result;
	}

	@Override
	public void init(EngineObject object, CrusaderEngine engine)
	{
		started = 0;

		startingOffset = Math.random()*Math.PI*2;
		halfStretch = (maxStretch-minStretch)/2;

		currentStretch = minStretch + halfStretch + Math.sin(startingOffset)*(halfStretch);
	}

	@Override
	public void execute(long framecount, EngineObject obj)
	{
		long nanoTime = System.nanoTime();
		if (started == 0)
		{
			started = nanoTime;
		}
		double ms = (nanoTime - started) / 1_000_000_000D;

		currentStretch = minStretch + halfStretch + Math.sin(startingOffset + ms*speed)*(halfStretch);
	}

	@Override
	public Point getCurrentRenderTextureData(EngineObject obj,
		int textureX, int textureY,
		int imageWidth, int imageHeight)
	{
		if (horizontal)
		{
			double xOffset = (textureX - imageWidth / 2D) * (currentStretch - 1.0);
			textureX = (int)(textureX + xOffset);
		}

		if (vertical)
		{
			double yOffset = (textureY - imageHeight / 2D) * (currentStretch - 1.0);
			textureY = (int)(textureY + yOffset);
		}

		return new Point(textureX, textureY);
	}

	/*-------------------------------------------------------------------------*/

	public double getSpeed()
	{
		return speed;
	}

	public double getMaxStretch()
	{
		return maxStretch;
	}

	public double getMinStretch()
	{
		return minStretch;
	}

	public boolean isVertical()
	{
		return vertical;
	}

	public boolean isHorizontal()
	{
		return horizontal;
	}

	public void setSpeed(double speed)
	{
		this.speed = speed;
	}

	public void setMaxStretch(double maxStretch)
	{
		this.maxStretch = maxStretch;
	}

	public void setMinStretch(double minStretch)
	{
		this.minStretch = minStretch;
	}

	public void setVertical(boolean vertical)
	{
		this.vertical = vertical;
	}

	public void setHorizontal(boolean horizontal)
	{
		this.horizontal = horizontal;
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

		SinusoidalStretch that = (SinusoidalStretch)o;

		if (Double.compare(that.getSpeed(), getSpeed()) != 0)
		{
			return false;
		}
		if (Double.compare(that.getMaxStretch(), getMaxStretch()) != 0)
		{
			return false;
		}
		if (Double.compare(that.getMinStretch(), getMinStretch()) != 0)
		{
			return false;
		}
		if (isVertical() != that.isVertical())
		{
			return false;
		}
		return isHorizontal() == that.isHorizontal();
	}

	@Override
	public int hashCode()
	{
		int result;
		long temp;
		temp = Double.doubleToLongBits(getSpeed());
		result = (int)(temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(getMaxStretch());
		result = 31 * result + (int)(temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(getMinStretch());
		result = 31 * result + (int)(temp ^ (temp >>> 32));
		result = 31 * result + (isVertical() ? 1 : 0);
		result = 31 * result + (isHorizontal() ? 1 : 0);
		return result;
	}
}
