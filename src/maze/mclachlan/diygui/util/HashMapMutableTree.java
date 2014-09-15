/*
 * Copyright (c) 2014 Alan McLachlan
 *
 * This file is part of Escape From The Maze.
 *
 * Escape From The Maze is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package mclachlan.diygui.util;

import java.util.*;

/**
 * A mutable tree implementation backed by a HashMap that links nodes and
 * parents.
 */
public class HashMapMutableTree<N> implements MutableTree<N>
{
	private final Map<N, N> nodeParent = new HashMap<N, N>();
	private final Set<N> nodeList = new LinkedHashSet<N>();

	/*----------------------------------------------------------------------*/
	public boolean add(N node, N parent)
	{
		boolean added = nodeList.add(node);
		if (parent != null)
		{
			nodeList.add(parent);
		}
		if (added)
		{
			nodeParent.put(node, parent);
		}

		return added;
	}

	/*----------------------------------------------------------------------*/
	public boolean remove(N node)
	{
		if (!nodeList.contains(node))
		{
			return false;
		}
		for (N child : getChildren(node))
		{
			remove(child);
		}
		nodeList.remove(node);
		return true;
	}

	/*----------------------------------------------------------------------*/
	public List<N> getRoots()
	{
		return getChildren(null);
	}

	/*----------------------------------------------------------------------*/
	public N getParent(N node)
	{
		return nodeParent.get(node);
	}

	/*----------------------------------------------------------------------*/
	public List<N> getChildren(N node)
	{
		List<N> children = new ArrayList<N>();

		for (N n : nodeList)
		{
			N parent = nodeParent.get(n);
			if (node == null && parent == null)
			{
				children.add(n);
			}
			else if (node != null && parent != null && parent.equals(node))
			{
				children.add(n);
			}
		}
		return children;
	}

	/*-------------------------------------------------------------------------*/
	public List<N> getSiblings(N node)
	{
		List<N> result = new ArrayList<N>();
		N myParent = nodeParent.get(node);

		for (N n : nodeList)
		{
			N parent = nodeParent.get(n);
			if (myParent == null && parent == null)
			{
				result.add(n);
			}
			else if (myParent != null && parent != null && parent.equals(myParent))
			{
				result.add(n);
			}
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public int getDepth(N node)
	{
		int result = 0;

		N cur = node;

		while (nodeParent.get(cur) != null)
		{
			cur = nodeParent.get(cur);
			result++;
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public int size()
	{
		return nodeList.size();
	}

	/*-------------------------------------------------------------------------*/
	public boolean isEmpty()
	{
		return size()==0;
	}

	/*-------------------------------------------------------------------------*/
	public Set<N> getNodes()
	{
		return Collections.unmodifiableSet(nodeList);
	}

	/*----------------------------------------------------------------------*/
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		dumpNodeStructure(builder, null, "- ");
		return builder.toString();
	}

	/*----------------------------------------------------------------------*/
	private void dumpNodeStructure(StringBuilder builder, N node,
		String prefix)
	{
		if (node != null)
		{
			builder.append(prefix);
			builder.append(node.toString());
			builder.append('\n');
			prefix = "    " + prefix;
		}

		for (N child : getChildren(node))
		{
			dumpNodeStructure(builder, child, prefix);
		}
	}
}
