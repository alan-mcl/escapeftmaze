package mclachlan.crusader.script;

import mclachlan.crusader.EngineObject;
import mclachlan.crusader.ObjectScript;

/**
 * A script to make an object appear from the side of the players field of view
 */
public class AppearanceFromTop extends ObjectScript
{
	private final int startTextureOffset, destTextureOffset;

	// coordinate change per ms
	private final double incZ;

	// intended duration of the animation, in ms
	private final int duration;

	/** nano time started */
	private long started = 0;

	/*-------------------------------------------------------------------------*/
	public AppearanceFromTop(EngineObject object, int duration)
	{

		int dist = object.getTextures()[0].imageHeight;
		this.startTextureOffset = -dist;
		this.destTextureOffset = 0;

		this.duration = duration;

		incZ = 1D*dist/duration;

		object.setTextureOffset(startTextureOffset);
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

		double dZ = incZ * timePassed / 1000000D;

		double posZ = startTextureOffset + dZ;

		obj.setTextureOffset((int)posZ);

		if ((timePassed/1000000) >= duration)
		{
			// we're done

			// correct if we have overshot
			if (posZ > destTextureOffset)
			{
				obj.setTextureOffset(destTextureOffset);
			}

			obj.removeScript(this);
		}
	}
}
