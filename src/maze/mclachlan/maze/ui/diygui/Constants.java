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

package mclachlan.maze.ui.diygui;

import java.awt.*;
import java.text.DecimalFormat;

/** 
 * arb gui constants
 */
public class Constants
{
	public static final int MAX_FOE_GROUPS = 10;
	public static final int MAX_PARTY_ALLIES = 6;

	/*-------------------------------------------------------------------------*/
	public static class Format
	{
		public static DecimalFormat decimal = new DecimalFormat();

		static
		{
			decimal.setMaximumFractionDigits(1);
			decimal.setMinimumIntegerDigits(1);
		}

		public static String formatWeight(double grams)
		{
			double kilos = grams / 1000;
			return decimal.format(kilos);
		}
	}

	/*-------------------------------------------------------------------------*/
	public static class Messages
	{
		public static final String BACK_TO_GAME = "BackToGame";
		public static final String DISPOSE_DIALOG = "DisposeDialog";
	}

	/*-------------------------------------------------------------------------*/
	public static class Colour
	{
		public static final Color GOLD = new Color(255, 215, 0);
		public static final Color SILVER = new Color(192, 192, 192);
		public static final Color PURPLE = new Color(128, 0, 128);
		public static final Color LIGHT_BLUE = new Color(93, 131, 253);
		public static final Color BROWN = new Color(165, 42, 42);

		public static final Color LIGHT_GREY = new Color(192, 192, 192);
		public static final Color MED_GREY = new Color(160, 160, 160);
		public static final Color GREY = new Color(128, 128, 128);
		public static final Color DARK_GREY = new Color(64, 64, 64);

		public static final Color COMBAT_RED = Color.RED.brighter();
		public static final Color STEALTH_GREEN = Color.GREEN.darker();
		public static final Color MAGIC_BLUE = new Color(93, 131, 253);
		public static final Color ATTRIBUTES_CYAN = Color.CYAN;
		public static final Color FATIGUE_PINK = new Color(255, 182, 193);
	}

	/*-------------------------------------------------------------------------*/
	public static class Conditions
	{
		public static final String FATIGUE_KO = "FATIGUE_KO";
		public static final String GUARDIAN_ANGEL = "GUARDIAN_ANGEL";
		public static final String RESTING_SLEEP = "RESTING_SLEEP";
		public static final String BERSERK = "BERSERK";
		public static final String PROTECT = "PROTECT";
	}
}
