/*
 * Copyright (c) 2013 Alan McLachlan
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

package mclachlan.jgpgoap.astar;

/**
 * A node in the graph on which the A* algorithm operates
 */
public class AStarNode<NodeType,EdgeType> implements Comparable<AStarNode>
{
	private NodeType contents;
	private EdgeType edge;
	private AStarNode<NodeType,EdgeType> source;
	private int g;
	private int h;

	/*-------------------------------------------------------------------------*/

	/**
	 * @param contents
	 * 	The contents of this graph node
	 * @param edge
	 * 	The edge that brought us to this graph node
	 * @param source
	 * 	The previous A* node
	 * @param g
	 * 	The accumulated cost of travelling this path in the graph
	 * @param h
	 * 	The estimated cost of travelling the rest of the way from here
	 */
	public AStarNode(NodeType contents, EdgeType edge, AStarNode<NodeType,EdgeType> source, int g, int h)
	{
		this.contents = contents;
		this.edge = edge;
		this.source = source;
		this.g = g;
		this.h = h;
	}

	/*-------------------------------------------------------------------------*/

	/**
	 * The A* algorithm searches for the least cost path. Note how the estimated
	 * future cost plays an important role.
	 */
	public int compareTo(AStarNode other)
	{
		return (this.g + this.h) - (other.g + other.h);
	}

	/*-------------------------------------------------------------------------*/
	public NodeType getContents()
	{
		return contents;
	}

	public void setContents(NodeType contents)
	{
		this.contents = contents;
	}

	public EdgeType getEdge()
	{
		return edge;
	}

	public void setEdge(EdgeType edge)
	{
		this.edge = edge;
	}

	public AStarNode<NodeType, EdgeType> getSource()
	{
		return source;
	}

	public void setSource(AStarNode<NodeType, EdgeType> source)
	{
		this.source = source;
	}

	public int getG()
	{
		return g;
	}

	public void setG(int g)
	{
		this.g = g;
	}

	public int getH()
	{
		return h;
	}

	public void setH(int h)
	{
		this.h = h;
	}

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		sb.append("AStarNode");
		sb.append("{contents=").append(contents);
		sb.append(", edge=").append(edge);
		sb.append(", source=").append(source);
		sb.append(", g=").append(g);
		sb.append(", h=").append(h);
		sb.append('}');
		return sb.toString();
	}
}
