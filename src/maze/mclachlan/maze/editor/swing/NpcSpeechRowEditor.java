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

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import javax.swing.*;
import mclachlan.maze.stat.npc.NpcSpeechRow;

/**
 *
 */
class NpcSpeechRowEditor extends JDialog implements ActionListener
{
	private JButton ok, cancel;
	private JTextField keywords;
	private JSpinner priority;
	private JTextArea speech;
	private NpcSpeechRow result;

	/*-------------------------------------------------------------------------*/
	public NpcSpeechRowEditor(SwingEditor owner, NpcSpeechRow row)
	{
		super(owner, "Edit NPC Speech Row", true);

		JPanel controls = new JPanel();

		ok = new JButton("OK");
		ok.addActionListener(this);
		cancel = new JButton("Cancel");
		cancel.addActionListener(this);

		JPanel buttons = new JPanel();
		buttons.add(ok);
		buttons.add(cancel);

		keywords = new JTextField(50);
		priority = new JSpinner(new SpinnerNumberModel(0, 0, 100, 1));
		speech = new JTextArea(15, 40);
		speech.setWrapStyleWord(true);
		speech.setLineWrap(true);

		dirtyGridLayoutCrap(controls,
			new JLabel("Priority"), priority,
			new JLabel("Keywords:"), keywords,
			new JLabel("Speech:"), new JScrollPane(speech));

		this.setLayout(new BorderLayout(3,3));
		this.add(controls, BorderLayout.CENTER);
		this.add(buttons, BorderLayout.SOUTH);

		priority.setValue(row.getPriority());
		keywords.setText(getKeywordsString(row.getKeywords()));
		speech.setText(row.getSpeech());

		this.pack();
		setLocationRelativeTo(owner);
		this.setVisible(true);
	}

	/*-------------------------------------------------------------------------*/
	private String getKeywordsString(Set<String> keywords)
	{
		StringBuilder b = new StringBuilder();
		for (String s : keywords)
		{
			b.append(s);
			b.append(",");
		}
		if (b.length() > 1)
		{
			b.deleteCharAt(b.length()-1);
		}
		return b.toString();
	}

	/*-------------------------------------------------------------------------*/
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == ok)
		{
			// save changes
			saveResult();
			setVisible(false);

		}
		else if (e.getSource() == cancel)
		{
			setVisible(false);
		}
	}

	/*-------------------------------------------------------------------------*/
	private void saveResult()
	{
		int p = (Integer)(priority.getValue());
		Set<String> keys = new HashSet<String>();
		String[] arr = keywords.getText().split(",");
		for (String s : arr)
		{
			keys.add(s.trim());
		}

		result = new NpcSpeechRow(p, keys, speech.getText());
	}

	/*-------------------------------------------------------------------------*/
	public NpcSpeechRow getResult()
	{
		return result;
	}

	/*-------------------------------------------------------------------------*/
	private void dirtyGridLayoutCrap(JPanel panel, Component... comps)
	{
		panel.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(3,3,3,3);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridheight = 1;
		gbc.gridwidth = 1;
		gbc.weightx = 0;
		gbc.weighty = 0;
		gbc.anchor = GridBagConstraints.NORTHWEST;

		for (int i=0; i<comps.length; i+=2)
		{
			if (i == comps.length-2)
			{
				gbc.weighty = 1;
			}
			gbc.gridx = 0;
			gbc.weightx = 0;
			if (comps[i+1] == null)
			{
				gbc.gridwidth = 2;
				gbc.weightx = 1;
			}
			panel.add(comps[i], gbc);
			gbc.gridx = 1;
			gbc.weightx = 1;
			if (comps[i+1] == null)
			{
				gbc.gridwidth = 1;
			}
			else
			{
				panel.add(comps[i+1], gbc);
			}
			gbc.gridy++;
		}
	}
}
