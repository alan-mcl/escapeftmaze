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
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import mclachlan.maze.stat.ItemTemplate;

/**
 *
 */
public class AmmoTypeComponent extends JPanel implements ActionListener
{
	int dirtyFlag;
	JCheckBox arrow, axe, bolt, dart, hammer, javelin, knife, self, shot, star, stone;

	/*-------------------------------------------------------------------------*/
	public AmmoTypeComponent(String title, int dirtyFlag)
	{
		this.dirtyFlag = dirtyFlag;

		setLayout(new GridLayout(4,1,2,2));

		arrow = new JCheckBox("Arrows");
		arrow.addActionListener(this);

		axe = new JCheckBox("Axes");
		axe.addActionListener(this);

		bolt = new JCheckBox("Bolts");
		bolt.addActionListener(this);

		dart = new JCheckBox("Darts");
		dart.addActionListener(this);

		hammer = new JCheckBox("Hammers");
		hammer.addActionListener(this);

		javelin = new JCheckBox("Javelins");
		javelin.addActionListener(this);

		knife = new JCheckBox("Knives");
		knife.addActionListener(this);

		self = new JCheckBox("Self");
		self.addActionListener(this);

		shot = new JCheckBox("Shot");
		shot.addActionListener(this);

		stone = new JCheckBox("Stones");
		stone.addActionListener(this);

		star = new JCheckBox("Stars");
		star.addActionListener(this);

		this.add(arrow);
		this.add(axe);
		this.add(bolt);
		this.add(dart);
		this.add(hammer);
		this.add(javelin);
		this.add(knife);
		this.add(self);
		this.add(shot);
		this.add(stone);
		this.add(star);

		this.setBorder(BorderFactory.createTitledBorder(title));
	}

	/*-------------------------------------------------------------------------*/
	public void refresh(List<ItemTemplate.AmmoType> ammoTypes)
	{
		if (ammoTypes == null)
		{
			arrow.setSelected(false);
			axe.setSelected(false);
			bolt.setSelected(false);
			dart.setSelected(false);
			hammer.setSelected(false);
			javelin.setSelected(false);
			knife.setSelected(false);
			self.setSelected(false);
			shot.setSelected(false);
			stone.setSelected(false);
			star.setSelected(false);
		}
		else
		{
			arrow.setSelected(ammoTypes.contains(ItemTemplate.AmmoType.ARROW));
			axe.setSelected(ammoTypes.contains(ItemTemplate.AmmoType.AXE));
			bolt.setSelected(ammoTypes.contains(ItemTemplate.AmmoType.BOLT));
			dart.setSelected(ammoTypes.contains(ItemTemplate.AmmoType.DART));
			hammer.setSelected(ammoTypes.contains(ItemTemplate.AmmoType.HAMMER));
			javelin.setSelected(ammoTypes.contains(ItemTemplate.AmmoType.JAVELIN));
			knife.setSelected(ammoTypes.contains(ItemTemplate.AmmoType.KNIFE));
			self.setSelected(ammoTypes.contains(ItemTemplate.AmmoType.SELF));
			shot.setSelected(ammoTypes.contains(ItemTemplate.AmmoType.SHOT));
			stone.setSelected(ammoTypes.contains(ItemTemplate.AmmoType.STONE));
			star.setSelected(ammoTypes.contains(ItemTemplate.AmmoType.STAR));
		}
	}

	/*-------------------------------------------------------------------------*/
	public List<ItemTemplate.AmmoType> getAmmoTypes()
	{
		List<ItemTemplate.AmmoType> result = new ArrayList<>();
		if (arrow.isSelected()) result.add(ItemTemplate.AmmoType.ARROW);
		if (axe.isSelected()) result.add(ItemTemplate.AmmoType.AXE);
		if (bolt.isSelected()) result.add(ItemTemplate.AmmoType.BOLT);
		if (dart.isSelected()) result.add(ItemTemplate.AmmoType.DART);
		if (hammer.isSelected()) result.add(ItemTemplate.AmmoType.HAMMER);
		if (javelin.isSelected()) result.add(ItemTemplate.AmmoType.JAVELIN);
		if (knife.isSelected()) result.add(ItemTemplate.AmmoType.KNIFE);
		if (self.isSelected()) result.add(ItemTemplate.AmmoType.SELF);
		if (shot.isSelected()) result.add(ItemTemplate.AmmoType.SHOT);
		if (stone.isSelected()) result.add(ItemTemplate.AmmoType.STONE);
		if (star.isSelected()) result.add(ItemTemplate.AmmoType.STAR);
		return result;
	}

	/*-------------------------------------------------------------------------*/
	public void actionPerformed(ActionEvent e)
	{
		SwingEditor.instance.setDirty(dirtyFlag);
	}
}
