package mclachlan.maze.data.v2;

import mclachlan.maze.data.Database;

/**
 * Used for migrating the data model: loads with one serialiser, saves with another
 */
public class MigrationSerialiser<E extends V2DataObject> implements V2SerialiserObject<E>
{
	private final V2SerialiserObject<E> oldSerialiser, newSerialiser;

	public MigrationSerialiser(V2SerialiserObject<E> oldSerialiser,
		V2SerialiserObject<E> newSerialiser)
	{
		this.oldSerialiser = oldSerialiser;
		this.newSerialiser = newSerialiser;
	}

	@Override
	public Object toObject(E e, Database db)
	{
		return newSerialiser.toObject(e, db);
	}

	@Override
	public E fromObject(Object obj, Database db)
	{
		return oldSerialiser.fromObject(obj, db);
	}
}
