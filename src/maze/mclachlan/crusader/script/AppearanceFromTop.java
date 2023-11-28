package mclachlan.crusader.script;

import java.util.*;
import mclachlan.crusader.CrusaderEngine;
import mclachlan.crusader.EngineObject;
import mclachlan.crusader.ObjectScript;

/**
 * A script to make an object appear from the side of the players field of view
 */
public class AppearanceFromTop extends ObjectScript
{
	/** intended duration of the animation, in ms */
	private final int duration;

	/** animation scripts for this object to run after appearance */
	private final List<ObjectScript> animationScripts;

	/** starting and destination offsets */
	private int startTextureOffset, destTextureOffset;

	/** coordinate change per ms */
	private double incZ;

	/** nano time started */
	private long started = 0;
	private CrusaderEngine engine;

	/*-------------------------------------------------------------------------*/
	public AppearanceFromTop(int duration, List<ObjectScript> animationScripts)
	{
		this.duration = duration;
		this.animationScripts = animationScripts;
	}

	@Override
	public ObjectScript spawnNewInstance(EngineObject object,
		CrusaderEngine engine)
	{
		AppearanceFromTop result = new AppearanceFromTop(duration, this.animationScripts);

		this.engine = engine;
		result.init(object, this.engine);

		return result;
	}

	@Override
	public void init(EngineObject object, CrusaderEngine engine)
	{
		this.startTextureOffset = object.calcFromTopTextureOffset(engine);
//		this.startTextureOffset -= object.getTextures()[0].imageHeight;

//		this.startTextureOffset = - ((CrusaderEngine32)engine).getProjectionPlaneHeight() / 2;

		this.destTextureOffset = 0;

		incZ = 1D* (destTextureOffset - startTextureOffset) /duration;

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

			if (this.animationScripts != null)
			{
				for (ObjectScript script : this.animationScripts)
				{
					obj.addScript(script.spawnNewInstance(obj, this.engine));
				}
			}
		}
	}
}
