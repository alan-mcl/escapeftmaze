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
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import mclachlan.maze.game.Campaign;
import java.util.List;
import java.awt.*;

/**
 *
 */
public class CampaignPanel extends JPanel implements ListSelectionListener
{
	JList campaigns;
	JTextArea description;
	private List<Campaign> campaignList;

	/*-------------------------------------------------------------------------*/
	public CampaignPanel(List<Campaign> campaignList)
	{
		super(new BorderLayout(3,3));
		this.campaignList = campaignList;

		campaigns = new JList();
		campaigns.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		campaigns.addListSelectionListener(this);

		description = new JTextArea();
		description.setEditable(false);
		description.setBackground(this.getBackground());
		description.setWrapStyleWord(true);
		description.setLineWrap(true);
		description.setColumns(25);
		description.setRows(25);

		add(new JScrollPane(campaigns), BorderLayout.WEST);
		add(new JScrollPane(description), BorderLayout.CENTER);

		campaigns.setModel(new CampaignListModel(campaignList));
		campaigns.setSelectedIndex(0);
	}

	/*-------------------------------------------------------------------------*/
	public Campaign getCampaign()
	{
		return campaignList.get(campaigns.getSelectedIndex());
	}

	/*-------------------------------------------------------------------------*/
	public void valueChanged(ListSelectionEvent e)
	{
		Campaign c = campaignList.get(campaigns.getSelectedIndex());
		description.setText(c.getDescription());
		description.setCaretPosition(0);
	}

	/*-------------------------------------------------------------------------*/
	static class CampaignListModel extends AbstractListModel
	{
		java.util.List<Campaign> clist;

		public CampaignListModel(java.util.List<Campaign> campaigns)
		{
			this.clist = campaigns;
		}

		public int getSize()
		{
			return clist.size();
		}

		public Object getElementAt(int index)
		{
			return clist.get(index).getDisplayName();
		}
	}

}
