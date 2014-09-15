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

import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import java.awt.*;
import mclachlan.maze.stat.CurMax;
import mclachlan.maze.stat.CurMaxSub;

/**
 *
 */
public class CurMaxComponent extends JPanel implements ChangeListener
{
	JSpinner cur, max, sub;
	private int dirtyFlag;
	private JLabel subLabel1;
	private JLabel subLabel2;
	private CurMax curMax;

	/*-------------------------------------------------------------------------*/
	public CurMaxComponent(int dirtyFlag, int minValue, int maxValue)
	{
		this.dirtyFlag = dirtyFlag;
		cur = new JSpinner(new SpinnerNumberModel(0, minValue, maxValue, 1));
		max = new JSpinner(new SpinnerNumberModel(0, minValue, maxValue, 1));
		sub = new JSpinner(new SpinnerNumberModel(0, minValue, maxValue, 1));
		
		subLabel1 = new JLabel("(");
		subLabel2 = new JLabel(")");

		cur.addChangeListener(this);
		max.addChangeListener(this);
		sub.addChangeListener(this);
		
		setLayout(new FlowLayout());
		
		add(cur);
		add(new JLabel("/"));
		add(max);
		add(subLabel1);
		add(sub);
		add(subLabel2);
	}
	
	/*-------------------------------------------------------------------------*/
	public void refresh(CurMax cm)
	{
		cur.removeChangeListener(this);
		max.removeChangeListener(this);
		sub.removeChangeListener(this);
		
		cur.setValue(cm.getCurrent());
		max.setValue(cm.getMaximum());
		
		this.curMax = cm;
		
		if (cm instanceof CurMaxSub)
		{
			subLabel1.setVisible(true);
			subLabel2.setVisible(true);
			sub.setVisible(true);
			sub.setValue(((CurMaxSub)cm).getSub());
		}
		else
		{
			subLabel1.setVisible(false);
			subLabel2.setVisible(false);
			sub.setVisible(false);
		}

		cur.addChangeListener(this);
		max.addChangeListener(this);
		sub.addChangeListener(this);
	}
	
	/*-------------------------------------------------------------------------*/
	public CurMax getCurMax()
	{
		if (this.curMax instanceof CurMaxSub)
		{
			return new CurMaxSub((Integer)cur.getValue(), (Integer)max.getValue(), (Integer)sub.getValue());
		}
		else
		{
			return new CurMax((Integer)cur.getValue(), (Integer)max.getValue());
		}
	}

	/*-------------------------------------------------------------------------*/
	public void stateChanged(ChangeEvent e)
	{
		SwingEditor.instance.setDirty(this.dirtyFlag);
	}
}
