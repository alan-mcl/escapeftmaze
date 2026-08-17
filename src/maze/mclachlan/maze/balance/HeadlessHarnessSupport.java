/*
 * Copyright (c) 2026 Alan McLachlan
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

package mclachlan.maze.balance;

import java.awt.*;
import java.awt.image.BufferedImage;
import mclachlan.diygui.toolkit.DIYToolkit;
import mclachlan.maze.game.journal.JournalManager;
import mclachlan.maze.stat.npc.NpcManager;

/**
 * Headless support for balance harnesses that need flavour-text dialogs and
 * end-of-turn NPC bookkeeping without a display.
 */
public final class HeadlessHarnessSupport
{
	private static final int WIDTH = 640;
	private static final int HEIGHT = 480;

	private HeadlessHarnessSupport()
	{
	}

	/*-------------------------------------------------------------------------*/
	public static void ensureInitialised()
	{
		if (DIYToolkit.getInstance() == null)
		{
			BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
			Component comp = new Canvas()
			{
				@Override
				public Dimension getSize()
				{
					return new Dimension(WIDTH, HEIGHT);
				}

				@Override
				public Graphics getGraphics()
				{
					return image.getGraphics();
				}
			};
			new DIYToolkit(WIDTH, HEIGHT, comp);
		}

		JournalManager.getInstance().startGame();
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * NPC init needs a party; call once the harness has set one on the maze.
	 */
	public static void ensureNpcManagerStarted()
	{
		if (NpcManager.getInstance().getNpcs() != null)
		{
			return;
		}

		mclachlan.maze.game.Maze maze = mclachlan.maze.game.Maze.getInstance();
		if (maze == null || maze.getParty() == null)
		{
			return;
		}

		NpcManager.getInstance().startGame();
	}

	/*-------------------------------------------------------------------------*/
	public static void prepareMaze(mclachlan.maze.game.Maze maze)
	{
		ensureInitialised();
		maze.initHarnessBookkeepingForTesting();
	}
}
