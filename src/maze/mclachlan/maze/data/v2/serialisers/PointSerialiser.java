package mclachlan.maze.data.v2.serialisers;

import java.awt.Point;
import mclachlan.maze.data.Database;
import mclachlan.maze.data.codec.PointCodec;
import mclachlan.maze.data.v2.V2SerialiserObject;

/**
 *
 */
public class PointSerialiser implements V2SerialiserObject<Point>
{
	@Override
	public Object toObject(Point point, Database db)
	{
		return PointCodec.toString(point);
	}

	@Override
	public Point fromObject(Object obj, Database db)
	{
		return PointCodec.fromString((String)obj);
	}
}
