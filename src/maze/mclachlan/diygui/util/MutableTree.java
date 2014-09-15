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

public interface MutableTree<N>
{
	/**
	 * @return a list of all the roots of the tree. Returns an empty list if
	 * the tree is empty. Never returns null.
	 */
	List<N> getRoots();

	/**
	 * @return the parent of the given node. Returns null if the node is a
	 * root node
	 */
	N getParent(N node);

	/**
	 * @return List of children of the given node. Returns empty if there are
	 * none. Never returns null.
	 */
	List<N> getChildren(N node);

	/**
	 * Adds a given node to the tree
	 * @param parent
	 * 	the parent of this node. Set to null if this node is a root node.
	 * @param node
	 * 	the node to add to the tree
	 * @return
	 * 	true if the tree did not already contain the given element
	 */
	boolean add(N node, N parent);

	/**
	 * Removes the given node and all it's children from the tree
	 * @param node
	 * 	the node to remove
	 * @return
	 * 	true if the tree contained the given node
	 */
	boolean remove(N node);

	/**
	 * @return List of the nodes that have the same parent as the given node.
	 * Returns empty if there are none. Never returns null.
	 */
	List<N> getSiblings(N node);

	/**
	 * @return the length of the path to this node's root. Roots are depth 0.
	 */
	int getDepth(N node);

	/**
	 * @return the size of this tree
	 */
	int size();

	/**
	 * @return true if this tree is empty
	 */
	boolean isEmpty();

	/**
	 * @return all the nodes in this tree, unordered
	 */
	Set<N> getNodes();
}
