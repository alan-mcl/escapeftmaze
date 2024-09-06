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

import java.awt.*;
import java.util.List;
import mclachlan.maze.stat.PlayerCharacter;
import mclachlan.maze.stat.PlayerParty;
import mclachlan.diygui.DIYLabel;
import mclachlan.diygui.DIYPane;
import mclachlan.diygui.toolkit.ActionEvent;
import mclachlan.diygui.toolkit.ActionListener;
import mclachlan.diygui.toolkit.DIYGridLayout;
import mclachlan.maze.ui.diygui.render.maze.MazeRendererFactory;

/**
 *
 */
public class FormationWidget extends DIYPane implements ActionListener
{
	/** the max nr of PCs that can be in each row */
	private static final int MAX_PCS_PER_ROW = 4;
	private DIYLabel[] labels = new DIYLabel[2*MAX_PCS_PER_ROW];
	private DIYLabel selected;
	private int formation;
	private List<PlayerCharacter> actors;

	/*-------------------------------------------------------------------------*/
	public FormationWidget(Rectangle bounds, PlayerParty party)
	{
		super(bounds);
		this.actors = party.getPlayerCharacters();
		this.formation = party.getFormation();
		this.buildGui();
		refresh(party.getFormation());
		setSelected(0);
	}

	/*-------------------------------------------------------------------------*/
	private void refresh(int formation)
	{
		for (DIYLabel label : labels)
		{
			label.setText("");
		}

		// front row
		for (int i=0; i<formation; i++)
		{
			labels[i].setText(actors.get(i).getName());
		}

		// back row
		for (int i=formation; i<actors.size(); i++)
		{
			labels[MAX_PCS_PER_ROW+i-formation].setText(actors.get(i).getName());
		}
	}

	/*-------------------------------------------------------------------------*/
	private void buildGui()
	{
		int inset = 1;
		this.setLayoutManager(new DIYGridLayout(1, labels.length+2, inset, inset));
		for (int i = 0; i < labels.length; i++)
		{
			labels[i] = new DIYLabel();
			labels[i].addActionListener(this);
		}

		// front row
		DIYLabel fr = new DIYLabel("Front Row");
		fr.setForegroundColour(Color.WHITE);
		this.add(fr);
		for (int i = 0; i < labels.length/2; i++)
		{
			this.add(labels[i]);
		}

		// back row
		DIYLabel br = new DIYLabel("Back Row");
		br.setForegroundColour(Color.WHITE);
		this.add(br);
		for (int i = labels.length/2; i < labels.length; i++)
		{
			this.add(labels[i]);
		}
	}

	/*-------------------------------------------------------------------------*/
	void moveDown()
	{
		int charIndex = getCharIndex();

		if (charIndex == actors.size()-1)
		{
			// last character, cannot move down
			return;
		}

		if (charIndex == formation-1 
			&& actors.size()-charIndex <= MAX_PCS_PER_ROW
			&& charIndex > 0)
		{
			// last character in the front row & there is space in back row
			formation--;
			refresh(formation);
			setSelected(charIndex);
			return;
		}

		// move the character within the party
		PlayerCharacter temp = actors.get(charIndex);
		actors.set(charIndex, actors.get(charIndex+1));
		actors.set(charIndex+1, temp);
		refresh(formation);
		setSelected(charIndex+1);
	}

	/*-------------------------------------------------------------------------*/
	void moveUp()
	{
		int charIndex = getCharIndex();

		if (charIndex == 0)
		{
			// first character, cannot move up
			return;
		}

		if (charIndex == formation && charIndex < MAX_PCS_PER_ROW)
		{
			// last character in the front row & there is space in front row
			formation++;
			refresh(formation);
			setSelected(charIndex);
			return;
		}

		// move the character within the party
		PlayerCharacter temp = actors.get(charIndex);
		actors.set(charIndex, actors.get(charIndex-1));
		actors.set(charIndex-1, temp);
		refresh(formation);
		setSelected(charIndex-1);
	}
	
	/*-------------------------------------------------------------------------*/
	public void moveSelectionUp()
	{
		int index;
		
		if (selected == null)
		{
			index = labels.length;
		}
		else
		{
			index = getSelectedIndex();
		}
		
		while (index > 0)
		{
			index--;
			if (!labels[index].getText().equals(""))
			{
				setSelected(labels[index]);
				return;
			}
		}
	}
	
	/*-------------------------------------------------------------------------*/
	public void moveSelectionDown()
	{
		int index;
		
		if (selected == null)
		{
			index = 0;
		}
		else
		{
			index = getSelectedIndex();
		}
		
		while (index < labels.length-1)
		{
			index++;
			if (!labels[index].getText().equals(""))
			{
				setSelected(labels[index]);
				return;
			}
		}
	}
	
	/*-------------------------------------------------------------------------*/
	private int getSelectedIndex()
	{
		for (int i = 0; i < labels.length; i++)
		{
			if (labels[i] == selected)
			{
				return i;
			}
		}
		
		return -1;
	}

	/*-------------------------------------------------------------------------*/
	private void setSelected(int charIndex)
	{
		String name = actors.get(charIndex).getName();
		for (DIYLabel label : labels)
		{
			// bit of a hack here
			label.setForegroundColour(Color.LIGHT_GRAY);
			if (label.getText().equals(name))
			{
				selected = label;
				label.setForegroundColour(Color.DARK_GRAY);
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	private void setSelected(DIYLabel label)
	{
		for (DIYLabel l : labels)
		{
			// bit of a hack here
			l.setForegroundColour(Color.LIGHT_GRAY);
		}

		selected = label;
		label.setForegroundColour(Color.DARK_GRAY);
	}

	/*-------------------------------------------------------------------------*/
	private int getCharIndex()
	{
		int charIndex = 0;
		for (int i = 0; i < actors.size(); i++)
		{
			if (actors.get(i).getName().equals(selected.getText()))
			{
				charIndex = i;
				break;
			}
		}
		return charIndex;
	}

	/*-------------------------------------------------------------------------*/

	@Override
	public String getWidgetName()
	{
		return MazeRendererFactory.FORMATION_WIDGET;
	}

	/*-------------------------------------------------------------------------*/
	public boolean actionPerformed(ActionEvent event)
	{
		Object obj = event.getSource();
		if (obj instanceof DIYLabel)
		{
			DIYLabel label = (DIYLabel)obj;
			String text = label.getText();
			if (!text.equals(""))
			{
				setSelected(label);
				return true;
			}
		}

		return false;
	}

	/*-------------------------------------------------------------------------*/
	public DIYLabel getSelected()
	{
		return selected;
	}

	/*-------------------------------------------------------------------------*/
	public List<PlayerCharacter> getActors()
	{
		return actors;
	}

	/*-------------------------------------------------------------------------*/
	public int getFormation()
	{
		return formation;
	}
}
