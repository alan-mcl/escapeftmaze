/*
 * Copyright (c) 2012 Alan McLachlan
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
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.*;
import javax.swing.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.stat.Personality;

/**
 *
 */
public class PersonalitySpeechPanel extends JPanel implements ActionListener, MouseListener
{
	private List<String> speechKeys, speechValues;
	private final JList speechRows;
	private final SpeechListModel dataModel;
	private JButton add;
	private JButton remove;
	private final JButton edit;
	private JButton quickFill;
	private JButton clear;
	private JButton addToAll;
	private final int dirtyFlag;

	/*-------------------------------------------------------------------------*/
	public PersonalitySpeechPanel(int dirtyFlag)
	{
		this.dirtyFlag = dirtyFlag;
		setBorder(BorderFactory.createEtchedBorder());
		setLayout(new BorderLayout(5,5));

		dataModel = new SpeechListModel();
		speechRows = new JList(dataModel);
		speechRows.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		speechRows.setSize(200,400);
		speechRows.setMaximumSize(new Dimension(300, 400));
		speechRows.addMouseListener(this);

		add = new JButton("Add");
		add.addActionListener(this);

		addToAll = new JButton("Add To All");
		addToAll.addActionListener(this);

		remove = new JButton("Remove");
		remove.addActionListener(this);

		edit = new JButton("Edit");
		edit.addActionListener(this);

		quickFill = new JButton("Quick Fill");
		quickFill.addActionListener(this);

		clear = new JButton("Clear");
		clear.addActionListener(this);

		JPanel buttons = new JPanel();
		buttons.add(add);
		buttons.add(addToAll);
		buttons.add(remove);
		buttons.add(edit);
		buttons.add(quickFill);
		buttons.add(clear);

		refresh(null);

		this.add(buttons, BorderLayout.NORTH);
		this.add(new JScrollPane(
			speechRows,
			JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
			JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS), BorderLayout.CENTER);
	}

	/*-------------------------------------------------------------------------*/
	public void refresh(Personality p)
	{
		speechKeys = new ArrayList<>();
		speechValues = new ArrayList<>();

		if (p != null)
		{
			for (String key : p.getSpeech().keySet())
			{
				speechKeys.add(key);
			}

			Collections.sort(speechKeys);

			for (String key : speechKeys)
			{
				speechValues.add(p.getSpeech().get(key));
			}
		}

		dataModel.refresh();
	}

	/*-------------------------------------------------------------------------*/
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == add)
		{
			addListItem();
		}
		if (e.getSource() == addToAll)
		{
			addListItemToAll();
		}
		else if (e.getSource() == edit)
		{
			editListItem();
		}
		else if (e.getSource() == remove)
		{
			removeListItem();
		}
		else if (e.getSource() == quickFill)
		{
			quickFill();
		}
		else if (e.getSource() == clear)
		{
			clear();
		}
	}

	/*-------------------------------------------------------------------------*/
	private void clear()
	{
		SwingEditor.instance.setDirty(dirtyFlag);
		speechKeys.clear();
		speechValues.clear();
		dataModel.refresh();
	}

	/*-------------------------------------------------------------------------*/
	private void removeListItem()
	{
		int index = speechRows.getSelectedIndex();
		if (dataModel.getSize() > 0 && index > -1)
		{
			speechKeys.remove(index);
			speechValues.remove(index);
			dataModel.refresh();
		}
	}

	/*-------------------------------------------------------------------------*/
	private void addListItem()
	{
		PersonalitySpeechRowEditor dialog = new PersonalitySpeechRowEditor(
			SwingEditor.instance,
			"",
			"",
			true);
		if (dialog.getKeyResult() != null)
		{
			SwingEditor.instance.setDirty(dirtyFlag);
			speechKeys.add(dialog.getKeyResult());
			speechValues.add(dialog.getSpeechResult());
			dataModel.refresh();
		}
	}

	/*-------------------------------------------------------------------------*/
	private void addListItemToAll()
	{
		PersonalitySpeechRowEditor dialog = new PersonalitySpeechRowEditor(
			SwingEditor.instance,
			"",
			"",
			false);

		String speechKey = dialog.getKeyResult();

		if (speechKey != null)
		{
			SwingEditor.instance.setDirty(dirtyFlag);

			for (Personality p : Database.getInstance().getPersonalities().values())
			{
				if (!p.getSpeech().containsKey(speechKey))
				{
					p.getSpeech().put(speechKey, "");
				}
			}

			speechKeys.add(speechKey);
			speechValues.add(dialog.getSpeechResult());
			dataModel.refresh();
		}
	}

	/*-------------------------------------------------------------------------*/
	private void editListItem()
	{
		int index = speechRows.getSelectedIndex();
		if (dataModel.getSize() > 0 && index > -1)
		{
			PersonalitySpeechRowEditor dialog = new PersonalitySpeechRowEditor(
				SwingEditor.instance,
				speechKeys.get(index),
				speechValues.get(index),
				true);
			if (dialog.getKeyResult() != null)
			{
				SwingEditor.instance.setDirty(dirtyFlag);
				speechKeys.set(index, dialog.getKeyResult());
				speechValues.set(index, dialog.getSpeechResult());
				dataModel.refresh();
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	private void quickFill()
	{
		for (Personality.BasicSpeech bs : Personality.BasicSpeech.values())
		{
			if (!speechKeys.contains(bs.getKey()))
			{
				speechKeys.add(bs.getKey());
				speechValues.add("");
			}
		}

		for (Personality.DefaultCampaignSpeech dcs :
			Personality.DefaultCampaignSpeech.values())
		{
			if (!speechKeys.contains(dcs.getKey()))
			{
				speechKeys.add(dcs.getKey());
				speechValues.add("");
			}
		}

		dataModel.refresh();
		SwingEditor.instance.setDirty(dirtyFlag);
	}

	/*-------------------------------------------------------------------------*/
	public void mouseClicked(MouseEvent e)
	{
		if (e.getSource() == speechRows)
		{
			if (e.getClickCount() == 2)
			{
				// a double click on a list item, treat as an edit
				SwingEditor.instance.setDirty(dirtyFlag);
				editListItem();
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	public void mousePressed(MouseEvent e)
	{
	}

	/*-------------------------------------------------------------------------*/
	public void mouseReleased(MouseEvent e)
	{
	}

	/*-------------------------------------------------------------------------*/
	public void mouseEntered(MouseEvent e)
	{
	}

	/*-------------------------------------------------------------------------*/
	public void mouseExited(MouseEvent e)
	{
	}

	/*-------------------------------------------------------------------------*/
	public Map<String, String> getSpeech()
	{
		Map<String, String> result = new HashMap<String, String>();

		for (int i=0; i<speechKeys.size(); i++)
		{
			result.put(speechKeys.get(i), speechValues.get(i));
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	private class SpeechListModel extends AbstractListModel
	{
		public int getSize()
		{
			return speechKeys.size();
		}

		public Object getElementAt(int index)
		{
			String key = speechKeys.get(index);
			String value = speechValues.get(index);

			StringBuilder b = new StringBuilder();

			b.append(key);
			b.append(": ");
			b.append(value);

			if (b.length() > 100)
			{
				return b.substring(0,97)+"...";
			}
			else
			{
				return b;
			}
		}

		void refresh()
		{
			fireContentsChanged(this, 0, speechKeys.size());
		}
	}
}
