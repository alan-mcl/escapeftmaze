package mclachlan.maze.data.v2.serialisers;

import mclachlan.maze.data.Database;
import mclachlan.maze.data.v2.V2SerialiserObject;
import mclachlan.maze.stat.TypeDescriptor;
import mclachlan.maze.stat.TypeDescriptorImpl;

/**
 *
 */
public class TypeDescriptorSerialiser implements V2SerialiserObject<TypeDescriptor>
{
	@Override
	public Object toObject(TypeDescriptor td, Database db)
	{
		return td == null ? null : td.getName();
	}

	@Override
	public TypeDescriptor fromObject(Object obj, Database db)
	{
		return obj == null ? null : new TypeDescriptorImpl((String)obj);
	}
}
