/*
 * Copyright (c) 2011 Alan McLachlan
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

package mclachlan.maze.data.codec;

import java.util.*;

/**
 * Shared string-format helpers for codecs, campaign config, and editor fields.
 */
public class CodecUtils
{
	public static String NEWLINE = "\r\n";

	/*-------------------------------------------------------------------------*/
	public static String stringListToString(List<String> list)
	{
		if (list == null)
		{
			return "";
		}

		StringBuilder sb = new StringBuilder();
		int max = list.size();
		for (int i = 0; i < max; i++)
		{
			sb.append(list.get(i));
			if (i < max - 1)
			{
				sb.append(",");
			}
		}
		return sb.toString();
	}

	public static List<String> stringListFromString(String s)
	{
		if (s == null || s.equals(""))
		{
			return null;
		}

		List<String> result = new ArrayList<>();
		Collections.addAll(result, s.split(",", -1));
		return result;
	}

	/*-------------------------------------------------------------------------*/
	public static String escapeNewlines(String str)
	{
		return str.replaceAll("\n", "%n");
	}

	/*-------------------------------------------------------------------------*/
	public static String replaceNewlines(String str)
	{
		return str.replaceAll("%n", "\n");
	}

	/*-------------------------------------------------------------------------*/
	public static String escapeCommas(String str)
	{
		return str.replaceAll(",", "%c");
	}

	/*-------------------------------------------------------------------------*/
	public static String replaceCommas(String str)
	{
		return str.replaceAll("%c", ",");
	}

	/*-------------------------------------------------------------------------*/
	public static String escapeNewlineaAndCommas(String str)
	{
		return escapeCommas(escapeNewlines(str));
	}

	/*-------------------------------------------------------------------------*/
	public static String replaceNewlineaAndCommas(String str)
	{
		return replaceCommas(replaceNewlines(str));
	}

	/*-------------------------------------------------------------------------*/
	public static String toStringInts(int[] arr, String separator)
	{
		if (arr == null)
		{
			return "";
		}

		StringBuilder s = new StringBuilder();
		for (int i = 0; i < arr.length; i++)
		{
			s.append(arr[i]);
			if (i < arr.length-1)
			{
				s.append(separator);
			}
		}

		return s.toString();
	}

	/*-------------------------------------------------------------------------*/
	public static int[] fromStringInts(String s, String separator)
	{
		if (s.equals(""))
		{
			return null;
		}

		String[] strs = s.split(separator);
		int[] result = new int[strs.length];

		for (int i = 0; i < strs.length; i++)
		{
			result[i] = Integer.parseInt(strs[i]);
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public static String toStringStrings(String[] arr, String separator)
	{
		if (arr == null)
		{
			return "";
		}

		StringBuilder s = new StringBuilder();
		for (int i = 0; i < arr.length; i++)
		{
			s.append(arr[i]);
			if (i < arr.length-1)
			{
				s.append(separator);
			}
		}

		return s.toString();
	}

	/*-------------------------------------------------------------------------*/
	public static String[] fromStringStrings(String s, String separator)
	{
		if (s.equals(""))
		{
			return null;
		}
		
		return s.split(separator);
	}
}
