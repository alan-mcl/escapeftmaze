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

package mclachlan.maze.stat.combat;

import java.util.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.data.StringUtil;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.game.MazeScript;
import mclachlan.maze.stat.ActorActionIntention;
import mclachlan.maze.stat.ActorActionOption;
import mclachlan.maze.stat.PlayerCharacter;
import mclachlan.maze.stat.UnifiedActor;
import mclachlan.maze.stat.combat.event.AnimationEvent;
import mclachlan.maze.ui.diygui.ChooseCharacterCallback;
import mclachlan.maze.ui.diygui.animation.AnimationContext;

/**
 *
 */
public class DisplaceOption extends ActorActionOption
	implements ChooseCharacterCallback
{
	private ActionOptionCallback callback;

	/*-------------------------------------------------------------------------*/

	public DisplaceOption()
	{
		super("Displace", "aao.displace");
	}

	/*-------------------------------------------------------------------------*/

	@Override
	public void select(
		UnifiedActor actor,
		Combat combat,
		ActionOptionCallback callback)
	{
		this.callback = callback;
		Maze.getInstance().getUi().chooseACharacter(this);
		return;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public ActorActionIntention getIntention()
	{
		return null;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public boolean characterChosen(PlayerCharacter target, int targetIndex)
	{
		UnifiedActor displacer = this.getActor();

		if (displacer.getActionPoints().getCurrent() < 2)
		{
			Maze.getInstance().getUi().addMessage(
				StringUtil.getEventText("msg.cannot.displace.ap", displacer.getDisplayName()));
			return true;
		}

		if (displacer.getCombatantData().isDisplaced())
		{
			Maze.getInstance().getUi().addMessage(
				StringUtil.getEventText("msg.already.displaced", displacer.getDisplayName()));
			return true;
		}

		displacer.getActionPoints().decCurrent(2);

		MazeScript script = Database.getInstance().getMazeScripts().get("blue portrait animation");
		AnimationContext animationContext = new AnimationContext(displacer);
		animationContext.addTarget(displacer);
		animationContext.addTarget(target);
		for (MazeEvent e : script.getEvents())
		{
			if (e instanceof AnimationEvent)
			{
				((AnimationEvent)e).setAnimationContext(animationContext);
			}
		}
		Maze.getInstance().appendEvents(script.getEvents());

		List<PlayerCharacter> actors = new ArrayList<>(
			Maze.getInstance().getParty().getPlayerCharacters());

		int displacerIndex = Maze.getInstance().getParty().getPlayerCharacterIndex((PlayerCharacter)displacer);

		actors.set(targetIndex, (PlayerCharacter)displacer);
		actors.set(displacerIndex, target);

		Maze.getInstance().reorderParty(actors, Maze.getInstance().getParty().getFormation());
		displacer.getCombatantData().setDisplaced(true);
		return true;
	}

	@Override
	public void afterCharacterChosen()
	{

	}

	/*-------------------------------------------------------------------------*/

	@Override
	public String toString()
	{
		return StringUtil.getUiLabel(getDisplayName());
	}
}
