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

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import mclachlan.maze.data.StringUtil;
import mclachlan.maze.stat.magic.ValueList;

/**
 *
 */
public class ValueComponent extends JButton implements ActionListener
{
	private ValueList value;
	private int dirtyFlag;

	/*-------------------------------------------------------------------------*/
	public ValueComponent(int dirtyFlag)
	{
		this(null, dirtyFlag);
	}

	/*-------------------------------------------------------------------------*/
	public ValueComponent(ValueList value, int dirtyFlag)
	{
		this.dirtyFlag = dirtyFlag;
		refresh(value);
		addActionListener(this);
	}

	/*-------------------------------------------------------------------------*/
	public Dimension getPreferredSize()
	{
		Dimension d = super.getPreferredSize();
		Dimension result = new Dimension(d);
		result.height -= 5;
		return result;
	}

	/*-------------------------------------------------------------------------*/
	public void refresh(ValueList value)
	{
		this.value = value;
		if (value == null)
		{
			this.setText("null");
		}
		else
		{
			String text = StringUtil.descValue(value);

			this.setToolTipText(text);

			if (text.length() > 10)
			{
				text = text.substring(0, 6)+"...";
			}
			this.setText(text);
		}
	}

	/*-------------------------------------------------------------------------*/
	public ValueList getValue()
	{
		return value;
	}

	/*-------------------------------------------------------------------------*/
	public void setValue(ValueList value)
	{
		this.value = value;
		this.refresh(value);
	}

	/*-------------------------------------------------------------------------*/
	public void actionPerformed(ActionEvent event)
	{
		if (event.getSource() == this)
		{
			ValueListDialog dialog = new ValueListDialog(SwingEditor.instance, this.value);
			ValueList v = dialog.getValue();
			if (v != null)
			{
				if (v.getValues().size()==1 && v.getValues().get(0) == ValueListDialog.NULL_VALUE)
				{
					this.value = null;
				}
				else
				{
					this.value = v;
				}
				refresh(this.value);
				SwingEditor.instance.setDirty(dirtyFlag);
			}
		}
	}
}
