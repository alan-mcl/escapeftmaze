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
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.*;
import javax.swing.*;
import mclachlan.maze.data.v1.V1Utils;
import mclachlan.maze.stat.npc.NpcSpeech;
import mclachlan.maze.stat.npc.NpcSpeechRow;

/**
 *
 */
public class NpcSpeechPanel extends JPanel implements ActionListener, MouseListener
{
	private List<NpcSpeechRow> speech;
	private JList speechRows;
	private NpcSpeechPanel.NpcSpeechListModel dataModel;
	private JButton add, remove, edit, quickFill, clear;
	private int dirtyFlag;

	/*-------------------------------------------------------------------------*/
	public NpcSpeechPanel(int dirtyFlag)
	{
		this.dirtyFlag = dirtyFlag;
		setBorder(BorderFactory.createEmptyBorder(3,3,3,3));
		setLayout(new BorderLayout(5,5));

		dataModel = new NpcSpeechListModel();
		speechRows = new JList(dataModel);
		speechRows.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		speechRows.setSize(200,400);
		speechRows.setMaximumSize(new Dimension(300, 400));
		speechRows.addMouseListener(this);

		add = new JButton("Add");
		add.addActionListener(this);

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
	public static String getKey(Set<String> keywords)
	{
		String[] arr = keywords.toArray(new String[keywords.size()]);
		Arrays.sort(arr);
		return V1Utils.toStringStrings(arr,",");
	}

	/*-------------------------------------------------------------------------*/
	public void refresh(NpcSpeech npcSpeech)
	{
		speech = new ArrayList<NpcSpeechRow>();

		if (npcSpeech != null)
		{
			for (NpcSpeechRow row : npcSpeech.getDialogue())
			{
				speech.add(row);
			}

			Collections.sort(speech, Comparator.comparing(o -> getKey(o.getKeywords())));
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
		speech.clear();
		dataModel.refresh();
	}

	/*-------------------------------------------------------------------------*/
	private void removeListItem()
	{
		int index = speechRows.getSelectedIndex();
		if (dataModel.getSize() > 0 && index > -1)
		{
			speech.remove(index);
			dataModel.refresh();
		}
	}

	/*-------------------------------------------------------------------------*/
	private void addListItem()
	{
		NpcSpeechRow row = new NpcSpeechRow(0, new HashSet<String>(), "");
		NpcSpeechRowEditor dialog = new NpcSpeechRowEditor(SwingEditor.instance, row);
		if (dialog.getResult() != null)
		{
			SwingEditor.instance.setDirty(dirtyFlag);
			speech.add(dialog.getResult());
			dataModel.refresh();
		}
	}

	/*-------------------------------------------------------------------------*/
	private void editListItem()
	{
		int index = speechRows.getSelectedIndex();
		if (dataModel.getSize() > 0 && index > -1)
		{
			NpcSpeechRow row = speech.get(index);
			NpcSpeechRowEditor dialog = new NpcSpeechRowEditor(SwingEditor.instance, row);
			if (dialog.getResult() != null)
			{
				SwingEditor.instance.setDirty(dirtyFlag);
				speech.set(index, dialog.getResult());
				dataModel.refresh();
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	private void quickFill()
	{
		addSpeech("maze");
		addSpeech("escape, freedom");
		addSpeech("rumour,rumours,rumors,rumors");
		addSpeech("hello, hi, greetings, salutations");
		addSpeech("bye, goodbye, farewell");
		addSpeech("fuck, fuck you");
		addSpeech("you, yourself, thou, thee");
		addSpeech("thank you, thanks");

		addSpeech("first realm");
		addSpeech("second realm");
		addSpeech("third realm");
		addSpeech("ichiba");
		addSpeech("aenen");
		addSpeech("danaos");
		addSpeech("gate");
		addSpeech("dalen");
		addSpeech("faerie");
		addSpeech("wastes, wastelands, wilds");
		addSpeech("hail");

		addSpeech("agenor");
		addSpeech("asius");
		addSpeech("belisarius");
		addSpeech("broken fang");
		addSpeech("diomedes");
		addSpeech("emmons");
		addSpeech("fangorn");
		addSpeech("glaucus");
		addSpeech("imogen");
		addSpeech("lorelei");
		addSpeech("mentes");
		addSpeech("mnesus");
		addSpeech("red ear");
		addSpeech("sarpedon");
		addSpeech("scrymgeour");
		addSpeech("stenelaus");
		addSpeech("sir kay,kay");
		addSpeech("stentor");
		addSpeech("three eyes");
		addSpeech("we pickett, pickett");
		addSpeech("maze master");

		addSpeech("gnomes, gnome");
		addSpeech("gnolls, gnoll");
		addSpeech("leonals, white order, leonal");
		addSpeech("chamber of commerce, coc, c.o.c.");
		addSpeech("thieves, gentlemen's social club, thieves guild");

		addSpeech("lady");
		addSpeech("garret");
		addSpeech("wasid");
		addSpeech("aello");
		addSpeech("beiweh");
		addSpeech("nergal");

		addSpeech("trade,deal,deals,sell,buy");
		addSpeech("quest,quests");

		dataModel.refresh();
		SwingEditor.instance.setDirty(dirtyFlag);
	}

	/*-------------------------------------------------------------------------*/
	private void addSpeech(String s)
	{
		NpcSpeechRow row = getNpcSpeechRow(s);

		// manual contains
		for (NpcSpeechRow r : speech)
		{
			if (r.getKeywords().equals(getSet(s)))
			{
				return;
			}
		}
		
		speech.add(row);
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @param keywords
	 * 	comma separated list of keywords
	 */
	private NpcSpeechRow getNpcSpeechRow(String keywords)
	{
		return new NpcSpeechRow(9, getSet(keywords), "");
	}

	/*-------------------------------------------------------------------------*/
	private Set<String> getSet(String keywords)
	{
		HashSet<String> result = new HashSet<String>();

		String[] arr = keywords.split(",");
		for (String s : arr)
		{
			result.add(s.trim());
		}

		return result;
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
	public NpcSpeech getDialogue()
	{
		NpcSpeech result = new NpcSpeech();

		for (NpcSpeechRow row : speech)
		{
			result.addNpcSpeechRow(row);
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	private class NpcSpeechListModel extends AbstractListModel
	{
		public int getSize()
		{
			return speech.size();
		}

		public Object getElementAt(int index)
		{
			NpcSpeechRow row = speech.get(index);

			StringBuilder b = new StringBuilder();

			b.append('(');
			b.append(row.getPriority());
			b.append(')');
			b.append(" [");
			for (String s : row.getKeywords())
			{
				b.append(s);
				b.append(",");
			}
			b.deleteCharAt(b.length()-1);
			b.append("] - ");
			b.append(row.getSpeech());

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
			fireContentsChanged(this, 0, speech.size());
		}
	}
}
