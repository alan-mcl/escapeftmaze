package mclachlan.maze.data.v2.serialisers;


import java.util.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.data.v1.V1BitSet;
import mclachlan.maze.data.v2.V2SerialiserObject;

/**
 *
 */
public class BitSetSerialiser implements V2SerialiserObject<BitSet>
{

	@Override
	public Object toObject(BitSet bitSet, Database db)
	{
		if (bitSet == null)
		{
			return null;
		}

		return V1BitSet.toString(bitSet);
	}

	@Override
	public BitSet fromObject(Object obj, Database db)
	{
		if (obj == null)
		{
			return null;
		}

		return V1BitSet.fromString((String)obj);
	}
}
