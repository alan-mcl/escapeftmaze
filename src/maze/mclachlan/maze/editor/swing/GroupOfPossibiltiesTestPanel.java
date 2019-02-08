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

package mclachlan.maze.editor.swing;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import javax.swing.*;
import mclachlan.maze.stat.GroupOfPossibilities;

/**
 *
 */
public class GroupOfPossibiltiesTestPanel<T> extends JPanel implements ActionListener
{
	private JButton test;

	private JTextArea testResult;
	private GroupOfPossibilities<T> gop;

	/*-------------------------------------------------------------------------*/
	protected GroupOfPossibiltiesTestPanel(int rows, int cols)
	{
		testResult = new JTextArea(rows, cols);
		testResult.setWrapStyleWord(true);
		testResult.setLineWrap(true);
		testResult.setBackground(this.getBackground());
		testResult.setEditable(false);

		test = new JButton("Test");
		test.addActionListener(this);

		JPanel buttons = new JPanel();
		buttons.add(test);

		this.setLayout(new BorderLayout(3,3));
		this.add(new JScrollPane(testResult), BorderLayout.CENTER);
		this.add(buttons, BorderLayout.SOUTH);
		this.setBorder(BorderFactory.createEtchedBorder());
	}

	/*-------------------------------------------------------------------------*/
	public void refresh(GroupOfPossibilities<T> gop)
	{
		this.gop = gop;
	}

	/*-------------------------------------------------------------------------*/
	public void test()
	{
		testResult.setText("");
		List<T> random = gop.getRandom();

		StringBuilder sb = new StringBuilder();
		sb.append(random.size()).append(" items:\n\n");

		for (T t : random)
		{
			sb.append(toString(t)).append("\n");
		}

		testResult.setText(sb.toString());
	}

	/*-------------------------------------------------------------------------*/
	protected String toString(T t)
	{
		return t.toString();
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == test)
		{
			test();
		}
	}
}
