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
import java.util.Vector;
import java.util.Collections;
import java.util.BitSet;
import java.awt.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.map.Trap;

/**
 *
 */
public class TrapsPanel extends EditorPanel
{
	ThiefToolsPanel tools;
	SingleTileScriptComponent payload;

	/*-------------------------------------------------------------------------*/
	public TrapsPanel()
	{
		super(SwingEditor.Tab.TRAPS);
	}

	/*-------------------------------------------------------------------------*/
	public JPanel getEditControls()
	{
		JPanel result = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = createGridBagConstraints();

		payload = new SingleTileScriptComponent(dirtyFlag, null);
		dodgyGridBagShite(result, new JLabel("Payload:"), payload, gbc);

		tools = new ThiefToolsPanel("Thief Tools To Disarm", dirtyFlag, null);
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.gridx=0;
		gbc.gridy++;
		gbc.gridheight = 1;
		gbc.gridwidth = 2;
		result.add(tools, gbc);

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public Vector loadData()
	{
		Vector<String> vec = new Vector<String>(Database.getInstance().getTraps().keySet());
		Collections.sort(vec);
		return vec;
	}

	/*-------------------------------------------------------------------------*/
	public void refresh(String name)
	{
		Trap trap = Database.getInstance().getTrap(name);
		tools.refresh(trap.getDifficulty(), trap.getRequired());
		payload.refresh(trap.getPayload(), null);
	}

	public void newItem(String name)
	{
		Trap t = new Trap(name, new int[]{Trap.Tool.MAX_TOOLS}, new BitSet(), null);
		Database.getInstance().getTraps().put(name, t);
	}

	public void renameItem(String newName)
	{
		Trap t = Database.getInstance().getTraps().remove(currentName);
		t.setName(newName);
		Database.getInstance().getTraps().put(newName, t);
	}

	public void copyItem(String newName)
	{
		Trap current = Database.getInstance().getTrap(currentName);
		Trap t = new Trap(newName, current.getDifficulty(), current.getRequired(), current.getPayload());
		Database.getInstance().getTraps().put(newName, t);
	}

	public void deleteItem()
	{
		Database.getInstance().getTraps().remove(currentName);
	}

	public void commit(String name)
	{
		Trap trap = Database.getInstance().getTrap(name);

		trap.setDifficulty(tools.getDifficulties());
		trap.setRequired(tools.getRequired());
		trap.setPayload(payload.getScript());
	}
}
