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
import java.awt.image.BufferedImage;
import mclachlan.diygui.DIYLabel;
import mclachlan.diygui.DIYPanel;
import mclachlan.diygui.toolkit.DIYToolkit;

/**
 *
 */
public class LoadingScreen extends BlockingScreen implements ProgressListenerCallback
{
	private final FilledBarWidget bar;
	private final DIYLabel message;

	/*-------------------------------------------------------------------------*/
	public LoadingScreen(int maxProgress)
	{
		super(
			DIYToolkit.getInstance().getRendererProperties().getImageResource("screen/loading_screen"),
			BlockingScreen.Mode.UNINTERRUPTABLE,
			null);

		DIYPanel panel = new DIYPanel();
		BufferedImage panelBack = DIYToolkit.getInstance().getRendererProperties().getImageResource("screen/loading_screen_panel");
		panel.setBackgroundImage(panelBack);

		panel.setBounds(
			width/2 -panelBack.getWidth()/2,
			height/3*2,
			panelBack.getWidth(),
			panelBack.getHeight());


		bar = new FilledBarWidget(
			panel.x +65,
			panel.y +70,
			panel.width -130,
			26,
			0, maxProgress);
		bar.setCallback(this);
		bar.setBarColour(Constants.Colour.GOLD);

		message = new DIYLabel();
		message.setForegroundColour(Color.WHITE);
		message.setBounds(
			panel.x +210,
			panel.y +28,
			panel.width -420,
			22);

		panel.add(message);
		panel.add(bar);

		this.add(panel);
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
