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

package mclachlan.maze.data.v1;

import java.util.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.ByteArrayInputStream;

/**
 * Legacy string-format helpers retained for V2 serialisers and editor display.
 */
public class V1Utils
{
	public static String NEWLINE = "\r\n";

	/*-------------------------------------------------------------------------*/
	public static V1List<String> stringList = new V1List<String>()
	{
		public String typeToString(String s)
		{
			return s;
		}

		public String typeFromString(String s)
		{
			return s;
		}
	};

	/*-------------------------------------------------------------------------*/
	public static Properties getProperties(BufferedReader reader)
		throws IOException
	{
		String line;
		StringBuilder s = new StringBuilder();
		line = reader.readLine();
		while (line != null && !line.equals("@"))
		{
			s.append(line).append("\n");
			line = reader.readLine();
		}

		Properties p = new Properties();
		ByteArrayInputStream inStream = new ByteArrayInputStream(s.toString().getBytes());
		p.load(inStream);
		inStream.close();

		return p;
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
