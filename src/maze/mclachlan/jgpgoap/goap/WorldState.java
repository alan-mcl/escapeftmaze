package mclachlan.jgpgoap.goap;

import java.util.*;

/**
 * World state is represented to the planner as a set of atomic facts.
 */
public class WorldState
{
	/**
	 * Explicit true or false values. Not present means indeterminate or "don't care"
	 */
	private Map<String, Atom> values;

	/*-------------------------------------------------------------------------*/

	/**
	 * Constructs a new, empty world state.
	 */
	public WorldState()
	{
	}

	/*-------------------------------------------------------------------------*/

	/**
	 * @param atoms
	 * 	A list of atomic facts that represent this world state
	 */
	public WorldState(Atom... atoms)
	{
		values = new HashMap<String, Atom>();

		for (Atom atom : atoms)
		{
			values.put(atom.getName(), atom);
		}
	}

	/*-------------------------------------------------------------------------*/

	/**
	 * Adds the given atom of state to this world state.
	 */
	public void add(Atom atom)
	{
		values.put(atom.getName(), atom);
	}

	/*-------------------------------------------------------------------------*/

	/**
	 * @return
	 * 	True if this world state meets the preconditions of the given world
	 * 	state. This will be the case if every atom in the given world state (a)
	 * 	exists in this world state and (b) has the same value.
	 */
	public boolean meetsPreCondition(WorldState pre)
	{
		for (Atom atom : pre.values.values())
		{
			if (this.values.containsKey(atom.getName()))
			{
				if (this.values.get(atom.getName()).getValue() != atom.getValue())
				{
					return false;
				}
			}
		}

		return true;
	}

	/*-------------------------------------------------------------------------*/

	/**
	 * @return
	 * 	A world state that represents this world state after applying the given
	 * 	post conditions. Thew new world state will contain a superset of both
	 * 	sets of atoms. Any new or shared atoms will have the value of the
	 * 	post-condition, while any atoms that exist in this world state but not
	 * 	in the post-condition will have unchanged values.
	 */
	public WorldState applyPostCondition(WorldState post)
	{
		WorldState result = new WorldState();

		for (Atom atom : this.values.values())
		{
			result.values.put(atom.getName(), new Atom(atom.getName(), atom.getValue()));
		}

		for (Atom atom : post.values.values())
		{
			result.values.put(atom.getName(), new Atom(atom.getName(), atom.getValue()));
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/

	/**
	 * @return
	 * 	The number of atoms in this world state that either (a) are not
	 * 	present in the other world state or (b) have a different value in the
	 * 	other world state
	 */
	public int countUnmatchedAtoms(WorldState other)
	{
		int result = 0;

		for (Atom atom : other.values.values())
		{
			Atom thisAtom = this.values.get(atom.getName());
			if (thisAtom == null || thisAtom.getValue() != atom.getValue())
			{
				result++;
			}
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public boolean equals(Object o)
	{
		if (this == o)
		{
			return true;
		}
		if (o == null || getClass() != o.getClass())
		{
			return false;
		}

		WorldState that = (WorldState)o;

		if (values != null ? !values.equals(that.values) : that.values != null)
		{
			return false;
		}

		return true;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public int hashCode()
	{
		return values != null ? values.hashCode() : 0;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		sb.append("WS[");
		for (Atom atom : values.values())
		{
			sb.append(atom).append(",");
		}
		sb.append(']');
		return sb.toString();
	}
}
