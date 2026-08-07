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

package mclachlan.maze.game.event;

import java.awt.Point;
import java.util.*;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.game.PartyCamp;
import mclachlan.maze.stat.PlayerCharacter;
import mclachlan.maze.ui.diygui.animation.FadeToBlackAnimation;

/**
 * Fades to black after a field-party wipe, then rebuilds the party from a camp.
 * <p>
 * Sequence: start fade ({@link Delay#WAIT_ON_CLICK}) → after fade wait, a queued
 * complete-phase instance rebuilds the party. The complete instance is appended
 * during fade resolve so {@code alreadyQueued(ResumeFromPartyCampEvent.class)}
 * stays true while the fade waits (later wipe checks cannot clear/restart).
 * <p>
 * Important: the fade instance must keep {@code phase == FADE} through
 * {@link #getDelay()} (called after {@link #resolve()}), otherwise the fade
 * wait is skipped and the party refreshes before the fade finishes.
 */
public class ResumeFromPartyCampEvent extends MazeEvent
{
	private enum Phase
	{
		FADE,
		COMPLETE
	}

	private final PartyCamp camp;
	private final List<PlayerCharacter> campPcs;
	private final int gold;
	private final int supplies;
	private final int formation;
	private final String resumeZone;
	private final Point resumeTile;
	private final Phase phase;

	/*-------------------------------------------------------------------------*/
	public ResumeFromPartyCampEvent(
		PartyCamp camp,
		List<PlayerCharacter> campPcs,
		int gold,
		int supplies,
		int formation,
		String resumeZone,
		Point resumeTile)
	{
		this(camp, campPcs, gold, supplies, formation, resumeZone, resumeTile, Phase.FADE);
	}

	/*-------------------------------------------------------------------------*/
	private ResumeFromPartyCampEvent(
		PartyCamp camp,
		List<PlayerCharacter> campPcs,
		int gold,
		int supplies,
		int formation,
		String resumeZone,
		Point resumeTile,
		Phase phase)
	{
		this.camp = camp;
		this.campPcs = new ArrayList<>(campPcs);
		this.gold = gold;
		this.supplies = supplies;
		this.formation = formation;
		this.resumeZone = resumeZone;
		this.resumeTile = new Point(resumeTile);
		this.phase = phase;
	}

	@Override
	public boolean shouldCheckPartyStatus()
	{
		return false;
	}

	@Override
	public int getDelay()
	{
		// Must remain WAIT_ON_CLICK after resolve() for the fade instance —
		// resolveEvent reads delay after resolve() returns.
		return phase == Phase.FADE ? Delay.WAIT_ON_CLICK : Delay.NONE;
	}

	@Override
	public List<MazeEvent> resolve()
	{
		Maze maze = Maze.getInstance();

		if (phase == Phase.FADE)
		{
			maze.startAnimation(
				new FadeToBlackAnimation(1500),
				maze.getEventMutex(),
				null);

			// Queue rebuild for after this event's fade wait. Keep this instance
			// as FADE so getDelay() still returns WAIT_ON_CLICK.
			maze.appendEvents(new ResumeFromPartyCampEvent(
				camp, campPcs, gold, supplies, formation, resumeZone, resumeTile,
				Phase.COMPLETE));
			return null;
		}

		return maze.completePartyResumeFromCamp(
			camp, campPcs, gold, supplies, formation, resumeZone, resumeTile);
	}
}
