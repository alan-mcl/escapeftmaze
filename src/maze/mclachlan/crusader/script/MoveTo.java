package mclachlan.crusader.script;

import java.awt.Point;
import java.util.*;
import mclachlan.crusader.CrusaderEngine;
import mclachlan.crusader.EngineObject;
import mclachlan.crusader.ObjectScript;

/**
 * A script to make an object move to a given x, y and texture offset
 */
public class MoveTo extends ObjectScript
{
	private int startX, startY, startOffset, destX, destY, destOffset;

	private final List<ObjectScript> animationScripts;

	/** coordinate change per ms */
	private double incX, incY, incOffset;

	/** intended duration of the animation, in ms */
	private final int duration;

	/** nano time started and last updated */
	private long started;
	private CrusaderEngine engine;

	/*-------------------------------------------------------------------------*/
	public MoveTo(int destX, int destY, int destOffset, int duration, List<ObjectScript> animationScripts)
	{
		this.destX = destX;
		this.destY = destY;
		this.destOffset = destOffset;
		this.duration = duration;
		this.animationScripts = animationScripts;
	}

	@Override
	public ObjectScript spawnNewInstance(EngineObject object, CrusaderEngine engine)
	{
		MoveTo result = new MoveTo(destX, destY, destOffset, duration, this.animationScripts);

		result.init(object, engine);

		return result;
	}

	@Override
	public void init(EngineObject object, CrusaderEngine engine)
	{
		this.startX = object.getXPos();
		this.startY = object.getYPos();
		this.startOffset = object.getTextureOffset();

		// todo:
		// work out the distance from the player of the destination x and y
		// int playerX = engine.getPlayerPos().x;
		//	int playerY = engine.getPlayerPos().y;
		// double dfp = Math.sqrt(Math.pow(destX - playerX, 2) + Math.pow(destY - playerY, 2));
		// assuming a 60deg FOV, use the law of sines to work out the distance from
		// a point dfp distance directly in front of the player
//		int dist = (int)(dfp * Math.sin(Math.toRadians(60)) / Math.sin(Math.toRadians(30)));

		this.engine = engine;

		incX = 1D*(destX-startX)/duration;
		incY = 1D*(destY-startY)/duration;
		incOffset = 1D*(destOffset-startOffset)/duration;
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
		double dOffset = incOffset * timePassed / 1000000D;

		double posX = startX + dX;
		double posY = startY + dY;
		double offset = startOffset + dOffset;

		obj.setXPos((int)posX);
		obj.setYPos((int)posY);
		obj.setTextureOffset((int)offset);

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
			if (incOffset > 0 && offset > destOffset || incOffset < 0 && offset < destOffset)
			{
				obj.setTextureOffset(destOffset);
			}

			obj.removeScript(this);

			if (this.animationScripts != null)
			{
				for (ObjectScript script : this.animationScripts)
				{
					obj.addScript(script.spawnNewInstance(obj, engine));
				}
			}
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
