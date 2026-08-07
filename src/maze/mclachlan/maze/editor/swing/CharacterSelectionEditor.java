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
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import mclachlan.maze.stat.CharacterSelection;
import mclachlan.maze.stat.CharacterSelectionMethod;

/**
 * Dialog editor for a {@link CharacterSelection}.
 */
public class CharacterSelectionEditor extends JDialog implements ActionListener
{
	private CharacterSelection result;
	private final int dirtyFlag;

	private CharacterSelectionMethodListPanel methodsPanel;
	private CharacterSelectionMethodListPanel exclusionsPanel;
	private JButton ok, cancel, clear;

	/*-------------------------------------------------------------------------*/
	public CharacterSelectionEditor(Frame owner, CharacterSelection selection, int dirtyFlag)
	{
		super(owner, "Edit Character Selection", true);
		this.dirtyFlag = dirtyFlag;

		methodsPanel = new CharacterSelectionMethodListPanel("Methods", true, dirtyFlag);
		exclusionsPanel = new CharacterSelectionMethodListPanel("Exclusions", false, dirtyFlag);

		ok = new JButton("OK");
		ok.addActionListener(this);
		cancel = new JButton("Cancel");
		cancel.addActionListener(this);
		clear = new JButton("Clear");
		clear.addActionListener(this);

		JPanel buttons = new JPanel();
		buttons.add(ok);
		buttons.add(cancel);
		buttons.add(clear);

		JPanel center = new JPanel(new GridLayout(1, 2, 6, 6));
		center.add(methodsPanel);
		center.add(exclusionsPanel);

		setLayout(new BorderLayout(3, 3));
		add(center, BorderLayout.CENTER);
		add(buttons, BorderLayout.SOUTH);

		if (selection != null)
		{
			methodsPanel.refresh(selection.getMethods());
			exclusionsPanel.refresh(selection.getExclusions());
		}

		pack();
		setLocationRelativeTo(owner);
		setVisible(true);
	}

	/*-------------------------------------------------------------------------*/
	public CharacterSelection getResult()
	{
		return result;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == ok)
		{
			result = new CharacterSelection(
				methodsPanel.getMethods(),
				exclusionsPanel.getMethods());
			setVisible(false);
		}
		else if (e.getSource() == cancel)
		{
			result = null;
			setVisible(false);
		}
		else if (e.getSource() == clear)
		{
			methodsPanel.refresh(null);
			exclusionsPanel.refresh(null);
			if (SwingEditor.instance != null)
			{
				SwingEditor.instance.setDirty(dirtyFlag);
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	static class CharacterSelectionMethodListPanel extends JPanel
		implements ActionListener, MouseListener
	{
		private final DefaultListModel<CharacterSelectionMethod> dataModel =
			new DefaultListModel<>();
		private final JList<CharacterSelectionMethod> list;
		private final boolean allowPlayer;
		private final int dirtyFlag;
		private final JButton add, remove, edit, moveUp, moveDown;

		CharacterSelectionMethodListPanel(String title, boolean allowPlayer, int dirtyFlag)
		{
			this.allowPlayer = allowPlayer;
			this.dirtyFlag = dirtyFlag;

			list = new JList<>(dataModel);
			list.addMouseListener(this);

			add = new JButton("Add");
			add.addActionListener(this);
			edit = new JButton("Edit");
			edit.addActionListener(this);
			remove = new JButton("Remove");
			remove.addActionListener(this);
			moveUp = new JButton("Move Up");
			moveUp.addActionListener(this);
			moveDown = new JButton("Move Down");
			moveDown.addActionListener(this);

			JPanel buttons = new JPanel(new GridLayout(5, 1, 3, 3));
			buttons.add(add);
			buttons.add(edit);
			buttons.add(remove);
			buttons.add(moveUp);
			buttons.add(moveDown);

			setLayout(new BorderLayout(3, 3));
			add(new JScrollPane(list), BorderLayout.CENTER);
			add(buttons, BorderLayout.EAST);
			setBorder(BorderFactory.createTitledBorder(title));
		}

		void refresh(List<CharacterSelectionMethod> methods)
		{
			dataModel.clear();
			if (methods != null)
			{
				for (CharacterSelectionMethod method : methods)
				{
					dataModel.addElement(method);
				}
			}
		}

		List<CharacterSelectionMethod> getMethods()
		{
			List<CharacterSelectionMethod> result = new ArrayList<>();
			for (int i = 0; i < dataModel.size(); i++)
			{
				result.add(dataModel.get(i));
			}
			return result;
		}

		private void setDirty()
		{
			if (SwingEditor.instance != null)
			{
				SwingEditor.instance.setDirty(dirtyFlag);
			}
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			int index = list.getSelectedIndex();
			if (e.getSource() == add)
			{
				CharacterSelectionMethodEditor dialog = new CharacterSelectionMethodEditor(
					SwingEditor.instance, null, allowPlayer, dirtyFlag);
				if (dialog.getResult() != null)
				{
					dataModel.addElement(dialog.getResult());
					list.setSelectedIndex(dataModel.size() - 1);
					setDirty();
				}
			}
			else if (e.getSource() == edit)
			{
				if (index < 0)
				{
					return;
				}
				CharacterSelectionMethodEditor dialog = new CharacterSelectionMethodEditor(
					SwingEditor.instance, dataModel.get(index), allowPlayer, dirtyFlag);
				if (dialog.getResult() != null)
				{
					dataModel.set(index, dialog.getResult());
					setDirty();
				}
			}
			else if (e.getSource() == remove)
			{
				if (index >= 0)
				{
					dataModel.remove(index);
					setDirty();
				}
			}
			else if (e.getSource() == moveUp)
			{
				if (index > 0)
				{
					CharacterSelectionMethod method = dataModel.remove(index);
					dataModel.insertElementAt(method, index - 1);
					list.setSelectedIndex(index - 1);
					setDirty();
				}
			}
			else if (e.getSource() == moveDown)
			{
				if (index >= 0 && index < dataModel.size() - 1)
				{
					CharacterSelectionMethod method = dataModel.remove(index);
					dataModel.insertElementAt(method, index + 1);
					list.setSelectedIndex(index + 1);
					setDirty();
				}
			}
		}

		@Override
		public void mouseClicked(MouseEvent e)
		{
			if (e.getClickCount() == 2 && list.getSelectedIndex() >= 0)
			{
				actionPerformed(new ActionEvent(edit, ActionEvent.ACTION_PERFORMED, "edit"));
			}
		}

		@Override
		public void mousePressed(MouseEvent e) { }

		@Override
		public void mouseReleased(MouseEvent e) { }

		@Override
		public void mouseEntered(MouseEvent e) { }

		@Override
		public void mouseExited(MouseEvent e) { }
	}
}
