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

import java.awt.Color;
import mclachlan.diygui.DIYLabel;

/**
 *
 */
public class LoadingScreen extends BlockingScreen implements ProgressListenerCallback
{
	private FilledBarWidget bar;
	private DIYLabel message;

	/*-------------------------------------------------------------------------*/
	public LoadingScreen(int maxProgress)
	{
		super("screen/loading_screen", BlockingScreen.Mode.UNINTERRUPTABLE, null);

		bar = new FilledBarWidget(width/2-100, height-100, 200, 20, 0, maxProgress);
		bar.setCallback(this);

		message = new DIYLabel();
		message.setForegroundColour(Color.WHITE);
		message.setBounds(width / 2 - 100, height -100 +20 +2, 200, 20);

		this.add(bar);
		this.add(message);
	}

	/*-------------------------------------------------------------------------*/
	public ProgressListener getProgressListener()
	{
		return bar;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public void callback(int progress)
	{
		// no op
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public void message(String msg)
	{
		message.setText(msg);
	}

	/*-------------------------------------------------------------------------*/

}
