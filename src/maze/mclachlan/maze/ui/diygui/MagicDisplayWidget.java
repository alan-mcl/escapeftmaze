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
import mclachlan.maze.stat.PlayerCharacter;
import mclachlan.diygui.DIYPane;

/**
 *
 */
public class MagicDisplayWidget extends DIYPane
{
	SpellDisplayWidget sdw;

	// todo
	private ManaDisplayWidget mana = new ManaDisplayWidget();

	/*-------------------------------------------------------------------------*/
	public MagicDisplayWidget(Rectangle bounds)
	{
		super(bounds);
		sdw = new SpellDisplayWidget(null, bounds);
		sdw.setBounds(bounds);
		this.add(sdw);
	}

/*
	mana.refresh(
		character.getAmountRedMagic(),
		character.getAmountBlackMagic(),
		character.getAmountPurpleMagic(),
		character.getAmountGoldMagic(),
		character.getAmountWhiteMagic(),
		character.getAmountGreenMagic(),
		character.getAmountBlueMagic());
*/

	
	/*-------------------------------------------------------------------------*/
	public void setCharacter(PlayerCharacter pc)
	{
		sdw.setPlayerCharacter(pc);
	}

	/*-------------------------------------------------------------------------*/
	public void setBounds(Rectangle r)
	{
		super.setBounds(r);
		sdw.setBounds(r);
	}
}
