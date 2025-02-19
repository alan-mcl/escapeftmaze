package mclachlan.maze.data.v2.serialisers;

import java.awt.Point;
import mclachlan.maze.data.Database;
import mclachlan.maze.data.v1.V1Point;
import mclachlan.maze.data.v2.V2SerialiserObject;

/**
 *
 */
public class PointSerialiser implements V2SerialiserObject<Point>
{
	@Override
	public Object toObject(Point point, Database db)
	{
		return V1Point.toString(point);
	}

	@Override
	public Point fromObject(Object obj, Database db)
	{
		return V1Point.fromString((String)obj);
	}
}
