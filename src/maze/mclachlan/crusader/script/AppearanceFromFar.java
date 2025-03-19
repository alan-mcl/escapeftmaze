package mclachlan.crusader.script;

import java.awt.Point;
import java.util.*;
import mclachlan.crusader.CrusaderEngine;
import mclachlan.crusader.EngineObject;
import mclachlan.crusader.ObjectScript;
import mclachlan.maze.util.MazeException;

/**
 * A script to make an object appear from the side of the players field of view
 */
public class AppearanceFromFar extends ObjectScript
{
	private int startX, startY, destX, destY;

	private final List<ObjectScript> animationScripts;

	/** coordinate change per ms */
	private double incX, incY;

	/** intended duration of the animation, in ms */
	private final int duration;

	/** nano time started and last updated */
	private long started;
	private CrusaderEngine engine;

	/*-------------------------------------------------------------------------*/
	public AppearanceFromFar(int duration, List<ObjectScript> animationScripts)
	{
		this.duration = duration;
		this.animationScripts = animationScripts;
	}

	@Override
	public ObjectScript spawnNewInstance(EngineObject object, CrusaderEngine engine)
	{
		AppearanceFromFar result = new AppearanceFromFar(duration, this.animationScripts);

		result.init(object, engine);

		return result;
	}

	@Override
	public void init(EngineObject object, CrusaderEngine engine)
	{
		this.destX = object.getXPos();
		this.destY = object.getYPos();

		// todo:
		// work out the distance from the player of the destination x and y
		// int playerX = engine.getPlayerPos().x;
		//	int playerY = engine.getPlayerPos().y;
		// double dfp = Math.sqrt(Math.pow(destX - playerX, 2) + Math.pow(destY - playerY, 2));
		// assuming a 60deg FOV, use the law of sines to work out the distance from
		// a point dfp distance directly in front of the player
//		int dist = (int)(dfp * Math.sin(Math.toRadians(60)) / Math.sin(Math.toRadians(30)));

		this.engine = engine;
		int dist = (int)(this.engine.getTileSize() * 3);

		incX = 0;
		incY = 0;

		// todo: only working for DISCRETE mode now
		this.startX = destX;
		this.startY = destY;
		switch (engine.getPlayerFacing())
		{
			case CrusaderEngine.Facing.NORTH ->
			{
				startX = destX;
				startY = Math.max(0, destY - dist);
				incY = 1D * dist / duration;
			}
			case CrusaderEngine.Facing.SOUTH ->
			{
				startX = destX;
				startY = Math.min(engine.getTileSize()*engine.getMapSize().height, destY + dist);
				incY = -1D * dist / duration;
			}
			case CrusaderEngine.Facing.EAST ->
			{
				startY = destY;
				startX = Math.min(engine.getTileSize()*engine.getMapSize().width, destX + dist);
				incX = -1D * dist / duration;
			}
			case CrusaderEngine.Facing.WEST ->
			{
				startY = destY;
				startX = Math.max(0, destX - dist);
				incX = 1D * dist / duration;
			}
			default ->
				throw new MazeException("invalid facing: " + engine.getPlayerFacing());
		}

		object.setXPos(startX);
		object.setYPos(startY);
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

		double posX = startX + dX;
		double posY = startY + dY;

		obj.setXPos((int)posX);
		obj.setYPos((int)posY);

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
