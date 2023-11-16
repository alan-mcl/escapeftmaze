package mclachlan.crusader.script;

import mclachlan.crusader.EngineObject;
import mclachlan.crusader.Map;
import mclachlan.crusader.MapScript;

/**
 *
 */
public class MoveObjectScript extends MapScript
{
	EngineObject engineObject;
	double radius, speed;
	double startingAngle;
	private static final double MAX_RADIANS = Math.PI * 2;

	int startingX, startingY;

	public MoveObjectScript(EngineObject object, double radius, double speed)
	{
		engineObject = object;
		this.radius = radius;
		this.speed = speed;

		this.startingX = engineObject.getXPos();
		this.startingY = engineObject.getYPos();

		// random starting angle. 360deg = pi*2.
		startingAngle = Math.random() * MAX_RADIANS;
	}

	@Override
	public void execute(long framecount, Map map)
	{
		double angle = startingAngle + ((speed * framecount) % MAX_RADIANS);

		int xPos = startingX + (int)(radius * Math.cos(angle));
		int yPos = startingY + (int)(radius * Math.sin(angle));

		engineObject.setXPos(xPos);
		engineObject.setYPos(yPos);
	}
}
