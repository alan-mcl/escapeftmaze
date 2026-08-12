package mclachlan.maze.data.v2.serialisers;


import java.awt.Color;
import mclachlan.maze.data.Database;
import mclachlan.maze.data.codec.ColourCodec;
import mclachlan.maze.data.v2.V2SerialiserObject;

/**
 *
 */
public class ColorSerialiser implements V2SerialiserObject<Color>
{
	@Override
	public Object toObject(Color color, Database db)
	{
		return ColourCodec.toString(color);
	}

	@Override
	public Color fromObject(Object obj, Database db)
	{
		return ColourCodec.fromString((String)obj);
	}
}
