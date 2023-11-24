package mclachlan.crusader.script;

import java.util.concurrent.*;
import mclachlan.crusader.EngineObject;
import mclachlan.crusader.ObjectScript;

/**
 *
 */
public class MoveObjectVerticallyAndHorizontallyScript extends ObjectScript
{
	/** max radius from the center to move the object */
	private final int maxRadius;

	private final EngineObject obj;
	/** min and max vertical offsets, in units of tile_size */
	private final int minOffset;
	private final int maxOffset;

	/** min and max speed of movement, in units of tile_size per second */
	private final int minSpeed, maxSpeed;

	// volatile

	/** the next offset value at which to reset destination and speed */
	private int nextDestX, nextDestY, nextDestZ;

	/** current speed */
	private double currentSpeed;

	/** nano time last updated */
	private long lastUpdated;

	private int direction;

	public MoveObjectVerticallyAndHorizontallyScript(
		EngineObject obj,
		int maxRadius,
		int minOffset,
		int maxOffset,
		int minSpeed,
		int maxSpeed)
	{
		this.obj = obj;
		this.maxRadius = maxRadius;
		this.minOffset = minOffset;
		this.maxOffset = maxOffset;
		this.minSpeed = minSpeed;
		this.maxSpeed = maxSpeed;
		int startingTextureOffset = (int)(Math.random() * (maxOffset - minOffset));
		obj.setTextureOffset(startingTextureOffset);

		resetDestAndSpeed(obj);

		lastUpdated = System.nanoTime();
	}

	private void resetDestAndSpeed(EngineObject obj)
	{
		this.nextDestZ = (int)(minOffset + Math.random() * (maxOffset - minOffset));
//		this.nextDest = nextDest == maxOffset ? minOffset : maxOffset;
		this.currentSpeed = minSpeed + Math.random() * (maxSpeed - minSpeed);

		if (obj.getTextureOffset() > nextDestZ)
		{
			direction = -1;
		}
		else
		{
			direction = 1;
		}
	}

	@Override
	public void execute(long framecount, EngineObject obj)
	{
		long ms = TimeUnit.MILLISECONDS.convert(System.nanoTime() - lastUpdated, TimeUnit.NANOSECONDS);
		int d = (int)(ms * currentSpeed / 1000) * direction;

		if (d != 0)
		{
			obj.setTextureOffset(obj.getTextureOffset() + d);

			if (direction > 0 && obj.getTextureOffset() > nextDestZ ||
				direction < 0 && obj.getTextureOffset() < nextDestZ)
			{
				resetDestAndSpeed(obj);
			}

			lastUpdated = System.nanoTime();
		}
	}
}
