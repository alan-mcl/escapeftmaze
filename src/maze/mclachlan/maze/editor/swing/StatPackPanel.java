
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
import java.awt.GridLayout;
import java.util.*;
import javax.swing.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.stat.CharacterClass;
import mclachlan.maze.stat.Race;
import mclachlan.maze.stat.StatModifier;
import mclachlan.maze.stat.Stats;

/**
 *
 */
public class StatPackPanel extends JPanel
{
	private static Map<String, StatModifier> statPacks =
		new HashMap<String, StatModifier>();

	JList list;
	JRadioButton straight, multByMinLevel, multByAvgLevel, multByMaxLevel;
	JRadioButton add, set;

	/*-------------------------------------------------------------------------*/
	public StatPackPanel()
	{
		setLayout(new BorderLayout(10,10));

		list = new JList(getStatPacks());

		JPanel radio1 = new JPanel(new GridLayout(4,1));
		ButtonGroup bg1 = new ButtonGroup();
		straight = new JRadioButton("Straight");
		multByMinLevel = new JRadioButton("Multiply this by min lvl");
		multByAvgLevel = new JRadioButton("Multiply this by avg lvl");
		multByMaxLevel = new JRadioButton("Multiply this by max lvl");
		radio1.add(straight);
		radio1.add(multByMinLevel);
		radio1.add(multByAvgLevel);
		radio1.add(multByMaxLevel);
		bg1.add(straight);
		bg1.add(multByMinLevel);
		bg1.add(multByAvgLevel);
		bg1.add(multByMaxLevel);
		radio1.setBorder(BorderFactory.createEtchedBorder());

		JPanel radio2 = new JPanel(new GridLayout(2,1,10,10));
		ButtonGroup bg2 = new ButtonGroup();
		add = new JRadioButton("Add this to stats");
		set = new JRadioButton("Set stats to this");
		radio2.add(add);
		radio2.add(set);
		bg2.add(add);
		bg2.add(set);
		radio2.setBorder(BorderFactory.createEtchedBorder());

		JPanel left = new JPanel(new GridLayout(2,1));
		left.add(radio1);
		left.add(radio2);

		add(new JScrollPane(list), BorderLayout.CENTER);
		add(left, BorderLayout.EAST);

		add.setSelected(true);
		straight.setSelected(true);
	}

	/*-------------------------------------------------------------------------*/
	private Vector<String> getStatPacks()
	{
		Vector<String> vec = new Vector<String>(statPacks.keySet());
		Collections.sort(vec);
		return vec;
	}

	/*-------------------------------------------------------------------------*/
	List<StatModifier> getSelectedStatPack()
	{
		List<StatModifier> result = new ArrayList<StatModifier>();
		Object[] selected = list.getSelectedValues();

		for (Object obj : selected)
		{
			String s = (String)obj;
			if (RACE.equals(s))
			{
				Object[] races = ((List<String>)new ArrayList<>(Database.getInstance().getRaces().keySet())).toArray();
				Arrays.sort(races);
				String option = (String)JOptionPane.showInputDialog(
					this,
					"Choose a race",
					"Choose a race",
					JOptionPane.QUESTION_MESSAGE,
					null,
					races,
					races[0]);

				if (option != null)
				{
					Race race = Database.getInstance().getRace(option);
					result.add(race.getStartingModifiers());
				}
			}
			else if (CHARACTER_CLASS.equals(s))
			{
				Object[] classes = ((List<String>)new ArrayList<>(Database.getInstance().getCharacterClasses().keySet())).toArray();
				Arrays.sort(classes);
				String option = (String)JOptionPane.showInputDialog(
					this,
					"Choose a class",
					"Choose a class",
					JOptionPane.QUESTION_MESSAGE,
					null,
					classes,
					classes[0]);

				if (option != null)
				{
					CharacterClass cc = Database.getInstance().getCharacterClass(option);
					result.add(cc.getStartingModifiers());
				}
			}
			else
			{
				result.add(statPacks.get(s));
			}
		}
		
		return result;
	}

	/*-------------------------------------------------------------------------*/
	BitSet getSelectedMode()
	{
		BitSet result = new BitSet();

		if (straight.isSelected()) result.set(Mode.STRAIGHT);
		if (multByMinLevel.isSelected()) result.set(Mode.MULT_BY_MIN_LVL);
		if (multByAvgLevel.isSelected()) result.set(Mode.MULT_BY_AVG_LVL);
		if (multByMaxLevel.isSelected()) result.set(Mode.MULT_BY_MAX_LVL);
		if (add.isSelected()) result.set(Mode.ADD);
		if (set.isSelected()) result.set(Mode.SET);

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public static class Mode
	{
		public static final int STRAIGHT = 0;
		public static final int MULT_BY_MIN_LVL = 1;
		public static final int MULT_BY_AVG_LVL = 2;
		public static final int MULT_BY_MAX_LVL = 3;
		public static final int ADD = 4;
		public static final int SET = 5;
	}

	private static String RACE = "*Character Race Stats*";
	private static String CHARACTER_CLASS = "*Character Class Stats*";

	/*-------------------------------------------------------------------------*/
	static
	{
		// todo: should be configurable in the database

		statPacks.put(RACE, null);
		statPacks.put(CHARACTER_CLASS, null);

		StatModifier zero = new StatModifier();
		statPacks.put("All Zeros", zero);

		StatModifier plantProperties = new StatModifier();
		plantProperties.setModifier(Stats.Modifier.IMMUNE_TO_POISON, 1);
		plantProperties.setModifier(Stats.Modifier.IMMUNE_TO_BLIND, 1);
		plantProperties.setModifier(Stats.Modifier.IMMUNE_TO_DISEASE, 1);
		plantProperties.setModifier(Stats.Modifier.IMMUNE_TO_INSANE, 1);
		plantProperties.setModifier(Stats.Modifier.IMMUNE_TO_NAUSEA, 1);
		plantProperties.setModifier(Stats.Modifier.RESIST_FIRE, -5);
		statPacks.put("Plant properties", plantProperties);

		StatModifier gnome = new StatModifier();
		gnome.setModifier(Stats.Modifier.THIEVING, 1);
		gnome.setModifier(Stats.Modifier.SNEAKING, 1);
		gnome.setModifier(Stats.Modifier.POWER, 1);
		gnome.setModifier(Stats.Modifier.DUNGEONEER, 1);
		gnome.setModifier(Stats.Modifier.POSTURE, 1);
		gnome.setModifier(Stats.Modifier.RHYME, 1);
		gnome.setModifier(Stats.Modifier.ALCHEMIC, 1);
		gnome.setModifier(Stats.Modifier.HERBAL, 1);
		statPacks.put("Gnome properties", gnome);

		StatModifier gnoll = new StatModifier();
		gnoll.setModifier(Stats.Modifier.BRAWN, 1);
		gnoll.setModifier(Stats.Modifier.SNEAKING, 1);
		gnoll.setModifier(Stats.Modifier.WILDERNESS_LORE, 1);
		gnoll.setModifier(Stats.Modifier.SURVIVAL, 1);
		statPacks.put("Gnoll properties", gnoll);

		StatModifier leonal = new StatModifier();
		leonal.setModifier(Stats.Modifier.BRAWN, 1);
		leonal.setModifier(Stats.Modifier.SKILL, 1);
		leonal.setModifier(Stats.Modifier.ATTACK, 1);
		leonal.setModifier(Stats.Modifier.CHIVALRY, 1);
		leonal.setModifier(Stats.Modifier.CHANT, 1);
		leonal.setModifier(Stats.Modifier.POSTURE, 1);
		leonal.setModifier(Stats.Modifier.VS_AMBUSH, 1);
		statPacks.put("Leonal properties", leonal);

		StatModifier undead = new StatModifier();
		undead.setModifier(Stats.Modifier.IMMUNE_TO_POISON, 1);
		undead.setModifier(Stats.Modifier.IMMUNE_TO_DISEASE, 1);
		undead.setModifier(Stats.Modifier.IMMUNE_TO_INSANE, 1);
		undead.setModifier(Stats.Modifier.IMMUNE_TO_NAUSEA, 1);
		undead.setModifier(Stats.Modifier.IMMUNE_TO_DISEASE, 1);
		undead.setModifier(Stats.Modifier.IMMUNE_TO_IRRITATE, 1);
		undead.setModifier(Stats.Modifier.IMMUNE_TO_POSSESSION, 1);
		undead.setModifier(Stats.Modifier.IMMUNE_TO_PSYCHIC, 1);
		undead.setModifier(Stats.Modifier.IMMUNE_TO_SLEEP, 1);
		undead.setModifier(Stats.Modifier.IMMUNE_TO_PARALYSE, 1);
		undead.setModifier(Stats.Modifier.IMMUNE_TO_FEAR, 1);
		statPacks.put("Undead properties", undead);

		StatModifier golem = new StatModifier();
		golem.setModifier(Stats.Modifier.IMMUNE_TO_POISON, 1);
		golem.setModifier(Stats.Modifier.IMMUNE_TO_PSYCHIC, 1);
		golem.setModifier(Stats.Modifier.IMMUNE_TO_DISEASE, 1);
		golem.setModifier(Stats.Modifier.IMMUNE_TO_FEAR, 1);
		golem.setModifier(Stats.Modifier.IMMUNE_TO_INSANE, 1);
		golem.setModifier(Stats.Modifier.IMMUNE_TO_IRRITATE, 1);
		golem.setModifier(Stats.Modifier.IMMUNE_TO_NAUSEA, 1);
		golem.setModifier(Stats.Modifier.IMMUNE_TO_PARALYSE, 1);
		golem.setModifier(Stats.Modifier.IMMUNE_TO_POSSESSION, 1);
		golem.setModifier(Stats.Modifier.IMMUNE_TO_SLEEP, 1);
		golem.setModifier(Stats.Modifier.IMMUNE_TO_STONE, 1);
		statPacks.put("Golem properties", golem);
	}
}
