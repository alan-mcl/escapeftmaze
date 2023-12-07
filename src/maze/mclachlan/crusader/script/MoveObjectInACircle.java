package mclachlan.crusader.script;

import java.awt.Point;
import mclachlan.crusader.CrusaderEngine;
import mclachlan.crusader.EngineObject;
import mclachlan.crusader.ObjectScript;

/**
 *
 */
public class MoveObjectInACircle extends ObjectScript
{
	double radius, speed;
	double startingAngle;
	private static final double MAX_RADIANS = Math.PI * 2;

	int startingX, startingY;

	public MoveObjectInACircle(double radius, double speed)
	{
		this.radius = radius;
		this.speed = speed;
	}

	@Override
	public ObjectScript spawnNewInstance(EngineObject object,
		CrusaderEngine engine)
	{
		MoveObjectInACircle result = new MoveObjectInACircle(radius, speed);
		result.init(object, engine);
		return result;
	}

	@Override
	public void init(EngineObject object, CrusaderEngine engine)
	{
		this.startingX = object.getXPos();
		this.startingY = object.getYPos();

		// random starting angle. 360deg = pi*2.
		startingAngle = Math.random() * MAX_RADIANS;
	}

	@Override
	public void execute(long framecount, EngineObject obj)
	{
		double angle = startingAngle + ((speed * framecount) % MAX_RADIANS);

		int xPos = startingX + (int)(radius * Math.cos(angle));
		int yPos = startingY + (int)(radius * Math.sin(angle));

		obj.setXPos(xPos);
		obj.setYPos(yPos);
	}

	@Override
	public Point getCurrentRenderTextureData(EngineObject obj,
		int textureX, int textureY,
		int imageWidth, int imageHeight)

	{
		return null;
	}
}
