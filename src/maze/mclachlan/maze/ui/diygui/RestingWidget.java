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

import mclachlan.diygui.DIYPane;
import mclachlan.diygui.DIYButton;
import mclachlan.diygui.DIYLabel;
import mclachlan.diygui.toolkit.DIYGridLayout;
import mclachlan.diygui.toolkit.ActionListener;
import mclachlan.diygui.toolkit.ActionEvent;
import java.awt.*;
import mclachlan.maze.game.Maze;

/**
 *
 */
public class RestingWidget extends DIYPane implements ActionListener
{
	private int inset = 4;
	private int buttonHeight = 18;
	private DIYLabel turn;
	DIYButton done;
	private int turnNr=0;

	/*-------------------------------------------------------------------------*/
	public RestingWidget(Rectangle bounds)
	{
		super(bounds);

		done = new DIYButton("(D)one");
		done.addActionListener(this);

		turn = new DIYLabel();

		this.setLayoutManager(new DIYGridLayout(5, height/buttonHeight, inset, inset));

		this.add(new DIYLabel());
		this.add(new DIYLabel("Resting..."));
		this.add(new DIYLabel());
		this.add(turn);
		this.add(new DIYLabel());

		this.add(new DIYLabel());
		this.add(new DIYLabel());
		this.add(done);
		this.add(new DIYLabel());
		this.add(new DIYLabel());

		this.add(new DIYLabel());
		this.add(new DIYLabel());
		this.add(new DIYLabel());
		this.add(new DIYLabel());
		this.add(new DIYLabel());
	}

	/*-------------------------------------------------------------------------*/
	public void start()
	{
		turnNr = 0;
		turn.setText(turnNr+" turns");
	}

	/*-------------------------------------------------------------------------*/
	public void refresh()
	{
		turnNr++;
		turn.setText(turnNr+" turns");
	}

	/*-------------------------------------------------------------------------*/
	public void actionPerformed(ActionEvent event)
	{
		if (event.getSource() == done)
		{
			done();
		}
	}

	/*-------------------------------------------------------------------------*/
	public void done()
	{
		Maze.getInstance().stopResting();
		Maze.getInstance().setState(Maze.State.MOVEMENT);
	}
}
